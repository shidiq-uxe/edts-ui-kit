package id.co.edtslib.uikit.utils

import android.annotation.SuppressLint
import android.os.Build
import android.text.Html
import android.text.Spanned

fun String.htmlToString(@SuppressLint("InlinedApi") mode: Int = Html.FROM_HTML_MODE_LEGACY): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, mode)
    } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(this)
    }
}