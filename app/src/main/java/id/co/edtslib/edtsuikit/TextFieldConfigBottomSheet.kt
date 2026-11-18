package id.co.edtslib.edtsuikit

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.core.widget.doAfterTextChanged
import id.co.edtslib.edtsuikit.databinding.BottomSheetTextFieldConfigBinding
import id.co.edtslib.uikit.tray.BottomSheetTray
import id.co.edtslib.uikit.utils.dimenPixelSize
import id.co.edtslib.uikit.utils.drawable
import id.co.edtslib.uikit.R as UIKitR

class TextFieldConfigBottomSheet : BottomSheetTray() {

    private var config = TextFieldConfig()
    private var product = TextFieldProduct.CORE
    private var sheetBinding: BottomSheetTextFieldConfigBinding? = null
    private var isBinding = false
    private var inputChoices = TextFieldInput.entries.toList()

    internal fun updatePreviewText(text: String) {
        config = config.copy(text = text)
        val editor = sheetBinding?.etValue ?: return
        if (editor.text?.toString() == text) return
        isBinding = true
        val selection = editor.selectionStart.coerceIn(0, text.length)
        editor.setText(text)
        editor.setSelection(selection)
        isBinding = false
    }

    private fun publish() {
        updateAvailableControls()
        parentFragmentManager.setFragmentResult(RESULT_CONFIG, Bundle().apply {
            putString(ARG_PRODUCT, product.name)
            putBundle(ARG_CONFIG, config.toBundle())
        })
    }

    private fun updateAvailableControls() {
        val controls = sheetBinding ?: return
        val hasPasswordToggle = product == TextFieldProduct.POINKU && config.input == TextFieldInput.PASSWORD
        val toggleOwnsSlot = hasPasswordToggle && config.isPasswordToggleEnabled ||
            product == TextFieldProduct.LEGACY && config.input == TextFieldInput.PASSWORD
        controls.tvPasswordToggle.isVisible = hasPasswordToggle
        controls.swPasswordToggle.isVisible = hasPasswordToggle
        controls.tvPasswordSlot.isVisible = toggleOwnsSlot
        controls.spEndIcon.isEnabled = !toggleOwnsSlot
        controls.swClearAction.isEnabled = !toggleOwnsSlot && config.endIcon != TextFieldIcon.NONE
        controls.tilActionDescription.isEnabled = !toggleOwnsSlot && config.isClearActionEnabled &&
            config.endIcon != TextFieldIcon.NONE
        controls.tilActionDescription.error = if (controls.tilActionDescription.isEnabled && config.actionDescription.isBlank()) {
            getString(R.string.text_field_config_description_required)
        } else {
            null
        }
    }

    private fun bindControls(controls: BottomSheetTextFieldConfigBinding) {
        isBinding = true
        controls.etLabel.setText(config.label)
        controls.etValue.setText(config.text)
        controls.etPlaceholder.setText(config.placeholder)
        controls.etSupporting.setText(config.supportingText)
        controls.etError.setText(config.errorText)
        controls.etMaxLength.setText(config.maxLength.toString())
        controls.etActionDescription.setText(config.actionDescription)
        controls.swEnabled.isChecked = config.isEnabled
        controls.swRequired.isChecked = config.isRequired
        controls.swCounter.isChecked = config.isCounterEnabled
        controls.swPasswordToggle.isChecked = config.isPasswordToggleEnabled
        controls.swClearAction.isChecked = config.isClearActionEnabled
        controls.spStartIcon.setSelection(config.startIcon.ordinal)
        controls.spEndIcon.setSelection(config.endIcon.ordinal)
        controls.spInput.setSelection(inputChoices.indexOf(config.input).coerceAtLeast(0))
        controls.spIme.setSelection(config.ime.ordinal)
        isBinding = false
        updateAvailableControls()
    }

    private fun setupIconSelector(spinner: Spinner, selected: TextFieldIcon, onSelected: (TextFieldIcon) -> Unit) {
        val icons = TextFieldIcon.entries
        spinner.adapter = object : ArrayAdapter<TextFieldIcon>(
            requireContext(), R.layout.item_text_field_icon_option, icons
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                return bindIcon(super.getView(position, convertView, parent), icons[position])
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                return bindIcon(super.getDropDownView(position, convertView, parent), icons[position])
            }

            private fun bindIcon(view: View, icon: TextFieldIcon): View {
                val textView = view as TextView
                textView.setText(icon.labelRes)
                val drawable = icon.drawableRes?.let { context.drawable(it).mutate() }
                val size = context.dimenPixelSize(UIKitR.dimen.dimen_24) ?: 0
                drawable?.setBounds(0, 0, size, size)
                textView.setCompoundDrawablesRelative(drawable, null, null, null)
                return view
            }
        }
        spinner.setSelection(selected.ordinal)
        onSelection(spinner) { onSelected(icons[it]) }
    }

    private fun onSelection(spinner: Spinner, update: (Int) -> Unit) {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isBinding) return
                update(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun setupListeners(controls: BottomSheetTextFieldConfigBinding) {
        controls.etLabel.doAfterTextChanged {
            if (isBinding) return@doAfterTextChanged
            config = config.copy(label = it.toString())
            publish()
        }
        controls.etValue.doAfterTextChanged {
            if (isBinding) return@doAfterTextChanged
            config = config.copy(text = it.toString())
            publish()
        }
        controls.etPlaceholder.doAfterTextChanged {
            if (isBinding) return@doAfterTextChanged
            config = config.copy(placeholder = it.toString())
            publish()
        }
        controls.etSupporting.doAfterTextChanged {
            if (isBinding) return@doAfterTextChanged
            config = config.copy(supportingText = it.toString())
            publish()
        }
        controls.etError.doAfterTextChanged {
            if (isBinding) return@doAfterTextChanged
            config = config.copy(errorText = it.toString())
            publish()
        }
        controls.etMaxLength.doAfterTextChanged {
            if (isBinding) return@doAfterTextChanged
            val raw = it.toString()
            val length = if (raw.isEmpty()) 0 else raw.toIntOrNull()
            controls.tilMaxLength.error = if (length == null || length < 0) {
                getString(R.string.text_field_config_invalid_length)
            } else {
                null
            }
            if (length != null && length >= 0) {
                config = config.copy(maxLength = length)
                publish()
            }
        }
        controls.etActionDescription.doAfterTextChanged {
            if (isBinding) return@doAfterTextChanged
            config = config.copy(actionDescription = it.toString())
            publish()
        }
        controls.swEnabled.setOnCheckedChangeListener { _, checked ->
            config = config.copy(isEnabled = checked)
            publish()
        }
        controls.swRequired.setOnCheckedChangeListener { _, checked ->
            config = config.copy(isRequired = checked)
            publish()
        }
        controls.swCounter.setOnCheckedChangeListener { _, checked ->
            config = config.copy(isCounterEnabled = checked)
            publish()
        }
        controls.swPasswordToggle.setOnCheckedChangeListener { _, checked ->
            config = config.copy(isPasswordToggleEnabled = checked)
            publish()
        }
        controls.swClearAction.setOnCheckedChangeListener { _, checked ->
            config = config.copy(isClearActionEnabled = checked)
            publish()
        }
        controls.btnResetConfig.setOnClickListener {
            config = TextFieldConfig()
            publish()
            dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = (savedInstanceState?.getBundle(ARG_CONFIG) ?: arguments?.getBundle(ARG_CONFIG))
            ?.let(TextFieldConfig::fromBundle) ?: TextFieldConfig()
        product = TextFieldProduct.entries.find { it.name == arguments?.getString(ARG_PRODUCT) } ?: TextFieldProduct.CORE
        inputChoices = TextFieldInput.entries.filter {
            it != TextFieldInput.PASSWORD || product in listOf(TextFieldProduct.POINKU, TextFieldProduct.LEGACY)
        }
        title = getString(R.string.configure_text_field)
        dragHandleVisibility = true
        titleDividerVisibility = true
        shouldShowClose = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val controls = BottomSheetTextFieldConfigBinding.inflate(inflater, null, false)
        sheetBinding = controls
        contentView = controls.root
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val controls = requireNotNull(sheetBinding)
        controls.spInput.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, inputChoices.map { getString(it.labelRes) }
        )
        controls.spIme.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, TextFieldIme.entries.map { getString(it.labelRes) }
        )
        bindControls(controls)
        setupIconSelector(controls.spStartIcon, config.startIcon) { icon ->
            if (icon == config.startIcon) return@setupIconSelector
            config = config.copy(startIcon = icon)
            publish()
        }
        setupIconSelector(controls.spEndIcon, config.endIcon) { icon ->
            if (icon == config.endIcon) return@setupIconSelector
            config = config.copy(endIcon = icon)
            publish()
        }
        onSelection(controls.spInput) { index ->
            val input = inputChoices[index]
            if (input != config.input) {
                config = config.copy(input = input)
                publish()
            }
        }
        onSelection(controls.spIme) { index ->
            val ime = TextFieldIme.entries[index]
            if (ime != config.ime) {
                config = config.copy(ime = ime)
                publish()
            }
        }
        setupListeners(controls)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
            )
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.updateLayoutParams {
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        binding.root.updateLayoutParams {
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        binding.flContent.updateLayoutParams<ConstraintLayout.LayoutParams> {
            height = 0
            bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        }
        dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.doOnLayout { sheet ->
            BottomSheetBehavior.from(sheet).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
            }
        }
        val controls = requireNotNull(sheetBinding)
        ViewCompat.setOnApplyWindowInsetsListener(controls.root) { view, insets ->
            view.updatePadding(bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom)
            insets
        }
        ViewCompat.requestApplyInsets(controls.root)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBundle(ARG_CONFIG, config.toBundle())
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        sheetBinding = null
        contentView = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "TEXT_FIELD_CONFIG_SHEET"
        const val RESULT_CONFIG = "TEXT_FIELD_CONFIG_RESULT"
        internal const val ARG_PRODUCT = "product"
        internal const val ARG_CONFIG = "config"

        internal fun newInstance(config: TextFieldConfig, product: TextFieldProduct) = TextFieldConfigBottomSheet().apply {
            arguments = Bundle().apply {
                putString(ARG_PRODUCT, product.name)
                putBundle(ARG_CONFIG, config.toBundle())
            }
        }
    }
}
