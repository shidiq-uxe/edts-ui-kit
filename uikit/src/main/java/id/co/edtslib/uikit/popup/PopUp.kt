package id.co.edtslib.uikit.popup

import android.content.Context
import android.view.Gravity
import android.view.View
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.marginStart
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.databinding.ViewDialogBinding
import id.co.edtslib.uikit.utils.alertDialog
import id.co.edtslib.uikit.utils.dimen
import id.co.edtslib.uikit.utils.disconnectBottom
import id.co.edtslib.uikit.utils.disconnectChains
import id.co.edtslib.uikit.utils.disconnectEnd
import id.co.edtslib.uikit.utils.disconnectHorizontalChain
import id.co.edtslib.uikit.utils.disconnectStart
import id.co.edtslib.uikit.utils.disconnectTop
import id.co.edtslib.uikit.utils.disconnectVerticalChain
import id.co.edtslib.uikit.utils.endToEndOf
import id.co.edtslib.uikit.utils.horizontalBias
import id.co.edtslib.uikit.utils.horizontalWeight
import id.co.edtslib.uikit.utils.htmlToString
import id.co.edtslib.uikit.utils.inflater
import id.co.edtslib.uikit.utils.marginBottom
import id.co.edtslib.uikit.utils.marginEnd
import id.co.edtslib.uikit.utils.marginHorizontal
import id.co.edtslib.uikit.utils.marginStart
import id.co.edtslib.uikit.utils.marginTop
import id.co.edtslib.uikit.utils.marginVertical
import id.co.edtslib.uikit.utils.startToStartOf
import id.co.edtslib.uikit.utils.topToBottomOf
import id.co.edtslib.uikit.utils.topToTopOf

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

    private fun ViewDialogBinding.adjustButtonVisibilityBasedOnActionText(
        positiveButtonText: String?,
        negativeButtonText: String?
    ) {
        this.btnPositive.isVisible = !positiveButtonText.isNullOrEmpty()
        this.btnNegative.isVisible = !negativeButtonText.isNullOrEmpty()
    }

    private fun ViewDialogBinding.adjustButtonPosition(
        isHorizontal: Boolean = true
    ) {
        val parent = this.root

        if (!isHorizontal) {
            btnPositive.disconnectBottom()

            btnPositive.marginVertical(parent, parent.context.dimen(R.dimen.dimen_36))
            btnPositive.marginHorizontal(parent, 0f)
            btnNegative.marginVertical(parent, parent.context.dimen(R.dimen.xxs))
            btnNegative.marginHorizontal(parent, 0f)

            btnPositive.topToBottomOf(tvMessage)
            btnPositive.startToStartOf(tvMessage)
            btnPositive.endToEndOf(tvMessage)

            btnNegative.topToBottomOf(btnPositive)
            btnNegative.startToStartOf(tvMessage)
            btnNegative.endToEndOf(tvMessage)
        }
    }

    /**
     * Displays a custom alert dialog with various configuration options.
     *
     * @param context The context in which the dialog should be displayed.
     * @param isCentered Determines whether the dialog content should be centered.
     * @param title The title text for the dialog.
     * @param message The message displayed in the dialog, using CharSequence for rich text.
     * @param displayAsHtml Determines if the title & message should be displayed as HTML or plainText.
     * @param isActionHorizontal Determines if the Button Should be Stacked or Chained Horizontally.
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
        isActionHorizontal: Boolean = true,
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
                    adjustButtonVisibilityBasedOnActionText(positiveButton, negativeButton)
                    adjustButtonPosition(isActionHorizontal)

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