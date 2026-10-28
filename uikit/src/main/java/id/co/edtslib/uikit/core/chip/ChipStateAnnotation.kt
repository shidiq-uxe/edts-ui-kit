package id.co.edtslib.uikit.core.chip

import androidx.annotation.IntDef

const val CHIP_STATE_IDLE = 0
const val CHIP_STATE_SELECTED = 1
const val CHIP_STATE_ACTIVE = 2

@Retention(AnnotationRetention.SOURCE)
@IntDef(CHIP_STATE_IDLE, CHIP_STATE_SELECTED, CHIP_STATE_ACTIVE)
annotation class ChipStateAnnotation