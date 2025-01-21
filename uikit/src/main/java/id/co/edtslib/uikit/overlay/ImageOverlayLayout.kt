package id.co.edtslib.uikit.overlay

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.carousel.HorizontalCarouselRecyclerView

class ImageOverlayLayout(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var overlayImageView: AppCompatImageView? = null
    private var imageUrl: String? = null
    private var drawableWidth: Float = 0f

    init {
        // Read attributes from XML
        context.theme.obtainStyledAttributes(attrs, R.styleable.ImageOverlayLayout, 0, 0).apply {
            drawableWidth = getDimension(R.styleable.ImageOverlayLayout_drawableWidth, 0f)
            recycle()
        }
    }

    /**
     * Binds an image URL to the overlay view.
     * A custom image loader can be provided for flexibility.
     */
    fun bindImage(
        imageUrl: String?,
        imageLoader: (ImageView, String?) -> Unit = { imageView, url ->
            Glide.with(imageView.context).load(url).into(imageView)
        }
    ) {
        this.imageUrl = imageUrl

        if (overlayImageView == null) {
            overlayImageView = AppCompatImageView(context).apply {
                scaleType = ImageView.ScaleType.FIT_XY
                layoutParams = LayoutParams(drawableWidth.toInt(), LayoutParams.MATCH_PARENT)
                addView(this, 0)
            }
        }

        imageLoader(overlayImageView!!, imageUrl)
    }

    fun attachToCarousel(carouselView: HorizontalCarouselRecyclerView<*, *>) {
        carouselView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                // Example of a fade-out effect during scroll
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val view = layoutManager.findViewByPosition(firstVisibleItemPosition)
                val alpha = view?.left?.toFloat()?.div(recyclerView.width) ?: 1f
                overlayImageView?.alpha = alpha.coerceAtLeast(0f)
            }
        })
    }
}
