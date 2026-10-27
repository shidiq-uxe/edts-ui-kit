package id.co.edtslib.edtsuikit.tmh

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import id.co.edtslib.edtsuikit.databinding.ItemTmhRedemptionProductBinding
import id.co.edtslib.uikit.utils.areSystemAnimationsEnabled
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.R as UIKitR

/** Per-row presentation state. Model quantities and the pending-cart state update immediately. */
internal class TmhProductSelectionAnimator(private val binding: ItemTmhRedemptionProductBinding) {

    private var productId: String? = null
    private var isSelected = false
    private var generation = 0
    private var borderAnimator: ValueAnimator? = null
    private var cornerAnimator: ValueAnimator? = null
    private val attachListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(view: View) = Unit
        override fun onViewDetachedFromWindow(view: View) { finishImmediately() }
    }

    init {
        binding.root.addOnAttachStateChangeListener(attachListener)
    }

    fun bind(id: String, selected: Boolean) {
        if (id != productId) {
            binding.stepper.cancelDragRepeat()
            productId = id
            isSelected = selected
            finishImmediately()
            return
        }
        if (selected == isSelected) return
        isSelected = selected
        if (!binding.root.isAttachedToWindow || !areAnimationsEnabled()) {
            finishImmediately()
            return
        }
        cancelAnimations()
        val token = generation
        val borderColor = binding.root.context.color(if (selected) UIKitR.color.primary_30 else UIKitR.color.black_30)
        borderAnimator = ValueAnimator.ofArgb(binding.root.strokeColor, borderColor).apply {
            duration = BORDER_DURATION_MS
            addUpdateListener { if (token == generation) binding.root.strokeColor = it.animatedValue as Int }
            start()
        }
        val corner = binding.ivSelected
        val offset = -corner.layoutParams.width.toFloat()
        if (!corner.isVisible) {
            corner.alpha = 0f
            corner.translationX = offset
            corner.translationY = offset
        }
        corner.isVisible = true
        val initialAlpha = corner.alpha
        val initialX = corner.translationX
        val initialY = corner.translationY
        val finalAlpha = if (selected) 1f else 0f
        val finalOffset = if (selected) 0f else offset
        cornerAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = if (selected) ENTER_DURATION_MS else EXIT_DURATION_MS
            interpolator = if (selected) DecelerateInterpolator() else AccelerateInterpolator()
            addUpdateListener {
                if (token == generation) {
                    val progress = it.animatedValue as Float
                    corner.alpha = initialAlpha + (finalAlpha - initialAlpha) * progress
                    corner.translationX = initialX + (finalOffset - initialX) * progress
                    corner.translationY = initialY + (finalOffset - initialY) * progress
                }
            }
            doOnEnd {
                if (token == generation && !isSelected) resetCorner()
            }
            start()
        }
    }

    fun release() {
        finishImmediately()
        binding.stepper.cancelDragRepeat()
        binding.root.removeOnAttachStateChangeListener(attachListener)
    }

    private fun finishImmediately() {
        cancelAnimations()
        binding.root.strokeColor = binding.root.context.color(
            if (isSelected) UIKitR.color.primary_30 else UIKitR.color.black_30
        )
        resetCorner()
    }

    private fun resetCorner() {
        val corner = binding.ivSelected
        corner.isVisible = isSelected
        corner.alpha = 1f
        corner.translationX = 0f
        corner.translationY = 0f
    }

    private fun cancelAnimations() {
        // Invalidate end callbacks before cancel(), which also dispatches animation end.
        generation++
        borderAnimator?.cancel()
        cornerAnimator?.cancel()
        borderAnimator = null
        cornerAnimator = null
    }

    private fun areAnimationsEnabled(): Boolean = binding.root.context.areSystemAnimationsEnabled()

    companion object {
        private const val BORDER_DURATION_MS = 250L
        private const val ENTER_DURATION_MS = 250L
        private const val EXIT_DURATION_MS = 200L
    }
}
