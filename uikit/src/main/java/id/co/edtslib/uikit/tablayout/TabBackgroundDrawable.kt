package id.co.edtslib.uikit.tablayout

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.ShapeAppearancePathProvider
import id.co.edtslib.uikit.utils.dp

class TabBackgroundDrawable(
    private val width: Int,
    private val height: Int,
    @ColorInt private val tabColor: Int
) : Drawable() {

    private val path = Path()
    private val paint = Paint().apply {
        isAntiAlias = true
        color = tabColor
        style = Paint.Style.FILL
    }

    private var cachedBitmap: Bitmap? = null
    private val bitmapPaint = Paint(Paint.FILTER_BITMAP_FLAG)

    init {
        createPath()
    }

    private fun createPath() {
        // Reset the path
        path.reset()

        // Top left corner radius
        val cornerRadius = 12.dp

        // Start from top left after the curve
        path.moveTo(cornerRadius.plus(cornerRadius), 0f)

        // Top line
        path.lineTo(width - cornerRadius.plus(cornerRadius), 0f)

        // Top right curve
        path.quadTo(width.minus(cornerRadius).toFloat(), 0f, width.minus(cornerRadius).toFloat(), cornerRadius)

        // Right side
        path.lineTo(width.minus(cornerRadius).toFloat(), height - cornerRadius)

        // Bottom right curve
        path.quadTo(width.minus(cornerRadius).toFloat(), height.toFloat(), width.toFloat(), height.toFloat())

        // Bottom line
        path.lineTo(0f, height.toFloat())

        // Bottom left curve (Fixed)
        path.quadTo(cornerRadius, height.toFloat(), cornerRadius, height - cornerRadius)

        // Left side
        path.lineTo(cornerRadius, cornerRadius)

        // Top left curve
        path.quadTo(cornerRadius, 0f, cornerRadius.plus(cornerRadius), 0f)

        // Close the path
        path.close()

        // Create a cached bitmap for better performance
        createCachedBitmap()
    }

    private fun createCachedBitmap() {
        if (width <= 0 || height <= 0) return

        cachedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(cachedBitmap!!)

        canvas.drawPath(path, paint)
    }

    override fun draw(canvas: Canvas) {
        // Use the cached bitmap if available
        cachedBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, bitmapPaint)
            return
        }

        // Fallback to drawing the path directly
        canvas.drawPath(path, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        bitmapPaint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        bitmapPaint.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        // If bounds change, update the path and cached bitmap
        if (bounds.width() > 0 && bounds.height() > 0) {
            cachedBitmap?.recycle()
            cachedBitmap = null
            createPath()
        }
    }

    fun getPath(): Path = path
}