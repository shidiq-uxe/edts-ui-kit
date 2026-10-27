package id.co.edtslib.uikit.header

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import id.co.edtslib.uikit.databinding.ViewRefreshHeaderBinding
import id.co.edtslib.uikit.utils.areSystemAnimationsEnabled
import id.co.edtslib.uikit.utils.inflater

/** Circular refresh artwork and motion shared with the coupon promotion prototype. */
class CircleRefreshHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), IRefreshHeader {

    private val binding = ViewRefreshHeaderBinding.inflate(this.context.inflater, this, true)
    private var rotationAnimator: ObjectAnimator? = null
    private var isRefreshing = false
    private val isMotionEnabled: Boolean
        get() = context.areSystemAnimationsEnabled()

    init {
        binding.refreshIndicator.scaleX = 0f
        binding.refreshIndicator.scaleY = 0f
    }

    override fun onPull(percent: Float) {
        if (isRefreshing) return
        binding.refreshIndicator.animate().cancel()
        val progress = percent.coerceIn(0f, 1f)
        binding.refreshIndicator.scaleX = progress
        binding.refreshIndicator.scaleY = progress
        binding.refreshIndicator.rotation = if (isMotionEnabled) progress * 360f else 0f
    }

    override fun onRefreshing() {
        isRefreshing = true
        binding.refreshIndicator.animate().cancel()
        binding.refreshIndicator.scaleX = 1f
        binding.refreshIndicator.scaleY = 1f
        rotationAnimator?.cancel()
        if (!isMotionEnabled || !isShown || windowVisibility != View.VISIBLE) return
        rotationAnimator = ObjectAnimator.ofFloat(
            binding.refreshIndicator,
            View.ROTATION,
            binding.refreshIndicator.rotation,
            binding.refreshIndicator.rotation + 360f,
        ).apply {
            duration = 800L
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            start()
        }
    }

    override fun onFinish() {
        isRefreshing = false
        rotationAnimator?.cancel()
        rotationAnimator = null
        binding.refreshIndicator.animate().cancel()
        binding.refreshIndicator.animate()
            .scaleX(0f)
            .scaleY(0f)
            .setDuration(if (isMotionEnabled) 200L else 0L)
            .start()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (!isShown) rotationAnimator?.cancel()
        else if (isRefreshing) onRefreshing()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility != View.VISIBLE) rotationAnimator?.cancel()
        else if (isRefreshing && isShown) onRefreshing()
    }

    override fun onDetachedFromWindow() {
        rotationAnimator?.cancel()
        rotationAnimator = null
        binding.refreshIndicator.animate().cancel()
        isRefreshing = false
        super.onDetachedFromWindow()
    }
}
