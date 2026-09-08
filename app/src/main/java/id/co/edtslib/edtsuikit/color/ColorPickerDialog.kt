package id.co.edtslib.edtsuikit.color

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.InputFilter
import android.text.InputType
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import id.co.edtslib.edtsuikit.R
import id.co.edtslib.edtsuikit.databinding.DialogColorPickerBinding
import id.co.edtslib.uikit.utils.asColor
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.inflater
import java.util.Locale

class ColorPickerDialog private constructor() {

    companion object {
        fun show(
            context: android.content.Context,
            title: String,
            selectedColor: Int?,
            onColorPicked: (Int) -> Unit,
        ) {
            val currentColor = selectedColor ?: "#1178D4".asColor
            val binding = DialogColorPickerBinding.inflate(context.inflater)
            var isUpdatingHex = false

            binding.picker.setColor(currentColor)
            binding.picker.contentDescription = context.getString(R.string.color_picker_description)
            binding.hexInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            binding.hexInput.filters = arrayOf(InputFilter.LengthFilter(9))
            binding.hexInput.setText(formatHex(currentColor))
            binding.hexInput.compoundDrawablePadding = 8.dp.toInt()

            fun updatePreview(color: Int) {
                val colorSwatch = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 6.dp
                    setColor(color)
                    setBounds(0, 0, 32.dp.toInt(), 32.dp.toInt())
                }
                binding.hexInput.setCompoundDrawablesRelative(colorSwatch, null, null, null)
            }

            fun updateHex(color: Int) {
                val value = formatHex(color)
                if (binding.hexInput.text?.toString() == value) return
                isUpdatingHex = true
                binding.hexInput.setText(value)
                binding.hexInput.setSelection(binding.hexInput.length())
                isUpdatingHex = false
            }

            fun parseHex(value: String): Int? {
                val normalized = value.trim().let { if (it.startsWith("#")) it else "#$it" }
                if (!normalized.matches(HEX_COLOR_PATTERN)) return null
                return runCatching { Color.parseColor(normalized) }.getOrNull()
            }

            fun applyHexInput(): Int? {
                val color = parseHex(binding.hexInput.text?.toString().orEmpty())
                binding.hexInputLayout.error = if (color == null) {
                    context.getString(R.string.color_hex_error)
                } else {
                    null
                }
                color?.let {
                    binding.picker.setColor(it)
                    updatePreview(it)
                }
                return color
            }

            updatePreview(currentColor)
            binding.picker.onColorChanged = { color ->
                updatePreview(color)
                updateHex(color)
                binding.hexInputLayout.error = null
            }
            binding.hexInput.doAfterTextChanged { editable ->
                if (isUpdatingHex) return@doAfterTextChanged
                val value = editable?.toString().orEmpty()
                if (value.isBlank()) {
                    binding.hexInputLayout.error = null
                    return@doAfterTextChanged
                }
                val color = parseHex(value)
                binding.hexInputLayout.error = if (color == null) {
                    context.getString(R.string.color_hex_error)
                } else {
                    null
                }
                color?.let {
                    binding.picker.setColor(it)
                    updatePreview(it)
                }
            }

            val dialog = MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setView(binding.root)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .create()

            dialog.setOnShowListener {
                dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener {
                    val color = applyHexInput() ?: return@setOnClickListener
                    onColorPicked(color)
                    dialog.dismiss()
                }
            }
            dialog.show()
        }

        private val HEX_COLOR_PATTERN = Regex("^#[0-9A-Fa-f]{6}(?:[0-9A-Fa-f]{2})?$")

        private fun formatHex(color: Int): String =
            String.format(Locale.ROOT, "#%06X", 0xFFFFFF and color)
    }
}
