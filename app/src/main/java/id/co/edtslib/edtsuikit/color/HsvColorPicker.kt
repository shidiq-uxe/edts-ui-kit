package id.co.edtslib.edtsuikit.color

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.min

class HsvColorPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onColorChanged: ((Int) -> Unit)? = null

    private var hue = 0f
    private var saturation = 1f
    private var value = 1f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3.dp
        color = Color.WHITE
    }
    private val thumbFillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val hueBarBounds = RectF()
    private val svRectBounds = RectF()

    private val thumbRadius = 10.dp
    private val hueBarHeight = 24.dp
    private val hueBarTopMargin = 12.dp
    private val svSize = 0f

    private var hueBarLeft = 0f
    private var huePosition = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val svSide = min(w, (h - hueBarHeight - hueBarTopMargin).toInt()).toFloat()
        val svLeft = (w - svSide) / 2f
        svRectBounds.set(svLeft, 0f, svLeft + svSide, svSide)

        hueBarLeft = svLeft
        hueBarBounds.set(svLeft, svSide + hueBarTopMargin, svLeft + svSide, svSide + hueBarTopMargin + hueBarHeight)
        huePosition = hueBarLeft + hue / 360f * svSide
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawSvSquare(canvas)
        drawHueBar(canvas)
        drawSvThumb(canvas)
        drawHueThumb(canvas)
    }

    private fun drawSvSquare(canvas: Canvas) {
        val r = svRectBounds
        val svWidth = r.width().toInt()
        val svHeight = r.height().toInt()
        if (svWidth <= 0 || svHeight <= 0) return

        val hueColor = Color.HSVToColor(floatArrayOf(hue, 1f, 1f))
        paint.color = hueColor
        paint.shader = null
        canvas.drawRect(r, paint)

        val whiteGradient = LinearGradient(r.left, 0f, r.right, 0f, Color.WHITE, Color.TRANSPARENT, Shader.TileMode.CLAMP)
        paint.shader = whiteGradient
        canvas.drawRect(r, paint)
        paint.shader = null

        val blackGradient = LinearGradient(0f, r.top, 0f, r.bottom, Color.TRANSPARENT, Color.BLACK, Shader.TileMode.CLAMP)
        paint.shader = blackGradient
        canvas.drawRect(r, paint)
        paint.shader = null
    }

    private fun drawHueBar(canvas: Canvas) {
        val colors = IntArray(7) { i -> Color.HSVToColor(floatArrayOf(i * 60f, 1f, 1f)) }
        val gradient = LinearGradient(hueBarLeft, 0f, hueBarBounds.right, 0f, colors, null, Shader.TileMode.CLAMP)
        paint.shader = gradient
        canvas.drawRoundRect(hueBarBounds, hueBarHeight / 2f, hueBarHeight / 2f, paint)
        paint.shader = null
    }

    private fun drawSvThumb(canvas: Canvas) {
        val x = svRectBounds.left + saturation * svRectBounds.width()
        val y = svRectBounds.top + (1f - value) * svRectBounds.height()
        val hueColor = Color.HSVToColor(floatArrayOf(hue, saturation, value))
        thumbFillPaint.color = hueColor

        canvas.drawCircle(x, y, thumbRadius + 2.dp, thumbPaint)
        canvas.drawCircle(x, y, thumbRadius, thumbFillPaint)
    }

    private fun drawHueThumb(canvas: Canvas) {
        val x = huePosition
        val y = hueBarBounds.centerY()
        val hueColor = Color.HSVToColor(floatArrayOf(hue, 1f, 1f))
        thumbFillPaint.color = hueColor

        canvas.drawCircle(x, y, thumbRadius + 2.dp, thumbPaint)
        canvas.drawCircle(x, y, thumbRadius, thumbFillPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val x = event.x
                val y = event.y

                if (y <= svRectBounds.bottom && x >= svRectBounds.left && x <= svRectBounds.right) {
                    saturation = ((x - svRectBounds.left) / svRectBounds.width()).coerceIn(0f, 1f)
                    value = (1f - (y - svRectBounds.top) / svRectBounds.height()).coerceIn(0f, 1f)
                }

                if (y >= hueBarBounds.top && y <= hueBarBounds.bottom) {
                    huePosition = x.coerceIn(hueBarLeft, hueBarBounds.right)
                    hue = ((huePosition - hueBarLeft) / hueBarBounds.width() * 360f).coerceIn(0f, 360f)
                }

                val color = Color.HSVToColor(floatArrayOf(hue, saturation, value))
                onColorChanged?.invoke(color)
                invalidate()
            }
        }
        return true
    }

    fun setColor(color: Int) {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hue = hsv[0]
        saturation = hsv[1]
        value = hsv[2]
        huePosition = hueBarLeft + hue / 360f * (hueBarBounds.width())
        invalidate()
    }

    fun getColor(): Int = Color.HSVToColor(floatArrayOf(hue, saturation, value))

    private val Float.dp: Float get() = this * resources.displayMetrics.density
    private val Int.dp: Float get() = this.toFloat() * resources.displayMetrics.density
}
