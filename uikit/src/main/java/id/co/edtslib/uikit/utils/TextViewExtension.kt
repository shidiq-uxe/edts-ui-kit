package id.co.edtslib.uikit.utils

import android.graphics.drawable.Drawable
import android.widget.TextView

/**
 * Set Desired Image into TextSpan Position of [Drawable] type
 */
fun TextView.setDrawable(
    drawableTop: Drawable? = null,
    drawableLeft: Drawable? = null,
    drawableBottom: Drawable? = null,
    drawableRight: Drawable? = null
) {
    setCompoundDrawablesWithIntrinsicBounds(
        drawableLeft, drawableTop, drawableRight, drawableBottom
    )
}

fun TextView.lineHeight(lineHeight: Int) {
    val fontHeight = paint.getFontMetricsInt(null)
    setLineSpacing(lineHeight.dp - fontHeight, 1f)

}