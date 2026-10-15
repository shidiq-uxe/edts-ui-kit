package id.co.edtslib.edtsuikit

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.core.view.doOnNextLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesTmhBinding
import id.co.edtslib.edtsuikit.databinding.ItemTmhFairBinding
import id.co.edtslib.edtsuikit.databinding.ItemTmhProductPreviewBinding
import id.co.edtslib.edtsuikit.databinding.SkeletonTmhFairBinding
import id.co.edtslib.edtsuikit.tmh.TmhFair
import id.co.edtslib.edtsuikit.tmh.TmhFairState
import id.co.edtslib.edtsuikit.tmh.TmhFixtures
import id.co.edtslib.edtsuikit.tmh.TmhProduct
import id.co.edtslib.edtsuikit.tmh.TmhRedemptionBottomSheet
import id.co.edtslib.edtsuikit.tmh.TmhScenario
import id.co.edtslib.edtsuikit.tmh.TmhScenarioBottomSheet
import id.co.edtslib.edtsuikit.tmh.bindFair
import id.co.edtslib.edtsuikit.tmh.originalPriceFlags
import id.co.edtslib.edtsuikit.tmh.quantities
import id.co.edtslib.edtsuikit.tmh.setUnavailable
import id.co.edtslib.edtsuikit.tmh.tmhTextAppearance
import id.co.edtslib.edtsuikit.tmh.tmhTypography
import id.co.edtslib.edtsuikit.tmh.toQuantityBundle
import id.co.edtslib.uikit.adapter.BaseAdapter
import id.co.edtslib.uikit.alert.AlertBoxDelegate
import id.co.edtslib.uikit.button.Button
import id.co.edtslib.uikit.tablayout.QuadRoundTabLayout
import id.co.edtslib.uikit.tablayout.QuadRoundTabLayoutDelegate
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.snack
import id.co.edtslib.uikit.R as UIKitR

class GuidelinesTmhActivity : GuidelinesBaseActivity() {

    override val statusBarScrimColor: Int
        get() = color(UIKitR.color.primary_30)

    override val isLightStatusBar: Boolean
        get() = false

    private val binding by viewBinding<ActivityGuidelinesTmhBinding>()
    private val handler = Handler(Looper.getMainLooper())
    private var scenario = TmhScenario.DEFAULT
    private var fairs = TmhFixtures.fairs(scenario)
    private var selectedTab = 0
    private var scrollTarget: Int? = null
    private var pendingLoad: Runnable? = null
    private var errorSnackbar: Snackbar? = null
    private var restoredListState: Parcelable? = null
    private var isUpdatingFairTabs = false
    private var tabFairIds = emptyList<String>()
    private var isLoading = false
    private var isRestoringPosition = false
    private var loadingAnchor: FairAnchor? = null
    private var pendingSuccessMessage: String? = null
    private val fairsLayoutManager by lazy { LinearLayoutManager(this) }

    private val fairAdapter by lazy {
        BaseAdapter.adapterOf<TmhFair, ItemTmhFairBinding>(
            register = BaseAdapter.Register(onBindHolder = { _, fair, row, _ -> bindFairCard(fair, row) }),
            diff = BaseAdapter.Diff(
                areItemsTheSame = { old, new -> old.id == new.id },
                areContentsTheSame = { old, new -> old == new },
            ),
        ).also { adapter ->
            adapter.setOnItemClickListener { _, fair, _ -> openFair(fair) }
            adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onChanged() { restoreScrollPosition() }
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) { restoreScrollPosition() }
                override fun onItemRangeChanged(positionStart: Int, itemCount: Int) { restoreScrollPosition() }
                override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) { restoreScrollPosition() }
            })
        }
    }

    private fun setupNavigation() {
        binding.toolbar.navigationContentDescription = getString(R.string.tmh_back)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.tvTitle.setOnLongClickListener {
            if (supportFragmentManager.findFragmentByTag(TmhScenarioBottomSheet.TAG) == null) {
                TmhScenarioBottomSheet.newInstance(scenario).show(supportFragmentManager, TmhScenarioBottomSheet.TAG)
            }
            true
        }
        binding.ivCart.setOnClickListener {
            binding.root.snack(getString(R.string.tmh_cart_summary, TmhFixtures.BASE_CART_QUANTITY + fairs.sumOf { it.savedTotal }), resTextAppearance = R.style.TextAppearance_Tmh_Regular_B3)
        }
        binding.btnAllPromos.buttonType = Button.ButtonType.FILLED_MEDIUM
        binding.btnAllPromos.tmhTextAppearance(R.style.TextAppearance_Tmh_SemiBold_Button_Text_Medium, 16)
        binding.btnAllPromos.setOnClickListener { selectScenario(TmhScenario.DEFAULT) }
        binding.promotionTabs.apply {
            scrollMode = QuadRoundTabLayout.MODE_FIXED
            tabTextAppearance = R.style.TextAppearance_Tmh_Medium_B3
            activeTextAppearance = R.style.TextAppearance_Tmh_Semibold_H2
            delegate = object : QuadRoundTabLayoutDelegate {
                override fun onTabSelected(position: Int, view: QuadRoundTabLayout.QuadRoundTabView, item: QuadRoundTabLayout.TabItem) = Unit
                override fun onPreventSelected(position: Int, view: QuadRoundTabLayout.QuadRoundTabView, item: QuadRoundTabLayout.TabItem): Boolean =
                    position == 1
            }
            doOnLayout { tmhTypography() }
            setTabs(listOf(
                QuadRoundTabLayout.TabItem(getString(R.string.tmh_tebus)),
                QuadRoundTabLayout.TabItem(getString(R.string.tmh_hadiah)),
            ))
        }
    }

    private fun setupLists() {
        binding.fairTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (!isUpdatingFairTabs) scrollToFair(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) = Unit

            override fun onTabReselected(tab: TabLayout.Tab) {
                if (!isUpdatingFairTabs) scrollToFair(tab.position)
            }
        })
        binding.fairTabs.doOnLayout { binding.fairTabs.tmhTypography() }
        binding.rvFairs.layoutManager = fairsLayoutManager
        binding.rvFairs.adapter = fairAdapter
        binding.rvFairs.itemAnimator = null
        binding.rvFairs.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (scrollTarget == null) selectVisibleFair()
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) scrollTarget = null
                if (newState == RecyclerView.SCROLL_STATE_IDLE && scrollTarget != null) {
                    scrollTarget = null
                    selectVisibleFair()
                }
            }
        })
        binding.refreshLayout.setColorSchemeResources(UIKitR.color.primary_30)
        binding.refreshLayout.setOnRefreshListener {
            errorSnackbar?.dismiss()
            errorSnackbar = null
            showLoading { renderFairs() }
        }
    }

    private fun bindFairCard(fair: TmhFair, row: ItemTmhFairBinding) {
        row.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = if (fair.id == fairs.lastOrNull()?.id) 0 else 12.dp.toInt()
        }
        row.root.tmhTypography()
        row.tvFairName.setText(fair.nameRes)
        row.ivBanner.setImageResource(fair.bannerRes)
        row.ivBanner.contentDescription = getString(fair.nameRes)
        row.ivBanner.setUnavailable(fair.state.isUnavailable)
        row.alertBox.bindFair(fair)
        row.alertBox.delegate = object : AlertBoxDelegate {
            override fun onCloseClickListener(view: View) = Unit
            override fun onButtonClickListener(view: View) = Unit
            override fun onActionClickListener(view: View) { openFair(fair) }
        }
        row.rvProducts.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        row.rvProducts.adapter = BaseAdapter.adapterOf<TmhProduct, ItemTmhProductPreviewBinding>(
            register = BaseAdapter.Register(onBindHolder = { _, product, productRow, _ ->
                bindProductPreview(product, fair, productRow)
            }),
            diff = BaseAdapter.Diff(
                areItemsTheSame = { old, new -> old.id == new.id },
                areContentsTheSame = { old, new -> old == new },
            ),
            itemList = fair.products,
        ).also { adapter -> adapter.setOnItemClickListener { _, _, _ -> openFair(fair) } }
    }

    private fun bindProductPreview(product: TmhProduct, fair: TmhFair, row: ItemTmhProductPreviewBinding) {
        val isUnavailable = fair.state.isUnavailable || fair.state == TmhFairState.REQUIREMENT_UNMET || !product.isInStock
        row.root.tmhTypography()
        row.ivProduct.setImageResource(product.imageRes)
        row.ivProduct.setUnavailable(isUnavailable)
        row.tvName.setText(product.nameRes)
        row.tvPrice.setText(product.priceRes)
        row.tvOriginalPrice.setText(product.originalPriceRes)
        row.tvOriginalPrice.paintFlags = originalPriceFlags()
        row.tvName.setTextColor(color(if (isUnavailable) UIKitR.color.black_40 else UIKitR.color.black_70))
        row.tvPrice.setTextColor(color(if (isUnavailable) UIKitR.color.black_40 else UIKitR.color.red_30))
        row.tvDiscount.setTextColor(color(if (isUnavailable) UIKitR.color.black_40 else UIKitR.color.red_30))
        row.tvDiscount.backgroundTintList = ColorStateList.valueOf(color(if (isUnavailable) UIKitR.color.black_30 else UIKitR.color.red_10))
        row.ivSelected.isVisible = (fair.savedQuantities[product.id] ?: 0) > 0
        row.root.strokeColor = color(if (row.ivSelected.isVisible) UIKitR.color.primary_30 else UIKitR.color.black_30)
    }

    private fun openFair(fair: TmhFair) {
        if (supportFragmentManager.findFragmentByTag(TmhRedemptionBottomSheet.TAG) != null) return
        TmhRedemptionBottomSheet.newInstance(fair, scenario).show(supportFragmentManager, TmhRedemptionBottomSheet.TAG)
    }

    private fun updateFairTabs() {
        val ids = fairs.map { it.id }
        if (tabFairIds == ids) return
        isUpdatingFairTabs = true
        try {
            binding.fairTabs.removeAllTabs()
            fairs.forEach { fair ->
                binding.fairTabs.addTab(binding.fairTabs.newTab().setText(fair.nameRes), false)
            }
            tabFairIds = ids
            binding.fairTabs.tmhTypography()
        } finally {
            isUpdatingFairTabs = false
        }
    }

    private fun selectTab(position: Int) {
        if (position !in fairs.indices) return
        selectedTab = position
        if (binding.fairTabs.selectedTabPosition == position) return
        val tab = binding.fairTabs.getTabAt(position) ?: return
        isUpdatingFairTabs = true
        try {
            binding.fairTabs.selectTab(tab)
        } finally {
            isUpdatingFairTabs = false
        }
    }

    private fun scrollToFair(position: Int) {
        if (position !in fairs.indices || binding.rvFairs.adapter !== fairAdapter) return
        binding.rvFairs.stopScroll()
        scrollTarget = position
        selectTab(position)
        val scroller = object : LinearSmoothScroller(this) {
            override fun getVerticalSnapPreference(): Int = SNAP_TO_START
        }
        scroller.targetPosition = position
        fairsLayoutManager.startSmoothScroll(scroller)
    }

    private fun selectVisibleFair() {
        if (isLoading || isRestoringPosition || binding.rvFairs.adapter !== fairAdapter || fairs.isEmpty()) return
        val first = fairsLayoutManager.findFirstVisibleItemPosition()
        if (first == RecyclerView.NO_POSITION) return
        val atBottom = !binding.rvFairs.canScrollVertically(1) && binding.rvFairs.canScrollVertically(-1)
        selectTab(if (atBottom) fairs.lastIndex else first)
    }

    private fun captureFairAnchor(): FairAnchor? {
        if (binding.rvFairs.adapter !== fairAdapter) return null
        val first = fairsLayoutManager.findFirstVisibleItemPosition()
        val fair = fairAdapter.items.getOrNull(first) ?: return null
        val card = fairsLayoutManager.findViewByPosition(first) ?: return null
        return FairAnchor(fair.id, fairsLayoutManager.getDecoratedTop(card) - binding.rvFairs.paddingTop)
    }

    private fun restoreScrollPosition() {
        if (isLoading || binding.rvFairs.adapter !== fairAdapter || fairAdapter.items != fairs) return
        val anchor = loadingAnchor
        val state = restoredListState
        loadingAnchor = null
        restoredListState = null
        isRestoringPosition = true
        if (anchor != null) {
            val index = fairs.indexOfFirst { it.id == anchor.id }.coerceAtLeast(0)
            fairsLayoutManager.scrollToPositionWithOffset(index, anchor.offset)
        } else if (state != null) {
            fairsLayoutManager.onRestoreInstanceState(state)
        }
        binding.rvFairs.doOnNextLayout {
            isRestoringPosition = false
            if (binding.rvFairs.adapter !== fairAdapter || isLoading) return@doOnNextLayout
            selectVisibleFair()
            pendingSuccessMessage?.let { message ->
                pendingSuccessMessage = null
                binding.root.snack(message, resTextAppearance = R.style.TextAppearance_Tmh_Regular_B3)
            }
        }
        binding.rvFairs.requestLayout()
    }

    private fun renderCartCount() {
        val cartTotal = TmhFixtures.BASE_CART_QUANTITY + fairs.sumOf { it.savedTotal }
        binding.tvCartCount.text = getString(R.string.tmh_cart_count, cartTotal)
        binding.ivCart.contentDescription = getString(R.string.tmh_cart_summary, cartTotal)
    }

    private fun renderFairs() {
        isLoading = false
        binding.loadingFairTabs.stopShimmer()
        binding.loadingFairTabs.isVisible = false
        binding.tabsDivider.isVisible = true
        binding.refreshLayout.isRefreshing = false
        binding.refreshLayout.isVisible = true
        binding.emptyState.isVisible = false
        binding.promotionTabs.isVisible = true
        binding.fairTabs.isVisible = true
        binding.rvFairs.adapter = fairAdapter
        fairAdapter.items = fairs
        updateFairTabs()
        selectTab(selectedTab.coerceIn(fairs.indices))
        renderCartCount()
        restoreScrollPosition()
    }

    private fun showLoading(onComplete: () -> Unit) {
        pendingLoad?.let(handler::removeCallbacks)
        if (!isLoading) loadingAnchor = captureFairAnchor() ?: loadingAnchor
        isLoading = true
        isRestoringPosition = false
        binding.refreshLayout.isRefreshing = false
        binding.emptyState.isVisible = false
        binding.refreshLayout.isVisible = true
        binding.promotionTabs.isVisible = true
        binding.fairTabs.visibility = View.INVISIBLE
        binding.tabsDivider.isVisible = true
        binding.loadingFairTabs.isVisible = true
        binding.loadingFairTabs.startShimmer()
        scrollTarget = null
        binding.rvFairs.stopScroll()
        binding.rvFairs.adapter = BaseAdapter.shimmerAdapter<SkeletonTmhFairBinding>(
            register = BaseAdapter.Register(onBindHolder = { position, _, row, _ ->
                row.alertPlaceholder.updateLayoutParams { height = (if (position == 2) 56 else 40).dp.toInt() }
                row.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = if (position == 2) 0 else 12.dp.toInt()
                }
            }),
            size = 3,
        )
        fairsLayoutManager.scrollToPositionWithOffset(0, 0)
        pendingLoad = Runnable {
            pendingLoad = null
            onComplete()
        }.also { handler.postDelayed(it, LOADING_DELAY_MS) }
    }

    private fun selectScenario(selected: TmhScenario) {
        pendingLoad?.let(handler::removeCallbacks)
        pendingLoad = null
        errorSnackbar?.dismiss()
        errorSnackbar = null
        binding.rvFairs.stopScroll()
        scrollTarget = null
        restoredListState = null
        loadingAnchor = null
        pendingSuccessMessage = null
        isRestoringPosition = false
        scenario = selected
        selectedTab = 0
        when (selected) {
            TmhScenario.EMPTY -> {
                isLoading = false
                binding.loadingFairTabs.stopShimmer()
                binding.loadingFairTabs.isVisible = false
                binding.refreshLayout.isRefreshing = false
                binding.refreshLayout.isVisible = false
                binding.fairTabs.isVisible = false
                binding.emptyState.isVisible = true
                binding.tabsDivider.isVisible = false
            }
            TmhScenario.LOADING -> showLoading { selectScenario(TmhScenario.DEFAULT) }
            TmhScenario.ERROR -> {
                renderFairs()
                errorSnackbar = binding.root.snack(
                    message = getString(R.string.tmh_load_error),
                    actionText = getString(R.string.tmh_retry),
                    displayLength = Snackbar.LENGTH_INDEFINITE,
                    backgroundColor = color(UIKitR.color.alert_primary),
                    resTextAppearance = R.style.TextAppearance_Tmh_Regular_B3,
                    action = { selectScenario(TmhScenario.LOADING) },
                )
            }
            else -> {
                fairs = TmhFixtures.fairs(selected)
                renderFairs()
                fairsLayoutManager.scrollToPositionWithOffset(0, 0)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guidelines_tmh)
        binding.root.tmhTypography()
        scenario = TmhScenario.valueOf(savedInstanceState?.getString(STATE_SCENARIO) ?: TmhScenario.DEFAULT.name)
        fairs = TmhFixtures.fairs(scenario).map { fair ->
            savedInstanceState?.getBundle(fair.id)?.let { saved ->
                if (fair.state.isEligible) fair.withSelection(saved.quantities()) else fair
            } ?: fair
        }
        selectedTab = savedInstanceState?.getInt(STATE_TAB) ?: 0
        @Suppress("DEPRECATION")
        restoredListState = savedInstanceState?.getParcelable(STATE_SCROLL)
        loadingAnchor = savedInstanceState?.getString(STATE_ANCHOR_ID)?.let {
            FairAnchor(it, savedInstanceState.getInt(STATE_ANCHOR_OFFSET))
        }
        pendingSuccessMessage = savedInstanceState?.getString(STATE_SUCCESS_MESSAGE)
        setupNavigation()
        setupLists()
        renderCartCount()
        supportFragmentManager.setFragmentResultListener(TmhScenarioBottomSheet.RESULT, this) { _, result ->
            selectScenario(TmhScenario.valueOf(requireNotNull(result.getString(TmhScenarioBottomSheet.KEY_SCENARIO))))
        }
        supportFragmentManager.setFragmentResultListener(TmhRedemptionBottomSheet.RESULT_SAVE, this) { _, result ->
            val id = result.getString(TmhRedemptionBottomSheet.KEY_FAIR_ID)
            val quantities = result.getBundle(TmhRedemptionBottomSheet.KEY_QUANTITIES)?.quantities().orEmpty()
            fairs = fairs.map { if (it.id == id) it.withSelection(quantities) else it }
            pendingSuccessMessage = getString(R.string.tmh_cart_updated)
            showLoading { renderFairs() }
        }
        if (savedInstanceState == null) {
            selectTab(0)
            showLoading { renderFairs() }
        } else if (savedInstanceState.getBoolean(STATE_LOADING) && scenario != TmhScenario.LOADING) {
            showLoading { renderFairs() }
        } else if (scenario == TmhScenario.EMPTY || scenario == TmhScenario.LOADING || scenario == TmhScenario.ERROR) {
            selectScenario(scenario)
        } else {
            renderFairs()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_SCENARIO, scenario.name)
        outState.putInt(STATE_TAB, selectedTab)
        outState.putParcelable(STATE_SCROLL, if (isLoading) restoredListState else fairsLayoutManager.onSaveInstanceState())
        outState.putBoolean(STATE_LOADING, isLoading)
        loadingAnchor?.let {
            outState.putString(STATE_ANCHOR_ID, it.id)
            outState.putInt(STATE_ANCHOR_OFFSET, it.offset)
        }
        outState.putString(STATE_SUCCESS_MESSAGE, pendingSuccessMessage)
        fairs.forEach { outState.putBundle(it.id, it.savedQuantities.toQuantityBundle()) }
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        pendingLoad?.let(handler::removeCallbacks)
        errorSnackbar?.dismiss()
        binding.loadingFairTabs.stopShimmer()
        super.onDestroy()
    }

    private data class FairAnchor(val id: String, val offset: Int)

    companion object {
        private const val STATE_SCENARIO = "tmh_scenario"
        private const val STATE_TAB = "tmh_selected_tab"
        private const val STATE_SCROLL = "tmh_scroll_state"
        private const val STATE_LOADING = "tmh_loading"
        private const val STATE_ANCHOR_ID = "tmh_anchor_id"
        private const val STATE_ANCHOR_OFFSET = "tmh_anchor_offset"
        private const val STATE_SUCCESS_MESSAGE = "tmh_success_message"
        private const val LOADING_DELAY_MS = 1200L
    }
}
