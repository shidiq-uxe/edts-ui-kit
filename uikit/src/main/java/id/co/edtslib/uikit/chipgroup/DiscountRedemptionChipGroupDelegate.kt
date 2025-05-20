package id.co.edtslib.uikit.chipgroup

import android.view.View

interface DiscountRedemptionChipGroupDelegate {
    fun onItemSelected(
        position: Int,
        view: View,
        item: DiscountRedemptionCategory,
    )
}