package id.co.edtslib.uikit.textfield

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.color

class TextField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.textInputStyle
) : TextInputLayout(context, attrs, defStyleAttr) {

    private var currentStyle: TextFieldStyle = TextFieldStyle.LABEL_INSIDE

    init {
        val textInputEditText = TextInputEditText(context, attrs).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }

        addView(editText)
    }

    fun applyStyle(style: TextFieldStyle) {
        context.theme.applyStyle(style.styleResId, true)
    }
}