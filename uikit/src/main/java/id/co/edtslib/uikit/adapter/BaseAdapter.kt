package id.co.edtslib.uikit.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

class BaseAdapter<T : Any, B : ViewBinding>(
    private var register: Register<T, B>,
    private var diff: Diff<T>,
    private var params: Params = Params(),
    bindingClass: Class<B>,
) : RecyclerView.Adapter<BaseAdapter<T, B>.BaseViewHolder>() {

    private val bindingMethod = bindingClass.getMethod(
        INFLATE_METHOD,
        LayoutInflater::class.java,
        ViewGroup::class.java,
        Boolean::class.java
    )

    var items: List<T>
        get() = diffUtil.currentList
        set(value) = diffUtil.submitList(value)

    private val diffCallBack = object : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T) =
            diff.areItemsTheSame(oldItem, newItem)

        override fun areContentsTheSame(oldItem: T, newItem: T) =
            diff.areContentsTheSame(oldItem, newItem)
    }

    private val diffUtil = AsyncListDiffer(this, diffCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        @Suppress("UNCHECKED_CAST")
        val viewBinding = bindingMethod.invoke(null, inflater, parent, false) as B

        viewBinding.root.apply {
            val width = ((params.widthPercent / 100) * parent.measuredWidth).toInt()
            val height = ((params.heightPercent / 100) * parent.measuredHeight).toInt()

            val baseParams = RecyclerView.LayoutParams(
                if (params.widthPercent != 0.0) width else ViewGroup.LayoutParams.WRAP_CONTENT,
                if (params.heightPercent != 0.0) height else ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(
                    params.margin.left,
                    params.margin.top,
                    params.margin.right,
                    params.margin.bottom
                )
            }

            if (params.widthPercent != 0.0 || params.heightPercent != 0.0) {
                layoutParams = baseParams
            }
        }

        return BaseViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = register.itemCount(items)

    inner class BaseViewHolder(private val viewBinding: B) : RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(item: T) {
            register.onBindHolder(adapterPosition, item, viewBinding, diffUtil)

            viewBinding.root.setOnClickListener {
                onItemClickCallback.invoke(viewBinding, item, adapterPosition)
            }
        }
    }

    private var onItemClickCallback: (binding: B, item: T, position: Int) -> Unit = { _, _,_ -> }

    fun setOnItemClickListener(callback: (view: B, item: T, position: Int) -> Unit) {
        onItemClickCallback = callback
    }

    data class Register<T, B: ViewBinding>(
        var onBindHolder: (position: Int, item: T, binding: B, diffUtil: AsyncListDiffer<T>) -> Unit,
        var asyncLayout: Boolean = false,
        var itemCount: (items: List<T>) -> Int = { it.size }
    )

    data class Diff<T>(
        var areContentsTheSame: (old: T, new: T) -> Boolean,
        var areItemsTheSame: (old: T, new: T) -> Boolean
    )

    data class Params(
        var widthPercent: Double = 0.0,
        var heightPercent: Double = 0.0,
        var margin: Margin = Margin()
    ) {
        data class Margin(
            val top: Int = 0,
            val left: Int = 0,
            val right: Int = 0,
            val bottom: Int = 0
        )
    }
    companion object {
        const val DECLARED_ID_NAME = "id"

        inline fun <T : Any, reified B : ViewBinding> adapterOf(
            register: Register<T, B>,
            diff: Diff<T>,
            itemList: List<T> = emptyList(),
            params: Params = Params()
        ): BaseAdapter<T, B> {
            return BaseAdapter(register, diff, params, B::class.java).apply {
                if (itemList.isNotEmpty()) items = itemList
            }
        }

        inline fun <reified B: ViewBinding> shimmerAdapter(
            register: Register<Int, B> = Register({ _, _, _, _ -> }),
            size: Int,
            params: Params = Params(),
        ): BaseAdapter<Int, B> {
            val diff = Diff<Int>(
                areContentsTheSame = { new, old -> new == old },
                areItemsTheSame = { new, old -> new == old }
            )

            val list = List(size) { it }

            return BaseAdapter(register, diff, params, B::class.java).apply {
                items = list
            }
        }
    }
}

class BaseMultipleTypeAdapter<T : Any, MB : ViewBinding, LB: ViewBinding>(
    private var register: Register<T, MB>,
    private var diff: Diff<T>,
    private var params: Params = Params(),
    mainBindingClass: Class<MB>,
    loadingBindingClass: Class<LB>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val mainBindingMethod = mainBindingClass.getMethod(
        INFLATE_METHOD,
        LayoutInflater::class.java,
        ViewGroup::class.java,
        Boolean::class.java
    )

    private val loadingBindingMethod = loadingBindingClass.getMethod(
        INFLATE_METHOD,
        LayoutInflater::class.java,
        ViewGroup::class.java,
        Boolean::class.java
    )

    var items: List<T>
        get() = diffUtil.currentList
        set(value) = diffUtil.submitList(value)

    var viewType: BaseViewType = BaseViewType.LOADING
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val diffCallBack = object : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T) =
            diff.areItemsTheSame(oldItem, newItem)

        override fun areContentsTheSame(oldItem: T, newItem: T) =
            diff.areContentsTheSame(oldItem, newItem)
    }

    private val diffUtil = AsyncListDiffer(this, diffCallBack)

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, itemViewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when(itemViewType) {
            BaseViewType.LOADING.ordinal -> {
                val loadingViewBinding = loadingBindingMethod.invoke(null, inflater, parent, false) as LB
                BaseLoadingViewHolder(loadingViewBinding)
            }
            BaseViewType.INITIAL.ordinal -> {
                val initialViewBinding = mainBindingMethod.invoke(null, inflater, parent, false) as MB

                initialViewBinding.root.apply {
                    val width = ((params.widthPercent / 100) * parent.measuredWidth).toInt()
                    val height = ((params.heightPercent / 100) * parent.measuredHeight).toInt()

                    val baseParams = RecyclerView.LayoutParams(
                        if (params.widthPercent != 0.0) width else ViewGroup.LayoutParams.WRAP_CONTENT,
                        if (params.heightPercent != 0.0) height else ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(
                            params.margin.left,
                            params.margin.top,
                            params.margin.right,
                            params.margin.bottom
                        )
                    }

                    if (params.widthPercent != 0.0 || params.heightPercent != 0.0) {
                        layoutParams = baseParams
                    }
                }

                BaseMainViewHolder(initialViewBinding)
            }
            else -> throw IllegalStateException("Unknown RecyclerView ViewHolder For ViewType : $itemViewType")
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)) {
            BaseViewType.LOADING.ordinal -> {
                holder as BaseAdapter<*, *>.BaseViewHolder
            }
            BaseViewType.INITIAL.ordinal -> {
                holder as BaseMultipleTypeAdapter<*, *, *>.BaseMainViewHolder
            }
        }
    }

    override fun getItemCount() = if (viewType == BaseViewType.INITIAL) items.size else register.loadingSize

    override fun getItemViewType(position: Int) = viewType.ordinal

    inner class BaseMainViewHolder(private val viewBinding: MB) : RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(item: T) {
            register.onBindHolder(adapterPosition, item, viewBinding)

            viewBinding.root.setOnClickListener {
                onItemClickCallback.invoke(viewBinding, item, adapterPosition)
            }
        }
    }

    inner class BaseLoadingViewHolder(viewBinding: LB) : RecyclerView.ViewHolder(viewBinding.root) {
        init {
            viewBinding.root
        }
    }

    private var onItemClickCallback: (binding: MB, item: T, position: Int) -> Unit = { _, _,_ -> }

    fun setOnItemClickListener(callback: (view: MB, item: T, position: Int) -> Unit) {
        onItemClickCallback = callback
    }

    data class Register<T, B: ViewBinding>(
        var asyncLayout: Boolean = false,
        var loadingSize: Int = 10,
        var onBindHolder: (position: Int, item: T, binding: B) -> Unit
    )

    data class Diff<T>(
        var areContentsTheSame: (old: T, new: T) -> Boolean,
        var areItemsTheSame: (old: T, new: T) -> Boolean
    )

    data class Params(
        var widthPercent: Double = 0.0,
        var heightPercent: Double = 0.0,
        var margin: Margin = Margin()
    ) {
        data class Margin(
            val top: Int = 0,
            val left: Int = 0,
            val right: Int = 0,
            val bottom: Int = 0
        )
    }
}

const val INFLATE_METHOD = "inflate"

enum class BaseViewType {
    LOADING, INITIAL
}

inline fun<T : Any, reified MB: ViewBinding, reified LB: ViewBinding> adapterWithLoadingHolder(
    register: BaseMultipleTypeAdapter.Register<T, MB>,
    diff: BaseMultipleTypeAdapter.Diff<T>,
    itemList: List<T> = emptyList(),
    params: BaseMultipleTypeAdapter.Params = BaseMultipleTypeAdapter.Params()
) = BaseMultipleTypeAdapter(
    register = register,
    diff = diff,
    params = params,
    mainBindingClass = MB::class.java,
    loadingBindingClass = LB::class.java
).apply {
    if (itemList.isNotEmpty()) items = itemList
}