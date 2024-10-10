package id.co.edtslib.uikit.boarding.adapter

import android.graphics.drawable.Drawable
import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import id.co.edtslib.uikit.adapter.BaseAdapter
import id.co.edtslib.uikit.boarding.Boarding
import id.co.edtslib.uikit.boarding.BoardingItemAlignment
import id.co.edtslib.uikit.databinding.ItemBoardingContentBinding
import id.co.edtslib.uikit.utils.horizontalBias
import id.co.edtslib.uikit.utils.resetScale

object BoardingAdapter {

    var circular = false
    var contentAlignment: BoardingItemAlignment = BoardingItemAlignment()

    fun boardingAdapter(): BaseAdapter<Boarding, ItemBoardingContentBinding> = BaseAdapter.adapterOf(
        register = BaseAdapter.Register(
            onBindHolder = { position, item, binding, diffUtil ->
                val items = diffUtil.currentList

                val newPosition = if (circular && items.size > 1) {
                    position % items.size
                } else {
                    position
                }

                binding.bindViewWithData(items[newPosition], newPosition)
                binding.adjustIndicatorAlignment()

                if (circular && items.size > 1) {
                    // Todo : Overrides Click Impl
                }
            },
            itemCount = { items ->
                if (circular && items.size > 1) Int.MAX_VALUE else items.size
            }
        ),
        diff = BaseAdapter.Diff(
            areContentsTheSame = { old, new -> old == new },
            areItemsTheSame = { old, new -> old == new }
        )
    )

    private val boardingAdapter get() = boardingAdapter()

    fun getRealPosition(position: Int): Int {
        return if (circular && boardingAdapter.items.size > 1) {
            position % boardingAdapter.items.size
        } else {
            position
        }
    }

    fun getFakePosition(position: Int): Int {
        return if (circular && boardingAdapter.items.size > 1) {
            (boardingAdapter.itemCount / 2 - (boardingAdapter.itemCount /2) % boardingAdapter.items.size)+position
        } else {
            position
        }
    }

    fun getInitialPosition(canBackOnFirstPosition: Boolean): Int {
        return if (circular && boardingAdapter.items.size > 1) {
            if (canBackOnFirstPosition) {
                boardingAdapter.itemCount / 2 - (boardingAdapter.itemCount /2) % boardingAdapter.items.size
            } else {
                0
            }
        } else {
            0
        }
    }

    private fun ItemBoardingContentBinding.bindViewWithData(item: Boarding, position: Int) {
        val context =
            if (root.context is FragmentActivity) root.context as FragmentActivity else root.context

        // Safeguard against destroyed activity or invalid context
        val activity = (context as? FragmentActivity)

        if (activity != null && (activity.isDestroyed || activity.isFinishing)) {
            // If the activity is destroyed, return early to avoid Glide loading issues
            return
        }

        val image  = item.image?.let { image ->
            when(image) {
                is Int -> context.getDrawable(image)
                is String -> if (image.startsWith("http")) {
                    image
                } else {
                    context.resources.getIdentifier(
                        image, "drawable",
                        context.packageName
                    )
                }
                else -> image
            }
        }

        Glide.with(context).load(image).into(ivBoardingIll)

        tvBoardingTitle.text = item.title
        tvBoardingDescription.text = item.description
    }

    private fun ItemBoardingContentBinding.adjustIndicatorAlignment() {
        val verticalAlignment = contentAlignment.verticalAlignmentPercent
        val horizontalAlignment = contentAlignment.horizontalAlignmentPercent

        // Re-explanation : 0f Means 0% from the start of the screen while 1f means 100% from start of the screen
        if (verticalAlignment > 0) {
            // You don't have to set other views since all views are chained with each other
            val ivParam = ivBoardingIll.layoutParams as ConstraintLayout.LayoutParams
            ivParam.verticalBias = verticalAlignment
        }

        // Since each views not chained horizontally, we have to set it manually to each view
        if (horizontalAlignment > 0) {
            listOf(
                tvBoardingTitle,
                tvBoardingDescription,
                ivBoardingIll
            ).map { it.horizontalBias(horizontalAlignment) }
        }
    }
}