package id.co.edtslib.uikit.carousel

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.res.use
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.CarouselStrategy
import com.google.android.material.carousel.MultiBrowseCarouselStrategy
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.adapter.BaseAdapter
import id.co.edtslib.uikit.utils.MarginItem
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.linearMarginItemDecoration

// Todo : Refactor the class To Use Material Carousel to Improve its performance | do during AGP Update
abstract class HorizontalCarouselRecyclerView<T : Any, B : ViewBinding>(
    context: Context,
    attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {

    abstract val carouselAdapter: BaseAdapter<T, B>

    var itemSpacing: Int = 0.dp.toInt()

    init {
        layoutManager = CarouselLayoutManager().apply {
            val strategy = MultiBrowseCarouselStrategy()

            setCarouselStrategy(strategy)
        }

        clipToPadding = false
        clipChildren = false

        adapter = carouselAdapter

        // Initialize from XML attributes if provided
        context.theme.obtainStyledAttributes(attrs, R.styleable.HorizontalCarouselRecyclerView, 0, 0).use {
            itemSpacing = it.getDimensionPixelSize(R.styleable.HorizontalCarouselRecyclerView_itemSpacing, 0)
            addItemDecoration(itemSpacingDecorator(itemSpacing))
        }
    }

    /**
     * Sets a new list of items for the carousel.
     * Automatically updates using DiffUtil.
     */
    var items: List<T> = carouselAdapter.items
        set(value) {
            field = value

            carouselAdapter.items = value
        }

    private fun itemSpacingDecorator(itemSpacing: Int) = linearMarginItemDecoration(
        orientation = LinearLayoutManager.HORIZONTAL,
        margin = MarginItem(
            left = itemSpacing.div(2),
            right = itemSpacing.div(2)
        )
    )
}
