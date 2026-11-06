package id.co.edtslib.edtsuikit

import android.content.res.ColorStateList
import android.graphics.Rect
import android.os.Bundle
import android.view.Gravity
import android.view.TouchDelegate
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.co.edtslib.edtsuikit.databinding.ActivityButtonBinding
import id.co.edtslib.edtsuikit.databinding.ItemButtonRecipeBinding
import id.co.edtslib.edtsuikit.databinding.ViewGuidelinesProductToolbarBinding
import id.co.edtslib.uikit.adapter.BaseAdapter
import id.co.edtslib.uikit.core.button.CoreButton
import id.co.edtslib.uikit.recipes.button.klik.KlikButton
import id.co.edtslib.uikit.recipes.button.poinku.PoinkuButton
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.drawable
import kotlin.math.roundToInt
import id.co.edtslib.uikit.R as UIKitR

class GuidelinesButtonActivity : GuidelinesBaseActivity() {

    override val hasConfigMenu = true
    override val enableEdgeToEdge = false

    private val binding by viewBinding<ActivityButtonBinding>()
    private val toolbarBinding by lazy { ViewGuidelinesProductToolbarBinding.inflate(layoutInflater) }
    private val configurations = mutableMapOf<ButtonProduct, ButtonConfig>()
    private var selectedProduct = ButtonProduct.CORE
    private var buttonAdapter: BaseAdapter<ButtonPreview, ItemButtonRecipeBinding>? = null
    private var productDialog: androidx.appcompat.app.AlertDialog? = null
    private val config: ButtonConfig
        get() = configurations[selectedProduct] ?: ButtonConfig()

    private fun setupToolbar() {
        toolbarBinding.tvTitle.setText(R.string.guidelines_button)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayShowCustomEnabled(true)
            setCustomView(
                toolbarBinding.root,
                ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            )
        }
        toolbarBinding.tvProduct.setOnClickListener { showProducts() }
        toolbarBinding.root.doOnLayout { host ->
            host.touchDelegate = TouchDelegate(Rect(0, 0, host.width, host.height), toolbarBinding.tvProduct)
        }
        updateProductLabel()
    }

    private fun showProducts() {
        if (productDialog?.isShowing == true) return
        productDialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.guidelines_product)
            .setSingleChoiceItems(R.array.guidelines_products, selectedProduct.ordinal) { dialog, position ->
                selectedProduct = ButtonProduct.entries[position]
                updateProductLabel()
                updateButtons()
                binding.rvButtons.scrollToPosition(0)
                dialog.dismiss()
            }
            .show()
    }

    private fun updateProductLabel() {
        val product = resources.getStringArray(R.array.guidelines_products)[selectedProduct.ordinal]
        toolbarBinding.tvProduct.text = product
        toolbarBinding.tvProduct.contentDescription = getString(R.string.guidelines_select_product, product)
    }

    private fun setupButtonList() {
        binding.rvButtons.layoutManager = LinearLayoutManager(this)
        buttonAdapter = BaseAdapter.adapterOf<ButtonPreview, ItemButtonRecipeBinding>(
            diff = BaseAdapter.Diff(
                areItemsTheSame = { old, new ->
                    old.product == new.product && old.example == new.example
                },
                areContentsTheSame = { old, new -> old == new }
            ),
            register = BaseAdapter.Register(
                onBindHolder = { _, item, row, _ -> bindButton(row, item) }
            )
        ).also { binding.rvButtons.adapter = it }
        updateButtons()
    }

    private fun updateButtons() {
        buttonAdapter?.items = config.examples(selectedProduct).map { ButtonPreview(it, selectedProduct, config) }
    }

    private fun bindButton(row: ItemButtonRecipeBinding, preview: ButtonPreview) {
        val example = preview.example
        val label = getString(
            if (example.isIconOnly) R.string.button_recipe_icon_label else R.string.button_recipe_label,
            getString(example.variantLabel),
            getString(example.sizeLabel)
        )
        row.tvRecipe.text = label
        val button = when (preview.product) {
            ButtonProduct.CORE -> CoreButton(this)
            ButtonProduct.KLIK -> KlikButton(this)
            ButtonProduct.POINKU -> PoinkuButton(this)
        }.apply {
            buttonRecipe = example.recipe
            text = if (example.isIconOnly) "" else getString(R.string.guidelines_button)
            if (example.hasIcons) {
                startIcon = context.drawable(UIKitR.drawable.ic_placeholder_medium_24)
                endIcon = if (example.isIconOnly) null else context.drawable(UIKitR.drawable.ic_placeholder_medium_24)
            }
            contentDescription = label
            isEnabled = !preview.config.disabled
            isDestructive = preview.product == ButtonProduct.KLIK && preview.config.destructive
            isPressed = preview.config.pressed && isEnabled
            shouldShowShimmer = preview.config.shimmer
            applyOverrides(this, preview)
            jumpDrawablesToCurrentState()
            setOnClickListener {
                Toast.makeText(context, getString(R.string.button_recipe_clicked, label), Toast.LENGTH_SHORT).show()
            }
        }
        (row.buttonHost.tag as? CoreButton)?.shouldShowShimmer = false
        row.buttonHost.removeAllViews()
        row.buttonHost.tag = button
        row.buttonHost.addView(
            button,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER)
        )
        row.buttonHost.doOnLayout { host ->
            host.touchDelegate = TouchDelegate(Rect(0, 0, host.width, host.height), button)
        }
    }

    private fun applyOverrides(button: CoreButton, preview: ButtonPreview) {
        preview.config.cornerRadius?.let { radius ->
            button.stateListAnimator = null
            button.cornerRadius = radius.dp.roundToInt()
            button.shapeAppearanceModel = button.shapeAppearanceModel.toBuilder()
                .setAllCornerSizes(radius.dp)
                .build()
        }
        if (preview.config.disabled || button.isDestructive) return
        preview.config.tintColor?.let { color ->
            val tint = ColorStateList.valueOf(color)
            when (preview.example.variantLabel) {
                R.string.button_recipe_primary, R.string.guidelines_neutral,
                R.string.button_recipe_label_only, R.string.button_recipe_with_icons -> button.backgroundTintList = tint
                else -> {
                    button.setTextColor(tint)
                    button.iconTint = tint
                    if (button.strokeWidth > 0) button.strokeColor = tint
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedProduct = savedInstanceState?.getString(STATE_PRODUCT)
            ?.let { name -> ButtonProduct.entries.find { it.name == name } } ?: ButtonProduct.CORE
        ButtonProduct.entries.forEach { product ->
            savedInstanceState?.getBundle(product.name)?.let { configurations[product] = it.toButtonConfig() }
        }
        supportFragmentManager.setFragmentResultListener(ButtonConfigBottomSheet.RESULT_CONFIG, this) { _, result ->
            configurations[selectedProduct] = result.toButtonConfig()
            updateButtons()
        }
        setContentView(R.layout.activity_button)
        setupToolbar()
        setupButtonList()
    }

    override fun onConfigMenuClicked() {
        if (supportFragmentManager.findFragmentByTag(ButtonConfigBottomSheet.TAG) != null) return
        ButtonConfigBottomSheet.newInstance(config, selectedProduct)
            .show(supportFragmentManager, ButtonConfigBottomSheet.TAG)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_PRODUCT, selectedProduct.name)
        configurations.forEach { (product, settings) -> outState.putBundle(product.name, settings.toBundle()) }
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        productDialog?.dismiss()
        super.onDestroy()
    }

    private data class ButtonPreview(
        val example: ButtonRecipeExample,
        val product: ButtonProduct,
        val config: ButtonConfig
    )

    companion object {
        private const val STATE_PRODUCT = "button_product"
    }
}
