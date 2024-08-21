package id.co.edtslib.uikit.textfield

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.view.doOnLayout
import androidx.core.view.marginEnd
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import com.google.android.material.textfield.TextInputEditText
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.colorStateList
import id.co.edtslib.uikit.utils.dimenPixelSize
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.px
import id.co.edtslib.uikit.utils.vibrateAnimation
import kotlin.math.roundToInt

class TextField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.textInputStyle
) : id.co.edtslib.uikit.textinputlayout.TextInputLayout(context, attrs, defStyleAttr) {

    private var currentStyle: TextFieldStyle = TextFieldStyle.LABEL_INSIDE

    init {
        val textInputEditText = TextInputEditText(context, attrs).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )

            hint = if(this@TextField.placeholderText.isNullOrEmpty()) this.hint else null
        }

        addView(textInputEditText)
    }

    override fun setError(errorText: CharSequence?) {
        super.setError(errorText)

        vibrateAnimation()

        setContainerPadding()
    }

    private fun setContainerPadding() {
        if (childCount > 1) {
            val tripleExtraSmall = resources.getDimensionPixelSize(R.dimen.xxxs)

            getChildAt(1).apply {
                setPadding(0, tripleExtraSmall, 0, 0)
            }
        }
    }

    fun applyStyle(style: TextFieldStyle) {
        context.theme.applyStyle(style.styleResId, true)
    }
}