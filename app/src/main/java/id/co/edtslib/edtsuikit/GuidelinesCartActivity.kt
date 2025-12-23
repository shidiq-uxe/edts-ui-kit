package id.co.edtslib.edtsuikit

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesCartBinding
import id.co.edtslib.edtsuikit.databinding.ItemCartDiscountRedemptionInfoBinding
import id.co.edtslib.edtsuikit.databinding.ItemCartPlaceholderBinding
import id.co.edtslib.edtsuikit.databinding.ItemCartProductContentSectionBinding
import id.co.edtslib.uikit.adapter.multiTypeAdapter
import id.co.edtslib.uikit.footer.CartFooter
import id.co.edtslib.uikit.footer.CartFooterDelegate
import id.co.edtslib.uikit.infobox.DiscountRedemptionBoxDelegate
import id.co.edtslib.uikit.utils.MarginItem
import id.co.edtslib.uikit.utils.TextStyleKey
import id.co.edtslib.uikit.utils.TextStyleProvider.get
import id.co.edtslib.uikit.utils.attachLinearMarginItemDecoration
import id.co.edtslib.uikit.utils.buildHighlightedMessage
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.drawable
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt
import id.co.edtslib.uikit.R as UIKitR

class GuidelinesCartActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityGuidelinesCartBinding>()

    private var currentProductPrice = PRICE_BEFORE_DISCOUNT
    private var currentQuantity = 0
    private val itemsPerDozen = 24

    private val previousQuantity by lazy {
        intent.getIntExtra(GuidelinesSearchProductActivity.Companion.QUANTITY_KEY, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guidelines_cart)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bindAdapter()
        bindAnimations()
        stimulateDummyLoading()
        bindDelegate()
    }

    private fun bindAnimations() {
        // binding.footerView.extendedFooter.hideCouponWithY(false)

        binding.discountRedemptionBox.shouldBeHidden = false

        binding.footerView.attachToRecyclerView(binding.rvCart)
        binding.discountRedemptionBox.attachToRecyclerView(binding.rvCart, DISCOUNT_REDEMPTION_BOX_ADAPTER_POSITION)
    }

    private fun bindAdapter() {
        binding.rvCart.adapter = dummyCartAdapter().apply {
            items = cartItems
        }

        binding.rvCart.attachLinearMarginItemDecoration(
            orientation = LinearLayoutManager.VERTICAL,
            margin = MarginItem(
                first = 0,
                top = 8.dp.toInt()
            )
        )
    }

    private fun bindDelegate() {
        binding.discountRedemptionBox.delegate = object : DiscountRedemptionBoxDelegate {
            override fun onInfoBoxClick(view: View) {
                // Action
            }

            override fun onScrolled(
                rv: RecyclerView,
                dx: Int,
                dy: Int
            ) {
                // Add Another Function when scrolling
            }

            override fun onScrollStateChanged(
                rv: RecyclerView,
                newState: Int
            ) {

            }
        }

        binding.footerView.isHtml = true
        binding.footerView.buttonText = "Pilih Pembayaran"
        // binding.footerView.buttonColor = color(id.co.edtslib.uikit.R.color.poin_coffee_button)
        // binding.footerView.extendedFooter.isVisible = false
        binding.footerView.delegate = object : CartFooterDelegate {
            override fun onCouponSectionClick() {
                binding.footerView.playPreLoadedAnimations()

                binding.footerView.isHtml = true
                binding.footerView.infoText = "Diskon <b>Rp15.000</b> Terpakai"

            }

            override fun onActionButtonClick() {
                stimulateDummyLoading()
            }

            override fun onSummaryClick() {
                if (binding.footerView.currentState == CartFooter.State.DEFAULT) {
                    binding.footerView.isConfettiBackgroundVisible = false
                    binding.footerView.setState(
                        CartFooter.State.WARNING
                    )
                } else {
                    binding.footerView.setState(
                        CartFooter.State.DEFAULT
                    )
                }

            }
        }
    }

    private fun stimulateDummyLoading() {
        binding.footerView.totalText = "Rp120.000"
        binding.footerView.isLoading = true

        Handler(Looper.getMainLooper()).postDelayed({
            binding.footerView.isLoading = false
            // binding.footerView.isButtonEnabled = false

        }, 3000L)

    }

    private fun dummyCartAdapter() = multiTypeAdapter(
        diffCallback = object : DiffUtil.ItemCallback<CartItem>() {
            override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem) = oldItem == newItem
            override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem) = oldItem == newItem
        },
        viewTypeConfig = { item ->
            item.layoutType
        },
        bindingConfig = {
            // Order Type
            registerViewType(
                viewType = CartItem.OrderType.layoutType,
                bindingInflater = { layoutInflater, viewGroup, attachToParent ->
                    ItemCartPlaceholderBinding.inflate(layoutInflater, viewGroup, attachToParent)
                },
                bind = { position, itemBinding, item ->
                    (itemBinding as ItemCartPlaceholderBinding).root.apply {
                        setImageDrawable(drawable(R.drawable.placeholder_order_type))
                    }
                }
            )
            // Services Switcher
            registerViewType(
                viewType = CartItem.ServiceSwitcher.layoutType,
                bindingInflater = { layoutInflater, viewGroup, attachToParent ->
                    ItemCartPlaceholderBinding.inflate(layoutInflater, viewGroup, attachToParent)
                },
                bind = { position, itemBinding, item ->
                    (itemBinding as ItemCartPlaceholderBinding).root.apply {
                        setImageDrawable(drawable(R.drawable.placeholder_service_switcher))
                    }
                }
            )
            // Discount Redemption Info
            registerViewType(
                viewType = CartItem.DiscountRedemptionInfo.layoutType,
                bindingInflater = { layoutInflater, viewGroup, attachToParent ->
                    ItemCartDiscountRedemptionInfoBinding.inflate(layoutInflater, viewGroup, attachToParent)
                },
                bind = { position, itemBinding, item ->
                    (itemBinding as ItemCartDiscountRedemptionInfoBinding).root.apply {
                        /*infoText = buildHighlightedMessage(
                            context = this@GuidelinesCartActivity,
                            message = "Kamu sudah ambil 1 dari 3 promo",
                            highlightedMessages = listOf("1 dari 3 promo"),
                            highlightedTextAppearance = listOf(
                                TextStyleKey.B4_BOLD.get(context, UIKitR.color.black_70)
                            )
                        )*/

                        isHtml = true
                        infoText = "Kamu sudah ambil <b>1 dari 3 promo</b>"
                    }
                }
            )
            // Cart Content
            registerViewType(
                viewType = CartItem.CartContent.layoutType,
                bindingInflater = { layoutInflater, viewGroup, attachToParent ->
                    ItemCartProductContentSectionBinding.inflate(layoutInflater, viewGroup, attachToParent)
                },
                bind = { position, itemBinding, item ->
                    (itemBinding as ItemCartProductContentSectionBinding).apply {
                        this.productCartItem.ivProductImage.setImageDrawable(
                            drawable(R.drawable.pdp_aqua_placeholder)
                        )

                        this.cbDeleteAction.isChecked = true
                        this.productCartItem.cbSelection.isChecked = true

                        this.productCartItem.sbQuantity.setOnCountChangeListener {
                            currentQuantity = it

                            val totalPrice = calculateTotalPrice(currentQuantity)
                            val averagePrice = calculateAveragePrice(currentQuantity, totalPrice)
                            val discountPercent = calculateDiscountPercent(PRICE_BEFORE_DISCOUNT, averagePrice)

                            this.productCartItem.tvProductPrice.text = averagePrice.toCurrency()
                            this.productCartItem.discountBadge.text = "${discountPercent.toInt()}%"

                            this.tvSubtotalPrice.text = totalPrice.toCurrency()
                            binding.footerView.totalText = totalPrice.toCurrency()
                        }

                        root.doOnLayout {
                            currentQuantity = previousQuantity
                            this.productCartItem.sbQuantity.setCount(currentQuantity)

                            val totalPrice = calculateTotalPrice(currentQuantity)
                            val averagePrice = calculateAveragePrice(currentQuantity, totalPrice)
                            val discountPercent = calculateDiscountPercent(PRICE_BEFORE_DISCOUNT, averagePrice)


                            this.productCartItem.tvProductPrice.text = averagePrice.toCurrency()
                            this.productCartItem.discountBadge.text = "${discountPercent.roundToInt()}%"

                            this.tvSubtotalPrice.text = totalPrice.toCurrency()
                            binding.footerView.totalText = totalPrice.toCurrency()
                        }
                    }
                }
            )
            // Discount Redemption Content
            registerViewType(
                viewType = CartItem.DiscountRedemptionContent.layoutType,
                bindingInflater = { layoutInflater, viewGroup, attachToParent ->
                    ItemCartPlaceholderBinding.inflate(layoutInflater, viewGroup, attachToParent)
                },
                bind = { position, itemBinding, item ->
                    (itemBinding as ItemCartPlaceholderBinding).root.apply {
                        setImageDrawable(drawable(R.drawable.placeholder_discount_redemption_content))
                    }
                }
            )
            // Fair Promo
            registerViewType(
                viewType = CartItem.FairPromo.layoutType,
                bindingInflater = { layoutInflater, viewGroup, attachToParent ->
                    ItemCartPlaceholderBinding.inflate(layoutInflater, viewGroup, attachToParent)
                },
                bind = { position, itemBinding, item ->
                    (itemBinding as ItemCartPlaceholderBinding).root.apply {
                        setImageDrawable(drawable(R.drawable.placeholder_fair_promo))
                    }
                }
            )

            // Frequently Bought Together
            registerViewType(
                viewType = CartItem.FrequentlyBoughtTogether.layoutType,
                bindingInflater = { layoutInflater, viewGroup, attachToParent ->
                    ItemCartPlaceholderBinding.inflate(layoutInflater, viewGroup, attachToParent)
                },
                bind = { position, itemBinding, item ->
                    (itemBinding as ItemCartPlaceholderBinding).root.apply {
                        setImageDrawable(drawable(R.drawable.placeholder_frequently_bought_together))
                    }
                }
            )
        }
    )

    private fun calculateTotalPrice(
        totalItems: Int,
        itemsPerDozen: Int = 24,
        dozenItemPrice: Int = DOZEN_ITEM_PRICE,
        singleItemPrice: Int = SINGLE_ITEM_PRICE
    ): Int {
        val dozenCount = totalItems / itemsPerDozen
        val remainingPieces = totalItems % itemsPerDozen

        return (dozenCount * itemsPerDozen * dozenItemPrice) +
                (remainingPieces * singleItemPrice)
    }

    private fun calculateAveragePrice(
        totalItems: Int,
        totalPrice: Int,
    ): Int {
        return totalItems
            .takeIf { it > 0 }
            ?.let { totalPrice / it }
            ?: 0
    }

    fun calculateDiscountPercent(
        originalPrice: Int,
        discountedPrice: Int
    ): Double {
        return ((originalPrice - discountedPrice).toDouble() / originalPrice) * 100
    }

    private fun Number.toCurrency(
        locale: Locale = Locale("id", "ID"),
        showDecimals: Boolean = false
    ): String {
        val formatter = NumberFormat.getCurrencyInstance(locale).apply {
            minimumFractionDigits = if (showDecimals) 2 else 0
            maximumFractionDigits = if (showDecimals) 2 else 0
        }
        return formatter.format(this)
    }

    private val cartItems = listOf<CartItem>(
        CartItem.OrderType,
        CartItem.ServiceSwitcher,
        CartItem.DiscountRedemptionInfo,
        CartItem.CartContent,
        CartItem.DiscountRedemptionContent,
        CartItem.FairPromo,
        CartItem.FrequentlyBoughtTogether
    )

    internal sealed class CartItem(val layoutType: Int) {
        object OrderType: CartItem(ORDER_TYPE)
        object ServiceSwitcher: CartItem(SERVICE_SWITCHER)
        object DiscountRedemptionInfo: CartItem(DISCOUNT_REDEMPTION_INFO)
        object CartContent: CartItem(CART_CONTENT)
        object DiscountRedemptionContent: CartItem(DISCOUNT_REDEMPTION_CONTENT)
        object FairPromo: CartItem(FAIR_PROMO)
        object FrequentlyBoughtTogether : CartItem(FREQUENTLY_BOUGHT_TOGETHER)

        companion object {
            const val ORDER_TYPE = 1
            const val SERVICE_SWITCHER = 2
            const val DISCOUNT_REDEMPTION_INFO = 3
            const val CART_CONTENT = 4
            const val DISCOUNT_REDEMPTION_CONTENT = 5
            const val FAIR_PROMO = 6
            const val FREQUENTLY_BOUGHT_TOGETHER = 7
        }
    }

    companion object {
        private const val DISCOUNT_REDEMPTION_BOX_ADAPTER_POSITION = 2
        private val DOZEN_ITEM_PRICE = 1800
        private val SINGLE_ITEM_PRICE = 2000
        private val PRICE_BEFORE_DISCOUNT = 4000
        private val SINGLE_PRODUCT_COUNT = 1
        private val DOZEN_PRODUCT_COUNT = 24
    }
}