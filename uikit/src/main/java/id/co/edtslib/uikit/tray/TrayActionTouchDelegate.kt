package id.co.edtslib.uikit.tray

import android.graphics.Rect
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import id.co.edtslib.uikit.utils.dp

internal class TrayActionTouchDelegate(
    parent: ViewGroup,
    actions: List<View>
) : TouchDelegate(Rect(), parent) {

    private val targets = actions.filter { it.isVisible }.map { action ->
        val bounds = Rect()
        bounds.set(0, 0, action.width, action.height)
        parent.offsetDescendantRectToMyCoords(action, bounds)
        val horizontalInset = ((48.dp - bounds.width()) / 2f).coerceAtLeast(0f).toInt()
        val verticalInset = ((48.dp - bounds.height()) / 2f).coerceAtLeast(0f).toInt()
        bounds.inset(-horizontalInset, -verticalInset)
        bounds to TouchDelegate(bounds, action)
    }
    private var active: TouchDelegate? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            active = targets.firstOrNull { it.first.contains(event.x.toInt(), event.y.toInt()) }?.second
        }
        val isHandled = active?.onTouchEvent(event) ?: false
        if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) active = null
        return isHandled
    }
}
