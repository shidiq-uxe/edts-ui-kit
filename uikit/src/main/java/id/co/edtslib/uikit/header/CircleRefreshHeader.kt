package id.co.edtslib.uikit.header

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import id.co.edtslib.uikit.databinding.ViewRefreshHeaderBinding
import id.co.edtslib.uikit.utils.inflater
import id.co.edtslib.uikit.R as UIKitR

class CircleRefreshHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), IRefreshHeader {

    internal val binding = ViewRefreshHeaderBinding.inflate(context.inflater, this, true)

    private val imageView: ImageView = binding.refreshIndicator

    private var infiniteAnimator: ObjectAnimator? = null

    init {
        imageView.scaleX = 0f
        imageView.scaleY = 0f
    }

    /**
     * Called during the drag.
     * @param percent 0.0 to 1.0 (or more if over-scrolling)
     */
    override fun onPull(percent: Float) {
        val dragPercent = percent.coerceIn(0f, 1f)

        imageView.scaleX = dragPercent
        imageView.scaleY = dragPercent

        imageView.rotation = dragPercent * 360f
    }

    override fun onRefreshing() {
        imageView.visibility = View.VISIBLE

        if (infiniteAnimator == null) {
            infiniteAnimator = ObjectAnimator.ofFloat(imageView, View.ROTATION, imageView.rotation, imageView.rotation + 360f).apply {
                duration = 800
                interpolator = LinearInterpolator()
                repeatCount = ValueAnimator.INFINITE
            }
        }
        infiniteAnimator?.start()
    }

    override fun onFinish() {
        infiniteAnimator?.cancel()
        imageView.animate()
            .scaleX(0f)
            .scaleY(0f)
            .setDuration(200)
            .start()
    }
}