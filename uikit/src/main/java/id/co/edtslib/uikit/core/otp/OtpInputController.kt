package id.co.edtslib.uikit.core.otp

import android.view.KeyEvent
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged

/** Coordinates digit editors for both the legacy group and the XML recipes. */
internal class OtpInputController(
    private val editors: List<EditText?>,
    private val notifyForEachChange: Boolean,
    private val requestFocusAt: (Int) -> Boolean = { editors.getOrNull(it)?.requestFocus() == true },
    private val onComplete: (String) -> Unit,
    private val onChange: (String) -> Unit
) {

    val value: String
        get() = editors.filterNotNull().joinToString("") { it.text?.toString().orEmpty() }

    private var batchDepth = 0

    init {
        editors.forEachIndexed { index, editor -> editor?.let { connect(it, index) } }
    }
    fun clear() {
        if (notifyForEachChange) {
            editors.filterNotNull().forEach { editor ->
                editor.setText("")
                requestFocusAt(0)
            }
            return
        }
        batch(notify = value.isNotEmpty()) {
            editors.filterNotNull().forEach { editor -> editor.setText("") }
            requestFocusAt(0)
        }
    }

    fun setValue(digits: String, notify: Boolean) {
        val sanitized = digits.filter(Char::isDigit).take(editors.size)
        if (sanitized == value) return
        batch(notify = notify) {
            editors.forEachIndexed { index, editor -> editor?.setText(sanitized.getOrNull(index)?.toString().orEmpty()) }
        }
    }

    private fun publish() {
        val otp = value
        if (otp.length == editors.size) onComplete(otp)
        if (notifyForEachChange || value == otp) onChange(otp)
    }

    private inline fun batch(notify: Boolean, action: () -> Unit) {
        batchDepth++
        try {
            action()
        } finally {
            batchDepth--
        }
        if (notify && batchDepth == 0) publish()
    }

    private fun connect(editor: EditText, index: Int) {
        editor.doAfterTextChanged { editable ->
            if (notifyForEachChange) {
                distribute(index)
                if (editable != null && editable.length > 1) editable.delete(1, editable.length)
                publish()
                return@doAfterTextChanged
            }

            editor.isSelected = !editable.isNullOrEmpty()
            if (batchDepth == 0) {
                batch(notify = true) {
                    distribute(index)
                    if (editable != null && editable.length > 1) editable.delete(1, editable.length)
                }
            }
        }

        fun moveFocusBack(): Boolean {
            return index > 0 && editor.length() == 0 && requestFocusAt(index - 1)
        }
        editor.setOnKeyListener { _, keyCode, event ->
            val moved = keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN && moveFocusBack()
            moved && !notifyForEachChange
        }
        if (editor is OtpEditText) editor.onBackspaceAtStart = ::moveFocusBack
        editor.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                editor.post {
                    if (notifyForEachChange) editor.setSelection(editor.length()) else if (editor.hasFocus()) editor.selectAll()
                }
            }
        }
    }

    private fun distribute(index: Int) {
        val entered = editors[index]?.text?.toString().orEmpty()
        if (notifyForEachChange) {
            val next = editors.getOrNull(index + 1) ?: return
            when {
                entered.length == 1 -> requestFocusAt(index + 1)
                entered.length > 1 -> next.setText(entered.substring(1))
            }
            return
        }

        if (entered.isEmpty()) return
        entered.drop(1).forEachIndexed { offset, digit ->
            editors.getOrNull(index + offset + 1)?.setText(digit.toString())
        }
        val destination = (index + entered.length).coerceAtMost(editors.lastIndex)
        requestFocusAt(destination)
    }
}
