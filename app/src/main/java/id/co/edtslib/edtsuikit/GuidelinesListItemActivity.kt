package id.co.edtslib.edtsuikit

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesListItemBinding
import id.co.edtslib.uikit.core.listitem.ActionKey
import id.co.edtslib.uikit.core.listitem.StandardGestures
import java.util.Locale

class GuidelinesListItemActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityGuidelinesListItemBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guidelines_list_item)

        setupProductGrid()
        setupCartList()
        renderAll()
    }

    private val catalog = listOf(
        Product(
            id = "1", name = "Aqua Air Mineral 600ml", imageUrl = null,
            price = 2_000, originalPrice = 4_000, discountPercent = 50,
            hasFreeGift = true, extraPromos = listOf("Banyak Lebih Hemat", "Paket Bundling")
        ),
        Product(
            id = "2", name = "Indomie Goreng Special 85g", imageUrl = null,
            price = 3_500, originalPrice = 5_000, discountPercent = 30
        ),
        Product(
            id = "3", name = "Susu UHT Full Cream 1L", imageUrl = null, price = 18_500,
            originalPrice = 21_000, discountPercent = 12, extraPromos = listOf("Banyak Lebih Hemat"),
            maxPerOrder = 3
        ),
        Product(
            id = "4", name = "Frozen Nugget Ayam 500g", imageUrl = null,
            price = 42_000, originalPrice = 60_000, discountPercent = 30
        ),
        Product(
            id = "5", name = "Sabun Mandi Cair 450ml", imageUrl = null,
            price = 24_900, originalPrice = 33_200, discountPercent = 25, hasFreeGift = true
        ),
        Product(
            id = "6", name = "Roti Tawar Gandum", imageUrl = null,
            price = 16_000, originalPrice = 20_000, discountPercent = 20
        ),
    )

    private val catalogById = catalog.associateBy { it.id }
    private val loadingId = "6"
    private var quantities: Map<String, Int> = linkedMapOf("1" to 2, "3" to 3, "5" to 1)
    private var selectedIds: Set<String> = setOf("1")

    private fun setQuantity(id: String, quantity: Int) {
        if (quantity <= 0) {
            quantities = quantities - id
            selectedIds = selectedIds - id
        } else {
            quantities = quantities + (id to quantity)
        }
        renderAll()
    }

    private fun setSelected(id: String, selected: Boolean) {
        selectedIds = if (selected) selectedIds + id else selectedIds - id
        renderAll()
    }

    private fun productRows() = catalog.map {
        ProductRow(it, ProductCardState(quantity = quantities[it.id] ?: 0, loading = it.id == loadingId))
    }

    private fun cartRows() = quantities.mapNotNull { (id, qty) ->
        catalogById[id]?.let { CartRow(it, CartCardState(selected = id in selectedIds, quantity = qty)) }
    }

    private fun renderAll() {
        productAdapter.submitList(productRows())
        cartAdapter.submitList(cartRows())
        renderCartSection()
    }

    private val productCallbacks: ProductCardCallbacks = object : ProductCardCallbacks {
        override fun onOpen(product: Product) = toast("Open: ${product.name}")
        override fun onLongPress(product: Product) = toast("Card long press: ${product.name}")
        override fun onImageClick(product: Product) = toast("Image click: ${product.name}")
        override fun onImageLongClick(product: Product) = toast("Image long click: ${product.name}")
        override fun onPromoBadgeClick(product: Product) = toast("Gift badge: ${product.name}")
        override fun onDiscountClick(product: Product) = toast("Discount: ${product.discountPercent}%")
        override fun onPriceClick(product: Product) = toast("Price: ${product.price}")

        override fun onExtraPromoClick(product: Product, index: Int) =
            toast("Promo #${index + 1}: ${product.extraPromos.getOrNull(index)}")

        override fun onQuantityChange(product: Product, change: QuantityChange, quantity: Int) {
            setQuantity(product.id, quantity)
            toast("Grid $change -> $quantity (${product.name})")
        }

        override fun onQuantityLimitReached(product: Product, max: Int) =
            toast("Max $max per order: ${product.name}")
    }

    private object DemoActions {
        val NameLong = ActionKey<Unit>("name_long")
        val Rate = ActionKey<Int>("rate")
    }

    private val productAdapter: ProductAdapter by lazy {
        ProductAdapter(
            loadImage = { view, _ -> view.setBackgroundColor(Color.LTGRAY) },
            callbacks = productCallbacks,
            configureCard = { card, b ->
                card
                    .bindAction(b.tvProductName, DemoActions.NameLong, StandardGestures.longClick)
                    .onAction(DemoActions.NameLong) { product, _, _ ->
                        toast("Custom key name_long: ${product.name}")
                    }
                    .bindAction(b.tvProductPriceCutOff, DemoActions.Rate, StandardGestures.longClick) { 5 }
                    .onAction(DemoActions.Rate) { product, stars, _ ->
                        toast("Custom key rate: $stars stars for ${product.name}")
                    }
            }
        )
    }

    private fun setupProductGrid() {
        binding.rvProducts.layoutManager = GridLayoutManager(this, 2)
        (binding.rvProducts.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        binding.rvProducts.adapter = productAdapter
    }

    private val cartCallbacks: CartCardCallbacks = object : CartCardCallbacks {
        override fun onOpen(product: Product) = toast("Cart open: ${product.name}")
        override fun onLongPress(product: Product) = toast("Cart row long press: ${product.name}")
        override fun onImageClick(product: Product) = toast("Cart image: ${product.name}")
        override fun onImageLongClick(product: Product) = toast("Cart image long press: ${product.name}")
        override fun onDiscountClick(product: Product) = toast("Cart discount: ${product.discountPercent}%")
        override fun onPriceClick(product: Product) = toast("Cart price: ${product.price}")

        override fun onDelete(product: Product) {
            setQuantity(product.id, 0)
            toast("Deleted: ${product.name}")
        }

        override fun onSelectionChanged(product: Product, selected: Boolean) =
            setSelected(product.id, selected)

        override fun onQuantityChange(product: Product, change: QuantityChange, quantity: Int) {
            setQuantity(product.id, quantity)
            toast("Cart $change -> $quantity (${product.name})")
        }

        override fun onQuantityLimitReached(product: Product, max: Int) =
            toast("Max $max per order: ${product.name}")
    }

    private val cartAdapter: CartAdapter by lazy {
        CartAdapter(
            loadImage = { view, _ -> view.setBackgroundColor(Color.LTGRAY) },
            callbacks = cartCallbacks
        )
    }

    private fun setupCartList() {
        val section = binding.cartSection

        hideShippingHeader()
        installCartRows()
        section.tvSubtotalTitle.text = "Subtotal"

        section.cbDeleteAction.setOnClickListener {
            selectedIds = if (section.cbDeleteAction.isChecked) quantities.keys.toSet() else emptySet()
            renderAll()
        }

        section.tvDeleteAll.setOnClickListener {
            val count = selectedIds.size
            if (count == 0) {
                toast("Nothing selected")
            } else {
                quantities = quantities - selectedIds
                selectedIds = emptySet()
                renderAll()
                toast("Deleted $count item(s)")
            }
        }
    }

    private fun hideShippingHeader() = with(binding.cartSection) {
        listOf(tvTitle, tvAddressSummary, productAvailableBadge, divider1).forEach { it.isVisible = false }
    }

    private fun installCartRows() {
        val section = binding.cartSection
        val parent = section.root
        val placeholder = section.productCartItem.root

        val list = RecyclerView(this).apply {
            id = placeholder.id
            layoutManager = LinearLayoutManager(context)
            isNestedScrollingEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
            adapter = cartAdapter
        }

        val params = ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topToBottom = R.id.divider2 }

        val index = parent.indexOfChild(placeholder)
        parent.removeView(placeholder)
        parent.addView(list, index, params)
    }

    private fun renderCartSection() = with(binding.cartSection) {
        cbDeleteAction.isChecked = quantities.isNotEmpty() && quantities.keys.all { it in selectedIds }

        val subtotal = quantities
            .filterKeys { it in selectedIds }
            .entries
            .sumOf { (id, qty) -> (catalogById[id]?.price ?: 0L) * qty }
        tvSubtotalPrice.text = rupiah(subtotal)
    }

    private fun rupiah(value: Long): String =
        "Rp" + String.format(Locale.forLanguageTag("id-ID"), "%,d", value)

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}