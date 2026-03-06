package id.co.edtslib.uikit.popup

import android.content.Context
import android.view.Gravity
import android.view.View
import androidx.core.view.isVisible
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.databinding.ViewDialogBinding
import id.co.edtslib.uikit.utils.alertDialog
import id.co.edtslib.uikit.utils.bottomToBottomOf
import id.co.edtslib.uikit.utils.dimen
import id.co.edtslib.uikit.utils.disconnectBottom
import id.co.edtslib.uikit.utils.disconnectEnd
import id.co.edtslib.uikit.utils.disconnectStart
import id.co.edtslib.uikit.utils.disconnectTop
import id.co.edtslib.uikit.utils.endToEndOf
import id.co.edtslib.uikit.utils.endToStartOf
import id.co.edtslib.uikit.utils.htmlToString
import id.co.edtslib.uikit.utils.inflater
import id.co.edtslib.uikit.utils.loadAny
import id.co.edtslib.uikit.utils.marginEnd
import id.co.edtslib.uikit.utils.marginHorizontal
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
        tvSupportMessage.gravity = Gravity.CENTER_HORIZONTAL
    }

    private fun ViewDialogBinding.bindContent(
        image: Any?,
        rawTitle: CharSequence,
        rawMessage: CharSequence,
        displayAsHtml: Boolean,
        rawSupportMessage: CharSequence?,
        isTitleLarge: Boolean = false
    ) {
        val message = if (displayAsHtml) rawMessage.toString().htmlToString() else rawMessage
        val title = if (displayAsHtml) rawTitle.toString().htmlToString() else rawTitle
        val supportMessage = if (displayAsHtml) rawSupportMessage?.toString()?.htmlToString() else rawSupportMessage

        val hasImage = image != null
        ivDialogImage.apply {
            isVisible = hasImage
            loadAny(image)
            adjustCloseButton(hasImage)
        }

        tvTitle.text = title
        tvMessage.text = message

        val titleStyle = if (isTitleLarge || image != null) {
            R.style.TextAppearance_Inter_Semibold_D4
        } else {
            R.style.TextAppearance_Inter_Semibold_H1
        }
        tvTitle.setTextAppearance(titleStyle)

        tvSupportMessage.apply {
            isVisible = !supportMessage.isNullOrEmpty()
            text = supportMessage
        }
    }

    private fun ViewDialogBinding.adjustButtonVisibilityBasedOnActionText(
        positiveButtonText: String?,
        negativeButtonText: String?
    ) {
        this.btnPositive.isVisible = !positiveButtonText.isNullOrEmpty()
        this.btnNegative.isVisible = !negativeButtonText.isNullOrEmpty()
    }

    private fun ViewDialogBinding.adjustButtonPosition(
        isHorizontal: Boolean = true,
        isImageVisible: Boolean = false
    ) {
        val parent = this.root

        val marginTopValue = if (isImageVisible) {
            parent.context.dimen(R.dimen.m2)
        } else {
            parent.context.dimen(R.dimen.dimen_36)
        }

        if (!isHorizontal) {
            btnPositive.apply {
                disconnectBottom()

                marginTop(parent, marginTopValue)
                marginHorizontal(parent, 0f)

                topToBottomOf(tvSupportMessage)
                startToStartOf(tvMessage)
                endToEndOf(tvMessage)
            }

            btnNegative.apply {
                marginVertical(parent, parent.context.dimen(R.dimen.xxs))
                marginHorizontal(parent, 0f)

                topToBottomOf(btnPositive)
                startToStartOf(tvMessage)
                endToEndOf(tvMessage)
            }
        } else {
            btnPositive.marginTop(parent, marginTopValue)
            btnNegative.marginTop(parent, marginTopValue)
        }
    }

    private fun ViewDialogBinding.adjustCloseButton(
        isImageVisible: Boolean
    ) {
        val parent = this.root

        ivAlertClose.apply {
            disconnectStart()
            disconnectTop()
            disconnectBottom()
            disconnectEnd()
        }
        tvTitle.disconnectEnd()

        if (isImageVisible) {
            ivAlertClose.apply {
                marginTop(parent, parent.context.dimen(R.dimen.s))
                marginEnd(parent, parent.context.dimen(R.dimen.s))

                topToTopOf(parent)
                endToEndOf(parent)
            }

            tvTitle.apply {
                marginEnd(parent, parent.context.dimen(R.dimen.s))

                endToEndOf(parent)
            }
        } else {
            ivAlertClose.apply {
                marginTop(parent, 0f)
                marginEnd(parent, parent.context.dimen(R.dimen.s))

                topToTopOf(tvTitle)
                bottomToBottomOf(tvTitle)
                endToEndOf(parent)
            }

            tvTitle.apply {
                marginEnd(parent, parent.context.dimen(R.dimen.s))
                endToStartOf(ivAlertClose)
            }
        }
        parent.requestLayout()
    }


    /**
     * Displays a custom alert dialog with various configuration options.
     *
     * @param context The context in which the dialog should be displayed.
     * @param isCentered Determines whether the dialog content should be centered.
     * @param image The image to be displayed at the top of the dialog.
     * @param title The title text for the dialog.
     * @param isTitleLarge Determines whether the title should use a larger text appearance style.
     * @param message The message displayed in the dialog, using CharSequence for rich text.
     * @param supportMessage An optional secondary message displayed below the main message.
     * @param displayAsHtml Determines if the title & message should be displayed as HTML or plainText.
     * @param isActionHorizontal Determines if the Button Should be Stacked or Chained Horizontally.
     * @param isFullScreen Indicates whether the dialog should be displayed in full-screen mode.
     * @param isDismissible Determines if the dialog can be dismissed by clicking outside or pressing the back button.
     * @param isCloseButtonVisible Indicates whether the close (X) button should be displayed.
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
        image: Any? = null,
        title: CharSequence,
        isTitleLarge: Boolean = false,
        displayAsHtml: Boolean = false,
        message: CharSequence,
        supportMessage: CharSequence? = null,
        isActionHorizontal: Boolean = true,
        isFullScreen: Boolean = false,
        isDismissible: Boolean = true,
        isCloseButtonVisible: Boolean = false,
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
                    bindContent(image, title, message, displayAsHtml, supportMessage, isTitleLarge)
                    adjustButtonVisibilityBasedOnActionText(positiveButton, negativeButton)
                    adjustButtonPosition(isActionHorizontal, image != null)

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

                    ivAlertClose.isVisible = isCloseButtonVisible
                    if (isCloseButtonVisible) {
                        ivAlertClose.setOnClickListener {
                            dialog.dismiss()
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