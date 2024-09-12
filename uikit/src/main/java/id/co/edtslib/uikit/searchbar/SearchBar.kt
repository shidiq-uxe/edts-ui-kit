package id.co.edtslib.uikit.searchbar

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.doOnLayout
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doOnTextChanged
import com.google.android.material.textfield.TextInputEditText
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.textfield.TextField
import id.co.edtslib.uikit.textinputlayout.TextInputLayout
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.colorStateList
import id.co.edtslib.uikit.utils.dimen
import id.co.edtslib.uikit.utils.dimenPixelSize
import id.co.edtslib.uikit.utils.drawable
import id.co.edtslib.uikit.utils.px
import kotlin.math.roundToInt

class SearchBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.searchBarStyle
) : TextInputLayout(context, attrs, defStyleAttr) {

    private val endIconRes = R.drawable.ic_cancel_rounded_16

    private val startIconView: ImageButton get() = findViewById(com.google.android.material.R.id.text_input_start_icon)
    private val endIconView: ImageButton get() = findViewById(com.google.android.material.R.id.text_input_end_icon)

    var isCancelVisible = true
        set(value) {
            field = value

            endIconMode = if (value) END_ICON_CUSTOM else END_ICON_NONE
            endIconDrawable = if (value) context.drawable(endIconRes) else null

            if (value) {
                endIconView.setBackgroundColor(Color.TRANSPARENT)
                endIconView.updateLayoutParams<MarginLayoutParams> {
                    this.marginStart = -8.px.roundToInt()
                    this.marginEnd = -4.px.roundToInt()
                }
            }
        }

    init {
        val editText = TextInputEditText(context, null, R.attr.inputSearchBarStyle).apply {
            this.height = ViewGroup.LayoutParams.MATCH_PARENT
            this.setPadding(this.paddingLeft, 0, this.paddingRight, 0)

            this.compoundDrawablePadding = 0
        }

        addView(editText)

        initAttrs(attrs, defStyleAttr)

        setEndIconOnClickListener {
            onCancelClickListener.invoke(it)
        }

        // Adjust the height to match 32dp based on DS
        doOnLayout {
            this.layoutParams = this.layoutParams?.apply {
                this.height = resources.getDimensionPixelSize(R.dimen.l)
            }
        }

        if (isStartIconVisible) {
            startIconView.updateLayoutParams<MarginLayoutParams> {
                this.marginStart = -4.px.roundToInt()
                this.marginEnd = -8.px.roundToInt()
            }
        }
    }

    private fun initAttrs(attrs: AttributeSet?, defStyleAttr: Int) {
        if (attrs != null) {
            context.theme.obtainStyledAttributes(attrs, R.styleable.SearchBar, defStyleAttr, 0).apply {
                isCancelVisible =
                    getBoolean(R.styleable.SearchBar_closeIconVisible, isCancelVisible)
            }
        }
    }

    private var onCancelClickListener: ((View) -> Unit) = {}

    fun setOnCancelClickListener(action: (View) -> Unit) {
        onCancelClickListener = action
    }

}