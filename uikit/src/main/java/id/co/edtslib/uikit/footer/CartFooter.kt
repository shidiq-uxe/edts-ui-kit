package id.co.edtslib.uikit.footer

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import androidx.annotation.RawRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.withStyledAttributes
import androidx.core.text.parseAsHtml
import androidx.core.view.doOnLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.databinding.ViewCartFooterKitBinding
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.drawable
import id.co.edtslib.uikit.utils.html.FontManager
import id.co.edtslib.uikit.utils.html.HtmlRenderer
import id.co.edtslib.uikit.utils.html.HtmlRendererConfig
import id.co.edtslib.uikit.utils.html.semiBoldStyle
import id.co.edtslib.uikit.utils.inflater
import id.co.edtslib.uikit.utils.setDrawable
import id.co.edtslib.uikit.utils.setGradientBackground

open class CartFooter @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(ContextThemeWrapper(context, R.style.Theme_EDTS_UIKit), attrs, defStyleAttr) {

    private val binding = ViewCartFooterKitBinding.inflate(this.context.inflater, this, true)

    private val skeletonLoaders = listOf(
        binding.shimmerTotal,
        binding.shimmerButton,
        binding.shimmerDiscountBadge,
        binding.shimmerCashback,
        binding.extendedCouponSection.binding.shimmerCoupon1,
        binding.extendedCouponSection.binding.shimmerCoupon2
    )

    var delegate: CartFooterDelegate? = null

    val extendedFooter = binding.extendedCouponSection

    var infoText: CharSequence? = null
        set(value) {
            field = value

            val fontManager = FontManager(context)
            val config = HtmlRendererConfig(
                fontStyles = mapOf("myb" to fontManager.semiBoldStyle())
            )

            val htmlRenderer = HtmlRenderer(config, fontManager)

            binding.extendedCouponSection.infoText = htmlRenderer.render(value.toString(), binding.extendedCouponSection.binding.tvInfo)

        }

    var totalText: CharSequence? = null
        set(value) {
            field = value
            binding.tvTotal.text = value
            binding.tvTotal.setDrawable(
                drawableRight = if (value.isNullOrEmpty()) null else context.drawable(R.drawable.ic_chevron_up_16)
            )
        }

    var buttonText: CharSequence? = null
        set(value) {
            field = value
            binding.btnSubmit.text = value
        }

    var isCouponSectionExpanded: Boolean = true
        set(value) {
            field = value
            binding.extendedCouponSection.isExpanded = value
        }

    var isInfoSectionVisible: Boolean = true
        set(value) {
            field = value
            binding.flInfoSection.isVisible = value
            isCouponSectionExpanded = value
        }

    var isConfettiBackgroundVisible: Boolean = true
        set(value) {
            field = value

            val confettiView = binding.extendedCouponSection.binding.ivConfetti
            val lottieView = binding.extendedCouponSection.binding.lottieLayer

            if (!value) {
                confettiView.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .withEndAction {
                        confettiView.isVisible = false
                        lottieView.isVisible = false
                    }
            } else {
                confettiView.isVisible = true
                lottieView.isVisible = true
            }

            binding.extendedCouponSection.binding.tvInfo.setDrawable(
                drawableLeft = context.drawable(R.drawable.ic_voucher_16)
            )
        }


    var isDiscountBadgeVisible: Boolean = false
        set(value) {
            field = value
            binding.discountBadge.isVisible = value
        }

    var discountBadgeText: CharSequence? = null
        set(value) {
            field = value
            isDiscountBadgeVisible = !value.isNullOrEmpty()
            binding.discountBadge.text = value
        }

    var gradientColors = intArrayOf(context.color(R.color.white), context.color(R.color.support_gradient))
        set(value) {
            field = value
            setGradientBackground(value)
        }

    var isLoading: Boolean = false
        set(value) {
            field = value
            binding.btnSubmit.isEnabled = !value
            binding.btnSubmit.text = if (value) null else buttonText
            binding.tvTotal.text = if (value) "" else totalText
            binding.tvTotal.setDrawable(
                drawableRight = if (value) null else context.drawable(R.drawable.ic_chevron_up_16)
            )

            binding.btnSubmit.isInvisible = value
            binding.discountBadge.isVisible = !value
            binding.extendedCouponSection.binding.tvInfo.isInvisible = value
            binding.flCashbackBadge.isVisible = !value

            skeletonLoaders.forEach {
                it.isVisible = value
            }
        }

    var cashbackBadgeIcon: Drawable = context.drawable(R.drawable.ill_point_n_stars_24)
        set(value) {
            field = value
            binding.illPointIcon.setImageDrawable(value)
        }

    var cashbackBadgeText: CharSequence? = null
        set(value) {
            field = value
            value?.let {
                binding.tvPromoPointInfo.text = if (isHtml) it.toString().parseAsHtml() else it
            }
        }

    var isHtml: Boolean = false
        set(value) {
            field = value
            binding.tvPromoPointInfo.text = if (value) cashbackBadgeText.toString().parseAsHtml() else cashbackBadgeText
        }

    var isCashbackBadgeVisible: Boolean = false
        set(value) {
            field = value
            binding.flCashbackBadge.isVisible = value
        }

    var isButtonEnabled: Boolean = false
        set(value) {
            field = value
            binding.btnSubmit.isEnabled = value
        }


    @RawRes
    var animationRawRes: Int = R.raw.applied_coupon_confetti

    init {
        initAttrs(attrs)
        bindClickAction()
    }

    private fun initAttrs(attrs: AttributeSet?) {
        this.context.withStyledAttributes(attrs, R.styleable.CartFooter) {
            infoText = getString(R.styleable.CartFooter_infoText)
            buttonText = getString(R.styleable.CartFooter_buttonText)
            isCouponSectionExpanded = getBoolean(R.styleable.CartFooter_isExpanded, isCouponSectionExpanded)
        }

        setGradientBackground(gradientColors)
    }

    fun playPreLoadedAnimations() {
        with(binding.extendedCouponSection.binding) {
            this.ivConfetti.alpha = 0f

            val fadeThreshold = 0.45f
            var currentLoop = 0
            var shouldTrigger = false

            if (!isConfettiBackgroundVisible) {
                isConfettiBackgroundVisible = true
            }

            lottieLayer.apply {
                setAnimation(animationRawRes)
                repeatCount = 1
                playAnimation()
            }

            lottieLayer.addAnimatorListener(object : AnimatorListenerAdapter() {
                override fun onAnimationRepeat(animation: Animator) {
                    currentLoop++
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (currentLoop == lottieLayer.repeatCount) {
                        shouldTrigger = true
                    }
                }

            })

            lottieLayer.addAnimatorUpdateListener { _ ->
                val progress = lottieLayer.progress
                val finalLoopIndex = lottieLayer.repeatCount

                if (!shouldTrigger && currentLoop == finalLoopIndex && progress >= fadeThreshold) {
                    shouldTrigger = true

                    ivConfetti.animate()
                        .alpha(1f)
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .setDuration(500)
                        .withStartAction {
                            binding.extendedCouponSection.binding.tvInfo.setDrawable(
                                drawableLeft = context.drawable(R.drawable.ic_voucher_applied_16)
                            )
                        }
                        .start()
                }
            }
        }
    }

    private fun bindClickAction() {
        binding.btnSubmit.setOnClickListener {
            delegate?.onActionButtonClick()
        }
        binding.tvTotal.setOnClickListener {
            delegate?.onSummaryClick()
        }
        extendedFooter.delegate = object : CartCouponExtendedFooterDelegate {
            override fun onFooterClick(view: View) {
                delegate?.onCouponSectionClick()
            }
        }
    }

    fun setGradientBackground(
        colors: IntArray,
        orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT,
    ) {
        binding.extendedCouponSection.binding.efContainer.setGradientBackground(
            colors = colors,
            orientation = orientation,
        )
    }

    fun attachToRecyclerView(
        recyclerView: RecyclerView,
        includePadding: Boolean = true,
    ) {
        binding.extendedCouponSection.attachToRecyclerView(recyclerView)

        if (includePadding) {
            recyclerView.clipToPadding = false
            doOnLayout {
                recyclerView.updatePadding(bottom = height.plus(recyclerView.paddingBottom))
            }
        }
    }

}