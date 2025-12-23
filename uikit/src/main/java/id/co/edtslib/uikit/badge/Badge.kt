package id.co.edtslib.uikit.badge

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.res.use
import androidx.core.view.doOnLayout
import androidx.core.view.setPadding
import androidx.core.view.updatePadding
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RelativeCornerSize
import com.google.android.material.shape.ShapeAppearanceModel
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dimen
import id.co.edtslib.uikit.utils.dp
import kotlin.text.toInt

// Todo : Research More about Material Badge Drawable
class Badge @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(ContextThemeWrapper(context, R.style.Theme_EDTS_UIKit), attrs, defStyleAttr) {

    @ColorInt
    var badgeColor = context.color(R.color.primary_30)
        set(value) {
            field = value
            badgeDrawable.setTint(value)
        }

    @ColorInt
    var textColor = context.color(R.color.white)
        set(value) {
            field = value
            textView.setTextColor(value)
        }

    var text: String? = null
        set(value) {
            field = value
            textView.text = value
            visibility = if (value.isNullOrEmpty()) GONE else VISIBLE

            // Immediately update layout based on text length
            if (!value.isNullOrEmpty()) {
                if (value.length == 1) {
                    // Will be sized in onMeasure
                    layoutParams = layoutParams?.apply {
                        width = LayoutParams.WRAP_CONTENT
                        height = LayoutParams.WRAP_CONTENT
                    }
                } else {
                    // Multi-character: ensure WRAP_CONTENT
                    layoutParams = layoutParams?.apply {
                        width = LayoutParams.WRAP_CONTENT
                        height = LayoutParams.WRAP_CONTENT
                    }
                    updatePadding(
                        left = 2.dp.toInt(),
                        right = 2.dp.toInt(),
                        top = 0,
                        bottom = 0
                    )
                }
            }
            requestLayout()
        }


    var includeStroke = false
        set(value) {
            invalidate()
        }

    var strokeWidth = context.dimen(R.dimen.xxxs)
        set(value) {
            invalidate()
        }

    @StyleRes
    var badgeTextAppearance = R.style.TextAppearance_Inter_SemiBold_B4
        set(value) {
            field = value
            textView.setTextAppearance(value)
        }

    internal val badgeShapeModel = ShapeAppearanceModel()
        .toBuilder()
        .setAllCornerSizes(RelativeCornerSize(0.5f))
        .build()

    internal val badgeDrawable = MaterialShapeDrawable(badgeShapeModel).apply {
        setTint(badgeColor)
        strokeWidth = if (includeStroke) this@Badge.strokeWidth else 0f
        strokeColor = ColorStateList.valueOf(context.color(R.color.white))
        setPadding(strokeWidth.toInt())
    }

    private val textView: TextView = TextView(this.context).apply {
        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setTextAppearance(badgeTextAppearance)
        }
        setTextColor(textColor)
    }

    init {
        initAttributes(attrs)
    }

    private fun initAttributes(attrs: AttributeSet?) {
        background = badgeDrawable
        addView(textView)

        this.updatePadding(
            left = 2.dp.toInt(),
            right = 2.dp.toInt(),
        )

        /*context.obtainStyledAttributes(attrs, R.styleable.Badge).use {
            if(it.hasValue(R.styleable.Badge_text)) {
                text = it.getString(R.styleable.Badge_text)
            }
        }*/
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val currentText = text
        if (currentText?.length == 1) {
            // For single character, make it circular
            val size = maxOf(measuredWidth, measuredHeight)
            setMeasuredDimension(size, size)

            // Adjust padding to center the text
            val textWidth = textView.measuredWidth
            val textHeight = textView.measuredHeight
            updatePadding(
                left = (size - textWidth) / 2,
                right = (size - textWidth) / 2,
                top = (size - textHeight) / 2,
                bottom = (size - textHeight) / 2
            )
        }
        // For multi-character text, super.onMeasure already handled it correctly
    }
}