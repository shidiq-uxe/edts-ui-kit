package id.co.edtslib.uikit.chip

import androidx.annotation.IntDef
import id.co.edtslib.uikit.chip.DiscountRedemptionChip.Companion.STATE_INACTIVE
import id.co.edtslib.uikit.chip.DiscountRedemptionChip.Companion.STATE_CHECKED
import id.co.edtslib.uikit.chip.DiscountRedemptionChip.Companion.STATE_DEFAULT

@Retention(AnnotationRetention.SOURCE)
@IntDef(STATE_INACTIVE, STATE_CHECKED, STATE_DEFAULT)
annotation class DiscountRedemptionChipStateAnnotation