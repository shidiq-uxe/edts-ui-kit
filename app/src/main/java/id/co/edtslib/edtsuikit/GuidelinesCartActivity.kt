package id.co.edtslib.edtsuikit

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesCartBinding
import id.co.edtslib.edtsuikit.databinding.ItemCartDiscountRedemptionInfoBinding
import id.co.edtslib.edtsuikit.databinding.ItemCartPlaceholderBinding
import id.co.edtslib.uikit.adapter.multiTypeAdapter
import id.co.edtslib.uikit.infobox.DiscountRedemptionBoxDelegate
import id.co.edtslib.uikit.utils.MarginItem
import id.co.edtslib.uikit.utils.TextStyleKey
import id.co.edtslib.uikit.utils.TextStyleProvider.get
import id.co.edtslib.uikit.utils.attachLinearMarginItemDecoration
import id.co.edtslib.uikit.utils.buildHighlightedMessage
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.drawable
import id.co.edtslib.uikit.R as UIKitR

class GuidelinesCartActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityGuidelinesCartBinding>()

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
    }

    private fun bindAnimations() {
        binding.discountRedemptionBox.attachToRecyclerView(binding.rvCart, DISCOUNT_REDEMPTION_BOX_ADAPTER_POSITION)
        binding.footerView.attachToRecyclerView(binding.rvCart)
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
        }
    }

    private fun stimulateDummyLoading() {
        binding.footerView.totalText = "Rp120.000"
        binding.footerView.isLoading = true

        Handler(Looper.getMainLooper()).postDelayed({
            binding.footerView.isLoading = false
            binding.footerView.isButtonEnabled = false

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
                        updateLayoutParams {
                            this.height = 300.dp.toInt()
                        }
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
                        updateLayoutParams {
                            this.height = 70.dp.toInt()
                        }
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
                        infoText = buildHighlightedMessage(
                            context = this@GuidelinesCartActivity,
                            message = "Tambah Rp50.000 untuk dapat promo",
                            highlightedMessages = listOf("Rp50.000"),
                            highlightedTextAppearance = listOf(
                                TextStyleKey.B4_SEMIBOLD.get(context, UIKitR.color.black_70)
                            )
                        )
                    }
                }
            )
            // Cart Content
            registerViewType(
                viewType = CartItem.CartContent.layoutType,
                bindingInflater = { layoutInflater, viewGroup, attachToParent ->
                    ItemCartPlaceholderBinding.inflate(layoutInflater, viewGroup, attachToParent)
                },
                bind = { position, itemBinding, item ->
                    (itemBinding as ItemCartPlaceholderBinding).root.apply {
                        updateLayoutParams {
                            this.height = 485.dp.toInt()
                        }
                        setImageDrawable(drawable(R.drawable.placeholder_cart_content))
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
                        updateLayoutParams {
                            this.height = 380.dp.toInt()
                        }
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
                        updateLayoutParams {
                            this.height = 285.dp.toInt()
                        }
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
                        updateLayoutParams {
                            this.height = 330.dp.toInt()
                        }
                        setImageDrawable(drawable(R.drawable.placeholder_frequently_bought_together))
                    }
                }
            )
        }
    )

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
    }
}