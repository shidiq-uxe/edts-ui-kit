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
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import id.co.edtslib.uikit.databinding.ViewCoachmarkBinding
import id.co.edtslib.uikit.utils.deviceHeight
import id.co.edtslib.uikit.utils.deviceWidth
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.inflater
import id.co.edtslib.uikit.utils.interpolator.EaseInterpolator
import kotlin.math.max

class CoachMarkOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    var coachMarkDelegate: CoachmarkDelegate? = null

    enum class SpotlightShape { CIRCLE, ROUNDED_RECTANGLE }
    private var spotlightShape = SpotlightShape.CIRCLE

    private var targetRect: RectF? = null

    private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B3000000")
    }

    private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    // Used to optionally animate a scale effect on the spotlight if desired.
    private var spotlightScale: Float = 1f

    private var coachMarkItems: List<CoachMarkData> = emptyList()
    private var currentCoachMarkIndex: Int = 0

    private val coachmarkBinding: ViewCoachmarkBinding = ViewCoachmarkBinding.inflate(context.inflater, this, false).apply {
        this.setOnButtonClickListener()
    }

    private val coachmarkView: View = coachmarkBinding.root.apply {
        this.updateLayoutParams<LayoutParams> {
            width = (context.deviceWidth * 0.84).toInt()
        }
        this.alpha = 1f
    }

    init {
        addView(coachmarkView)
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    private fun ViewCoachmarkBinding.setOnButtonClickListener() {
        this.btnNext.setOnClickListener {
            coachMarkDelegate?.onNextClickClickListener()
            showNextCoachMark()
        }
        this.btnSkip.setOnClickListener {
            coachMarkDelegate?.onSkipClickListener()
        }
    }

    /**
     * Set the coachmark items.
     */
    fun setCoachMarkItems(vararg items: CoachMarkData) {
        coachMarkItems = items.toList()
        currentCoachMarkIndex = 0
        updateCoachMarkContent()
        updateCurrentTarget()
    }

    /**
     * Set the coachmark items.
     */
    fun setCoachMarkItems(items: List<CoachMarkData>) {
        coachMarkItems = items
        currentCoachMarkIndex = 0
        updateCoachMarkContent()
        updateCurrentTarget()
    }

    @SuppressLint("SetTextI18n")
    private fun updateCoachMarkContent() {
        if (coachMarkItems.isEmpty()) return
        val currentItem = coachMarkItems[currentCoachMarkIndex]
        coachmarkBinding.tvTiTle.text = currentItem.title
        coachmarkBinding.tvDescription.text = currentItem.description
        coachmarkBinding.tvCoachmarkCount.text = "${currentCoachMarkIndex.plus(1)}/${coachMarkItems.size}"

        coachmarkBinding.btnNext.text =
            if (currentCoachMarkIndex == coachMarkItems.size - 1) "Tutup" else "Berikutnya"
    }

    /**
     * Update the current target based on the coachMarkItems list.
     */
    private fun updateCurrentTarget() {
        if (coachMarkItems.isEmpty()) return
        val newTarget = coachMarkItems[currentCoachMarkIndex].target
        // Immediately set the initial target without animation.
        setTargetView(newTarget)
    }

    /**
     * Set the target view to be highlighted.
     * @param target The view to highlight.
     * @param shape The shape of the spotlight (circle or rounded rectangle).
     * @param padding Optional extra padding around the target.
     */
    fun setTargetView(
        target: View,
        shape: SpotlightShape = SpotlightShape.ROUNDED_RECTANGLE,
        padding: Int = 4.dp.toInt()
    ) {
        this.spotlightShape = shape
        targetRect = calculateTargetRect(target, padding)
        // Position the coachmark immediately for the first target.
        updateCoachmarkPosition(targetRect!!)
        // Start the entrance animation for the spotlight.
        startSpotlightAnimation()
    }

    val tooltipEdgeTreatment = NotchTriangleEdgeTreatment(
        triangleWidth = 12.dp,
        triangleHeight = 8.dp,
        triangleOffset = 24.dp,
        roundedCornerRadius = 2.dp,
        isEdgeAtTop = false
    )

    /**
     * Update the coachmark position relative to the given target rect.
     * Instead of instantly setting the position, this method computes the top margin.
     */
    private fun updateCoachmarkPosition(rect: RectF) {
        val screenHeight = context.deviceHeight
        val spaceAbove = rect.top
        val spaceBelow = screenHeight - rect.bottom
        val newMargin = if (spaceBelow > spaceAbove) {
            // Position below the target.
            rect.bottom.toInt() + 16.dp.toInt()
        } else {
            // Position above the target.
            rect.top.toInt() - coachmarkView.height - 16.dp.toInt()
        }
        // Directly set layout top margin if needed,
        // or use translationY for smooth animations.
        coachmarkView.translationY = newMargin.toFloat()

        // Adjust the tooltip edge accordingly.
        coachmarkBinding.root.shapeAppearanceModel = if (spaceBelow > spaceAbove) {
            coachmarkBinding.root.shapeAppearanceModel.toBuilder()
                .setTopEdge(tooltipEdgeTreatment)
                .build()
        } else {
            coachmarkBinding.root.shapeAppearanceModel.toBuilder()
                .setBottomEdge(tooltipEdgeTreatment)
                .build()
        }
    }

    /**
     * Transition to a new target by morphing the spotlight and moving the coachmark.
     */
    private fun transitionToTarget(newTarget: View, onTransitionEnd: () -> Unit) {
        // Calculate new target rectangle.
        val newRect = calculateTargetRect(newTarget)
        val currentRect = targetRect ?: newRect

        // Compute current and final coachmark positions.
        // Use the same logic as updateCoachmarkPosition.
        val screenHeight = context.deviceHeight
        val currentMargin = if ((screenHeight - currentRect.bottom) > currentRect.top) {
            currentRect.bottom.toInt() + 16.dp.toInt()
        } else {
            currentRect.top.toInt() - coachmarkView.height - 16.dp.toInt()
        }
        val finalMargin = if ((screenHeight - newRect.bottom) > newRect.top) {
            newRect.bottom.toInt() + 16.dp.toInt()
        } else {
            newRect.top.toInt() - coachmarkView.height - 16.dp.toInt()
        }

        // Store the starting coachmark position.
        val startCoachmarkY = coachmarkView.translationY

        // Animate both the spotlight rect and the coachmark position.
        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = SPOTLIGHT_ENTER_EXIT_DURATION
            interpolator = EaseInterpolator.EaseInOutQubicInterpolator
            addUpdateListener { animation ->
                val fraction = animation.animatedFraction

                // Interpolate the spotlight rectangle edges.
                val left = lerp(currentRect.left, newRect.left, fraction)
                val top = lerp(currentRect.top, newRect.top, fraction)
                val right = lerp(currentRect.right, newRect.right, fraction)
                val bottom = lerp(currentRect.bottom, newRect.bottom, fraction)
                targetRect = RectF(left, top, right, bottom)

                // Optionally adjust spotlight scale if needed.
                spotlightScale = 1f

                // Interpolate coachmark position.
                val interpolatedMargin = lerp(currentMargin.toFloat(), finalMargin.toFloat(), fraction)
                coachmarkView.translationY = interpolatedMargin

                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    // Once the animation ends, update the content.
                    currentCoachMarkIndex++
                    updateCoachMarkContent()

                    // Ensure coachmark is positioned exactly at the final value.
                    coachmarkView.translationY = finalMargin.toFloat()

                    // Fade in the coachmark view if needed.
                    val coachmarkFadeIn = ObjectAnimator.ofFloat(coachmarkView, ALPHA, 1f).apply {
                        duration = COACHMARK_ENTER_DURATION
                    }
                    coachmarkFadeIn.start()

                    onTransitionEnd()
                }
            })
        }
        animator.start()
    }

    /**
     * Show next coachmark item with a morphing transition.
     */
    fun showNextCoachMark() {
        if (currentCoachMarkIndex < coachMarkItems.size - 1) {
            // Get the next target view.
            val nextTarget = coachMarkItems[currentCoachMarkIndex + 1].target
            // Morph the spotlight and move the coachmark in sync.
            transitionToTarget(nextTarget) {
                coachMarkDelegate?.onNextClickClickListener()
            }
        } else {
            coachMarkDelegate?.onFinishClickListener()
            finishSpotlight { }
        }
    }

    /**
     * Fade out the overlay (with a fade-out transition) and run an action when finished.
     */
    fun fadeOutAndRemove(onAnimationEnd: () -> Unit) {
        coachmarkView.animate().alpha(0f)
            .setDuration(COACHMARK_EXIT_DURATION)
            .withEndAction { onAnimationEnd() }
            .start()
    }

    override fun dispatchDraw(canvas: Canvas?) {
        // Draw the overlay and then clear the spotlight area.
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

    /**
     * Animate the entrance of the spotlight.
     */
    fun startSpotlightAnimation() {
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = SPOTLIGHT_ENTER_EXIT_DURATION
            interpolator = EaseInterpolator.EaseOutQubicInterpolator
            addUpdateListener { animation ->
                spotlightScale = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    /**
     * Animate finishing the spotlight and remove the overlay.
     */
    fun finishSpotlight(onAnimationEnd: () -> Unit) {
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
                    onAnimationEnd()
                }
            })
            start()
        }
    }

    // Helper to linearly interpolate between two float values.
    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + fraction * (end - start)
    }

    // Helper to calculate the target rect for a given view.
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
}



