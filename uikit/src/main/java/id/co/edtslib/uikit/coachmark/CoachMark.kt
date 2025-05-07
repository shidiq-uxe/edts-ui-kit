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
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import androidx.appcompat.view.ContextThemeWrapper
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

class CoachMarkOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(ContextThemeWrapper(context, R.style.Theme_EDTS_UIKit), attrs, defStyle) {

    var coachMarkDelegate: CoachmarkDelegate? = null

    enum class SpotlightShape { CIRCLE, ROUNDED_RECTANGLE }
    private var spotlightShape = SpotlightShape.CIRCLE

    private var targetRect: RectF? = null

    private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#B3000000".toColorInt()
    }

    private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private var spotlightScale: Float = 1f

    private var coachMarkItems: List<CoachMarkData> = emptyList()
    private var currentCoachMarkIndex: Int = 0

    private var isDismissible = false

    private val coachmarkBinding: ViewCoachmarkUikitBinding =
        ViewCoachmarkUikitBinding.inflate(LayoutInflater.from(this.context), this, false).apply {
            setOnButtonClickListener()
        }

    private val coachmarkView: View = coachmarkBinding.root.apply {
        updateLayoutParams<LayoutParams> {
            width = (context.deviceWidth * 0.84).toInt()
        }
        alpha = 0f
    }

    enum class CoachMarkHorizontalGravity { START, CENTER, END }

    init {
        addView(coachmarkView)
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    private fun ViewCoachmarkUikitBinding.setOnButtonClickListener() {
        btnNext.setOnClickListener {
            coachMarkDelegate?.onNextClickClickListener()
            showNextCoachMark()
        }
        btnSkip.setOnClickListener {
            coachMarkDelegate?.onSkipClickListener()
            dismiss {}
        }
    }

    /**
     * Sets the coach mark items.
     *
     * @param items vararg list of [CoachMarkData] items.
     */
    fun setCoachMarkItems(vararg items: CoachMarkData) {
        setCoachMarkItems(items.toList())
    }

    /**
     * Sets the coach mark items.
     *
     * @param items list of [CoachMarkData] items.
     */
    fun setCoachMarkItems(items: List<CoachMarkData>) {
        coachMarkItems = items
        currentCoachMarkIndex = 0
        updateCoachMarkContent()
        updateCurrentTarget()
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

    @SuppressLint("SetTextI18n")
    private fun updateCoachMarkContent() {
        if (coachMarkItems.isEmpty()) return
        val currentItem = coachMarkItems[currentCoachMarkIndex]

        val isOnTheLastIndex = (currentCoachMarkIndex == coachMarkItems.size.minus(1))

        with(coachmarkBinding) {
            tvTiTle.text = currentItem.title
            tvDescription.text = currentItem.description
            tvCoachmarkCount.text = "${currentCoachMarkIndex.plus(1)}/${coachMarkItems.size}"
            btnNext.text = if (isOnTheLastIndex) "Tutup" else "Berikutnya"
            btnSkip.isVisible = !isOnTheLastIndex
        }
    }

    /**
     * Updates the current target based on the coach mark items.
     */
    private fun updateCurrentTarget() {
        if (coachMarkItems.isEmpty()) return
        val newTarget = coachMarkItems[currentCoachMarkIndex].target
        setTargetView(newTarget)
    }

    /**
     * Sets the target view to be highlighted.
     *
     * @param target The view to highlight.
     * @param shape The shape of the spotlight.
     * @param padding Optional padding around the target.
     */
    fun setTargetView(
        target: View,
        shape: SpotlightShape = SpotlightShape.ROUNDED_RECTANGLE,
        padding: Int = 4.dp.toInt()
    ) {
        this.spotlightShape = shape
        targetRect = calculateTargetRect(target, padding)
        updateCoachmarkPosition(targetRect!!)
        startSpotlightAnimation()
    }

    /**
     * Computes the coachmark's horizontal and vertical translation values based on the given target rectangle.
     *
     * @param rect the target rectangle.
     * @return a Pair where first is the horizontal translation (translationX) and second is the vertical translation (translationY).
     */
    private fun computeCoachmarkPosition(rect: RectF): Pair<Float, Float> {
        val screenHeight = context.deviceHeight.toFloat()
        val spaceAbove = rect.top
        val spaceBelow = screenHeight - rect.bottom
        val vertical = if (spaceBelow > spaceAbove) {
            rect.bottom + 8.dp
        } else {
            rect.top - coachmarkView.height - 8.dp
        }

        val overlayWidth = this.width.toFloat()
        val targetCenterX = rect.centerX()

        // Calculate a centered horizontal position then clamp it between left and right bounds
        val centeredPosition = targetCenterX - coachmarkView.width / 2f
        val horizontal = centeredPosition.coerceIn(8.dp, overlayWidth - coachmarkView.width - 8.dp)

        return Pair(horizontal, vertical)
    }


    private fun updateCoachMarkShapeAppearanceEdge(
        gravity: CoachMarkHorizontalGravity,
        isEdgeAtTop: Boolean
    ) {
        val placement = when (gravity) {
            CoachMarkHorizontalGravity.START -> coachmarkBinding.cardContainer.width.div(2).minus(20.dp).toFloat()
            CoachMarkHorizontalGravity.CENTER -> 0f
            CoachMarkHorizontalGravity.END -> -coachmarkBinding.cardContainer.width.div(2).minus(20.dp).toFloat()
        }

        val markerEdgeTreatment = RoundTipTriangleEdgeTreatment(12.dp, 8.dp, (1.5).toInt().dp, isEdgeAtTop)
        val offsetEdgeTreatment = OffsetEdgeTreatment(markerEdgeTreatment, if (isEdgeAtTop) placement else -placement)

        coachmarkBinding.cardContainer.shapeAppearanceModel = offsetEdgeTreatment.let {
            ShapeAppearanceModel().toBuilder()
                .setAllCornerSizes(8.dp)
                .apply {
                    if (isEdgeAtTop) setTopEdge(it)
                    else setBottomEdge(it)
                }
                .build()
        }
    }

    /**
     * Updates the coach mark position (both vertical and horizontal) relative to the target rectangle.
     *
     * @param rect the target rectangle.
     */
    private fun updateCoachmarkPosition(rect: RectF) {
        coachmarkView.post {
            val (newX, newY) = computeCoachmarkPosition(rect)
            coachmarkView.translationX = newX
            coachmarkView.translationY = newY

            val targetCenterX = rect.centerX()
            val screenHeight = context.deviceHeight
            val spaceBelow = screenHeight - rect.bottom
            val spaceAbove = rect.top

            val edgeIsAtTop = spaceBelow > spaceAbove

            val horizontalGravity =  when {
                targetCenterX < this.width / 3f -> CoachMarkHorizontalGravity.START
                targetCenterX > this.width * 2 / 3f -> CoachMarkHorizontalGravity.END
                else -> CoachMarkHorizontalGravity.CENTER
            }

            updateCoachMarkShapeAppearanceEdge(horizontalGravity, edgeIsAtTop)
        }
    }


    /**
     * Transitions to a new target view with a morphing effect.
     *
     * @param newTarget the new target view.
     * @param onTransitionEnd callback when the transition ends.
     */
    private fun transitionToTarget(newTarget: View, onTransitionEnd: () -> Unit) {
        val newRect = calculateTargetRect(newTarget)
        val currentRect = targetRect ?: newRect

        val (currentX, currentY) = computeCoachmarkPosition(currentRect)
        val (finalX, finalY) = computeCoachmarkPosition(newRect)

        val targetCenterX = newRect.centerX()
        val isEdgeAtTop = finalY > newRect.bottom
        val gravity = when {
            targetCenterX < width / 3f -> CoachMarkHorizontalGravity.START
            targetCenterX > width * 2 / 3f -> CoachMarkHorizontalGravity.END
            else -> CoachMarkHorizontalGravity.CENTER
        }

        updateCoachMarkShapeAppearanceEdge(gravity, isEdgeAtTop)

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

                coachmarkView.translationX = lerp(currentX, finalX, fraction)
                coachmarkView.translationY = lerp(currentY, finalY, fraction)

                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) = onTransitionEnd()
            })
            start()
        }
    }



    /**
     * Shows the next coach mark item with a morphing transition.
     */
    fun showNextCoachMark() {
        if (currentCoachMarkIndex < coachMarkItems.size - 1) {
            val nextTarget = coachMarkItems[currentCoachMarkIndex + 1].target
            transitionToTarget(nextTarget) {
                coachMarkDelegate?.onNextClickClickListener()
            }
        } else {
            dismiss {}
        }
    }

    /**
     * Fades out the overlay.
     *
     * @param onAnimationEnd callback when fade out is finished.
     */
    fun fadeOutAndRemove(onAnimationEnd: () -> Unit) {
        coachmarkView.animate().alpha(0f)
            .setDuration(COACHMARK_EXIT_DURATION)
            .setInterpolator(EaseInterpolator.EaseInQubicInterpolator)
            .withEndAction { onAnimationEnd() }
            .start()
    }

    override fun dispatchDraw(canvas: Canvas?) {
        val saveCount = canvas?.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
        canvas?.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
        targetRect?.let { rect ->
            when (spotlightShape) {
                SpotlightShape.CIRCLE -> {
                    val cx = rect.centerX()
                    val cy = rect.centerY()
                    val baseRadius = max(rect.width(), rect.height()) / 2f
                    val radius = baseRadius * spotlightScale
                    canvas?.drawCircle(cx, cy, radius, clearPaint)
                }
                SpotlightShape.ROUNDED_RECTANGLE -> {
                    val scaledRect = RectF(
                        rect.left + (rect.width() * (1 - spotlightScale) / 2),
                        rect.top + (rect.height() * (1 - spotlightScale) / 2),
                        rect.right - (rect.width() * (1 - spotlightScale) / 2),
                        rect.bottom - (rect.height() * (1 - spotlightScale) / 2)
                    )
                    canvas?.drawRoundRect(scaledRect, 16f, 16f, clearPaint)
                }
            }
        }
        saveCount?.let { canvas.restoreToCount(it) }
        super.dispatchDraw(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isFocusableInTouchMode = true
        requestFocus()

        setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                if (!isDismissible) {
                    return@setOnKeyListener true
                } else {
                    dismiss {}
                    return@setOnKeyListener true
                }
            }
            false
        }
    }


    /**
     * Animates the entrance of the spotlight and fades in the coach mark.
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
        val coachmarkAlphaAnimator = ObjectAnimator.ofFloat(coachmarkView, ALPHA, 1f).apply {
            duration = COACHMARK_ENTER_DURATION
            interpolator = EaseInterpolator.EaseOutQubicInterpolator
        }
        AnimatorSet().apply {
            playTogether(spotlightAnimator, coachmarkAlphaAnimator)
            start()
        }
    }

    /**
     * Animates the exit of the spotlight and removes the overlay.
     *
     * @param onAnimationEnd callback when animation is finished.
     */
    fun dismiss(onAnimationEnd: () -> Unit) {
        fadeOutAndRemove { }
        ValueAnimator.ofFloat(spotlightScale, 0f).apply {
            duration = SPOTLIGHT_ENTER_EXIT_DURATION
            interpolator = EaseInterpolator.EaseInQubicInterpolator
            addUpdateListener { animation ->
                spotlightScale = animation.animatedValue as Float
                invalidate()
            }
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

    private fun lerp(start: Float, end: Float, fraction: Float): Float =
        start + fraction * (end - start)

    /**
     * Calculates the target rectangle for the specified view.
     *
     * @param target the target view.
     * @param padding optional padding to apply.
     * @return the calculated [RectF].
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
     * Builder class for creating and configuring a [CoachMarkOverlay].
     */
    class Builder(private val context: FragmentActivity) {
        private var coachMarkItems: List<CoachMarkData> = emptyList()
        private var dismissibleOnBack: Boolean = false
        private var container: ViewGroup? = context.window?.decorView as? ViewGroup
        private var delegate: CoachmarkDelegate? = null

        fun setDismissibleOnBack(isDismissible: Boolean) = apply {
            this.dismissibleOnBack = isDismissible
        }

        /**
         * Sets the coach mark items.
         *
         * @param items list of [CoachMarkData].
         */
        fun setCoachMarkItems(items: List<CoachMarkData>) = apply {
            this.coachMarkItems = items
        }

        /**
         * Sets the coach mark items.
         *
         * @param items vararg of [CoachMarkData].
         */
        fun setCoachMarkItems(vararg items: CoachMarkData) = apply {
            this.coachMarkItems = items.toList()
        }

        /**
         * Sets the container for the overlay.
         *
         * @param container the parent [ViewGroup].
         */
        fun setContainer(container: ViewGroup) = apply {
            this.container = container
        }

        /**
         * Sets the delegate for coach mark actions.
         *
         * @param delegate the [CoachmarkDelegate].
         */
        fun setCoachMarkDelegate(delegate: CoachmarkDelegate) = apply {
            this.delegate = delegate
        }

        fun build(): CoachMarkOverlay {
            val overlay = CoachMarkOverlay(context)
            overlay.coachMarkDelegate = delegate
            overlay.setCoachMarkItems(coachMarkItems)
            container?.let { overlay.setContainer(it) }
            overlay.isDismissible = dismissibleOnBack
            return overlay
        }
    }
}



