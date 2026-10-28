package id.co.edtslib.uikit.core.linearprogressbar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
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

open class CoreLinearProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.linearProgressBarStyle,
    protected val variant: LinearProgressBarVariant = DefaultLinearProgressBarVariant
) : View(context, attrs, defStyleAttr, R.style.Widget_EDTS_UIKit_LinearProgressBar) {

    enum class IntermittentMode {
        FIXED_WIDTH,
        STRETCH
    }

    var delegate: LinearProgressBarDelegate? = null

    private val trackRect = RectF()
    private val indicatorRect = RectF()
    private val completedRect = RectF()
    private val thumbRect = RectF()
    private val outerShadowRect = RectF()
    private val holeRect = RectF()

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val completedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val innerShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private val clipPath = Path()
    private val innerShadowClipPath = Path()
    private val innerShadowDonutPath = Path()

    private var progressAnimator: ValueAnimator? = null
    private var intermittentAnimator: ValueAnimator? = null
    private var lapAnimator: ValueAnimator? = null
    private var thumbAnimator: ValueAnimator? = null
    private var badgeAnimator: ValueAnimator? = null
    private var animationTarget = 0f

    private var indicatorGradient: IntArray? = null
    private var indicatorGradientOrientation = GradientDrawable.Orientation.LEFT_RIGHT
    private var trackGradient: IntArray? = null
    private var trackGradientOrientation = GradientDrawable.Orientation.LEFT_RIGHT
    private var thumbGradient: IntArray? = null
    private var thumbGradientOrientation = GradientDrawable.Orientation.LEFT_RIGHT
    private var completedGradient: IntArray? = null
    private var completedGradientOrientation = GradientDrawable.Orientation.LEFT_RIGHT

    private var intermittentMode = IntermittentMode.FIXED_WIDTH
    private var intermittentFraction = 0f

    private var lapTransition = 0f
    private var thumbScale = 1f
    private var badgeScale = 1f
    private var completedIndicatorAlpha = 0f
    private var restartLapIndex: Int? = null
    private var isThumbHiddenForLoading = false
    private var isFinishPending = false
    private var xmlInitialProgress: Float? = null
    private var disabledTrackColor = Color.TRANSPARENT
    private var disabledIndicatorColor = Color.TRANSPARENT
    private var disabledThumbColor = Color.TRANSPARENT
    private var disabledCompletedColor = Color.TRANSPARENT
    private var disabledShadowColor = Color.TRANSPARENT

    private val startAnimationRunnable = Runnable {
        if (!isEnabled) return@Runnable
        if (isProgressAnimating) return@Runnable

        prepareProgressAnimation { animateToTarget() }
    }

    var currentProgress: Float = 0f
        private set

    var isIntermittentRunning: Boolean = false
        private set

    private val isProgressAnimating: Boolean
        get() = progressAnimator?.isRunning == true

    var shouldAnimate: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    var progressAnimationDuration: Long = 800L
    var progressAnimationDelay: Long = 300L

    var maxIndicatorProgress: Float = 100f
        set(value) {
            field = value.coerceAtLeast(1f)
            invalidate()
        }

    private var explicitProgressLimit: Float? = null

    var progressLimit: Float
        get() = explicitProgressLimit ?: maxIndicatorProgress
        set(value) {
            explicitProgressLimit = value.coerceAtLeast(0f)
            updateBadgeCount()
        }

    var indicatorProgress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, progressLimit)

            if (isIntermittentRunning) stopIntermittentAnimation()

            if (!shouldAnimate || !isEnabled) {
                cancelAllAnimations()
                currentProgress = field
                updateBadgeCount()
                invalidate()
                return
            }

            if (field == currentProgress) return

            removeCallbacks(startAnimationRunnable)
            postDelayed(startAnimationRunnable, progressAnimationDelay)
        }

    @ColorInt
    var indicatorColor: Int = Color.TRANSPARENT
        set(value) {
            field = value
            indicatorGradient = null
            invalidate()
        }

    var indicatorPadding: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    @ColorInt
    var trackColor: Int = Color.TRANSPARENT
        set(value) {
            field = value
            trackGradient = null
            invalidate()
        }

    var trackThickness: Float = 0f
        set(value) {
            field = value
            requestLayout()
        }

    var trackCornerRadius: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    var trackPadding: Float = 0f
        set(value) {
            field = value
            requestLayout()
        }

    @ColorInt
    var completedIndicatorColor: Int = Color.TRANSPARENT
        set(value) {
            field = value
            completedGradient = null
            invalidate()
        }

    var showCompletedIndicator: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    var showThumb: Boolean = false
        set(value) {
            field = value
            requestLayout()
        }

    var thumbSize: Float = 0f
        set(value) {
            field = value
            requestLayout()
        }

    var thumbCornerRadius: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    @ColorInt
    var thumbColor: Int = Color.TRANSPARENT
        set(value) {
            field = value
            thumbGradient = null
            invalidate()
        }

    var showInnerShadow: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    @ColorInt
    var innerShadowColor: Int = Color.TRANSPARENT
        set(value) {
            field = value
            invalidate()
        }

    var innerShadowOffsetX: Float = 0f
        set(value) {
            field = value.dp
            invalidate()
        }

    var innerShadowOffsetY: Float = 0f
        set(value) {
            field = value.dp
            invalidate()
        }

    var innerShadowBlur: Float = 0f
        set(value) {
            field = value
            innerShadowPaint.maskFilter =
                if (value > 0f) BlurMaskFilter(value, BlurMaskFilter.Blur.NORMAL) else null
            invalidate()
        }

    var innerShadowSpread: Float = 0f
        set(value) {
            field = value.dp
            invalidate()
        }

    var showBadge: Boolean = false
        set(value) {
            field = value
            requestLayout()
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

    private val displayProgress: Float
        get() = when {
            restartLapIndex != null -> {
                val lapStart = restartLapIndex!! * maxIndicatorProgress
                (currentProgress - lapStart).coerceIn(0f, maxIndicatorProgress)
            }
            currentProgress > 0f && currentProgress % maxIndicatorProgress == 0f -> maxIndicatorProgress
            else -> currentProgress % maxIndicatorProgress
        }

    private val displayTrackColor get() = if (isEnabled) trackColor else disabledTrackColor
    private val displayTrackGradient get() = if (isEnabled) trackGradient else null
    private val displayIndicatorColor get() = if (isEnabled) indicatorColor else disabledIndicatorColor
    private val displayIndicatorGradient get() = if (isEnabled) indicatorGradient else null
    private val displayThumbColor get() = if (isEnabled) thumbColor else disabledThumbColor
    private val displayThumbGradient get() = if (isEnabled) thumbGradient else null
    private val displayCompletedColor get() = if (isEnabled) completedIndicatorColor else disabledCompletedColor
    private val displayCompletedGradient get() = if (isEnabled) completedGradient else null
    private val displayShadowColor get() = if (isEnabled) innerShadowColor else disabledShadowColor

    init {
        applyVariant(variant)

        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.LinearProgressBar, defStyleAttr, R.style.Widget_EDTS_UIKit_LinearProgressBar)
        try {
            applyStyledAttributes(typedArray)
        } finally {
            typedArray.recycle()
        }
        refreshBadgeBackground()
    }

    private fun applyStyledAttributes(a: TypedArray) {
        maxIndicatorProgress = a.getFloat(R.styleable.LinearProgressBar_maxIndicatorProgress, maxIndicatorProgress)
        if (a.hasValue(R.styleable.LinearProgressBar_progressLimit)) {
            progressLimit = a.getFloat(R.styleable.LinearProgressBar_progressLimit, progressLimit)
        }
        shouldAnimate = a.getBoolean(R.styleable.LinearProgressBar_shouldAnimate, shouldAnimate)
        trackThickness = a.getDimension(R.styleable.LinearProgressBar_trackThickness, trackThickness)
        trackPadding = a.getDimension(R.styleable.LinearProgressBar_trackPadding, trackPadding)
        trackCornerRadius = if (a.hasValue(R.styleable.LinearProgressBar_trackCornerRadius)) {
            a.getDimension(R.styleable.LinearProgressBar_trackCornerRadius, trackCornerRadius)
        } else {
            variant.trackCornerRadius?.dp ?: (trackThickness / 2f)
        }
        if (a.hasValue(R.styleable.LinearProgressBar_trackColor)) {
            trackColor = a.getColor(R.styleable.LinearProgressBar_trackColor, trackColor)
        }
        indicatorPadding = a.getDimension(R.styleable.LinearProgressBar_indicatorPadding, indicatorPadding)
        if (a.hasValue(R.styleable.LinearProgressBar_indicatorColor)) {
            indicatorColor = a.getColor(R.styleable.LinearProgressBar_indicatorColor, indicatorColor)
        }
        if (a.hasValue(R.styleable.LinearProgressBar_showCompletedIndicator)) {
            showCompletedIndicator = a.getBoolean(R.styleable.LinearProgressBar_showCompletedIndicator, showCompletedIndicator)
        }
        if (a.hasValue(R.styleable.LinearProgressBar_completedIndicatorColor)) {
            completedIndicatorColor = a.getColor(R.styleable.LinearProgressBar_completedIndicatorColor, completedIndicatorColor)
        }
        if (a.hasValue(R.styleable.LinearProgressBar_showThumb)) {
            showThumb = a.getBoolean(R.styleable.LinearProgressBar_showThumb, showThumb)
        }
        thumbSize = a.getDimension(R.styleable.LinearProgressBar_thumbSize, thumbSize)
        thumbCornerRadius = a.getDimension(R.styleable.LinearProgressBar_thumbCornerRadius, thumbSize / 2f)
        if (a.hasValue(R.styleable.LinearProgressBar_thumbColor)) {
            thumbColor = a.getColor(R.styleable.LinearProgressBar_thumbColor, thumbColor)
        }
        var hasShadowAttr = false
        if (a.hasValue(R.styleable.LinearProgressBar_innerShadowColor)) {
            innerShadowColor = a.getColor(R.styleable.LinearProgressBar_innerShadowColor, innerShadowColor)
            hasShadowAttr = true
        }
        if (a.hasValue(R.styleable.LinearProgressBar_innerShadowOffsetX)) {
            innerShadowOffsetX = a.getFloat(R.styleable.LinearProgressBar_innerShadowOffsetX, 0f)
            hasShadowAttr = true
        }
        if (a.hasValue(R.styleable.LinearProgressBar_innerShadowOffsetY)) {
            innerShadowOffsetY = a.getFloat(R.styleable.LinearProgressBar_innerShadowOffsetY, 0f)
            hasShadowAttr = true
        }
        if (a.hasValue(R.styleable.LinearProgressBar_innerShadowBlur)) {
            innerShadowBlur = a.getFloat(R.styleable.LinearProgressBar_innerShadowBlur, innerShadowBlur)
            hasShadowAttr = true
        }
        if (a.hasValue(R.styleable.LinearProgressBar_innerShadowSpread)) {
            innerShadowSpread = a.getFloat(R.styleable.LinearProgressBar_innerShadowSpread, 0f)
            hasShadowAttr = true
        }
        if (hasShadowAttr) showInnerShadow = Color.alpha(innerShadowColor) != 0
        if (a.hasValue(R.styleable.LinearProgressBar_showBadge)) {
            showBadge = a.getBoolean(R.styleable.LinearProgressBar_showBadge, showBadge)
        }
        if (a.hasValue(R.styleable.LinearProgressBar_indicatorProgress)) {
            xmlInitialProgress = a.getFloat(R.styleable.LinearProgressBar_indicatorProgress, 0f)
        }
    }

    private fun applyVariant(variant: LinearProgressBarVariant) {
        trackThickness = variant.trackThickness.dp
        trackCornerRadius = variant.trackCornerRadius?.dp ?: (trackThickness / 2f)
        trackPadding = variant.trackPadding.dp
        indicatorPadding = variant.indicatorPadding.dp
        trackColor = context.color(variant.trackColorRes)
        indicatorColor = context.color(variant.indicatorColorRes)

        variant.indicatorGradient?.let {
            setIndicatorGradient(it.resolveColors(), variant.gradientOrientation)
        }

        showThumb = variant.showThumb
        thumbSize = context.dimen(variant.thumbSizeRes)
        thumbCornerRadius = thumbSize / 2f
        thumbColor = context.color(variant.thumbColorRes)
        variant.thumbGradient?.let { setThumbGradient(it.resolveColors(), variant.gradientOrientation) }
        completedIndicatorColor = context.color(variant.completedColorRes)
        showInnerShadow = variant.showInnerShadow
        innerShadowColor = context.color(variant.innerShadowColorRes)
        innerShadowOffsetX = variant.innerShadowOffsetX
        innerShadowOffsetY = variant.innerShadowOffsetY
        innerShadowBlur = variant.innerShadowBlur
        innerShadowSpread = variant.innerShadowSpread
        showBadge = variant.showBadge
        disabledTrackColor = context.color(variant.disabledTrackColorRes)
        disabledIndicatorColor = context.color(variant.disabledIndicatorColorRes)
        disabledThumbColor = context.color(variant.disabledThumbColorRes)
        disabledCompletedColor = context.color(variant.disabledCompletedColorRes)
        disabledShadowColor = context.color(variant.disabledInnerShadowColorRes)
        progressAnimationDuration = variant.animationDurationMs
        progressAnimationDelay = variant.animationDelayMs
    }

    private fun IntArray.resolveColors() = map { context.color(it) }.toIntArray()

    private fun enabledThumbColors() = thumbGradient ?: intArrayOf(thumbColor, thumbColor)

    private fun refreshBadgeBackground() {
        badge.setGradientBackground(
            if (isEnabled) {
                enabledThumbColors()
            } else {
                intArrayOf(disabledThumbColor, disabledThumbColor)
            },
            GradientDrawable.Orientation.LEFT_RIGHT
        )
    }

    fun setIndicatorGradient(@ColorInt colors: IntArray, orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT) {
        indicatorGradient = colors
        indicatorGradientOrientation = orientation
        invalidate()
    }

    fun setTrackGradient(@ColorInt colors: IntArray, orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT) {
        trackGradient = colors
        trackGradientOrientation = orientation
        invalidate()
    }

    fun setThumbGradient(@ColorInt colors: IntArray, orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT) {
        thumbGradient = colors
        thumbGradientOrientation = orientation
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

    fun startIntermittentAnimation(mode: IntermittentMode = intermittentMode) {
        cancelAllAnimations()

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

    fun setLoadingStarted() {
        if (isThumbHiddenForLoading) return

        isThumbHiddenForLoading = true
        isFinishPending = false
        animateThumbScale(from = thumbScale, to = 0f, duration = 200L)
    }

    fun setLoadingFinished() {
        if (!isThumbHiddenForLoading) return

        if (isProgressAnimating) {
            isFinishPending = true
            return
        }

        isThumbHiddenForLoading = false
        animateThumbScale(from = thumbScale, to = 1f, duration = 200L)
    }

    private fun updateBadgeCount() {
        val completed = (currentProgress / maxIndicatorProgress).toInt()
        badgeCount = completed
        showCompletedIndicator = completed > 0
    }

    private fun prepareProgressAnimation(start: () -> Unit) {
        if (!showThumb || isThumbHiddenForLoading) {
            start()
        } else {
            animateThumbScale(from = thumbScale, to = 0f, duration = 200L) { start() }
        }
    }

    private fun animateToTarget() {
        val current = currentProgress
        val target = indicatorProgress
        restartLapIndex = null

        if (current >= target) {
            animateProgressChange(target) { onReachedCurrentPendingTarget() }
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
            currentProgress = targetLap * maxIndicatorProgress
            updateBadgeCount()
            invalidate()
            animateRemainderAfterLapBoundary(targetLap, currentProgress)
        }
    }

    private fun onReachedCurrentPendingTarget() {
        if (currentProgress < indicatorProgress) {
            animateToTarget()
            return
        }

        if (isFinishPending) {
            isFinishPending = false
            isThumbHiddenForLoading = false
            animateThumbScale(from = thumbScale, to = 1f, duration = 200L)
        } else if (!isThumbHiddenForLoading) {
            animateThumbScale(from = thumbScale, to = 1f, duration = 200L)
        }
    }

    private fun animateRemainderAfterLapBoundary(targetLap: Int, lapEndProgress: Float) {
        val remainder = indicatorProgress % maxIndicatorProgress
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

    private fun animateProgressChange(target: Float, onEnd: (() -> Unit)? = null) {
        if (currentProgress == target) {
            onEnd?.invoke()
            return
        }

        animationTarget = target

        val animator = progressAnimator ?: ValueAnimator().apply {
            interpolator = EaseInterpolator.EaseInOutQubicInterpolator

            addUpdateListener { animation ->
                val value = (animation.animatedValue as Float).coerceIn(0f, progressLimit)

                delegate?.onAnimationUpdateListener(this@CoreLinearProgressBar, value, animationTarget)

                currentProgress = value
                updateBadgeCount()
                invalidate()
            }
        }.also { progressAnimator = it }

        animator.duration = progressAnimationDuration
        animator.setFloatValues(currentProgress, target)
        animator.removeAllListeners()
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                currentProgress = target
                updateBadgeCount()
                invalidate()
                onEnd?.invoke()
            }
        })
        animator.start()
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

    private fun animateThumbScale(from: Float, to: Float, duration: Long, onEnd: (() -> Unit)? = null) {
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

    private fun cancelAllAnimations() {
        removeCallbacks(startAnimationRunnable)

        progressAnimator.cancelSilently()
        lapAnimator.cancelSilently()
        thumbAnimator.cancelSilently()
        badgeAnimator.cancelSilently()

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

    private fun ValueAnimator?.cancelSilently() {
        this?.run {
            removeAllListeners()
            cancel()
        }
    }

    final override fun onFinishInflate() {
        super.onFinishInflate()
        xmlInitialProgress?.let { indicatorProgress = it }
        xmlInitialProgress = null
    }

    final override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancelAllAnimations()
    }

    final override fun setEnabled(enabled: Boolean) {
        if (enabled == isEnabled) return
        super.setEnabled(enabled)
        refreshBadgeBackground()
        invalidate()
    }

    final override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), computeContentHeight().toInt())
    }

    private fun computeContentHeight(): Float {
        val trackHeight = trackThickness + trackPadding * 2
        val thumbHeight = if (showThumb) thumbSize else 0f
        val badgeHeight = if (showBadge) {
            badge.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            badge.measuredHeight.toFloat()
        } else {
            0f
        }
        return maxOf(trackHeight, thumbHeight, badgeHeight)
    }

    final override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val top = (height - trackThickness) / 2f
        trackRect.set(
            paddingLeft + trackPadding,
            top,
            width - paddingRight - trackPadding,
            top + trackThickness
        )

        trackPaint.applyFill(trackRect, displayTrackColor, displayTrackGradient, trackGradientOrientation)
        canvas.drawRoundRect(trackRect, trackCornerRadius, trackCornerRadius, trackPaint)

        if (showInnerShadow) drawInnerShadow(canvas)
        if (showCompletedIndicator) drawCompletedIndicator(canvas)

        val clipRadius = minOf(
            trackCornerRadius,
            (trackRect.height() - indicatorPadding * 2f) / 2f
        ).coerceAtLeast(0f)

        clipPath.reset()
        clipPath.addRoundRect(
            trackRect.left + indicatorPadding,
            trackRect.top + indicatorPadding,
            trackRect.right - indicatorPadding,
            trackRect.bottom - indicatorPadding,
            clipRadius,
            clipRadius,
            Path.Direction.CW
        )

        canvas.withClip(clipPath) {
            if (isIntermittentRunning) {
                drawIntermittentIndicator(this)
            } else {
                drawIndicator(this)
            }
        }

        if (!isIntermittentRunning) {
            if (showThumb) drawThumb(canvas)
            if (showBadge && badgeCount > 0) drawBadge(canvas)
        }
    }

    private fun drawIndicator(canvas: Canvas) {
        val completedLaps = (currentProgress / maxIndicatorProgress).toInt()
        val startIndicatorWidth =
            if (showThumb && completedLaps == 0) thumbSize / 2f + context.dimen(R.dimen.dimen_1) else 0f

        val availableWidth = (trackRect.width() - indicatorPadding * 2 - startIndicatorWidth).coerceAtLeast(0f)
        val progressWidth = availableWidth * (displayProgress / maxIndicatorProgress)
        val indicatorWidth = startIndicatorWidth + progressWidth

        indicatorRect.set(
            trackRect.left + indicatorPadding,
            trackRect.top + indicatorPadding,
            (trackRect.left + indicatorPadding + indicatorWidth).coerceAtMost(trackRect.right - indicatorPadding),
            trackRect.bottom - indicatorPadding
        )

        drawIndicatorRect(canvas, indicatorRect, alpha = (255 * (1f - lapTransition)).toInt())
    }

    private fun drawIntermittentIndicator(canvas: Canvas) {
        val innerLeft = trackRect.left + indicatorPadding
        val innerRight = (trackRect.right - indicatorPadding).coerceAtLeast(innerLeft)
        val innerWidth = innerRight - innerLeft

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
        drawIndicatorRect(canvas, indicatorRect)
    }

    private fun drawIndicatorRect(canvas: Canvas, rect: RectF, alpha: Int = 255) {
        indicatorPaint.applyFill(rect, displayIndicatorColor, displayIndicatorGradient, indicatorGradientOrientation)
        indicatorPaint.alpha = alpha
        canvas.drawRoundRect(rect, trackCornerRadius, trackCornerRadius, indicatorPaint)
        indicatorPaint.alpha = 255
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

        thumbPaint.applyFill(thumbRect, displayThumbColor, displayThumbGradient, thumbGradientOrientation)
        canvas.drawRoundRect(
            thumbRect,
            thumbCornerRadius * thumbScale,
            thumbCornerRadius * thumbScale,
            thumbPaint
        )
    }

    private fun drawCompletedIndicator(canvas: Canvas) {
        completedRect.set(
            trackRect.left + indicatorPadding,
            trackRect.top + indicatorPadding,
            trackRect.right - indicatorPadding,
            trackRect.bottom - indicatorPadding
        )

        completedPaint.applyFill(completedRect, displayCompletedColor, displayCompletedGradient, completedGradientOrientation)
        val alpha = if (lapTransition > 0f) 1f else completedIndicatorAlpha
        completedPaint.alpha = (255 * alpha).toInt()

        canvas.drawRoundRect(completedRect, trackCornerRadius, trackCornerRadius, completedPaint)
    }

    private fun drawInnerShadow(canvas: Canvas) {
        canvas.save()

        // Paint inside the track
        innerShadowClipPath.reset()
        innerShadowClipPath.addRoundRect(trackRect, trackCornerRadius, trackCornerRadius, Path.Direction.CW)
        canvas.clipPath(innerShadowClipPath)

        innerShadowPaint.color = displayShadowColor

        // Outer rect
        val outerMargin = innerShadowBlur * 3f + max(abs(innerShadowOffsetX), abs(innerShadowOffsetY))
        outerShadowRect.set(
            trackRect.left - outerMargin,
            trackRect.top - outerMargin,
            trackRect.right + outerMargin,
            trackRect.bottom + outerMargin
        )

        // The track shape
        val spread = innerShadowSpread.coerceAtMost(trackRect.height() / 2f)
        holeRect.set(trackRect)
        holeRect.inset(spread, spread)
        val holeRadius = (trackCornerRadius - spread).coerceAtLeast(0f)

        // Inner shadow
        innerShadowDonutPath.reset()
        innerShadowDonutPath.fillType = Path.FillType.EVEN_ODD
        innerShadowDonutPath.addRect(outerShadowRect, Path.Direction.CW)
        innerShadowDonutPath.addRoundRect(holeRect, holeRadius, holeRadius, Path.Direction.CW)

        canvas.translate(innerShadowOffsetX, innerShadowOffsetY)
        canvas.drawPath(innerShadowDonutPath, innerShadowPaint)
        canvas.restore()
    }

    private fun drawBadge(canvas: Canvas) {
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

        canvas.scale(badgeScale, badgeScale, pivotX, pivotY)
        canvas.translate(badgeLeft.toFloat(), badgeTop.toFloat())

        badge.draw(canvas)
        canvas.restore()
    }

    private fun createDefaultBadge() = CoreTextBadge(context).apply {
        text = "x$badgeCount"
        setCoreShape(DefaultShape())
        iconVisible = false
        badgeTextAppearance = R.style.TextAppearance_Inter_Bold_TextBadge
        minimumHeight = context.dimen(R.dimen.s).toInt()
        doOnLayout { setCornerRadius(measuredHeight / 2f) }
    }

    private companion object {
        const val INTERMITTENT_FIXED_WIDTH_DURATION_MS = 2100L
        const val INTERMITTENT_STRETCH_DEFAULT_DURATION_MS = 1400L
    }
}