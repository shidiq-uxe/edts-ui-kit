package id.co.edtslib.uikit.utils

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import id.co.edtslib.uikit.R
import kotlin.math.roundToInt

/**
 * Showing [Snackbar] with certain Attribute Options
 * @param message used for display message to user
 * @param actionText is display action for user and callback for action with certain Callback
 * @param displayLength is Time for a [Snackbar] displayed to user
 * @param isAnchored used for anchoring [Snackbar] to Extended [View]
 * @param action Execute all process inside lambda function for [actionText] Callback
 */
fun View?.snack(
    message: CharSequence,
    actionText: CharSequence? = null,
    displayLength: Int = Snackbar.LENGTH_SHORT,
    @ColorInt backgroundColor: Int = Color.BLACK,
    @StyleRes resTextAppearance: Int = -1,
    @ColorInt textColor: Int = Color.WHITE,
    isAnchored: Boolean = false,
    anchoredView: View? = this,
    onViewCreated: (Snackbar) -> Unit = {},
    action: ((View) -> Unit)? = null
) {
    this?.let {
        Snackbar.make(it, message, displayLength).apply {
            this.setBackgroundTint(
                if (backgroundColor != Color.BLACK) backgroundColor
                else context.color(R.color.black_70)
            )

            with(this.view) {
                onViewCreated(this@apply)

                if (isAnchored) {
                    anchorView = anchoredView ?: this
                }

                findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
                    gravity = Gravity.CENTER
                    maxLines = 2
                    ellipsize = TextUtils.TruncateAt.END

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        setTextAppearance(
                            if (resTextAppearance != -1) resTextAppearance
                            else R.style.TextAppearance_Inter_Regular_B2
                        )
                    }

                    this.setTextColor(
                        if (textColor != Color.WHITE) textColor
                        else Color.WHITE
                    )
                }

                if (actionText != null) {
                    findViewById<TextView>(com.google.android.material.R.id.snackbar_action)

                    setAction(actionText) {
                        action?.invoke(this)
                    }
                }
            }
        }.show()
    }
}

enum class AlertType {
    SUCCESS, ERROR, WARNING, INFO, LIGHT
}

fun View?.alertSnack(
    message: CharSequence,
    alertType: AlertType,
    isAnchored: Boolean = false,
    anchoredView: View? = this,
    action: ((View) -> Unit)? = null,
) {
   val backgroundColor = this?.context.color(
       when(alertType) {
           AlertType.SUCCESS -> R.color.success_background
           AlertType.ERROR -> R.color.alert_background
           AlertType.WARNING -> R.color.warning_background
           AlertType.INFO -> R.color.info_background
           AlertType.LIGHT -> R.color.light_background
       }
   )

    val textColor = this?.context.color(
        when(alertType) {
            AlertType.SUCCESS -> R.color.success_primary
            AlertType.ERROR -> R.color.alert_primary
            AlertType.WARNING -> R.color.warning_primary
            AlertType.INFO -> R.color.info_primary
            AlertType.LIGHT -> R.color.light_primary
        }
    )

    val borderWidth = this?.context.dimen(R.dimen.dimen_1).roundToInt()

    val drawable = GradientDrawable().apply {
        this.setStroke(borderWidth, textColor)
    }

    snack(
        message = message,
        backgroundColor = backgroundColor,
        resTextAppearance = R.style.TextAppearance_Inter_Regular_B2,
        textColor = textColor,
        displayLength = Snackbar.LENGTH_LONG,
        isAnchored = isAnchored,
        anchoredView = anchoredView,
        onViewCreated = {
            it.setAlertIcon(alertType)
            it.setCloseIcon()
        },
        action = action
    )
}

private fun Snackbar.setAlertIcon(alertType: AlertType) {
    val drawable = context.drawable(
        when(alertType) {
            AlertType.SUCCESS -> R.drawable.ic_alert_success
            AlertType.ERROR -> R.drawable.ic_alert_error
            AlertType.WARNING -> R.drawable.ic_alert_warning
            AlertType.INFO -> R.drawable.ic_alert_info
            AlertType.LIGHT -> R.drawable.ic_alert_light
        }
    )

    view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
        setDrawable(drawableLeft = drawable)

        compoundDrawablePadding = context.dimen(R.dimen.xxs).roundToInt()
    }
}

private fun Snackbar.setCloseIcon() {
    val closeDrawable = context.drawable(R.drawable.ic_close_16)

    setAction(" ") {
        dismiss()
    }

    view.findViewById<TextView>(com.google.android.material.R.id.snackbar_action).apply {
        setDrawable(drawableRight = closeDrawable)
        isVisible = true
    }
}