package id.co.edtslib.uikit.stepper

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.view.ViewGroup
import android.view.animation.PathInterpolator
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.core.graphics.withClip

/** Draws only the rolling numerals; the real view keeps the latest value and accessibility node. */
internal class StepperCountAnimator {

    private var animator: ValueAnimator? = null
    private var host: ViewGroup? = null
    private var textView: TextView? = null
    private var overlay: CountDrawable? = null
    private var restingAlpha = 1f

    fun start(view: TextView, previous: Int, next: Int) {
        cancel()
        val parent = view.parent as? ViewGroup ?: return
        if (view.width == 0 || view.height == 0 || view.alpha != 1f) return
        val drawable = CountDrawable(view, previous.toString(), next.toString(), if (next > previous) -1 else 1)
        drawable.setBounds(view.left, view.top, view.right, view.bottom)
        host = parent
        textView = view
        overlay = drawable
        restingAlpha = view.alpha
        parent.overlay.add(drawable)
        view.alpha = 0f
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = DURATION_MS
            interpolator = PathInterpolator(0.2f, 0f, 0f, 1f)
            addUpdateListener {
                drawable.progress = it.animatedValue as Float
                drawable.invalidateSelf()
            }
            doOnEnd { clearOverlay() }
            start()
        }
    }

    fun cancel() {
        animator?.removeAllUpdateListeners()
        animator?.cancel()
        animator = null
        clearOverlay()
    }

    private fun clearOverlay() {
        overlay?.let { host?.overlay?.remove(it) }
        textView?.alpha = restingAlpha
        host = null
        textView = null
        overlay = null
        animator = null
    }

    private class CountDrawable(
        view: TextView,
        private val previous: String,
        private val next: String,
        private val direction: Int
    ) : Drawable() {

        var progress = 0f

        private val paint = TextPaint(view.paint).apply { color = view.currentTextColor }
        private val baseline = view.baseline.toFloat()
        private val travel = view.lineHeight.toFloat()

        override fun draw(canvas: Canvas) {
            canvas.withClip(bounds) {
                translate(bounds.left.toFloat(), bounds.top.toFloat())
                paint.alpha = ((1f - progress) * 255).toInt()
                drawText(
                    previous,
                    (bounds.width() - paint.measureText(previous)) / 2f,
                    baseline + direction * travel * progress,
                    paint
                )
                paint.alpha = (progress * 255).toInt()
                drawText(
                    next,
                    (bounds.width() - paint.measureText(next)) / 2f,
                    baseline - direction * travel * (1f - progress),
                    paint
                )
            }
        }

        override fun setAlpha(alpha: Int) = Unit
        override fun setColorFilter(colorFilter: ColorFilter?) = Unit
        @Suppress("OVERRIDE_DEPRECATION")
        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    }

    companion object {
        private const val DURATION_MS = 160L
    }
}
