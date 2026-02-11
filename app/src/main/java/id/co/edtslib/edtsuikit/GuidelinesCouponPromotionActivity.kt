package id.co.edtslib.edtsuikit

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.View.generateViewId
import android.view.animation.DecelerateInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesCouponPromotionBinding
import id.co.edtslib.edtsuikit.databinding.ItemPromotionCoupon2Binding
import id.co.edtslib.edtsuikit.databinding.ItemPromotionCouponCardBinding
import id.co.edtslib.uikit.adapter.BaseMultipleTypeAdapter
import id.co.edtslib.uikit.adapter.BaseViewType
import id.co.edtslib.uikit.adapter.adapterWithLoadingHolder
import id.co.edtslib.uikit.pulltorefresh.LiteRefreshDelegate
import id.co.edtslib.uikit.utils.MarginItem
import id.co.edtslib.uikit.utils.alertSnack
import id.co.edtslib.uikit.utils.attachLinearMarginItemDecoration
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.drawable
import id.co.edtslib.uikit.utils.hideLoading
import id.co.edtslib.uikit.utils.html.FontManager
import id.co.edtslib.uikit.utils.html.HtmlListConfig
import id.co.edtslib.uikit.utils.html.HtmlRenderer
import id.co.edtslib.uikit.utils.html.HtmlRendererConfig
import id.co.edtslib.uikit.utils.html.applyHtmlConfig
import id.co.edtslib.uikit.utils.html.renderHtml
import id.co.edtslib.uikit.utils.html.semiBoldStyle
import id.co.edtslib.uikit.utils.isDeviceStruggling
import id.co.edtslib.uikit.utils.showFloatingAnimation
import id.co.edtslib.uikit.utils.showLoading
import id.co.edtslib.uikit.utils.sp
import kotlin.math.abs
import id.co.edtslib.uikit.R as UIKitR

class  GuidelinesCouponPromotionActivity : AppCompatActivity(), LiteRefreshDelegate {

    private val binding by viewBinding<ActivityGuidelinesCouponPromotionBinding>()

    private var adapter: BaseMultipleTypeAdapter<CouponItem, ItemPromotionCoupon2Binding, ItemPromotionCouponCardBinding>? = null
    private var items: List<CouponItem> = emptyList()

    private var count = 5
        set(value) {
            field = value
            binding.liquidGlassCard1.badgeText = value.toString()
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            binding.appBarLayout.setPadding(0, systemBars.top, 0, 0)
            binding.rvCoupons.updatePadding(bottom = systemBars.bottom.plus(12.dp.toInt()))

            insets
        }

        attachRecyclerViewWithParent()
        dummyAdapter()
        setupScrollingLogic()
        onFilterClick()

        binding.smartRefreshLayout.refreshDelegate = this

        binding.liquidGlassCard1.setupBlur(this, binding.blurTarget)

        binding.liquidGlassCard1.setOnClickListener {
            startActivity(Intent(this, GuidelinesMyCouponActivity::class.java))
        }
    }


    override fun onPull(percent: Float, offset: Float) {}

    override fun onRefreshing() {
        simulateFakeShimmerLoading(2500)
    }

    override fun onFinish() {
    }

    var lastScrimIsLight = false
    var lastIsCollapsed = false

    private var isTabHidden = false
    private var radiusAnimator: ValueAnimator? = null


    /*TODO: Tab Level 2 Elevation and color interpolation*/
    private fun setupScrollingLogic() {
        val toolbarContentColors = Pair(color(R.color.white), color(id.co.edtslib.uikit.R.color.black_50))
        val tabLevel2Colors = Pair(color(id.co.edtslib.uikit.R.color.black_10), color(R.color.white))

        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange
            val percentage = (abs(verticalOffset).toFloat() / totalScrollRange).coerceIn(0f, 1f)
            val fadeMultiplier = 1.75f

            binding.liquidGlassCard1.alpha = (1 - percentage * fadeMultiplier).coerceIn(0f, 1f)

            val toolbarInterpolatedColor = ArgbEvaluator().evaluate(percentage, toolbarContentColors.first, toolbarContentColors.second) as Int
            binding.toolbar.setNavigationIconTint(toolbarInterpolatedColor)

            val tabLevel2InterpolatedColor = ArgbEvaluator().evaluate(percentage, tabLevel2Colors.first, tabLevel2Colors.second) as Int
            binding.cgTabLevel2.background = tabLevel2InterpolatedColor.toDrawable()

            val scrimThreshold = 0.55f
            val scrimIsLight = percentage >= scrimThreshold

            if (scrimIsLight != lastScrimIsLight) {
                lastScrimIsLight = scrimIsLight

                WindowCompat.getInsetsController(window, window.decorView)
                    .isAppearanceLightStatusBars = scrimIsLight

                isDeviceStruggling { isDeviceStruggling ->
                    if (isDeviceStruggling) {
                        val radius = if (lastIsCollapsed) 12.dp else 0.dp
                        binding.stickyTabContainer.shapeAppearanceModel =
                            binding.stickyTabContainer.shapeAppearanceModel
                                .toBuilder()
                                .setTopLeftCornerSize(radius)
                                .setTopRightCornerSize(radius)
                                .build()
                    } else {
                        animateRadiusChange(scrimIsLight)
                    }
                }
            }

            val isCollapsed = abs(verticalOffset) >= totalScrollRange
            if (lastIsCollapsed != isCollapsed) {
                lastIsCollapsed = isCollapsed

                animateElevation(binding.cgTabLevel2, if (isCollapsed) 2.dp else 0f)
            }
        }

        binding.rvCoupons.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (lastIsCollapsed && dy != 0 && !isTabHidden) {
                    hideTab()
                } else if (lastIsCollapsed.not()) {
                    showTab(0L, 0L)
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE && isTabHidden || lastIsCollapsed.not()) {
                    showTab()
                }
            }
        })
    }

    private fun onFilterClick() {
        val filterList = listOf("Semua", "Xtra", "Xpress")

        addFilterChip(filterList)
        checkChipByPosition(0)

        setOnChipCheckListener {
            if (!binding.rvCoupons.canScrollVertically(-1)) {
                binding.appBarLayout.setExpanded(true, true)
                return@setOnChipCheckListener
            }

            binding.rvCoupons.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        recyclerView.removeOnScrollListener(this)

                        binding.appBarLayout.setExpanded(true, true)
                    }
                }
            })

            Handler(Looper.getMainLooper()).postDelayed({
                simulateFakeShimmerLoading()
            }, 100)

            binding.rvCoupons.smoothScrollToPosition(0)
        }
    }

    // Todo : Workaround
    private fun attachRecyclerViewWithParent(immediatelyAttach: Boolean = false) {
        if (immediatelyAttach) {
            binding.cgTabLevel2.post {
                binding.rvCoupons.updatePadding(top = binding.cgTabLevel2.height)
                binding.rvCoupons.scrollToPosition(0)
            }
        } else {
            binding.shimmerTL2Container.post {
                binding.rvCoupons.updatePadding(top = binding.shimmerTL2Container.height)
                binding.rvCoupons.scrollToPosition(0)
            }
        }
    }

    private fun hideTab(
        duration: Long = 150L,
        delay: Long = 100L,
    ) {
        isTabHidden = true
        binding.cgTabLevel2.animate()
            .translationY(-binding.cgTabLevel2.height.plus(2.dp).toFloat())
            .setDuration(duration)
            .setStartDelay(delay)
            .start()
    }

    private fun showTab(
        duration: Long = 200L,
        delay: Long = 300L,
    ) {
        isTabHidden = false
        binding.cgTabLevel2.animate()
            .translationY(0f)
            .setDuration(duration)
            .setStartDelay(delay)
            .start()
    }

    private var elevationAnimator: ValueAnimator? = null

    private fun animateElevation(target: View, to: Float, duration: Long = 250L) {
        elevationAnimator?.cancel()

        val from = target.elevation

        elevationAnimator = ValueAnimator.ofFloat(from, to).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                target.elevation = animator.animatedValue as Float
            }
            start()
        }
    }



    private fun animateRadiusChange(isCollapsed: Boolean) {
        val from = if (isCollapsed) 12.dp else 0.dp
        val to = if (isCollapsed) 0.dp else 12.dp

        radiusAnimator?.cancel()

        radiusAnimator = ValueAnimator.ofFloat(from, to).apply {
            duration = 180L
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                val value = animator.animatedValue as Float
                binding.stickyTabContainer.shapeAppearanceModel =
                    binding.stickyTabContainer.shapeAppearanceModel
                        .toBuilder()
                        .setTopLeftCornerSize(value)
                        .setTopRightCornerSize(value)
                        .build()
            }
            start()
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun dummyAdapter() {
        val dummyTitleList = listOf(
            "Diskon Ongkir Hingga Rp3.000 Pakai Semua Pengiriman",
            "Diskon Ongkir Hingga 5% (maks. Rp5.000) Pakai Pengiriman Tertentu",
            "Diskon Ongkir Rp7.000 Pakai Instan",
            "Diskon Ongkir Rp2.000 Pakai Reguler",
        )

        val dummyCountDownList = listOf(
            Pair("7 hari lagi", color(UIKitR.color.black_60)),
            Pair("20 hari lagi", color(UIKitR.color.black_60)),
            Pair("59 menit lagi", color(UIKitR.color.red_30)),
            Pair("3 menit lagi", color(UIKitR.color.red_30)),
        )

        items = List(25) {
            CouponItem(
                id = it.toLong(),
                title = dummyTitleList.random(),
                timeRemaining = dummyCountDownList.random(),
                isActive = it < 12
            )
        }

        adapter = adapterWithLoadingHolder<CouponItem, ItemPromotionCoupon2Binding, ItemPromotionCouponCardBinding>(
            register = BaseMultipleTypeAdapter.Register(
                loadingSize = 5,
                onBindHolder = { position, item, adapterBinding ->
                    val cardView = adapterBinding.card

                    val titleColor = color(id.co.edtslib.uikit.R.color.black_70)
                    val infoColor = color(id.co.edtslib.uikit.R.color.black_60)
                    val disabledColor = color(id.co.edtslib.uikit.R.color.black_40)

                    val titleConditionColor = if (item.isActive) titleColor else disabledColor
                    val infoConditionColor = if (item.isActive) infoColor else disabledColor

                    // Todo: Add Function
                    adapterBinding.ribbon.isVisible = position == 0
                    cardView.binding.tvTitle.setTextColor(titleConditionColor)
                    cardView.binding.tvEnd.setTextColor(infoConditionColor)
                    cardView.binding.tvEnd2.setTextColor(infoConditionColor)
                    cardView.binding.tvEnd3.setTextColor(infoConditionColor)

                    cardView.binding.greyScale.disabled = !item.isActive
                    cardView.binding.availableGroup.isVisible = item.isActive
                    cardView.binding.disabledFooter.isVisible = !item.isActive

                    cardView.binding.tvTitle.text = item.title

                    val htmlCoupon = "Kode: <b>BARUINSTAN10RB***</b>"

                    val fontManager = FontManager(this@GuidelinesCouponPromotionActivity)
                    val config = HtmlRendererConfig(
                        fontStyles = mapOf("myb" to fontManager.semiBoldStyle(color(UIKitR.color.black_50)))
                    )

                    // Todo : Fix Default Font Properties
                    cardView.binding.couponCode.applyHtmlConfig(HtmlListConfig(
                        textSizeSp = cardView.binding.couponCode.textSize.sp,
                    ))
                        .renderHtml(htmlCoupon.toString(), HtmlRenderer(config, fontManager))

                    if (item.isRedeemed) {
                        cardView.binding.btnRedeem.setButtonToClickedState(UIKitR.color.black_60)

                        // Todo : Wrap in function
                        cardView.binding.tvStart3.text = if (item.isRedeemed) "Berlaku Hingga" else "Periode"
                        cardView.binding.tvEnd3.text = item.timeRemaining.first
                        cardView.binding.tvEnd3.setTextColor(ColorStateList.valueOf(item.timeRemaining.second))
                    }

                    cardView.binding.btnRedeem.setOnTouchListener { v, event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                v.animate()
                                    .scaleX(0.95f)
                                    .scaleY(0.95f)
                                    .setDuration(120)
                                    .start()
                            }

                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                v.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(120)
                                    .start()
                            }
                        }
                        false
                    }

                    cardView.binding.btnRedeem.setOnClickListener {
                        this.showLoading()

                        Handler(Looper.getMainLooper()).postDelayed({
                            cardView.binding.btnRedeem.revertButtonState()

                            this.hideLoading()

                            if (position == 2) {
                                binding.root.alertSnack(
                                    message = "Kupon tidak bisa ditukar",
                                    textVerticalPadding = 4.dp.toInt()
                                )

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    it.performHapticFeedback(HapticFeedbackConstants.REJECT)
                                } else {
                                    it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                }

                                Handler(Looper.getMainLooper()).postDelayed({
                                   simulateFakeShimmerLoading()
                                }, 200)
                            } else {
                                it.showFloatingAnimation(
                                    icon = id.co.edtslib.uikit.R.drawable.ic_voucher_applied_16,
                                    text = "+1",
                                )

                                count += 1

                                // Todo : Wrap in function
                                cardView.binding.tvStart3.text = "Berlaku Hingga"
                                cardView.binding.tvEnd3.text = item.timeRemaining.first
                                cardView.binding.tvEnd3.setTextColor(ColorStateList.valueOf(item.timeRemaining.second))

                                cardView.binding.btnRedeem.setButtonToClickedState(UIKitR.color.black_60)
                            }
                        }, 1500)


                    }
                }
            ),
            diff = BaseMultipleTypeAdapter.Diff(
                areItemsTheSame = { old, new -> old.id == new.id },
                areContentsTheSame = { old, new -> old == new }
            ),
            itemList = items
        )

        binding.rvCoupons.adapter =  adapter

        binding.rvCoupons.attachLinearMarginItemDecoration(
            orientation = LinearLayoutManager.VERTICAL,
            margin = MarginItem(first =  0, top = 0)
        )

        simulateFakeShimmerLoading()
    }

    private fun simulateFakeShimmerLoading(
        duration: Long = 2000L
    ) {
        binding.rvCoupons.scrollToPosition(0)
        adapter?.viewType = BaseViewType.LOADING

        binding.shimmerTL1Container.isVisible = true
        binding.shimmerTL2Container.isVisible = true

        binding.tlLevel1.isVisible = false
        binding.cgTabLevel2.isVisible = false

        Handler(Looper.getMainLooper()).postDelayed( {
            adapter?.viewType = BaseViewType.INITIAL

            binding.shimmerTL1Container.isVisible = false
            binding.shimmerTL2Container.isVisible = false

            binding.tlLevel1.isVisible = true
            binding.cgTabLevel2.isVisible = true

            attachRecyclerViewWithParent(true)

        }, duration )
    }

    private var onChipCheckListener: () -> Unit = {}

    private fun setOnChipCheckListener(action: () -> Unit) {
        onChipCheckListener = action
    }

    private fun addFilterChip(
        chipItems: List<String>,
        chipAttrs: Chip.() -> Unit = {}
    ) {
        chipItems.forEachIndexed { position, text ->
            val chip = Chip(this, null, UIKitR.attr.chipFilterStyle).apply {
                this.id = generateViewId()
                this.text = text

                chipAttrs()

                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        checkChipByPosition(position)
                        onChipCheckListener.invoke()
                    }
                }
            }

            binding.cgTabLevel2.addView(chip)
        }
    }



    private fun checkChipByPosition(position: Int) {
        if (binding.cgTabLevel2.isNotEmpty()) {
            (binding.cgTabLevel2.getChildAt(position) as? Chip)?.isChecked = true
        }
    }

    fun MaterialButton.setButtonToClickedState(
        strokeColorRes: Int
    ) {
        tag = tag ?: ButtonState(
            backgroundColor = backgroundTintList,
            textColor = currentTextColor,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth
        )

        this.background = context.drawable(R.drawable.bg_stroke_button)

        setTextColor(ContextCompat.getColor(context, strokeColorRes))

        this.text = "Lihat"

        isEnabled = false
    }

    // Helper to revert button state
    fun MaterialButton.revertButtonState() {
        val savedState = tag as? ButtonState ?: return

        backgroundTintList = savedState.backgroundColor
        setTextColor(savedState.textColor)
        strokeColor = savedState.strokeColor
        strokeWidth = savedState.strokeWidth
        isEnabled = true
    }

    // Data class to store button state
    private data class ButtonState(
        val backgroundColor: android.content.res.ColorStateList?,
        val textColor: Int,
        val strokeColor: android.content.res.ColorStateList?,
        val strokeWidth: Int
    )

    data class CouponItem(
        val id: Long,
        val title: String,
        val timeRemaining: Pair<String, Int>,
        val isRedeemed: Boolean = false,
        val isActive: Boolean = true,
    )

}