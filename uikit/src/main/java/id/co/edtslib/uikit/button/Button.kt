package id.co.edtslib.uikit.button

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.Button
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.button.MaterialButton
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.attachShimmerEffect
import id.co.edtslib.uikit.utils.detachShimmerEffect
import id.co.edtslib.uikit.utils.resetScale

class Button @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.buttonStyle,
) : MaterialButton(context, attrs, defStyleAttr) {

    // Only call this variable only when shouldShowShimmer is true
    var shimmerFrameLayout: ShimmerFrameLayout? = null

    var shouldShowShimmer = false
        set(value) {
            field = value

            if (value) {
                shimmerFrameLayout = this.attachShimmerEffect()
            } else {
                shimmerFrameLayout?.detachShimmerEffect()
            }
        }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val scaleValue = 0.95f
        val resetValue = 1f

        event?.let { motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    this.animate().scaleX(scaleValue).scaleY(scaleValue).setDuration(100).start()
                }
                MotionEvent.ACTION_UP -> {
                    this.animate().scaleX(resetValue).scaleY(resetValue).setDuration(100).start()
                    performClick()
                }
                MotionEvent.ACTION_CANCEL -> {
                    this.animate().scaleX(resetValue).scaleY(resetValue).setDuration(100).start()
                }

                else -> {}
            }
        }

        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onDetachedFromWindow() {
        shimmerFrameLayout = null

        super.onDetachedFromWindow()
    }
}