package id.co.edtslib.uikit.coachmark

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentActivity
import com.google.android.material.shape.OffsetEdgeTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.databinding.ViewCoachmarkUikitBinding
import id.co.edtslib.uikit.utils.deviceHeight
import id.co.edtslib.uikit.utils.deviceWidth
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.inflater
import id.co.edtslib.uikit.utils.interpolator.EaseInterpolator
import id.co.edtslib.uikit.utils.isDeviceStruggling
import kotlin.math.max
import androidx.core.graphics.toColorInt
import androidx.core.view.doOnPreDraw
import androidx.core.view.updateMargins

class CoachMarkOverlay @JvmOverloads constructor(
    rawContext: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(ContextThemeWrapper(rawContext, R.style.Theme_EDTS_UIKit), attrs, defStyle) {

    var coachMarkDelegate: CoachmarkDelegate? = null

    enum class SpotlightShape { CIRCLE, ROUNDED_RECTANGLE }

    enum class CoachMarkType { SINGLE, MULTIPLE }

    sealed class CoachMarkVerticalPosition {
        object Auto : CoachMarkVerticalPosition()

        /** Force the coachmark above the target spotlight. */
        object Above : CoachMarkVerticalPosition()

        /** Force the coachmark below the target spotlight. */
        object Below : CoachMarkVerticalPosition()

        /**
         * Pin the coachmark to an explicit Y coordinate on screen.
         * Useful for canvas-drawn targets, map pins, or any non-View target.
         *
         * @param y absolute Y position in window coordinates.
         */
        data class Absolute(val y: Float) : CoachMarkVerticalPosition()
    }


    private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#B3000000".toColorInt()
    }

    private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private var spotlightShape = SpotlightShape.ROUNDED_RECTANGLE
    private var targetRect: RectF? = null
    private var spotlightScale: Float = 1f
    private var coachMarkItems: List<CoachMarkData> = emptyList()
    private var currentCoachMarkIndex: Int = 0
    private var isDismissible = false
    private var verticalPosition: CoachMarkVerticalPosition = CoachMarkVerticalPosition.Auto
    private var verticalGap: Float = 8.dp

    private var coachmarkDefaultWidthPercent = 0.84f
        set(value) {
            field = value
            coachmarkContainer.updateLayoutParams<LayoutParams> {
                width = (context.deviceWidth * value).toInt()
            }
        }

    private var skipButtonVisibleByDefault = true
        set(value) {
            field = value
            coachmarkBinding?.btnSkip?.isVisible = value
        }

    private var stepProgressDivider = "/"

    private var coachMarkType = CoachMarkType.MULTIPLE

    internal var nextDefaultText = "Berikutnya"
    internal var closeDefaultText = "Tutup"
    internal var skipDefaultText = "Tutup"
        set(value) {
            field = value
            coachmarkBinding?.btnSkip?.text = value
        }

    /**
     * Nullable — null signals that a custom layout is active.
     * All internal usages guard with ?: return.
     */
    private var coachmarkBinding: ViewCoachmarkUikitBinding? = null

    /**
     * The single positioned element. Positioning logic only ever touches this container.
     * Content (default binding or custom layout) lives inside it.
     */
    private val coachmarkContainer: FrameLayout = FrameLayout(this.context).apply {
        layoutParams = LayoutParams(
            (context.deviceWidth * coachmarkDefaultWidthPercent).toInt(),
            LayoutParams.WRAP_CONTENT
        )
        alpha = 0f
    }.also { container ->
        coachmarkBinding = ViewCoachmarkUikitBinding
            .inflate(LayoutInflater.from(this.context), container, true)
            .apply { setOnButtonClickListener() }
    }


    init {
        addView(coachmarkContainer)
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    private fun ViewCoachmarkUikitBinding.setOnButtonClickListener() {
        btnNext.setOnClickListener {
            coachMarkDelegate?.onNextClickClickListener(currentCoachMarkIndex)
            showNextCoachMark()
        }
        btnSkip.setOnClickListener {
            coachMarkDelegate?.onSkipClickListener(currentCoachMarkIndex)
            dismiss {}
        }
        btnSingleAction.setOnClickListener {
            coachMarkDelegate?.onSkipClickListener(currentCoachMarkIndex)
            dismiss {}
        }
    }


    /**
     * Sets the coach mark items and resets to the first item.
     *
     * @param items list of [CoachMarkData] items.
     */
    fun setCoachMarkItems(items: List<CoachMarkData>) {
        coachMarkItems = items
        currentCoachMarkIndex = 0
        updateCoachMarkContent()
        updateCurrentTarget()
    }

    fun setCloseIconVisibility(isVisible: Boolean) {
        coachmarkBinding?.ctaClose?.isVisible = isVisible
    }

    fun setCoachMarkType(type: CoachMarkType) {
        val isSingle = type == CoachMarkType.SINGLE
        val visibility = if (isSingle) View.GONE else View.VISIBLE

        with(coachmarkBinding ?: return) {
            iconContainer.visibility = visibility
            divider.visibility = visibility
            btnSkip.visibility = visibility
            btnNext.visibility = visibility
            tvCoachmarkCount.visibility = visibility
            btnSingleAction.isVisible = isSingle
        }
    }

    fun setCoachmarkTitleTextAppearance(@StyleRes resId: Int) {
        coachmarkBinding?.tvTiTle?.setTextAppearance(resId)
    }

    fun setCoachmarkDescriptionTextAppearance(@StyleRes resId: Int) {
        coachmarkBinding?.tvDescription?.setTextAppearance(resId)
    }

    fun setCoachmarkWidthPercent(percent: Float) {
        coachmarkDefaultWidthPercent = percent
    }

    fun setStepProgressDivider(divider: String) {
        stepProgressDivider = divider
    }

    /**
     * Replaces the default coach mark card with a custom [View].
     *
     * Once called, [coachmarkBinding] is null and the default next/skip
     * buttons are no longer auto-wired. Use [navigationHandler] to drive
     * [showNextCoachMark] and [dismiss] from your own layout.
     *
     * @param view the custom view to display.
     * @param navigationHandler lambda receiving this overlay for manual button wiring.
     */
    fun setCustomLayout(
        view: View,
        navigationHandler: (CoachMarkOverlay.() -> Unit)? = null
    ) {
        coachmarkBinding = null
        coachmarkContainer.removeAllViews()
        coachmarkContainer.addView(view)
        navigationHandler?.invoke(this)
    }

    /**
     * Inflates a layout resource as the custom coach mark card.
     *
     * @param layoutRes the layout resource to inflate.
     * @param navigationHandler lambda receiving the inflated view and this overlay.
     * @return the inflated [View] for further configuration if needed.
     */
    fun setCustomLayout(
        @LayoutRes layoutRes: Int,
        navigationHandler: (view: View, overlay: CoachMarkOverlay) -> Unit = { _, _ -> }
    ): View {
        coachmarkBinding = null
        coachmarkContainer.removeAllViews()
        val inflated = LayoutInflater.from(context).inflate(layoutRes, coachmarkContainer, true)
        navigationHandler(inflated, this)
        return inflated
    }

    /**
     * Sets the container for this overlay.
     *
     * @param container the parent [ViewGroup] to attach the overlay to.
     */
    fun setContainer(
        container: ViewGroup = ((context as? FragmentActivity)?.window?.decorView as? ViewGroup)
            ?: throw IllegalArgumentException("Unable to retrieve container")
    ) {
        val params = ViewGroup.LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
        container.addView(this, params)
    }

    /**
     * Sets a [View] as the spotlight target.
     * Defers coordinate calculation via [doOnPreDraw] to guarantee
     * the view is fully measured and laid out.
     *
     * @param target the view to highlight.
     * @param shape the shape of the spotlight cutout.
     * @param padding optional padding around the target in px.
     */
    fun setTargetView(
        target: View,
        shape: SpotlightShape = SpotlightShape.ROUNDED_RECTANGLE,
        padding: Int = 8.dp.toInt()
    ) {
        this.spotlightShape = shape
        target.doOnPreDraw {
            targetRect = calculateTargetRect(target, padding)
            updateCoachmarkPosition(targetRect!!)
            startSpotlightAnimation()
        }
    }

    /**
     * Sets a raw [RectF] as the spotlight target.
     *
     * Use when the target has no [View] backing it — e.g. a canvas-drawn region,
     * a computed area, or a map pin projected to screen coordinates.
     *
     * Coordinates must be in window space relative to this overlay's position.
     * Unlike [setTargetView], no deferral occurs — the caller is responsible
     * for ensuring the rect is valid before calling this.
     *
     * @param rect the target area to highlight in window coordinates.
     * @param shape the shape of the spotlight cutout.
     */
    fun setTargetRect(
        rect: RectF,
        shape: SpotlightShape = SpotlightShape.ROUNDED_RECTANGLE
    ) {
        this.spotlightShape = shape
        this.targetRect = rect
        updateCoachmarkPosition(rect)
        startSpotlightAnimation()
    }

    /**
     * Explicitly controls where the coachmark card appears relative to the spotlight.
     * Defaults to [CoachMarkVerticalPosition.Auto].
     *
     * @param position the desired vertical positioning strategy.
     * @param gapDp gap in dp between the spotlight edge and the coachmark card.
     */
    fun setVerticalPosition(
        position: CoachMarkVerticalPosition,
        gapDp: Float = 8f
    ) {
        this.verticalPosition = position
        this.verticalGap = gapDp.dp
    }

    fun getCurrentCoachmarkIndex() = currentCoachMarkIndex

    @SuppressLint("SetTextI18n")
    private fun updateCoachMarkContent() {
        val binding = coachmarkBinding ?: return
        if (coachMarkItems.isEmpty()) return
        val currentItem = coachMarkItems[currentCoachMarkIndex]
        val isOnTheLastIndex = currentCoachMarkIndex == coachMarkItems.size - 1

        with(binding) {
            tvTiTle.text = currentItem.title
            tvDescription.text = currentItem.description
            tvCoachmarkCount.text = "${currentCoachMarkIndex + 1}$stepProgressDivider${coachMarkItems.size}"
            btnNext.text = if (isOnTheLastIndex) closeDefaultText else nextDefaultText
            btnSkip.text = skipDefaultText
            btnSingleAction.text = skipDefaultText
            btnSkip.isVisible = !isOnTheLastIndex && skipButtonVisibleByDefault
        }
    }

    /**
     * Updates the current target based on the active coach mark item.
     */
    private fun updateCurrentTarget() {
        if (coachMarkItems.isEmpty()) return
        setTargetView(coachMarkItems[currentCoachMarkIndex].target)
    }

    /**
     * Computes the coachmark container's translation values based on the target rectangle.
     * Respects [verticalPosition] for explicit overrides.
     *
     * @param rect the target spotlight rectangle.
     * @return Pair of (translationX, translationY).
     */
    private fun computeCoachmarkPosition(rect: RectF): Pair<Float, Float> {
        val screenHeight = context.deviceHeight.toFloat()
        val spaceAbove = rect.top
        val spaceBelow = screenHeight - rect.bottom

        val coachmarkHeight = coachmarkContainer.height
            .takeIf { it > 0 }
            ?: run {
                coachmarkContainer.measure(
                    MeasureSpec.makeMeasureSpec(
                        (context.deviceWidth * coachmarkDefaultWidthPercent).toInt(),
                        MeasureSpec.EXACTLY
                    ),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                )
                coachmarkContainer.measuredHeight
            }

        val fitsBelow = spaceBelow >= coachmarkHeight + verticalGap
        val fitsAbove = spaceAbove >= coachmarkHeight + verticalGap

        val vertical = when (val pos = verticalPosition) {
            is CoachMarkVerticalPosition.Auto -> when {
                fitsBelow && fitsAbove -> rect.bottom + verticalGap
                fitsBelow             -> rect.bottom + verticalGap
                fitsAbove             -> rect.top - coachmarkHeight - verticalGap

                else -> if (spaceBelow > spaceAbove)
                    (rect.bottom + verticalGap).coerceAtMost(screenHeight - coachmarkHeight - verticalGap)
                else
                    (rect.top - coachmarkHeight - verticalGap).coerceAtLeast(verticalGap)
            }
            is CoachMarkVerticalPosition.Below ->
                (rect.bottom + verticalGap)
                    .coerceAtMost(screenHeight - coachmarkHeight - verticalGap)

            is CoachMarkVerticalPosition.Above ->
                (rect.top - coachmarkHeight - verticalGap)
                    .coerceAtLeast(verticalGap)

            is CoachMarkVerticalPosition.Absolute -> pos.y
        }

        val overlayWidth = this.width.toFloat()
        val targetCenterX = rect.centerX()
        val coachmarkWidth = coachmarkContainer.width.toFloat()
        val padding = 8.dp

        if (coachmarkWidth <= 0 || overlayWidth <= 0) {
            val fallbackWidth = context.deviceWidth * coachmarkDefaultWidthPercent
            return Pair((overlayWidth - fallbackWidth) / 2f, vertical)
        }

        val centeredPosition = targetCenterX - coachmarkWidth / 2f
        val minBound = padding
        val maxBound = (overlayWidth - coachmarkWidth - padding).coerceAtLeast(minBound)
        val horizontal = centeredPosition.coerceIn(minBound, maxBound)

        return Pair(horizontal, vertical)
    }

    /**
     * Updates the coach mark container position relative to the target rectangle.
     *
     * @param rect the target rectangle.
     */
    private fun updateCoachmarkPosition(rect: RectF) {
        coachmarkContainer.post {
            if (coachmarkContainer.width <= 0 || coachmarkContainer.height <= 0) {
                coachmarkContainer.measure(
                    MeasureSpec.makeMeasureSpec(
                        (context.deviceWidth * coachmarkDefaultWidthPercent).toInt(),
                        MeasureSpec.EXACTLY
                    ),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                )
            }

            val (newX, newY) = computeCoachmarkPosition(rect)
            coachmarkContainer.translationX = newX
            coachmarkContainer.translationY = newY

            val edgeIsAtTop = resolveEdgeIsAtTop(rect, newY)
            updateCoachMarkShapeAppearanceEdge(rect.centerX(), edgeIsAtTop)
        }
    }

    /**
     * Resolves triangle pointer direction based on [verticalPosition] and computed Y.
     *
     * @param rect the target spotlight rectangle.
     * @param finalY the computed vertical translation of the coachmark card.
     * @return true if the triangle should point upward (card is below target).
     */
    private fun resolveEdgeIsAtTop(rect: RectF, finalY: Float): Boolean {
        return when (verticalPosition) {
            is CoachMarkVerticalPosition.Above   -> false
            is CoachMarkVerticalPosition.Below   -> true
            is CoachMarkVerticalPosition.Absolute -> finalY > rect.centerY()
            is CoachMarkVerticalPosition.Auto    -> finalY > rect.bottom
        }
    }

    /**
     * Updates the triangular pointer edge of the coach mark card.
     *
     * @param targetCenterX the horizontal center of the target spotlight.
     * @param isEdgeAtTop true if the triangle points upward (card below target).
     * @param cardLeft optional override for card left position (used during animation).
     */
    private fun updateCoachMarkShapeAppearanceEdge(
        targetCenterX: Float,
        isEdgeAtTop: Boolean,
        cardLeft: Float? = null
    ) {
        coachmarkBinding?.cardContainer?.post {
            val actualCardLeft = cardLeft ?: coachmarkContainer.translationX
            val cardRight = actualCardLeft + coachmarkContainer.width
            val cardCenterX = (actualCardLeft + cardRight) / 2f
            val offsetFromCardCenter = targetCenterX - cardCenterX

            val verticalBasedOffset = if (isEdgeAtTop) -offsetFromCardCenter else offsetFromCardCenter

            val cardWidth = coachmarkBinding?.cardContainer?.width?.toFloat() ?: return@post
            if (cardWidth <= 0f) return@post

            val maxOffset = (cardWidth / 2f - 16.dp).coerceAtLeast(0f)
            val clampedOffset = verticalBasedOffset.coerceIn(-maxOffset, maxOffset)

            val markerEdgeTreatment = RoundTipTriangleEdgeTreatment(12.dp, 8.dp, (1.5).toInt().dp, isEdgeAtTop)
            val offsetEdgeTreatment = OffsetEdgeTreatment(markerEdgeTreatment, clampedOffset)

            coachmarkBinding?.cardContainer?.shapeAppearanceModel =
                ShapeAppearanceModel().toBuilder()
                    .setAllCornerSizes(8.dp)
                    .apply {
                        if (isEdgeAtTop) setTopEdge(offsetEdgeTreatment)
                        else setBottomEdge(offsetEdgeTreatment)
                    }
                    .build()
        }
    }

    /**
     * Transitions to a new target with a morphing animation.
     *
     * @param newTarget the next target view.
     * @param onTransitionEnd callback when the transition ends.
     */
    private fun transitionToTarget(newTarget: View, onTransitionEnd: () -> Unit) {
        val newRect = calculateTargetRect(newTarget)
        val currentRect = targetRect ?: newRect

        val (currentX, currentY) = computeCoachmarkPosition(currentRect)
        val (finalX, finalY) = computeCoachmarkPosition(newRect)

        val targetCenterX = newRect.centerX()
        val edgeIsAtTop = resolveEdgeIsAtTop(newRect, finalY)

        updateCoachMarkShapeAppearanceEdge(targetCenterX, edgeIsAtTop, finalX)

        currentCoachMarkIndex++
        updateCoachMarkContent()

        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = SPOTLIGHT_ENTER_EXIT_DURATION
            interpolator = EaseInterpolator.EaseInOutQubicInterpolator
            addUpdateListener { animation ->
                val fraction = animation.animatedFraction
                targetRect?.set(
                    lerp(currentRect.left, newRect.left, fraction),
                    lerp(currentRect.top, newRect.top, fraction),
                    lerp(currentRect.right, newRect.right, fraction),
                    lerp(currentRect.bottom, newRect.bottom, fraction)
                )
                spotlightScale = 1f
                coachmarkContainer.translationX = lerp(currentX, finalX, fraction)
                coachmarkContainer.translationY = lerp(currentY, finalY, fraction)
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onTransitionEnd()
                }
            })
            start()
        }
    }

    /**
     * Shows the next coach mark with a morphing transition.
     * Dismisses the overlay if already on the last item.
     */
    fun showNextCoachMark() {
        if (currentCoachMarkIndex < coachMarkItems.size - 1) {
            val nextTarget = coachMarkItems[currentCoachMarkIndex + 1].target
            transitionToTarget(nextTarget) {}
        } else {
            dismiss {}
        }
    }

    /**
     * Animates the entrance of the spotlight and fades in the coachmark card.
     */
    fun startSpotlightAnimation() {
        val spotlightAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = SPOTLIGHT_ENTER_EXIT_DURATION
            interpolator = EaseInterpolator.EaseOutQubicInterpolator
            addUpdateListener { animation ->
                spotlightScale = animation.animatedValue as Float
                invalidate()
            }
        }
        val coachmarkAlphaAnimator = ObjectAnimator.ofFloat(coachmarkContainer, ALPHA, 1f).apply {
            duration = COACHMARK_ENTER_DURATION
            interpolator = EaseInterpolator.EaseOutQubicInterpolator
        }
        AnimatorSet().apply {
            playTogether(spotlightAnimator, coachmarkAlphaAnimator)
            start()
        }
    }

    /**
     * Dismisses the overlay — fades out the card and collapses the spotlight simultaneously.
     * Removes the overlay from the window when both are complete.
     *
     * @param onAnimationEnd callback invoked after the overlay is hidden.
     */
    fun dismiss(onAnimationEnd: () -> Unit) {
        val cardFadeOut = ObjectAnimator.ofFloat(coachmarkContainer, ALPHA, 0f).apply {
            duration = COACHMARK_EXIT_DURATION
            interpolator = EaseInterpolator.EaseInQubicInterpolator
        }
        val spotlightCollapse = ValueAnimator.ofFloat(spotlightScale, 0f).apply {
            duration = SPOTLIGHT_ENTER_EXIT_DURATION
            interpolator = EaseInterpolator.EaseInQubicInterpolator
            addUpdateListener { animation ->
                spotlightScale = animation.animatedValue as Float
                invalidate()
            }
        }
        AnimatorSet().apply {
            playTogether(cardFadeOut, spotlightCollapse)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    this@CoachMarkOverlay.isVisible = false
                    coachMarkDelegate?.onDismissListener()
                    onAnimationEnd()
                }
            })
            start()
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        val saveCount = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
        targetRect?.let { rect ->
            when (spotlightShape) {
                SpotlightShape.CIRCLE -> {
                    val cx = rect.centerX()
                    val cy = rect.centerY()
                    val baseRadius = max(rect.width(), rect.height()) / 2f
                    val radius = baseRadius * spotlightScale
                    canvas.drawCircle(cx, cy, radius, clearPaint)
                }
                SpotlightShape.ROUNDED_RECTANGLE -> {
                    val scaledRect = RectF(
                        rect.left + (rect.width() * (1 - spotlightScale) / 2),
                        rect.top + (rect.height() * (1 - spotlightScale) / 2),
                        rect.right - (rect.width() * (1 - spotlightScale) / 2),
                        rect.bottom - (rect.height() * (1 - spotlightScale) / 2)
                    )
                    canvas.drawRoundRect(scaledRect, 24f, 24f, clearPaint)
                }
            }
        }
        saveCount.let { canvas.restoreToCount(it) }
        super.dispatchDraw(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean = true

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isFocusableInTouchMode = true
        requestFocus()
        setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                if (isDismissible) dismiss {}
                return@setOnKeyListener true
            }
            false
        }
    }

    private fun lerp(start: Float, end: Float, fraction: Float): Float =
        start + fraction * (end - start)

    /**
     * Calculates the spotlight rectangle for the given [View] relative to this overlay.
     *
     * @param target the target view.
     * @param padding optional padding in px.
     * @return calculated [RectF] in overlay-relative window coordinates.
     */
    private fun calculateTargetRect(target: View, padding: Int = 4.dp.toInt()): RectF {
        val targetLocation = IntArray(2)
        target.getLocationInWindow(targetLocation)
        val overlayLocation = IntArray(2)
        this.getLocationInWindow(overlayLocation)
        val relativeX = targetLocation[0] - overlayLocation[0]
        val relativeY = targetLocation[1] - overlayLocation[1]
        return RectF(
            relativeX.toFloat() - padding,
            relativeY.toFloat() - padding,
            (relativeX + target.width).toFloat() + padding,
            (relativeY + target.height).toFloat() + padding
        )
    }

    companion object {
        private const val COACHMARK_ENTER_DURATION = 500L
        private const val COACHMARK_EXIT_DURATION = 300L
        private const val SPOTLIGHT_ENTER_EXIT_DURATION = 500L
    }

    /**
     * Builder for creating and configuring a [CoachMarkOverlay].
     */
    class Builder(private val context: FragmentActivity) {
        private var coachMarkItems: List<CoachMarkData> = emptyList()
        private var dismissibleOnBack: Boolean = false
        private var container: ViewGroup? = context.window?.decorView as? ViewGroup
        private var delegate: CoachmarkDelegate? = null

        @StyleRes private var titleTextAppearance: Int = R.style.TextAppearance_Inter_Semibold_H1
        @StyleRes private var descriptionTextAppearance: Int = R.style.TextAppearance_Inter_Regular_P2

        private var coachmarkWidthPercent: Float = 0.84f
        private var skipButtonVisibleByDefault: Boolean = true
        private var stepProgressDivider: String = "/"
        private var skipText: String = "Tutup"
        private var nextText: String = "Berikutnya"
        private var verticalPosition: CoachMarkVerticalPosition = CoachMarkVerticalPosition.Auto
        private var verticalGap: Float = 8f

        private var closeIconVisible = false

        private var coachMarkType = CoachMarkType.MULTIPLE

        private var customView: View? = null
        private var customViewNavigationHandler: (CoachMarkOverlay.() -> Unit)? = null
        private var customLayoutRes: Int? = null
        private var customResNavigationHandler: ((View, CoachMarkOverlay) -> Unit)? = null

        fun setDismissibleOnBack(isDismissible: Boolean) = apply {
            this.dismissibleOnBack = isDismissible
        }

        fun setCoachMarkItems(items: List<CoachMarkData>) = apply {
            this.coachMarkItems = items
        }

        fun setCoachMarkItems(vararg items: CoachMarkData) = apply {
            this.coachMarkItems = items.toList()
        }

        fun setContainer(container: ViewGroup) = apply {
            this.container = container
        }

        fun setCoachMarkDelegate(delegate: CoachmarkDelegate) = apply {
            this.delegate = delegate
        }

        fun setCoachmarkTitleTextAppearance(@StyleRes resId: Int) = apply {
            this.titleTextAppearance = resId
        }

        fun setCoachmarkDescriptionTextAppearance(@StyleRes resId: Int) = apply {
            this.descriptionTextAppearance = resId
        }

        fun setCoachmarkWidthPercent(percent: Float) = apply {
            this.coachmarkWidthPercent = percent
        }

        fun setCoachmarkSkipText(text: String) = apply {
            this.skipText = text
        }

        fun setCoachmarkNextText(text: String) = apply {
            this.nextText = text
        }

        fun setSkipButtonVisibleByDefault(isVisible: Boolean) = apply {
            this.skipButtonVisibleByDefault = isVisible
        }

        fun setStepProgressDivider(divider: String) = apply {
            this.stepProgressDivider = divider
        }

        fun setCloseIconVisible(isVisible: Boolean) = apply {
            this.closeIconVisible = isVisible
        }

        fun setCoachMarkType(type: CoachMarkType) = apply {
            this.coachMarkType = type
        }

        /**
         * Sets the vertical positioning strategy for the coachmark card.
         *
         * @param position the desired [CoachMarkVerticalPosition].
         * @param gapDp gap in dp between the spotlight edge and the coachmark card.
         */
        fun setVerticalPosition(
            position: CoachMarkVerticalPosition,
            gapDp: Float = 8f
        ) = apply {
            this.verticalPosition = position
            this.verticalGap = gapDp
        }

        /**
         * Replaces the default coach mark layout with a custom [View].
         * Mutually exclusive with [setCustomLayout] (layoutRes variant) — last call wins.
         *
         * @param view custom view to display inside the coachmark card.
         * @param navigationHandler lambda receiving the overlay for manual button wiring.
         */
        fun setCustomLayout(
            view: View,
            navigationHandler: (CoachMarkOverlay.() -> Unit)? = null
        ) = apply {
            this.customView = view
            this.customViewNavigationHandler = navigationHandler
            this.customLayoutRes = null
        }

        /**
         * Inflates a layout resource as the custom coach mark card.
         * Mutually exclusive with [setCustomLayout] (View variant) — last call wins.
         *
         * @param layoutRes layout resource ID to inflate.
         * @param navigationHandler lambda receiving the inflated view and overlay for button wiring.
         */
        fun setCustomLayout(
            @LayoutRes layoutRes: Int,
            navigationHandler: (View, CoachMarkOverlay) -> Unit = { _, _ -> }
        ) = apply {
            this.customLayoutRes = layoutRes
            this.customResNavigationHandler = navigationHandler
            this.customView = null
        }

        fun build(): CoachMarkOverlay {
            val overlay = CoachMarkOverlay(context)

            overlay.coachMarkDelegate = delegate
            overlay.setCoachmarkTitleTextAppearance(titleTextAppearance)
            overlay.setCoachmarkDescriptionTextAppearance(descriptionTextAppearance)
            overlay.setCoachmarkWidthPercent(coachmarkWidthPercent)
            overlay.setStepProgressDivider(stepProgressDivider)
            overlay.setCoachMarkType(coachMarkType)
            overlay.setCloseIconVisibility(closeIconVisible)
            overlay.skipButtonVisibleByDefault = skipButtonVisibleByDefault
            overlay.skipDefaultText = skipText
            overlay.nextDefaultText = nextText
            overlay.setVerticalPosition(verticalPosition, verticalGap)
            overlay.isDismissible = dismissibleOnBack

            customView?.let { overlay.setCustomLayout(it, customViewNavigationHandler) }
            customLayoutRes?.let { overlay.setCustomLayout(it, customResNavigationHandler ?: { _, _ -> }) }

            overlay.setCoachMarkItems(coachMarkItems)

            container?.let { overlay.setContainer(it) }

            return overlay
        }
    }
}




