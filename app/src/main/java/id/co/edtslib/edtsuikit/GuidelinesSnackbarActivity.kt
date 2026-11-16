package id.co.edtslib.edtsuikit

import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesSnackbarBinding
import id.co.edtslib.edtsuikit.databinding.ViewGuidelinesProductToolbarBinding
import id.co.edtslib.uikit.core.snackbar.CoreSnackbar
import id.co.edtslib.uikit.recipes.snackbar.klik.KlikToast
import id.co.edtslib.uikit.recipes.snackbar.poinku.PoinkuToast
import id.co.edtslib.uikit.R as UIKitR

class GuidelinesSnackbarActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityGuidelinesSnackbarBinding>()
    private val toolbarBinding by lazy { ViewGuidelinesProductToolbarBinding.inflate(layoutInflater) }
    private val productNames by lazy { resources.getStringArray(R.array.guidelines_products) }
    private var product = Product.CORE
    private var snackbar: Snackbar? = null

    private fun setupToolbar() {
        toolbarBinding.tvTitle.setText(R.string.guidelines_snackbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayShowCustomEnabled(true)
            setCustomView(
                toolbarBinding.root,
                ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            )
        }
        toolbarBinding.tvProduct.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.guidelines_product)
                .setSingleChoiceItems(productNames, product.ordinal) { dialog, index ->
                    snackbar?.dismiss()
                    product = Product.entries[index]
                    updateProductLabel()
                    dialog.dismiss()
                }
                .show()
        }
        updateProductLabel()
    }

    private fun updateProductLabel() {
        val name = productNames[product.ordinal]
        toolbarBinding.tvProduct.text = name
        toolbarBinding.tvProduct.contentDescription = getString(R.string.guidelines_select_product, name)
    }

    private fun showSnackbar() {
        val presenter = when (product) {
            Product.CORE -> CoreSnackbar
            Product.KLIK -> KlikToast
            Product.POINKU -> PoinkuToast
        }
        val variant = if (binding.rbError.isChecked) CoreSnackbar.Variant.ERROR else CoreSnackbar.Variant.INFO
        val message = when {
            binding.rbMultiline.isChecked -> R.string.snackbar_demo_multiline_message
            product == Product.KLIK -> R.string.snackbar_demo_klik_message
            product == Product.POINKU -> R.string.snackbar_demo_poinku_message
            else -> R.string.snackbar_demo_core_message
        }
        val animation = if (binding.rbFade.isChecked) Snackbar.ANIMATION_MODE_FADE else Snackbar.ANIMATION_MODE_SLIDE
        snackbar = presenter.show(
            host = binding.root,
            message = getString(message),
            variant = variant,
            iconRes = UIKitR.drawable.ic_placeholder_medium_24.takeIf { binding.cbIncludeIcon.isChecked },
            actionText = getString(R.string.guidelines_button).takeIf { binding.cbIncludeAction.isChecked },
            anchor = binding.btnShowSnackbar,
            animationMode = animation
        ) {
            Toast.makeText(this, R.string.snackbar_demo_action_clicked, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        product = Product.entries[savedInstanceState?.getInt(STATE_PRODUCT) ?: Product.CORE.ordinal]
        setContentView(R.layout.activity_guidelines_snackbar)
        setupToolbar()
        binding.btnShowSnackbar.setOnClickListener { showSnackbar() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_PRODUCT, product.ordinal)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        snackbar?.dismiss()
        super.onDestroy()
    }

    private enum class Product { CORE, KLIK, POINKU }

    private companion object {
        const val STATE_PRODUCT = "snackbar_product"
    }
}
