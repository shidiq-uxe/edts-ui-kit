package id.co.edtslib.uikit.recipes.chip.klik

import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.core.chip.ChipPalette
import id.co.edtslib.uikit.core.chip.ChipPreset
import id.co.edtslib.uikit.core.chip.ChipVariant

sealed class KlikChipVariant : ChipVariant() {

    object Suggestion : KlikChipVariant() {
        override val preset = ChipPreset(
            verticalPaddingDp = 4,
            horizontalPaddingDp = 8,
        )

        override val idlePalette = ChipPalette(
            backgroundColorRes = R.color.klik_color_grey_20,
            textColorRes = R.color.klik_color_blue_50,
            startIconColorRes = R.color.klik_color_blue_50,
            endIconColorRes = R.color.klik_color_blue_50,
            startIconBackgroundColorRes = R.color.klik_color_neutral_white,
            endIconBackgroundColorRes = R.color.klik_color_neutral_white,
            isStartIconBackgroundVisible = false,
            isEndIconBackgroundVisible = false
        )

        override val activePalette = ChipPalette(
            backgroundColorRes = R.color.klik_color_blue_50,
            textColorRes = R.color.klik_color_neutral_white,
            startIconColorRes = R.color.klik_color_neutral_white,
            endIconColorRes = R.color.klik_color_neutral_white,
            startIconBackgroundColorRes = R.color.klik_color_neutral_white,
            endIconBackgroundColorRes = R.color.klik_color_neutral_white,
            isStartIconBackgroundVisible = false,
            isEndIconBackgroundVisible = false
        )

        override val selectedPalette = ChipPalette(
            backgroundColorRes = R.color.klik_color_blue_50,
            textColorRes = R.color.klik_color_neutral_white,
            startIconColorRes = R.color.klik_color_neutral_white,
            endIconColorRes = R.color.klik_color_neutral_white,
            startIconBackgroundColorRes = R.color.klik_color_neutral_white,
            endIconBackgroundColorRes = R.color.klik_color_neutral_white,
            isStartIconBackgroundVisible = false,
            isEndIconBackgroundVisible = false
        )
    }

    object Filters : KlikChipVariant() {
        override val preset = ChipPreset(
            borderWidth = 1,
            textAppearance = R.style.TextAppearance_Inter_Regular_B3,
            verticalPaddingDp = 6,
            horizontalPaddingDp = 8,
        )

        override val idlePalette = ChipPalette(
            backgroundColorRes = R.color.klik_color_neutral_white,
            textColorRes = R.color.klik_color_grey_50,
            startIconColorRes = R.color.klik_color_grey_50,
            endIconColorRes = R.color.klik_color_grey_50,
            borderColorRes = R.color.klik_color_grey_30,
            isStartIconBackgroundVisible = false,
            isEndIconBackgroundVisible = false,
            startIconBackgroundColorRes = android.R.color.transparent,
            endIconBackgroundColorRes = android.R.color.transparent
        )

        override val activePalette = ChipPalette(
            backgroundColorRes = R.color.klik_color_blue_20,
            textColorRes = R.color.klik_color_blue_50,
            startIconColorRes = R.color.klik_color_blue_50,
            endIconColorRes = R.color.klik_color_blue_50,
            borderColorRes = R.color.klik_color_blue_50,
            isStartIconBackgroundVisible = false,
            isEndIconBackgroundVisible = false,
            startIconBackgroundColorRes = android.R.color.transparent,
            endIconBackgroundColorRes = android.R.color.transparent
        )

        override val selectedPalette = ChipPalette(
            backgroundColorRes = R.color.klik_color_blue_20,
            textColorRes = R.color.klik_color_blue_50,
            startIconColorRes = R.color.klik_color_blue_50,
            endIconColorRes = R.color.klik_color_blue_50,
            borderColorRes = R.color.klik_color_blue_50,
            isStartIconBackgroundVisible = false,
            isEndIconBackgroundVisible = false,
            startIconBackgroundColorRes = android.R.color.transparent,
            endIconBackgroundColorRes = android.R.color.transparent
        )
    }
}