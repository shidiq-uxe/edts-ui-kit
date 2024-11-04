package id.co.edtslib.uikit.progressbar

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.interpolator.EaseInterpolator

class GradientCircularProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private var indicatorRect = RectF()
    private var animator: ValueAnimator? = null

    var shouldAnimate: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    var indicatorProgress: Float = 0f
        set(value) {
            if (shouldAnimate) {
                animateProgressChange(value) { animation ->
                    field = (animation.animatedValue as Float).coerceIn(0f, maxIndicatorProgress)
                    invalidate()
                }
            } else {
                field = value.coerceIn(0f, maxIndicatorProgress)
                invalidate()
            }
        }

    var maxIndicatorProgress: Float = 100f
        set(value) {
            field = value.coerceAtLeast(1f)
            invalidate()
        }

    @ColorInt
    var indicatorStartColor: Int = context.color(R.color.primary_20)
        set(value) {
            field = value
            invalidate()
        }

    @ColorInt
    var indicatorEndColor: Int = context.color(R.color.primary_40)
        set(value) {
            field = value
            invalidate()
        }

    @ColorInt
    var trackColor: Int = context.color(R.color.black_20)
        set(value) {
            field = value
            invalidate()
        }

    var trackThickness: Float = 20f
        set(value) {
            field = value
            invalidate()
        }

    var trackCornerRadius: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.GradientCircularProgressBar, 0, 0).apply {
            try {
                indicatorProgress = getFloat(R.styleable.GradientCircularProgressBar_indicatorProgress, indicatorProgress)
                maxIndicatorProgress = getFloat(R.styleable.GradientCircularProgressBar_maxIndicatorProgress, maxIndicatorProgress)
                indicatorStartColor = getColor(R.styleable.GradientCircularProgressBar_indicatorStartColor, indicatorStartColor)
                indicatorEndColor = getColor(R.styleable.GradientCircularProgressBar_indicatorEndColor, indicatorEndColor)
                trackColor = getColor(R.styleable.GradientCircularProgressBar_trackColor, trackColor)
                trackThickness = getDimension(R.styleable.GradientCircularProgressBar_trackThickness, trackThickness)
                shouldAnimate = getBoolean(R.styleable.GradientCircularProgressBar_shouldAnimate, shouldAnimate)
            } finally {
                recycle()
            }
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = width.coerceAtMost(height).toFloat()
        val padding = trackThickness / 2f
        indicatorRect.set(padding, padding, size - padding, size - padding)

        // Draw track circle
        trackPaint.color = trackColor
        trackPaint.strokeWidth = trackThickness
        canvas.drawArc(indicatorRect, 0f, 360f, false, trackPaint)

        // Draw gradient progress indicator
        val sweepAngle = (indicatorProgress / maxIndicatorProgress) * 360f
        paint.strokeWidth = trackThickness
        paint.shader = LinearGradient(
            0f, 0f, indicatorRect.width(), indicatorRect.height(),
            indicatorStartColor, indicatorEndColor, Shader.TileMode.CLAMP
        )
        canvas.drawArc(indicatorRect, -90f, sweepAngle, false, paint)
    }

    private fun animateProgressChange(targetProgress: Float, onUpdate: (animation: ValueAnimator) -> Unit) {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(indicatorProgress, targetProgress).apply {
            duration = 1000
            interpolator = EaseInterpolator.EaseInOutQubicInterpolator
            addUpdateListener { animation ->
                onUpdate.invoke(animation)
            }
            start()
        }
    }
}
