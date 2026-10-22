package id.co.edtslib.edtsuikit

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.co.edtslib.uikit.core.listitem.ActionKey
import id.co.edtslib.uikit.core.listitem.ListItem
import id.co.edtslib.uikit.core.listitem.ListItemEvent
import id.co.edtslib.uikit.core.listitem.ListItemListener
import id.co.edtslib.uikit.core.listitem.StandardGestures
import id.co.edtslib.edtsuikit.databinding.ItemCartProductBinding
import java.util.Locale

data class CartCardState(
    val selected: Boolean = false,
    val quantity: Int = 1,
    val loading: Boolean = false
)

typealias CartCardController = ListItem<Product, CartCardState>

data class CartRow(val product: Product, val state: CartCardState = CartCardState())

fun CartCardController.toggleSelected(): Boolean = update { it.copy(selected = !it.selected) }

object CartActions {
    val Delete = ActionKey<Unit>("delete")
}

fun List<CartRow>.withSingleSelection(id: String, selected: Boolean): List<CartRow> = map { row ->
    val newSelected = when {
        row.product.id == id -> selected
        selected -> false
        else -> row.state.selected
    }
    if (newSelected == row.state.selected) row else row.copy(state = row.state.copy(selected = newSelected))
}

interface CartCardCallbacks {
    fun onOpen(product: Product) {}
    fun onLongPress(product: Product) {}
    fun onImageClick(product: Product) {}
    fun onImageLongClick(product: Product) {}
    fun onDiscountClick(product: Product) {}
    fun onPriceClick(product: Product) {}
    fun onDelete(product: Product) {}
    fun onSelectionChanged(product: Product, selected: Boolean) {}
    fun onQuantityChange(product: Product, change: QuantityChange, quantity: Int) {}
    fun onQuantityLimitReached(product: Product, max: Int) {}
    fun onOtherAction(custom: ListItemEvent.Custom<Product>) {}
}

fun CartCardCallbacks.toListener() = ListItemListener<Product, CartCardState> { e ->
    when (e) {
        is ListItemEvent.Click -> onOpen(e.item)
        is ListItemEvent.LongClick -> onLongPress(e.item)

        is ListItemEvent.Custom -> when (e.key) {
            ProductActions.Image -> onImageClick(e.item)
            ProductActions.ImageLong -> onImageLongClick(e.item)
            ProductActions.Discount -> onDiscountClick(e.item)
            ProductActions.Price -> onPriceClick(e.item)
            CartActions.Delete -> onDelete(e.item)
            ProductActions.QuantityLimit ->
                onQuantityLimitReached(e.item, e.payloadOf(ProductActions.QuantityLimit))
            else -> onOtherAction(e)
        }

        is ListItemEvent.StateChanged -> {
            quantityChange(e.oldState.quantity, e.newState.quantity)?.let { onQuantityChange(e.item, it, e.newState.quantity) }
            if (e.oldState.selected != e.newState.selected) onSelectionChanged(e.item, e.newState.selected)
        }
    }
}

class CartCardHolder(
    private val binding: ItemCartProductBinding,
    private val loadImage: (ImageView, String?) -> Unit,
    listener: ListItemListener<Product, CartCardState>,
    configureCard: (CartCardController, ItemCartProductBinding) -> Unit = { _, _ -> }
) : RecyclerView.ViewHolder(binding.root) {


    val card = ListItem<Product, CartCardState>(
        root = binding.root,
        defaultState = CartCardState()
    )
        .enableCardClicks()
        .bindAction(binding.ivProductImage, ProductActions.Image)
        .bindAction(
            binding.ivProductImage,
            ProductActions.ImageLong,
            StandardGestures.longClick
        )
        .bindAction(binding.discountBadge, ProductActions.Discount)
        .bindAction(binding.tvProductPrice, ProductActions.Price)
        .bindAction(binding.tvProductPriceCutOff, ProductActions.Price)
        .bindAction(binding.btnDelete, CartActions.Delete)
        .setRenderer { root, item, state ->
            render(item, state)
        }
        .addListener(listener)

    init {
        binding.cbSelection.setOnClickListener {
            if (!card.toggleSelected()) card.refresh()
        }

        binding.btnDelete.setOnCheckedChangeListener { button, checked ->
            if (checked) button.isChecked = false
        }

        binding.sbQuantity.setMinValue(1)
        binding.sbQuantity.setOnCountChangeListener { newQty -> onStepperChanged(newQty) }

        configureCard(card, binding)
    }

    private fun onStepperChanged(newQty: Int) {
        val item = card.boundItem ?: return

        if (newQty > item.maxQuantity) {
            card.triggerAction(ProductActions.QuantityLimit, item.maxQuantity, binding.sbQuantity)
            card.refresh()
            return
        }
        if (!card.update { it.copy(quantity = newQty) }) card.refresh()
    }


    private fun render(item: Product, state: CartCardState) {
        with(binding) {
            root.isSelected = state.selected
            root.isEnabled = !state.loading

            tvProductName.text = item.name
            tvProductPrice.text = rupiah(item.price)
            loadImage(ivProductImage, item.imageUrl)

            val original = item.originalPrice
            val percent = item.discountPercent
            val hasDiscount = original != null && percent != null

            discountBadge.isVisible = hasDiscount
            tvProductPriceCutOff.isVisible = hasDiscount

            if (hasDiscount) {
                discountBadge.text = "$percent%"
                tvProductPriceCutOff.text = rupiah(original!!)
            }

            cbSelection.isChecked = state.selected
            cbSelection.isEnabled = !state.loading

            btnDelete.isEnabled = !state.loading

            sbQuantity.isEnabled = !state.loading && item.maxQuantity > 0
            sbQuantity.setCount(state.quantity)
        }
    }

    private fun rupiah(value: Long): String =
        "Rp" + String.format(Locale.forLanguageTag("id-ID"), "%,d", value)
}

class CartAdapter(
    private val loadImage: (ImageView, String?) -> Unit,
    callbacks: CartCardCallbacks,
    private val configureCard: (CartCardController, ItemCartProductBinding) -> Unit = { _, _ -> }
) : ListAdapter<CartRow, CartCardHolder>(Diff) {

    private val listener = callbacks.toListener()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CartCardHolder(
            ItemCartProductBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            loadImage,
            listener,
            configureCard
        )

    override fun onBindViewHolder(holder: CartCardHolder, position: Int) {
        val row = getItem(position)
        holder.card.bind(row.product, row.state)
    }

    override fun onViewRecycled(holder: CartCardHolder) {
        holder.card.unbind()
    }

    object Diff : DiffUtil.ItemCallback<CartRow>() {
        override fun areItemsTheSame(a: CartRow, b: CartRow) = a.product.id == b.product.id
        override fun areContentsTheSame(a: CartRow, b: CartRow) = a == b
    }
}