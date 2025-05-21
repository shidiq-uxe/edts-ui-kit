package id.co.edtslib.uikit.alert

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.text.parseAsHtml
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.button.Button.ButtonType
import id.co.edtslib.uikit.databinding.ViewAlertBinding
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.drawable
import id.co.edtslib.uikit.utils.inflater
import org.w3c.dom.Attr

class AlertBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(ContextThemeWrapper(context, R.style.Theme_EDTS_UIKit), attrs, defStyleAttr)  {

    var delegate: AlertBoxDelegate? = null

    private val binding = ViewAlertBinding.inflate(this.context.inflater, this, true)

    var alertType: AlertType = AlertType.LIGHT
        set(value) {
            field = value
            value.bindAlertType()
        }

    var cornerRadius: Float = 8.dp
        set(value) {
            field = value
            binding.root.shapeAppearanceModel = binding.root.shapeAppearanceModel.toBuilder()
                .setAllCornerSizes(cornerRadius)
                .build()
        }

    var strokeWidth: Float = 1.dp
        set(value) {
            field = value
            binding.root.strokeWidth = value.toInt()
        }

    @ColorInt
    var strokeColor: Int = context.color(alertType.strokeColor)
        set(value) {
            field = value
            binding.root.strokeColor = value
        }

    @ColorInt
    var alertBackgroundColor: Int = context.color(alertType.background)
        set(value) {
            field = value
            binding.root.setCardBackgroundColor(value)
        }

    var textAppearance = R.style.TextAppearance_Inter_Regular_P2
        set(value) {
            field = value

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                binding.tvAlertMessage.setTextAppearance(value)
            } else {
                binding.tvAlertMessage.setTextAppearance(context, value)
            }
        }

    var text: CharSequence = ""
        set(value) {
            field = value
            binding.tvAlertMessage.text = if (isHtml) value.toString().parseAsHtml() else value
        }

    var isHtml: Boolean = false
        set(value) {
            field = value
            binding.tvAlertMessage.text = if (value) text.toString().parseAsHtml() else text
        }

    var textColor: Int = context.color(R.color.black_60)
        set(value) {
            field = value
            binding.tvAlertMessage.setTextColor(value)
        }

    var buttonText: CharSequence = ""
        set(value) {
            field = value
            binding.btnAction.text = value
        }

    var buttonType: ButtonType = ButtonType.FILLED
        set(value) {
            field = value
            binding.btnAction.buttonType = value
        }

    var isButtonVisible: Boolean = false
        set(value) {
            field = value
            binding.btnAction.isVisible = value
        }

    var isStartIconVisible: Boolean = true
        set(value) {
            field = value
            binding.ivAlertIcon.isVisible = value
        }

    var isCloseIconVisible: Boolean = true
        set(value) {
            field = value
            binding.ivAlertClose.isVisible = value
        }

    var startIcon: Drawable = alertType.alertIcon
        set(value) {
            field = value
            binding.ivAlertIcon.setImageDrawable(value)
        }

    var closeIcon: Drawable = context.drawable(R.drawable.ic_close_16)
        set(value) {
            field = value
            binding.ivAlertClose.setButtonDrawable(value)
        }

    @ColorInt
    var startIconTint: Int? = null
        set(value) {
            field = value
            value?.let { binding.ivAlertIcon.setColorFilter(it) }
        }

    @ColorInt
    var closeIconTint: Int = context.color(R.color.black_50)
        set(value) {
            field = value
            value.let { binding.ivAlertClose.buttonIconTintList = ColorStateList.valueOf(it) }
        }

    private fun AlertType.bindAlertType() {
        binding.apply {
            root.apply {
                setCardBackgroundColor(context.color(this@bindAlertType.background))
                strokeColor = context.color(this@bindAlertType.strokeColor)
            }
            ivAlertIcon.setImageDrawable(this@bindAlertType.alertIcon)
        }
    }

    private val AlertType.background get() =  when(this) {
        AlertType.SUCCESS -> R.color.success_background
        AlertType.ERROR -> R.color.alert_background
        AlertType.WARNING -> R.color.warning_background
        AlertType.INFO -> R.color.info_background
        AlertType.LIGHT -> R.color.light_background
    }

    private val AlertType.strokeColor get() = when(this) {
        AlertType.SUCCESS -> R.color.success_border
        AlertType.ERROR -> R.color.alert_border
        AlertType.WARNING -> R.color.warning_border
        AlertType.INFO -> R.color.info_border
        AlertType.LIGHT -> R.color.light_border
    }

    private val AlertType.alertIcon get() = context.drawable(when(this) {
        AlertType.SUCCESS -> R.drawable.ic_success_16
        AlertType.ERROR -> R.drawable.ic_error_16
        AlertType.WARNING -> R.drawable.ic_attention_16
        AlertType.INFO -> R.drawable.ic_info_blue_16
        AlertType.LIGHT -> R.drawable.ic_info_16
    })

    init {
        initAttrs(attrs)

        binding.ivAlertClose.setOnClickListener {
            delegate?.onCloseClickListener(it)
        }

        binding.btnAction.setOnClickListener {
            delegate?.onButtonClickListener(it)
        }
    }

    private fun initAttrs(attr: AttributeSet?) {
        context.withStyledAttributes(attr, R.styleable.AlertBox) {
            val alertType = getInt(R.styleable.AlertBox_alertType, alertType.ordinal)

            this@AlertBox.strokeWidth = getDimension(R.styleable.AlertBox_strokeWidth, strokeWidth)
            this@AlertBox.strokeColor = getColor(R.styleable.AlertBox_strokeColor, strokeColor)

            this@AlertBox.cornerRadius = getDimension(R.styleable.AlertBox_cornerRadius, cornerRadius)
            this@AlertBox.alertBackgroundColor = getColor(R.styleable.AlertBox_backgroundColor, alertBackgroundColor)

            this@AlertBox.text = getString(R.styleable.AlertBox_text).toString()
            this@AlertBox.textAppearance = getResourceId(R.styleable.AlertBox_textAppearance, textAppearance)
            this@AlertBox.textColor = getColor(R.styleable.AlertBox_textColor, textColor)

            this@AlertBox.buttonText = getString(R.styleable.AlertBox_buttonText).toString()
            this@AlertBox.isButtonVisible = getBoolean(R.styleable.AlertBox_isButtonVisible, isButtonVisible)

            this@AlertBox.isStartIconVisible = getBoolean(R.styleable.AlertBox_isStartIconVisible, isStartIconVisible)
            this@AlertBox.isCloseIconVisible = getBoolean(R.styleable.AlertBox_isCloseIconVisible, isCloseIconVisible)

            this@AlertBox.startIcon = getDrawable(R.styleable.AlertBox_startIcon) ?: startIcon
            this@AlertBox.closeIcon = getDrawable(R.styleable.AlertBox_closeIcon) ?: closeIcon

            this@AlertBox.startIconTint = startIconTint?.let { getColor(R.styleable.AlertBox_startIconTint,  it) }
            this@AlertBox.closeIconTint = getColor(R.styleable.AlertBox_closeIconTint, closeIconTint)

            this@AlertBox.isHtml = getBoolean(R.styleable.AlertBox_isHtml, isHtml)

            if (hasValue(R.styleable.AlertBox_alertType)) {
                this@AlertBox.alertType = AlertType.values()[alertType]
            }
        }
    }

    fun showOpticalSpacing(enable: Boolean = true) {
        binding.ivAlertIcon.updateLayoutParams<MarginLayoutParams> {
            updateMargins(
                top = if (enable) 2.dp.toInt() else 0.dp.toInt(),
            )
        }
    }

    enum class AlertType {
        SUCCESS, ERROR, WARNING, INFO, LIGHT
    }
}