package id.co.edtslib.uikit.viewgroup

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.google.android.material.card.MaterialCardView

/**
 * A custom MaterialCardView with built-in fade, slide, and optional physics-based spring animations.
 * Supports enter (IN) and exit (OUT) modes for both fade and slide in any direction.
 * Inherits all MaterialCardView styling (shape, stroke, elevation) attributes.
 */
open class AnimatedCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.materialCardViewStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    enum class AnimationType { FADE, SLIDE }

    enum class AnimationMode { IN, OUT }

    enum class SlideDirection { LEFT, RIGHT, TOP, BOTTOM }

    /**
     * @param type FADE or SLIDE
     * @param mode IN (enter) or OUT (exit)
     * @param slideDir required if type == SLIDE; direction to slide
     * @param durationMs duration for linear animations (ms)
     * @param offsetPx optional pixel offset for slide distance; defaults to view size
     * @param useSpring if true, applies spring animation instead of linear
     * @param stiffness spring stiffness (use SpringForce constants)
     * @param dampingRatio spring damping ratio (use SpringForce constants)
     */
    fun animate(
        type: AnimationType,
        mode: AnimationMode = AnimationMode.IN,
        slideDir: SlideDirection? = null,
        durationMs: Long = 300,
        offsetPx: Float? = null,
        useSpring: Boolean = false,
        stiffness: Float = SpringForce.STIFFNESS_MEDIUM,
        dampingRatio: Float = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
    ) {
        when (type) {
            AnimationType.FADE -> when (mode) {
                AnimationMode.IN -> fadeIn(durationMs)
                AnimationMode.OUT -> fadeOut(durationMs)
            }
            AnimationType.SLIDE -> {
                requireNotNull(slideDir) { "slideDir must be provided for SLIDE animations" }
                if (useSpring) {
                    springSlide(slideDir, mode, offsetPx, stiffness, dampingRatio)
                } else {
                    slideAnimate(slideDir, mode, durationMs, offsetPx)
                }
            }
        }
    }

    private fun fadeIn(durationMs: Long) {
        alpha = 0f
        animate()
            .alpha(1f)
            .setDuration(durationMs)
            .start()
    }

    private fun fadeOut(durationMs: Long) {
        alpha = 1f
        animate()
            .alpha(0f)
            .setDuration(durationMs)
            .start()
    }

    private fun slideAnimate(
        direction: SlideDirection,
        mode: AnimationMode,
        durationMs: Long,
        offsetPx: Float?
    ) {
        val distance = offsetPx ?: when (direction) {
            SlideDirection.LEFT, SlideDirection.RIGHT -> width.toFloat()
            SlideDirection.TOP, SlideDirection.BOTTOM -> height.toFloat()
        }

        if (mode == AnimationMode.IN) {
            when (direction) {
                SlideDirection.LEFT -> translationX = -distance
                SlideDirection.RIGHT -> translationX = distance
                SlideDirection.TOP -> translationY = -distance
                SlideDirection.BOTTOM -> translationY = distance
            }
            visibility = View.VISIBLE
            animate()
                .setDuration(durationMs)
                .translationX(0f)
                .translationY(0f)
                .start()
        } else {
            animate()
                .setDuration(durationMs)
                .translationX(if (direction == SlideDirection.LEFT) -distance else if (direction == SlideDirection.RIGHT) distance else translationX)
                .translationY(if (direction == SlideDirection.TOP) -distance else if (direction == SlideDirection.BOTTOM) distance else translationY)
                .withEndAction { visibility = View.GONE }
                .start()
        }
    }

    private fun springSlide(
        direction: SlideDirection,
        mode: AnimationMode,
        offsetPx: Float?,
        stiffness: Float,
        dampingRatio: Float
    ) {
        val distance = offsetPx ?: when (direction) {
            SlideDirection.LEFT, SlideDirection.RIGHT -> width.toFloat()
            SlideDirection.TOP, SlideDirection.BOTTOM -> height.toFloat()
        }

        val property = when (direction) {
            SlideDirection.LEFT, SlideDirection.RIGHT -> SpringAnimation.TRANSLATION_X
            SlideDirection.TOP, SlideDirection.BOTTOM -> SpringAnimation.TRANSLATION_Y
        }

        val finalPos = if (mode == AnimationMode.IN) 0f else if (direction == SlideDirection.LEFT || direction == SlideDirection.TOP) -distance else distance
        val springForce = SpringForce(finalPos).apply {
            this.stiffness = stiffness
            this.dampingRatio = dampingRatio
        }

        if (mode == AnimationMode.IN) {
            when (direction) {
                SlideDirection.LEFT -> translationX = -distance
                SlideDirection.RIGHT -> translationX = distance
                SlideDirection.TOP -> translationY = -distance
                SlideDirection.BOTTOM -> translationY = distance
            }
            visibility = View.VISIBLE
        }

        SpringAnimation(this, property).apply {
            this.spring = springForce
        }.start()
    }
}