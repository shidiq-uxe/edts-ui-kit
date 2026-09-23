package id.co.edtslib.uikit.core.chip

import id.co.edtslib.uikit.R

abstract class ChipVariant {
    abstract val preset: ChipPreset
    abstract val idlePalette: ChipPalette
    open val activePalette: ChipPalette? = null
    open val selectedPalette: ChipPalette? = null

    fun paletteFor(@ChipStateAnnotation state: Int): ChipPalette = when (state) {
        CHIP_STATE_ACTIVE -> activePalette ?: idlePalette
        CHIP_STATE_SELECTED -> selectedPalette ?: idlePalette
        else -> idlePalette
    }
}

object CoreChipVariant : ChipVariant() {
    override val preset = ChipPreset()

    override val idlePalette = ChipPalette(
        backgroundColorRes = R.color.foundation_color_grey_20,
        textColorRes = R.color.foundation_color_primary_blue_30,
        startIconBackgroundColorRes = R.color.foundation_color_grey_20,
        endIconBackgroundColorRes = R.color.foundation_color_grey_20,
        isStartIconBackgroundVisible = true,
        isEndIconBackgroundVisible = true
    )
}