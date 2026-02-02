package id.co.edtslib.edtsuikit

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.DiffUtil
import id.co.edtslib.edtsuikit.GuidelinePDPActivity.PDPItem
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesSearchProductBinding
import id.co.edtslib.edtsuikit.databinding.ItemGridProductBinding
import id.co.edtslib.edtsuikit.databinding.ItemPdpImageSectionBinding
import id.co.edtslib.uikit.adapter.multiTypeAdapter
import id.co.edtslib.uikit.utils.colorStateList
import id.co.edtslib.uikit.utils.drawable
import id.co.edtslib.uikit.utils.toCurrency
import id.co.edtslib.uikit.R as UIKitR

class GuidelinesSearchProductActivity : AppCompatActivity() {

    private val binding by viewBinding<ActivityGuidelinesSearchProductBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_guidelines_search_product)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        attachRecyclerView()
        bindClickListener()

        binding.fabActionCart.apply {
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
        }

        binding.sbSearchProduct.text = "Aqua"
    }

    private var previousQuantity = 0

    private fun attachRecyclerView() {
        binding.rvProducts.adapter = multiTypeAdapter(
            diffCallback = object : DiffUtil.ItemCallback<Int>() {
                override fun areItemsTheSame(
                    oldItem: Int,
                    newItem: Int
                ) = oldItem == newItem

                override fun areContentsTheSame(
                    oldItem: Int,
                    newItem: Int
                ) = oldItem == newItem

            },
            viewTypeConfig = { item ->
                1
            },
            bindingConfig = {
                registerViewType(
                    viewType = 1,
                    bindingInflater = { layoutInflater, viewGroup, attachToParent ->
                        ItemGridProductBinding.inflate(layoutInflater, viewGroup, attachToParent)
                    },
                    bind = { position, itemBinding, item ->
                        (itemBinding as ItemGridProductBinding).apply {
                            this.setStepperColor()

                            this.ivCouponImage.setImageDrawable(
                                drawable(R.drawable.pdp_aqua_placeholder)
                            )

                            this.sbQuantity.setOnCountChangeListener { quantity ->
                                val shouldHide = quantity < 0
                                val basePrice = 2000
                                val totalPrice = basePrice * quantity

                                binding.fabActionCart.supportingText = "($quantity Barang)"
                                binding.fabActionCart.highlightText = totalPrice.toCurrency()

                                val wasZero = previousQuantity == 0
                                val isZero = quantity == 0

                                if (wasZero != isZero) {
                                    animateActionToCartButton(hide = isZero)
                                }

                                previousQuantity = quantity
                            }

                            this.root.setOnClickListener {
                                Intent(this@GuidelinesSearchProductActivity, GuidelinePDPActivity::class.java).apply {
                                    putExtra(QUANTITY_KEY, sbQuantity.getCount())
                                }.also {
                                    startActivity(it)
                                }
                            }
                        }
                    }
                )
            },
        ).apply {
            items = List(1) { it }
        }
    }

    private fun ItemGridProductBinding.setStepperColor() {
        sbQuantity.binding.singlePlusButton.backgroundTintList = colorStateList(UIKitR.color.slr_button_filled_bg)
        sbQuantity.binding.singlePlusButton.iconTint = colorStateList(UIKitR.color.white)
        sbQuantity.binding.stepperContainer.setCardBackgroundColor(colorStateList(UIKitR.color.slr_button_filled_bg))
        sbQuantity.binding.stepperContainer.strokeWidth = 0
        sbQuantity.binding.countText.setTextColor(colorStateList(UIKitR.color.white))
        sbQuantity.binding.countEditText.setTextColor(colorStateList(UIKitR.color.white))
        sbQuantity.binding.plusButton.backgroundTintList = colorStateList(UIKitR.color.white)
        sbQuantity.binding.plusButton.iconTint = colorStateList(UIKitR.color.primary_30)
        sbQuantity.binding.minusButton.iconTint = colorStateList(UIKitR.color.white)
    }

    private fun bindClickListener() {
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.fabActionCart.setOnClickListener {
            Intent(this, GuidelinesCartActivity::class.java).apply {
                putExtra(GuidelinesSearchProductActivity.Companion.QUANTITY_KEY, previousQuantity)
            }.also {
                startActivity(it)
            }
        }
    }

    private fun animateActionToCartButton(hide: Boolean = false) {
        val duration = 300L
        val interpolator = FastOutSlowInInterpolator()

        if (hide) {
            binding.fabActionCart.animate()
                .translationY(binding.fabActionCart.height.toFloat() + 100f)
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(duration)
                .setInterpolator(interpolator)
                .start()
        } else {
            binding.fabActionCart.apply {
                alpha = 0f
                scaleX = 0.8f
                scaleY = 0.8f
                translationY = height.toFloat() + 100f
            }

            binding.fabActionCart.animate()
                .translationY(0f)
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(duration)
                .setInterpolator(interpolator)
                .start()
        }
    }

    enum class Selector {
        RadioButton,
        Checkbox
    }

    companion object {
        val QUANTITY_KEY = "quantity_key"
    }
}