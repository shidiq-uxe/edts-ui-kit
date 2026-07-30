package id.co.edtslib.edtsuikit.color

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.isVisible

class ColorPaletteView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var onColorSelected: ((Int) -> Unit)? = null
    var onAddClicked: ((context: Context, selectedColor: Int?) -> Unit)? = null

    private var selectedColor: Int? = null
    private var colors: List<Int> = emptyList()

    private val colorViews = mutableListOf<FrameLayout>()
    private var addButton: FrameLayout? = null

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }

    fun setColors(colors: List<Int>, selectedColor: Int?) {
        this.colors = colors.take(6)
        this.selectedColor = selectedColor
        colorViews.clear()
        removeAllViews()

        this.colors.forEach { color ->
            val colorView = createColorView(color)
            colorViews.add(colorView)
            addView(colorView)
        }

        addButton = createAddButton()
        addView(addButton)
    }

    private fun createColorView(color: Int): FrameLayout {
        val size = 40.dp
        val borderWidth = 3.dp

        val outer = FrameLayout(context).apply {
            layoutParams = LayoutParams(size, size).apply {
                marginEnd = 8.dp
            }
            isClickable = true
            isFocusable = true
            setOnClickListener {
                selectedColor = color
                onColorSelected?.invoke(color)
                updateSelections()
            }
        }

        val innerFrame = FrameLayout(context).apply {
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setStroke(borderWidth, color)
                setColor(Color.TRANSPARENT)
            }
            background = bg
            isVisible = color == selectedColor
            tag = "border"
            layoutParams = FrameLayout.LayoutParams(size, size)
        }

        val dot = View(context).apply {
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
            }
            background = bg
            val inset = borderWidth + 4.dp
            layoutParams = FrameLayout.LayoutParams(size - inset * 2, size - inset * 2).apply {
                gravity = Gravity.CENTER
            }
        }

        outer.addView(dot)
        outer.addView(innerFrame)
        outer.tag = color
        return outer
    }

    private fun createAddButton(): FrameLayout {
        val size = 40.dp

        val frame = FrameLayout(context).apply {
            layoutParams = LayoutParams(size, size)
            isClickable = true
            isFocusable = true
            setOnClickListener { onAddClicked?.invoke(context, selectedColor) }
        }

        val bg = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setStroke(2.dp, Color.parseColor("#878F99"))
            setColor(Color.TRANSPARENT)
        }

        val border = View(context).apply {
            background = bg
            layoutParams = FrameLayout.LayoutParams(size, size)
        }

        val plusView = android.widget.TextView(context).apply {
            text = "+"
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#878F99"))
            layoutParams = FrameLayout.LayoutParams(size, size)
        }

        frame.addView(border)
        frame.addView(plusView)
        return frame
    }

    private fun updateSelections() {
        colorViews.forEach { outer ->
            val border = outer.findViewWithTag<View>("border")
            val tag = outer.tag as? Int
            border?.isVisible = tag == selectedColor
        }
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}
