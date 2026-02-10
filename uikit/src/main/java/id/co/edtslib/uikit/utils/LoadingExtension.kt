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
import android.app.Activity
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.graphics.toColorInt
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.RelativeCornerSize

fun Context.getProgressIndicatorDrawable(
    @ColorRes color: Int = R.color.primary_30,
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
    @ColorRes color: Int = R.color.primary_30,
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

private const val LOADING_VIEW_ID = 999101
private const val ANIMATION_DURATION = 300L

fun Activity.showLoading() {
    val rootLayout = findViewById<ViewGroup>(android.R.id.content)
    if (rootLayout.findViewById<View>(LOADING_VIEW_ID) != null) return

    val container = FrameLayout(this).apply {
        id = LOADING_VIEW_ID
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        setBackgroundColor("#80000000".toColorInt())
        isClickable = true
        isFocusable = true
        alpha = 0f
    }

    val loadingContainer = MaterialCardView(this).apply {
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }

        setCardBackgroundColor(Color.WHITE)
        shapeAppearanceModel = shapeAppearanceModel.toBuilder()
            .setAllCornerSizes(RelativeCornerSize(0.5f))
            .build()
    }

    val progressBar = ProgressBar(this).apply {
        val margin = 8.dp.toInt()
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            setMargins(margin, margin, margin, margin)
        }
    }

    loadingContainer.addView(progressBar)
    container.addView(loadingContainer)
    rootLayout.addView(container)

    container.animate()
        .alpha(1f)
        .setDuration(ANIMATION_DURATION)
        .start()
}

fun Activity.hideLoading() {
    val rootLayout = findViewById<ViewGroup>(android.R.id.content)
    val loadingView = rootLayout.findViewById<View>(LOADING_VIEW_ID) ?: return

    loadingView.animate()
        .alpha(0f)
        .setDuration(ANIMATION_DURATION)
        .withEndAction {
            rootLayout.removeView(loadingView)
        }
        .start()
}