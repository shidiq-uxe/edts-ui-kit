package id.co.edtslib.uikit.core.chipgroup

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.co.edtslib.uikit.core.chip.ChipDelegate
import id.co.edtslib.uikit.core.chip.CoreChip

class ChipGroupAdapter<T : Any>(
    private val createChip: (parent: ViewGroup) -> CoreChip,
    private val bindChip: (chip: CoreChip, item: T, position: Int) -> Unit,
    private val areItemsTheSame: (old: T, new: T) -> Boolean = { old, new -> old == new },
    private val areContentsTheSame: (old: T, new: T) -> Boolean = { old, new -> old == new },
    private val isSelected: (item: T) -> Boolean = { false },
    var delegate: ChipGroupDelegate<T>? = null,
) : RecyclerView.Adapter<ChipGroupAdapter.ChipViewHolder>() {

    private var items: List<T> = emptyList()

    fun submitList(newItems: List<T>) {
        val oldItems = items
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = oldItems.size
            override fun getNewListSize() = newItems.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                areItemsTheSame(oldItems[oldItemPosition], newItems[newItemPosition])
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                areContentsTheSame(oldItems[oldItemPosition], newItems[newItemPosition])
        })
        items = newItems
        diff.dispatchUpdatesTo(this)
    }

    fun currentList(): List<T> = items

    fun selectedItems(): List<T> = items.filter(isSelected)

    class ChipViewHolder(val chip: CoreChip) : RecyclerView.ViewHolder(chip)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder =
        ChipViewHolder(createChip(parent))

    override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
        val item = items[position]
        bindChip(holder.chip, item, position)
        wireDelegate(holder, item)
    }

    override fun getItemCount(): Int = items.size

    private fun wireDelegate(holder: ChipViewHolder, item: T) {
        val listDelegate = delegate ?: return
        val chip = holder.chip
        chip.delegate = object : ChipDelegate {
            override fun onClick(view: View, state: Int) = listDelegate.onChipClick(chip, item, holder.adapterPosition)
            override fun onStartIconClick(view: View, state: Int) = listDelegate.onChipStartIconClick(chip, item, holder.adapterPosition)
            override fun onEndIconClick(view: View, state: Int) = listDelegate.onChipEndIconClick(chip, item, holder.adapterPosition)
        }
    }
}