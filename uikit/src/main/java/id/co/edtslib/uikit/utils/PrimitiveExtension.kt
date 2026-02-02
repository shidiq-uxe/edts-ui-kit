package id.co.edtslib.uikit.utils

import android.annotation.SuppressLint
import android.os.Build
import android.text.Html
import android.text.Spanned
import java.text.NumberFormat
import java.util.Locale

fun String.htmlToString(@SuppressLint("InlinedApi") mode: Int = Html.FROM_HTML_MODE_LEGACY): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, mode)
    } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(this)
    }
}

fun Number.toCurrency(
    locale: Locale = Locale("id", "ID"),
    showDecimals: Boolean = false
): String {
    val formatter = NumberFormat.getCurrencyInstance(locale).apply {
        minimumFractionDigits = if (showDecimals) 2 else 0
        maximumFractionDigits = if (showDecimals) 2 else 0
    }
    return formatter.format(this)
}