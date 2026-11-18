package id.co.edtslib.edtsuikit

import android.os.Bundle
import android.text.InputType
import android.view.inputmethod.EditorInfo
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import id.co.edtslib.uikit.R as UIKitR

internal data class TextFieldConfig(
    val label: String = "Label",
    val text: String = "",
    val placeholder: String = "Placeholder text",
    val supportingText: String = "Supporting text",
    val errorText: String = "",
    val isEnabled: Boolean = true,
    val isRequired: Boolean = true,
    val isCounterEnabled: Boolean = true,
    val maxLength: Int = 250,
    val startIcon: TextFieldIcon = TextFieldIcon.PLACEHOLDER,
    val endIcon: TextFieldIcon = TextFieldIcon.PLACEHOLDER,
    val input: TextFieldInput = TextFieldInput.TEXT,
    val ime: TextFieldIme = TextFieldIme.NEXT,
    val isPasswordToggleEnabled: Boolean = true,
    val isClearActionEnabled: Boolean = false,
    val actionDescription: String = "Clear text"
) {
    fun toBundle(): Bundle = Bundle().apply {
        putString("label", label)
        putString("text", text)
        putString("placeholder", placeholder)
        putString("supporting", supportingText)
        putString("error", errorText)
        putBoolean("enabled", isEnabled)
        putBoolean("required", isRequired)
        putBoolean("counter", isCounterEnabled)
        putInt("maxLength", maxLength)
        putString("startIcon", startIcon.name)
        putString("endIcon", endIcon.name)
        putString("input", input.name)
        putString("ime", ime.name)
        putBoolean("passwordToggle", isPasswordToggleEnabled)
        putBoolean("clearAction", isClearActionEnabled)
        putString("actionDescription", actionDescription)
    }

    companion object {
        fun fromBundle(bundle: Bundle): TextFieldConfig {
            val defaults = TextFieldConfig()
            return defaults.copy(
                label = bundle.getString("label", defaults.label),
                text = bundle.getString("text", defaults.text),
                placeholder = bundle.getString("placeholder", defaults.placeholder),
                supportingText = bundle.getString("supporting", defaults.supportingText),
                errorText = bundle.getString("error", defaults.errorText),
                isEnabled = bundle.getBoolean("enabled", defaults.isEnabled),
                isRequired = bundle.getBoolean("required", defaults.isRequired),
                isCounterEnabled = bundle.getBoolean("counter", defaults.isCounterEnabled),
                maxLength = bundle.getInt("maxLength", defaults.maxLength).coerceAtLeast(0),
                startIcon = TextFieldIcon.entries.find { it.name == bundle.getString("startIcon") } ?: defaults.startIcon,
                endIcon = TextFieldIcon.entries.find { it.name == bundle.getString("endIcon") } ?: defaults.endIcon,
                input = TextFieldInput.entries.find { it.name == bundle.getString("input") } ?: defaults.input,
                ime = TextFieldIme.entries.find { it.name == bundle.getString("ime") } ?: defaults.ime,
                isPasswordToggleEnabled = bundle.getBoolean("passwordToggle", defaults.isPasswordToggleEnabled),
                isClearActionEnabled = bundle.getBoolean("clearAction", defaults.isClearActionEnabled),
                actionDescription = bundle.getString("actionDescription", defaults.actionDescription)
            )
        }
    }
}

internal enum class TextFieldProduct { CORE, KLIK, POINKU, LEGACY }

internal enum class TextFieldIcon(@StringRes val labelRes: Int, @DrawableRes val drawableRes: Int?) {
    NONE(R.string.text_field_config_none, null),
    PLACEHOLDER(R.string.text_field_config_placeholder_icon, UIKitR.drawable.ic_placeholder_medium_24),
    INFO(R.string.text_field_config_info_icon, UIKitR.drawable.ic_info_16),
    CHECK(R.string.text_field_config_check_icon, UIKitR.drawable.ic_check),
    CLOSE(R.string.text_field_config_close_icon, UIKitR.drawable.ic_close_16)
}

internal enum class TextFieldInput(@StringRes val labelRes: Int, val androidType: Int) {
    TEXT(R.string.text_field_config_input_text, InputType.TYPE_CLASS_TEXT),
    EMAIL(R.string.text_field_config_input_email, InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS),
    PHONE(R.string.text_field_config_input_phone, InputType.TYPE_CLASS_PHONE),
    NUMBER(R.string.text_field_config_input_number, InputType.TYPE_CLASS_NUMBER),
    PASSWORD(R.string.text_field_config_input_password, InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
}

internal enum class TextFieldIme(@StringRes val labelRes: Int, val androidAction: Int) {
    NEXT(R.string.text_field_config_ime_next, EditorInfo.IME_ACTION_NEXT),
    DONE(R.string.text_field_config_ime_done, EditorInfo.IME_ACTION_DONE),
    SEARCH(R.string.text_field_config_ime_search, EditorInfo.IME_ACTION_SEARCH),
    SEND(R.string.text_field_config_ime_send, EditorInfo.IME_ACTION_SEND)
}
