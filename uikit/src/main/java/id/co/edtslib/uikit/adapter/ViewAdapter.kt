package id.co.edtslib.uikit.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class ViewAdapter<V : View, T : Any>(
    private val viewFactory: (Context) -> V,
    private val configure: (position: Int, view: V, item: T) -> Unit,
    private var diff: BaseAdapter.Diff<T> = BaseAdapter.Diff(
        areItemsTheSame = { old, new -> old == new },
        areContentsTheSame = { old, new -> old == new }
    ),
) : RecyclerView.Adapter<ViewAdapter<V, T>.ViewHolder>() {

    var items: List<T>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    private val diffCallback = object : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T) =
            diff.areItemsTheSame(oldItem, newItem)

        override fun areContentsTheSame(oldItem: T, newItem: T) =
            diff.areContentsTheSame(oldItem, newItem)
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = viewFactory(parent.context)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(view: V) : RecyclerView.ViewHolder(view) {
        @Suppress("UNCHECKED_CAST")
        val itemView: V get() = super.itemView as V

        fun bind(item: T) {
            configure(adapterPosition, itemView, item)

            itemView.setOnClickListener {
                onItemClickCallback.invoke(itemView, item, adapterPosition)
            }
        }
    }

    private var onItemClickCallback: (view: V, item: T, position: Int) -> Unit = { _, _, _ -> }

    fun setOnItemClickListener(callback: (view: V, item: T, position: Int) -> Unit) {
        onItemClickCallback = callback
    }

    companion object {
        fun <V : View, T : Any> adapterOf(
            viewFactory: (Context) -> V,
            configure: (position: Int, view: V, item: T) -> Unit,
            diff: BaseAdapter.Diff<T> = BaseAdapter.Diff(
                areItemsTheSame = { old, new -> old == new },
                areContentsTheSame = { old, new -> old == new }
            ),
            itemList: List<T> = emptyList(),
        ): ViewAdapter<V, T> {
            return ViewAdapter(viewFactory, configure, diff).apply {
                if (itemList.isNotEmpty()) items = itemList
            }
        }
    }
}
