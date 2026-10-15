package id.co.edtslib.edtsuikit.tmh

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.core.widget.TextViewCompat
import id.co.edtslib.uikit.utils.dp


// TODO: Consolidate TMH text appearances with shared Inter typography after the deferred font review.
fun View.tmhTypography() {
    if (this is TextView) includeFontPadding = false
    if (this is ViewGroup) {
        for (index in 0 until childCount) getChildAt(index).tmhTypography()
    }
}

/** Font changes must retain the button's enabled/disabled color state list. */
fun TextView.tmhTextAppearance(@StyleRes appearance: Int, lineHeightDp: Int) {
    val colors = textColors
    setTextAppearance(appearance)
    setTextColor(colors)
    includeFontPadding = false
    TextViewCompat.setLineHeight(this, lineHeightDp.dp.toInt())
}
