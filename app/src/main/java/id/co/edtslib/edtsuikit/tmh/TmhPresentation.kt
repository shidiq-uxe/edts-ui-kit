package id.co.edtslib.edtsuikit.tmh

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.widget.ImageView
import id.co.edtslib.edtsuikit.R
import id.co.edtslib.uikit.alert.AlertBox
import id.co.edtslib.uikit.utils.TextStyleKey
import id.co.edtslib.uikit.utils.TextStyleProvider.get
import id.co.edtslib.uikit.utils.buildHighlightedMessage
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.drawable
import id.co.edtslib.uikit.R as UIKitR

fun TmhFair.alertText(context: Context): CharSequence = buildHighlightedMessage(
    context = context,
    htmlMessage = when (state) {
        TmhFairState.ELIGIBLE -> context.getString(R.string.tmh_available)
        TmhFairState.PARTIALLY_SELECTED, TmhFairState.FULLY_SELECTED ->
            context.getString(R.string.tmh_selected, savedTotal)
        TmhFairState.REQUIREMENT_UNMET -> context.getString(R.string.tmh_requirement)
        TmhFairState.OUT_OF_STOCK -> context.getString(R.string.tmh_stock_message)
        TmhFairState.QUOTA_EXHAUSTED -> context.getString(R.string.tmh_quota_message)
        TmhFairState.LIMIT_EXHAUSTED -> context.getString(R.string.tmh_limit_message)
        TmhFairState.FIRST_TRANSACTION_ONLY -> context.getString(R.string.tmh_first_transaction_message)
    },
    highlightedTextAppearance = TextStyleKey.P2_SEMIBOLD.get(context, UIKitR.color.black_70),
)

fun TmhFair.alertBackgroundRes(): Int = when {
    state == TmhFairState.FULLY_SELECTED -> R.color.tmh_selected_background
    state.isUnavailable -> UIKitR.color.black_20
    else -> UIKitR.color.support_selected
}

fun TmhFair.actionColorRes(): Int =
    if (state == TmhFairState.FULLY_SELECTED) UIKitR.color.green_50 else UIKitR.color.primary_30

fun AlertBox.bindFair(fair: TmhFair) {
    alertType = AlertBox.AlertType.LIGHT
    text = fair.alertText(context)
    strokeWidth = 0f
    alertBackgroundColor = context.color(fair.alertBackgroundRes())
    cornerRadius = 6.dp
    textAppearance = R.style.TextAppearance_Tmh_Regular_P2
    startIconVerticalAlignment = AlertBox.StartIconVerticalAlignment.CENTER
    isCloseIconVisible = false
    isButtonVisible = false
    actionText = context.getString(when {
        fair.state == TmhFairState.REQUIREMENT_UNMET -> R.string.tmh_add
        fair.state.isUnavailable -> R.string.tmh_terms
        fair.savedTotal > 0 -> R.string.tmh_edit
        else -> R.string.tmh_choose
    })
    actionTextAppearance = R.style.TextAppearance_Tmh_SemiBold_Button_Text_Small
    actionTextColor = context.color(fair.actionColorRes())
    actionEndIcon = context.drawable(UIKitR.drawable.ic_chevron_right_16)
    actionEndIconTint = actionTextColor
    isActionVisible = true
    tmhTypography()
    startIcon = context.drawable(
        if (fair.state == TmhFairState.FULLY_SELECTED) UIKitR.drawable.ic_success_16
        else if (fair.state.isEligible) R.drawable.tmh_discount_icon
        else UIKitR.drawable.ic_info_16
    )
    startIconTint = context.color(
        if (fair.state.isUnavailable) UIKitR.color.black_60 else fair.actionColorRes()
    )
}

fun ImageView.setUnavailable(isUnavailable: Boolean) {
    colorFilter = if (isUnavailable) ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) }) else null
}

fun originalPriceFlags(): Int = Paint.ANTI_ALIAS_FLAG or Paint.STRIKE_THRU_TEXT_FLAG
