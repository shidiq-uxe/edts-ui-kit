package id.co.edtslib.uikit.tablayout

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import id.co.edtslib.uikit.utils.dp
import androidx.core.graphics.createBitmap
import com.google.android.material.shadow.ShadowDrawableWrapper
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.ShapeAppearancePathProvider
import com.google.android.material.shape.ShapePath

class QuadRoundTabBackgroundDrawable(
    private val width: Int,
    private val height: Int,
    @ColorInt private val tabColor: Int
) : MaterialShapeDrawable() {

    private val path = Path()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = tabColor
    }

    private val cornerRadius = 12.dp

    override fun draw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        updatePath(bounds.width(), bounds.height())
    }

    private fun updatePath(width: Int, height: Int) {
        path.reset()
        if (width <= 0 || height <= 0) return

        path.moveTo(cornerRadius.plus(cornerRadius), 0f)
        path.lineTo(width - cornerRadius.plus(cornerRadius), 0f)
        path.quadTo(width.minus(cornerRadius).toFloat(), 0f, width.minus(cornerRadius).toFloat(), cornerRadius)
        path.lineTo(width.minus(cornerRadius).toFloat(), height - cornerRadius)
        path.quadTo(width.minus(cornerRadius).toFloat(), height.toFloat(), width.toFloat(), height.toFloat())
        path.lineTo(0f, height.toFloat())
        path.quadTo(cornerRadius, height.toFloat(), cornerRadius, height - cornerRadius)
        path.lineTo(cornerRadius, cornerRadius)
        path.quadTo(cornerRadius, 0f, cornerRadius.plus(cornerRadius), 0f)
        path.close()
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    fun getPath(): Path = path

    fun containerSubtract(): Float = cornerRadius
}