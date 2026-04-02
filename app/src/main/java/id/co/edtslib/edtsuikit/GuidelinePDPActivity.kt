package id.co.edtslib.edtsuikit

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.DiffUtil
import id.co.edtslib.edtsuikit.GuidelinesCartActivity.CartItem
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinePdpactivityBinding
import id.co.edtslib.edtsuikit.databinding.ItemCartDiscountRedemptionInfoBinding
import id.co.edtslib.edtsuikit.databinding.ItemCartPlaceholderBinding
import id.co.edtslib.edtsuikit.databinding.ItemPdpBundlingSectionBinding
import id.co.edtslib.edtsuikit.databinding.ItemPdpDescriptionSectionBinding
import id.co.edtslib.edtsuikit.databinding.ItemPdpImageSectionBinding
import id.co.edtslib.edtsuikit.databinding.ItemPdpInformationSectionBinding
import id.co.edtslib.edtsuikit.helper.SelectionItem
import id.co.edtslib.uikit.adapter.BaseMultiTypeAdapter
import id.co.edtslib.uikit.adapter.BaseMultipleTypeAdapter
import id.co.edtslib.uikit.adapter.multiTypeAdapter
import id.co.edtslib.uikit.footer.PDPFooter
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.drawable
import id.co.edtslib.uikit.utils.setLightStatusBar
import id.co.edtslib.uikit.utils.setSystemBarStyle
import id.co.edtslib.uikit.utils.snack
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt
import id.co.edtslib.uikit.R as UIKitR

class GuidelinePDPActivity : AppCompatActivity() {

    private val binding by viewBinding<ActivityGuidelinePdpactivityBinding>()

    private var currentProductPrice = PRICE_BEFORE_DISCOUNT
    private var currentCount = 0
    private var incrementalCount = SINGLE_PRODUCT_COUNT

    private var quantityType = QuantityType.SINGLE

    private val shouldShowWarning by lazy {
        intent.getBooleanExtra(GuidelinesPDPEntryActivity.Companion.SHOULD_SHOW_TOAST, false)
    }

    private val shouldDisableVariant by lazy {
        intent.getBooleanExtra(GuidelinesPDPEntryActivity.Companion.SHOULD_DISABLE_VARIANT, false)
    }

    private val previousQuantity by lazy {
        intent.getIntExtra(GuidelinesSearchProductActivity.Companion.QUANTITY_KEY, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_guideline_pdpactivity)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.setPadding(0, systemBars.top.plus(12.dp.toInt()), systemBars.right, 12.dp.toInt())
            insets
        }

        attachRecyclerView()
        attachFooter()
        attachPriceChangeDelegate()
        bindOnClickListener()
        onBackPressedAction()
    }

    private fun attachRecyclerView() {
        binding.rvPDP.adapter = multiTypeAdapter(
            diffCallback = object : DiffUtil.ItemCallback<PDPItem>() {
                override fun areItemsTheSame(oldItem: PDPItem, newItem: PDPItem) = oldItem == newItem
                override fun areContentsTheSame(oldItem: PDPItem, newItem: PDPItem) = oldItem == newItem
            },
            viewTypeConfig = { item ->
                item.layoutType
            },
            bindingConfig = {
                registerImageSection()
                registerInformationSection()
                registerBundlingRecommendationSection()
                registerDescriptionSection()
            }
        ).apply {
            items = pdpItems
        }
    }

    private fun attachFooter() {
        binding.pdpFooter.binding.sbQuantity.setCount(previousQuantity)
        binding.pdpFooter.binding.btnAddToCart.isInvisible = previousQuantity > 0
        binding.pdpFooter.binding.tvPrice.text = (currentProductPrice * previousQuantity).toCurrency()
        binding.pdpFooter.binding.qtyBadge.apply {
            isVisible = previousQuantity > 0
            text = if (previousQuantity <= 99) previousQuantity.toString() else "99+"
            badgeColor = context.color(UIKitR.color.red_30)
        }

        binding.pdpFooter.doOnLayout {
            binding.rvPDP.updatePadding(
                bottom = (it.height + 16.dp).toInt()
            )
        }
    }

    private fun attachPriceChangeDelegate() {
        binding.pdpFooter.binding.sbQuantity.setAllowTextEditing(false)

        binding.pdpFooter.binding.sbQuantity.setOnCountChangeListener {
            currentCount = it

            if (it < 1) {
                binding.pdpFooter.setStateImmediately(PDPFooter.CartState.DEFAULT)
            }

            if (shouldShowWarning) {
                val currentCountMoreThan48 = it >= 48
                binding.pdpFooter.binding.sbQuantity.setPlusButtonEnabled(!currentCountMoreThan48)
                if (currentCountMoreThan48) {
                    binding.pdpFooter.snack(
                        message = "Produk ini hanya bisa dibeli maksimal 48 per transaksi",
                        resTextAppearance = id.co.edtslib.uikit.R.style.TextAppearance_Inter_Regular_B3,
                        isAnchored = true
                    )
                    binding.pdpFooter.binding.sbQuantity.setCount(48)
                }
            }

            binding.pdpFooter.binding.qtyBadge.text = if (it <= 99) it.toString() else "99+"
            binding.pdpFooter.binding.qtyBadge.requestLayout()
            binding.pdpFooter.binding.tvPrice.text = (currentProductPrice * currentCount).toCurrency()
        }
    }

    fun calculateDiscountPercent(
        originalPrice: Int,
        discountedPrice: Int
    ): Double {
        return ((originalPrice - discountedPrice).toDouble() / originalPrice) * 100
    }


    private fun bindOnClickListener() {
        binding.pdpFooter.binding.ctaCart.setOnClickListener {
            val destination = GuidelinesCartActivity::class.java
            Intent(this, destination).apply {
                putExtra(GuidelinesSearchProductActivity.Companion.QUANTITY_KEY, binding.pdpFooter.binding.sbQuantity.getCount())
            }.also {
                startActivity(it)
            }
        }

        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun <T> BaseMultiTypeAdapter<T>.registerImageSection() {
        registerViewType(
            viewType = PDPItem.ImageSlider.layoutType,
            bindingInflater = { layoutInflater, viewGroup, attachToParent ->
                ItemPdpImageSectionBinding.inflate(layoutInflater, viewGroup, attachToParent)
            },
            bind = { position, itemBinding, item ->
                (itemBinding as ItemPdpImageSectionBinding).ivProductPlaceholder.apply {
                    setImageDrawable(drawable(R.drawable.pdp_aqua_placeholder))
                }
            }
        )
    }

    private fun <T> BaseMultiTypeAdapter<T>.registerInformationSection() {
        registerViewType(
            viewType = PDPItem.Information.layoutType,
            bindingInflater = { layoutInflater, viewGroup, attachToParent ->
                ItemPdpInformationSectionBinding.inflate(layoutInflater, viewGroup, attachToParent)
            },
            bind = { position, itemBinding, item ->
                with(itemBinding as ItemPdpInformationSectionBinding) {
                    // TODO : Add Price Binding & Formatting

                    itemBinding.pdpSelectionGroup.doOnLayout {
                        itemBinding.pdpSelectionGroup.setRightCardEnabled(!shouldDisableVariant)
                    }


                    this.pdpSelectionGroup.setItems(
                        leftItem = SelectionItem("Satuan", "${QuantityType.SINGLE.quantity}Pcs"),
                        rightItem = SelectionItem("Karton", "${QuantityType.DOZEN.quantity}Pcs"),
                        defaultSelectedPosition = 0
                    )

                    this.pdpSelectionGroup.setOnSelectionChangedListener { item, position ->
                        quantityType = when(position) {
                            0 -> QuantityType.SINGLE
                            1 -> QuantityType.DOZEN
                            else -> QuantityType.SINGLE
                        }

                        incrementalCount = if (quantityType == QuantityType.SINGLE) {
                            SINGLE_PRODUCT_COUNT
                        } else {
                            DOZEN_PRODUCT_COUNT
                        }

                        binding.pdpFooter.binding.sbQuantity.setIncrementalValue(incrementalCount)

                        val price = if(quantityType == QuantityType.SINGLE) {
                            SINGLE_ITEM_PRICE
                        } else {
                            DOZEN_ITEM_PRICE
                        }

                        val discountPercentage = calculateDiscountPercent(PRICE_BEFORE_DISCOUNT, price)

                        this.tvProductPrice.text = price.toCurrency()
                        this.discountBadge.text = "${discountPercentage.roundToInt()}%"

                        currentProductPrice = price
                    }
                }
            }
        )
    }

    private fun <T> BaseMultiTypeAdapter<T>.registerBundlingRecommendationSection() {
        registerViewType(
            viewType = PDPItem.BundlingRecommendation.layoutType,
            bindingInflater = { layoutInflater, viewGroup, attachToParent ->
                ItemPdpBundlingSectionBinding.inflate(layoutInflater, viewGroup, attachToParent)
            },
            bind = { position, itemBinding, item ->
                with(itemBinding as ItemPdpBundlingSectionBinding) {}
            }
        )
    }

    private fun <T> BaseMultiTypeAdapter<T>.registerDescriptionSection() {
        registerViewType(
            viewType = PDPItem.Description.layoutType,
            bindingInflater = { layoutInflater, viewGroup, attachToParent ->
                ItemPdpDescriptionSectionBinding.inflate(layoutInflater, viewGroup, attachToParent)
            },
            bind = { position, itemBinding, item ->
                with(itemBinding as ItemPdpDescriptionSectionBinding) {}
            }
        )
    }

    private val pdpItems = listOf<PDPItem>(
        PDPItem.ImageSlider,
        PDPItem.Information,
        PDPItem.BundlingRecommendation,
        PDPItem.Description,
    )

    internal sealed class PDPItem(val layoutType: Int) {
        object ImageSlider: PDPItem(IMAGE_SLIDER)
        object Information: PDPItem(INFORMATION)
        object BundlingRecommendation: PDPItem(BUNDLING_RECOMMENDATION)
        object Description: PDPItem(DESCRIPTION)

        companion object {
            const val IMAGE_SLIDER = 1
            const val INFORMATION = 2
            const val BUNDLING_RECOMMENDATION = 3
            const val DESCRIPTION = 4
        }
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

    private enum class QuantityType(val quantity: Int) {
        SINGLE(SINGLE_PRODUCT_COUNT),
        DOZEN(DOZEN_PRODUCT_COUNT)
    }

    private fun onBackPressedAction() {
        onBackPressedDispatcher.addCallback(this) {

            finish()
        }

    }

    companion object {
        // Dummy Purpose
        private val DOZEN_ITEM_PRICE = 1800
        private val SINGLE_ITEM_PRICE = 2000
        private val PRICE_BEFORE_DISCOUNT = 4000
        private val SINGLE_PRODUCT_COUNT = 1
        private val DOZEN_PRODUCT_COUNT = 24
    }
}