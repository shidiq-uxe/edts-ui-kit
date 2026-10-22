package id.co.edtslib.edtsuikit

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.co.edtslib.edtsuikit.databinding.ItemGridProductBinding
import com.google.android.material.chip.Chip
import id.co.edtslib.uikit.core.listitem.ActionKey
import id.co.edtslib.uikit.core.listitem.ListItem
import id.co.edtslib.uikit.core.listitem.ListItemEvent
import id.co.edtslib.uikit.core.listitem.ListItemListener
import id.co.edtslib.uikit.core.listitem.StandardGestures
import java.util.Locale

data class Product(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val price: Long,
    val originalPrice: Long? = null,
    val discountPercent: Int? = null,
    val hasFreeGift: Boolean = false,
    val extraPromos: List<String> = emptyList(),
    val stock: Int = Int.MAX_VALUE,
    val maxPerOrder: Int = Int.MAX_VALUE
) {
    val maxQuantity: Int get() = minOf(stock, maxPerOrder)
}

data class ProductCardState(
    val quantity: Int = 0,
    val loading: Boolean = false
)

typealias ProductCardController = ListItem<Product, ProductCardState>

data class ProductRow(
    val product: Product,
    val state: ProductCardState = ProductCardState()
)

object ProductActions {
    val Image = ActionKey<Unit>("image")
    val ImageLong = ActionKey<Unit>("image_long")
    val PromoBadge = ActionKey<Unit>("promo_badge")
    val Discount = ActionKey<Unit>("discount")
    val Price = ActionKey<Unit>("price")
    val ExtraPromo1 = ActionKey<Unit>("extra_promo_1")
    val ExtraPromo2 = ActionKey<Unit>("extra_promo_2")
    val QuantityLimit = ActionKey<Int>("quantity_limit")
}

enum class QuantityChange {
    ADDED,
    INCREASED,
    DECREASED,
    REMOVED
}

fun quantityChange(o: Int, n: Int): QuantityChange? {
    return when {
        o == n -> null
        o == 0 -> QuantityChange.ADDED
        n == 0 -> QuantityChange.REMOVED
        n > o -> QuantityChange.INCREASED
        else -> QuantityChange.DECREASED
    }
}

interface ProductCardCallbacks {
    fun onOpen(product: Product) {}
    fun onLongPress(product: Product) {}
    fun onImageClick(product: Product) {}
    fun onImageLongClick(product: Product) {}
    fun onPromoBadgeClick(product: Product) {}
    fun onDiscountClick(product: Product) {}
    fun onPriceClick(product: Product) {}
    fun onExtraPromoClick(product: Product, index: Int) {}
    fun onQuantityChange(product: Product, change: QuantityChange, quantity: Int) {}
    fun onQuantityLimitReached(product: Product, max: Int) {}
    fun onOtherAction(custom: ListItemEvent.Custom<Product>) {}
}

fun ProductCardCallbacks.toListener() = ListItemListener<Product, ProductCardState> { e ->
    when (e) {
        is ListItemEvent.Click -> onOpen(e.item)
        is ListItemEvent.LongClick -> onLongPress(e.item)

        is ListItemEvent.Custom -> when (e.key) {
            ProductActions.Image -> onImageClick(e.item)
            ProductActions.ImageLong -> onImageLongClick(e.item)
            ProductActions.PromoBadge -> onPromoBadgeClick(e.item)
            ProductActions.Discount -> onDiscountClick(e.item)
            ProductActions.Price -> onPriceClick(e.item)
            ProductActions.ExtraPromo1 -> onExtraPromoClick(e.item, 0)
            ProductActions.ExtraPromo2 -> onExtraPromoClick(e.item, 1)
            ProductActions.QuantityLimit ->
                onQuantityLimitReached(e.item, e.payloadOf(ProductActions.QuantityLimit))

            else -> onOtherAction(e)
        }

        is ListItemEvent.StateChanged -> {
            quantityChange(e.oldState.quantity, e.newState.quantity)?.let {
                onQuantityChange(
                    e.item,
                    it,
                    e.newState.quantity
                )
            }
        }
    }
}

class ProductCardHolder(
    private val binding: ItemGridProductBinding,
    private val loadImage: (ImageView, String?) -> Unit,
    listener: ListItemListener<Product, ProductCardState>,
    configureCard: (ProductCardController, ItemGridProductBinding) -> Unit = { _, _ -> }
) : RecyclerView.ViewHolder(binding.root) {


    val card = ListItem<Product, ProductCardState>(
        root = binding.root,
        defaultState = ProductCardState()
    )
        .enableCardClicks()
        .bindAction(binding.ivCouponImage, ProductActions.Image)
        .bindAction(
            binding.ivCouponImage,
            ProductActions.ImageLong,
            StandardGestures.longClick
        )
        .bindAction(binding.badgePromotion, ProductActions.PromoBadge)
        .bindAction(binding.discountBadge, ProductActions.Discount)
        .bindAction(binding.tvProductPrice, ProductActions.Price)
        .bindAction(binding.tvProductPriceCutOff, ProductActions.Price)
        .bindAction(binding.extraPromotionBadge, ProductActions.ExtraPromo1)
        .bindAction(binding.extraPromotionBadge2, ProductActions.ExtraPromo2)
        .setRenderer { root, item, state ->
            render(item, state)
        }
        .addListener(listener)

    init {
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

    private fun render(item: Product, state: ProductCardState) {
        with(binding) {
            root.isEnabled = !state.loading

            tvProductName.text = item.name
            tvProductPrice.text = rupiah(item.price)
            loadImage(ivCouponImage, item.imageUrl)

            badgePromotion.isVisible = item.hasFreeGift

            val original = item.originalPrice
            val percent = item.discountPercent
            val hasDiscount = original != null && percent != null

            discountBadge.isVisible = hasDiscount
            tvProductPriceCutOff.isVisible = hasDiscount

            if (original != null && percent != null) {
                discountBadge.text = "$percent%"
                tvProductPriceCutOff.text = rupiah(original)
            }

            bindPromo(extraPromotionBadge, item.extraPromos.getOrNull(0))
            bindPromo(extraPromotionBadge2, item.extraPromos.getOrNull(1))

            val canBuy = item.maxQuantity > 0
            sbQuantity.isEnabled = canBuy && !state.loading
            sbQuantity.setCount(state.quantity)
        }
    }

    private fun bindPromo(chip: Chip, text: String?) {
        chip.isVisible = text != null
        if (text != null) chip.text = text
    }

    private fun rupiah(value: Long): String =
        "Rp" + String.format(Locale.forLanguageTag("id-ID"), "%,d", value)
}

class ProductAdapter(
    private val loadImage: (ImageView, String?) -> Unit,
    callbacks: ProductCardCallbacks,
    private val configureCard: (ProductCardController, ItemGridProductBinding) -> Unit = { _, _ -> }
) : ListAdapter<ProductRow, ProductCardHolder>(Diff) {

    private val listener = callbacks.toListener()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ProductCardHolder(
            ItemGridProductBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            loadImage,
            listener,
            configureCard
        )

    override fun onBindViewHolder(holder: ProductCardHolder, position: Int) {
        val row = getItem(position)
        holder.card.bind(row.product, row.state)
    }

    override fun onViewRecycled(holder: ProductCardHolder) {
        holder.card.unbind()
    }

    object Diff : DiffUtil.ItemCallback<ProductRow>() {
        override fun areItemsTheSame(a: ProductRow, b: ProductRow) = a.product.id == b.product.id
        override fun areContentsTheSame(a: ProductRow, b: ProductRow) = a == b
    }
}