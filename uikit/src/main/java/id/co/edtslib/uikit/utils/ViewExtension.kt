package id.co.edtslib.uikit.utils

import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import androidx.core.view.setMargins
import androidx.core.view.updateLayoutParams
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout

/**
 * Adds a pulsing shimmer effect to the target view by wrapping it in a `ShimmerFrameLayout`.
 * This effect is useful for drawing attention to a view or indicating a loading state.
 *
 * @param baseAlpha The base alpha level of the shimmer effect, where 1.0f is fully opaque. Default is 1f.
 * @param highlightAlpha The highlight alpha level for the shimmer animation, where 1.0f is fully opaque and 0.0f is transparent. Default is 0.75f.
 * @param shimmerDirection The direction of the shimmer effect, which can be one of the directions specified in `Shimmer.Direction`, such as LEFT_TO_RIGHT. Default is `Shimmer.Direction.LEFT_TO_RIGHT`.
 * @param duration The duration of one shimmer cycle in milliseconds. Default is 3000ms.
 */
fun View.attachShimmerEffect(
    baseAlpha: Float = 1f,
    highlightAlpha: Float = 0.75f,
    shimmerDirection: Int = Shimmer.Direction.LEFT_TO_RIGHT,
    duration: Long = 3000
): ShimmerFrameLayout? {
    val parent = this.parent as? ViewGroup ?: return null

    val shimmerFrameLayout = ShimmerFrameLayout(this.context).apply {
        this@apply.layoutParams = this@attachShimmerEffect.layoutParams
    }

    val shimmer = Shimmer.AlphaHighlightBuilder()
        .setBaseAlpha(baseAlpha)
        .setHighlightAlpha(highlightAlpha)
        .setDirection(shimmerDirection)
        .setAutoStart(true)
        .setDuration(duration)
        .build()

    shimmerFrameLayout.setShimmer(shimmer)

    val targetIndex = parent.indexOfChild(this)
    parent.removeViewAt(targetIndex)
    shimmerFrameLayout.addView(this)
    parent.addView(shimmerFrameLayout, targetIndex)

    this.updateLayoutParams<MarginLayoutParams> {
        // Reset View Margin to Default
        setMargins(0)
    }

    shimmerFrameLayout.startShimmer()

    return shimmerFrameLayout
}

/**
 * Removes the shimmer effect from a `ShimmerFrameLayout` and restores the original view to its parent.
 * This stops the shimmer animation and replaces the shimmer layout with the original view.
 */
fun ShimmerFrameLayout.detachShimmerEffect(): View? {
    this.stopShimmer()

    val originalView = this.getChildAt(0) ?: return null

    val parent = this.parent as? ViewGroup ?: return null

    val index = parent.indexOfChild(this)
    originalView.layoutParams = this.layoutParams

    this.removeView(originalView)
    parent.removeViewAt(index)
    parent.addView(originalView, index)

    return originalView
}

fun View.setGradientBackground(
    colors: IntArray,
    orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT,
    cornerRadiusDp: Float = 0f
) {
    val radiusPx = cornerRadiusDp * resources.displayMetrics.density
    val gd = GradientDrawable(orientation, colors).apply {
        cornerRadius = radiusPx
        gradientType = GradientDrawable.LINEAR_GRADIENT
    }
    background = gd
}

