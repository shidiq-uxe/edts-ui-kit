package id.co.edtslib.uikit.chip

import android.view.View

interface DiscountRedemptionChipDelegate {
     fun onClick(view: View, @DiscountRedemptionChipStateAnnotation state: Int)
}