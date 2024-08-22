package id.co.edtslib.uikit.textfield

import android.content.Context
import android.os.Build
import android.text.InputFilter
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.dimenPixelSize
import id.co.edtslib.uikit.utils.lineHeight
import id.co.edtslib.uikit.utils.vibrateAnimation
import android.text.InputType as AndroidTextInputType

class TextField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.textInputStyle
) : id.co.edtslib.uikit.textinputlayout.TextInputLayout(context, attrs, defStyleAttr) {

    var delegate: TextFieldDelegate? = null

    init {
        val textInputEditText = TextInputEditText(context, attrs).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )

            hint = if(this@TextField.placeholderText.isNullOrEmpty()) this.hint else null
        }

        addView(textInputEditText)

        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        if (attrs != null) {
            context.theme.obtainStyledAttributes(attrs, R.styleable.TextField, 0, 0).apply {
                inputType = InputType.values()[getInt(R.styleable.TextField_fieldInputType, 0)]
                maxLength = getInt(R.styleable.TextField_fieldMaxLength, 0)
                imeOption = ImeOption.values()[getInt(R.styleable.TextField_fieldMaxLength, 0)]

                recycle()
            }
        } else {
            inputType = InputType.Text
            imeOption = ImeOption.Next
        }
    }



    override fun setError(errorText: CharSequence?) {
        super.setError(errorText)
        // Set Spacing because Normal TextAppearance Styling won't work
        val lineSpacingHeight = context.dimenPixelSize(R.dimen.dimen_18) ?: 0

        this.findViewById<TextView>(com.google.android.material.R.id.textinput_error).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                this.lineHeight = lineSpacingHeight
            } else {
                this.lineHeight(lineSpacingHeight)
            }
        }

        setContainerPadding()

        this.vibrateAnimation()
    }

    private fun setContainerPadding() {
        if (childCount > 1) {
            // Since the container has it own padding so 2dp is enough to match the DS
            val tripleExtraSmall = resources.getDimensionPixelSize(R.dimen.dimen_2)

            getChildAt(1).apply {
                setPadding(0, tripleExtraSmall, 0, 0)
            }
        }
    }

    private fun addFilter(inputFilter: InputFilter) {
        val filters = editText?.filters?.toMutableList() ?: mutableListOf()
        filters.add(inputFilter)
        editText?.filters = filters.toTypedArray()
    }

    private fun removeMaxLengthFilter() {
        val filters = editText?.filters?.toMutableList() ?: return
        filters.removeAll { it is InputFilter.LengthFilter }
        editText?.filters = filters.toTypedArray()
    }

    enum class ImeOption {
        Next, Send, Search, Done
    }

    var imeOption = ImeOption.Next
        set(value) {
            field = value
            val imeAction = when (value) {
                ImeOption.Done -> EditorInfo.IME_ACTION_DONE
                ImeOption.Send -> EditorInfo.IME_ACTION_SEND
                ImeOption.Search -> EditorInfo.IME_ACTION_SEARCH
                else -> EditorInfo.IME_ACTION_NEXT
            }
            editText?.imeOptions = imeAction
        }


    enum class InputType {
        Text, Password
    }

    var inputType = InputType.Text
        set(value) {
            field = value

            val (inputType, endIconMode) = when (value) {
                InputType.Password -> {
                    Pair(
                        AndroidTextInputType.TYPE_CLASS_TEXT or AndroidTextInputType.TYPE_TEXT_VARIATION_PASSWORD,
                        END_ICON_PASSWORD_TOGGLE
                    )
                }
                else -> {
                    Pair(
                        AndroidTextInputType.TYPE_CLASS_TEXT or AndroidTextInputType.TYPE_TEXT_VARIATION_NORMAL,
                        null
                    )
                }
            }

            editText?.apply {
                this.inputType = inputType
                endIconMode?.let { this@TextField.endIconMode = it }

                addTextChangedListener {
                    delegate?.onValueChange(it?.toString())
                }
            }
        }


    var maxLength = 0
        set(value) {
            field = value
            if (value > 0) addFilter(InputFilter.LengthFilter(maxLength))
            else removeMaxLengthFilter()
        }
}