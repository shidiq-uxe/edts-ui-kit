package id.co.edtslib.uikit.recipes.chip.poinku

import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.core.chip.ChipPalette
import id.co.edtslib.uikit.core.chip.ChipPreset
import id.co.edtslib.uikit.core.chip.ChipVariant

object PoinkuChipVariant : ChipVariant() {
    override val preset = ChipPreset(
        textAppearance = R.style.TextAppearance_Poinku_Body_B3_Light,
        borderWidth = 1,
        verticalPaddingDp = 4,
        horizontalPaddingDp = 8
    )

    override val idlePalette = ChipPalette(
        backgroundColorRes = R.color.poinku_color_grey_20,
        textColorRes = R.color.poinku_color_grey_80,
        startIconColorRes = R.color.poinku_color_grey_60,
        endIconColorRes = R.color.poinku_color_grey_60,
        borderColorRes = R.color.poinku_color_grey_20,
        startIconBackgroundColorRes = R.color.transparent,
        endIconBackgroundColorRes = R.color.transparent,
        isStartIconBackgroundVisible = false,
        isEndIconBackgroundVisible = false
    )

    override val activePalette = ChipPalette(
        backgroundColorRes = R.color.poinku_color_support_highlight_primary_weak,
        textColorRes = R.color.poinku_color_primary_blue_30,
        startIconColorRes = R.color.poinku_color_primary_blue_30,
        endIconColorRes = R.color.poinku_color_primary_blue_30,
        borderColorRes = R.color.poinku_color_primary_blue_30,
        startIconBackgroundColorRes = R.color.transparent,
        endIconBackgroundColorRes = R.color.transparent,
        isStartIconBackgroundVisible = false,
        isEndIconBackgroundVisible = false
    )

    override val selectedPalette = ChipPalette(
        backgroundColorRes = R.color.poinku_color_primary_blue_30,
        textColorRes = R.color.poinku_color_neutral_white,
        startIconColorRes = R.color.poinku_color_neutral_white,
        endIconColorRes = R.color.poinku_color_neutral_white,
        borderColorRes = R.color.poinku_color_primary_blue_40,
        startIconBackgroundColorRes = R.color.transparent,
        endIconBackgroundColorRes = R.color.transparent,
        isStartIconBackgroundVisible = false,
        isEndIconBackgroundVisible = false
    )
}