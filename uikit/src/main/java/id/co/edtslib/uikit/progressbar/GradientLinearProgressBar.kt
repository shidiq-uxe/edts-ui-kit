package id.co.edtslib.uikit.progressbar

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
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

class GradientLinearProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var delegate: GradientProgressBarDelegate? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        maskFilter = BlurMaskFilter(4f, BlurMaskFilter.Blur.NORMAL)
    }

    private var indicatorRect = RectF()
    private var trackRect = RectF()
    private var animator: ValueAnimator? = null
    private var gradientShader: LinearGradient? = null

    var shouldAnimate: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    var indicatorProgress: Float = 0f
        set(value) {
            if (shouldAnimate) {
                animateProgressChange(value) { animation ->
                    val animatedValue = (animation.animatedValue as Float).coerceIn(0f, maxIndicatorProgress)
                    delegate?.onAnimationUpdateListener(this, animatedValue, value.coerceIn(0f, maxIndicatorProgress))

                    field = animatedValue
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
            updateGradientShader()
            invalidate()
        }

    @ColorInt
    var indicatorEndColor: Int = context.color(R.color.primary_40)
        set(value) {
            field = value
            updateGradientShader()
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
            requestLayout()
        }

    var trackCornerRadius: Float = 10f
        set(value) {
            field = value
            invalidate()
        }

    var trackPadding: Float = 0f
        set(value) {
            field = value
            requestLayout()
        }

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.GradientLinearProgressBar, 0, 0).apply {
            try {
                indicatorProgress = getFloat(R.styleable.GradientLinearProgressBar_indicatorProgress, indicatorProgress)
                maxIndicatorProgress = getFloat(R.styleable.GradientLinearProgressBar_maxIndicatorProgress, maxIndicatorProgress)
                indicatorStartColor = getColor(R.styleable.GradientLinearProgressBar_indicatorStartColor, indicatorStartColor)
                indicatorEndColor = getColor(R.styleable.GradientLinearProgressBar_indicatorEndColor, indicatorEndColor)
                trackColor = getColor(R.styleable.GradientLinearProgressBar_trackColor, trackColor)
                trackThickness = getDimension(R.styleable.GradientLinearProgressBar_trackThickness, trackThickness)
                trackCornerRadius = getDimension(R.styleable.GradientLinearProgressBar_trackCornerRadius, trackCornerRadius)
                trackPadding = getDimension(R.styleable.GradientLinearProgressBar_trackPadding, trackPadding)
                shouldAnimate = getBoolean(R.styleable.GradientLinearProgressBar_shouldAnimate, shouldAnimate)
            } finally {
                recycle()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = (trackThickness + trackPadding * 2).toInt()
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Define the track rectangle
        trackRect.set(
            paddingLeft + trackPadding,
            paddingTop + trackPadding,
            width - paddingRight - trackPadding,
            paddingTop + trackPadding + trackThickness
        )

        // Draw the track
        trackPaint.color = trackColor
        canvas.drawRoundRect(trackRect, trackCornerRadius, trackCornerRadius, trackPaint)

        // Draw inner shadow over the track to create 3D effect
        drawInnerShadow(canvas)

        updateGradientShader()

        // Draw the indicator
        val indicatorWidth = (trackRect.width() - trackPadding * 2) * (indicatorProgress / maxIndicatorProgress)
        indicatorRect.set(trackRect.left, trackRect.top, trackRect.left + indicatorWidth, trackRect.bottom)
        paint.shader = gradientShader  // Apply cached gradient
        canvas.drawRoundRect(indicatorRect, trackCornerRadius, trackCornerRadius, paint)
    }

    private fun drawInnerShadow(canvas: Canvas) {
        shadowPaint.shader = LinearGradient(
            0f, trackRect.top, 0f, trackRect.top + trackThickness / 2f,
            Color.parseColor("#0A000000"), Color.TRANSPARENT, Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(trackRect, trackCornerRadius, trackCornerRadius, shadowPaint)

        shadowPaint.shader = LinearGradient(
            0f, trackRect.bottom - trackThickness / 2f, 0f, trackRect.bottom,
            Color.TRANSPARENT, Color.parseColor("#0A000000"), Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(trackRect, trackCornerRadius, trackCornerRadius, shadowPaint)
    }

    private fun updateGradientShader() {
        gradientShader = LinearGradient(
            0f, 0f, width.toFloat(), 0f,
            indicatorStartColor, indicatorEndColor,
            Shader.TileMode.CLAMP
        )
    }

    private fun animateProgressChange(targetProgress: Float, onUpdateListener: (ValueAnimator) -> Unit) {
        if (animator == null) {

            animator = ValueAnimator().apply {
                duration = 800
                interpolator = EaseInterpolator.EaseInOutQubicInterpolator
                addUpdateListener { animation ->
                    onUpdateListener.invoke(animation)
                }
            }
        }

        animator?.apply {
            setFloatValues(indicatorProgress, targetProgress)
            start()
        }
    }
}
