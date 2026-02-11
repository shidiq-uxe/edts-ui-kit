package id.co.edtslib.uikit.coordinator.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout

// Todo :
class AnchorOffsetBehavior(
    context: Context,
    attrs: AttributeSet? = null
) : CoordinatorLayout.Behavior<View>(context, attrs) {

    private val marginPx: Int = 0

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        val lp = child.layoutParams as? CoordinatorLayout.LayoutParams ?: return false
        return lp.anchorId == dependency.id
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        parent.onLayoutChild(child, layoutDirection)

        val lp = child.layoutParams as? CoordinatorLayout.LayoutParams
        val anchorId = lp?.anchorId ?: View.NO_ID
        if (anchorId != View.NO_ID) {
            val anchor = parent.findViewById<View>(anchorId)
            if (anchor != null) {
                val targetTop = anchor.bottom + marginPx
                child.y = targetTop.toFloat()
                return true
            }
        }
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        val targetTop = dependency.bottom + marginPx
        child.y = targetTop.toFloat()
        return true
    }
}
