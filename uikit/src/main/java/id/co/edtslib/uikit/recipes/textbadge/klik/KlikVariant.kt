package id.co.edtslib.uikit.recipes.textbadge.klik

import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.core.textbadge.BadgePalette
import id.co.edtslib.uikit.core.textbadge.BadgePreset
import id.co.edtslib.uikit.core.textbadge.BadgeShape
import id.co.edtslib.uikit.core.textbadge.BadgeVariant

/**
 * Composite variants for Klik text badges.
 *
 * Usage:
 * ```
 * klikBadge.klikVariant = KlikVariant.Promo.MEDIUM
 * klikBadge.klikVariant = KlikVariant.Highlight.LARGE
 * ```
 */
sealed class KlikVariant : BadgeVariant() {

    sealed class Promo : KlikVariant() {
        override val shape: BadgeShape get() = KlikShape.Promo

        override val palette = BadgePalette(
            backgroundColorRes = R.color.klik_color_blue_10,
            textColorRes = R.color.klik_color_blue_50,
            iconColorRes = R.color.klik_color_blue_50
        )

        override val disabledPalette = BadgePalette(
            backgroundColorRes = R.color.klik_color_grey_40,
            textColorRes = R.color.klik_color_neutral_white,
            iconColorRes = R.color.klik_color_neutral_white
        )

        object DEFAULT : Promo() {
            override val preset = BadgePreset(
                horizontalPadding = 4,
                verticalPadding = 2,
                iconSize = 12,
                textAppearance = R.style.TextAppearance_Inter_SemiBold_B4,
                drawablePadding = 2
            )
        }
    }

    sealed class Fair : KlikVariant() {
        override val shape: BadgeShape get() = KlikShape.Fair

        override val palette = BadgePalette(
            gradientColors = intArrayOf(
                R.color.text_badge_background_fair_gradient_start,
                R.color.text_badge_background_fair_gradient_end
            ),
            textColorRes = R.color.klik_color_neutral_white,
            iconColorRes = R.color.klik_color_neutral_white
        )

        override val disabledPalette = BadgePalette(
            backgroundColorRes = R.color.klik_color_grey_40,
            textColorRes = R.color.klik_color_neutral_white,
            iconColorRes = R.color.klik_color_neutral_white
        )

        object SMALL : Fair() {
            override val preset = BadgePreset(
                horizontalPadding = 4,
                verticalPadding = 4,
                iconSize = 10,
                textAppearance = R.style.TextAppearance_Inter_SemiBold_TextBadge,
                drawablePadding = 2
            )
        }

        object BIG : Fair() {
            override val preset = BadgePreset(
                horizontalPadding = 4,
                verticalPadding = 4,
                iconSize = 12,
                textAppearance = R.style.TextAppearance_Inter_SemiBold_B4,
                drawablePadding = 2
            )
        }
    }

    sealed class Highlight : KlikVariant() {
        override val shape: BadgeShape get() = KlikShape.Highlight

        override val palette = BadgePalette(
            gradientColors = intArrayOf(
                R.color.text_badge_background_highlight_gradient_start,
                R.color.text_badge_background_highlight_gradient_end
            ),
            textColorRes = R.color.klik_color_neutral_white,
            iconColorRes = R.color.klik_color_neutral_white
        )

        override val disabledPalette = BadgePalette(
            backgroundColorRes = R.color.klik_color_grey_40,
            textColorRes = R.color.klik_color_neutral_white,
            iconColorRes = R.color.klik_color_neutral_white
        )

        object DEFAULT : Highlight() {
            override val preset = BadgePreset(
                verticalPadding = 2,
                leftPadding = 4,
                rightPadding = 3,
                iconSize = 12,
                textAppearance = R.style.TextAppearance_Inter_SemiBold_B4,
                drawablePadding = 2
            )
        }
    }

    sealed class Quota : KlikVariant() {
        override val shape: BadgeShape get() = KlikShape.Quota

        override val palette = BadgePalette(
            backgroundColorRes = R.color.klik_color_green_10,
            textColorRes = R.color.klik_color_green_50,
            iconColorRes = R.color.klik_color_green_50
        )

        override val disabledPalette = BadgePalette(
            backgroundColorRes = R.color.klik_color_grey_30,
            textColorRes = R.color.klik_color_grey_40,
            iconColorRes = R.color.klik_color_grey_40
        )

        object DEFAULT : Quota() {
            override val preset = BadgePreset(
                horizontalPadding = 8,
                verticalPadding = 4,
                iconSize = 12,
                textAppearance = R.style.TextAppearance_Inter_SemiBold_P2,
                drawablePadding = 2
            )
        }
    }
}
