package id.co.edtslib.uikit.alert

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.withStyledAttributes
import androidx.core.text.parseAsHtml
import androidx.core.view.isVisible
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.button.Button.ButtonType
import id.co.edtslib.uikit.databinding.ViewAlertBinding
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.drawable
import id.co.edtslib.uikit.utils.inflater

class AlertBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(ContextThemeWrapper(context, R.style.Theme_EDTS_UIKit), attrs, defStyleAttr)  {

    var delegate: AlertBoxDelegate? = null

    var startIconVerticalAlignment: StartIconVerticalAlignment = StartIconVerticalAlignment.TOP
        set(value) {
            field = value
            updateStartIconAlignment()
        }

    var actionText: CharSequence = ""
        set(value) {
            field = value
            binding.tvAction.text = value
        }

    @StyleRes
    var actionTextAppearance: Int = R.style.TextAppearance_Inter_SemiBold_Button_Text_Small
        set(value) {
            field = value
            binding.tvAction.setTextAppearance(value)
        }

    @ColorInt
    var actionTextColor: Int = context.color(R.color.primary_30)
        set(value) {
            field = value
            binding.tvAction.setTextColor(value)
        }

    var actionMaxLines: Int = 1
        set(value) {
            require(value > 0) { "actionMaxLines must be positive" }
            field = value
            binding.tvAction.maxLines = value
        }

    var actionEllipsize: TextUtils.TruncateAt? = TextUtils.TruncateAt.END
        set(value) {
            field = value
            binding.tvAction.ellipsize = value
        }

    var actionEndIcon: Drawable? = null
        set(value) {
            field = value
            updateActionIcon()
        }

    @ColorInt
    var actionEndIconTint: Int? = null
        set(value) {
            field = value
            updateActionIcon()
        }

    /** Spacing between the inline action label and icon, in pixels. */
    var actionIconPadding: Int = 4.dp.toInt()
        set(value) {
            field = value
            binding.tvAction.compoundDrawablePadding = value
        }

    var isActionVisible: Boolean = false
        set(value) {
            field = value
            binding.tvAction.isVisible = value
        }

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

    private val binding = ViewAlertBinding.inflate(this.context.inflater, this, true)
    private var isOpticalSpacingEnabled = true

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

        binding.tvAction.setOnClickListener {
            delegate?.onActionClickListener(it)
        }
    }

    private fun updateStartIconAlignment() {
        val container = binding.ivAlertIcon.parent as ConstraintLayout
        val constraints = ConstraintSet().apply { clone(container) }
        constraints.clear(R.id.ivAlertIcon, ConstraintSet.TOP)
        constraints.clear(R.id.ivAlertIcon, ConstraintSet.BOTTOM)
        if (startIconVerticalAlignment != StartIconVerticalAlignment.BOTTOM) {
            constraints.connect(
                R.id.ivAlertIcon,
                ConstraintSet.TOP,
                R.id.tvAlertMessage,
                ConstraintSet.TOP,
                if (startIconVerticalAlignment == StartIconVerticalAlignment.TOP && isOpticalSpacingEnabled) 2.dp.toInt() else 0,
            )
        }
        if (startIconVerticalAlignment != StartIconVerticalAlignment.TOP) {
            constraints.connect(R.id.ivAlertIcon, ConstraintSet.BOTTOM, R.id.tvAlertMessage, ConstraintSet.BOTTOM)
        }
        constraints.applyTo(container)
    }

    private fun updateActionIcon() {
        val icon = actionEndIcon?.mutate()
        actionEndIconTint?.let { icon?.setTint(it) }
        binding.tvAction.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, icon, null)
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

            this@AlertBox.startIconVerticalAlignment = StartIconVerticalAlignment.entries[
                getInt(R.styleable.AlertBox_startIconVerticalAlignment, startIconVerticalAlignment.ordinal)
                    .coerceIn(0, StartIconVerticalAlignment.entries.lastIndex)
            ]
            this@AlertBox.actionText = getText(R.styleable.AlertBox_actionText) ?: actionText
            this@AlertBox.actionTextAppearance = getResourceId(R.styleable.AlertBox_actionTextAppearance, actionTextAppearance)
            this@AlertBox.actionTextColor = getColor(R.styleable.AlertBox_actionTextColor, actionTextColor)
            this@AlertBox.actionMaxLines = getInt(R.styleable.AlertBox_actionMaxLines, actionMaxLines).coerceAtLeast(1)
            this@AlertBox.actionEllipsize = when (getInt(R.styleable.AlertBox_actionEllipsize, 3)) {
                1 -> TextUtils.TruncateAt.START
                2 -> TextUtils.TruncateAt.MIDDLE
                3 -> TextUtils.TruncateAt.END
                else -> null
            }
            this@AlertBox.actionEndIcon = getDrawable(R.styleable.AlertBox_actionEndIcon)
            if (hasValue(R.styleable.AlertBox_actionEndIconTint)) {
                this@AlertBox.actionEndIconTint = getColor(R.styleable.AlertBox_actionEndIconTint, actionTextColor)
            }
            this@AlertBox.actionIconPadding = getDimensionPixelSize(R.styleable.AlertBox_actionIconPadding, actionIconPadding)
            this@AlertBox.isActionVisible = getBoolean(R.styleable.AlertBox_isActionVisible, isActionVisible)

            if (hasValue(R.styleable.AlertBox_alertType)) {
                this@AlertBox.alertType = AlertType.values()[alertType]
            }
        }
    }

    fun showOpticalSpacing(enable: Boolean = true) {
        isOpticalSpacingEnabled = enable
        updateStartIconAlignment()
    }

    enum class StartIconVerticalAlignment {
        TOP, CENTER, BOTTOM
    }

    enum class AlertType {
        SUCCESS, ERROR, WARNING, INFO, LIGHT
    }
}
