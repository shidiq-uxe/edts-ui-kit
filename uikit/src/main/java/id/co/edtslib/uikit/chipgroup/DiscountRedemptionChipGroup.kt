package id.co.edtslib.uikit.chipgroup

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.chip.DiscountRedemptionChip
import id.co.edtslib.uikit.chip.DiscountRedemptionChipDelegate
import id.co.edtslib.uikit.utils.MarginItem
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.linearMarginItemDecoration

class DiscountRedemptionChipGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(ContextThemeWrapper(context, R.style.Theme_EDTS_UIKit), attrs, defStyleAttr) {

    var delegate: DiscountRedemptionChipGroupDelegate? = null

    private val chipGroupAdapter = BucketChipGroupAdapter()
    var items: List<DiscountRedemptionCategory> = chipGroupAdapter.items
        set(value) {
            field = value
            chipGroupAdapter.updateItems(value)
        }

    private val spacingItemDecorator = linearMarginItemDecoration(
        orientation = LinearLayoutManager.HORIZONTAL,
        margin = MarginItem(left = 4.dp.toInt(), right = 4.dp.toInt())
    )

    private val snapHelper = LinearSnapHelper()
    private var isSnapping = false

    init {
        initView()
    }

    private fun initView() {
        clipToPadding = false
        layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        adapter = chipGroupAdapter
        spacingItemDecorator.takeIf { itemDecorationCount == 0 }?.let {
            addItemDecoration(it)
        }
    }

    fun setSelectedPosition(position: Int) {
        if (chipGroupAdapter.selectedPosition == position) return

        val previousSelected = chipGroupAdapter.selectedPosition
        chipGroupAdapter.selectedPosition = position
        notifySelectionChange(previousSelected, position)

        snapToPosition(position)
    }


    fun snapToPosition(position: Int) {
        if (isSnapping) return
        (layoutManager as? LinearLayoutManager)?.let { lm ->
            val targetView = lm.findViewByPosition(position) ?: run {
                smoothScrollToPosition(position)
                postDelayed({ snapToPosition(position) }, 150)
                return
            }
            snapHelper.calculateDistanceToFinalSnap(lm, targetView)?.let { distance ->
                if (distance[0] != 0 || distance[1] != 0) {
                    isSnapping = true
                    smoothScrollBy(distance[0], distance[1])
                    postDelayed({ isSnapping = false }, 300)
                }
            }
        }
    }

    private fun notifySelectionChange(previous: Int, current: Int) {
        if (previous != NO_POSITION) {
            chipGroupAdapter.notifyItemChanged(previous, "selection")
        }
        chipGroupAdapter.notifyItemChanged(current, "selection")
    }

    private inner class BucketChipGroupAdapter : Adapter<BucketChipGroupAdapter.ViewHolder>() {

        var items: List<DiscountRedemptionCategory> = emptyList()
            private set

        var selectedPosition: Int = 0

        fun updateItems(newItems: List<DiscountRedemptionCategory>) {
            items = newItems
            selectedPosition = 0
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(DiscountRedemptionChip(this@DiscountRedemptionChipGroup.context))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position], position == selectedPosition)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
            if (payloads.contains("selection")) {
                holder.toggleSelection(position == selectedPosition, items[position])
            } else {
                holder.bind(items[position], position == selectedPosition)
            }
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(private val chip: DiscountRedemptionChip) : RecyclerView.ViewHolder(chip) {
            fun bind(item: DiscountRedemptionCategory, isSelected: Boolean) {
                chip.apply {
                    text = item.categoryName
                    changeState(item.state)

                    toggleSelection(isSelected, item)

                    chip.delegate = object : DiscountRedemptionChipDelegate {
                        override fun onClick(view: View, state: Int) {
                            setSelectedPosition(adapterPosition)

                            this@DiscountRedemptionChipGroup.delegate?.onItemSelected(
                                adapterPosition, view, item
                            )
                        }
                    }
                }
            }

            fun toggleSelection(isSelected: Boolean, item: DiscountRedemptionCategory) {
                chip.isIndicatorVisible = isSelected
                if (isSelected && item.state == DiscountRedemptionChip.STATE_DEFAULT) {
                    chip.changeState(DiscountRedemptionChip.STATE_CHECKED)
                } else {
                    chip.changeState(item.state)
                }
            }

        }
    }
}