package id.co.edtslib.edtsuikit

import android.graphics.Rect
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBar
import androidx.core.view.children
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout as MaterialTextInputLayout
import id.co.edtslib.edtsuikit.databinding.ActivityTextfieldBinding
import id.co.edtslib.edtsuikit.databinding.ViewGuidelinesProductToolbarBinding
import id.co.edtslib.uikit.R as UIKitR
import id.co.edtslib.uikit.core.textfield.CoreTextField
import id.co.edtslib.uikit.recipes.textfield.klik.KlikTextField
import id.co.edtslib.uikit.recipes.textfield.poinku.PoinkuTextField
import id.co.edtslib.uikit.textfield.TextField
import id.co.edtslib.uikit.textfield.TextFieldDelegate
import id.co.edtslib.uikit.utils.TextStyleKey
import id.co.edtslib.uikit.utils.TextStyleProvider.get
import id.co.edtslib.uikit.utils.buildHighlightedMessage
import id.co.edtslib.uikit.utils.dimenPixelSize
import id.co.edtslib.uikit.utils.drawable

class GuidelinesTextfieldActivity : GuidelinesBaseActivity() {

    override val hasConfigMenu = true

    private val binding by viewBinding<ActivityTextfieldBinding>()
    private val toolbarBinding by lazy { ViewGuidelinesProductToolbarBinding.inflate(layoutInflater) }
    private val productNames by lazy {
        resources.getStringArray(R.array.guidelines_products) + getString(R.string.text_field_demo_legacy)
    }

    private var product = TextFieldProduct.KLIK
    private val configurations = mutableMapOf<TextFieldProduct, TextFieldConfig>()
    private var preview: View? = null
    private var appliedConfig: TextFieldConfig? = null
    private var isApplyingConfig = false
    private var productDialog: androidx.appcompat.app.AlertDialog? = null
    private val config: TextFieldConfig
        get() = configurations[product] ?: TextFieldConfig()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_textfield)
        product = TextFieldProduct.entries.find { it.name == savedInstanceState?.getString(STATE_PRODUCT) }
            ?: TextFieldProduct.KLIK
        TextFieldProduct.entries.forEach { key ->
            savedInstanceState?.getBundle(key.name)?.let { configurations[key] = TextFieldConfig.fromBundle(it) }
        }
        supportFragmentManager.setFragmentResultListener(TextFieldConfigBottomSheet.RESULT_CONFIG, this) { _, result ->
            val key = TextFieldProduct.entries.find { it.name == result.getString(TextFieldConfigBottomSheet.ARG_PRODUCT) }
            val settings = result.getBundle(TextFieldConfigBottomSheet.ARG_CONFIG)?.let(TextFieldConfig::fromBundle)
            if (key != null && settings != null) {
                configurations[key] = settings
                if (key == product) applyPreviewConfig()
            }
        }

        configureLegacyExamples()
        setupToolbar()
        showExamples()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_PRODUCT, product.name)
        configurations.forEach { (key, settings) -> outState.putBundle(key.name, settings.toBundle()) }
    }

    private fun setupToolbar() {
        toolbarBinding.tvTitle.text = title
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayShowCustomEnabled(true)
            setCustomView(
                toolbarBinding.root,
                ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            )
        }
        toolbarBinding.root.doOnLayout { host ->
            host.touchDelegate = TouchDelegate(Rect(0, 0, host.width, host.height), toolbarBinding.tvProduct)
        }
        toolbarBinding.tvProduct.setOnClickListener {
            if (productDialog?.isShowing == true) return@setOnClickListener
            productDialog = MaterialAlertDialogBuilder(this)
                .setTitle(R.string.guidelines_product)
                .setSingleChoiceItems(productNames, product.ordinal) { dialog, selection ->
                    product = TextFieldProduct.entries[selection]
                    showExamples()
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun showExamples() {
        val productName = productNames[product.ordinal]
        toolbarBinding.tvProduct.text = productName
        toolbarBinding.tvProduct.contentDescription = getString(R.string.guidelines_select_product, productName)

        binding.llExamples.children.forEach {
            it.isVisible = when (it) {
                binding.tvPreview, binding.flPreview, binding.tvExamples -> true
                binding.llRecipes -> product != TextFieldProduct.LEGACY
                else -> product == TextFieldProduct.LEGACY
            }
        }
        createPreview()
        binding.llRecipes.removeAllViews()
        if (product == TextFieldProduct.LEGACY) return

        binding.llExamples.requestFocus()
        repeat(6) { index ->
            val example = newField().apply {
                val sampleText = getString(
                    if (product == TextFieldProduct.POINKU) R.string.text_field_demo_poinku_placeholder
                    else R.string.text_field_demo_klik_placeholder
                )
                label = getString(R.string.text_field_demo_label)
                placeholder = sampleText
                isFieldRequired = true
                supportingText = getString(R.string.text_field_demo_supporting)
                counterEnabled = true
                maxLength = 250
                startIcon = context.drawable(UIKitR.drawable.ic_placeholder_medium_24)
                endIcon = context.drawable(UIKitR.drawable.ic_placeholder_medium_24)
                if (index % 2 != 0) text = sampleText
                if (index in 2..3) errorText = supportingText
                if (index >= 4) isEnabled = false
            }
            addExample(example)
        }
        if (product == TextFieldProduct.POINKU) {
            addExample(newField().apply {
                label = getString(R.string.text_field_demo_password)
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                passwordToggleEnabled = true
                text = getString(R.string.text_field_demo_password_example)
            })
        }
    }

    private fun createPreview() {
        preview = if (product == TextFieldProduct.LEGACY) TextField(this) else newField()
        appliedConfig = null
        binding.flPreview.removeAllViews()
        val field = requireNotNull(preview)
        field.isSaveEnabled = false
        binding.flPreview.addView(field)
        when (field) {
            is CoreTextField -> field.setOnTextChangedListener(::onPreviewTextChanged)
            is TextField -> field.delegate = object : TextFieldDelegate {
                override fun onValueChange(value: String?) = onPreviewTextChanged(value.orEmpty())
            }
        }
        applyPreviewConfig()
    }

    private fun onPreviewTextChanged(text: String) {
        if (isApplyingConfig) return
        configurations[product] = config.copy(text = text)
        (supportFragmentManager.findFragmentByTag(TextFieldConfigBottomSheet.TAG) as? TextFieldConfigBottomSheet)
            ?.updatePreviewText(text)
    }

    private fun applyPreviewConfig() {
        val settings = config
        isApplyingConfig = true
        try {
            when (val field = preview) {
                is CoreTextField -> applyCoreConfig(field, settings)
                is TextField -> applyLegacyConfig(field, settings)
            }
        } finally {
            isApplyingConfig = false
        }
        val acceptedText = when (val field = preview) {
            is CoreTextField -> field.text.toString()
            is TextField -> field.editText?.text?.toString().orEmpty()
            else -> settings.text
        }
        configurations[product] = settings.copy(text = acceptedText)
        appliedConfig = configurations[product]
        (supportFragmentManager.findFragmentByTag(TextFieldConfigBottomSheet.TAG) as? TextFieldConfigBottomSheet)
            ?.updatePreviewText(acceptedText)
    }

    private fun applyCoreConfig(field: CoreTextField, settings: TextFieldConfig) {
        field.label = settings.label
        field.isFieldRequired = settings.isRequired
        field.placeholder = settings.placeholder
        field.supportingText = settings.supportingText
        field.errorText = settings.errorText
        field.counterEnabled = settings.isCounterEnabled
        if (appliedConfig?.input != settings.input) field.inputType = settings.input.androidType
        field.imeOptions = settings.ime.androidAction
        if (appliedConfig?.maxLength != settings.maxLength) field.maxLength = settings.maxLength
        field.startIcon = settings.startIcon.drawableRes?.let { drawable(it) }
        field.endIcon = settings.endIcon.drawableRes?.let { drawable(it) }
        field.passwordToggleEnabled = product == TextFieldProduct.POINKU && settings.isPasswordToggleEnabled
        val hasAction = settings.isClearActionEnabled && settings.actionDescription.isNotBlank()
        field.setEndIconAction(settings.actionDescription, if (hasAction) ({ field.text = "" }) else null)
        if (field.text.toString() != settings.text) field.text = settings.text
        field.isEnabled = settings.isEnabled
    }

    private fun applyLegacyConfig(field: TextField, settings: TextFieldConfig) {
        field.isFieldRequired = settings.isRequired
        field.hint = if (settings.isRequired && settings.label.isNotEmpty()) {
            TextUtils.concat(
                settings.label,
                " ",
                buildHighlightedMessage(
                    context = this,
                    message = "*",
                    highlightedMessages = listOf("*"),
                    highlightedTextAppearance = listOf(TextStyleKey.H3_SEMIBOLD.get(this, UIKitR.color.red_30))
                )
            )
        } else {
            settings.label
        }
        field.placeholderText = settings.placeholder
        if (appliedConfig?.input != settings.input) {
            field.inputType = when (settings.input) {
                TextFieldInput.EMAIL -> TextField.InputType.Email
                TextFieldInput.PHONE -> TextField.InputType.Phone
                TextFieldInput.PASSWORD -> TextField.InputType.Password
                TextFieldInput.TEXT, TextFieldInput.NUMBER -> TextField.InputType.Text
            }
            if (settings.input == TextFieldInput.NUMBER) field.editText?.inputType = settings.input.androidType
        }
        field.imeOption = when (settings.ime) {
            TextFieldIme.DONE -> TextField.ImeOption.Done
            TextFieldIme.SEARCH -> TextField.ImeOption.Search
            TextFieldIme.SEND -> TextField.ImeOption.Send
            TextFieldIme.NEXT -> TextField.ImeOption.Next
        }
        if (appliedConfig?.maxLength != settings.maxLength) {
            field.maxLength = 0
            field.maxLength = settings.maxLength
        }
        field.isCounterEnabled = settings.isCounterEnabled && settings.maxLength > 0
        field.counterMaxLength = settings.maxLength
        field.startIconDrawable = settings.startIcon.drawableRes?.let { drawable(it) }
        if (settings.input != TextFieldInput.PASSWORD) {
            field.endIconMode = if (settings.endIcon == TextFieldIcon.NONE) MaterialTextInputLayout.END_ICON_NONE else MaterialTextInputLayout.END_ICON_CUSTOM
            field.endIconDrawable = settings.endIcon.drawableRes?.let { drawable(it) }
            field.endIconContentDescription = settings.actionDescription
            field.setEndIconOnClickListener(
                if (settings.isClearActionEnabled && settings.actionDescription.isNotBlank()) {
                    View.OnClickListener { field.editText?.setText("") }
                } else {
                    null
                }
            )
        }
        if (appliedConfig?.errorText != settings.errorText) field.error = settings.errorText.takeIf { it.isNotEmpty() }
        if (settings.errorText.isEmpty()) field.helperText = settings.supportingText
        val exceedsLimit = settings.maxLength > 0 && (field.editText?.length() ?: 0) > settings.maxLength
        if (field.editText?.text?.toString() != settings.text || exceedsLimit) field.editText?.setText(settings.text)
        field.isEnabled = settings.isEnabled
    }

    override fun onConfigMenuClicked() {
        if (supportFragmentManager.findFragmentByTag(TextFieldConfigBottomSheet.TAG) != null) return
        TextFieldConfigBottomSheet.newInstance(config, product).show(supportFragmentManager, TextFieldConfigBottomSheet.TAG)
    }

    override fun onDestroy() {
        productDialog?.dismiss()
        super.onDestroy()
    }

    private fun newField(): CoreTextField {
        return when (product) {
            TextFieldProduct.KLIK -> KlikTextField(this)
            TextFieldProduct.POINKU -> PoinkuTextField(this)
            TextFieldProduct.CORE, TextFieldProduct.LEGACY -> CoreTextField(this)
        }
    }

    private fun addExample(field: CoreTextField) {
        binding.llRecipes.addView(
            field,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dimenPixelSize(UIKitR.dimen.s) ?: 0
            }
        )
    }

    private fun configureLegacyExamples() {
        binding.tilLabelInsideErrorWStartEndIcon.error = "Supporting Text"
        binding.tilLabelInsideErrorHelperCounter.error = "Supporting Text"
        binding.tilLabelInsideLoading.isLoading = true

        binding.tilLabelInside.apply {
            delegate = object : TextFieldDelegate {
                override fun onValueChange(value: String?) {
                    Log.e("Test", "Value $value")
                }
            }
            inputType = TextField.InputType.Email
            imeOption = TextField.ImeOption.Done
            maxLength = 50
        }
    }

    private companion object {
        const val STATE_PRODUCT = "product"
    }
}
