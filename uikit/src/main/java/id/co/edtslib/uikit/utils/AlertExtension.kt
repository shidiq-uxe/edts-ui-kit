package id.co.edtslib.uikit.utils

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.core.graphics.drawable.DrawableCompat
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
    @ColorInt backgroundColor: Int = this?.context.color(R.color.black_60),
    @StyleRes resTextAppearance: Int = -1,
    @ColorInt textColor: Int = Color.WHITE,
    messageHasStyle: Boolean = false,
    isAnchored: Boolean = false,
    anchoredView: View? = this,
    animationMode: Int = Snackbar.ANIMATION_MODE_FADE,
    bottomMargin: Int? = null,
    onViewCreated: (Snackbar) -> Unit = {},
    action: ((View) -> Unit)? = null
): Snackbar? {
    return this?.let {
        Snackbar.make(it, message, displayLength).apply {
            this.setBackgroundTint(backgroundColor)
            this.animationMode = animationMode

            with(this.view) {
                if (bottomMargin != null) {
                    val params = layoutParams as? FrameLayout.LayoutParams
                    params?.bottomMargin = bottomMargin
                    layoutParams = params
                }

                onViewCreated(this@apply)

                if (isAnchored) {
                    anchorView = anchoredView ?: this
                }

                findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
                    gravity = Gravity.CENTER
                    maxLines = 2
                    ellipsize = TextUtils.TruncateAt.END

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!messageHasStyle) {
                            setTextAppearance(
                                if (resTextAppearance != -1) resTextAppearance
                                else R.style.TextAppearance_Inter_Regular_B3
                            )
                        }
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
        }.also {
            it.show()
        }
    }
}


enum class AlertType {
    DEFAULT, ERROR
}

fun View?.alertSnack(
    message: CharSequence,
    messageHasStyle: Boolean = false,
    alertType: AlertType = AlertType.DEFAULT,
    actionText: CharSequence? = null,
    @DrawableRes startIconRes: Int? = null,
    bottomMargin: Int? = null,
    isAnchored: Boolean = false,
    anchoredView: View? = this,
    animationMode: Int = Snackbar.ANIMATION_MODE_FADE,
    action: ((View) -> Unit)? = null,
): Snackbar? {
   val backgroundColor = this?.context.color(
       when(alertType) {
           AlertType.DEFAULT -> R.color.black_60
           AlertType.ERROR -> R.color.alert_primary
       }
   )

    val textColor = this?.context.color(
        when(alertType) {
            AlertType.DEFAULT -> R.color.white
            AlertType.ERROR -> R.color.white
        }
    )

    return snack(
        message = message,
        backgroundColor = backgroundColor,
        resTextAppearance = R.style.TextAppearance_Inter_Regular_B3,
        textColor = textColor,
        messageHasStyle = messageHasStyle,
        actionText = actionText,
        displayLength = Snackbar.LENGTH_LONG,
        bottomMargin = bottomMargin,
        isAnchored = isAnchored,
        anchoredView = anchoredView,
        animationMode = animationMode,
        onViewCreated = { snackBar ->
            startIconRes?.let { res ->
                snackBar.setAlertIcon(res)
            }
        },
        action = action
    )
}

private fun Snackbar.setAlertIcon(@DrawableRes drawableRes: Int, colorInt: Int = Color.WHITE) {
    val drawable = context.drawable(drawableRes).apply {
        DrawableCompat.setTint(this, colorInt)
    }

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