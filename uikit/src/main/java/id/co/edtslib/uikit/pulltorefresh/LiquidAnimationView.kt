package id.co.edtslib.uikit.pulltorefresh

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.DrawableCompat
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.drawable
import id.co.edtslib.uikit.utils.px

class LiquidAnimationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    internal enum class AnimatorStatus {
        PULL_DOWN, DRAG_DOWN, RELEASE_DRAG, SPRING_UP,
        POP_BALL, OUTER_CIRCLE, REFRESHING, DONE, STOP;

        override fun toString() = when (this) {
            PULL_DOWN -> "on View Pulled Down"
            DRAG_DOWN -> "on View Dragged Down"
            RELEASE_DRAG -> "on Drag Released"
            SPRING_UP -> "on View Spring Up"
            POP_BALL -> "on Pop Ball Showing"
            OUTER_CIRCLE -> "on Outer Circle Showing"
            REFRESHING -> "on Refreshing"
            DONE -> "on Animation Done"
            STOP -> "on Animation Stopped"
        }
    }

    private var animatorStatus = AnimatorStatus.PULL_DOWN

    var iconDrawable: Drawable? = context.drawable(R.drawable.ic_placeholder_medium_24).apply {
        DrawableCompat.setTint(this, context.color(R.color.primary_30))
        mutate()
    }

    private var pullHeight: Int = 0
    private var pullDelta: Int = 0
    private var widthOffset: Float = 0.toFloat()

    private var backPaint: Paint? = null
    private var ballPaint: Paint? = null
    private var outPaint: Paint? = null
    private var path: Path? = null

    private var radius: Int = 0
    private var localWidth: Int = 0
    private var localHeight: Int = 0

    private var refreshStart = 90
    private var refreshStop = 90
    private var targetDegree = 270
    private var isStart = true
    private var isRefreshing = true

    private var lastHeight: Int = 0

    private val releaseHeight: Int get() = (spriDeta * (1 - releaseRatio)).toInt()
    private val springDelta: Int
        get() = (pullDelta * springRatio).toInt()

    private var start1: Long = 0
    private var stop: Long = 0
    private var spriDeta: Int = 0

    private val releaseRatio: Float get() {
        if (System.currentTimeMillis() >= stop) {
            springUp()
            return 1f
        }
        val ratio = (System.currentTimeMillis() - start1) / REL_DRAG_DUR.toFloat()
        return ratio.coerceAtMost(1f)
    }

    private var sprStart: Long = 0
    private var sprStop: Long = 0

    private val springRatio: Float get() {
        if (System.currentTimeMillis() >= sprStop) {
            popBall()
            return 1f
        }

        val ratio = (System.currentTimeMillis() - sprStart) / SPRING_DUR.toFloat()
        return 1f.coerceAtMost(ratio)
    }

    private var popStart: Long = 0
    private var popStop: Long = 0

    private val popRatio: Float
        get() {
            if (System.currentTimeMillis() >= popStop) {
                startOutCir()
                return 1f
            }

            val ratio = (System.currentTimeMillis() - popStart) / POP_BALL_DUR.toFloat()
            return ratio.coerceAtMost(1f)
        }
    private var outStart: Long = 0
    private var outStop: Long = 0

    private val outRatio: Float
        get() {
            if (System.currentTimeMillis() >= outStop) {
                animatorStatus = AnimatorStatus.REFRESHING
                isRefreshing = true
                return 1f
            }
            val ratio = (System.currentTimeMillis() - outStart) / OUTER_DUR.toFloat()
            return ratio.coerceAtMost(1f)
        }
    private var doneStart: Long = 0
    private var doneStop: Long = 0

    private val doneRatio: Float get() {
        if (System.currentTimeMillis() >= doneStop) {
            animatorStatus = AnimatorStatus.STOP
            if (onAnimationDoneListener != null) {
                onAnimationDoneListener!!.viewAniDone()
            }

            return 1f
        }

        val ratio = (System.currentTimeMillis() - doneStart) / DONE_DUR.toFloat()
        return ratio.coerceAtMost(1f)
    }

    private val ballYProperty = @RequiresApi(Build.VERSION_CODES.N)
    object : FloatPropertyCompat<LiquidAnimationView>( "ballY") {
        override fun setValue(view: LiquidAnimationView, value: Float) {
            view.ballInnerY = value.toInt()// Update the Y position of the ball
            view.invalidate() // Trigger a redraw of the view
        }

        override fun getValue(view: LiquidAnimationView): Float {
            return view.ballInnerY.toFloat()
        }
    }

    private lateinit var ballYSpringAnim: SpringAnimation

    private var onAnimationDoneListener: OnAnimationDoneListener? = null

    var ballInnerY = 0

    init {
        initView(context)
        initSpringAnimation()
    }

    private fun initView(context: Context) {
        pullHeight = 100.px.toInt()
        pullDelta = 50.px.toInt()
        widthOffset = 0.5f

        val defaultBallColor = Color.WHITE
        val defaultGroundColor = context.color(R.color.primary_30)

        // Initialize Paint instances
        backPaint = createPaint(defaultGroundColor, Paint.Style.FILL)
        ballPaint = createPaint(defaultBallColor, Paint.Style.FILL)
        outPaint = createPaint(defaultBallColor, Paint.Style.STROKE, strokeWidth = 5f)

        path = Path()
    }

    // Create a Paint instance with common properties
    private fun createPaint(
        color: Int,
        style: Paint.Style,
        isAntiAlias: Boolean = true,
        strokeWidth: Float = 0f
    ) = Paint().apply {
            this.isAntiAlias = isAntiAlias
            this.style = style
            this.color = color
            if (strokeWidth > 0f) {
                this.strokeWidth = strokeWidth
            }
    }

    // Used for Ball animation during popping phase until refreshing phase
    private fun initSpringAnimation() {
        ballYSpringAnim = SpringAnimation(this, ballYProperty).apply {
            // Apply Spring Force with final Position on the Y Center of the screen
            spring = SpringForce((pullHeight / 2).toFloat()).apply {
                dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                stiffness = SpringForce.STIFFNESS_LOW
            }

            // Apply reversed effect spring with negative value
            setStartVelocity(-900f)
        }
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        var tempHeightMeasureSpec = heightMeasureSpec
        val height = MeasureSpec.getSize(tempHeightMeasureSpec)
        if (height > pullDelta + pullHeight) {
            tempHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                pullDelta + pullHeight,
                MeasureSpec.getMode(tempHeightMeasureSpec)
            )
        }
        super.onMeasure(widthMeasureSpec, tempHeightMeasureSpec)
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            radius = height / 6
            localWidth = width
            localHeight = height
            when {
                localHeight < pullHeight -> animatorStatus = AnimatorStatus.PULL_DOWN
            }
            when {
                animatorStatus == AnimatorStatus.PULL_DOWN && localHeight >= pullHeight -> animatorStatus =
                    AnimatorStatus.DRAG_DOWN
            }

        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        when (animatorStatus) {
            AnimatorStatus.PULL_DOWN -> canvas.drawRect(
                0f, 0f, localWidth.toFloat(),
                localHeight.toFloat(), backPaint!!
            )
            AnimatorStatus.RELEASE_DRAG, AnimatorStatus.DRAG_DOWN -> {
                drawDrag(canvas)
            }
            AnimatorStatus.SPRING_UP -> {
                drawSpring(canvas, springDelta)
                invalidate()
            }
            AnimatorStatus.POP_BALL -> {
                drawPopBall(canvas)
                invalidate()
            }
            AnimatorStatus.OUTER_CIRCLE -> {
                drawOutCir(canvas)
                drawVectorDrawable(canvas)
                startAlphaAnimation()
                invalidate()
            }
            AnimatorStatus.REFRESHING -> {
                drawRefreshing(canvas)
                drawVectorDrawable(canvas)
                invalidate()
            }
            AnimatorStatus.DONE -> {
                drawDone(canvas)
                stopAlphaAnimation()
                invalidate()
            }
            AnimatorStatus.STOP -> drawDone(canvas)
        }

        if (animatorStatus == AnimatorStatus.RELEASE_DRAG) {
            val params = layoutParams
            var height: Int
            do {
                height = releaseHeight
            } while (height == lastHeight && releaseRatio != 1f)
            lastHeight = height
            params.height = pullHeight + height
            requestLayout()
        }

    }

    private fun drawDrag(canvas: Canvas) {
        canvas.drawRect(0f, 0f, localWidth.toFloat(), pullHeight.toFloat(), backPaint!!)
        path!!.reset()
        path!!.moveTo(0f, pullHeight.toFloat())
        path!!.quadTo(
            widthOffset * localWidth, (pullHeight + (localHeight - pullHeight) * 2).toFloat(),
            localWidth.toFloat(), pullHeight.toFloat()
        )
        canvas.drawPath(path!!, backPaint!!)
    }

    private fun drawSpring(
        canvas: Canvas,
        springDelta: Int
    ) {
        path!!.reset()
        path!!.moveTo(0f, 0f)
        path!!.lineTo(0f, pullHeight.toFloat())
        path!!.quadTo(
            (localWidth / 2).toFloat(), (pullHeight - springDelta).toFloat(),
            localWidth.toFloat(), pullHeight.toFloat()
        )
        path!!.lineTo(localWidth.toFloat(), 0f)
        canvas.drawPath(path!!, backPaint!!)
        val curH = pullHeight - springDelta / 2
        if (curH > pullHeight - pullDelta / 2) {
            val leftX = (localWidth / 2 - 2 * radius + springRatio * radius).toInt()
            path!!.reset()
            path!!.moveTo(leftX.toFloat(), curH.toFloat())
            path!!.quadTo(
                (localWidth / 2).toFloat(), curH - radius.toFloat() * springRatio * 2f,
                (localWidth - leftX).toFloat(), curH.toFloat()
            )
            canvas.drawPath(path!!, ballPaint!!)
        } else {
            canvas.drawArc(
                RectF(
                    (localWidth / 2 - radius).toFloat(), (curH - radius).toFloat(),
                    (localWidth / 2 + radius).toFloat(), (curH + radius).toFloat()
                ),
                180f, 180f, true, ballPaint!!
            )
        }
    }

    private fun drawPopBall(canvas: Canvas) {
        path!!.reset()
        path!!.moveTo(0f, 0f)
        path!!.lineTo(0f, pullHeight.toFloat())
        path!!.quadTo(
            (localWidth / 2).toFloat(), (pullHeight - pullDelta).toFloat(),
            localWidth.toFloat(), pullHeight.toFloat()
        )
        path!!.lineTo(localWidth.toFloat(), 0f)
        canvas.drawPath(path!!, backPaint!!)

        val cirCentStart = pullHeight - pullDelta / 2
        ballInnerY = (cirCentStart - radius.toFloat() * 2f * popRatio).toInt()

        // Use ballInnerY for drawing
        val innerY = ballInnerY
        canvas.drawArc(
            RectF(
                (localWidth / 2 - radius).toFloat(),
                (innerY - radius).toFloat(),
                (localWidth / 2 + radius).toFloat(),
                (innerY + radius).toFloat()
            ),
            180f, 360f, true, ballPaint!!
        )

        if (popRatio < 1) {
            drawTail(canvas, ballInnerY, cirCentStart + 1, popRatio)
        } else {
            canvas.drawCircle(
                (localWidth / 2).toFloat(),
                ballInnerY.toFloat(),
                radius.toFloat(),
                ballPaint!!
            )
        }
    }

    private fun drawTail(
        canvas: Canvas,
        centerY: Int,
        bottom: Int,
        fraction: Float
    ) {
        val bezier1w = (localWidth / 2 + radius * 3 / 4 * (1 - fraction)).toInt()
        val start = PointF((localWidth / 2 + radius).toFloat(), centerY.toFloat())
        val bezier1 = PointF(bezier1w.toFloat(), bottom.toFloat())
        val bezier2 = PointF((bezier1w + radius / 2).toFloat(), bottom.toFloat())

        path!!.reset()
        path!!.moveTo(start.x, start.y)
        path!!.quadTo(
            bezier1.x, bezier1.y,
            bezier2.x, bezier2.y
        )
        path!!.lineTo(localWidth - bezier2.x, bezier2.y)
        path!!.quadTo(
            localWidth - bezier1.x, bezier1.y,
            localWidth - start.x, start.y
        )
        canvas.drawPath(path!!, ballPaint!!)
    }

    private fun drawOutCir(canvas: Canvas) {
        path!!.reset()
        path!!.moveTo(0f, 0f)
        path!!.lineTo(0f, pullHeight.toFloat())
        path!!.quadTo(
            (localWidth / 2).toFloat(), pullHeight - (1 - outRatio) * pullDelta,
            localWidth.toFloat(), pullHeight.toFloat()
        )
        path!!.lineTo(localWidth.toFloat(), 0f)
        canvas.drawPath(path!!, backPaint!!)

        ballYSpringAnim.start()

        canvas.drawCircle(
            (localWidth / 2).toFloat(),
            ballInnerY.toFloat(),
            radius.toFloat(),
            ballPaint!!
        )
    }

    private fun drawRefreshing(canvas: Canvas) {
        canvas.drawRect(0f, 0f, localWidth.toFloat(), localHeight.toFloat(), backPaint!!)

        ballYSpringAnim.start()

        canvas.drawCircle((localWidth / 2).toFloat(), ballInnerY.toFloat(), radius.toFloat(), ballPaint!!)
        val outerR = radius + 10
        refreshStart += if (isStart) 3 else 10
        refreshStop += if (isStart) 10 else 3
        refreshStart %= 360
        refreshStop %= 360
        var swipe = refreshStop - refreshStart
        swipe = if (swipe < 0) swipe + 360 else swipe
        canvas.drawArc(
            RectF(
                (localWidth / 2 - outerR).toFloat(), (ballInnerY.toFloat() - outerR),
                (localWidth / 2 + outerR).toFloat(), (ballInnerY.toFloat() + outerR)
            ),
            refreshStart.toFloat(), swipe.toFloat(), false, outPaint!!.apply {
                alpha = 105
            }
        )
        if (swipe >= targetDegree) {
            isStart = false
        } else if (swipe <= 10) {
            isStart = true
        }
        if (!isRefreshing) {
            applyDone()

        }
    }

    fun setRefreshing(isFresh: Boolean) {
        isRefreshing = isFresh
    }

    private fun drawDone(canvas: Canvas) {
        val beforeColor = outPaint!!.color
        if (doneRatio < 0.3) {
            canvas.drawRect(0f, 0f, localWidth.toFloat(), localHeight.toFloat(), backPaint!!)
            val innerY = pullHeight - pullDelta / 2 - radius * 2
            canvas.drawCircle((localWidth / 2).toFloat(), innerY.toFloat(), radius.toFloat(), ballPaint!!)
            val outerR = (radius.toFloat() + 10f + 10 * doneRatio / 0.3f).toInt()
            val afterColor = Color.argb(
                (0xff * (1 - doneRatio / 0.3f)).toInt(), Color.red(beforeColor),
                Color.green(beforeColor), Color.blue(beforeColor)
            )
            outPaint!!.color = afterColor
            canvas.drawArc(
                RectF(
                    (localWidth / 2 - outerR).toFloat(), (innerY - outerR).toFloat(),
                    (localWidth / 2 + outerR).toFloat(), (innerY + outerR).toFloat()
                ),
                0f, 360f, false, outPaint!!
            )
        }
        outPaint!!.color = beforeColor
        if (doneRatio >= 0.3 && doneRatio < 0.7) {
            canvas.drawRect(0f, 0f, localWidth.toFloat(), localHeight.toFloat(), backPaint!!)
            val fraction = (doneRatio - 0.3f) / 0.4f
            val startCentY = pullHeight - pullDelta / 2 - radius * 2
            val curCentY = (startCentY + (pullDelta / 2 + radius * 2) * fraction).toInt()
            canvas.drawCircle(
                (localWidth / 2).toFloat(),
                curCentY.toFloat(),
                radius.toFloat(),
                ballPaint!!
            )
            if (curCentY >= pullHeight - radius * 2) {
                drawTail(canvas, curCentY, pullHeight, 1 - fraction)
            }
        }
        if (!(doneRatio < 0.7 || doneRatio > 1)) {
            val fraction = (doneRatio - 0.7f) / 0.3f
            canvas.drawRect(0f, 0f, localWidth.toFloat(), localHeight.toFloat(), backPaint!!)
            val leftX =
                ((localWidth / 2).toFloat() - radius.toFloat() - 2f * radius.toFloat() * fraction).toInt()
            path!!.reset()
            path!!.moveTo(leftX.toFloat(), pullHeight.toFloat())
            path!!.quadTo(
                (localWidth / 2).toFloat(), pullHeight - radius * (1 - fraction),
                (localWidth - leftX).toFloat(), pullHeight.toFloat()
            )
            canvas.drawPath(path!!, ballPaint!!)
        }
    }

    fun releaseDrag() {
        start1 = System.currentTimeMillis()
        stop = start1 + REL_DRAG_DUR
        animatorStatus = AnimatorStatus.RELEASE_DRAG
        spriDeta = localHeight - pullHeight
        requestLayout()

        performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    private fun springUp() {
        sprStart = System.currentTimeMillis()
        sprStop = sprStart + SPRING_DUR
        animatorStatus = AnimatorStatus.SPRING_UP
        invalidate()
    }

    private fun popBall() {
        popStart = System.currentTimeMillis()
        popStop = popStart + POP_BALL_DUR
        animatorStatus = AnimatorStatus.POP_BALL
        invalidate()
    }

    private fun startOutCir() {
        outStart = System.currentTimeMillis()
        outStop = outStart + OUTER_DUR
        animatorStatus = AnimatorStatus.OUTER_CIRCLE
        refreshStart = 90
        refreshStop = 90
        targetDegree = 270
        isStart = true
        isRefreshing = true
        invalidate()
    }

    private var animatedAlpha: Float = 1f // Track the alpha value for the drawable
    private var alphaAnimator: ValueAnimator? = null // Declare animator variable

    private fun startAlphaAnimation() {
        // Create an animator to change the alpha value from 0 to 255 and back to 0
        alphaAnimator = ValueAnimator.ofInt(55, 255)
        alphaAnimator?.apply {
            duration = 800 // Duration of the animation cycle
            repeatCount = ValueAnimator.INFINITE // Repeat indefinitely
            repeatMode = ValueAnimator.REVERSE // Reverse the animation after it reaches the end
            addUpdateListener { animator ->
                // Update the animated alpha value and invalidate the view
                animatedAlpha = (animator.animatedValue as Int).toFloat() / 255 // Convert back to float [0,1]
                invalidate() // Request a redraw to update the view with the new alpha
            }
            start() // Start the animation
        }
    }

    private fun stopAlphaAnimation() {
        alphaAnimator?.cancel() // Stop the animation if it's running
        animatedAlpha = 1f // Reset alpha to fully opaque after stopping
    }

    // Update the draw method to use the animatedAlpha value
    private fun drawVectorDrawable(canvas: Canvas) {
        iconDrawable?.let { drawable ->

            drawable.alpha = (animatedAlpha * 255).toInt()
            // Calculate the y position based on pull height and radius

            // Calculate the center position for the drawable
            val centerX = (localWidth / 2).toFloat() // Center X of the drawable
            val centerY = ballInnerY.toFloat()           // Adjusted Y position based on pull

            // Calculate the size of the drawable (scaled down relative to the circle radius)
            val drawableRadius = radius * 0.6f // Adjust size relative to circle radius

            // Calculate bounds around the centerX and centerY
            val left = (centerX - drawableRadius).toInt()
            val top = (centerY - drawableRadius).toInt()
            val right = (centerX + drawableRadius).toInt()
            val bottom = (centerY + drawableRadius).toInt()

            // Set the bounds for the vector drawable
            drawable.setBounds(left, top, right, bottom)

            // Draw the vector drawable on the canvas
            drawable.draw(canvas)
        }
    }

    private fun applyDone() {
        doneStart = System.currentTimeMillis()
        doneStop = doneStart + DONE_DUR
        animatorStatus = AnimatorStatus.DONE
    }

    fun setOnAnimationDoneListener(onAnimationDoneListener: OnAnimationDoneListener) {
        this.onAnimationDoneListener = onAnimationDoneListener
    }

    interface OnAnimationDoneListener {
        fun viewAniDone()
    }

    fun setAniBackColor(color: Int) {
        backPaint!!.color = color
    }

    fun setAniForeColor(color: Int) {
        ballPaint!!.color = color
        outPaint!!.color = color
        setBackgroundColor(color)
    }

    fun setRadius(smallTimes: Int) {
        radius = localHeight / smallTimes
    }

    companion object {
        private const val REL_DRAG_DUR: Long = 100

        private const val SPRING_DUR: Long = 100

        private const val POP_BALL_DUR: Long = 100

        private const val OUTER_DUR: Long = 200

        private const val DONE_DUR: Long = 200
    }
}