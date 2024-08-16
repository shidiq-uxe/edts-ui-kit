package id.co.edtslib.uikit.utils.transformer

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class ScalePageTransformer(
    private val minScale: Float = 0.9f,
    private val minAlpha: Float = 0.6f
) : ViewPager2.PageTransformer {

    override fun transformPage(page: View, position: Float) {
        if (position >= -1 || position <= 1) {
            // Modify the default slide transition to shrink the page as well
            val height = page.height
            val scaleFactor = minScale.coerceAtLeast(1 - abs(position))
            val verticalMargin = height * (1 - scaleFactor) / 2
            val horizontalMargin = page.width * (1 - scaleFactor) / 2

            // Center vertically
            page.pivotY = 0.5f * height

            if (position < 0) {
                page.translationX = horizontalMargin - verticalMargin / 2
            } else {
                page.translationX = -horizontalMargin + verticalMargin / 2
            }

            // Scale the page down (between MIN_SCALE and 1)
            page.scaleX = scaleFactor
            page.scaleY = scaleFactor

            // Fade the page relative to its size.
            page.alpha = minAlpha + (scaleFactor - minScale) / (1 - minScale) * (1 - minAlpha)
        }
    }
}
