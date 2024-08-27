package id.co.edtslib.uikit.popup

import android.content.Context
import android.view.Gravity
import android.view.View
import id.co.edtslib.uikit.databinding.ViewDialogBinding
import id.co.edtslib.uikit.utils.alertDialog
import id.co.edtslib.uikit.utils.htmlToString
import id.co.edtslib.uikit.utils.inflater

class PopUp {

    companion object {
        private val Context.binding get() = ViewDialogBinding.inflate(this.inflater)

        private fun ViewDialogBinding.centerAlignContents() {
            tvTitle.gravity = Gravity.CENTER
            tvMessage.gravity = Gravity.CENTER_HORIZONTAL
        }

        private fun ViewDialogBinding.bindContent(
            title: String,
            message: String
        ) {
            tvTitle.text = title.htmlToString()
            tvMessage.text = message.htmlToString()
        }

        fun show(
            context: Context,
            isCentered: Boolean = false,
            title: String,
            message: String,
            isFullScreen: Boolean = false,
            isDismissible: Boolean = true,
            positiveButton: String? = null,
            negativeButton: String? = null,
            onBindView: (ViewDialogBinding) -> Unit = {},
            onPositiveButtonClick: (View) -> Unit = {},
            onNegativeButtonClick: (View) -> Unit = {},
            onDismiss: () -> Unit = {}
        ) {
            context.alertDialog(
                view = context.binding.apply {
                    bindContent(title, message)

                    if (!positiveButton.isNullOrEmpty()) {
                        btnPositive.text = positiveButton

                        btnPositive.setOnClickListener {
                            onPositiveButtonClick(btnPositive)
                        }
                    }

                    if (!negativeButton.isNullOrEmpty()) {
                        btnNegative.text = negativeButton

                        btnNegative.setOnClickListener {
                            onNegativeButtonClick(btnNegative)
                        }
                    }

                    if (isCentered) centerAlignContents()
                    onBindView(this)
                }.root,
                fullScreen = isFullScreen
            ).apply {
                setOnDismissListener { onDismiss() }
                setCancelable(isDismissible)
            }.show()
        }
    }
}