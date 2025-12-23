package id.co.edtslib.uikit.stepper

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import kotlin.math.min

class StepperView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Colors
    private val activeColor = 0xFF1E88E5.toInt()
    private val disabledColor = 0xFFCCCCCC.toInt()
    private val backgroundColor = 0xFFFFFFFF.toInt()
    private val textColor = 0xFF000000.toInt()
    private val disabledTextColor = 0xFFCCCCCC.toInt()

    // State
    private var count = 0
    private var isExpanded = false
    private var isComponentEnabled = true
    private var isPlusEnabled = true
    private var isMinusEnabled = true

    // Animation
    private var expandProgress = 0f
    private val animationDuration = 300L

    // Dimensions
    private val collapsedSize = 120f
    private val expandedWidth = 400f
    private val buttonSize = 120f
    private val strokeWidth = 6f

    // Paint objects
    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = this@StepperView.strokeWidth
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 60f
        textAlign = Paint.Align.CENTER
    }

    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
    }

    // Touch areas
    private val plusButtonRect = RectF()
    private val minusButtonRect = RectF()

    // Listener
    private var onCountChangeListener: ((Int) -> Unit)? = null

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = expandedWidth.toInt()
        val desiredHeight = buttonSize.toInt()

        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerY = height / 2f
        val currentWidth = collapsedSize + (expandedWidth - collapsedSize) * expandProgress
        val left = (width - currentWidth) / 2f
        val right = left + currentWidth
        val radius = buttonSize / 2f

        // Draw background
        fillPaint.color = backgroundColor
        canvas.drawRoundRect(left, 0f, right, height.toFloat(), radius, radius, fillPaint)

        // Draw outline
        outlinePaint.color = if (isComponentEnabled) activeColor else disabledColor
        canvas.drawRoundRect(left, 0f, right, height.toFloat(), radius, radius, outlinePaint)

        if (!isExpanded) {
            // Draw single plus button
            drawPlusIcon(canvas, width / 2f, centerY, isPlusEnabled && isComponentEnabled)
            plusButtonRect.set(left, 0f, right, height.toFloat())
        } else {
            // Draw minus button (fade in with expansion)
            val minusAlpha = (expandProgress * 255).toInt()
            drawMinusButton(canvas, left + radius, centerY, minusAlpha)

            // Draw count
            textPaint.color = if (isComponentEnabled) textColor else disabledTextColor
            val countText = count.toString()
            val textY = centerY - (textPaint.descent() + textPaint.ascent()) / 2
            canvas.drawText(countText, width / 2f, textY, textPaint)

            // Draw plus button
            drawPlusButton(canvas, right - radius, centerY)

            // Update touch areas
            minusButtonRect.set(left, 0f, left + buttonSize, height.toFloat())
            plusButtonRect.set(right - buttonSize, 0f, right, height.toFloat())
        }
    }

    private fun drawPlusIcon(canvas: Canvas, cx: Float, cy: Float, enabled: Boolean) {
        iconPaint.color = if (enabled) activeColor else disabledColor
        val iconSize = 30f

        // Horizontal line
        canvas.drawLine(cx - iconSize, cy, cx + iconSize, cy, iconPaint)
        // Vertical line
        canvas.drawLine(cx, cy - iconSize, cx, cy + iconSize, iconPaint)
    }

    private fun drawMinusButton(canvas: Canvas, cx: Float, cy: Float, alpha: Int) {
        // Draw minus circle background
        fillPaint.color = backgroundColor
        fillPaint.alpha = alpha
        canvas.drawCircle(cx, cy, buttonSize / 2f - strokeWidth, fillPaint)
        fillPaint.alpha = 255

        // Draw minus icon
        iconPaint.color = if (isMinusEnabled && isComponentEnabled) activeColor else disabledColor
        iconPaint.alpha = alpha
        val iconSize = 30f
        canvas.drawLine(cx - iconSize, cy, cx + iconSize, cy, iconPaint)
        iconPaint.alpha = 255
    }

    private fun drawPlusButton(canvas: Canvas, cx: Float, cy: Float) {
        // Draw plus circle fill
        fillPaint.color = if (isPlusEnabled && isComponentEnabled) activeColor else disabledColor
        canvas.drawCircle(cx, cy, buttonSize / 2f - strokeWidth, fillPaint)

        // Draw plus icon in white
        iconPaint.color = backgroundColor
        val iconSize = 30f
        canvas.drawLine(cx - iconSize, cy, cx + iconSize, cy, iconPaint)
        canvas.drawLine(cx, cy - iconSize, cx, cy + iconSize, iconPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isComponentEnabled) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                return true
            }
            MotionEvent.ACTION_UP -> {
                val x = event.x
                val y = event.y

                if (!isExpanded) {
                    // Single plus button clicked
                    if (plusButtonRect.contains(x, y) && isPlusEnabled) {
                        incrementCount()
                        expand()
                    }
                } else {
                    // Check which button was clicked
                    if (plusButtonRect.contains(x, y) && isPlusEnabled) {
                        incrementCount()
                    } else if (minusButtonRect.contains(x, y) && isMinusEnabled) {
                        decrementCount()
                    }
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun expand() {
        isExpanded = true
        animateExpansion(0f, 1f)
    }

    private fun collapse() {
        isExpanded = false
        animateExpansion(1f, 0f)
    }

    private fun animateExpansion(from: Float, to: Float) {
        val animator = ValueAnimator.ofFloat(from, to).apply {
            duration = animationDuration
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                expandProgress = animation.animatedValue as Float
                invalidate()
            }
        }
        animator.start()
    }

    private fun incrementCount() {
        count++
        onCountChangeListener?.invoke(count)
        invalidate()
    }

    private fun decrementCount() {
        if (count > 0) {
            count--
            onCountChangeListener?.invoke(count)

            if (count == 0) {
                collapse()
            } else {
                invalidate()
            }
        }
    }

    // Public API
    fun setCount(value: Int) {
        count = value.coerceAtLeast(0)
        if (count > 0 && !isExpanded) {
            expand()
        } else if (count == 0 && isExpanded) {
            collapse()
        } else {
            invalidate()
        }
    }

    fun getCount(): Int = count

    fun setOnCountChangeListener(listener: (Int) -> Unit) {
        onCountChangeListener = listener
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        isComponentEnabled = enabled
        invalidate()
    }

    fun setPlusButtonEnabled(enabled: Boolean) {
        isPlusEnabled = enabled
        invalidate()
    }

    fun setMinusButtonEnabled(enabled: Boolean) {
        isMinusEnabled = enabled
        invalidate()
    }
}