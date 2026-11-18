package id.co.edtslib.uikit.otp

import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.doOnLayout
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.core.otp.OtpInputController
import id.co.edtslib.uikit.databinding.TextFieldOtpBinding
import id.co.edtslib.uikit.textfield.TextField
import id.co.edtslib.uikit.utils.hapticfeedback.HapticFeedback
import id.co.edtslib.uikit.utils.inflater
import id.co.edtslib.uikit.utils.vibrateAnimation
import id.co.edtslib.uikit.utils.vibratePhone

class OtpGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    var delegate: OtpDelegate? = null

    private var otpCount: Int = 4
    private var marginBetween: Int = 8 // default margin in dp

    private val otpInputLayouts = mutableListOf<TextField>()
    private lateinit var inputController: OtpInputController

    var isError: Boolean
        get() = otpInputLayouts.all { it.isError }
        set(value) = otpInputLayouts.forEach {
            it.isError = value

            if (value && shouldAnimateError) {
                it.vibrateAnimation()

                if (shouldVibrate) context.vibratePhone(HapticFeedback.ERROR)
            }
        }

    var shouldAnimateError: Boolean = true
    var shouldVibrate: Boolean = true

    init {
        context.withStyledAttributes(attrs, R.styleable.OtpGroup, defStyleAttr, 0) {
            otpCount = getInt(R.styleable.OtpGroup_otpCount, otpCount)
            marginBetween = getDimensionPixelSize(R.styleable.OtpGroup_marginBetween, marginBetween)
            isError = getBoolean(R.styleable.OtpGroup_isError, isError)
            shouldAnimateError = getBoolean(R.styleable.OtpGroup_animateError, shouldAnimateError)
            shouldVibrate = getBoolean(R.styleable.OtpGroup_vibrateOnError, shouldVibrate)
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

        val editors = otpInputLayouts.map { it.editText }
        editors.forEachIndexed { index, editor ->
            editor?.filters = arrayOf(InputFilter.LengthFilter(editors.size - index))
        }
        inputController = OtpInputController(
            editors = editors,
            notifyForEachChange = true,
            requestFocusAt = { otpInputLayouts[it].requestFocus() },
            onComplete = { delegate?.setOnOtpCompleteListener(it) },
            onChange = { delegate?.setOnTextChangeListener(it) }
        )
    }

    fun clear() {
        inputController.clear()
    }

    val otp: String get() = inputController.value

    companion object {
        private const val OTP_DELAY_MILLIS = 100L
    }
}