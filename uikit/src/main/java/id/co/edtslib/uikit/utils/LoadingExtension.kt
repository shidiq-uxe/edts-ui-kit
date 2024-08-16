package id.co.edtslib.uikit.utils

import android.content.Context
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec
import com.google.android.material.progressindicator.IndeterminateDrawable
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.progressview.bindProgressButton
import id.co.edtslib.uikit.progressview.showProgress

fun Context.getProgressIndicatorDrawable(
    @ColorRes color: Int = R.color.primary_50,
    @StyleRes style: Int = R.style.Widget_EDTS_UIKit_CircularProgressIndicator_Small,
): IndeterminateDrawable<CircularProgressIndicatorSpec> {
    val progressIndicatorSpec = CircularProgressIndicatorSpec(
        this, null, 0, style
    )

    progressIndicatorSpec.indicatorColors = intArrayOf(color(color))

    return IndeterminateDrawable.createCircularDrawable(this, progressIndicatorSpec).apply {
        setVisible(isVisible, isVisible)
    }
}

fun Context.getProgressIndicatorDrawableLarge(
    @ColorRes color: Int = R.color.primary_50,
    @StyleRes style: Int = R.style.Widget_EDTS_UIKit_CircularProgressIndicator_Large,
): IndeterminateDrawable<CircularProgressIndicatorSpec> =
    getProgressIndicatorDrawable(color, style)

fun LifecycleOwner.bindProgressButtons(
    vararg buttons: TextView,
) {
    buttons.forEach {
        bindProgressButton(it)
    }
}

fun TextView.showProgress(
    @AttrRes colorAttr: Int? = null,
    @ColorRes color: Int? = null,
) {
    isClickable = false

    showProgress {
        progressStrokeRes = R.dimen.dimen_2

        if (colorAttr != null) {
            progressColor = context.colorAttr(colorAttr)
        } else if (color != null) {
            progressColor = context.color(color)
        } else {
            progressColor = context.run {
                when (tag) {
                    getString(R.string.button_filled_tag) ->
                        colorAttr(com.google.android.material.R.attr.colorOnPrimary)

                    getString(R.string.button_outlined_tag),
                    getString(R.string.button_text_button_tag),
                    getString(R.string.button_icon_button_tag),
                    ->
                        colorAttr(com.google.android.material.R.attr.colorPrimary)

                    else ->
                        colorAttr(com.google.android.material.R.attr.colorPrimary)
                }
            }
        }
    }
}

fun TextView.hideProgress(newText: String? = null) {
    isClickable = true
    hideProgress(newText)
}

fun TextView.hideProgress(@StringRes newTextRes: Int) {
    isClickable = true
    hideProgress(newTextRes)
}