package id.co.edtslib.uikit.coordinator.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible

class HideOnScrollBehavior(context: Context, attrs: AttributeSet) :
    CoordinatorLayout.Behavior<View>(context, attrs) {

    private var isAnimateRunning = false

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout, child: View,
        directTargetChild: View, target: View, axes: Int, type: Int
    ): Boolean {
        // We only care about vertical scrolling
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout, child: View,
        target: View, dx: Int, dy: Int, consumed: IntArray, type: Int
    ) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)

        if (dy > 0 && !isAnimateRunning) {
            animateHide(child)
        } else if (dy < -10  && !isAnimateRunning) {
            animateShow(child)
        }
    }

    private fun animateHide(view: View) {
        view.animate()
            .translationY(-view.height.toFloat())
            .alpha(0f)
            .setDuration(200L)
            .withStartAction { isAnimateRunning = true }
            .withEndAction {
                // view.visibility = View.GONE
                isAnimateRunning = false
            }
            .start()
    }

    private fun animateShow(view: View) {
        view.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(200L)
            .withStartAction {
                // view.visibility = View.VISIBLE
                isAnimateRunning = true
            }
            .withEndAction { isAnimateRunning = false }
            .start()
    }
}