package id.co.edtslib.uikit.stepper

import android.animation.ValueAnimator
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.PopupWindow
import androidx.core.animation.doOnEnd
import com.google.android.material.card.MaterialCardView
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.databinding.ViewStepperDragFeedbackBinding
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.inflater

/** A non-touchable window keeps feedback outside the finger without changing row measurement. */
internal class StepperDragFeedback(
    private val owner: View,
    private val container: MaterialCardView,
) {

    private val binding = ViewStepperDragFeedbackBinding.inflate(owner.context.inflater)
    private val popup = PopupWindow(binding.root, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        isTouchable = false
        isFocusable = false
        isOutsideTouchable = false
        isClippingEnabled = true
        animationStyle = 0
        // Keep the hint inside the same dialog/window layer as its anchor.
        inputMethodMode = PopupWindow.INPUT_METHOD_NOT_NEEDED
    }
    private var lastFeedback: StepperDragRepeatTouch.Feedback? = null
    private var originalStrokeColor: Int? = null
    private var animator: ValueAnimator? = null
    private var isHiding = false
    private var lastDirection = 1
    private val chevronAnimator = StepperDragChevronAnimator(
        owner = owner,
        indicator = binding.chevronIndicator,
        chevrons = listOf(binding.ivChevronFirst, binding.ivChevronSecond, binding.ivChevronThird),
        areAnimationsEnabled = ::areAnimationsEnabled,
    )

    fun show(feedback: StepperDragRepeatTouch.Feedback) {
        if (!owner.isAttachedToWindow || !owner.isShown) return
        if (originalStrokeColor == null) originalStrokeColor = container.strokeColor
        container.strokeColor = owner.context.color(R.color.primary_30)
        if (lastFeedback == null) lastDirection = if (feedback.allowsIncrease) 1 else -1
        if (feedback.direction != 0) lastDirection = feedback.direction
        if (lastFeedback != feedback) {
            binding.tvDirection.setText(when {
                feedback.isBlocked -> R.string.stepper_drag_limit
                lastDirection > 0 -> R.string.stepper_drag_increasing
                else -> R.string.stepper_drag_decreasing
            })
            lastFeedback = feedback
        }
        if (!popup.isShowing) {
            // Reserve the widest label once, so direction/cap changes don't move the bubble.
            val textWidth = listOf(
                R.string.stepper_drag_increasing, R.string.stepper_drag_decreasing, R.string.stepper_drag_limit,
            ).maxOf { binding.tvDirection.paint.measureText(owner.context.getString(it)) }
            binding.tvDirection.width = kotlin.math.ceil(textWidth).toInt()
            binding.root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            popup.width = binding.root.measuredWidth
            popup.height = binding.root.measuredHeight
            binding.root.alpha = 0f
            popup.showAsDropDown(owner, (owner.width - popup.width) / 2, -owner.height - popup.height - 8.dp.toInt())
            animateAlpha(1f)
        } else if (isHiding) {
            animateAlpha(1f)
        }
        chevronAnimator.show(
            nextDirection = lastDirection,
            speedLevel = feedback.speedLevel,
            isRepeating = feedback.direction != 0 && !feedback.isBlocked,
        )
    }

    fun hide(immediately: Boolean = false) {
        // Stop the highlight now, even while the bubble is still fading out.
        chevronAnimator.stop()
        originalStrokeColor?.let { container.strokeColor = it }
        originalStrokeColor = null
        lastFeedback = null
        if (immediately) {
            animator?.cancel()
            animator = null
            isHiding = false
            popup.dismiss()
        } else if (popup.isShowing && !isHiding) {
            animateAlpha(0f)
        }
    }

    private fun animateAlpha(target: Float) {
        // Cancel before changing isHiding: a cancelled fade-out must not dismiss a new gesture.
        isHiding = false
        animator?.cancel()
        isHiding = target == 0f
        if (!areAnimationsEnabled()) {
            binding.root.alpha = target
            if (isHiding) popup.dismiss()
            return
        }
        animator = ValueAnimator.ofFloat(binding.root.alpha, target).apply {
            duration = FADE_DURATION_MS
            interpolator = DecelerateInterpolator()
            addUpdateListener { binding.root.alpha = it.animatedValue as Float }
            doOnEnd { if (isHiding && target == 0f) popup.dismiss() }
            start()
        }
    }

    private fun areAnimationsEnabled(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ValueAnimator.areAnimatorsEnabled()
        else Settings.Global.getFloat(owner.context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f

    companion object {
        private const val FADE_DURATION_MS = 150L
    }
}
