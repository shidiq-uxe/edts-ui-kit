package id.co.edtslib.uikit.divider

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt

class DashDividerDrawable(
    @ColorInt var color: Int,
    var thickness: Int,
    var segmentLength: Int,
    var gapLength: Int
) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        this.color = this@DashDividerDrawable.color
    }
    private val capsuleRect = RectF()

    override fun draw(canvas: Canvas) {
        val radius = thickness / 2f
        val centerY = bounds.centerY().toFloat()
        val top = centerY - (thickness / 2f)
        val bottom = centerY + (thickness / 2f)

        var currentX = bounds.left.toFloat()

        while (currentX < bounds.right) {
            val endX = (currentX + segmentLength).coerceAtMost(bounds.right.toFloat())

            capsuleRect.set(currentX, top, endX, bottom)
            canvas.drawRoundRect(capsuleRect, radius, radius, paint)

            currentX += segmentLength + gapLength
        }
    }

    override fun setAlpha(alpha: Int) { paint.alpha = alpha }
    override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter }
    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    override fun getIntrinsicHeight(): Int = thickness
}