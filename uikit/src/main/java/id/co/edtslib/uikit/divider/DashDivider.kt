package id.co.edtslib.uikit.divider

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.toColorInt
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.color

class DashDivider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Todo : Add to color xml
    private val defaultColor = "#E6E9EB".toColorInt()

    private lateinit var capsuleDrawable: DashDividerDrawable

    init {
        context.withStyledAttributes(attrs, R.styleable.CapsuleDividerView) {
            val color = getColor(R.styleable.CapsuleDividerView_dividerColor, "#E6E9EB".toColorInt())
            val thickness = getDimensionPixelSize(R.styleable.CapsuleDividerView_dividerThickness, 8)
            val segment = getDimensionPixelSize(R.styleable.CapsuleDividerView_segmentWidth, 24)
            val gap = getDimensionPixelSize(R.styleable.CapsuleDividerView_gapWidth, 8)

            capsuleDrawable = DashDividerDrawable(color, thickness, segment, gap)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        val height = when (hMode) {
            MeasureSpec.EXACTLY -> hSize
            else -> capsuleDrawable.intrinsicHeight + paddingTop + paddingBottom
        }
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        capsuleDrawable.setBounds(paddingLeft, paddingTop, w - paddingRight, h - paddingBottom)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        capsuleDrawable.draw(canvas)
    }
}