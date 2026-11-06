package id.co.edtslib.edtsuikit

import android.os.Bundle

internal data class AlertBoxConfig(
    val message: String = "",
    val isHtml: Boolean = false,
    val showStartIcon: Boolean = true,
    val showClose: Boolean = true,
    val showButton: Boolean = false,
    val showAction: Boolean = false,
    val disabled: Boolean = false,
    val iconAlignment: Int = 0
) {
    fun toBundle(): Bundle = Bundle().apply {
        putString("message", message)
        putBoolean("html", isHtml)
        putBoolean("start", showStartIcon)
        putBoolean("close", showClose)
        putBoolean("button", showButton)
        putBoolean("action", showAction)
        putBoolean("disabled", disabled)
        putInt("alignment", iconAlignment)
    }

    companion object {
        fun fromBundle(bundle: Bundle) = AlertBoxConfig(
            message = bundle.getString("message").orEmpty(),
            isHtml = bundle.getBoolean("html"),
            showStartIcon = bundle.getBoolean("start", true),
            showClose = bundle.getBoolean("close", true),
            showButton = bundle.getBoolean("button"),
            showAction = bundle.getBoolean("action"),
            disabled = bundle.getBoolean("disabled"),
            iconAlignment = bundle.getInt("alignment").coerceIn(0, 2)
        )
    }
}
