package id.co.edtslib.uikit.textfield

import android.content.Context
import android.os.Build
import android.text.InputFilter
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.content.res.use
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.textinputlayout.TextInputLayout
import id.co.edtslib.uikit.utils.buildHighlightedMessage
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.colorStateList
import id.co.edtslib.uikit.utils.dimenPixelSize
import id.co.edtslib.uikit.utils.hapticfeedback.HapticFeedback
import id.co.edtslib.uikit.utils.lineHeight
import id.co.edtslib.uikit.utils.vibrateAnimation
import id.co.edtslib.uikit.utils.vibratePhone
import id.co.edtslib.uikit.utils.TextStyle
import android.text.InputType as AndroidTextInputType

open class TextField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.textFieldStyle
) : TextInputLayout(context, attrs, defStyleAttr) {

    var delegate: TextFieldDelegate? = null

    var isFieldRequired = true

    init {
        val textInputEditText = TextInputEditText(context, attrs).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )

            hint = if(this@TextField.placeholderText.isNullOrEmpty()) this.hint else null
        }

        this@TextField.addView(textInputEditText)

        init(attrs, defStyleAttr)

        setContainerPadding()
    }

    private fun overrideCollapsingTextHelperErrorColor(errorText: CharSequence?) {
        val textColorStates = context.colorStateList(
            if (errorText.isNullOrEmpty()) R.color.black_60
            else R.color.red_30
        )

        isError = !errorText.isNullOrEmpty()

        setHelperTextColor(textColorStates)

        helperText = errorText
        isHelperTextEnabled = !errorText.isNullOrEmpty()

        if (isCounterEnabled) {
            counterTextColor = textColorStates
        }
    }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int) {
        this.isExpandedHintEnabled = false

        if (attrs != null) {
            context.theme.obtainStyledAttributes(attrs, R.styleable.TextField, defStyleAttr, 0).use {
                inputType = InputType.values()[it.getInt(R.styleable.TextField_fieldInputType, 0)]
                maxLength = it.getInt(R.styleable.TextField_fieldMaxLength, 0)
                imeOption = ImeOption.values()[it.getInt(R.styleable.TextField_fieldImeOptions, 0)]
                isFieldRequired = it.getBoolean(R.styleable.TextField_isFieldRequired, isFieldRequired)
            }
        } else {
            inputType = InputType.Text
            imeOption = ImeOption.Next
        }

       if (!isInEditMode) {
           if (isFieldRequired) {
               hint = buildHighlightedMessage(
                   context = context,
                   message = "$hint *",
                   highlightedMessages = listOf("*"),
                   highlightedTextAppearance = listOf(
                       TextStyle.h3Style(
                           context = context,
                           color = context.color(R.color.red_30)
                       )
                   ),
                   defaultTextAppearance = TextStyle.h3Style(
                       context = context,
                       color = context.color(R.color.black_50)
                   )
               )
           }
       }
    }

    override fun isHelperTextEnabled(): Boolean {
        setContainerPadding()
        return super.isHelperTextEnabled()
    }

    // Ensure to call this view only when the errorText is not null
    val textInputError: TextView? get() = this.findViewById(com.google.android.material.R.id.textinput_error)

    override fun setError(errorText: CharSequence?) {
        // In order to achieve 1:1 Design System i have done several workarounds such as using Helper text instead errorText and using custom background for Error
        // Sadly even after using reflection won't work to change the hint error color, it was defined inside Internal File named CollapsingTextHelper
        overrideCollapsingTextHelperErrorColor(errorText)

        if (inputType == InputType.OTP) {
            this.editText?.setTextColor(context.color(R.color.red_30))
        }

        // Break the function when the errorText is null to ensure all logic works fine
        errorText ?: return

        // Since normal TextAppearance won't work, programmatically approach is used
        val lineSpacingHeight = context.dimenPixelSize(R.dimen.dimen_18) ?: 0

        textInputError?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                this.lineHeight = lineSpacingHeight
            } else {
                this.lineHeight(lineSpacingHeight)
            }
        }

        setContainerPadding()

        this.vibrateAnimation().also {
            context.vibratePhone(HapticFeedback.ERROR)
        }
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
        Text, Password, Pin, Phone, Email, OTP
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
                InputType.Pin -> {
                    Pair(
                        AndroidTextInputType.TYPE_CLASS_NUMBER or AndroidTextInputType.TYPE_NUMBER_VARIATION_PASSWORD,
                        END_ICON_PASSWORD_TOGGLE
                    )
                }
                InputType.Phone -> {
                    Pair(
                        android.text.InputType.TYPE_CLASS_PHONE or android.text.InputType.TYPE_TEXT_VARIATION_PHONETIC,
                        endIconMode
                    )
                }
                InputType.Email -> {
                    Pair(
                        android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                        endIconMode
                    )
                }
                InputType.OTP -> {
                    Pair(
                        android.text.InputType.TYPE_CLASS_NUMBER,
                        endIconMode
                    )
                }
                else -> {
                    Pair(
                        AndroidTextInputType.TYPE_CLASS_TEXT or AndroidTextInputType.TYPE_TEXT_VARIATION_NORMAL,
                        endIconMode
                    )
                }
            }

            editText?.apply {
                this.inputType = inputType
                this@TextField.endIconMode = endIconMode

                if (value == InputType.OTP) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        this.setTextAppearance(R.style.TextAppearance_Inter_Semibold_D1)
                    } else {
                        this.setTextAppearance(context, R.style.TextAppearance_Inter_Semibold_D1)
                    }
                }

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