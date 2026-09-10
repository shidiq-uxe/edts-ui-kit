package id.co.edtslib.uikit.progressbar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withClip
import androidx.core.view.doOnLayout
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.core.textbadge.CoreTextBadge
import id.co.edtslib.uikit.core.textbadge.DefaultShape
import id.co.edtslib.uikit.utils.applyFill
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dimen
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.interpolator.EaseInterpolator
import kotlin.math.abs
import kotlin.math.max

class LinearProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var delegate: LinearProgressBarDelegate? = null

    enum class IntermittentMode {
        FIXED_WIDTH,
        STRETCH
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val completedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val innerShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private val innerShadowClipPath = Path()
    private val innerShadowDonutPath = Path()
    private val trackPath = Path()
    private var indicatorRect = RectF()
    private var trackRect = RectF()
    private val completedRect = RectF()
    private val thumbRect = RectF()
    private val outerShadowRect = RectF()

    private var progressAnimator: ValueAnimator? = null
    private var lapAnimator: ValueAnimator? = null
    private var thumbAnimator: ValueAnimator? = null
    private var badgeAnimator: ValueAnimator? = null
    private var intermittentAnimator: ValueAnimator? = null

    private var indicatorGradient: IntArray? = intArrayOf(context.color(R.color.progress_bar_light_blue), context.color(R.color.progress_bar_dark_blue))
    private var indicatorGradientOrientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT

    private var thumbGradient: IntArray? = intArrayOf(context.color(R.color.progress_bar_light_blue), context.color(R.color.progress_bar_dark_blue))
    private var thumbGradientOrientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT

    private var trackGradient: IntArray? = null
    private var trackGradientOrientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT

    private var completedGradient: IntArray? = null
    private var completedGradientOrientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT
    private var intermittentMode: IntermittentMode = IntermittentMode.FIXED_WIDTH

    private var lapTransition = 0f
    private var thumbScale = 1f
    private var badgeScale = 1f
    private var progress = 0f
    private var pendingTargetProgress = 0f
    private var completedIndicatorAlpha = 0f
    private var intermittentFraction = 0f
    private var restartLapIndex: Int? = null
    private var isThumbHiddenForLoading = false
    private var isFinishPending = false
    private var isIntermittentRunning = false

    private val startAnimationRunnable = Runnable {
        if (!isEnabled) return@Runnable
        if (progressAnimator?.isRunning == true) return@Runnable

        if (isThumbHiddenForLoading) {
            animateToPendingTarget()
        } else {
            animateThumbScale(from = thumbScale, to = 0f, duration = 200L) {
                animateToPendingTarget()
            }
        }
    }

    private val displayProgress: Float
        get() = when {
            restartLapIndex != null -> {
                val lapStart = restartLapIndex!! * maxIndicatorProgress
                (progress - lapStart).coerceIn(0f, maxIndicatorProgress)
            }
            progress > 0f && progress % maxIndicatorProgress == 0f -> maxIndicatorProgress
            else -> progress % maxIndicatorProgress
        }

    private val disabledTrackColor: Int by lazy { context.color(R.color.black_20) }
    private val disabledCompletedColor: Int by lazy { context.color(R.color.black_30) }
    private val disabledIndicatorColor: Int by lazy { context.color(R.color.black_40) }
    private val disabledShadowColor: Int by lazy { (context.color(R.color.progress_bar_black_opacity)) }

    private val displayIndicatorColor: Int
        get() = if (isEnabled) indicatorColor else disabledIndicatorColor
    private val displayIndicatorGradient: IntArray?
        get() = if (isEnabled) indicatorGradient else null
    private val displayIndicatorGradientOrientation: GradientDrawable.Orientation
        get() = indicatorGradientOrientation
    private val displayThumbColor: Int
        get() = if (isEnabled) thumbColor else context.color(R.color.black_40)
    private val displayThumbGradient: IntArray?
        get() = if (isEnabled) thumbGradient else null
    private val displayThumbGradientOrientation: GradientDrawable.Orientation
        get() = thumbGradientOrientation
    private val displayTrackColor: Int
        get() = if (isEnabled) trackColor else disabledTrackColor
    private val displayTrackGradient: IntArray?
        get() = if (isEnabled) trackGradient else null
    private val displayTrackGradientOrientation: GradientDrawable.Orientation
        get() = trackGradientOrientation
    private val displayCompletedColor: Int
        get() = if (isEnabled) completedIndicatorColor else disabledCompletedColor
    private val displayCompletedGradient: IntArray?
        get() = if (isEnabled) completedGradient else null
    private val displayCompletedGradientOrientation: GradientDrawable.Orientation
        get() = completedGradientOrientation
    private val displayShadowColor: Int
        get() = if (isEnabled) innerShadowColor else disabledShadowColor

    var shouldAnimate: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    var indicatorProgress: Float
        get() = pendingTargetProgress
        set(value) {
            pendingTargetProgress = value.coerceIn(0f, progressLimit)

            if (isIntermittentRunning) stopIntermittentAnimation()

            if (!shouldAnimate || !isEnabled) {
                skipAnimations()

                progress = pendingTargetProgress
                updateBadgeCount()
                invalidate()
                return
            }

            if (pendingTargetProgress == progress) return

            removeCallbacks(startAnimationRunnable)
            postDelayed(startAnimationRunnable, 300L)
        }

    var maxIndicatorProgress: Float = 100f
        set(value) {
            field = value.coerceAtLeast(1f)
            invalidate()
        }

    var progressLimit: Float = 100f
        set(value) {
            field = value.coerceAtLeast(0f)
            updateBadgeCount()
        }

    @ColorInt
    var indicatorColor: Int = context.color(R.color.primary_30)
        set(value) {
            field = value
            indicatorGradient = null
            invalidate()
        }

    var indicatorPadding: Float = context.dimen(R.dimen.dimen_1)
        set(value) {
            field = value
            invalidate()
        }

    @ColorInt
    var trackColor: Int = context.color(R.color.black_20)
        set(value) {
            field = value
            trackGradient = null
            invalidate()
        }

    var trackThickness: Float = context.dimen(R.dimen.dimen_6)
        set(value) {
            field = value
            requestLayout()
        }

    var trackCornerRadius: Float = trackThickness / 2f
        set(value) {
            field = value
            invalidate()
        }

    var trackPadding: Float = 0f
        set(value) {
            field = value
            requestLayout()
        }

    var completedIndicatorColor: Int = context.color(R.color.primary_10)
        set(value) {
            field = value
            completedGradient = null
            invalidate()
        }

    var showCompletedIndicator: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    var thumbSize: Float = context.dimen(R.dimen.xxs)
        set(value) {
            field = value
            requestLayout()
        }

    var thumbCornerRadius: Float = thumbSize / 2f
        set(value) {
            field = value
            invalidate()
        }

    @ColorInt
    var thumbColor: Int = context.color(R.color.primary_30)
        set(value) {
            field = value
            thumbGradient = null
            invalidate()
        }

    var showThumb: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    @ColorInt
    var innerShadowColor: Int = (context.color(R.color.progress_bar_black_opacity))
        set(value) {
            field = value
            invalidate()
        }

    var innerShadowOffsetX: Float = 1f
        set(value) {
            field = value.dp
            invalidate()
        }

    var innerShadowOffsetY: Float = 2f
        set(value) {
            field = value.dp
            invalidate()
        }

    var innerShadowBlur: Float = 5f
        set(value) {
            field = value
            innerShadowPaint.maskFilter = BlurMaskFilter(value, BlurMaskFilter.Blur.NORMAL)
            invalidate()
        }

    var showBadge: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    var badgeCount: Int = 0
        set(value) {
            if (field == value) return

            val previous = field
            field = value
            badge.text = "x$value"

            when {
                previous == 0 && value == 1 -> {
                    badgeScale = 0f
                    animateBadgeAppear()
                }
                value > 1 -> {
                    badgeScale = 1f
                    animateBadgeBounce()
                }
            }
            invalidate()
        }

    var badge: CoreTextBadge = createDefaultBadge()
        set(value) {
            field = value
            invalidate()
            requestLayout()
        }

    fun setIndicatorGradient(
        @ColorInt colors: IntArray,
        orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT
    ) {
        indicatorGradient = colors
        indicatorGradientOrientation = orientation
        invalidate()
    }

    fun setThumbGradient(
        @ColorInt colors: IntArray,
        orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT
    ) {
        thumbGradient = colors
        thumbGradientOrientation = orientation
        invalidate()
    }

    fun setTrackGradient(
        @ColorInt colors: IntArray,
        orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT
    ) {
        trackGradient = colors
        trackGradientOrientation = orientation
        invalidate()
    }

    fun setCompletedIndicatorGradient(
        @ColorInt colors: IntArray,
        orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT
    ) {
        completedGradient = colors
        completedGradientOrientation = orientation
        invalidate()
    }

    fun setLoadingStarted() {
        if (isThumbHiddenForLoading) return

        isThumbHiddenForLoading = true
        isFinishPending = false
        animateThumbScale(from = thumbScale, to = 0f, duration = 200L)
    }

    fun setLoadingFinished() {
        if (!isThumbHiddenForLoading) return

        if (progressAnimator?.isRunning == true) {
            isFinishPending = true
            return
        }

        isThumbHiddenForLoading = false
        animateThumbScale(from = thumbScale, to = 1f, duration = 200L)
    }

    fun startIntermittentAnimation(mode: IntermittentMode = intermittentMode) {
        skipAnimations()
        removeCallbacks(startAnimationRunnable)

        intermittentMode = mode
        isIntermittentRunning = true

        val durationMs = if (mode == IntermittentMode.FIXED_WIDTH) {
            INTERMITTENT_FIXED_WIDTH_DURATION_MS
        } else {
            INTERMITTENT_STRETCH_DEFAULT_DURATION_MS
        }

        intermittentAnimator?.cancel()
        intermittentAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = durationMs
            interpolator = if (mode == IntermittentMode.FIXED_WIDTH) {
                LinearInterpolator()
            } else {
                EaseInterpolator.EaseInOutQubicInterpolator
            }
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART

            addUpdateListener {
                intermittentFraction = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun stopIntermittentAnimation() {
        intermittentAnimator?.cancel()
        intermittentAnimator = null
        isIntermittentRunning = false
        intermittentFraction = 0f
        invalidate()
    }

    init {
        innerShadowPaint.maskFilter = BlurMaskFilter(innerShadowBlur, BlurMaskFilter.Blur.NORMAL)

        context.theme.obtainStyledAttributes(attrs, R.styleable.LinearProgressBar, 0, 0).apply {
            try {
                indicatorProgress = getFloat(R.styleable.LinearProgressBar_indicatorProgress, indicatorProgress)
                maxIndicatorProgress = getFloat(R.styleable.LinearProgressBar_maxIndicatorProgress, maxIndicatorProgress)
                progressLimit = getFloat(R.styleable.LinearProgressBar_progressLimit, progressLimit)
                shouldAnimate = getBoolean(R.styleable.LinearProgressBar_shouldAnimate, shouldAnimate)
                indicatorPadding = getDimension(R.styleable.LinearProgressBar_indicatorPadding, indicatorPadding)
                trackThickness = getDimension(R.styleable.LinearProgressBar_trackThickness, trackThickness)
                trackPadding = getDimension(R.styleable.LinearProgressBar_trackPadding, trackPadding)
                trackCornerRadius = getDimension(R.styleable.LinearProgressBar_trackCornerRadius, trackThickness / 2f)
                showCompletedIndicator = getBoolean(R.styleable.LinearProgressBar_showCompletedIndicator, showCompletedIndicator)
                thumbSize = getDimension(R.styleable.LinearProgressBar_thumbSize, thumbSize)
                thumbCornerRadius = getDimension(R.styleable.LinearProgressBar_thumbCornerRadius,thumbSize / 2f)
                showThumb = getBoolean(R.styleable.LinearProgressBar_showThumb, showThumb)
                innerShadowOffsetX = getFloat(R.styleable.LinearProgressBar_innerShadowOffsetX, innerShadowOffsetX)
                innerShadowOffsetY = getFloat(R.styleable.LinearProgressBar_innerShadowOffsetY, innerShadowOffsetY)
                innerShadowBlur = getFloat(R.styleable.LinearProgressBar_innerShadowBlur, innerShadowBlur)
                showBadge = getBoolean(R.styleable.LinearProgressBar_showBadge, showBadge)

                if (hasValue(R.styleable.LinearProgressBar_indicatorColor)) {
                    indicatorColor = getColor(R.styleable.LinearProgressBar_indicatorColor, indicatorColor)
                }
                if (hasValue(R.styleable.LinearProgressBar_trackColor)) {
                    trackColor = getColor(R.styleable.LinearProgressBar_trackColor, trackColor)
                }
                if (hasValue(R.styleable.LinearProgressBar_completedIndicatorColor)) {
                    completedIndicatorColor = getColor(R.styleable.LinearProgressBar_completedIndicatorColor, completedIndicatorColor)
                }
                if (hasValue(R.styleable.LinearProgressBar_thumbColor)) {
                    thumbColor = getColor(R.styleable.LinearProgressBar_thumbColor, thumbColor)
                }
                if (hasValue(R.styleable.LinearProgressBar_innerShadowColor)) {
                    innerShadowColor = getColor(R.styleable.LinearProgressBar_innerShadowColor, innerShadowColor)
                }
            } finally {
                recycle()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        skipAnimations()
    }

    override fun setEnabled(enabled: Boolean) {
        if (enabled == isEnabled) return
        super.setEnabled(enabled)
        badge.setGradientBackground(
            if (enabled) {
                thumbGradient ?: intArrayOf(thumbColor, thumbColor)
            } else {
                val disabledThumb = context.color(R.color.black_40)
                intArrayOf(disabledThumb, disabledThumb)
            },
            GradientDrawable.Orientation.LEFT_RIGHT
        )
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val trackHeight = trackThickness + trackPadding * 2
        val thumbHeight = thumbSize

        badge.minimumHeight = (context.dimen(R.dimen.s)).toInt()
        badge.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        val badgeHeight = if (showBadge) badge.measuredHeight else 0f

        val height = maxOf(trackHeight, thumbHeight, badgeHeight.toFloat())
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height.toInt())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Define the track rectangle
        val left = paddingLeft + trackPadding
        val right = width - paddingRight - trackPadding
        val top = (height - trackThickness) / 2f
        val bottom = top + trackThickness
        trackRect.set(left, top, right, bottom)

        // Draw the track
        trackPaint.applyFill(trackRect, displayTrackColor, displayTrackGradient, displayTrackGradientOrientation)
        canvas.drawRoundRect(trackRect, trackCornerRadius, trackCornerRadius, trackPaint)

        // Draw inner shadow
        drawInnerShadow(canvas)

        if (showCompletedIndicator) {
            drawCompletedIndicator(canvas)
        }

        // Clip indicator if over the track
        val clipRadius = minOf(
            trackCornerRadius,
            (trackRect.height() - indicatorPadding * 2f) / 2f
        )

        trackPath.reset()
        trackPath.addRoundRect(
            trackRect.left + indicatorPadding,
            trackRect.top + indicatorPadding,
            trackRect.right - indicatorPadding,
            trackRect.bottom - indicatorPadding,
            clipRadius,
            clipRadius,
            Path.Direction.CW
        )

        canvas.withClip(trackPath) {
            if (isIntermittentRunning) {
                drawIntermittentIndicator(this)
            } else {
                drawCurrentIndicator(this)
            }
        }

        // Draw thumb
        if (showThumb && !isIntermittentRunning) {
            drawThumb(canvas)
        }

        // Draw badge
        if (showBadge && badgeCount > 0 && !isIntermittentRunning) {
            drawBadge(canvas)
        }
    }

    private fun drawThumb(canvas: Canvas) {
        if (showBadge && displayProgress >= maxIndicatorProgress) {
            return
        }

        val thumbCenterX = indicatorRect.right.coerceAtMost(trackRect.right - thumbSize / 2f)
        val thumbCenterY = trackRect.centerY()
        val scaledSize = thumbSize * thumbScale

        thumbRect.set(
            thumbCenterX - scaledSize / 2f,
            thumbCenterY - scaledSize / 2f,
            thumbCenterX + scaledSize / 2f,
            thumbCenterY + scaledSize / 2f
        )

        thumbPaint.applyFill(thumbRect, displayThumbColor, displayThumbGradient, displayThumbGradientOrientation)
        canvas.drawRoundRect(
            thumbRect,
            thumbCornerRadius * thumbScale,
            thumbCornerRadius * thumbScale,
            thumbPaint
        )
    }

    private fun drawCurrentIndicator(canvas: Canvas) {
        val completedLaps = (progress / maxIndicatorProgress).toInt()
        val startIndicatorWidth =
            if (completedLaps == 0) {
                thumbSize / 2f + context.dimen(R.dimen.dimen_1)
            } else {
                0f
            }
        val endIndicatorWidth = thumbSize / 2f
        val availableWidth = (trackRect.width() - indicatorPadding * 2 - startIndicatorWidth).coerceAtLeast(0f)
        val progressWidth = availableWidth * (displayProgress / maxIndicatorProgress)
        val indicatorWidth = startIndicatorWidth + progressWidth

        indicatorRect.set(
            trackRect.left + indicatorPadding,
            trackRect.top + indicatorPadding,
            (trackRect.left + indicatorPadding + indicatorWidth).coerceAtMost(trackRect.right - indicatorPadding),
            trackRect.bottom - indicatorPadding
        )

        paint.applyFill(indicatorRect, displayIndicatorColor, displayIndicatorGradient, displayIndicatorGradientOrientation)
        paint.alpha = (255 * (1f - lapTransition)).toInt()

        canvas.drawRoundRect(indicatorRect, trackCornerRadius, trackCornerRadius, paint)

        paint.alpha = 255
    }

    private fun drawCompletedIndicator(canvas: Canvas) {
        completedRect.set(
            trackRect.left + indicatorPadding,
            trackRect.top + indicatorPadding,
            trackRect.right - indicatorPadding,
            trackRect.bottom - indicatorPadding
        )

        completedPaint.applyFill(completedRect, displayCompletedColor, displayCompletedGradient, displayCompletedGradientOrientation)
        val alpha = if (lapTransition > 0f) 1f else completedIndicatorAlpha
        completedPaint.alpha = (255 * alpha).toInt()

        canvas.drawRoundRect(completedRect, trackCornerRadius, trackCornerRadius, completedPaint)
    }

    private fun drawIntermittentIndicator(canvas: Canvas) {
        val innerLeft = trackRect.left + indicatorPadding
        val innerRight = trackRect.right - indicatorPadding
        val innerWidth = (innerRight - innerLeft).coerceAtLeast(0f)

        val (segLeft, segRight) = when (intermittentMode) {
            IntermittentMode.FIXED_WIDTH -> {
                val segmentWidth = innerWidth * (1f / 3f)
                val travel = innerWidth + segmentWidth
                val start = innerLeft - segmentWidth + travel * intermittentFraction
                start to (start + segmentWidth)
            }
            IntermittentMode.STRETCH -> {
                if (intermittentFraction <= 0.5f) {
                    val growPhase = intermittentFraction / 0.5f
                    innerLeft to (innerLeft + innerWidth * growPhase)
                } else {
                    val shrinkPhase = (intermittentFraction - 0.5f) / 0.5f
                    (innerLeft + innerWidth * shrinkPhase) to innerRight
                }
            }
        }

        indicatorRect.set(
            segLeft.coerceIn(innerLeft, innerRight),
            trackRect.top + indicatorPadding,
            segRight.coerceIn(innerLeft, innerRight),
            trackRect.bottom - indicatorPadding
        )

        paint.applyFill(indicatorRect, displayIndicatorColor, displayIndicatorGradient, displayIndicatorGradientOrientation)
        paint.alpha = 255
        canvas.drawRoundRect(indicatorRect, trackCornerRadius, trackCornerRadius, paint)
    }

    private fun drawInnerShadow(canvas: Canvas) {
        canvas.save()

        innerShadowClipPath.reset()
        innerShadowClipPath.addRoundRect(trackRect, trackCornerRadius, trackCornerRadius, Path.Direction.CW)
        canvas.clipPath(innerShadowClipPath)

        innerShadowPaint.color = displayShadowColor
        val spread = innerShadowBlur * 3f + max(abs(innerShadowOffsetX), abs(innerShadowOffsetY))
        outerShadowRect.set(
            trackRect.left - spread,
            trackRect.top - spread,
            trackRect.right + spread,
            trackRect.bottom + spread
        )

        innerShadowDonutPath.reset()
        innerShadowDonutPath.fillType = Path.FillType.EVEN_ODD
        innerShadowDonutPath.addRect(outerShadowRect, Path.Direction.CW)
        innerShadowDonutPath.addRoundRect(trackRect, trackCornerRadius, trackCornerRadius, Path.Direction.CW)

        canvas.translate(innerShadowOffsetX, innerShadowOffsetY)
        canvas.drawPath(innerShadowDonutPath, innerShadowPaint)

        canvas.restore()
    }

    private fun drawBadge(canvas: Canvas) {
        // Apply minWidth and minHeight to keep the initial round shape
        badge.minimumHeight = (context.dimen(R.dimen.s)).toInt()
        badge.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        val minWidthPx = badge.measuredHeight

        if (badge.measuredWidth < minWidthPx) {
            badge.minimumWidth = minWidthPx
            badge.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        }

        val badgeWidth = badge.measuredWidth
        val badgeHeight = badge.measuredHeight
        val badgeRight = trackRect.right
        val badgeLeft = (badgeRight - badgeWidth).coerceIn(0f, (width - badgeWidth).toFloat()).toInt()
        val badgeTop = ((height - badgeHeight) / 2f).toInt()
        badge.layout(badgeLeft, badgeTop, badgeLeft + badgeWidth, badgeTop + badgeHeight)
        canvas.save()

        val pivotX = badgeLeft + badgeWidth / 2f
        val pivotY = badgeTop + badgeHeight / 2f

        canvas.scale(
            badgeScale,
            badgeScale,
            pivotX,
            pivotY
        )

        canvas.translate(
            badgeLeft.toFloat(),
            badgeTop.toFloat()
        )

        badge.draw(canvas)
        canvas.restore()
    }

    private fun createDefaultBadge() = CoreTextBadge(context).apply {
        text = "x$badgeCount"
        setCoreShape(DefaultShape())
        textColor = context.color(R.color.white)
        badgeTextAppearance = R.style.TextAppearance_Inter_Bold_TextBadge
        iconVisible = false
        doOnLayout { setCornerRadius(badge.measuredHeight / 2f) }
        setGradientBackground(
            thumbGradient ?: intArrayOf("#58AAF3".toColorInt(), "#1178D4".toColorInt()),
            GradientDrawable.Orientation.LEFT_RIGHT
        )
    }

    private fun updateBadgeCount() {
        val completed = (progress / maxIndicatorProgress).toInt()
        badgeCount = completed
        showCompletedIndicator = completed > 0
    }

    private fun animateToPendingTarget() {
        val current = progress
        val target = pendingTargetProgress
        restartLapIndex = null

        if (current >= target) {
            return
        }

        val currentLap = if (current > 0f && current % maxIndicatorProgress == 0f) {
            (current / maxIndicatorProgress).toInt() - 1
        } else {
            (current / maxIndicatorProgress).toInt()
        }
        val targetLap = (target / maxIndicatorProgress).toInt()
        val lapDiff = targetLap - currentLap

        if (lapDiff == 0) {
            animateProgressChange(target) {
                lapTransition = 0f
                onReachedCurrentPendingTarget()
            }
            return
        }

        val endOfCurrentLap = (currentLap + 1) * maxIndicatorProgress

        if (lapDiff == 1) {
            animateProgressChange(endOfCurrentLap) {
                animateRemainderAfterLapBoundary(targetLap, endOfCurrentLap)
            }
            return
        }

        animateProgressChange(endOfCurrentLap) {
            progress = targetLap * maxIndicatorProgress
            updateBadgeCount()
            invalidate()
            animateRemainderAfterLapBoundary(targetLap, progress)
        }
    }

    private fun onReachedCurrentPendingTarget() {
        if (progress < pendingTargetProgress) {
            animateToPendingTarget()
            return
        }

        if (isFinishPending) {
            isFinishPending = false
            isThumbHiddenForLoading = false
            animateThumbScale(from = thumbScale, to = 1f, duration = 200L)
        } else if (!isThumbHiddenForLoading) {
            animateThumbScale(from = 0f, to = 1f, duration = 200L)
        }
    }

    private fun animateProgressChange(
        targetProgress: Float,
        onEnd: (() -> Unit)? = null
    ) {
        if (progress == targetProgress) {
            onEnd?.invoke()
            return
        }

        if (progressAnimator == null) {
            progressAnimator = ValueAnimator().apply {
                duration = 800
                interpolator = EaseInterpolator.EaseInOutQubicInterpolator

                addUpdateListener { animation ->
                    val animatedValue = (animation.animatedValue as Float).coerceIn(0f, progressLimit)

                    delegate?.onAnimationUpdateListener(
                        this@LinearProgressBar,
                        animatedValue,
                        targetProgress
                    )

                    progress = animatedValue
                    updateBadgeCount()
                    invalidate()
                }
            }
        }
        progressAnimator?.apply {
            setFloatValues(progress, targetProgress)
            removeAllListeners()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onEnd?.invoke()
                }
            })

            start()
        }
    }

    private fun animateLapTransition(onEnd: (() -> Unit)? = null) {
        lapAnimator?.cancel()
        lapAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 400L
            interpolator = EaseInterpolator.EaseInOutQubicInterpolator

            addUpdateListener {
                lapTransition = it.animatedValue as Float
                invalidate()
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    completedIndicatorAlpha = 1f
                    onEnd?.invoke()
                }
            })

            start()
        }
    }

    private fun animateThumbScale(
        from: Float,
        to: Float,
        duration: Long,
        onEnd: (() -> Unit)? = null
    ) {
        thumbAnimator?.cancel()
        thumbAnimator = ValueAnimator.ofFloat(from, to).apply {
            this.duration = duration
            interpolator = EaseInterpolator.EaseInOutQubicInterpolator

            addUpdateListener {
                thumbScale = it.animatedValue as Float
                invalidate()
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onEnd?.invoke()
                }
            })

            start()
        }
    }

    private fun animateBadgeAppear() {
        badgeAnimator?.cancel()

        badgeAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 200L
            interpolator = EaseInterpolator.EaseInOutQubicInterpolator

            addUpdateListener {
                badgeScale = it.animatedValue as Float
                invalidate()
            }

            start()
        }
    }

    private fun animateBadgeBounce() {
        badgeAnimator?.cancel()

        badgeAnimator = ValueAnimator.ofFloat(1f, 1.1f).apply {
            duration = 200L
            interpolator = EaseInterpolator.EaseInOutQubicInterpolator
            addUpdateListener { badgeScale = it.animatedValue as Float; invalidate() }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    badgeAnimator = ValueAnimator.ofFloat(1.1f, 1f).apply {
                        duration = 200L
                        interpolator = EaseInterpolator.EaseInOutQubicInterpolator
                        addUpdateListener { badgeScale = it.animatedValue as Float; invalidate() }
                        start()
                    }
                }
            })
            start()
        }
    }

    private fun animateRemainderAfterLapBoundary(targetLap: Int, lapEndProgress: Float) {
        val remainder = pendingTargetProgress % maxIndicatorProgress
        if (remainder > 0f) {
            animateLapTransition {
                lapTransition = 0f
                restartLapIndex = targetLap
                animateProgressChange(lapEndProgress + remainder) {
                    onReachedCurrentPendingTarget()
                }
            }
        } else {
            animateProgressChange(lapEndProgress + remainder) {
                onReachedCurrentPendingTarget()
            }
        }
    }

    private fun skipAnimations() {
        removeCallbacks(startAnimationRunnable)

        progressAnimator?.cancel()
        lapAnimator?.cancel()
        thumbAnimator?.cancel()
        badgeAnimator?.cancel()
        intermittentAnimator?.cancel()
        intermittentAnimator = null
        isIntermittentRunning = false
        intermittentFraction = 0f

        thumbScale = 1f
        lapTransition = 0f
        restartLapIndex = null
        isThumbHiddenForLoading = false
        isFinishPending = false
        badgeScale = if (badgeCount > 0) 1f else 0f
    }

    companion object {
        private const val INTERMITTENT_FIXED_WIDTH_DURATION_MS = 2100L
        private const val INTERMITTENT_STRETCH_DEFAULT_DURATION_MS = 1400L
    }
}