package id.co.edtslib.uikit.badge

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.updatePadding
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RelativeCornerSize
import com.google.android.material.shape.ShapeAppearanceModel
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dimen
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.font

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
            invalidate()
        }

    // Todo : Font weight fallback
    var fontFamily = context.font(R.font.inter_semibold)
        set(value) {
            field = value
            textView.setTypeface(value)
        }

    private var extraPaddingLeft = 0
    private var extraPaddingTop = 0
    private var extraPaddingRight = 0
    private var extraPaddingBottom = 0

    private val baseHorizontalPadding = 2.dp.toInt()
    private val baseVerticalPadding = 1.dp.toInt()

    internal val badgeShapeModel = ShapeAppearanceModel()
        .toBuilder()
        .setAllCornerSizes(RelativeCornerSize(0.5f))
        .build()

    internal val badgeDrawable = MaterialShapeDrawable(badgeShapeModel).apply {
        setTint(badgeColor)
        strokeWidth = if (includeStroke) this@Badge.strokeWidth else 0f
        strokeColor = ColorStateList.valueOf(context.color(R.color.white))
        val stroke = strokeWidth.toInt()
        setPadding(stroke, stroke, stroke, stroke)
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
        setTypeface(fontFamily)
        setTextColor(textColor)
    }

    init {
        initAttributes(attrs)
    }

    private fun initAttributes(attrs: AttributeSet?) {
        background = badgeDrawable
        addView(textView)

        /*context.obtainStyledAttributes(attrs, R.styleable.Badge).use {
            if(it.hasValue(R.styleable.Badge_text)) {
                text = it.getString(R.styleable.Badge_text)
            }
        }*/
    }

    fun extraPadding(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0) {
        extraPaddingLeft = left
        extraPaddingTop = top
        extraPaddingRight = right
        extraPaddingBottom = bottom
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val currentText = text.orEmpty()
        val isSingleChar = currentText.length == 1

        if (currentText.isEmpty()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        val horizontalPadding =
            if (isSingleChar) 0 else baseHorizontalPadding
        val verticalPadding =
            if (isSingleChar) 0 else baseVerticalPadding

        setPadding(
            horizontalPadding + extraPaddingLeft,
            verticalPadding + extraPaddingTop,
            horizontalPadding + extraPaddingRight,
            verticalPadding + extraPaddingBottom
        )

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (isSingleChar) {
            val size = maxOf(measuredWidth, measuredHeight)
            setMeasuredDimension(size, size)
        }
    }
}