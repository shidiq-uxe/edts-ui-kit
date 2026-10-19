package id.co.edtslib.uikit.tray

import android.content.Context
import android.util.AttributeSet
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.google.android.material.checkbox.MaterialCheckBox

/** Keeps the legacy checkbox binding contract without exposing a toggle action. */
class TrayActionButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.checkboxStyle
) : MaterialCheckBox(context, attrs, defStyleAttr) {

    override fun toggle() = Unit

    override fun getAccessibilityClassName(): CharSequence = android.widget.Button::class.java.name

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo?) {
        super.onInitializeAccessibilityNodeInfo(info)
        info?.className = accessibilityClassName
        info?.isCheckable = false
        info?.isChecked = false
    }

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        event.className = accessibilityClassName
        event.isChecked = false
    }
}
