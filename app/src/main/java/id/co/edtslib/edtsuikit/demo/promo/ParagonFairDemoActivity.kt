package id.co.edtslib.edtsuikit.demo.promo

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.view.animation.PathInterpolator
import android.view.inputmethod.EditorInfo
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import id.co.edtslib.edtsuikit.GuidelinesBaseActivity
import id.co.edtslib.edtsuikit.R
import id.co.edtslib.edtsuikit.databinding.ActivityParagonFairDemoBinding
import id.co.edtslib.edtsuikit.databinding.ItemFairHeaderBinding
import id.co.edtslib.edtsuikit.databinding.ItemFairOptionBinding
import id.co.edtslib.edtsuikit.databinding.ItemFairProductBinding
import id.co.edtslib.edtsuikit.databinding.ItemFairRedeemProductBinding
import id.co.edtslib.edtsuikit.databinding.ItemFairSearchEmptyBinding
import id.co.edtslib.edtsuikit.databinding.ItemFairSearchLoadingBinding
import id.co.edtslib.edtsuikit.databinding.ItemFairSearchScopeBinding
import id.co.edtslib.edtsuikit.databinding.ItemFairSectionBinding
import id.co.edtslib.edtsuikit.databinding.SheetFairDiscardBinding
import id.co.edtslib.edtsuikit.databinding.SheetFairOptionsBinding
import id.co.edtslib.edtsuikit.databinding.SheetFairRedemptionBinding
import id.co.edtslib.edtsuikit.databinding.ViewFairProgressBinding
import id.co.edtslib.edtsuikit.tmh.TmhBottomSheet
import id.co.edtslib.edtsuikit.tmh.TmhFair
import id.co.edtslib.edtsuikit.tmh.TmhCatalogFixtures
import id.co.edtslib.edtsuikit.tmh.TmhFlowState
import id.co.edtslib.edtsuikit.tmh.TmhRedemptionBottomSheet
import id.co.edtslib.edtsuikit.tmh.TmhTermsBottomSheet
import id.co.edtslib.edtsuikit.tmh.alertText
import id.co.edtslib.edtsuikit.tmh.quantities
import id.co.edtslib.edtsuikit.viewBinding
import id.co.edtslib.uikit.adapter.BaseAdapter
import id.co.edtslib.uikit.databinding.SearchBarBinding
import id.co.edtslib.uikit.recipes.textbadge.klik.KlikVariant
import id.co.edtslib.uikit.searchbar.SearchBarDelegate
import id.co.edtslib.uikit.stepper.StepperButton
import id.co.edtslib.uikit.tray.BottomSheetTray
import id.co.edtslib.uikit.utils.TextStyleKey
import id.co.edtslib.uikit.utils.TextStyleProvider.get
import id.co.edtslib.uikit.utils.buildHighlightedMessage
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.colorStateList
import id.co.edtslib.uikit.utils.dimenPixelSize
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.inflater
import id.co.edtslib.uikit.utils.window.WindowInsetsConfig
import java.text.NumberFormat
import java.util.Locale
import id.co.edtslib.uikit.R as UIKitR

class ParagonFairDemoActivity : GuidelinesBaseActivity() {

    override val navigationBarScrimColor: Int = Color.WHITE

    override val windowInsetsConfig = WindowInsetsConfig.Custom { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
        view.setPadding(bars.left, bars.top, bars.right, maxOf(bars.bottom, ime.bottom))
        insets
    }

    private val binding by viewBinding<ActivityParagonFairDemoBinding>()

    private var state = PromoDemoState(products = emptyList())
    private var flow: TmhFlowState? = null
    private var fairId: String? = null
    private val integratedFair: TmhFair?
        get() = fairId?.let { flow?.fair(it) }

    private var promotion = DemoPromotion.REDEMPTION

    private var searchState = PromoSearchState()

    private val searchHandler = Handler(Looper.getMainLooper())

    private var pendingSearch: Runnable? = null

    private var searchScrollObserver: RecyclerView.AdapterDataObserver? = null

    private var expectedProductIds = emptyList<String>()

    private var isUpdatingQuery = false

    private var shouldFailNextSearch = false

    private var detailScroll: Parcelable? = null

    private var resultScroll: Parcelable? = null

    private var lastRenderedMode = SearchMode.DETAIL

    private val emptyConstraints by lazy {
        ConstraintSet().apply { clone(ItemFairSearchEmptyBinding.inflate(inflater).messagePanel) }
    }

    private val isInSearch
        get() = searchState.mode != SearchMode.DETAIL

    private val isEditingSearch
        get() = searchState.mode == SearchMode.EDITING

    private val promotionTitle
        get() = if (flow != null) {
            getString(R.string.tmh_promo_rule, state.fairName, money(state.threshold), TmhFlowState.MAXIMUM_REDEMPTIONS)
        } else {
            getString(
                if (promotion == DemoPromotion.FREE_GIFT) R.string.fair_search_gift_title
                else R.string.fair_search_redemption_title,
            )
        }

    private val searchBack =
        object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                val isImeVisible =
                    ViewCompat.getRootWindowInsets(binding.root)?.isVisible(WindowInsetsCompat.Type.ime()) == true
                when {
                    isEditingSearch && isImeVisible ->
                        ViewCompat.getWindowInsetsController(binding.queryField)?.hide(WindowInsetsCompat.Type.ime())
                    isEditingSearch && searchState.hasSubmission -> {
                        cancelSearchRequest()
                        searchState =
                            searchState.copy(
                                mode = if (searchMatches().isEmpty()) SearchMode.EMPTY else SearchMode.RESULTS,
                                draft = searchState.submitted)
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

    private var brand = FILTER_ALL

    private var sortOrder = SORT_RELEVANCE

    private val shown = java.util.WeakHashMap<View, Float>()

    private var displayedProgress = 0f

    private var focusedProductId: String? = null

    private val clearFocusTask = Runnable { applyFocus(null) }

    private var isCompactVisible = false

    private val easing = PathInterpolator(0.2f, 0.8f, 0.2f, 1f)

    private var isCartShown = false

    private var cartMove: ObjectAnimator? = null

    private val rupiah = NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID"))

    private val isMotionEnabled: Boolean
        get() = Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f

    private val effectiveColumns: Int
        get() = if (resources.configuration.fontScale > 1.4f) 1 else columns

    private var skeletonPulse: ObjectAnimator? = null

    private var skeletonFinish: Runnable? = null

    private val headerAdapter by lazy {
        BaseAdapter.adapterOf<HeaderRow, ItemFairHeaderBinding>(
            register = BaseAdapter.Register(onBindHolder = { _, item, view, _ -> bindHeader(view, item.state) }),
            diff =
                BaseAdapter.Diff(
                    areItemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
                    areContentsTheSame = { oldItem, newItem -> oldItem == newItem }))
    }

    private val sectionAdapter by lazy {
        BaseAdapter.adapterOf<SectionRow, ItemFairSectionBinding>(
            register =
                BaseAdapter.Register(
                    onBindHolder = { _, item, view, _ ->
                        view.count.setTextAppearance(
                            if (isInSearch) R.style.FairDemo_CaptionStrong else R.style.FairDemo_Caption)
                        view.displayLabel.setTextAppearance(
                            if (isInSearch) R.style.FairDemo_CaptionStrong else R.style.FairDemo_Caption)
                        view.heading.text = if (isInSearch) promotionTitle else getString(R.string.fair_products)
                        view.count.text = getString(R.string.fair_product_count, item.count)
                        view.grid.makeStatic()
                        view.list.makeStatic()


                        ViewCompat.setAccessibilityHeading(view.heading, true)
                        view.root.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                    }),
            diff =
                BaseAdapter.Diff(
                    areItemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
                    areContentsTheSame = { oldItem, newItem -> oldItem == newItem }))
    }

    private val productAdapter by lazy {
        BaseAdapter.adapterOf<ProductRow, ItemFairProductBinding>(
            register = BaseAdapter.Register(onBindHolder = { _, item, view, _ -> bindProduct(view, item) }),
            diff =
                BaseAdapter.Diff(
                    areItemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
                    areContentsTheSame = { oldItem, newItem -> oldItem == newItem }))
    }

    private val scopeAdapter by lazy {
        BaseAdapter.adapterOf<ScopeRow, ItemFairSearchScopeBinding>(
                register =
                    BaseAdapter.Register(
                        onBindHolder = { _, row, view, _ ->
                            view.query.text = row.query
                            view.scope.text = row.description
                            view.root.contentDescription =
                                getString(R.string.fair_search_scope_description, row.query, row.description)
                            ViewCompat.setScreenReaderFocusable(view.root, true)
                            view.root.setOnLongClickListener {
                                if (row.id == "promo") submitSearch() else showGeneralSearchHandoff()
                                true
                            }
                        }),
                diff =
                    BaseAdapter.Diff(
                        areItemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
                        areContentsTheSame = { oldItem, newItem -> oldItem == newItem }))
            .apply {
                setOnItemClickListener { _, row, _ ->
                    if (row.id == "promo") submitSearch() else showGeneralSearchHandoff()
                }
            }
    }

    private val emptyAdapter by lazy {
        BaseAdapter.adapterOf<EmptyRow, ItemFairSearchEmptyBinding>(
            register = BaseAdapter.Register(onBindHolder = { _, row, view, _ -> bindSearchEmpty(view, row) }),
            diff =
                BaseAdapter.Diff(
                    areItemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
                    areContentsTheSame = { oldItem, newItem -> oldItem == newItem }))
    }

    private val loadingAdapter by lazy {
        BaseAdapter.adapterOf<String, ItemFairSearchLoadingBinding>(
            register =
                BaseAdapter.Register(
                    onBindHolder = { _, _, view, _ ->
                        view.spinner.isVisible = isMotionEnabled
                        view.root.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                    }),
            diff =
                BaseAdapter.Diff(
                    areItemsTheSame = { oldItem, newItem -> oldItem == newItem },
                    areContentsTheSame = { oldItem, newItem -> oldItem == newItem }))
    }

    private fun synchronizeFlow() {
        val id = fairId ?: return
        val updated = flow?.withModel(id, state) ?: return
        flow = updated
        state = updated.modelFor(id)
        setResult(Activity.RESULT_OK, Intent().putExtra(TmhFlowState.EXTRA_FLOW, updated.toBundle()))
    }

    private fun render(shouldAnnounce: Boolean = false) {
        synchronizeFlow()
        val base =
            when (searchState.mode) {
                SearchMode.DETAIL -> state.products
                SearchMode.RESULTS -> searchMatches()
                else -> emptyList()
            }
        val visible =
            base
                .filter { brand == FILTER_ALL || it.name.startsWith(brand) }
                .let {
                    when (sortOrder) {
                        SORT_NAME -> it.sortedBy { p -> p.name }
                        SORT_PRICE_LOW -> it.sortedBy { p -> p.price }
                        SORT_PRICE_HIGH -> it.sortedByDescending { p -> p.price }
                        else -> it
                    }
                }
        val isSubmitted = isInSearch && !isEditingSearch
        searchBack.isEnabled = isInSearch
        binding.title.isVisible = !isInSearch
        binding.share.isVisible = !isInSearch
        binding.queryField.isVisible = isInSearch
        binding.toolbar.elevation = if (isEditingSearch) 2.dp else 0f
        if (isInSearch && binding.queryField.editText.text.toString() != searchState.draft) {
            isUpdatingQuery = true
            binding.queryField.text = searchState.draft
            binding.queryField.editText.setSelection(searchState.draft.length)
            isUpdatingQuery = false
        }
        val layout = binding.products.layoutManager as StaggeredGridLayoutManager
        layout.spanCount = if (isEditingSearch) 1 else effectiveColumns
        binding.products.updateLayoutParams<ConstraintLayout.LayoutParams> {
            topToBottom = if (isSubmitted) R.id.compactProgress else R.id.toolbar
        }
        if (isInSearch) {
            binding.compactProgress.animate().cancel()
            binding.compactProgress.alpha = 1f
            binding.compactProgress.translationY = 0f
            binding.compactProgress.visibility = if (isSubmitted) View.VISIBLE else View.INVISIBLE
            isCompactVisible = isSubmitted
        }
        binding.bottomActions.isVisible = !isEditingSearch
        productAdapter.stateRestorationPolicy =
            if (isInSearch) RecyclerView.Adapter.StateRestorationPolicy.ALLOW
            else RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        headerAdapter.items = if (isInSearch) emptyList() else listOf(HeaderRow(state))
        sectionAdapter.items =
            if (isInSearch && searchState.mode != SearchMode.RESULTS) emptyList()
            else listOf(SectionRow(count = visible.size, columns = columns))
        expectedProductIds = visible.map { it.id }

        productAdapter.items = visible.map {
            ProductRow(
                product = it,
                availability = state.availability,
                columns = effectiveColumns,
                isSearch = isInSearch,
                promotion = promotion,
                isFocused = it.id == focusedProductId)
        }
        val query = PromoSearchState.normalize(searchState.draft)
        scopeAdapter.items =
            if (isEditingSearch && query.isNotEmpty())
                listOf(
                    ScopeRow(
                        id = "promo",
                        query = query,
                        description = getString(R.string.fair_search_promo_scope, promotionTitle)),
                    ScopeRow(id = "global", query = query, description = getString(R.string.fair_search_global_scope)))
            else emptyList()
        emptyAdapter.items =
            when {
                searchState.mode in listOf(SearchMode.EMPTY, SearchMode.ERROR) ->
                    listOf(EmptyRow(mode = searchState.mode, hasWebsiteFallback = searchState.isFallbackAvailable))
                searchState.mode == SearchMode.RESULTS && visible.isEmpty() ->
                    listOf(EmptyRow(mode = SearchMode.EMPTY, hasWebsiteFallback = false, isFiltered = true))
                else -> emptyList()
            }
        loadingAdapter.items = if (searchState.mode == SearchMode.LOADING) listOf("loading") else emptyList()
        if (lastRenderedMode != searchState.mode) {
            binding.products.animate().cancel()
            binding.products.alpha = if (isMotionEnabled) 0f else 1f
            binding.products.animate().alpha(1f).setDuration(if (isMotionEnabled) 160 else 0).start()
            lastRenderedMode = searchState.mode
        }
        bindMeter(binding.compactMeter, state)
        binding.filterSortPill.isVisible =
            state.availability == PromoAvailability.ACTIVE && (!isInSearch || searchState.mode == SearchMode.RESULTS)
        binding.search.isVisible = !isInSearch && state.availability != PromoAvailability.NEW_CUSTOMER_ONLY

        if (state.cartCount > 0) {
            binding.cartCount.text = getString(R.string.fair_cart_count, state.cartCount)
            binding.cartTotal.text = money(state.cartTotal)
            binding.cart.contentDescription =
                getString(R.string.fair_cart_description, state.cartCount, money(state.cartTotal))
        }
        setCartVisible(state.cartCount > 0)

        if (shouldAnnounce) binding.root.announceForAccessibility(message(state).toString())
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
                listOf(
                        R.id.periodValue to R.id.periodLabel,
                        R.id.minimumValue to R.id.minimumLabel,
                        R.id.terms to R.id.termsLabel)
                    .forEach { (value, label) ->
                        connect(value, ConstraintSet.START, label, ConstraintSet.START)
                        connect(value, ConstraintSet.TOP, label, ConstraintSet.BOTTOM, 4.dp.toInt())
                    }
                applyTo(view.root)
            }
            view.periodValue.gravity = android.view.Gravity.START
            view.minimumValue.gravity = android.view.Gravity.START
            view.terms.gravity = android.view.Gravity.START
        }
        val isActive = model.availability == PromoAvailability.ACTIVE && integratedFair?.state?.isUnavailable != true
        val monochrome = if (isActive) null else ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
        listOf(view.banner, view.giftOne, view.giftTwo, view.giftThree).forEach { it.colorFilter = monochrome }
        view.newUser.isVisible = model.availability == PromoAvailability.NEW_CUSTOMER_ONLY
        view.fairBadge.apply {
            text = model.fairName
            textColor = color(if (isActive) UIKitR.color.primary_30 else UIKitR.color.black_50)
            badgeColor = color(if (isActive) UIKitR.color.support_selected else UIKitR.color.black_30)
            badgeTextAppearance = R.style.FairDemo_CaptionStrong
            setBadgePadding(horizontal = 8, vertical = 4)
            setCornerRadius(4f)
        }
        view.quotaBadge.apply {
            klikVariant = KlikVariant.Quota.DEFAULT
            text =
                if (model.availability == PromoAvailability.QUOTA_EXHAUSTED) getString(R.string.fair_quota_empty)
                else getString(R.string.fair_quota_full)
            badgeColor = color(if (isActive) R.color.fair_quota_background else UIKitR.color.black_30)
            textColor = color(if (isActive) R.color.fair_quota_foreground else UIKitR.color.black_50)
            badgeTextAppearance = R.style.FairDemo_CaptionStrong
        }
        view.promoTitle.text =
            if (promotion == DemoPromotion.REDEMPTION) promotionTitle
            else
                getString(
                    if (model.rule == RewardRule.ONE_TIME) R.string.fair_search_gift_title
                    else R.string.fair_repeatable_title)

        integratedFair?.let { fair ->
            view.promoTitle.maxLines = Int.MAX_VALUE
            view.promoTitle.ellipsize = null
            view.banner.setImageResource(fair.bannerRes)
            view.banner.contentDescription = getString(fair.nameRes)
            val previews = listOf(view.giftOne, view.giftTwo, view.giftThree)
            previews.forEachIndexed { index, image ->
                val product = fair.products.getOrNull(index)
                image.isVisible = product != null
                image.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                product?.let { image.setImageResource(it.imageRes) }
            }
            view.periodValue.setText(R.string.tmh_promo_period)
        }
        view.banner.isVisible = true
        view.gifts.isVisible = true
        view.minimumValue.text = money(model.threshold)
        view.terms.paintFlags = view.terms.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        view.terms.setOnClickListener { showTerms() }
        view.info.makeStatic()

        view.giftsAction.setOnClickListener { showGifts() }
        bindMeter(view.meter, model)
        view.meter.message.importantForAccessibility =
            if (isCompactVisible) View.IMPORTANT_FOR_ACCESSIBILITY_NO else View.IMPORTANT_FOR_ACCESSIBILITY_YES
        ViewCompat.setAccessibilityHeading(view.promoTitle, true)
        view.root.doOnLayout { updateCompactHeader() }
    }

    private fun bindMeter(view: ViewFairProgressBinding, model: PromoDemoState) {
        view.seeGifts.contentDescription =
            if (promotion == DemoPromotion.FREE_GIFT) {
                getString(R.string.fair_view_gifts)
            } else {
                getString(R.string.fair_view_redemption)
            }

        val hasLaps = model.rule == RewardRule.REPEATABLE && model.earnedGifts > 0

        view.progressBar.apply {
            showBadge = true

            val limit = model.barLimit

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

            if (isMotionEnabled) {
                view.message.alpha = 0f
                view.message.translationY = 4.dp

                view.message.animate().alpha(1f).translationY(0f).setDuration(160).setInterpolator(easing).start()
            } else {
                view.message.alpha = 1f
                view.message.translationY = 0f
            }
        } else {
            view.message.text = next
        }

        ViewCompat.setStateDescription(
            view.message,
            getString(
                R.string.fair_progress_description,
                money(model.qualifyingSubtotal),
                money(model.threshold),
                model.claimedGifts.size))
    }

    private fun bindProduct(view: ItemFairProductBinding, row: ProductRow) {
        val product = row.product
        val isActive = row.availability == PromoAvailability.ACTIVE && integratedFair?.state?.isUnavailable != true
        view.image.setImageResource(product.image)
        view.card.strokeWidth = if (row.isSearch && row.promotion == DemoPromotion.REDEMPTION) 1.dp.toInt() else 0
        view.card.setStrokeColor(color(UIKitR.color.black_30))
        view.card.alpha = if (isActive) 1f else 0.35f
        view.name.text = product.name
        view.name.maxLines = if (resources.configuration.fontScale > 1.4f) Int.MAX_VALUE else 2
        view.price.text = money(product.price)
        view.discount.isVisible = product.isDiscounted
        view.originalPrice.isVisible = product.isDiscounted
        view.originalPrice.text = money(product.originalPrice)
        view.originalPrice.paintFlags = view.originalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        view.promotion.text = product.promotion
        val isSavingsPromotion = product.promotion == "Banyak Lebih Hemat"
        view.promotion.setTextColor(
            color(if (isSavingsPromotion) UIKitR.color.klik_color_red_30 else UIKitR.color.warning_primary))
        view.promotion.setBackgroundResource(
            if (isSavingsPromotion) R.drawable.fair_discount_background else R.drawable.fair_extra_background)
        view.discount.setTextAppearance(if (row.isSearch) R.style.FairSearch_SmallStrong else R.style.FairDemo_Small)
        view.promotion.setTextAppearance(if (row.isSearch) R.style.FairSearch_SmallStrong else R.style.FairDemo_Small)
        view.discount.setTextColor(color(UIKitR.color.klik_color_red_30))
        view.promotion.setTextColor(
            color(if (isSavingsPromotion) UIKitR.color.klik_color_red_30 else UIKitR.color.warning_primary))
        view.benefitStrip.isVisible = product.benefit.isNotEmpty() && (!row.isSearch || row.promotion == DemoPromotion.REDEMPTION)
        view.contentBottomSpace.isVisible = !view.benefitStrip.isVisible
        view.benefit.text = product.benefit
        view.promotion.isVisible = product.promotion.isNotEmpty()
        view.discount.text = getString(R.string.fair_discount_label)
        val highlight = product.highlight
        view.benefitIcon.setImageResource(
            if (product.benefit.contains("Stamp")) R.drawable.fair_stamp else R.drawable.fair_point)
        view.highlight.apply {
            klikVariant = KlikVariant.Highlight.DEFAULT
            text = highlight
            isVisible = highlight.isNotEmpty()
            setIcon(
                when (highlight) {
                    "Paket Bundling" -> R.drawable.fair_search_bundling
                    "Banyak Lebih Hemat" -> R.drawable.fair_search_percent
                    else -> R.drawable.fair_gift_icon
                })
            if (row.promotion == DemoPromotion.REDEMPTION && highlight.isNotEmpty()) {
                setGradientBackground(
                    intArrayOf(
                        color(
                            if (highlight == "Paket Bundling") R.color.fair_bundling_start
                            else R.color.fair_savings_start),
                        color(
                            if (highlight == "Paket Bundling") R.color.fair_bundling_end
                            else R.color.fair_savings_end)))
            }
            iconVisible = true
        }

        view.root.makeStatic()
        view.card.makeStatic()
        view.card.contentDescription =
            listOf(
                    product.name,
                    money(product.price),
                    highlight,
                    product.promotion,
                    if (view.benefitStrip.isVisible) product.benefit else "")
                .filter { it.isNotEmpty() }
                .joinToString(", ")
        view.root.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        ViewCompat.setStateDescription(
            view.card,
            if (isActive) getString(R.string.fair_quantity_description, product.quantity)
            else getString(R.string.fair_unavailable_description))
        ViewCompat.setScreenReaderFocusable(view.card, true)
        view.quantity.apply {
            clearListener()
            setAllowTextEditing(false)
            setMinValue(0)

            setMaxValue(Int.MAX_VALUE)
            setCount(product.quantity, animate = false)
            setMaxValue(product.stock)
            isEnabled = isActive
            setPlusButtonEnabled(isActive && product.quantity < product.stock)
            setMinusButtonEnabled(isActive && product.quantity > 0)
            val blue = colorStateList(UIKitR.color.primary_30)
            val white = colorStateList(UIKitR.color.white)
            binding.singlePlusButton.backgroundTintList = blue
            binding.singlePlusButton.strokeWidth = 0
            binding.singlePlusButton.iconTint = white
            binding.stepperContainer.setCardBackgroundColor(blue)
            binding.stepperContainer.strokeWidth = 0
            binding.countText.setTextColor(color(UIKitR.color.white))
            if (resources.configuration.fontScale > 1.4f)
                binding.countText.setTextSize(android.util.TypedValue.COMPLEX_UNIT_DIP, 14f)
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
        val face =
            when {
                product.quantity == 0 -> StepperFace.ADD
                row.isFocused -> StepperFace.FOCUSED
                else -> StepperFace.COUNT
            }

        applyStepperFace(view, product.id, face, product.quantity)
        view.quantity.binding.singlePlusButton.apply {
            if (face == StepperFace.COUNT) {
                isEnabled = isActive
                contentDescription = getString(R.string.fair_quantity_edit_description, product.name, product.quantity)
            }
        }
    }

    private fun applyFocus(id: String?) {
        binding.products.removeCallbacks(clearFocusTask)
        if (id != null) binding.products.postDelayed(clearFocusTask, 5000L)
        if (focusedProductId == id) return
        focusedProductId = id
        productAdapter.items = productAdapter.items.map { it.copy(isFocused = it.id == id) }
    }

    private fun step(id: String, delta: Int) {
        applyFocus(id)
        state.products.firstOrNull { it.id == id }?.let { changeQuantity(id, it.quantity + delta) }
    }

    private fun changeQuantity(id: String, quantity: Int) {
        val old = state
        state = state.withQuantity(id, quantity)
        if (state == old) return
        render(shouldAnnounce = old.earnedGifts != state.earnedGifts || old.claimedGifts != state.claimedGifts)
        if (flow != null && state.claimedGifts.size < old.claimedGifts.size) {
            Snackbar.make(binding.root, R.string.tmh_eligibility_reduced, Snackbar.LENGTH_LONG).show()
        }

        animateProgress(state.barProgress)
        if (isMotionEnabled && state.cartCount > 0) {
            binding.cartIcon.animate().cancel()
            binding.cartIcon.scaleX = 1f
            binding.cartIcon.scaleY = 1f
        }
    }

    private fun animateProgress(target: Float) {
        if (target == displayedProgress) return
        val isIncreasing = target > displayedProgress
        displayedProgress = target
        android.util.Log.d("FairBar", "prevTarget=$displayedProgress target=$target")
        listOfNotNull(header?.meter?.progressBar, binding.compactMeter.progressBar).forEach { bar ->
            bar.shouldAnimate = isMotionEnabled && isIncreasing
            android.util.Log.d("FairBar", "getter=${bar.indicatorProgress} animate=${bar.shouldAnimate}")
            bar.indicatorProgress = target
            shown[bar] = target
        }
    }

    private fun updateCompactHeader() {
        val grid = binding.products.layoutManager as StaggeredGridLayoutManager
        val view = header
        val first = grid.findFirstVisibleItemPositions(null).minOrNull() ?: RecyclerView.NO_POSITION
        val threshold = if (isCompactVisible) 4.dp.toInt() else -4.dp.toInt()
        val isVisible =
            state.products.isNotEmpty() &&
                (first > 0 || (first == 0 && view != null && view.root.top + view.gifts.bottom < threshold))
        if (isInSearch) return
        if (isVisible == isCompactVisible) return
        isCompactVisible = isVisible
        binding.compactProgress.animate().cancel()
        if (isVisible) {
            binding.compactProgress.isVisible = true
            binding.compactProgress.alpha = if (isMotionEnabled) 0f else 1f
            binding.compactProgress.translationY = if (isMotionEnabled) -4.dp else 0f
            binding.compactProgress
                .animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(if (isMotionEnabled) 160 else 0)
                .start()
        } else {
            binding.compactProgress
                .animate()
                .alpha(0f)
                .setDuration(if (isMotionEnabled) 120 else 0)
                .withEndAction {
                    if (!isCompactVisible) binding.compactProgress.visibility = View.INVISIBLE
                }
                .start()
        }
        view?.meter?.message?.importantForAccessibility =
            if (isVisible) View.IMPORTANT_FOR_ACCESSIBILITY_NO else View.IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    private fun changeColumns(value: Int) {
        val anchor = binding.products.layoutManager?.onSaveInstanceState()
        columns = value
        (binding.products.layoutManager as StaggeredGridLayoutManager).spanCount = effectiveColumns
        render()
        restoreScroll(anchor)
    }

    private fun message(model: PromoDemoState): CharSequence {
        integratedFair?.let { fair ->
            val html = when {
                fair.state.isUnavailable -> return fair.alertText(this)
                model.earnedGifts == 0 -> getString(R.string.tmh_remaining_spend, money(model.remainingSpend))
                model.claimedGifts.isNotEmpty() && model.unclaimedGifts > 0 ->
                    getString(R.string.tmh_ppp_selected_partial, model.claimedGifts.size, model.earnedGifts)
                model.claimedGifts.isNotEmpty() -> getString(R.string.tmh_ppp_selected_full, model.claimedGifts.size)
                else -> getString(R.string.tmh_remaining_allowance, model.earnedGifts)
            }
            return buildHighlightedMessage(
                context = this,
                htmlMessage = html,
                highlightedTextAppearance = TextStyleKey.B3_SEMIBOLD.get(this, UIKitR.color.primary_30),
            )
        }
        val text =
            when (model.availability) {
                PromoAvailability.QUOTA_EXHAUSTED -> getString(R.string.fair_message_quota_exhausted)
                PromoAvailability.LIMIT_REACHED -> getString(R.string.fair_message_limit_reached)
                PromoAvailability.NEW_CUSTOMER_ONLY -> getString(R.string.fair_message_first_transaction)
                PromoAvailability.ACTIVE ->
                    when {
                        promotion == DemoPromotion.REDEMPTION &&
                            model.unclaimedGifts > 0 &&
                            model.claimedGifts.isNotEmpty() ->
                            getString(
                                R.string.fair_message_redemption_partial, model.claimedGifts.size, model.earnedGifts)
                        promotion == DemoPromotion.REDEMPTION && model.unclaimedGifts > 0 ->
                            getString(R.string.fair_message_redemption_available, model.earnedGifts)
                        promotion == DemoPromotion.REDEMPTION && model.claimedGifts.isNotEmpty() ->
                            getString(R.string.fair_message_redemption_claimed, model.claimedGifts.size)
                        model.qualifyingSubtotal == 0L -> getString(R.string.fair_message_start)
                        model.earnedGifts == 0 ->
                            getString(R.string.fair_message_remaining_spend, money(model.remainingSpend))
                        model.unclaimedGifts > 0 && model.claimedGifts.isNotEmpty() ->
                            getString(R.string.fair_message_partial_claim, model.claimedGifts.size, model.earnedGifts)
                        model.unclaimedGifts > 0 -> getString(R.string.fair_message_gifts_available, model.earnedGifts)
                        model.rule == RewardRule.REPEATABLE && model.claimedGifts.size == 1 ->
                            getString(R.string.fair_message_repeat_claim)
                        else -> getString(R.string.fair_message_claimed, model.claimedGifts.size)
                    }
            }

        return buildHighlightedMessage(
            message = text,
            highlightedMessages =
                Regex("Rp[\\d.]+|\\d+(?: dari \\d+)? (?:hadiah|tebus murah)").findAll(text).map { it.value }.toList(),
            highlightColor = color(UIKitR.color.primary_30))
    }

    private fun applyStepperFace(v: ItemFairProductBinding, productId: String, face: StepperFace, count: Int) {
        val stepper = v.quantity
        val b = stepper.binding
        val collapsedW = requireNotNull(dimenPixelSize(UIKitR.dimen.l))
        val expandedW = requireNotNull(dimenPixelSize(UIKitR.dimen.dimen_90))
        val blue = color(UIKitR.color.primary_30)
        val white = color(UIKitR.color.white)

        val prev = stepper.tag as? FaceTag
        prev?.isCancelled = true
        prev?.animator?.cancel()
        val isSameCard = prev != null && prev.productId == productId
        val from = if (isSameCard) prev!!.face else face
        val tag = FaceTag(productId = productId, face = face)
        stepper.tag = tag

        fun setWidth(w: Int) {
            val lp = b.stepperContainer.layoutParams
            if (lp.width != w) {
                lp.width = w
                b.stepperContainer.layoutParams = lp
            }
        }
        fun setControls(isVisible: Boolean, alpha: Float = 1f) {
            listOf(b.minusButton, b.plusButton, b.countText).forEach {
                it.isVisible = isVisible
                it.alpha = alpha
            }
            b.countEditText.isVisible = false
        }

        fun styleCircle(isCount: Boolean) {
            b.singlePlusButton.apply {
                if (isCount) {
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
        fun styleContainer(isCount: Boolean) {
            b.stepperContainer.setCardBackgroundColor(if (isCount) white else blue)
            b.stepperContainer.strokeWidth = if (isCount) 1.dp.toInt() else 0
            if (isCount) b.stepperContainer.setStrokeColor(color(UIKitR.color.black_30))
        }

        fun applyFinal() {
            stepper.isVisible = true
            when (face) {
                StepperFace.ADD -> {
                    setWidth(collapsedW)
                    styleContainer(false)
                    styleCircle(false)
                    b.singlePlusButton.isVisible = true
                    b.singlePlusButton.alpha = 1f
                    setControls(false)
                }
                StepperFace.FOCUSED -> {
                    setWidth(expandedW)
                    styleContainer(false)
                    styleCircle(false)
                    b.singlePlusButton.isVisible = false
                    setControls(true)
                }
                StepperFace.COUNT -> {
                    setWidth(collapsedW)
                    styleContainer(true)
                    styleCircle(true)
                    b.singlePlusButton.isVisible = true
                    b.singlePlusButton.alpha = 1f
                    setControls(false)
                }
            }
        }

        val isExpanding = face == StepperFace.FOCUSED
        val isCollapsing = from == StepperFace.FOCUSED && !isExpanding
        if (!isMotionEnabled || !isSameCard || from == face || !(isExpanding || isCollapsing)) {
            applyFinal()
            return
        }

        fun fade(from: Float, to: Float, ms: Long, delay: Long = 0L, apply: (Float) -> Unit) =
            ValueAnimator.ofFloat(from, to).apply {
                duration = ms
                startDelay = delay
                addUpdateListener { apply(it.animatedValue as Float) }
            }
        fun width(from: Int, to: Int) =
            ValueAnimator.ofInt(from, to).apply {
                duration = 300L
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { setWidth(it.animatedValue as Int) }
            }
        fun containerColor(from: Int, to: Int, ms: Long, delay: Long = 0L) =
            ValueAnimator.ofArgb(from, to).apply {
                duration = ms
                startDelay = delay
                addUpdateListener { b.stepperContainer.setCardBackgroundColor(it.animatedValue as Int) }
            }
        val controls = listOf(b.minusButton, b.plusButton, b.countText)

        val parts = mutableListOf<Animator>()
        stepper.isVisible = true
        if (isExpanding) {

            setWidth(collapsedW)
            setControls(true, 0f)
            b.singlePlusButton.isVisible = true
            b.singlePlusButton.alpha = 1f
            b.stepperContainer.strokeWidth = 0
            parts += width(collapsedW, expandedW)
            parts += fade(0f, 1f, 150L, 150L) { a -> controls.forEach { it.alpha = a } }
            parts += fade(1f, 0f, 150L) { b.singlePlusButton.alpha = it }
            if (from == StepperFace.COUNT) parts += containerColor(white, blue, 150L)
        } else {
            setWidth(expandedW)
            setControls(true, 1f)
            styleCircle(face == StepperFace.COUNT)
            b.singlePlusButton.isVisible = true
            b.singlePlusButton.alpha = 0f
            parts += width(expandedW, collapsedW)
            parts += fade(1f, 0f, 150L, delay = 300L) { a -> controls.forEach { it.alpha = a } }
            parts += fade(0f, 1f, 150L, delay = 300L) { b.singlePlusButton.alpha = it }
            if (face == StepperFace.COUNT) parts += containerColor(blue, white, 150L, delay = 300L)
        }

        tag.animator =
            AnimatorSet().apply {
                playTogether(parts)
                addListener(
                    object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            if (!tag.isCancelled) applyFinal()
                        }
                    })
                start()
            }
    }

    private fun showTerms() {
        if (flow != null) {
            if (supportFragmentManager.findFragmentByTag(TmhTermsBottomSheet.TAG) == null) {
                TmhTermsBottomSheet.newInstance(
                    fairName = state.fairName,
                    isIntegrated = true,
                    offerDescriptions = state.giftOffers.map { "${it.name} (${money(it.price)})" },
                    minimumSpend = state.threshold,
                )
                    .show(supportFragmentManager, TmhTermsBottomSheet.TAG)
            }
            return
        }
        val isRepeatable = state.rule == RewardRule.REPEATABLE
        showOptions(
            title = getString(R.string.fair_terms_title),
            description =
                getString(
                    R.string.fair_terms_description,
                    money(state.threshold),
                    getString(if (isRepeatable) R.string.fair_rule_repeatable else R.string.fair_rule_one_time)),
            options = emptyList()) {}
    }

    private fun showSkeleton(durationMs: Long = 1500L, onFinished: () -> Unit = {}) {
        val root = binding.skeleton.root
        skeletonFinish?.let { root.removeCallbacks(it) } // a newer request replaces a pending one
        root.animate().cancel()
        root.alpha = 1f
        root.isVisible = true
        if (isMotionEnabled) binding.skeleton.shimmer.startShimmer()

        val finish = Runnable {
            skeletonFinish = null
            root
                .animate()
                .alpha(0f)
                .setDuration(150L)
                .withEndAction {
                    binding.skeleton.shimmer.stopShimmer()
                    root.isVisible = false
                    root.alpha = 1f
                    if (!isFinishing && !isDestroyed) onFinished()
                }
                .start()
        }
        skeletonFinish = finish
        root.postDelayed(finish, durationMs)
    }

    private fun showGifts() {

        showRedemptionTray()
    }

    private fun showRedemptionTray() {
        val currentFlow = flow
        val fair = integratedFair
        if (currentFlow != null && fair != null) {
            if (supportFragmentManager.findFragmentByTag(TmhRedemptionBottomSheet.TAG) == null) {
                TmhRedemptionBottomSheet.newInstance(fair, currentFlow.scenario, isPromoDetail = true)
                    .show(supportFragmentManager, TmhRedemptionBottomSheet.TAG)
            }
            return
        }
        val limit = state.earnedGifts
        val isLocked = limit == 0
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

        val adapter =
            BaseAdapter.adapterOf<RedeemRow, ItemFairRedeemProductBinding>(
                register =
                    BaseAdapter.Register(
                        onBindHolder = { _, row, v, _ ->
                            val item = row.item
                            val isSelected = item.quantity > 0 && !item.isRestocking
                            val isGreyedOut = row.isLocked || item.isRestocking
                            v.root.post {
                                android.util.Log.d(
                                    "RedeemCard",
                                    "late state: root clickable=${v.root.isClickable} listener=${v.root.hasOnClickListeners()} info clickable=${v.info.isClickable}")
                                v.root.makeStatic()
                                v.info.makeStatic()
                            }

                            applySelection(v, item.id, isSelected)
                            v.total.isVisible = isSelected
                            if (isSelected) {
                                val pcs = getString(R.string.fair_gift_pieces, item.quantity * item.packSize)
                                v.total.text =
                                    buildHighlightedMessage(
                                        message = getString(R.string.fair_gift_total, pcs),
                                        highlightedMessages = listOf(pcs),
                                        highlightColor = color(UIKitR.color.black_70))
                            }
                            v.image.setImageResource(item.image)
                            v.name.text = item.name
                            v.pack.text = getString(R.string.fair_gift_pack, item.packSize)
                            v.discount.text =
                                getString(R.string.fair_gift_discount, 100 - item.price * 100 / item.originalPrice)
                            v.originalPrice.text = money(item.originalPrice)
                            v.originalPrice.paintFlags = v.originalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                            v.price.text =
                                if (item.price == 0L) getString(R.string.fair_gift_free) else money(item.price)
                            v.restockLabel.isVisible = item.isRestocking
                            v.stepper.isVisible = !item.isRestocking

                            v.root.alpha = 1f
                            v.info.animationDuration = 0L
                            v.info.setDisabledWithAnimation(isGreyedOut, animate = true)
                            v.footer.setBackgroundColor(
                                color(if (isGreyedOut) UIKitR.color.black_20 else UIKitR.color.info_background))
                            v.info.alpha = if (isGreyedOut) 0.5f else 1f
                            v.stepper.apply {
                                clearListener()
                                setAllowTextEditing(false)
                                showExpanded()

                                setMinValue(0)
                                setMaxValue(Int.MAX_VALUE)
                                setCount(item.quantity, animate = false)
                                setMaxValue(item.quantity + row.remaining)
                                isEnabled = !row.isLocked
                                setPlusButtonEnabled(row.shouldAllowAdd && !item.isRestocking)
                                setMinusButtonEnabled(item.quantity > 0)

                                applyRedeemStyle(
                                    shouldDimCount = row.isLocked || (item.quantity == 0 && !row.shouldAllowAdd))
                                binding.singlePlusButton.contentDescription =
                                    getString(R.string.fair_gift_add, item.name)
                                binding.plusButton.contentDescription = getString(R.string.fair_gift_add, item.name)
                                binding.minusButton.contentDescription = getString(R.string.fair_gift_remove, item.name)
                                setOnCountChangeListener { newCount -> setQuantity(item.id, newCount) }
                            }
                            v.root.contentDescription =
                                getString(
                                    R.string.fair_gift_description,
                                    item.name,
                                    item.packSize,
                                    item.quantity,
                                    if (isSelected) getString(R.string.fair_selected_suffix) else "")
                        }),
                diff =
                    BaseAdapter.Diff(
                        areItemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
                        areContentsTheSame = { oldItem, newItem -> oldItem == newItem }))

        fun quantities(list: List<RedeemItem>) = list.associate { it.id to it.quantity }
        fun hasChanges() = quantities(draft) != quantities(committed)

        fun isRemoval(): Boolean {
            val before = quantities(committed)
            val isAdded = draft.any { it.quantity > (before[it.id] ?: 0) }
            val isRemoved = draft.any { it.quantity == 0 && (before[it.id] ?: 0) > 0 }
            return hasChanges() && isRemoved && !isAdded
        }

        refresh = {
            val used = draft.sumOf { it.quantity }
            val isCartEmpty = committed.sumOf { it.quantity } == 0
            val shouldRemove = isRemoval()
            val isEnabled = hasChanges()

            android.util.Log.d(
                "FairDemo",
                "draft=${quantities(draft)} committed=${quantities(committed)} changed=$isEnabled removal=$shouldRemove cartEmpty=$isCartEmpty")

            sheet.counter.text = getString(R.string.fair_redemption_counter, used, limit)
            sheet.counter.setTextColor(color(if (isLocked) UIKitR.color.black_40 else UIKitR.color.primary_30))
            sheet.counter.backgroundTintList = if (isLocked) colorStateList(UIKitR.color.black_20) else null
            sheet.update.text =
                when {
                    shouldRemove -> getString(R.string.fair_cart_delete)
                    isCartEmpty -> getString(R.string.fair_cart_title)
                    else -> getString(R.string.fair_cart_update)
                }
            if (isCartEmpty) sheet.update.setIconResource(UIKitR.drawable.ic_plus_stepper) else sheet.update.icon = null
            sheet.update.backgroundTintList = if (shouldRemove) destructiveTint else defaultTint
            sheet.update.isEnabled = isEnabled
            sheet.update.refreshDrawableState()
            adapter.items = draft.map {
                RedeemRow(
                    item = it,
                    shouldAllowAdd = used < limit,
                    isLocked = isLocked,
                    remaining = (limit - used).coerceAtLeast(0))
            }
            sheetBehavior?.isHideable = !isEnabled
        }

        fun commit() {
            val shouldRemove = isRemoval()
            val removedQty = committed.sumOf { it.quantity } - draft.sumOf { it.quantity }

            state = state.withClaimedGifts(draft.flatMap { d -> List(d.quantity) { d.id } })
            committed = draft
            render(shouldAnnounce = true)

            tray?.dismiss()
            showSkeleton {
                Snackbar.make(
                        binding.main,
                        if (shouldRemove) getString(R.string.fair_cart_removed, removedQty)
                        else getString(R.string.fair_cart_updated),
                        Snackbar.LENGTH_SHORT)
                    .apply {
                        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                            leftMargin = 16.dp.toInt()
                            rightMargin = 16.dp.toInt()
                            bottomMargin = 8.dp.toInt()
                        }
                    }
                    .show()
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

        tray =
            BottomSheetTray.newInstance(title = state.fairName, contentLayout = sheet.root).apply {
                dragHandleVisibility = true
                titleDividerVisibility = true
                shouldShowClose = true
                customAnimationsEnabled = isMotionEnabled
            }
        tray.show(supportFragmentManager, "fair-redemption")
        supportFragmentManager.executePendingTransactions()

        (tray.dialog as? BottomSheetDialog)?.let { dialog ->
            dialog.onBackPressedDispatcher.addCallback(
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() = requestClose()
                })

            dialog.findViewById<View>(com.google.android.material.R.id.touch_outside)?.setOnClickListener {
                requestClose()
            }

            sheetBehavior = dialog.behavior
            dialog.behavior.addBottomSheetCallback(
                object : BottomSheetBehavior.BottomSheetCallback() {
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

    private fun setCartVisible(shouldShow: Boolean) {
        if (shouldShow == isCartShown) return
        isCartShown = shouldShow
        val cart = binding.cart
        cart.animate().cancel()
        cartMove?.cancel()
        if (!isMotionEnabled) {
            cart.alpha = 1f
            cart.translationY = 0f
            cart.isVisible = shouldShow
            return
        }
        if (shouldShow) {
            if (!cart.isVisible) {
                cart.isVisible = true
                cart.alpha = 0f
                cart.translationY = 16.dp
            }
            cart.animate().alpha(1f).setDuration(50).setInterpolator(AccelerateDecelerateInterpolator()).start()
            cartMove =
                ObjectAnimator.ofFloat(cart, View.TRANSLATION_Y, cart.translationY, 0f).apply {
                    duration = 320L
                    interpolator = OvershootInterpolator(1f)
                    start()
                }
        } else {
            cart
                .animate()
                .alpha(0f)
                .translationY(16.dp)
                .setDuration(200)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction {
                    if (!isCartShown) {
                        cart.isVisible = false
                        cart.alpha = 1f
                        cart.translationY = 0f
                    }
                }
                .start()
        }
    }

    private fun showSearch() {
        if (!isInSearch) detailScroll = binding.products.layoutManager?.onSaveInstanceState()
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
            editText.setOnClickListener { if (!isEditingSearch) beginEditing() }
            editText.setOnEditorActionListener { _, action, event ->
                if (action == EditorInfo.IME_ACTION_SEARCH ||
                    (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP)) {
                    submitSearch()
                    true
                } else false
            }
            val inner = SearchBarBinding.bind(getChildAt(0))
            inner.ivStartIcon.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            inner.ivEndIcon.contentDescription = getString(R.string.fair_search_clear)
            buttonSemantics(inner.ivEndIcon)
            searchBarDelegate =
                object : SearchBarDelegate {
                    override fun onCloseIconClicked(view: View) {
                        isUpdatingQuery = true
                        text = ""
                        isUpdatingQuery = false
                        beginEditing("")
                    }

                    override fun onFocusChange(view: View, hasFocus: Boolean) {
                        if (hasFocus && isInSearch && !isEditingSearch && !isUpdatingQuery) beginEditing()
                    }

                    override fun onTextChange(text: String) {
                        inner.ivEndIcon.isVisible = text.isNotEmpty()
                        if (isUpdatingQuery || !isInSearch) return
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
            if (isEditingSearch && !isFinishing) {
                binding.queryField.editText.requestFocus()
                binding.queryField.editText.setSelection(binding.queryField.editText.text.length)
                ViewCompat.getWindowInsetsController(binding.queryField)?.show(WindowInsetsCompat.Type.ime())
            }
        }
    }

    private fun submitSearch() {
        if (PromoSearchState.normalize(searchState.draft).isEmpty()) {
            focusSearch()
            return
        }
        cancelSearchRequest()
        searchState = searchState.submit()
        isUpdatingQuery = true
        binding.queryField.editText.clearFocus()
        isUpdatingQuery = false
        ViewCompat.getWindowInsetsController(binding.queryField)?.hide(WindowInsetsCompat.Type.ime())
        render()
        binding.products.scrollToPosition(0)
        val request = searchState.generation
        val shouldSimulateFailure = shouldFailNextSearch
        shouldFailNextSearch = false

        pendingSearch =
            Runnable {
                if (isDestroyed) return@Runnable
                val matches = searchMatches()
                searchState =
                    if (shouldSimulateFailure) searchState.fail(request)
                    else
                        searchState.complete(
                            request, matches.size, PromoSearchState.websiteFallback(searchState.submitted))
                if (searchState.generation != request || isEditingSearch || !isInSearch) return@Runnable
                pendingSearch = null
                render()
                scrollToNewSearchResults()
                binding.products.announceForAccessibility(
                    if (searchState.mode == SearchMode.ERROR) getString(R.string.fair_search_error_title)
                    else getString(R.string.fair_search_results, matches.size, searchState.submitted))
            }
                .also { searchHandler.postDelayed(it, 220) }
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
            if (request != searchState.generation || !isInSearch || isEditingSearch) return
            if (scopeAdapter.itemCount != 0 ||
                headerAdapter.itemCount != 0 ||
                loadingAdapter.itemCount != 0 ||
                sectionAdapter.itemCount != expectedSection ||
                emptyAdapter.itemCount != expectedEmpty ||
                productAdapter.items.map { it.id } != expectedProductIds)
                return
            searchScrollObserver?.let { adapter.unregisterAdapterDataObserver(it) }
            searchScrollObserver = null
            binding.products.post {
                if (request == searchState.generation && isInSearch && !isEditingSearch)
                    (binding.products.layoutManager as StaggeredGridLayoutManager).scrollToPositionWithOffset(0, 0)
            }
        }
        searchScrollObserver =
            object : RecyclerView.AdapterDataObserver() {
                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = checkCommitted()

                    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = checkCommitted()

                    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) = checkCommitted()

                    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) = checkCommitted()

                    override fun onChanged() = checkCommitted()
                }
                .also { adapter.registerAdapterDataObserver(it) }
        checkCommitted()
    }

    private fun closeSearch() {
        cancelSearchRequest()
        ViewCompat.getWindowInsetsController(binding.queryField)?.hide(WindowInsetsCompat.Type.ime())
        binding.queryField.editText.clearFocus()
        searchState = PromoSearchState()
        resultScroll = null
        isCompactVisible = false
        binding.compactProgress.visibility = View.INVISIBLE
        render()
        restoreScroll(detailScroll)
        binding.search.requestFocus()
    }

    private fun restoreScroll(scroll: Parcelable?) {
        if (scroll == null) return

        val adapter = binding.products.adapter ?: return
        var isRegistered = true
        val observer =
            object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    if (isRegistered) adapter.unregisterAdapterDataObserver(this)
                    isRegistered = false
                    binding.products.post { binding.products.layoutManager?.onRestoreInstanceState(scroll) }
                }
            }
        adapter.registerAdapterDataObserver(observer)
        binding.products.postDelayed(
            {
                if (isRegistered) adapter.unregisterAdapterDataObserver(observer)
                isRegistered = false
                binding.products.layoutManager?.onRestoreInstanceState(scroll)
            },
            180)
    }

    private fun bindSearchEmpty(view: ItemFairSearchEmptyBinding, row: EmptyRow) {
        emptyConstraints.applyTo(view.messagePanel)
        view.action.setTextAppearance(R.style.FairDemo_CaptionStrong)
        view.action.setTextColor(color(UIKitR.color.white))
        val isError = row.mode == SearchMode.ERROR
        view.heading.setText(
            when {
                isError -> R.string.fair_search_error_title
                row.isFiltered -> R.string.fair_search_filtered_title
                else -> R.string.fair_search_empty_title
            })
        view.description.setText(
            when {
                isError -> R.string.fair_search_error_description
                row.isFiltered -> R.string.fair_search_filtered_description
                else -> R.string.fair_search_empty_description
            })
        view.action.setText(
            when {
                isError -> R.string.fair_search_retry
                row.isFiltered -> R.string.fair_search_clear_filter
                else -> R.string.fair_search_edit
            })
        view.illustration.isVisible = !isError
        view.websiteBanner.isVisible = row.hasWebsiteFallback && !isError
        view.divider.isVisible = row.hasWebsiteFallback && !isError
        ViewCompat.setAccessibilityHeading(view.heading, true)
        view.root.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        buttonSemantics(view.action)
        buttonSemantics(view.websiteBanner)
        if (resources.configuration.fontScale > 1.4f || isError) {
            ConstraintSet().apply {
                clone(view.messagePanel)
                clear(R.id.heading, ConstraintSet.BOTTOM)
                connect(R.id.heading, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                connect(
                    R.id.heading,
                    ConstraintSet.TOP,
                    if (isError) ConstraintSet.PARENT_ID else R.id.illustration,
                    if (isError) ConstraintSet.TOP else ConstraintSet.BOTTOM,
                    if (isError) 0 else 12.dp.toInt())
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
                isError -> submitSearch()
                row.isFiltered -> {
                    brand = FILTER_ALL
                    render()
                }
                else -> beginEditing(searchState.submitted)
            }
        }
        view.websiteBanner.setOnClickListener { openWebsite() }
    }

    private fun buttonSemantics(view: View) {
        ViewCompat.setAccessibilityDelegate(
            view,
            object : AccessibilityDelegateCompat() {
                override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.className = "android.widget.Button"
                }
            })
    }

    private fun applySelection(v: ItemFairRedeemProductBinding, itemId: String, isSelected: Boolean) {
        val card = v.root
        val mark = v.selectedMark
        val previous = card.tag as? SelectionTag

        val isSameItem = previous != null && previous.itemId == itemId
        val shouldAnimate = isMotionEnabled && isSameItem && previous!!.isSelected != isSelected

        previous?.strokeAnimator?.cancel()
        mark.animate().cancel()

        val tag = if (isSameItem) previous!! else SelectionTag(itemId = itemId, isSelected = isSelected)
        tag.isSelected = isSelected
        tag.strokeAnimator = null
        card.tag = tag

        val newBorder = color(if (isSelected) UIKitR.color.primary_30 else UIKitR.color.black_30)
        if (shouldAnimate) {
            tag.strokeAnimator =
                ValueAnimator.ofArgb(card.strokeColor, newBorder).apply {
                    duration = 250L
                    addUpdateListener { card.setStrokeColor(it.animatedValue as Int) }
                    start()
                }
        } else {
            card.setStrokeColor(newBorder)
        }

        val offscreen = -(if (mark.width > 0) mark.width else 32.dp.toInt()).toFloat()
        if (isSelected) {
            mark.isVisible = true
            if (shouldAnimate) {
                mark.alpha = 0f
                mark.translationX = offscreen
                mark.translationY = offscreen
                mark
                    .animate()
                    .alpha(1f)
                    .translationX(0f)
                    .translationY(0f)
                    .setDuration(250L)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            } else {
                mark.alpha = 1f
                mark.translationX = 0f
                mark.translationY = 0f
            }
        } else if (shouldAnimate) {
            mark
                .animate()
                .alpha(0f)
                .translationX(offscreen)
                .translationY(offscreen)
                .setDuration(200L)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction { if (!tag.isSelected) mark.isVisible = false }
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
        showOptions(
            title = getString(R.string.fair_general_search_title),
            description =
                getString(R.string.fair_general_search_description, PromoSearchState.normalize(searchState.draft)),
            options = listOf(Option(id = "website", label = getString(R.string.fair_open_website)))) {
                openWebsite()
            }
    }

    private fun openWebsite() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.klikindomaret.com"))
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            showOptions(
                title = getString(R.string.fair_website_title),
                description = getString(R.string.fair_website_unavailable),
                options = emptyList()) {}
        }
    }

    private fun showDiscardConfirmation(onDiscard: () -> Unit) {
        if (supportFragmentManager.findFragmentByTag("fair-discard") != null) return
        val view = SheetFairDiscardBinding.inflate(inflater)
        var confirm: BottomSheetTray? = null
        view.stay.setOnClickListener { confirm?.dismiss() }
        view.discard.setOnClickListener {
            confirm?.dismiss()
            onDiscard()
        }
        confirm =
            BottomSheetTray.newInstance(title = getString(R.string.fair_discard_title), contentLayout = view.root)
                .apply {
                    dragHandleVisibility = false
                    titleDividerVisibility = true
                    shouldShowClose = false
                    customAnimationsEnabled = isMotionEnabled
                }
        confirm.show(supportFragmentManager, "fair-discard")
    }

    private fun hookTrayClose(dialog: Dialog, ownContent: View, onClose: () -> Unit) {
        val decor = dialog.window?.decorView ?: return
        val scrim = dialog.findViewById<View>(com.google.android.material.R.id.touch_outside)
        val min = 24.dp.toInt()
        val max = 64.dp.toInt()

        decor.post {
            val candidates = mutableListOf<View>()

            fun idName(v: View) =
                if (v.id == View.NO_ID) "-" else runCatching { resources.getResourceEntryName(v.id) }.getOrDefault("?")

            fun walk(v: View, depth: Int) {
                if (v === ownContent || v === scrim) return
                android.util.Log.d(
                    "FairDemo",
                    "tray view: ${"  ".repeat(depth)}${v.javaClass.simpleName} id=${idName(v)} clickable=${v.isClickable} ${v.width}x${v.height}")
                if (v.isShown && v.isClickable && v.width in min..max && v.height in min..max) candidates += v
                if (v is ViewGroup) for (i in 0 until v.childCount) walk(v.getChildAt(i), depth + 1)
            }
            walk(decor, 0)

            val close = candidates.maxByOrNull { v -> IntArray(2).also { v.getLocationOnScreen(it) }[0] }
            if (close != null) close.setOnClickListener { onClose() }
            else android.util.Log.w("FairDemo", "tray close button not found; ✕ is not guarded")
        }
    }

    private fun showScenarios() {
        showOptions(
            title = getString(R.string.fair_scenarios_title),
            description = getString(R.string.fair_scenarios_description),
            options = DemoScenario.entries.map { Option(id = it.name, label = scenarioLabel(it)) }) {
                cancelSearchRequest()
                searchState = PromoSearchState()
                state = scenarioState(DemoScenario.valueOf(it.id))
                brand = FILTER_ALL
                sortOrder = SORT_RELEVANCE

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
        val adapter =
            BaseAdapter.adapterOf<Option, ItemFairOptionBinding>(
                register =
                    BaseAdapter.Register(
                        onBindHolder = { _, option, row, _ ->
                            row.label.text = option.label
                            row.image.isVisible = option.image != null
                            option.image?.let { row.image.setImageResource(it) }
                            row.check.visibility = if (option.isSelected) View.VISIBLE else View.INVISIBLE
                            row.root.contentDescription = option.label
                            row.label.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                            row.root.isSelected = option.isSelected
                        }),
                diff =
                    BaseAdapter.Diff(
                        areItemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
                        areContentsTheSame = { oldItem, newItem -> oldItem == newItem }),
                itemList = options)
        adapter.setOnItemClickListener { _, option, _ ->
            tray?.dismiss()
            binding.root.post { onSelect(option) }
        }
        view.options.adapter = adapter
        view.options.isVisible = options.isNotEmpty()
        tray =
            (if (flow != null) TmhBottomSheet().apply {
                this.title = title
                contentView = view.root
            } else BottomSheetTray.newInstance(title = title, contentLayout = view.root)).apply {
                dragHandleVisibility = true
                shouldShowClose = true
                customAnimationsEnabled = isMotionEnabled
            }
        tray.show(supportFragmentManager, "fair-options")
    }

    private fun scenarioState(scenario: DemoScenario): PromoDemoState {
        promotion =
            if (scenario == DemoScenario.SEARCH_REDEMPTION || scenario == DemoScenario.START) DemoPromotion.REDEMPTION
            else DemoPromotion.FREE_GIFT
        shouldFailNextSearch = scenario == DemoScenario.SEARCH_ERROR
        var model = PromoDemoState(products = fixtureProducts(), giftOffers = giftOffers())

        if (promotion == DemoPromotion.REDEMPTION) {
            model =
                model.copy(
                    threshold = 50_000L,
                    rule =
                        if (scenario == DemoScenario.SEARCH_REDEMPTION) RewardRule.REPEATABLE else RewardRule.ONE_TIME)
        }
        if (scenario in listOf(DemoScenario.REPEATABLE, DemoScenario.REPEATABLE_PARTIAL, DemoScenario.REPEATABLE_THREE))
            model = model.copy(rule = RewardRule.REPEATABLE)
        val quantity =
            when (scenario) {
                DemoScenario.HALFWAY,
                DemoScenario.SEARCH_GIFT,
                DemoScenario.SEARCH_ERROR -> 1
                DemoScenario.ELIGIBLE,
                DemoScenario.CLAIMED,
                DemoScenario.QUOTA,
                DemoScenario.LIMIT -> 2
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
            DemoScenario.NEW_USER ->
                model.copy(
                    availability = PromoAvailability.NEW_CUSTOMER_ONLY, otherCartCount = 3, otherCartTotal = 999_999)
            DemoScenario.ZERO_PRODUCTS -> model.copy(products = emptyList())
            else -> model
        }
    }

    private fun fixtureProducts(): List<DemoProduct> =
        if (promotion == DemoPromotion.REDEMPTION) redemptionProducts()
        else TmhCatalogFixtures.paragonProducts(this)

    private fun redemptionProducts() =
        listOf(
            DemoProduct(
                id = "wash",
                name = "Emina Bright Stuff Face Wash 100ml",
                image = R.drawable.fair_emina_wash,
                isQualifying = true),
            DemoProduct(
                id = "aceh",
                name = "Indomie Mi Instan Goreng Aceh 90g",
                image = R.drawable.promo_product_aceh,
                price = 10_000,
                originalPrice = 6_400,
                promotion = "",
                benefit = "Beli 5, +99 Poin",
                highlight = ""),
            DemoProduct(
                id = "special",
                name = "Indomie Mi Instan Goreng Plus Special 80g",
                image = R.drawable.promo_product_mi_goreng,
                price = 10_000,
                isDiscounted = false,
                promotion = "Banyak Lebih Hemat",
                benefit = "Beli 3, +10 Stamp",
                highlight = "Paket Bundling"),
            DemoProduct(
                id = "ayam",
                name = "Indomie Mi Instan Goreng Ayam Geprek 85g",
                image = R.drawable.promo_product_ayam,
                price = 3_200,
                originalPrice = 6_400,
                promotion = "",
                benefit = "+99 Poin",
                highlight = ""),
            DemoProduct(
                id = "jumbo",
                name = "Indomie Mi Instan Goreng Jumbo Special 129g",
                image = R.drawable.promo_product_jumbo,
                price = 10_000,
                isDiscounted = false,
                promotion = "Paket Bundling",
                benefit = "Beli 3, +10 Stamp",
                highlight = "Banyak Lebih Hemat"))

    private fun redemptionOffers(): List<RedeemItem> {
        val original = if (promotion == DemoPromotion.FREE_GIFT) 50_000L else 10_000L
        val discountPercent = 50
        val price = original * (100 - discountPercent) / 100
        return listOf(
            RedeemItem(
                id = "gel",
                name = "Emina Water Barrier Balancing Gel 30g",
                image = R.drawable.fair_emina_gel,
                packSize = 2,
                originalPrice = original,
                price = price),
            RedeemItem(
                id = "scrub",
                name = "Kahf Face Wash Scrub Acne & Pore",
                image = R.drawable.fair_gift_cotton,
                originalPrice = original,
                price = price),
            RedeemItem(
                id = "sunscreen",
                name = "Wardah UV Shield Airy Sunscreen",
                image = R.drawable.fair_gift_cleanser,
                originalPrice = original,
                price = price,
                isRestocking = true))
    }

    private fun giftOffers() =
        redemptionOffers().map {
            GiftOffer(
                id = it.id,
                name = it.name,
                image = it.image,
                packSize = it.packSize,
                originalPrice = it.originalPrice,
                price = it.price,
                isRestocking = it.isRestocking)
        }

    private fun scenarioLabel(scenario: DemoScenario): String =
        getString(
            when (scenario) {
                DemoScenario.SEARCH_GIFT -> R.string.fair_scenario_search_gift
                DemoScenario.SEARCH_REDEMPTION -> R.string.fair_scenario_search_redemption
                DemoScenario.SEARCH_ERROR -> R.string.fair_scenario_search_error
                DemoScenario.START -> R.string.fair_scenario_start
                DemoScenario.HALFWAY -> R.string.fair_scenario_halfway
                DemoScenario.ELIGIBLE -> R.string.fair_scenario_eligible
                DemoScenario.CLAIMED -> R.string.fair_scenario_claimed
                DemoScenario.REPEATABLE -> R.string.fair_scenario_repeatable
                DemoScenario.REPEATABLE_PARTIAL -> R.string.fair_scenario_repeatable_partial
                DemoScenario.REPEATABLE_THREE -> R.string.fair_scenario_repeatable_three
                DemoScenario.QUOTA -> R.string.fair_scenario_quota
                DemoScenario.LIMIT -> R.string.fair_scenario_limit
                DemoScenario.NEW_USER -> R.string.fair_scenario_new_user
                DemoScenario.ZERO_PRODUCTS -> R.string.fair_scenario_zero_products
            })

    private fun money(value: Long): String = "Rp${rupiah.format(value)}"

    private fun restoreState(saved: Bundle?): PromoDemoState? {
        if (saved == null) return null
        val quantities = saved.getIntArray("quantities") ?: return null
        val products =
            if (saved.getBoolean("empty")) emptyList()
            else fixtureProducts().mapIndexed { i, p -> p.copy(quantity = quantities.getOrElse(i) { 0 }) }
        return PromoDemoState(
            products = products,
            rule = RewardRule.valueOf(saved.getString("rule") ?: "ONE_TIME"),
            availability = PromoAvailability.valueOf(saved.getString("availability") ?: "ACTIVE"),
            claimedGifts = saved.getStringArrayList("gifts") ?: emptyList(),
            threshold = saved.getLong("threshold", 100_000L),
            otherCartCount = saved.getInt("otherCount"),
            otherCartTotal = saved.getLong("otherTotal"),
            giftOffers = giftOffers())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paragon_fair_demo)
        val snapshot = savedInstanceState?.getBundle(TmhFlowState.EXTRA_FLOW)
            ?: intent.getBundleExtra(TmhFlowState.EXTRA_FLOW)
        fairId = savedInstanceState?.getString(TmhFlowState.EXTRA_FAIR_ID)
            ?: intent.getStringExtra(TmhFlowState.EXTRA_FAIR_ID)
        flow = snapshot?.let { TmhFlowState.restore(this, it) }
        if (flow != null && fairId != null) {
            promotion = DemoPromotion.REDEMPTION
            state = requireNotNull(flow).modelFor(requireNotNull(fairId))
        } else {
            promotion = DemoPromotion.valueOf(savedInstanceState?.getString("promotion") ?: "FREE_GIFT")
            state = restoreState(savedInstanceState)
                ?: scenarioState(DemoScenario.entries.firstOrNull { it.name == intent.getStringExtra(EXTRA_SCENARIO) }
                    ?: DemoScenario.START)
        }
        supportFragmentManager.setFragmentResultListener(TmhRedemptionBottomSheet.RESULT_SAVE, this) { _, result ->
            val id = fairId ?: return@setFragmentResultListener
            val quantities = result.getBundle(TmhRedemptionBottomSheet.KEY_QUANTITIES)?.quantities().orEmpty()
            flow = flow?.withSelection(id, quantities)
            state = requireNotNull(flow).modelFor(id)
            render(shouldAnnounce = true)
            animateProgress(state.barProgress)
            showSkeleton {
                Snackbar.make(binding.root, R.string.tmh_cart_updated, Snackbar.LENGTH_SHORT).show()
            }
        }
        savedInstanceState?.let {
            searchState =
                PromoSearchState(
                    mode = SearchMode.valueOf(it.getString("searchMode") ?: "DETAIL"),
                    draft = it.getString("searchDraft") ?: "",
                    submitted = it.getString("searchSubmitted") ?: "",
                    isFallbackAvailable = it.getBoolean("searchFallback"),
                    hasSubmission = it.getBoolean("searchHasSubmission"))
            shouldFailNextSearch = it.getBoolean("searchFailNext")
            detailScroll = it.getParcelable("detailScroll")
            resultScroll = it.getParcelable("resultScroll")
        }
        columns = savedInstanceState?.getInt("columns", 2) ?: 2
        brand = savedInstanceState?.getString("brand") ?: FILTER_ALL
        sortOrder = savedInstanceState?.getString("sort") ?: SORT_RELEVANCE

        displayedProgress = if (savedInstanceState != null) state.barProgress else 0f
        val grid =
            StaggeredGridLayoutManager(effectiveColumns, StaggeredGridLayoutManager.VERTICAL).apply {
                gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
            }
        binding.products.layoutManager = grid
        binding.products.updatePadding(left = gridEdge - gridGap / 2, right = gridEdge - gridGap / 2)
        binding.products.adapter =
            ConcatAdapter(headerAdapter, scopeAdapter, sectionAdapter, productAdapter, emptyAdapter, loadingAdapter)
        binding.products.addOnChildAttachStateChangeListener(
            object : RecyclerView.OnChildAttachStateChangeListener {
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
        binding.products.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) = updateCompactHeader()
            })
        binding.bottomActions.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            if (flow != null) binding.products.updatePadding(bottom = binding.bottomActions.height + 12.dp.toInt())
            binding.products.clipToPadding = false
            binding.products.clipChildren = false
            (binding.products.parent as? ViewGroup)?.clipChildren = false
            binding.products.clipToPadding = false
        }
        binding.back.setOnClickListener { if (isInSearch) closeSearch() else onBackPressedDispatcher.onBackPressed() }
        if (flow == null) binding.title.setOnLongClickListener {
            showScenarios()
            true
        }
        if (flow == null) ViewCompat.replaceAccessibilityAction(
            binding.title,
            AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_LONG_CLICK,
            getString(R.string.fair_scenario_action)) { _, _ ->
                showScenarios()
                true
            }

        configureSearchField()
        listOf(binding.search, binding.share, binding.cart, binding.sort, binding.filter).forEach { it.makeStatic() }
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
        if (isEditingSearch) focusSearch()
        if (searchState.mode == SearchMode.LOADING) submitSearch()
        if (savedInstanceState == null && intent.getBooleanExtra(EXTRA_SEARCH, false)) showSearch()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        flow?.let { outState.putBundle(TmhFlowState.EXTRA_FLOW, it.toBundle()) }
        outState.putString(TmhFlowState.EXTRA_FAIR_ID, fairId)
        outState.putString("promotion", promotion.name)
        outState.putLong("threshold", state.threshold)
        outState.putString("searchMode", searchState.mode.name)
        outState.putString("searchDraft", searchState.draft)
        outState.putString("searchSubmitted", searchState.submitted)
        outState.putBoolean("searchFallback", searchState.isFallbackAvailable)
        outState.putBoolean("searchHasSubmission", searchState.hasSubmission)
        outState.putBoolean("searchFailNext", shouldFailNextSearch)
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

    override fun onDestroy() {
        cancelSearchRequest()
        searchHandler.removeCallbacksAndMessages(null)
        header = null
        skeletonFinish?.let { binding.skeleton.root.removeCallbacks(it) }
        binding.skeleton.shimmer.stopShimmer()
        binding.products.removeCallbacks(clearFocusTask)
        super.onDestroy()
    }

    private fun View.makeStatic() {
        setOnClickListener(null)
        setOnLongClickListener(null)
        isClickable = false
        isLongClickable = false
        isFocusable = false
    }

    private fun StepperButton.showExpanded() {
        val width = requireNotNull(dimenPixelSize(UIKitR.dimen.dimen_90))
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

    private fun StepperButton.applyRedeemStyle(shouldDimCount: Boolean) {
        val grey = color(UIKitR.color.black_30)
        val darkGrey = color(UIKitR.color.black_60)
        val blue = color(UIKitR.color.primary_30)
        val white = color(UIKitR.color.white)

        fun states(enabled: Int, disabled: Int) =
            ColorStateList(
                arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf()), intArrayOf(disabled, enabled))

        binding.stepperContainer.apply {
            setCardBackgroundColor(white)
            setStrokeColor(grey)
            strokeWidth = 1.dp.toInt()
        }
        binding.minusButton.iconTint = states(darkGrey, grey)
        binding.plusButton.backgroundTintList = states(blue, grey)
        binding.plusButton.iconTint = ColorStateList.valueOf(white)
        binding.countText.setTextColor(if (shouldDimCount) grey else color(UIKitR.color.black_70))
        binding.countText.alpha = 1f
    }

    private class SelectionTag(val itemId: String, var isSelected: Boolean, var strokeAnimator: ValueAnimator? = null)

    private inner class ProductSpacingDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: android.graphics.Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            if (position == RecyclerView.NO_POSITION) {
                outRect.set(0, 0, 0, 0)
                return
            }

            val firstProduct = headerAdapter.itemCount + scopeAdapter.itemCount + sectionAdapter.itemCount
            val lastProduct = firstProduct + productAdapter.itemCount
            val half = gridGap / 2

            if (position in firstProduct until lastProduct) {
                outRect.set(half, half, half, half) // same on every side: no column or span lookup needed
            } else {
                outRect.set(
                    -(gridEdge - half),
                    0,
                    -(gridEdge - half),
                    0) // header, section, empty and loading rows cancel the list padding
            }
        }
    }

    private data class HeaderRow(val state: PromoDemoState, val id: String = "header")

    private data class SectionRow(val count: Int, val columns: Int, val id: String = "section")

    private data class ProductRow(
        val product: DemoProduct,
        val availability: PromoAvailability,
        val columns: Int,
        val isSearch: Boolean,
        val promotion: DemoPromotion,
        val isFocused: Boolean = false
    ) {
        val id
            get() = product.id
    }

    private data class Option(
        val id: String,
        val label: String,
        val image: Int? = null,
        val isSelected: Boolean = false
    )

    private data class ScopeRow(val id: String, val query: String, val description: String)

    private data class EmptyRow(
        val mode: SearchMode,
        val hasWebsiteFallback: Boolean,
        val isFiltered: Boolean = false,
        val id: String = "empty"
    )

    private data class RedeemItem(
        val id: String,
        val name: String,
        val image: Int,
        val packSize: Int = 1,
        val originalPrice: Long = 10_000,
        val price: Long = 5_000,
        val isRestocking: Boolean = false,
        val quantity: Int = 0
    )

    private data class RedeemRow(
        val item: RedeemItem,
        val shouldAllowAdd: Boolean,
        val isLocked: Boolean,
        val remaining: Int
    ) {
        val id
            get() = item.id
    }

    private enum class StepperFace {
        ADD,
        FOCUSED,
        COUNT
    }

    private class FaceTag(
        val productId: String,
        val face: StepperFace,
        var animator: AnimatorSet? = null,
        var isCancelled: Boolean = false
    )

    companion object {
        const val EXTRA_SEARCH = "fair_search_demo"
        const val EXTRA_SCENARIO = "fair_scenario"
        private const val FILTER_ALL = "Semua"
        private const val SORT_RELEVANCE = "Relevansi"
        private const val SORT_NAME = "Nama A–Z"
        private const val SORT_PRICE_LOW = "Harga terendah"
        private const val SORT_PRICE_HIGH = "Harga tertinggi"
    }
}
