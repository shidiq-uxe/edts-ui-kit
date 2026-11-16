package id.co.edtslib.uikit.core.snackbar

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.updateLayoutParams
import androidx.core.widget.TextViewCompat
import com.google.android.material.snackbar.Snackbar
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.dimen
import id.co.edtslib.uikit.utils.drawable
import kotlin.math.roundToInt

/** In-app feedback presented by Material Snackbar. Use a product recipe for branded appearance. */
open class CoreSnackbar protected constructor(
    @param:StyleRes private val infoTheme: Int,
    @param:StyleRes private val errorTheme: Int
) {

    enum class Variant { INFO, ERROR }

    /**
     * Shows feedback from a view attached to the screen. The icon is decorative.
     * Material dismisses the snackbar after the action runs or the duration expires.
     */
    fun show(
        host: View,
        message: CharSequence,
        variant: Variant = Variant.INFO,
        @DrawableRes iconRes: Int? = null,
        actionText: CharSequence? = null,
        anchor: View? = null,
        duration: Int = Snackbar.LENGTH_LONG,
        animationMode: Int = Snackbar.ANIMATION_MODE_FADE,
        onAction: ((View) -> Unit)? = null
    ): Snackbar {
        val recipe = when (variant) {
            Variant.INFO -> infoTheme
            Variant.ERROR -> errorTheme
        }
        val themedContext = ContextThemeWrapper(host.context, recipe)
        return host.showMaterialSnackbar(message, duration, actionText, onAction, themedContext) {
            // The parent creates Material's initial layout params with the host theme.
            val margin = themedContext.dimen(R.dimen.s).roundToInt()
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                setMargins(margin, margin, margin, margin)
            }
            anchorView = anchor
            this.animationMode = animationMode
            val messageView = view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            val actionView = view.findViewById<TextView>(com.google.android.material.R.id.snackbar_action)
            val lineHeight = themedContext.dimen(R.dimen.snackbar_line_height).roundToInt()
            TextViewCompat.setLineHeight(messageView, lineHeight)
            TextViewCompat.setLineHeight(actionView, lineHeight)
            if (iconRes != null) {
                val size = themedContext.dimen(R.dimen.s).roundToInt()
                val icon = themedContext.drawable(iconRes)?.mutate()?.apply {
                    setBounds(0, 0, size, size)
                    DrawableCompat.setTint(this, messageView.currentTextColor)
                }
                messageView.setCompoundDrawablesRelative(icon, null, null, null)
            }
        }
    }

    companion object : CoreSnackbar(
        R.style.ThemeOverlay_EDTS_UIKit_Snackbar_Recipe,
        R.style.ThemeOverlay_EDTS_UIKit_Snackbar_Recipe_Error
    )
}

internal fun View.showMaterialSnackbar(
    message: CharSequence,
    duration: Int,
    actionText: CharSequence?,
    onAction: ((View) -> Unit)?,
    themedContext: Context? = null,
    configure: Snackbar.() -> Unit
): Snackbar {
    val snackbar = if (themedContext == null) {
        Snackbar.make(this, message, duration)
    } else {
        Snackbar.make(themedContext, this, message, duration)
    }
    return snackbar.apply {
        configure()
        if (actionText != null) {
            setAction(actionText) { onAction?.invoke(it) }
        }
    }.also(Snackbar::show)
}
