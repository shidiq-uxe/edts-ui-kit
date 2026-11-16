package id.co.edtslib.uikit.alert

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.withStyledAttributes
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.button.Button
import id.co.edtslib.uikit.button.Button.ButtonType
import id.co.edtslib.uikit.core.alertbox.CoreAlertBox
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.drawable

@Deprecated("Use KlikInfoBox or PoinkuAlertBox with an explicit XML recipe. See docs/AlertBox.md.")
class AlertBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CoreAlertBox(ContextThemeWrapper(context, R.style.Theme_EDTS_UIKit), attrs, defStyleAttr, R.layout.view_alert) {

    var alertType: AlertType = AlertType.LIGHT
        set(value) {
            field = value
            applyTypeAppearance(
                background = this.context.color(value.background),
                border = this.context.color(value.border),
                icon = this.context.drawable(value.icon)
            )
        }

    var buttonType: ButtonType = ButtonType.FILLED
        set(value) {
            field = value
            (footerButton as Button).buttonType = value
        }

    var startIconVerticalAlignment: StartIconVerticalAlignment = StartIconVerticalAlignment.TOP
        set(value) {
            field = value
            iconAlignment = IconAlignment.entries[value.ordinal]
        }

    public override var cornerRadius: Float
        get() = super.cornerRadius
        set(value) {
            super.cornerRadius = value
        }

    public override var strokeWidth: Float
        get() = super.strokeWidth
        set(value) {
            super.strokeWidth = value
        }

    public override var strokeColor: Int
        get() = super.strokeColor
        set(value) {
            super.strokeColor = value
        }

    public override var alertBackgroundColor: Int
        get() = super.alertBackgroundColor
        set(value) {
            super.alertBackgroundColor = value
        }

    public override var textAppearance: Int
        get() = super.textAppearance
        set(value) {
            super.textAppearance = value
        }

    public override var textColor: Int
        get() = super.textColor
        set(value) {
            super.textColor = value
        }

    public override var startIconTint: Int?
        get() = super.startIconTint
        set(value) {
            super.startIconTint = value
        }

    public override var closeIconTint: Int
        get() = super.closeIconTint
        set(value) {
            super.closeIconTint = value
        }

    public override var actionTextAppearance: Int
        get() = super.actionTextAppearance
        set(value) {
            super.actionTextAppearance = value
        }

    public override var actionTextColor: Int
        get() = super.actionTextColor
        set(value) {
            super.actionTextColor = value
        }

    private val AlertType.background: Int
        get() = when (this) {
            AlertType.SUCCESS -> R.color.success_background
            AlertType.ERROR -> R.color.alert_background
            AlertType.WARNING -> R.color.warning_background
            AlertType.INFO -> R.color.info_background
            AlertType.LIGHT -> R.color.light_background
        }

    private val AlertType.border: Int
        get() = when (this) {
            AlertType.SUCCESS -> R.color.success_border
            AlertType.ERROR -> R.color.alert_border
            AlertType.WARNING -> R.color.warning_border
            AlertType.INFO -> R.color.info_border
            AlertType.LIGHT -> R.color.light_border
        }

    private val AlertType.icon: Int
        get() = when (this) {
            AlertType.SUCCESS -> R.drawable.ic_success_16
            AlertType.ERROR -> R.drawable.ic_error_16
            AlertType.WARNING -> R.drawable.ic_attention_16
            AlertType.INFO -> R.drawable.ic_info_blue_16
            AlertType.LIGHT -> R.drawable.ic_info_16
        }

    init {
        cornerRadius = 8.dp
        strokeWidth = 1.dp
        strokeColor = this.context.color(R.color.light_border)
        alertBackgroundColor = this.context.color(R.color.light_background)
        textAppearance = R.style.TextAppearance_Inter_Regular_P2
        textColor = this.context.color(R.color.black_60)
        startIcon = this.context.drawable(R.drawable.ic_info_16)
        closeIcon = this.context.drawable(R.drawable.ic_close_16)
        closeIconTint = this.context.color(R.color.black_50)
        actionTextAppearance = R.style.TextAppearance_Inter_SemiBold_Button_Text_Small
        actionTextColor = this.context.color(R.color.primary_30)
        initAttrs(attrs)
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

            this@AlertBox.startIconTint = startIconTint?.let { getColor(R.styleable.AlertBox_startIconTint, it) }
            this@AlertBox.closeIconTint = getColor(R.styleable.AlertBox_closeIconTint, closeIconTint)

            this@AlertBox.isHtml = getBoolean(R.styleable.AlertBox_isHtml, isHtml)

            this@AlertBox.startIconVerticalAlignment = StartIconVerticalAlignment.entries[
                getInt(R.styleable.AlertBox_startIconVerticalAlignment, 0)
                    .coerceIn(0, StartIconVerticalAlignment.entries.lastIndex)
            ]
            this@AlertBox.actionTextAppearance = getResourceId(R.styleable.AlertBox_actionTextAppearance, actionTextAppearance)
            this@AlertBox.actionTextColor = getColor(R.styleable.AlertBox_actionTextColor, actionTextColor)
            readInlineActionAttributes(attr)

            if (hasValue(R.styleable.AlertBox_alertType)) {
                this@AlertBox.alertType = AlertType.values()[alertType]
            }
        }
    }

    fun showOpticalSpacing(enable: Boolean = true) {
        setOpticalSpacingEnabled(enable)
    }

    enum class StartIconVerticalAlignment { TOP, CENTER, BOTTOM }

    enum class AlertType {
        SUCCESS, ERROR, WARNING, INFO, LIGHT
    }
}
