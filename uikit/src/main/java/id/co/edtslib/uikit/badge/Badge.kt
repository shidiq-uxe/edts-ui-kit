package id.co.edtslib.uikit.badge

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RelativeCornerSize
import com.google.android.material.shape.ShapeAppearanceModel
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dp

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
            visibility = if (value.isNullOrEmpty()) View.GONE else View.VISIBLE
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
    }

    private val textView: TextView = TextView(this.context).apply {
        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        )
        setTextAppearance(badgeTextAppearance)
        setTextColor(textColor)
    }

    init {
        initAttributes()
    }

    private fun initAttributes() {
        background = badgeDrawable
        addView(textView)

        this.updatePadding(
            left = 2.dp.toInt(),
            right = 2.dp.toInt(),
            top = 1.dp.toInt(),
            bottom = 1.dp.toInt()
        )
    }
}