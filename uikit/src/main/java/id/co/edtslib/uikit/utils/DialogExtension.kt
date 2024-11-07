package id.co.edtslib.uikit.utils

import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.co.edtslib.uikit.R

enum class DialogStyle {
    DEFAULT, SIMPLE, SINGLE, MULTI
}

fun Context.dialogBuilder(
    title: String? = null,
    message: String? = null,
    icon: Drawable? = null,
    centered: Boolean = false,
    isCancelable: Boolean = true,
    style: DialogStyle = DialogStyle.DEFAULT,
    items: Array<String> = emptyArray(),
    view: ((MaterialAlertDialogBuilder) -> View)? = null,
    positiveMessage: String? = null,
    onClickedAction: ((dialog: DialogInterface, position: Int) -> Unit)? = null,
    onMultiChoiceAction: ((dialog: DialogInterface, position: Int, checked: Boolean) -> Unit)? = null,
): MaterialAlertDialogBuilder {
    val styleRes =
        if (centered) R.style.ThemeOverlay_EDTS_UIKit_MaterialAlertDialog_Centered
        else R.style.ThemeOverlay_EDTS_UIKit_MaterialAlertDialog

    return MaterialAlertDialogBuilder(this, styleRes).apply {
        if (title != null) {
            setTitle(title)
        }
        if (message != null) {
            setMessage(message)
        }
        if (icon != null) {
            setIcon(icon)
        }
        if (view != null) {
            setView(view(this))
        }

        setCancelable(isCancelable)

        if (onClickedAction != null) {
            setPositiveButton(positiveMessage) { dialogInterface, position ->
                onClickedAction(dialogInterface, position)
            }
        }

        when {
            style == DialogStyle.DEFAULT && items.isEmpty() -> return@apply
            style == DialogStyle.SIMPLE && items.isNotEmpty() -> setItems(items) { dialog, which ->
                if (onClickedAction != null) onClickedAction(dialog, which)
            }

            style == DialogStyle.SINGLE && items.isNotEmpty() -> setSingleChoiceItems(
                items,
                0
            ) { dialog, which ->
                if (onClickedAction != null) onClickedAction(dialog, which)
            }

            style == DialogStyle.MULTI && items.isNotEmpty() -> setMultiChoiceItems(
                items,
                booleanArrayOf(false)
            ) { dialog, which, isChecked ->
                if (onMultiChoiceAction != null) onMultiChoiceAction(dialog, which, isChecked)
            }
        }
    }
}

fun Context.alertDialog(
    view: View,
    isCancelable: Boolean = true,
    fullScreen: Boolean = true,
    configureView: (view: View, dialog: AlertDialog) -> Unit
): AlertDialog {
    val alertDialog = dialogBuilder(view = { view }, isCancelable = isCancelable).create()

    alertDialog.apply {
        if (fullScreen) {
            val params = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
            }
            window?.attributes = params
        }

        // Configure the view with the AlertDialog instance
        configureView(view, this)
    }

    return alertDialog
}
