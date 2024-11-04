package id.co.edtslib.uikit.progressindicator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.px

class DoubleArcProgressIndicator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        color = progressColor
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    var progressColor = context.color(R.color.primary_30)
        set(value) {
            field = value
            paint.color = value
            invalidate()
        }

    var innerArcPadding: Float = 20f
        set(value) {
            field = value
            requestLayout()
        }

    var progressThickness: Float = 10f
        set(value) {
            field = value
            paint.strokeWidth = value
            invalidate()
        }

    var progressSize: Float = 50.px
        set(value) {
            field = value
            requestLayout()
        }

    var rotateSpeed: Float = 6f
        set(value) {
            field = value
        }

    var arcSweepAngle: Float = 270f
        set(value) {
            field = value
            invalidate()
        }

    var isRunning: Boolean = false
        private set

    private val outerRect = RectF()
    private val innerRect = RectF()

    private var startAngleOuter = 0f
    private var startAngleInner = 180f

    private var animateRunnable: Runnable? = null

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.DoubleArcProgressIndicator, 0, 0).apply {
            try {
                innerArcPadding = getDimension(R.styleable.DoubleArcProgressIndicator_innerArcPadding, innerArcPadding)
                progressThickness = getDimension(R.styleable.DoubleArcProgressIndicator_progressThickness, progressThickness)
                progressSize = getDimension(R.styleable.DoubleArcProgressIndicator_progressSize, progressSize)
                rotateSpeed = getFloat(R.styleable.DoubleArcProgressIndicator_rotateSpeed, rotateSpeed)
                arcSweepAngle = getFloat(R.styleable.DoubleArcProgressIndicator_arcSweepAngle, arcSweepAngle)
                progressColor = getColor(R.styleable.DoubleArcProgressIndicator_progressColor, progressColor)
            } finally {
                recycle()
            }
        }

        paint.color = progressColor
        paint.strokeWidth = progressThickness

        initAnimation()

        startAnimation()
    }

    private fun initAnimation() {
        animateRunnable = object : Runnable {
            override fun run() {
                isRunning = true
                startAngleOuter = (startAngleOuter + rotateSpeed) % 360
                startAngleInner = (startAngleInner - rotateSpeed) % 360
                invalidate()
                // 60 Frame Animation
                postDelayed(this, 16)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = progressSize.toInt()
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateRectBounds()
    }

    private fun updateRectBounds() {
        outerRect.set(innerArcPadding, innerArcPadding, width - innerArcPadding, height - innerArcPadding)
        innerRect.set(innerArcPadding * 2, innerArcPadding * 2, width - innerArcPadding * 2, height - innerArcPadding * 2)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawArc(outerRect, startAngleOuter, arcSweepAngle, false, paint)
        canvas.drawArc(innerRect, startAngleInner, arcSweepAngle, false, paint)
    }

    fun startAnimation() {
        post(animateRunnable)
    }

    fun stopAnimation() {
        removeCallbacks(animateRunnable)
        isRunning = false
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == VISIBLE) {
            stopAnimation()
            startAnimation()
        } else {
            stopAnimation()
        }
    }

    override fun onDetachedFromWindow() {
        stopAnimation()

        super.onDetachedFromWindow()
    }
}





