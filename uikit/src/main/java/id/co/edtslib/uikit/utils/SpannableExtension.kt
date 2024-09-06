package id.co.edtslib.uikit.utils

import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.text.inSpans

fun SpannableStringBuilder.noUnderlineClick(onClick: (View) -> Unit, builderAction: SpannableStringBuilder.() -> Unit) {
    inSpans(object : ClickableSpan() {
        override fun onClick(widget: View) {
            onClick(widget)
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = false
        }
    }) {
        builderAction.invoke(this)
    }
}

fun SpannableStringBuilder.click(onClick: (View) -> Unit, builderAction: SpannableStringBuilder.() -> Unit) {
    inSpans(object : ClickableSpan() {
        override fun onClick(widget: View) {
            onClick(widget)
        }
    }) {
        builderAction.invoke(this)
    }
}