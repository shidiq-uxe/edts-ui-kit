package id.co.edtslib.uikit.core.chip

import androidx.annotation.StyleRes

data class ChipPreset(
    val widthPercent: Double? = null,
    val cornerRadius: Float? = 99f,
    val borderWidth: Int = 0,
    val startIconSizeDp: Int? = null,
    val endIconSizeDp: Int? = null,
    @StyleRes val textAppearance: Int? = null,
    val horizontalPaddingDp: Int? = null,
    val verticalPaddingDp: Int? = null
)