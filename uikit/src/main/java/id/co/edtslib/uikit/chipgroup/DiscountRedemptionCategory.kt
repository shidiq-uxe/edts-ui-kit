package id.co.edtslib.uikit.chipgroup

import id.co.edtslib.uikit.chip.DiscountRedemptionChipStateAnnotation

data class DiscountRedemptionCategory(
    var id: String,
    var categoryName: String,
    @DiscountRedemptionChipStateAnnotation var state: Int,
)
