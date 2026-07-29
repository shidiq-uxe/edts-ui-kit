package id.co.edtslib.uikit.utils

import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import androidx.annotation.ColorInt

// Helpers for drawing gradients on a Canvas with Paint (e.g. inside a custom View's onDraw).

/**
 * Returns the start/end points of a gradient across this rect, based on [orientation].
 */
fun RectF.gradientEndpoints(
    orientation: GradientDrawable.Orientation
): Pair<PointF, PointF> = when (orientation) {
    GradientDrawable.Orientation.LEFT_RIGHT -> PointF(left, top) to PointF(right, top)
    GradientDrawable.Orientation.RIGHT_LEFT -> PointF(right, top) to PointF(left, top)
    GradientDrawable.Orientation.TOP_BOTTOM -> PointF(left, top) to PointF(left, bottom)
    GradientDrawable.Orientation.BOTTOM_TOP -> PointF(left, bottom) to PointF(left, top)
    GradientDrawable.Orientation.TL_BR -> PointF(left, top) to PointF(right, bottom)
    GradientDrawable.Orientation.TR_BL -> PointF(right, top) to PointF(left, bottom)
    GradientDrawable.Orientation.BL_TR -> PointF(left, bottom) to PointF(right, top)
    GradientDrawable.Orientation.BR_TL -> PointF(right, bottom) to PointF(left, top)
}

/**
 * Builds a LinearGradient shader spanning this rect in the given [orientation].
 * Assign the result to [Paint.shader].
 */
fun RectF.toLinearGradient(
    @ColorInt colors: IntArray,
    orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT,
    positions: FloatArray? = null,
    tileMode: Shader.TileMode = Shader.TileMode.CLAMP
): LinearGradient {
    require(colors.size >= 2) { "Gradient needs at least 2 colors" }
    val (start, end) = gradientEndpoints(orientation)
    return LinearGradient(start.x, start.y, end.x, end.y, colors, positions, tileMode)
}

/**
 * Sets this Paint to a solid color, or a gradient if [gradientColors] is provided.
 */
fun Paint.applyFill(
    rect: RectF,
    @ColorInt solidColor: Int,
    @ColorInt gradientColors: IntArray? = null,
    orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT
) {
    shader = gradientColors?.let { rect.toLinearGradient(it, orientation) }
    if (shader == null) color = solidColor
}