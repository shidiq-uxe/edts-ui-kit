package id.co.edtslib.uikit.alert

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.databinding.ViewAlertBinding
import id.co.edtslib.uikit.utils.AlertType
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.colorStateList
import id.co.edtslib.uikit.utils.drawable
import id.co.edtslib.uikit.utils.inflater

// Todo : Adjust More Customization & Animation
class AlertBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr)  {

    val delegate: AlertBoxDelegate? = null

    private val binding = ViewAlertBinding.inflate(context.inflater, this, true)

    private val alertBoxOptions = AlertBoxOptions()

    var alertType: AlertType = AlertType.LIGHT
        set(value) {
            value.bindAlertType()

            field = value
        }

    var textAppearance = R.style.TextAppearance_Inter_Regular_B2
        set(value) {
            field = value

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                binding.tvAlertMessage.setTextAppearance(value)
            } else {
                binding.tvAlertMessage.setTextAppearance(context, value)
            }

            binding.tvAlertMessage.setTextColor(context.color(alertType.textColor))
        }

    var text: CharSequence = ""
        set(value) {
            binding.tvAlertMessage.text = value
            field = value
        }

    private fun AlertType.bindAlertType() {
        Log.e(this@AlertBox.javaClass.name, "bindAlertType $this")

        binding.apply {
            tvAlertMessage.setTextColor(ContextCompat.getColor(context, this@bindAlertType.textColor))

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

    private val AlertType.textColor get() = when(this) {
        AlertType.SUCCESS -> R.color.success_primary
        AlertType.ERROR -> R.color.alert_primary
        AlertType.WARNING -> R.color.warning_primary
        AlertType.INFO -> R.color.info_primary
        AlertType.LIGHT -> R.color.light_primary
    }

    private val AlertType.strokeColor get() = when(this) {
        AlertType.SUCCESS -> R.color.success_border
        AlertType.ERROR -> R.color.alert_border
        AlertType.WARNING -> R.color.warning_border
        AlertType.INFO -> R.color.info_border
        AlertType.LIGHT -> R.color.light_border
    }

    private val AlertType.alertIcon get() = context.drawable(when(this) {
            AlertType.SUCCESS -> R.drawable.ic_alert_success
            AlertType.ERROR -> R.drawable.ic_alert_error
            AlertType.WARNING -> R.drawable.ic_alert_warning
            AlertType.INFO -> R.drawable.ic_alert_info
            AlertType.LIGHT -> R.drawable.ic_alert_light
        })

    init {
        binding.ivAlertClose.setOnClickListener {
            delegate?.onCloseClickListener(it)
        }

        AlertBoxAttrsFactory.initAttrs(context, attrs, alertBoxOptions)

        textAppearance = alertBoxOptions.textAppearance
        alertType = alertBoxOptions.alertType
        alertBoxOptions.text?.let { text = it }
    }
}