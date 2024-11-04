package id.co.edtslib.uikit.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseMultiTypeAdapter<T>(
	diffCallback: DiffUtil.ItemCallback<T>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	private val differ = AsyncListDiffer(this, diffCallback)

	var items: List<T>
		get() = differ.currentList
		set(value) = differ.submitList(value)

	private val viewTypeBindings = mutableMapOf<Int, (LayoutInflater, ViewGroup, Boolean) -> ViewBinding>()
	private val viewTypeBindingsMap = mutableMapOf<Int, (Int, ViewBinding, T) -> Unit>()

	fun registerViewType(
		viewType: Int,
		bindingInflater: (LayoutInflater, ViewGroup, Boolean) -> ViewBinding,
		bind: (Int, ViewBinding, T) -> Unit
	) {
		viewTypeBindings[viewType] = bindingInflater
		viewTypeBindingsMap[viewType] = bind
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		val binding = viewTypeBindings[viewType]!!.invoke(LayoutInflater.from(parent.context), parent, false)
		return BaseViewHolder(binding)
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		val item = items[position]
		val viewType = getItemViewType(position)
		viewTypeBindingsMap[viewType]!!.invoke(position, (holder as BaseMultiTypeAdapter<*>.BaseViewHolder).binding, item)
	}

	override fun getItemCount(): Int = items.size

	inner class BaseViewHolder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root)

	abstract override fun getItemViewType(position: Int): Int
}

fun <T> multiTypeAdapter(
	diffCallback: DiffUtil.ItemCallback<T>,
	viewTypeConfig: (item: T)  -> Int,
	bindingConfig: BaseMultiTypeAdapter<T>.() -> Unit,
	onViewDetachedFromWindow: (holder: RecyclerView.ViewHolder) -> Unit = {}
): BaseMultiTypeAdapter<T> {
	val baseAdapter = object : BaseMultiTypeAdapter<T>(diffCallback) {
		override fun getItemViewType(position: Int): Int {
			return viewTypeConfig.invoke(items[position])
		}

		override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
			onViewDetachedFromWindow.invoke(holder)
			super.onViewDetachedFromWindow(holder)
		}

	}
	baseAdapter.bindingConfig()
	return baseAdapter
}