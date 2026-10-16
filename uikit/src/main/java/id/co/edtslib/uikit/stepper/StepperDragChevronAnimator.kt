package id.co.edtslib.uikit.stepper

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.dp
import kotlin.math.abs

/** Presentation only: speed levels come from the existing distance-based repeat controller. */
internal class StepperDragChevronAnimator(
    private val owner: View,
    private val indicator: View,
    private val chevrons: List<ImageView>,
    private val areAnimationsEnabled: () -> Boolean,
) {

    private var direction = 1
    private var count = 1
    private var cycleAnimator: ValueAnimator? = null

    init {
        indicator.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(view: View) = Unit
            override fun onViewDetachedFromWindow(view: View) { stop() }
        })
    }

    fun show(nextDirection: Int, speedLevel: Int, isRepeating: Boolean) {
        val nextCount = speedLevel.coerceIn(1, 3)
        val hasDirectionChanged = direction != nextDirection
        val hasCountChanged = count != nextCount
        val phase = if (hasDirectionChanged) 0f else cycleAnimator?.animatedValue as? Float ?: 0f
        direction = nextDirection
        count = nextCount
        if (hasDirectionChanged || hasCountChanged) {
            chevrons.forEachIndexed { index, chevron ->
                chevron.visibility = if (index < count) View.VISIBLE else View.INVISIBLE
                chevron.setImageResource(if (direction > 0) R.drawable.ic_chevron_up_16 else R.drawable.ic_chevron_down_16)
                chevron.translationY = (index - (count - 1) / 2f) * CHEVRON_SPACING_DP.dp
            }
        }
        if (!isRepeating || !areAnimationsEnabled() || !indicator.isAttachedToWindow) {
            stop()
            return
        }
        if (cycleAnimator?.isRunning == true && !hasDirectionChanged && !hasCountChanged) return
        stop()
        cycleAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = when (count) {
                1 -> SLOW_CYCLE_MS
                2 -> MEDIUM_CYCLE_MS
                else -> FAST_CYCLE_MS
            }
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                if (!owner.isShown || !indicator.isShown || !areAnimationsEnabled()) stop()
                else renderPhase(it.animatedValue as Float)
            }
            start()
            currentPlayTime = (phase * duration).toLong()
        }
    }

    fun stop() {
        cycleAnimator?.removeAllUpdateListeners()
        cycleAnimator?.cancel()
        cycleAnimator = null
        chevrons.forEach { it.alpha = 1f }
    }

    private fun renderPhase(phase: Float) {
        // Travel from outside one end to outside the other, including a quiet cycle boundary.
        val position = phase * (count + 1) - 1f
        chevrons.forEachIndexed { index, chevron ->
            if (index >= count) return@forEachIndexed
            val travelIndex = if (direction > 0) count - 1 - index else index
            val highlight = (1f - abs(position - travelIndex)).coerceIn(0f, 1f)
            chevron.alpha = BASE_ALPHA + (1f - BASE_ALPHA) * highlight
        }
    }

    companion object {
        private const val CHEVRON_SPACING_DP = 7
        private const val BASE_ALPHA = 0.35f
        // Initial tuning values, independent of the quantity-repeat intervals.
        private const val SLOW_CYCLE_MS = 900L
        private const val MEDIUM_CYCLE_MS = 600L
        private const val FAST_CYCLE_MS = 350L
    }
}
