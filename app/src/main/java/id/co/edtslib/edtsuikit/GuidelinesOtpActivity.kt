package id.co.edtslib.edtsuikit

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.core.view.children
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.co.edtslib.edtsuikit.databinding.ActivityOtpBinding
import id.co.edtslib.edtsuikit.databinding.ViewGuidelinesProductToolbarBinding

class GuidelinesOtpActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityOtpBinding>()
    private val toolbarBinding by lazy { ViewGuidelinesProductToolbarBinding.inflate(layoutInflater) }
    private val productNames by lazy {
        arrayOf(
            resources.getStringArray(R.array.guidelines_products)[0],
            resources.getStringArray(R.array.guidelines_products)[2],
            getString(R.string.text_field_demo_legacy)
        )
    }
    private var product = Product.POINKU
    private var productDialog: androidx.appcompat.app.AlertDialog? = null

    private fun setupToolbar() {
        toolbarBinding.tvTitle.setText(R.string.guidelines_otp)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayShowCustomEnabled(true)
            setCustomView(
                toolbarBinding.root,
                ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            )
        }
        toolbarBinding.tvProduct.setOnClickListener {
            if (productDialog?.isShowing == true) return@setOnClickListener
            productDialog = MaterialAlertDialogBuilder(this)
                .setTitle(R.string.guidelines_product)
                .setSingleChoiceItems(productNames, product.ordinal) { dialog, index ->
                    product = Product.entries[index]
                    showProduct()
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun showProduct() {
        val name = productNames[product.ordinal]
        toolbarBinding.tvProduct.text = name
        toolbarBinding.tvProduct.contentDescription = getString(R.string.guidelines_select_product, name)
        val legacy = setOf(R.id.ogDefault, R.id.ogError, R.id.btnTriggerError)
        binding.root.children.forEach { view ->
            view.isVisible = when (product) {
                Product.CORE -> view == binding.llCoreExamples
                Product.LEGACY -> view.id in legacy
                Product.POINKU -> view != binding.llCoreExamples && view.id !in legacy
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
        product = Product.entries.find { it.name == savedInstanceState?.getString(STATE_PRODUCT) } ?: Product.POINKU

        binding.ogError.isError = true
        if (savedInstanceState == null) {
            binding.ogPoinkuError.otp = "0000"
            binding.ogPoinkuDisabled.otp = "0000"
            binding.ogCoreError.otp = "0000"
            binding.ogCoreDisabled.otp = "0000"
        }
        binding.btnTriggerError.setOnClickListener { binding.ogError.isError = true }
        setupToolbar()
        showProduct()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_PRODUCT, product.name)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        productDialog?.dismiss()
        super.onDestroy()
    }

    private enum class Product { CORE, POINKU, LEGACY }

    private companion object {
        const val STATE_PRODUCT = "otp_product"
    }
}
