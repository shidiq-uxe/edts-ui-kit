package id.co.edtslib.uikit.core.otp

import android.content.Context
import android.util.AttributeSet
import android.view.accessibility.AccessibilityNodeInfo
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import androidx.appcompat.widget.AppCompatEditText
import id.co.edtslib.uikit.R

internal class OtpEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    var onBackspaceAtStart: (() -> Boolean)? = null

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        if (isActivated && isEnabled) {
            info.isContentInvalid = true
            info.error = context.getString(R.string.core_otp_invalid_code)
        }
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        val connection = super.onCreateInputConnection(outAttrs) ?: return null
        return object : InputConnectionWrapper(connection, false) {
            override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
                return moveToPreviousCell(beforeLength, afterLength) ||
                    super.deleteSurroundingText(beforeLength, afterLength)
            }

            override fun deleteSurroundingTextInCodePoints(beforeLength: Int, afterLength: Int): Boolean {
                return moveToPreviousCell(beforeLength, afterLength) ||
                    super.deleteSurroundingTextInCodePoints(beforeLength, afterLength)
            }
        }
    }

    private fun moveToPreviousCell(beforeLength: Int, afterLength: Int): Boolean {
        return beforeLength == 1 && afterLength == 0 && onBackspaceAtStart?.invoke() == true
    }
}
