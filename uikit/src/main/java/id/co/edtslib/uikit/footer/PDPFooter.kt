package id.co.edtslib.uikit.footer

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import id.co.edtslib.uikit.databinding.ViewPdpFooterBinding
import id.co.edtslib.uikit.utils.interpolator.EaseInterpolator

/**
 * Custom view that morphs between cart button and stepper states
 * Subtle morphing animation optimized for performance
 */
class PDPFooter @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val binding: ViewPdpFooterBinding =
        ViewPdpFooterBinding.inflate(LayoutInflater.from(context), this, true)
    private var currentState = CartState.DEFAULT
    private var isAnimating = false

    enum class CartState {
        DEFAULT,  // Shows btnAddToCart
        FINALE    // Shows stepper, price, delete button
    }

    // Callbacks
    var onAddToCartClick: (() -> Unit)? = null
    var onDeleteClick: (() -> Unit)? = null
    var onQuantityChanged: ((Int) -> Unit)? = null
    var onStateChanged: ((CartState) -> Unit)? = null

    init {
        setupView()
        setupClickListeners()
    }

    private fun setupView() {
        binding.qtyBadge.includeStroke = true
    }

    private fun setupClickListeners() {
        binding.btnAddToCart.setOnClickListener {
            if (!isAnimating) {
                onAddToCartClick?.invoke()
                setStateImmediately(PDPFooter.CartState.FINALE)
                binding.sbQuantity.setCount(1)
            }
        }

        binding.btnDelete.setOnClickListener {
            if (!isAnimating) {
                onDeleteClick?.invoke()
                setStateImmediately(CartState.DEFAULT)
            }
        }

    }

    /**
     * Morph button into stepper (Default → Finale)
     * Subtle animation: button shrinks to stepper position on the right
     */
    fun animateToFinaleState(onComplete: (() -> Unit)? = null) {
        if (currentState == CartState.FINALE || isAnimating) return
        isAnimating = true

        val button = binding.btnAddToCart
        val stepper = binding.sbQuantity
        val tvPrice = binding.tvPrice
        val tvHelper = binding.tvHelperTitle
        val btnDelete = binding.btnDelete

        // Measure stepper size
        stepper.measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )

        val buttonStartWidth = button.width.toFloat()
        val stepperWidth = stepper.measuredWidth.toFloat()

        // Calculate how much to translate button to align with stepper position
        val buttonEndMargin = context.resources.displayMetrics.density * 16 // s dimension
        val translateX = buttonStartWidth - stepperWidth - buttonEndMargin

        val morphSet = AnimatorSet()

        // 1. Shrink button width
        val shrinkWidth = ValueAnimator.ofFloat(buttonStartWidth, stepperWidth).apply {
            duration = ANIMATION_DURATION
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                val value = animator.animatedValue as Float
                button.layoutParams.width = value.toInt()
                button.requestLayout()
            }
        }

        // 2. Move button to the right (to stepper position)
        val moveButton = ObjectAnimator.ofFloat(button, "translationX", 0f, translateX).apply {
            duration = ANIMATION_DURATION
            interpolator = DecelerateInterpolator()
        }

        // 3. Fade out button content
        val fadeOutButton = ObjectAnimator.ofFloat(button, "alpha", 1f, 0f).apply {
            duration = FADE_DURATION
        }

        // 4. Prepare and fade in stepper
        stepper.alpha = 0f
        stepper.visibility = View.VISIBLE

        val fadeInStepper = ObjectAnimator.ofFloat(stepper, "alpha", 0f, 1f).apply {
            duration = FADE_DURATION
            startDelay = ANIMATION_DURATION / 2
        }

        // 5. Simply fade in other components (no sliding)
        listOf(tvPrice, tvHelper, btnDelete).forEach { view ->
            view.alpha = 0f
            view.visibility = View.VISIBLE
        }

        val fadeInOthers = listOf(tvPrice, tvHelper, btnDelete).map { view ->
            ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
                duration = FADE_DURATION
                startDelay = ANIMATION_DURATION / 2
            }
        }

        morphSet.apply {
            playTogether(
                shrinkWidth,
                moveButton,
                fadeOutButton,
                fadeInStepper,
                *fadeInOthers.toTypedArray()
            )

            doOnEnd {
                button.visibility = View.GONE
                button.alpha = 1f
                button.translationX = 0f
                button.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT

                currentState = CartState.FINALE
                isAnimating = false
                onStateChanged?.invoke(currentState)
                onComplete?.invoke()
            }
        }

        morphSet.start()
    }

    /**
     * Morph stepper back to button (Finale → Default)
     * Subtle animation: button expands from stepper position
     */
    fun animateToDefaultState(onComplete: (() -> Unit)? = null) {
        if (currentState == CartState.DEFAULT || isAnimating) return
        isAnimating = true

        val button = binding.btnAddToCart
        val stepper = binding.sbQuantity
        val tvPrice = binding.tvPrice
        val tvHelper = binding.tvHelperTitle
        val btnDelete = binding.btnDelete

        val stepperWidth = stepper.width.toFloat()
        val buttonEndMargin = context.resources.displayMetrics.density * 16
        val finalButtonWidth = (parent as View).width - binding.ctaCart.width - buttonEndMargin * 2

        // 1. Fade out other components first
        val fadeOutOthers = listOf(tvPrice, tvHelper, btnDelete).map { view ->
            ObjectAnimator.ofFloat(view, "alpha", 1f, 0f).apply {
                duration = FADE_DURATION
            }
        }

        val fadeOutStepper = ObjectAnimator.ofFloat(stepper, "alpha", 1f, 0f).apply {
            duration = FADE_DURATION
        }

        val fadeOutSet = AnimatorSet().apply {
            playTogether(fadeOutStepper, *fadeOutOthers.toTypedArray())
        }

        fadeOutSet.doOnEnd {
            // Hide components
            listOf(tvPrice, tvHelper, btnDelete, stepper).forEach { it.visibility = View.GONE }

            // Prepare button at stepper position
            val translateX = finalButtonWidth - stepperWidth
            button.translationX = translateX
            button.layoutParams.width = stepperWidth.toInt()
            button.alpha = 0f
            button.visibility = View.VISIBLE
            button.requestLayout()

            post {
                val morphBackSet = AnimatorSet()

                // Expand button width
                val expandWidth = ValueAnimator.ofFloat(stepperWidth, finalButtonWidth).apply {
                    duration = ANIMATION_DURATION
                    interpolator = DecelerateInterpolator()
                    addUpdateListener { animator ->
                        val value = animator.animatedValue as Float
                        button.layoutParams.width = value.toInt()
                        button.requestLayout()
                    }
                }

                // Move button back to original position
                val moveButton = ObjectAnimator.ofFloat(button, "translationX", translateX, 0f).apply {
                    duration = ANIMATION_DURATION
                    interpolator = DecelerateInterpolator()
                }

                // Fade in button content
                val fadeInButton = ObjectAnimator.ofFloat(button, "alpha", 0f, 1f).apply {
                    duration = FADE_DURATION
                    startDelay = ANIMATION_DURATION / 3
                }

                morphBackSet.apply {
                    playTogether(expandWidth, moveButton, fadeInButton)

                    doOnEnd {
                        button.translationX = 0f
                        button.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                        button.requestLayout()

                        stepper.alpha = 1f

                        currentState = CartState.DEFAULT
                        isAnimating = false
                        onStateChanged?.invoke(currentState)
                        onComplete?.invoke()
                    }
                }

                morphBackSet.start()
            }
        }

        fadeOutSet.start()
    }

    /**
     * Set state without animation (useful for RecyclerView)
     */
    fun setStateImmediately(state: CartState) {
        if (isAnimating) return

        when (state) {
            CartState.DEFAULT -> {

                binding.btnAddToCart.apply {
                    binding.btnAddToCart.visibility = View.VISIBLE
                    this.animate()
                        .alpha(1f)
                        .setDuration(250L)
                        .setInterpolator(AccelerateInterpolator())
                        .start()
                }
                binding.tvHelperTitle.visibility = View.GONE
                binding.tvPrice.visibility = View.GONE
                binding.btnDelete.visibility = View.GONE
                binding.sbQuantity.apply {
                    visibility = View.GONE
                    alpha = 1f
                }
                currentState = CartState.DEFAULT
            }
            CartState.FINALE -> {
                binding.btnAddToCart.visibility = View.INVISIBLE
                binding.btnAddToCart.alpha = 0f

                binding.tvHelperTitle.visibility = View.VISIBLE
                binding.tvPrice.visibility = View.VISIBLE
                binding.btnDelete.visibility = View.VISIBLE
                binding.sbQuantity.visibility = View.VISIBLE
                currentState = CartState.FINALE
            }
        }
        onStateChanged?.invoke(currentState)
    }

    /**
     * Get current state
     */
    fun getCurrentState(): CartState = currentState

    /**
     * Set price text
     */
    fun setPrice(price: String) {
        binding.tvPrice.text = price
    }

    /**
     * Set helper title text
     */
    fun setHelperTitle(title: String) {
        binding.tvHelperTitle.text = title
    }

    /**
     * Set button text
     */
    fun setButtonText(text: String) {
        binding.btnAddToCart.text = text
    }

    companion object {
        private const val ANIMATION_DURATION = 300L
        private const val FADE_DURATION = 200L
    }
}