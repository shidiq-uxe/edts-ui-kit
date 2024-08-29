package id.co.edtslib.uikit.popup

import android.content.Context
import android.view.Gravity
import android.view.View
import id.co.edtslib.uikit.databinding.ViewDialogBinding
import id.co.edtslib.uikit.utils.alertDialog
import id.co.edtslib.uikit.utils.htmlToString
import id.co.edtslib.uikit.utils.inflater

object PopUp {

    private val Context.binding get() = ViewDialogBinding.inflate(this.inflater)

    private fun ViewDialogBinding.centerAlignContents() {
        tvTitle.gravity = Gravity.CENTER
        tvMessage.gravity = Gravity.CENTER_HORIZONTAL
    }

    private fun ViewDialogBinding.bindContent(
        rawTitle: CharSequence,
        rawMessage: CharSequence,
        displayAsHtml: Boolean
    ) {
        val message = if (displayAsHtml) rawMessage.toString().htmlToString() else rawMessage
        val title = if (displayAsHtml) rawTitle.toString().htmlToString() else rawTitle

        tvTitle.text = title
        tvMessage.text = message
    }


    /**
     * Displays a custom alert dialog with various configuration options.
     *
     * @param context The context in which the dialog should be displayed.
     * @param isCentered Determines whether the dialog content should be centered.
     * @param title The title text for the dialog.
     * @param message The message displayed in the dialog, using CharSequence for rich text.
     * @param displayAsHtml Determines if the title & message should be displayed as HTML or plainText.
     * @param isFullScreen Indicates whether the dialog should be displayed in full-screen mode.
     * @param isDismissible Determines if the dialog can be dismissed by clicking outside or pressing the back button.
     * @param positiveButton The text for the positive button. If null or empty, the button will not be shown.
     * @param negativeButton The text for the negative button. If null or empty, the button will not be shown.
     * @param onBindView A lambda that allows additional binding or customization of the dialog's view after it's inflated.
     * @param onPositiveButtonClick A lambda executed when the positive button is clicked. Receives the button view and the AlertDialog instance.
     * @param onNegativeButtonClick A lambda executed when the negative button is clicked. Receives the button view and the AlertDialog instance.
     * @param onDismiss A lambda that is executed when the dialog is dismissed.
     *
     * The function leverages the `alertDialog` extension function to display the dialog.
     * It binds the title and message content, handles button visibility and click events, and supports optional view customization via `onBindView`.
     * The dialog can be customized further with options like `isCentered`, `isFullScreen`, and `isDismissible`.
     */
    fun show(
        context: Context,
        isCentered: Boolean = false,
        title: CharSequence,
        displayAsHtml: Boolean = false,
        message: CharSequence,
        isFullScreen: Boolean = false,
        isDismissible: Boolean = true,
        positiveButton: String? = null,
        negativeButton: String? = null,
        onBindView: (ViewDialogBinding) -> Unit = {},
        onPositiveButtonClick: (View, androidx.appcompat.app.AlertDialog) -> Unit = { _, _ -> },
        onNegativeButtonClick: (View, androidx.appcompat.app.AlertDialog) -> Unit = { _, _ -> },
        onDismiss: () -> Unit = {}
    ) {
        context.alertDialog(
            view = context.binding.root,
            fullScreen = isFullScreen,
            configureView = { view, dialog ->
                ViewDialogBinding.bind(view).apply {
                    bindContent(title, message, displayAsHtml)

                    if (!positiveButton.isNullOrEmpty()) {
                        btnPositive.text = positiveButton

                        btnPositive.setOnClickListener {
                            onPositiveButtonClick(btnPositive, dialog)
                        }
                    }

                    if (!negativeButton.isNullOrEmpty()) {
                        btnNegative.text = negativeButton

                        btnNegative.setOnClickListener {
                            onNegativeButtonClick(btnNegative, dialog)
                        }
                    }

                    if (isCentered) centerAlignContents()
                    onBindView(this)
                }
            }
        ).apply {
            setOnDismissListener { onDismiss() }
            setCancelable(isDismissible)
        }.show()
    }
}