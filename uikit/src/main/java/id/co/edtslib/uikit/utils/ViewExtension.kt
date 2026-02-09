package id.co.edtslib.uikit.utils

import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.graphics.toColorInt
import androidx.core.view.setMargins
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.request.RequestOptions
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import id.co.edtslib.uikit.R

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
    baseColor: Int = context.color(R.color.shimmerSkeleton),
    highlightColor: Int = context.color(R.color.white),
    shimmerDirection: Int = Shimmer.Direction.LEFT_TO_RIGHT,
    duration: Long = 3000,
    shimmerMethod: ShimmerMethod = ShimmerMethod.COLOR_HIGHLIGHT
): ShimmerFrameLayout? {
    val parent = this.parent as? ViewGroup ?: return null

    val originalLayoutParams = this.layoutParams

    val shimmerFrameLayout = ShimmerFrameLayout(this.context).apply {
        layoutParams = originalLayoutParams
    }

    val shimmer = if (shimmerMethod == ShimmerMethod.ALPHA_HIGHLIGHT) {
          Shimmer.AlphaHighlightBuilder()
              .setBaseAlpha(baseAlpha)
              .setHighlightAlpha(highlightAlpha)
              .setBaseAlpha(baseAlpha)
              .setDirection(shimmerDirection)
              .setAutoStart(true)
              .setDuration(duration)
              .build()
    } else {
        Shimmer.ColorHighlightBuilder()
            .setBaseColor(baseColor)
            .setHighlightColor(highlightColor)
            .setBaseAlpha(baseAlpha)
            .setDirection(shimmerDirection)
            .setAutoStart(true)
            .setDuration(duration)
            .build()
    }

    shimmerFrameLayout.setShimmer(shimmer)

    val targetIndex = parent.indexOfChild(this)
    parent.removeViewAt(targetIndex)

    this.layoutParams = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.WRAP_CONTENT,
        FrameLayout.LayoutParams.WRAP_CONTENT
    )

    shimmerFrameLayout.addView(this)
    parent.addView(shimmerFrameLayout, targetIndex)
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
    parent.removeViewAt( index)
    parent.addView(originalView, index)

    return originalView
}

enum class ShimmerMethod {
    COLOR_HIGHLIGHT,
    ALPHA_HIGHLIGHT
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


fun ImageView.loadRes(
    @DrawableRes resId: Int,
    requestOptions: RequestOptions? = normalOptions,
) {
    val glide = Glide.with(this)
        .load(resId)

    requestOptions?.let { glide.apply(it) }

    glide.into(this)
}

/**
 *
 * @param lowerDecoding reduce memory per-pixel but don't use it with transparency
 */
fun lowPerfOptions(lowerDecoding: Boolean = false) = RequestOptions()
    .downsample(DownsampleStrategy.AT_MOST)
    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
    .encodeQuality(85)
    .format(if (lowerDecoding) DecodeFormat.PREFER_RGB_565 else DecodeFormat.PREFER_ARGB_8888)

val normalOptions = RequestOptions()
    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
    .format(DecodeFormat.PREFER_ARGB_8888)
