package id.co.edtslib.uikit.otp

import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.doOnLayout
import androidx.core.widget.doAfterTextChanged
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.databinding.TextFieldOtpBinding
import id.co.edtslib.uikit.textfield.TextField
import id.co.edtslib.uikit.textinputlayout.TextInputLayout
import id.co.edtslib.uikit.utils.inflater
import id.co.edtslib.uikit.utils.vibrateAnimation

class OtpGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    var delegate: OtpDelegate? = null

    private var otpCount: Int = 4
    private var marginBetween: Int = 8 // default margin in dp

    private val otpInputLayouts = mutableListOf<TextField>()

    var isError: Boolean
        get() = otpInputLayouts.all { it.isError }
        set(value) = otpInputLayouts.forEach {
            it.isError = value

            if (value && shouldAnimateError) {
                it.vibrateAnimation()
            }
        }

    var shouldAnimateError: Boolean = true

    init {
        context.withStyledAttributes(attrs, R.styleable.OtpGroup, defStyleAttr, 0) {
            otpCount = getInt(R.styleable.OtpGroup_otpCount, otpCount)
            marginBetween = getDimensionPixelSize(R.styleable.OtpGroup_marginBetween, marginBetween)
            isError = getBoolean(R.styleable.OtpGroup_isError, isError)
            shouldAnimateError = getBoolean(R.styleable.OtpGroup_animateError, shouldAnimateError)
        }

        setupView()
    }

    private fun setupView() {
        orientation = HORIZONTAL

        repeat(otpCount) { index ->
            val otpView = TextFieldOtpBinding.inflate(context.inflater)

            val textField = otpView.root.apply {
                this.inputType = TextField.InputType.OTP
                this.shouldChangeTextColor = true

                this.editText?.apply {
                    this.id = generateViewId()
                    this.gravity = Gravity.CENTER
                }
            }

            otpInputLayouts.add(textField)

            this.addView(textField)

            this@OtpGroup.doOnLayout {
                textField.layoutParams = (textField.layoutParams as LayoutParams).apply {
                    // Set each OTP ratio to 4:5
                    width = this@OtpGroup.height * 4 / 5

                    val margin = marginBetween.div(2)
                    when (index) {
                        0 -> marginEnd = margin
                        otpCount - 1 -> marginStart = margin
                        else -> {
                            marginStart = margin
                            marginEnd = margin
                        }
                    }
                }
                textField.requestLayout()
            }
        }

        otpInputLayouts.forEachIndexed { i, textInputLayout ->
            textInputLayout.editText?.let { editText ->
                editText.filters = arrayOf(InputFilter.LengthFilter(otpInputLayouts.size - i))

                editText.doAfterTextChanged {
                    if (i < otpInputLayouts.size - 1) {
                        if (it?.length == 1) {
                            otpInputLayouts[i + 1].requestFocus()
                        } else if ((it?.length ?: 0) > 1) {
                            otpInputLayouts[i + 1].editText?.setText(it?.substring(1))
                        }
                    }

                    if ((it?.length ?: 0) > 1) {
                        it?.delete(1, it.length)
                    }

                    val otpString = otpInputLayouts.mapNotNull { it.editText }
                        .joinToString(separator = "") { editText -> editText.text ?: "" }

                    if (otpString.length == otpInputLayouts.size) {
                        delegate?.setOnOtpCompleteListener(otpString)
                    }

                    delegate?.setOnTextChangeListener(otpString)
                }

                editText.setOnKeyListener { v, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                        if (editText.text.isNullOrEmpty() && i != 0) {
                            otpInputLayouts[i - 1].requestFocus()
                            return@setOnKeyListener true
                        }
                    }
                    return@setOnKeyListener false
                }

                editText.setOnFocusChangeListener { v, hasFocus ->
                    if (hasFocus) {
                        editText.post {
                            editText.setSelection(editText.text?.length ?: 0)
                        }
                    }
                }
            }
        }
    }

    fun clear() {
        otpInputLayouts.mapNotNull { it.editText }.forEach { editText ->
            editText.setText("")
            otpInputLayouts[0].requestFocus()
        }
    }

    val otp: String get() = otpInputLayouts.mapNotNull { it.editText }.joinToString("") { it.text.toString() }

    companion object {
        private const val OTP_DELAY_MILLIS = 100L
    }
}