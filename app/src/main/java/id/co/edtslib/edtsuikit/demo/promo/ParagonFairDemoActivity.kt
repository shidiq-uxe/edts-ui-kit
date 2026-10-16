package id.co.edtslib.edtsuikit.demo.promo

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.ActivityNotFoundException
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.net.Uri
import android.view.inputmethod.EditorInfo
import android.view.KeyEvent
import androidx.activity.OnBackPressedCallback
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.TextViewCompat
import androidx.core.view.AccessibilityDelegateCompat
import id.co.edtslib.uikit.utils.window.WindowInsetsConfig
import id.co.edtslib.uikit.searchbar.SearchBarDelegate
import id.co.edtslib.uikit.databinding.SearchBarBinding
import id.co.edtslib.edtsuikit.databinding.ItemFairSearchScopeBinding
import id.co.edtslib.edtsuikit.databinding.ItemFairSearchEmptyBinding
import id.co.edtslib.edtsuikit.databinding.ItemFairSearchLoadingBinding
import id.co.edtslib.edtsuikit.databinding.SheetFairDiscardBinding
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.animation.PathInterpolator
import androidx.core.view.ViewCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import android.app.Dialog
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import id.co.edtslib.edtsuikit.GuidelinesBaseActivity
import id.co.edtslib.edtsuikit.R
import id.co.edtslib.edtsuikit.databinding.ActivityParagonFairDemoBinding
import id.co.edtslib.edtsuikit.databinding.ItemFairCartGiftBinding
import id.co.edtslib.edtsuikit.databinding.ItemFairHeaderBinding
import id.co.edtslib.edtsuikit.databinding.ItemFairOptionBinding
import id.co.edtslib.edtsuikit.databinding.ItemFairProductBinding
import id.co.edtslib.edtsuikit.databinding.ItemFairRedeemProductBinding
import id.co.edtslib.edtsuikit.databinding.ItemFairSectionBinding
import id.co.edtslib.edtsuikit.databinding.SheetFairCartBinding
import id.co.edtslib.edtsuikit.databinding.SheetFairOptionsBinding
import id.co.edtslib.edtsuikit.databinding.SheetFairRedemptionBinding
import id.co.edtslib.edtsuikit.databinding.ViewFairProgressBinding
import id.co.edtslib.edtsuikit.viewBinding
import id.co.edtslib.uikit.adapter.BaseAdapter
import id.co.edtslib.uikit.recipes.textbadge.klik.KlikVariant
import id.co.edtslib.uikit.stepper.StepperButton
import id.co.edtslib.uikit.tray.BottomSheetTray
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.colorStateList
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.inflater
import java.text.NumberFormat
import java.util.Locale
import id.co.edtslib.uikit.R as UIKitR
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class ParagonFairDemoActivity : GuidelinesBaseActivity() {
    private val binding by viewBinding<ActivityParagonFairDemoBinding>()
    override val navigationBarScrimColor: Int = Color.WHITE

    override val windowInsetsConfig = WindowInsetsConfig.Custom { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
        view.setPadding(bars.left, bars.top, bars.right, maxOf(bars.bottom, ime.bottom))
        insets
    }
    private var state = PromoDemoState(emptyList())
    private var promotion = DemoPromotion.REDEMPTION
    private var searchState = PromoSearchState()
    private val searchHandler = Handler(Looper.getMainLooper())
    private var pendingSearch: Runnable? = null
    private var searchScrollObserver: RecyclerView.AdapterDataObserver? = null
    private var expectedProductIds = emptyList<String>()
    private var updatingQuery = false
    private var failNextSearch = false
    private var detailScroll: Parcelable? = null
    private var resultScroll: Parcelable? = null
    private var lastRenderedMode = SearchMode.DETAIL
    private val emptyConstraints by lazy { ConstraintSet().apply { clone(ItemFairSearchEmptyBinding.inflate(inflater).messagePanel) } }
    private val inSearch get() = searchState.mode != SearchMode.DETAIL
    private val editingSearch get() = searchState.mode == SearchMode.EDITING
    private val promotionTitle get() = getString(if (promotion == DemoPromotion.FREE_GIFT) R.string.fair_search_gift_title else R.string.fair_search_redemption_title)
    private val searchBack = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            val ime = ViewCompat.getRootWindowInsets(binding.root)?.isVisible(WindowInsetsCompat.Type.ime()) == true
            when {
                editingSearch && ime -> ViewCompat.getWindowInsetsController(binding.queryField)?.hide(WindowInsetsCompat.Type.ime())
                editingSearch && searchState.hasSubmission -> {
                    cancelSearchRequest()
                    searchState = searchState.copy(mode = if (searchMatches().isEmpty()) SearchMode.EMPTY else SearchMode.RESULTS, draft = searchState.submitted)
                    binding.queryField.editText.clearFocus()
                    render()
                    restoreScroll(resultScroll)
                }
                else -> closeSearch()
            }
        }
    }
    private var header: ItemFairHeaderBinding? = null
    private var columns = 2
    private val gridEdge = 16.dp.toInt()
    private val gridGap = 12.dp.toInt()
    private var brand = "Semua"
    private var sortOrder = "Relevansi"
    private val shown = java.util.WeakHashMap<View, Float>()
    private var displayedProgress = 0f
    private var focusedProductId: String? = null
    private val clearFocusTask = Runnable { applyFocus(null) }
    private var compactVisible = false
    private val easing = PathInterpolator(0.2f, 0.8f, 0.2f, 1f)
    private var cartShown = false
    private var cartMove: ObjectAnimator? = null
    private val rupiah = NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID"))
    private val motionEnabled: Boolean get() = Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    private val effectiveColumns: Int get() = if (resources.configuration.fontScale > 1.4f) 1 else columns
    private var skeletonPulse: ObjectAnimator? = null
    private var skeletonFinish: Runnable? = null

    private val headerAdapter by lazy {
        BaseAdapter.adapterOf<HeaderRow, ItemFairHeaderBinding>(
            register = BaseAdapter.Register(onBindHolder = { _, item, view, _ -> bindHeader(view, item.state) }),
            diff = BaseAdapter.Diff(areItemsTheSame = { a, b -> a.id == b.id }, areContentsTheSame = { a, b -> a == b })
        )
    }
    private val sectionAdapter by lazy {
        BaseAdapter.adapterOf<SectionRow, ItemFairSectionBinding>(
            register = BaseAdapter.Register(onBindHolder = { _, item, view, _ ->
                view.count.setTextAppearance(if (inSearch) R.style.FairDemo_CaptionStrong else R.style.FairDemo_Caption)
                view.displayLabel.setTextAppearance(if (inSearch) R.style.FairDemo_CaptionStrong else R.style.FairDemo_Caption)
                view.heading.text = if (inSearch) promotionTitle else getString(R.string.fair_products)
                view.count.text = getString(R.string.fair_product_count, item.count)

                ViewCompat.setAccessibilityHeading(view.heading, true)
                view.root.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }),
            diff = BaseAdapter.Diff(areItemsTheSame = { a, b -> a.id == b.id }, areContentsTheSame = { a, b -> a == b })
        )
    }
    private val productAdapter by lazy {
        BaseAdapter.adapterOf<ProductRow, ItemFairProductBinding>(
            register = BaseAdapter.Register(onBindHolder = { _, item, view, _ -> bindProduct(view, item) }),
            diff = BaseAdapter.Diff(areItemsTheSame = { a, b -> a.id == b.id }, areContentsTheSame = { a, b -> a == b })
        )

    }

    private val scopeAdapter by lazy {
        BaseAdapter.adapterOf<ScopeRow, ItemFairSearchScopeBinding>(
            register = BaseAdapter.Register(onBindHolder = { _, row, view, _ ->
                view.query.text = row.query
                view.scope.text = row.description
                view.root.contentDescription = "${row.query}, ${row.description}"
                ViewCompat.setScreenReaderFocusable(view.root, true)
                view.root.setOnLongClickListener { if (row.id == "promo") submitSearch() else showGeneralSearchHandoff(); true }
            }),
            diff = BaseAdapter.Diff(areItemsTheSame = { a, b -> a.id == b.id }, areContentsTheSame = { a, b -> a == b })
        ).apply { setOnItemClickListener { _, row, _ -> if (row.id == "promo") submitSearch() else showGeneralSearchHandoff() } }
    }
    private val emptyAdapter by lazy {
        BaseAdapter.adapterOf<EmptyRow, ItemFairSearchEmptyBinding>(
            register = BaseAdapter.Register(onBindHolder = { _, row, view, _ -> bindSearchEmpty(view, row) }),
            diff = BaseAdapter.Diff(areItemsTheSame = { a, b -> a.id == b.id }, areContentsTheSame = { a, b -> a == b })
        )
    }
    private val loadingAdapter by lazy {
        BaseAdapter.adapterOf<String, ItemFairSearchLoadingBinding>(
            register = BaseAdapter.Register(onBindHolder = { _, _, view, _ ->
                view.spinner.isVisible = motionEnabled
                view.root.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }),
            diff = BaseAdapter.Diff(areItemsTheSame = { a, b -> a == b }, areContentsTheSame = { a, b -> a == b })
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paragon_fair_demo)
        promotion = DemoPromotion.valueOf(savedInstanceState?.getString("promotion") ?: "FREE_GIFT")
        state = restoreState(savedInstanceState) ?: scenarioState(
            DemoScenario.entries.firstOrNull { it.name == intent.getStringExtra(EXTRA_SCENARIO) } ?: DemoScenario.START
        )
        savedInstanceState?.let {
            searchState = PromoSearchState(
                mode = SearchMode.valueOf(it.getString("searchMode") ?: "DETAIL"),
                draft = it.getString("searchDraft") ?: "", submitted = it.getString("searchSubmitted") ?: "",
                fallbackAvailable = it.getBoolean("searchFallback"), hasSubmission = it.getBoolean("searchHasSubmission")
            )
            failNextSearch = it.getBoolean("searchFailNext")
            detailScroll = it.getParcelable("detailScroll")
            resultScroll = it.getParcelable("resultScroll")
        }
        columns = savedInstanceState?.getInt("columns", 2) ?: 2
        brand = savedInstanceState?.getString("brand") ?: "Semua"
        sortOrder = savedInstanceState?.getString("sort") ?: "Relevansi"

        displayedProgress = if (savedInstanceState != null) state.barProgress else 0f
        val grid = StaggeredGridLayoutManager(effectiveColumns, StaggeredGridLayoutManager.VERTICAL).apply {
            gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        }
        binding.products.layoutManager = grid
        binding.products.updatePadding(left = gridEdge - gridGap / 2, right = gridEdge - gridGap / 2)
        binding.products.adapter = ConcatAdapter(headerAdapter, scopeAdapter, sectionAdapter, productAdapter, emptyAdapter, loadingAdapter)
        binding.products.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                val position = binding.products.getChildAdapterPosition(view)
                val firstProduct = headerAdapter.itemCount + scopeAdapter.itemCount + sectionAdapter.itemCount
                val lastProduct = firstProduct + productAdapter.itemCount
                val params = view.layoutParams as? StaggeredGridLayoutManager.LayoutParams ?: return
                params.isFullSpan = position !in firstProduct until lastProduct
                view.layoutParams = params
            }

            override fun onChildViewDetachedFromWindow(view: View) = Unit
        })
        (binding.products.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        binding.products.addItemDecoration(ProductSpacingDecoration())
        productAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        binding.products.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) = updateCompactHeader()
        })
        binding.bottomActions.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            binding.products.clipToPadding = false
            binding.products.clipChildren = false
            (binding.products.parent as? ViewGroup)?.clipChildren = false
            binding.products.clipToPadding = false
        }
        binding.back.setOnClickListener { if (inSearch) closeSearch() else onBackPressedDispatcher.onBackPressed() }
        binding.title.setOnLongClickListener { showScenarios(); true }
        ViewCompat.replaceAccessibilityAction(binding.title, AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_LONG_CLICK,
            "Pilih skenario demo") { _, _ -> showScenarios(); true }

        configureSearchField()
        onBackPressedDispatcher.addCallback(this, searchBack)

        if (resources.configuration.fontScale > 1.4f) {
            binding.sortLabel.isVisible = false
            binding.filterLabel.isVisible = false
            binding.cartLabel.isVisible = false
            binding.cartCount.isVisible = false
        }
        binding.compactMeter.seeGifts.isVisible = true
        binding.compactMeter.seeGifts.setOnClickListener { showGifts() }
        binding.compactMeter.message.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        render()

        if (savedInstanceState == null) showSkeleton { animateProgress(state.barProgress) }
        if (editingSearch) focusSearch()
        if (searchState.mode == SearchMode.LOADING) submitSearch()
        if (savedInstanceState == null && intent.getBooleanExtra(EXTRA_SEARCH, false)) showSearch()
    }

    private fun render(announce: Boolean = false) {
        val base = when (searchState.mode) {
            SearchMode.DETAIL -> state.products
            SearchMode.RESULTS -> searchMatches()
            else -> emptyList()
        }
        val visible = base.filter { brand == "Semua" || it.name.startsWith(brand) }.let {
            when (sortOrder) {
                "Nama A–Z" -> it.sortedBy { p -> p.name }
                "Harga terendah" -> it.sortedBy { p -> p.price }
                "Harga tertinggi" -> it.sortedByDescending { p -> p.price }
                else -> it
            }
        }
        val submitted = inSearch && !editingSearch
        searchBack.isEnabled = inSearch
        binding.title.isVisible = !inSearch
        binding.share.isVisible = !inSearch
        binding.queryField.isVisible = inSearch
        binding.toolbar.elevation = if (editingSearch) 2.dp else 0f
        if (inSearch && binding.queryField.editText.text.toString() != searchState.draft) {
            updatingQuery = true
            binding.queryField.text = searchState.draft
            binding.queryField.editText.setSelection(searchState.draft.length)
            updatingQuery = false
        }
        val layout = binding.products.layoutManager as StaggeredGridLayoutManager
        layout.spanCount = if (editingSearch) 1 else effectiveColumns
        binding.products.updateLayoutParams<ConstraintLayout.LayoutParams> {
            topToBottom = if (submitted) R.id.compactProgress else R.id.toolbar
        }
        if (inSearch) {
            binding.compactProgress.animate().cancel()
            binding.compactProgress.alpha = 1f
            binding.compactProgress.translationY = 0f
            binding.compactProgress.isVisible = submitted
            compactVisible = submitted
        }
        binding.bottomActions.isVisible = !editingSearch
        productAdapter.stateRestorationPolicy = if (inSearch) RecyclerView.Adapter.StateRestorationPolicy.ALLOW else RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        headerAdapter.items = if (inSearch) emptyList() else listOf(HeaderRow(state))
        sectionAdapter.items = if (inSearch && searchState.mode != SearchMode.RESULTS) emptyList() else listOf(SectionRow(visible.size, columns))
        expectedProductIds = visible.map { it.id }

        productAdapter.items = visible.map { ProductRow(it, state.availability, effectiveColumns, inSearch, promotion, focused = it.id == focusedProductId) }
        val query = PromoSearchState.normalize(searchState.draft)
        scopeAdapter.items = if (editingSearch && query.isNotEmpty()) listOf(
            ScopeRow("promo", query, getString(R.string.fair_search_promo_scope, promotionTitle)),
            ScopeRow("global", query, getString(R.string.fair_search_global_scope))
        ) else emptyList()
        emptyAdapter.items = when {
            searchState.mode in listOf(SearchMode.EMPTY, SearchMode.ERROR) -> listOf(EmptyRow(searchState.mode, searchState.fallbackAvailable))
            searchState.mode == SearchMode.RESULTS && visible.isEmpty() -> listOf(EmptyRow(SearchMode.EMPTY, false, filtered = true))
            else -> emptyList()
        }
        loadingAdapter.items = if (searchState.mode == SearchMode.LOADING) listOf("loading") else emptyList()
        if (lastRenderedMode != searchState.mode) {
            binding.products.animate().cancel()
            binding.products.alpha = if (motionEnabled) 0f else 1f
            binding.products.animate().alpha(1f).setDuration(if (motionEnabled) 160 else 0).start()
            lastRenderedMode = searchState.mode
        }
        bindMeter(binding.compactMeter, state)
        binding.filterSortPill.isVisible = state.availability == PromoAvailability.ACTIVE && (!inSearch || searchState.mode == SearchMode.RESULTS)
        binding.search.isVisible = !inSearch && state.availability != PromoAvailability.NEW_CUSTOMER_ONLY

        if (state.cartCount > 0) {
            binding.cartCount.text = getString(R.string.fair_cart_count, state.cartCount)
            binding.cartTotal.text = money(state.cartTotal)
            binding.cart.contentDescription = getString(R.string.fair_cart_description, state.cartCount, money(state.cartTotal))
        }
        setCartVisible(state.cartCount > 0)

        if (announce) binding.root.announceForAccessibility(message(state).toString())
        binding.products.post { updateCompactHeader() }
    }

    private fun bindHeader(view: ItemFairHeaderBinding, model: PromoDemoState) {
        header = view
        view.root.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        if (resources.configuration.fontScale > 1.4f) {

            ConstraintSet().apply {
                clone(view.progressPanel)
                connect(R.id.quotaBadge, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                connect(R.id.quotaBadge, ConstraintSet.TOP, R.id.fairBadge, ConstraintSet.BOTTOM, 8.dp.toInt())
                connect(R.id.meter, ConstraintSet.TOP, R.id.quotaBadge, ConstraintSet.BOTTOM, 8.dp.toInt())
                constrainHeight(R.id.fairBadge, ConstraintSet.WRAP_CONTENT)
                constrainHeight(R.id.quotaBadge, ConstraintSet.WRAP_CONTENT)
                applyTo(view.progressPanel)
            }
            ConstraintSet().apply {
                clone(view.root)
                listOf(R.id.periodValue to R.id.periodLabel, R.id.minimumValue to R.id.minimumLabel, R.id.terms to R.id.termsLabel).forEach { (value, label) ->
                    connect(value, ConstraintSet.START, label, ConstraintSet.START)
                    connect(value, ConstraintSet.TOP, label, ConstraintSet.BOTTOM, 4.dp.toInt())
                }
                applyTo(view.root)
            }
            view.periodValue.gravity = android.view.Gravity.START
            view.minimumValue.gravity = android.view.Gravity.START
            view.terms.gravity = android.view.Gravity.START
        }
        val active = model.availability == PromoAvailability.ACTIVE
        val monochrome = if (active) null else ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
        listOf(view.banner, view.giftOne, view.giftTwo, view.giftThree).forEach { it.colorFilter = monochrome }
        view.newUser.isVisible = model.availability == PromoAvailability.NEW_CUSTOMER_ONLY
        view.fairBadge.apply {
            text = model.fairName
            textColor = color(if (active) UIKitR.color.primary_30 else UIKitR.color.black_50)
            badgeColor = color(if (active) UIKitR.color.support_selected else UIKitR.color.black_30)
            badgeTextAppearance = R.style.FairDemo_CaptionStrong
            setBadgePadding(8, 4)
            setCornerRadius(4f)
        }
        view.quotaBadge.apply {
            klikVariant = KlikVariant.Quota.DEFAULT
            text = if (model.availability == PromoAvailability.QUOTA_EXHAUSTED) "Kuota Promo 0%" else "Kuota Promo 100%"
            badgeColor = color(if (active) R.color.fair_quota_background else UIKitR.color.black_30)
            textColor = color(if (active) R.color.fair_quota_foreground else UIKitR.color.black_50)
            badgeTextAppearance = R.style.FairDemo_CaptionStrong
        }
        view.promoTitle.text = if (promotion == DemoPromotion.REDEMPTION) promotionTitle else "Belanja Paragon Fair Senilai Rp100.000 Dapat Hadiah. " +
                if (model.rule == RewardRule.ONE_TIME) "Tidak Berlaku Kelipatan." else "Berlaku Kelipatan."

        view.banner.isVisible = true
        view.gifts.isVisible = true
        view.minimumValue.text = money(model.threshold)
        view.terms.paintFlags = view.terms.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        view.terms.setOnClickListener { showTerms() }
        view.info.makeStatic()

        view.giftsAction.setOnClickListener { showGifts() }
        bindMeter(view.meter, model)
        view.meter.message.importantForAccessibility = if (compactVisible) View.IMPORTANT_FOR_ACCESSIBILITY_NO else View.IMPORTANT_FOR_ACCESSIBILITY_YES
        ViewCompat.setAccessibilityHeading(view.promoTitle, true)
        view.root.doOnLayout { updateCompactHeader() }
    }

    private fun bindMeter(view: ViewFairProgressBinding, model: PromoDemoState) {
        view.seeGifts.contentDescription =
            if (promotion == DemoPromotion.FREE_GIFT) {
                getString(R.string.fair_view_gifts)
            } else {
                "Lihat penawaran tebus murah"
            }

        val hasLaps =
            model.rule == RewardRule.REPEATABLE && model.earnedGifts > 0

        view.progressBar.apply {
            showBadge = true

            val limit = if (model.rule == RewardRule.REPEATABLE) {
                100_000f
            } else {
                100f
            }

            if (maxIndicatorProgress != 100f) {
                maxIndicatorProgress = 100f
            }

            if (progressLimit != limit) {
                progressLimit = limit
            }

            isEnabled = model.availability == PromoAvailability.ACTIVE
        }

        val next = message(model)

        if (view.message.text.toString() != next.toString()) {
            view.message.animate().cancel()
            view.message.text = next

            if (motionEnabled) {
                view.message.alpha = 0f
                view.message.translationY = 4.dp

                view.message.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(160)
                    .setInterpolator(easing)
                    .start()
            } else {
                view.message.alpha = 1f
                view.message.translationY = 0f
            }
        } else {
            view.message.text = next
        }

        ViewCompat.setStateDescription(
            view.message,
            "${money(model.qualifyingSubtotal)} dari ${money(model.threshold)}; " +
                    "${model.claimedGifts.size} benefit dipilih"
        )
    }

    private fun bindProduct(view: ItemFairProductBinding, row: ProductRow) {
        val product = row.product
        val active = row.availability == PromoAvailability.ACTIVE
        view.image.setImageResource(product.image)
        view.card.strokeWidth = if (row.search && row.promotion == DemoPromotion.REDEMPTION) 1.dp.toInt() else 0
        view.card.setStrokeColor(color(UIKitR.color.black_30))
        view.card.alpha = if (active) 1f else 0.35f
        view.name.text = product.name
        view.name.maxLines = if (resources.configuration.fontScale > 1.4f) Int.MAX_VALUE else 2
        view.price.text = money(product.price)
        view.discount.isVisible = product.discounted
        view.originalPrice.isVisible = product.discounted
        view.originalPrice.text = money(product.originalPrice)
        view.originalPrice.paintFlags = view.originalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        view.promotion.text = product.promotion
        val savings = product.promotion == "Banyak Lebih Hemat"
        view.promotion.setTextColor(color(if (savings) UIKitR.color.klik_color_red_30 else UIKitR.color.warning_primary))
        view.promotion.setBackgroundResource(if (savings) R.drawable.fair_discount_background else R.drawable.fair_extra_background)
        (view.image.parent as View).setPadding(0, 0, 0, if (row.search && row.promotion == DemoPromotion.FREE_GIFT) 8.dp.toInt() else 0)
        view.discount.setTextAppearance(if (row.search) R.style.FairSearch_SmallStrong else R.style.FairDemo_Small)
        view.promotion.setTextAppearance(if (row.search) R.style.FairSearch_SmallStrong else R.style.FairDemo_Small)
        view.discount.setTextColor(color(UIKitR.color.klik_color_red_30))
        view.promotion.setTextColor(color(if (savings) UIKitR.color.klik_color_red_30 else UIKitR.color.warning_primary))
        view.benefitStrip.isVisible = !row.search || row.promotion == DemoPromotion.REDEMPTION
        view.benefit.text = product.benefit
        view.promotion.isVisible = product.promotion.isNotEmpty()
        view.discount.text = "50%"
        val highlight = product.highlight
        view.benefitIcon.setImageResource(if (product.benefit.contains("Stamp")) R.drawable.fair_stamp else R.drawable.fair_point)
        view.highlight.apply {
            klikVariant = KlikVariant.Highlight.DEFAULT
            text = highlight
            isVisible = highlight.isNotEmpty()
            setIcon(when (highlight) {
                "Paket Bundling" -> R.drawable.fair_search_bundling
                "Banyak Lebih Hemat" -> R.drawable.fair_search_percent
                else -> R.drawable.fair_gift_icon
            })
            if (row.promotion == DemoPromotion.REDEMPTION && highlight.isNotEmpty()) {
                setGradientBackground(intArrayOf(
                    color(if (highlight == "Paket Bundling") R.color.fair_bundling_start else R.color.fair_savings_start),
                    color(if (highlight == "Paket Bundling") R.color.fair_bundling_end else R.color.fair_savings_end)
                ))
            }
            iconVisible = true
        }

        view.root.makeStatic()
        view.card.makeStatic()
        view.card.contentDescription = listOf(product.name, money(product.price), highlight, product.promotion,
            if (view.benefitStrip.isVisible) product.benefit else "").filter { it.isNotEmpty() }.joinToString(", ")
        view.root.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        ViewCompat.setStateDescription(view.card, if (active) "Jumlah ${product.quantity}" else "Promo tidak tersedia")
        ViewCompat.setScreenReaderFocusable(view.card, true)
        view.quantity.apply {
            clearListener()
            setAllowTextEditing(false)
            setMinValue(0)

            setMaxValue(Int.MAX_VALUE)
            setCount(product.quantity, animate = false)
            setMaxValue(product.stock)
            isEnabled = active
            setPlusButtonEnabled(active && product.quantity < product.stock)
            setMinusButtonEnabled(active && product.quantity > 0)
            val blue = colorStateList(UIKitR.color.primary_30)
            val white = colorStateList(UIKitR.color.white)
            binding.singlePlusButton.backgroundTintList = blue
            binding.singlePlusButton.strokeWidth = 0
            binding.singlePlusButton.iconTint = white
            binding.stepperContainer.setCardBackgroundColor(blue)
            binding.stepperContainer.strokeWidth = 0
            binding.countText.setTextColor(color(UIKitR.color.white))
            if (resources.configuration.fontScale > 1.4f) binding.countText.setTextSize(android.util.TypedValue.COMPLEX_UNIT_DIP, 14f)
            binding.plusButton.backgroundTintList = white
            binding.plusButton.iconTint = blue
            binding.minusButton.iconTint = colorStateList(UIKitR.color.black_30)
            binding.singlePlusButton.contentDescription = getString(R.string.fair_add, product.name, product.quantity)
            binding.plusButton.contentDescription = getString(R.string.fair_add, product.name, product.quantity)
            binding.minusButton.contentDescription = getString(R.string.fair_remove, product.name, product.quantity)

            binding.singlePlusButton.setOnClickListener {
                val current = state.products.firstOrNull { it.id == product.id }?.quantity ?: 0
                if (current == 0) step(product.id, 1) else applyFocus(product.id)
            }
            binding.plusButton.setOnClickListener { step(product.id, 1) }
            binding.minusButton.setOnClickListener { step(product.id, -1) }
        }
        val face = when {
            product.quantity == 0 -> StepperFace.ADD
            row.focused -> StepperFace.FOCUSED
            else -> StepperFace.COUNT
        }

        applyStepperFace(view, product.id, face, product.quantity)
        view.quantity.binding.singlePlusButton.apply {
            if (face == StepperFace.COUNT) {
                isEnabled = active
                contentDescription = "${product.name}, jumlah ${product.quantity}. Ketuk untuk mengubah jumlah"
            }
        }
    }

    private fun View.makeStatic() {
        setOnClickListener(null)
        setOnLongClickListener(null)
        isClickable = false
        isLongClickable = false
        isFocusable = false
    }

    private fun applyFocus(id: String?) {
        binding.products.removeCallbacks(clearFocusTask)
        if (id != null) binding.products.postDelayed(clearFocusTask, 5000L)
        if (focusedProductId == id) return
        focusedProductId = id
        productAdapter.items = productAdapter.items.map { it.copy(focused = it.id == id) }
    }

    private fun step(id: String, delta: Int) {
        applyFocus(id)
        state.products.firstOrNull { it.id == id }?.let { changeQuantity(id, it.quantity + delta) }
    }

    private fun changeQuantity(id: String, quantity: Int) {
        val old = state
        state = state.withQuantity(id, quantity)
        if (state == old) return
        render(announce = old.earnedGifts != state.earnedGifts || old.claimedGifts != state.claimedGifts)

        animateProgress(state.barProgress)
        if (motionEnabled && state.cartCount > 0) {
            binding.cartIcon.animate().cancel()
            binding.cartIcon.scaleX = 1f
            binding.cartIcon.scaleY = 1f

        }

    }

    private fun animateProgress(target: Float) {
        val increasing = target > displayedProgress
        displayedProgress = target
        android.util.Log.d("FairBar", "prevTarget=$displayedProgress target=$target")
        listOfNotNull(header?.meter?.progressBar, binding.compactMeter.progressBar).forEach { bar ->
            bar.shouldAnimate = motionEnabled && increasing
            android.util.Log.d("FairBar", "getter=${bar.indicatorProgress} animate=${bar.shouldAnimate}")
            bar.indicatorProgress = target
            shown[bar] = target
        }
    }

    private fun updateCompactHeader() {
        val grid = binding.products.layoutManager as StaggeredGridLayoutManager
        val view = header
        val first = grid.findFirstVisibleItemPositions(null).minOrNull() ?: RecyclerView.NO_POSITION
        val threshold = if (compactVisible) 4.dp.toInt() else -4.dp.toInt()
        val visible = state.products.isNotEmpty() && (first > 0 || (first == 0 && view != null && view.root.top + view.gifts.bottom < threshold))
        if (inSearch) return
        if (visible == compactVisible) return
        compactVisible = visible
        binding.compactProgress.animate().cancel()
        if (visible) {
            binding.compactProgress.isVisible = true
            binding.compactProgress.alpha = if (motionEnabled) 0f else 1f
            binding.compactProgress.translationY = if (motionEnabled) -4.dp else 0f
            binding.compactProgress.animate().alpha(1f).translationY(0f).setDuration(if (motionEnabled) 160 else 0).start()
        } else {
            binding.compactProgress.animate().alpha(0f).setDuration(if (motionEnabled) 120 else 0).withEndAction {
                if (!compactVisible) binding.compactProgress.isVisible = false
            }.start()
        }
        view?.meter?.message?.importantForAccessibility = if (visible) View.IMPORTANT_FOR_ACCESSIBILITY_NO else View.IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    private fun changeColumns(value: Int) {
        val anchor = binding.products.layoutManager?.onSaveInstanceState()
        columns = value
        (binding.products.layoutManager as StaggeredGridLayoutManager).spanCount = effectiveColumns
        render()
        restoreScroll(anchor)
    }

    private fun message(model: PromoDemoState): CharSequence {
        val text = when (model.availability) {
            PromoAvailability.QUOTA_EXHAUSTED -> "Kuota promo sudah habis. Cek promo fair lainnya yang tersedia yuk!"
            PromoAvailability.LIMIT_REACHED -> "Limit fair sudah habis. Cek promo fair lainnya yang tersedia yuk!"
            PromoAvailability.NEW_CUSTOMER_ONLY -> "Promo hanya berlaku untuk transaksi pertamamu di Klik Indomaret"
            PromoAvailability.ACTIVE -> when {

                promotion == DemoPromotion.REDEMPTION && model.unclaimedGifts > 0 && model.claimedGifts.isNotEmpty() -> "Kamu sudah ambil ${model.claimedGifts.size} dari ${model.earnedGifts} tebus murah. Yuk pilih produk lagi"
                promotion == DemoPromotion.REDEMPTION && model.unclaimedGifts > 0 -> "Kamu bisa ambil ${model.earnedGifts} tebus murah"
                promotion == DemoPromotion.REDEMPTION && model.claimedGifts.isNotEmpty() -> "Kamu sudah ambil ${model.claimedGifts.size} tebus murah"
                model.qualifyingSubtotal == 0L -> "Tambah produk dulu, yuk!"
                model.earnedGifts == 0 -> "Tambah ${money(model.remainingSpend)} lagi untuk dapatkan benefit"
                model.unclaimedGifts > 0 && model.claimedGifts.isNotEmpty() -> "Kamu sudah ambil ${model.claimedGifts.size} dari ${model.earnedGifts} hadiah. Yuk pilih produk lagi"
                model.unclaimedGifts > 0 -> "Kamu bisa ambil ${model.earnedGifts} hadiah"
                model.rule == RewardRule.REPEATABLE && model.claimedGifts.size == 1 -> "Kamu sudah ambil 1 hadiah."
                else -> "Kamu sudah ambil ${model.claimedGifts.size} hadiah"
            }
        }

        return SpannableString(text).apply {
            Regex("Rp[\\d.]+|\\d+(?: dari \\d+)? (?:hadiah|tebus murah)").findAll(text).forEach {
                setSpan(ForegroundColorSpan(color(UIKitR.color.primary_30)), it.range.first, it.range.last + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(StyleSpan(android.graphics.Typeface.BOLD), it.range.first, it.range.last + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    private fun StepperButton.showExpanded() {
        val width = resources.getDimensionPixelSize(UIKitR.dimen.dimen_90)
        val params = binding.stepperContainer.layoutParams
        if (params.width != width) {
            params.width = width
            binding.stepperContainer.layoutParams = params
        }
        binding.singlePlusButton.visibility = View.GONE
        binding.minusButton.visibility = View.VISIBLE
        binding.plusButton.visibility = View.VISIBLE
        binding.countText.visibility = View.VISIBLE
        binding.countEditText.visibility = View.GONE
        binding.minusButton.alpha = 1f
        binding.plusButton.alpha = 1f
        binding.countText.alpha = 1f
    }

    private fun StepperButton.applyRedeemStyle(dimCount: Boolean) {
        val grey = color(UIKitR.color.black_30)
        val darkGrey = color(UIKitR.color.black_60)
        val blue = color(UIKitR.color.primary_30)
        val white = color(UIKitR.color.white)

        fun states(enabled: Int, disabled: Int) = ColorStateList(
            arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf()),
            intArrayOf(disabled, enabled)
        )

        binding.stepperContainer.apply {
            setCardBackgroundColor(white)
            setStrokeColor(grey)
            strokeWidth = 1.dp.toInt()
        }
        binding.minusButton.iconTint = states(darkGrey, grey)
        binding.plusButton.backgroundTintList = states(blue, grey)
        binding.plusButton.iconTint = ColorStateList.valueOf(white)
        binding.countText.setTextColor(if (dimCount) grey else color(UIKitR.color.black_70))
        binding.countText.alpha = 1f
    }

    private fun applyStepperFace(v: ItemFairProductBinding, productId: String, face: StepperFace, count: Int) {
        val stepper = v.quantity
        val b = stepper.binding
        val collapsedW = resources.getDimensionPixelSize(UIKitR.dimen.l)
        val expandedW = resources.getDimensionPixelSize(UIKitR.dimen.dimen_90)
        val blue = color(UIKitR.color.primary_30)
        val white = color(UIKitR.color.white)

        val prev = stepper.tag as? FaceTag
        prev?.cancelled = true
        prev?.animator?.cancel()
        val sameCard = prev != null && prev.productId == productId
        val from = if (sameCard) prev!!.face else face
        val tag = FaceTag(productId, face)
        stepper.tag = tag

        fun setWidth(w: Int) {
            val lp = b.stepperContainer.layoutParams
            if (lp.width != w) { lp.width = w; b.stepperContainer.layoutParams = lp }
        }
        fun setControls(visible: Boolean, alpha: Float = 1f) {
            listOf(b.minusButton, b.plusButton, b.countText).forEach { it.isVisible = visible; it.alpha = alpha }
            b.countEditText.isVisible = false
        }

        fun styleCircle(asCount: Boolean) {
            b.singlePlusButton.apply {
                if (asCount) {
                    icon = null
                    text = count.toString()
                    setTextAppearance(R.style.FairDemo_Strong)
                    setTextColor(color(UIKitR.color.black_70))
                    setPadding(0, 0, 0, 0)
                    backgroundTintList = ColorStateList.valueOf(white)
                } else {
                    text = ""
                    setIconResource(UIKitR.drawable.ic_plus_stepper)
                    iconTint = ColorStateList.valueOf(white)
                    backgroundTintList = ColorStateList.valueOf(blue)
                }
            }
        }
        fun styleContainer(asCount: Boolean) {
            b.stepperContainer.setCardBackgroundColor(if (asCount) white else blue)
            b.stepperContainer.strokeWidth = if (asCount) 1.dp.toInt() else 0
            if (asCount) b.stepperContainer.setStrokeColor(color(UIKitR.color.black_30))
        }

        fun applyFinal() {
            stepper.isVisible = true
            when (face) {
                StepperFace.ADD -> {
                    setWidth(collapsedW); styleContainer(false); styleCircle(false)
                    b.singlePlusButton.isVisible = true; b.singlePlusButton.alpha = 1f; setControls(false)
                }
                StepperFace.FOCUSED -> {
                    setWidth(expandedW); styleContainer(false); styleCircle(false)
                    b.singlePlusButton.isVisible = false; setControls(true)
                }
                StepperFace.COUNT -> {
                    setWidth(collapsedW); styleContainer(true); styleCircle(true)
                    b.singlePlusButton.isVisible = true; b.singlePlusButton.alpha = 1f; setControls(false)
                }
            }
        }

        val expanding = face == StepperFace.FOCUSED
        val collapsing = from == StepperFace.FOCUSED && !expanding
        if (!motionEnabled || !sameCard || from == face || !(expanding || collapsing)) { applyFinal(); return }

        fun fade(from: Float, to: Float, ms: Long, delay: Long = 0L, apply: (Float) -> Unit) =
            ValueAnimator.ofFloat(from, to).apply {
                duration = ms; startDelay = delay
                addUpdateListener { apply(it.animatedValue as Float) }
            }
        fun width(from: Int, to: Int) = ValueAnimator.ofInt(from, to).apply {
            duration = 300L; interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { setWidth(it.animatedValue as Int) }
        }
        fun containerColor(from: Int, to: Int, ms: Long, delay: Long = 0L) = ValueAnimator.ofArgb(from, to).apply {
            duration = ms; startDelay = delay
            addUpdateListener { b.stepperContainer.setCardBackgroundColor(it.animatedValue as Int) }
        }
        val controls = listOf(b.minusButton, b.plusButton, b.countText)

        val parts = mutableListOf<Animator>()
        stepper.isVisible = true
        if (expanding) {

            setWidth(collapsedW); setControls(true, 0f)
            b.singlePlusButton.isVisible = true; b.singlePlusButton.alpha = 1f
            b.stepperContainer.strokeWidth = 0
            parts += width(collapsedW, expandedW)
            parts += fade(0f, 1f, 150L, 150L) { a -> controls.forEach { it.alpha = a } }
            parts += fade(1f, 0f, 150L) { b.singlePlusButton.alpha = it }
            if (from == StepperFace.COUNT) parts += containerColor(white, blue, 150L)
        } else {
            setWidth(expandedW); setControls(true, 1f)
            styleCircle(face == StepperFace.COUNT)
            b.singlePlusButton.isVisible = true; b.singlePlusButton.alpha = 0f
            parts += width(expandedW, collapsedW)
            parts += fade(1f, 0f, 150L, delay = 300L) { a -> controls.forEach { it.alpha = a } }
            parts += fade(0f, 1f, 150L, delay = 300L) { b.singlePlusButton.alpha = it }
            if (face == StepperFace.COUNT) parts += containerColor(blue, white, 150L, delay = 300L)
        }

        tag.animator = AnimatorSet().apply {
            playTogether(parts)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) { if (!tag.cancelled) applyFinal() }
            })
            start()
        }
    }

    private fun showTerms() {
        val repeat = state.rule == RewardRule.REPEATABLE
        showOptions("Syarat dan ketentuan", "Belanja produk Paragon Fair senilai ${money(state.threshold)} untuk mendapat hadiah. " +
                (if (repeat) "Berlaku kelipatan. " else "Tidak berlaku kelipatan. ") +
                "Hadiah dipilih melalui Lihat dan mengikuti kuota serta kelayakan promo. Periode sesuai referensi: 03 Jan–31 Des 2025.", emptyList()) {}
    }

    private fun showSkeleton(durationMs: Long = 1500L, onFinished: () -> Unit = {}) {
        val root = binding.skeleton.root
        skeletonFinish?.let { root.removeCallbacks(it) }
        root.animate().cancel()
        root.alpha = 1f
        root.isVisible = true

        if (skeletonPulse == null && motionEnabled) {
            skeletonPulse = ObjectAnimator.ofFloat(binding.skeleton.content, View.ALPHA, 1f, 0.5f).apply {
                duration = 700L
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                start()
            }
        }

        val finish = Runnable {
            skeletonFinish = null
            root.animate().alpha(0f).setDuration(150L).withEndAction {
                skeletonPulse?.cancel(); skeletonPulse = null
                root.isVisible = false
                root.alpha = 1f
                if (!isFinishing && !isDestroyed) onFinished()
            }.start()
        }
        skeletonFinish = finish
        root.postDelayed(finish, durationMs)
    }

    private fun showGifts() {

        showRedemptionTray()
    }

    private fun showRedemptionTray() {
        val limit = state.earnedGifts
        val locked = limit == 0
        var committed = redemptionOffers().map { o -> o.copy(quantity = state.claimedGifts.count { it == o.id }) }
        var draft = committed
        var tray: BottomSheetTray? = null
        var sheetBehavior: BottomSheetBehavior<*>? = null
        val sheet = SheetFairRedemptionBinding.inflate(inflater)
        val defaultTint = sheet.update.backgroundTintList
        val destructiveTint = colorStateList(UIKitR.color.klik_color_red_30)
        lateinit var refresh: () -> Unit

        fun setQuantity(id: String, quantity: Int) {
            val current = draft.firstOrNull { it.id == id } ?: return
            val room = (limit - draft.sumOf { it.quantity }).coerceAtLeast(0)
            val capped = quantity.coerceIn(0, current.quantity + room)
            if (capped == current.quantity) return
            draft = draft.map { if (it.id == id) it.copy(quantity = capped) else it }
            refresh()
        }

        val adapter = BaseAdapter.adapterOf<RedeemRow, ItemFairRedeemProductBinding>(
            register = BaseAdapter.Register(onBindHolder = { _, row, v, _ ->
                val item = row.item
                val selected = item.quantity > 0 && !item.restocking
                val greyed = row.locked || item.restocking
                v.root.post {
                    android.util.Log.d("RedeemCard", "late state: root clickable=${v.root.isClickable} listener=${v.root.hasOnClickListeners()} info clickable=${v.info.isClickable}")
                    v.root.makeStatic()
                    v.info.makeStatic()
                }

                applySelection(v, item.id, selected)
                v.total.isVisible = selected
                if (selected) {
                    val pcs = "${item.quantity * item.packSize} pcs"
                    v.total.text = SpannableString("Total dapat: $pcs").apply {
                        val start = length - pcs.length
                        setSpan(StyleSpan(android.graphics.Typeface.BOLD), start, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        setSpan(ForegroundColorSpan(color(UIKitR.color.black_70)), start, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
                v.image.setImageResource(item.image)
                v.name.text = item.name
                v.pack.text = "Isi ${item.packSize} pcs"
                v.discount.text = "${100 - item.price * 100 / item.originalPrice}%"
                v.originalPrice.text = money(item.originalPrice)
                v.originalPrice.paintFlags = v.originalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                v.price.text = if (item.price == 0L) "GRATIS" else money(item.price)
                v.restockLabel.isVisible = item.restocking
                v.stepper.isVisible = !item.restocking

                v.root.alpha = 1f
                v.info.animationDuration = 0L
                v.info.setDisabledWithAnimation(greyed, animate = true)
                v.footer.setBackgroundColor(color(if (greyed) UIKitR.color.black_20 else UIKitR.color.info_background))
                v.info.alpha = if (greyed) 0.5f else 1f
                v.stepper.apply {
                    clearListener()
                    setAllowTextEditing(false)
                    showExpanded()

                    setMinValue(0)
                    setMaxValue(Int.MAX_VALUE)
                    setCount(item.quantity, animate = false)
                    setMaxValue(item.quantity + row.remaining)
                    isEnabled = !row.locked
                    setPlusButtonEnabled(row.canAdd && !item.restocking)
                    setMinusButtonEnabled(item.quantity > 0)

                    applyRedeemStyle(dimCount = row.locked || (item.quantity == 0 && !row.canAdd))
                    binding.singlePlusButton.contentDescription = "Tambah ${item.name}"
                    binding.plusButton.contentDescription = "Tambah ${item.name}"
                    binding.minusButton.contentDescription = "Kurangi ${item.name}"
                    setOnCountChangeListener { newCount -> setQuantity(item.id, newCount) }
                }
                v.root.contentDescription = "${item.name}, isi ${item.packSize} pcs, jumlah ${item.quantity}" + if (selected) ", terpilih" else ""
            }),
            diff = BaseAdapter.Diff(areItemsTheSame = { a, b -> a.id == b.id }, areContentsTheSame = { a, b -> a == b })
        )

        fun quantities(list: List<RedeemItem>) = list.associate { it.id to it.quantity }
        fun hasChanges() = quantities(draft) != quantities(committed)

        fun isRemoval(): Boolean {
            val before = quantities(committed)
            val added = draft.any { it.quantity > (before[it.id] ?: 0) }
            val removed = draft.any { it.quantity == 0 && (before[it.id] ?: 0) > 0 }
            return hasChanges() && removed && !added
        }

        refresh = {
            val used = draft.sumOf { it.quantity }
            val cartEmpty = committed.sumOf { it.quantity } == 0
            val remove = isRemoval()
            val enabled = hasChanges()

            android.util.Log.d("FairDemo", "draft=${quantities(draft)} committed=${quantities(committed)} changed=$enabled removal=$remove cartEmpty=$cartEmpty")

            sheet.counter.text = "$used/$limit"
            sheet.counter.setTextColor(color(if (locked) UIKitR.color.black_40 else UIKitR.color.primary_30))
            sheet.counter.backgroundTintList = if (locked) colorStateList(UIKitR.color.black_20) else null
            sheet.update.text = when {
                remove -> "Hapus"
                cartEmpty -> "Keranjang"
                else -> "Perbarui Keranjang"
            }
            if (cartEmpty) sheet.update.setIconResource(UIKitR.drawable.ic_plus_stepper) else sheet.update.icon = null
            sheet.update.backgroundTintList = if (remove) destructiveTint else defaultTint
            sheet.update.isEnabled = enabled
            sheet.update.refreshDrawableState()
            adapter.items = draft.map {
                RedeemRow(it, canAdd = used < limit, locked = locked, remaining = (limit - used).coerceAtLeast(0))
            }
            sheetBehavior?.isHideable = !enabled
        }

        fun commit() {
            val removal = isRemoval()
            val removedQty = committed.sumOf { it.quantity } - draft.sumOf { it.quantity }

            state = state.withClaimedGifts(draft.flatMap { d -> List(d.quantity) { d.id } })
            committed = draft
            render(announce = true)

            tray?.dismiss()
            showSkeleton {
                Snackbar.make(
                    binding.main,
                    if (removal) "$removedQty produk telah dihapus" else "Keranjang berhasil diperbarui",
                    Snackbar.LENGTH_SHORT
                ).apply {
                    view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        leftMargin = 16.dp.toInt()
                        rightMargin = 16.dp.toInt()
                        bottomMargin = 8.dp.toInt()
                    }
                }.show()
            }
        }

        fun requestClose() {
            if (hasChanges()) showDiscardConfirmation { tray?.dismiss() } else tray?.dismiss()
        }

        sheet.update.setOnClickListener { commit() }
        sheet.list.layoutManager = LinearLayoutManager(this)
        sheet.list.adapter = adapter
        sheet.list.itemAnimator = null
        refresh()
        sheet.update.setOnClickListener { commit() }

        tray = BottomSheetTray.newInstance(title = state.fairName, contentLayout = sheet.root).apply {
            dragHandleVisibility = true
            titleDividerVisibility = true
            shouldShowClose = true
            customAnimationsEnabled = motionEnabled
        }
        tray.show(supportFragmentManager, "fair-redemption")
        supportFragmentManager.executePendingTransactions()

        (tray.dialog as? BottomSheetDialog)?.let { dialog ->

            dialog.onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = requestClose()
            })

            dialog.findViewById<View>(com.google.android.material.R.id.touch_outside)?.setOnClickListener { requestClose() }

            sheetBehavior = dialog.behavior
            dialog.behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_COLLAPSED && hasChanges()) {
                        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        requestClose()
                    }
                }
                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })
            hookTrayClose(dialog, sheet.root) { requestClose() }
            refresh()
        }
    }

    private fun setCartVisible(show: Boolean) {
        if (show == cartShown) return
        cartShown = show
        val cart = binding.cart
        cart.animate().cancel()
        cartMove?.cancel()
        if (!motionEnabled) {
            cart.alpha = 1f; cart.translationY = 0f; cart.isVisible = show
            return
        }
        if (show) {
            if (!cart.isVisible) {
                cart.isVisible = true
                cart.alpha = 0f
                cart.translationY = 16.dp
            }
            cart.animate().alpha(1f).setDuration(50).setInterpolator(
                AccelerateDecelerateInterpolator()).start()
            cartMove = ObjectAnimator.ofFloat(cart, View.TRANSLATION_Y, cart.translationY, 0f).apply {
                duration = 320L
                interpolator = OvershootInterpolator(1f)
                start()
            }
        } else {
            cart.animate().alpha(0f).translationY(16.dp).setDuration(200).setInterpolator(
                AccelerateDecelerateInterpolator())
                .withEndAction {
                    if (!cartShown) { cart.isVisible = false; cart.alpha = 1f; cart.translationY = 0f }
                }.start()
        }
    }

    private fun showSearch() {
        if (!inSearch) detailScroll = binding.products.layoutManager?.onSaveInstanceState()
        beginEditing()
    }

    private fun configureSearchField() {
        binding.queryField.apply {
            shouldAnimatePlaceholder = false
            placeholderTexts = arrayOf(getString(R.string.fair_search_hint))
            startIcon = R.drawable.fair_search
            placeholderTextAppearance = R.style.FairDemo_Caption
            editText.isSaveEnabled = false
            editText.setPadding(editText.paddingLeft, 0, editText.paddingRight, 0)
            editText.imeOptions = EditorInfo.IME_ACTION_SEARCH
            editText.isSingleLine = true
            editText.setOnClickListener { if (!editingSearch) beginEditing() }
            editText.setOnEditorActionListener { _, action, event ->
                if (action == EditorInfo.IME_ACTION_SEARCH || (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP)) {
                    submitSearch(); true
                } else false
            }
            val inner = SearchBarBinding.bind(getChildAt(0))
            inner.ivStartIcon.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            inner.ivEndIcon.contentDescription = getString(R.string.fair_search_clear)
            buttonSemantics(inner.ivEndIcon)
            searchBarDelegate = object : SearchBarDelegate {
                override fun onCloseIconClicked(view: View) {
                    updatingQuery = true
                    text = ""
                    updatingQuery = false
                    beginEditing("")
                }
                override fun onFocusChange(view: View, hasFocus: Boolean) {
                    if (hasFocus && inSearch && !editingSearch && !updatingQuery) beginEditing()
                }
                override fun onTextChange(text: String) {
                    inner.ivEndIcon.isVisible = text.isNotEmpty()
                    if (updatingQuery || !inSearch) return
                    cancelSearchRequest()
                    searchState = searchState.edit(text)
                    render()
                }
            }
            inner.ivEndIcon.isVisible = false
        }
    }

    private fun beginEditing(query: String = searchState.draft) {
        if (searchState.mode in listOf(SearchMode.RESULTS, SearchMode.EMPTY, SearchMode.ERROR))
            resultScroll = binding.products.layoutManager?.onSaveInstanceState()
        cancelSearchRequest()
        searchState = searchState.edit(query)
        render()
        binding.products.scrollToPosition(0)
        focusSearch()
    }

    private fun focusSearch() {
        binding.queryField.post {
            if (editingSearch && !isFinishing) {
                binding.queryField.editText.requestFocus()
                binding.queryField.editText.setSelection(binding.queryField.editText.text.length)
                ViewCompat.getWindowInsetsController(binding.queryField)?.show(WindowInsetsCompat.Type.ime())
            }
        }
    }

    private fun submitSearch() {
        if (PromoSearchState.normalize(searchState.draft).isEmpty()) { focusSearch(); return }
        cancelSearchRequest()
        searchState = searchState.submit()
        updatingQuery = true
        binding.queryField.editText.clearFocus()
        updatingQuery = false
        ViewCompat.getWindowInsetsController(binding.queryField)?.hide(WindowInsetsCompat.Type.ime())
        render()
        binding.products.scrollToPosition(0)
        val request = searchState.generation
        val simulateFailure = failNextSearch
        failNextSearch = false

        pendingSearch = Runnable {
            if (isDestroyed) return@Runnable
            val matches = searchMatches()
            searchState = if (simulateFailure) searchState.fail(request)
            else searchState.complete(request, matches.size, PromoSearchState.websiteFallback(searchState.submitted))
            if (searchState.generation != request || editingSearch || !inSearch) return@Runnable
            pendingSearch = null
            render()
            scrollToNewSearchResults()
            binding.products.announceForAccessibility(if (searchState.mode == SearchMode.ERROR) getString(R.string.fair_search_error_title)
            else getString(R.string.fair_search_results, matches.size, searchState.submitted))
        }.also { searchHandler.postDelayed(it, 220) }
    }

    private fun searchMatches() = PromoSearchState.matching(state.products, searchState.submitted)

    private fun cancelSearchRequest() {
        searchScrollObserver?.let { binding.products.adapter?.unregisterAdapterDataObserver(it) }
        searchScrollObserver = null
        pendingSearch?.let { searchHandler.removeCallbacks(it) }
        pendingSearch = null
    }

    private fun scrollToNewSearchResults() {
        val adapter = binding.products.adapter ?: return
        val request = searchState.generation
        fun checkCommitted() {
            val expectedSection = if (searchState.mode == SearchMode.RESULTS) 1 else 0
            val expectedEmpty = if (searchState.mode != SearchMode.RESULTS || expectedProductIds.isEmpty()) 1 else 0
            if (request != searchState.generation || !inSearch || editingSearch) return
            if (scopeAdapter.itemCount != 0 || headerAdapter.itemCount != 0 || loadingAdapter.itemCount != 0 ||
                sectionAdapter.itemCount != expectedSection || emptyAdapter.itemCount != expectedEmpty ||
                productAdapter.items.map { it.id } != expectedProductIds) return
            searchScrollObserver?.let { adapter.unregisterAdapterDataObserver(it) }
            searchScrollObserver = null
            binding.products.post {
                if (request == searchState.generation && inSearch && !editingSearch)
                    (binding.products.layoutManager as StaggeredGridLayoutManager).scrollToPositionWithOffset(0, 0)
            }
        }
        searchScrollObserver = object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = checkCommitted()
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = checkCommitted()
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) = checkCommitted()
            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) = checkCommitted()
            override fun onChanged() = checkCommitted()
        }.also { adapter.registerAdapterDataObserver(it) }
        checkCommitted()
    }

    private fun closeSearch() {
        cancelSearchRequest()
        ViewCompat.getWindowInsetsController(binding.queryField)?.hide(WindowInsetsCompat.Type.ime())
        binding.queryField.editText.clearFocus()
        searchState = PromoSearchState()
        resultScroll = null
        compactVisible = false
        binding.compactProgress.isVisible = false
        render()
        restoreScroll(detailScroll)
        binding.search.requestFocus()
    }

    private fun restoreScroll(scroll: Parcelable?) {
        if (scroll == null) return

        val adapter = binding.products.adapter ?: return
        var registered = true
        val observer = object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (registered) adapter.unregisterAdapterDataObserver(this)
                registered = false
                binding.products.post { binding.products.layoutManager?.onRestoreInstanceState(scroll) }
            }
        }
        adapter.registerAdapterDataObserver(observer)
        binding.products.postDelayed({
            if (registered) adapter.unregisterAdapterDataObserver(observer)
            registered = false
            binding.products.layoutManager?.onRestoreInstanceState(scroll)
        }, 180)
    }

    private fun bindSearchEmpty(view: ItemFairSearchEmptyBinding, row: EmptyRow) {
        emptyConstraints.applyTo(view.messagePanel)
        view.action.setTextAppearance(R.style.FairDemo_CaptionStrong)
        view.action.setTextColor(color(UIKitR.color.white))
        val error = row.mode == SearchMode.ERROR
        view.heading.setText(when { error -> R.string.fair_search_error_title; row.filtered -> R.string.fair_search_filtered_title; else -> R.string.fair_search_empty_title })
        view.description.setText(when { error -> R.string.fair_search_error_description; row.filtered -> R.string.fair_search_filtered_description; else -> R.string.fair_search_empty_description })
        view.action.setText(when { error -> R.string.fair_search_retry; row.filtered -> R.string.fair_search_clear_filter; else -> R.string.fair_search_edit })
        view.illustration.isVisible = !error
        view.websiteBanner.isVisible = row.fallback && !error
        view.divider.isVisible = row.fallback && !error
        ViewCompat.setAccessibilityHeading(view.heading, true)
        view.root.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        buttonSemantics(view.action)
        buttonSemantics(view.websiteBanner)
        if (resources.configuration.fontScale > 1.4f || error) {
            ConstraintSet().apply {
                clone(view.messagePanel)
                clear(R.id.heading, ConstraintSet.BOTTOM)
                connect(R.id.heading, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                connect(R.id.heading, ConstraintSet.TOP, if (error) ConstraintSet.PARENT_ID else R.id.illustration,
                    if (error) ConstraintSet.TOP else ConstraintSet.BOTTOM, if (error) 0 else 12.dp.toInt())
                clear(R.id.description, ConstraintSet.BOTTOM)
                applyTo(view.messagePanel)
            }
        }
        if (resources.configuration.fontScale > 1.4f) {

            view.action.setTextSize(android.util.TypedValue.COMPLEX_UNIT_DIP, 12f)
            TextViewCompat.setLineHeight(view.action, 16.dp.toInt())
        }
        view.action.setOnClickListener {
            when {
                error -> submitSearch()
                row.filtered -> { brand = "Semua"; render() }
                else -> beginEditing(searchState.submitted)
            }
        }
        view.websiteBanner.setOnClickListener { openWebsite() }
    }

    private fun buttonSemantics(view: View) {
        ViewCompat.setAccessibilityDelegate(view, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.className = "android.widget.Button"
            }
        })
    }

    private class SelectionTag(val itemId: String, var selected: Boolean, var strokeAnimator: ValueAnimator? = null)

    private fun applySelection(v: ItemFairRedeemProductBinding, itemId: String, selected: Boolean) {
        val card = v.root
        val mark = v.selectedMark
        val previous = card.tag as? SelectionTag

        val sameItem = previous != null && previous.itemId == itemId
        val animated = motionEnabled && sameItem && previous!!.selected != selected

        previous?.strokeAnimator?.cancel()
        mark.animate().cancel()

        val tag = if (sameItem) previous!! else SelectionTag(itemId, selected)
        tag.selected = selected
        tag.strokeAnimator = null
        card.tag = tag

        val newBorder = color(if (selected) UIKitR.color.primary_30 else UIKitR.color.black_30)
        if (animated) {
            tag.strokeAnimator = ValueAnimator.ofArgb(card.strokeColor, newBorder).apply {
                duration = 250L
                addUpdateListener { card.setStrokeColor(it.animatedValue as Int) }
                start()
            }
        } else {
            card.setStrokeColor(newBorder)
        }

        val offscreen = -(if (mark.width > 0) mark.width else (32 * resources.displayMetrics.density).toInt()).toFloat()
        if (selected) {
            mark.isVisible = true
            if (animated) {
                mark.alpha = 0f
                mark.translationX = offscreen
                mark.translationY = offscreen
                mark.animate()
                    .alpha(1f).translationX(0f).translationY(0f)
                    .setDuration(250L).setInterpolator(DecelerateInterpolator())
                    .start()
            } else {
                mark.alpha = 1f
                mark.translationX = 0f
                mark.translationY = 0f
            }
        } else if (animated) {
            mark.animate()
                .alpha(0f).translationX(offscreen).translationY(offscreen)
                .setDuration(200L).setInterpolator(AccelerateInterpolator())
                .withEndAction { if (!tag.selected) mark.isVisible = false }
                .start()
        } else {
            mark.isVisible = false
            mark.alpha = 1f
            mark.translationX = 0f
            mark.translationY = 0f
        }
    }

    private fun showGeneralSearchHandoff() {
        ViewCompat.getWindowInsetsController(binding.queryField)?.hide(WindowInsetsCompat.Type.ime())
        showOptions("Pencarian Klik Indomaret", "Kata kunci: ${PromoSearchState.normalize(searchState.draft)}\nHandoff demo. Halaman pencarian umum tidak disediakan dalam frame Figma ini.",
            listOf(Option("website", "Buka situs Klik Indomaret"))) { openWebsite() }
    }

    private fun openWebsite() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.klikindomaret.com"))
        try { startActivity(intent) }
        catch (_: ActivityNotFoundException) {
            showOptions("Klik Indomaret", "www.klikindomaret.com\nTidak ada browser yang tersedia di perangkat ini.", emptyList()) {}
        }
    }

    private fun showDiscardConfirmation(onDiscard: () -> Unit) {
        if (supportFragmentManager.findFragmentByTag("fair-discard") != null) return
        val view = SheetFairDiscardBinding.inflate(inflater)
        var confirm: BottomSheetTray? = null
        view.stay.setOnClickListener { confirm?.dismiss() }
        view.discard.setOnClickListener { confirm?.dismiss(); onDiscard() }
        confirm = BottomSheetTray.newInstance(title = "Perubahan Belum Disimpan", contentLayout = view.root).apply {
            dragHandleVisibility = false
            titleDividerVisibility = true
            shouldShowClose = false
            customAnimationsEnabled = motionEnabled
        }
        confirm.show(supportFragmentManager, "fair-discard")
    }

    private fun hookTrayClose(dialog: Dialog, ownContent: View, onClose: () -> Unit) {
        val decor = dialog.window?.decorView ?: return
        val scrim = dialog.findViewById<View>(com.google.android.material.R.id.touch_outside)
        val density = resources.displayMetrics.density
        val min = (24 * density).toInt()
        val max = (64 * density).toInt()

        decor.post {
            val candidates = mutableListOf<View>()

            fun idName(v: View) = if (v.id == View.NO_ID) "-" else runCatching { resources.getResourceEntryName(v.id) }.getOrDefault("?")

            fun walk(v: View, depth: Int) {
                if (v === ownContent || v === scrim) return
                android.util.Log.d("FairDemo", "tray view: ${"  ".repeat(depth)}${v.javaClass.simpleName} id=${idName(v)} clickable=${v.isClickable} ${v.width}x${v.height}")
                if (v.isShown && v.isClickable && v.width in min..max && v.height in min..max) candidates += v
                if (v is ViewGroup) for (i in 0 until v.childCount) walk(v.getChildAt(i), depth + 1)
            }
            walk(decor, 0)

            val close = candidates.maxByOrNull { v -> IntArray(2).also { v.getLocationOnScreen(it) }[0] }
            if (close != null) close.setOnClickListener { onClose() }
            else android.util.Log.w("FairDemo", "tray close button not found; ✕ is not guarded")
        }
    }

    private inner class ProductSpacingDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: android.graphics.Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            if (position == RecyclerView.NO_POSITION) { outRect.set(0, 0, 0, 0); return }

            val firstProduct = headerAdapter.itemCount + scopeAdapter.itemCount + sectionAdapter.itemCount
            val lastProduct = firstProduct + productAdapter.itemCount
            val half = gridGap / 2

            if (position in firstProduct until lastProduct) {
                outRect.set(half, half, half, half)                        // same on every side: no column or span lookup needed
            } else {
                outRect.set(-(gridEdge - half), 0, -(gridEdge - half), 0)  // header, section, empty and loading rows cancel the list padding
            }
        }
    }

    private fun showScenarios() {
        showOptions("Skenario demo", "Pilih kondisi promo untuk menguji tampilan dan interaksi.", DemoScenario.entries.map { Option(it.name, it.label) }) {

            cancelSearchRequest()
            searchState = PromoSearchState()
            state = scenarioState(DemoScenario.valueOf(it.id))
            brand = "Semua"
            sortOrder = "Relevansi"

            displayedProgress = state.barProgress
            render()
            binding.products.scrollToPosition(0)
            if (it.id.startsWith("SEARCH_")) showSearch()
        }
    }

    private fun showOptions(title: String, description: String, options: List<Option>, onSelect: (Option) -> Unit) {
        val view = SheetFairOptionsBinding.inflate(inflater)
        view.description.text = description
        view.options.layoutManager = LinearLayoutManager(this)
        view.options.updateLayoutParams<ConstraintLayout.LayoutParams> {
            matchConstraintMaxHeight = (binding.main.height * 0.55f).toInt()
        }
        var tray: BottomSheetTray? = null
        val adapter = BaseAdapter.adapterOf<Option, ItemFairOptionBinding>(
            register = BaseAdapter.Register(onBindHolder = { _, option, row, _ ->
                row.label.text = option.label
                row.image.isVisible = option.image != null
                option.image?.let { row.image.setImageResource(it) }
                row.check.visibility = if (option.selected) View.VISIBLE else View.INVISIBLE
                row.root.contentDescription = option.label
                row.label.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                row.root.isSelected = option.selected
            }),
            diff = BaseAdapter.Diff(areItemsTheSame = { a, b -> a.id == b.id }, areContentsTheSame = { a, b -> a == b }),
            itemList = options
        )
        adapter.setOnItemClickListener { _, option, _ ->
            tray?.dismiss()
            binding.root.post { onSelect(option) }
        }
        view.options.adapter = adapter
        view.options.isVisible = options.isNotEmpty()
        tray = BottomSheetTray.newInstance(title = title, contentLayout = view.root).apply {
            dragHandleVisibility = true
            shouldShowClose = true
            customAnimationsEnabled = motionEnabled
        }
        tray.show(supportFragmentManager, "fair-options")
    }

    private fun scenarioState(scenario: DemoScenario): PromoDemoState {
        promotion = if (scenario == DemoScenario.SEARCH_REDEMPTION || scenario == DemoScenario.START) DemoPromotion.REDEMPTION else DemoPromotion.FREE_GIFT
        failNextSearch = scenario == DemoScenario.SEARCH_ERROR
        var model = PromoDemoState(fixtureProducts(), giftOffers = giftOffers())

        if (promotion == DemoPromotion.REDEMPTION) {
            model = model.copy(
                threshold = 50_000L,
                rule = if (scenario == DemoScenario.SEARCH_REDEMPTION) RewardRule.REPEATABLE else RewardRule.ONE_TIME
            )
        }
        if (scenario in listOf(DemoScenario.REPEATABLE, DemoScenario.REPEATABLE_PARTIAL, DemoScenario.REPEATABLE_THREE)) model = model.copy(rule = RewardRule.REPEATABLE)
        val quantity = when (scenario) {
            DemoScenario.HALFWAY, DemoScenario.SEARCH_GIFT, DemoScenario.SEARCH_ERROR -> 1
            DemoScenario.ELIGIBLE, DemoScenario.CLAIMED, DemoScenario.QUOTA, DemoScenario.LIMIT -> 2
            DemoScenario.REPEATABLE_PARTIAL -> 4
            DemoScenario.REPEATABLE_THREE -> 6
            else -> 0
        }
        model = model.withQuantity("gel", quantity)
        if (scenario == DemoScenario.SEARCH_REDEMPTION) model = model.withQuantity("wash", 1)
        if (scenario in listOf(DemoScenario.CLAIMED, DemoScenario.REPEATABLE_PARTIAL)) model = model.claimGift("gel")
        if (scenario == DemoScenario.REPEATABLE_THREE) repeat(3) { model = model.claimGift("gel") }
        return when (scenario) {
            DemoScenario.QUOTA -> model.copy(availability = PromoAvailability.QUOTA_EXHAUSTED)
            DemoScenario.LIMIT -> model.copy(availability = PromoAvailability.LIMIT_REACHED)
            DemoScenario.NEW_USER -> model.copy(availability = PromoAvailability.NEW_CUSTOMER_ONLY, otherCartCount = 3, otherCartTotal = 999_999)
            DemoScenario.ZERO_PRODUCTS -> model.copy(products = emptyList())
            else -> model
        }
    }

    private fun fixtureProducts(): List<DemoProduct> = if (promotion == DemoPromotion.REDEMPTION) redemptionProducts() else listOf(
        DemoProduct("wash", "Emina Bright Stuff Face Wash 100ml", R.drawable.fair_emina_wash),
        DemoProduct("gel", "Emina Water Barrier Balancing Gel 30g", R.drawable.fair_emina_gel, discounted = false, promotion = "Banyak Lebih Hemat", benefit = "+2 Stamp"),
        DemoProduct("cooling", "Kahf Anti Perspirant Deodorant Cooling Power 45ml", R.drawable.fair_kahf_cooling),
        DemoProduct("dry", "Kahf Anti Perspirant Deodorant Extra Dry Power 45ml", R.drawable.fair_kahf_dry, promotion = "Banyak Lebih Hemat"),
        DemoProduct("dry-second", "Kahf Anti Perspirant Deodorant Extra Dry Power 45ml", R.drawable.fair_kahf_dry),
        DemoProduct("cooling-second", "Kahf Anti Perspirant Deodorant Cooling Power 45ml", R.drawable.fair_kahf_cooling),
        DemoProduct("wash-second", "Emina Bright Stuff Face Wash 100ml", R.drawable.fair_emina_wash),
        DemoProduct("gel-second", "Emina Water Barrier Balancing Gel 30g", R.drawable.fair_emina_gel, discounted = false, promotion = "Banyak Lebih Hemat", benefit = "+2 Stamp")
    )

    private fun redemptionProducts() = listOf(
        DemoProduct("wash", "Emina Bright Stuff Face Wash 100ml", R.drawable.fair_emina_wash, qualifies = true),
        DemoProduct("aceh", "Indomie Mi Instan Goreng Aceh 90g", R.drawable.promo_product_aceh, price = 10_000, originalPrice = 6_400, promotion = "", benefit = "Beli 5, +99 Poin", highlight = ""),
        DemoProduct("special", "Indomie Mi Instan Goreng Plus Special 80g", R.drawable.promo_product_mi_goreng, price = 10_000, discounted = false, promotion = "Banyak Lebih Hemat", benefit = "Beli 3, +10 Stamp", highlight = "Paket Bundling"),
        DemoProduct("ayam", "Indomie Mi Instan Goreng Ayam Geprek 85g", R.drawable.promo_product_ayam, price = 3_200, originalPrice = 6_400, promotion = "", benefit = "+99 Poin", highlight = ""),
        DemoProduct("jumbo", "Indomie Mi Instan Goreng Jumbo Special 129g", R.drawable.promo_product_jumbo, price = 10_000, discounted = false, promotion = "Paket Bundling", benefit = "Beli 3, +10 Stamp", highlight = "Banyak Lebih Hemat")
    )

    private fun redemptionOffers(): List<RedeemItem> {
        val original = if (promotion == DemoPromotion.FREE_GIFT) 50_000L else 10_000L
        val discountPercent = 50
        val price = original * (100 - discountPercent) / 100
        return listOf(
            RedeemItem("gel", "Emina Water Barrier Balancing Gel 30g", R.drawable.fair_emina_gel, packSize = 2, originalPrice = original, price = price),
            RedeemItem("scrub", "Kahf Face Wash Scrub Acne & Pore", R.drawable.fair_gift_cotton, originalPrice = original, price = price),
            RedeemItem("sunscreen", "Wardah UV Shield Airy Sunscreen", R.drawable.fair_gift_cleanser, originalPrice = original, price = price, restocking = true)
        )
    }

    private fun giftOffers() = redemptionOffers().map {
        GiftOffer(it.id, it.name, it.image, it.packSize, it.originalPrice, it.price, it.restocking)
    }

    private fun money(value: Long): String = "Rp${rupiah.format(value)}"

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("promotion", promotion.name)
        outState.putLong("threshold", state.threshold)
        outState.putString("searchMode", searchState.mode.name)
        outState.putString("searchDraft", searchState.draft)
        outState.putString("searchSubmitted", searchState.submitted)
        outState.putBoolean("searchFallback", searchState.fallbackAvailable)
        outState.putBoolean("searchHasSubmission", searchState.hasSubmission)
        outState.putBoolean("searchFailNext", failNextSearch)
        outState.putParcelable("detailScroll", detailScroll)
        outState.putParcelable("resultScroll", resultScroll)
        outState.putString("rule", state.rule.name)
        outState.putString("availability", state.availability.name)
        outState.putIntArray("quantities", state.products.map { it.quantity }.toIntArray())
        outState.putStringArrayList("gifts", ArrayList(state.claimedGifts))
        outState.putBoolean("empty", state.products.isEmpty())
        outState.putInt("otherCount", state.otherCartCount)
        outState.putLong("otherTotal", state.otherCartTotal)
        outState.putInt("columns", columns)
        outState.putString("brand", brand)
        outState.putString("sort", sortOrder)
        super.onSaveInstanceState(outState)
    }

    private fun restoreState(saved: Bundle?): PromoDemoState? {
        if (saved == null) return null
        val quantities = saved.getIntArray("quantities") ?: return null
        val products = if (saved.getBoolean("empty")) emptyList() else fixtureProducts().mapIndexed { i, p -> p.copy(quantity = quantities.getOrElse(i) { 0 }) }
        return PromoDemoState(products, RewardRule.valueOf(saved.getString("rule") ?: "ONE_TIME"),
            PromoAvailability.valueOf(saved.getString("availability") ?: "ACTIVE"), saved.getStringArrayList("gifts") ?: emptyList(),
            threshold = saved.getLong("threshold", 100_000L), otherCartCount = saved.getInt("otherCount"), otherCartTotal = saved.getLong("otherTotal"),
            giftOffers = giftOffers())
    }

    override fun onDestroy() {
        cancelSearchRequest()
        searchHandler.removeCallbacksAndMessages(null)
        header = null
        skeletonFinish?.let { binding.skeleton.root.removeCallbacks(it) }
        skeletonPulse?.cancel()
        binding.products.removeCallbacks(clearFocusTask)
        super.onDestroy()
    }

    private data class HeaderRow(val state: PromoDemoState, val id: String = "header")
    private data class SectionRow(val count: Int, val columns: Int, val id: String = "section")
    private data class ProductRow(val product: DemoProduct, val availability: PromoAvailability, val columns: Int, val search: Boolean, val promotion: DemoPromotion, val focused: Boolean = false) { val id get() = product.id }
    private data class Option(val id: String, val label: String, val image: Int? = null, val selected: Boolean = false)

    private data class ScopeRow(val id: String, val query: String, val description: String)
    private data class EmptyRow(val mode: SearchMode, val fallback: Boolean, val filtered: Boolean = false, val id: String = "empty")

    private data class RedeemItem(
        val id: String, val name: String, val image: Int,
        val packSize: Int = 1,
        val originalPrice: Long = 10_000, val price: Long = 5_000,
        val restocking: Boolean = false, val quantity: Int = 0
    )
    private data class RedeemRow(val item: RedeemItem, val canAdd: Boolean, val locked: Boolean, val remaining: Int) { val id get() = item.id }

    private enum class StepperFace { ADD, FOCUSED, COUNT }

    private class FaceTag(val productId: String, val face: StepperFace, var animator: AnimatorSet? = null, var cancelled: Boolean = false)

    companion object { const val EXTRA_SEARCH = "fair_search_demo"; const val EXTRA_SCENARIO = "fair_scenario" }
}
