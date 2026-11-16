package id.co.edtslib.edtsuikit

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.co.edtslib.edtsuikit.databinding.ActivityAlertboxBinding
import id.co.edtslib.edtsuikit.databinding.ItemAlertBoxExampleBinding
import id.co.edtslib.edtsuikit.databinding.ViewGuidelinesProductToolbarBinding
import id.co.edtslib.uikit.adapter.BaseAdapter
import id.co.edtslib.uikit.alert.AlertBoxDelegate
import id.co.edtslib.uikit.core.alertbox.CoreAlertBox
import id.co.edtslib.uikit.recipes.alertbox.klik.KlikInfoBox
import id.co.edtslib.uikit.recipes.alertbox.poinku.PoinkuAlertBox
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.drawable
import id.co.edtslib.uikit.R as UIKitR

class GuidelinesAlertBoxActivity : GuidelinesBaseActivity() {

    override val hasConfigMenu = true
    override val enableEdgeToEdge = false

    private val binding by viewBinding<ActivityAlertboxBinding>()
    private val toolbarBinding by lazy { ViewGuidelinesProductToolbarBinding.inflate(layoutInflater) }
    private val configurations = mutableMapOf<AlertBoxProduct, AlertBoxConfig>()
    private val dismissed = mutableSetOf<Int>()
    private var product = AlertBoxProduct.CORE
    private var examplesAdapter: BaseAdapter<Preview, ItemAlertBoxExampleBinding>? = null
    private var productDialog: androidx.appcompat.app.AlertDialog? = null
    private val config: AlertBoxConfig
        get() = configurations[product] ?: AlertBoxConfig(showButton = product == AlertBoxProduct.KLIK)

    private fun setupToolbar() {
        toolbarBinding.tvTitle.setText(R.string.guidelines_alert_box)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayShowCustomEnabled(true)
            setCustomView(toolbarBinding.root,
                ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
        toolbarBinding.tvProduct.setOnClickListener {
            if (productDialog?.isShowing == true) return@setOnClickListener
            productDialog = MaterialAlertDialogBuilder(this)
                .setTitle(R.string.guidelines_product)
                .setSingleChoiceItems(R.array.guidelines_products, product.ordinal) { dialog, index ->
                    product = AlertBoxProduct.entries[index]
                    updateProductLabel()
                    updateExamples()
                    binding.rvAlertBoxes.scrollToPosition(0)
                    dialog.dismiss()
                }.show()
        }
        updateProductLabel()
    }

    private fun updateProductLabel() {
        val name = resources.getStringArray(R.array.guidelines_products)[product.ordinal]
        toolbarBinding.tvProduct.text = name
        toolbarBinding.tvProduct.contentDescription = getString(R.string.guidelines_select_product, name)
    }

    private fun updateExamples() {
        examplesAdapter?.items = AlertBoxExamples.forProduct(product).filter { it.recipe !in dismissed }
            .map { Preview(it, product, config) }
    }

    private fun bindExample(row: ItemAlertBoxExampleBinding, item: Preview) {
        row.tvExample.setText(item.example.label)
        val padding = if (item.example.isFullWidth) 0 else 16.dp.toInt()
        row.root.setPadding(padding, 0, padding, 16.dp.toInt())
        val alert = when (item.product) {
            AlertBoxProduct.CORE -> CoreAlertBox(this)
            AlertBoxProduct.KLIK -> KlikInfoBox(this)
            AlertBoxProduct.POINKU -> PoinkuAlertBox(this)
        }.apply {
            alertRecipe = item.example.recipe
            text = item.config.message.ifEmpty {
                getString(if (item.product == AlertBoxProduct.POINKU) R.string.alert_demo_poinku_message else R.string.alert_demo_klik_message)
            }
            isHtml = item.config.isHtml
            isStartIconVisible = item.config.showStartIcon
            isCloseIconVisible = item.config.showClose
            isButtonVisible = item.config.showButton
            buttonText = getString(R.string.guidelines_button)
            isActionVisible = item.config.showAction
            actionText = getString(R.string.alert_demo_inline_action)
            actionEndIcon = context.drawable(UIKitR.drawable.ic_chevron_right_16)
            iconAlignment = CoreAlertBox.IconAlignment.entries[item.config.iconAlignment]
            isEnabled = !item.config.disabled
            delegate = object : AlertBoxDelegate {
                override fun onCloseClickListener(view: View) {
                    dismissed.add(item.example.recipe)
                    updateExamples()
                }

                override fun onButtonClickListener(view: View) = showAction(R.string.alert_demo_footer_clicked)
                override fun onActionClickListener(view: View) = showAction(R.string.alert_demo_inline_clicked)
            }
        }
        row.flAlert.removeAllViews()
        row.flAlert.addView(alert, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    }

    private fun showAction(message: Int) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        product = savedInstanceState?.getString(STATE_PRODUCT)?.let { name ->
            AlertBoxProduct.entries.find { it.name == name }
        } ?: AlertBoxProduct.CORE
        AlertBoxProduct.entries.forEach { key ->
            savedInstanceState?.getBundle(key.name)?.let { configurations[key] = AlertBoxConfig.fromBundle(it) }
        }
        savedInstanceState?.getIntArray(STATE_DISMISSED)?.let { dismissed.addAll(it.toList()) }
        setContentView(R.layout.activity_alertbox)
        setupToolbar()
        binding.rvAlertBoxes.layoutManager = LinearLayoutManager(this)
        examplesAdapter = BaseAdapter.adapterOf<Preview, ItemAlertBoxExampleBinding>(
            diff = BaseAdapter.Diff(
                areItemsTheSame = { a, b -> a.product == b.product && a.example.recipe == b.example.recipe },
                areContentsTheSame = { a, b -> a == b }
            ),
            register = BaseAdapter.Register(onBindHolder = { _, item, row, _ -> bindExample(row, item) })
        ).also { binding.rvAlertBoxes.adapter = it }
        supportFragmentManager.setFragmentResultListener(AlertBoxConfigBottomSheet.RESULT, this) { _, result ->
            configurations[product] = AlertBoxConfig.fromBundle(result)
            dismissed.clear()
            updateExamples()
        }
        binding.btnResetAlerts.setOnClickListener {
            dismissed.clear()
            configurations.remove(product)
            updateExamples()
        }
        updateExamples()
    }

    override fun onConfigMenuClicked() {
        if (supportFragmentManager.findFragmentByTag(AlertBoxConfigBottomSheet.TAG) == null) {
            AlertBoxConfigBottomSheet.newInstance(config, product == AlertBoxProduct.KLIK).show(supportFragmentManager, AlertBoxConfigBottomSheet.TAG)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_PRODUCT, product.name)
        outState.putIntArray(STATE_DISMISSED, dismissed.toIntArray())
        configurations.forEach { (key, settings) -> outState.putBundle(key.name, settings.toBundle()) }
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        productDialog?.dismiss()
        super.onDestroy()
    }

    private data class Preview(val example: AlertBoxExample, val product: AlertBoxProduct, val config: AlertBoxConfig)

    companion object {
        private const val STATE_PRODUCT = "alert_product"
        private const val STATE_DISMISSED = "alert_dismissed"
    }
}
