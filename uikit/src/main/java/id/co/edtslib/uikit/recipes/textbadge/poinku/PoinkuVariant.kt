package id.co.edtslib.uikit.recipes.textbadge.poinku

import android.graphics.drawable.GradientDrawable
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.core.textbadge.BadgePalette
import id.co.edtslib.uikit.core.textbadge.BadgePreset
import id.co.edtslib.uikit.core.textbadge.BadgeShape
import id.co.edtslib.uikit.core.textbadge.BadgeVariant

/**
 * Composite variants for Poinku text badges.
 * Each leaf is a fully-resolved identity.
 *
 * Usage:
 * ```
 * poinkuBadge.poinkuVariant = PoinkuVariant.Loyalty.Full
 * poinkuBadge.poinkuVariant = PoinkuVariant.Coupon.Lite
 * poinkuBadge.poinkuVariant = PoinkuVariant.Freeform
 * ```
 */
sealed class PoinkuVariant : BadgeVariant() {

    sealed class Loyalty : PoinkuVariant() {
        override val shape: BadgeShape get() = PoinkuShape.Loyalty

        object Full : Loyalty() {
            override val preset = BadgePreset(
                horizontalPadding = 8,
                verticalPadding = 4,
                iconSize = 16,
                drawablePadding = 4,
                textAppearance = R.style.TextAppearance_Poinku_Body_B3_Light
            )

            override val palette = BadgePalette(
                gradientColors = intArrayOf(
                    R.color.text_badge_background_blue_gradient_start,
                    R.color.text_badge_background_blue_gradient_end
                ),
                gradientOrientation = GradientDrawable.Orientation.BL_TR,
                textColorRes = R.color.poinku_color_neutral_white,
                iconColorRes = R.color.poinku_color_neutral_white
            )
        }

        object Lite : Loyalty() {
            override val preset = BadgePreset(
                horizontalPadding = 0,
                verticalPadding = 0,
                iconSize = 20,
                drawablePadding = 4,
                textAppearance = R.style.TextAppearance_Poinku_Body_B3_Light
            )

            override val palette = BadgePalette(
                backgroundColorRes = android.R.color.transparent,
                textColorRes = R.color.poinku_color_grey_60
            )
        }
    }

    sealed class Coupon : PoinkuVariant() {
        override val shape: BadgeShape get() = PoinkuShape.Coupon

        object Full : Coupon() {
            override val preset = BadgePreset(
                horizontalPadding = 8,
                verticalPadding = 2,
                iconSize = 16,
                drawablePadding = 4,
                textAppearance = R.style.TextAppearance_Poinku_Body_B4_Light,
                borderWidth = 1
            )

            override val palette = BadgePalette(
                backgroundColorRes = R.color.poinku_color_support_highlight_primary_weak,
                textColorRes = R.color.poinku_color_primary_blue_30,
                iconColorRes = R.color.poinku_color_primary_blue_30,
                borderColorRes = R.color.poinku_color_primary_blue_30
            )

            override val disabledPalette = BadgePalette(
                backgroundColorRes = R.color.poinku_color_grey_20,
                textColorRes = R.color.poinku_color_grey_50,
                iconColorRes = R.color.poinku_color_grey_50,
                borderColorRes = R.color.poinku_color_grey_50
            )
        }

        object Lite : Coupon() {
            override val preset = BadgePreset(
                horizontalPadding = 0,
                verticalPadding = 0,
                iconSize = 16,
                drawablePadding = 4,
                textAppearance = R.style.TextAppearance_Poinku_Body_B4_Light
            )

            override val palette = BadgePalette(
                backgroundColorRes = android.R.color.transparent,
                textColorRes = R.color.poinku_color_grey_60,
                iconColorRes = R.color.poinku_color_primary_blue_30
            )

            override val disabledPalette = BadgePalette(
                backgroundColorRes = android.R.color.transparent,
                textColorRes = R.color.poinku_color_grey_50,
                iconColorRes = R.color.poinku_color_grey_50
            )
        }
    }

    sealed class Currency : PoinkuVariant() {
        override val shape: BadgeShape get() = PoinkuShape.Currency

        object Full : Currency() {
            override val preset = BadgePreset(
                horizontalPadding = 6,
                verticalPadding = 2,
                iconSize = 16,
                drawablePadding = 4,
                textAppearance = R.style.TextAppearance_Poinku_Body_B4_Light,
                borderWidth = 1
            )

            override val palette = BadgePalette(
                backgroundColorRes = R.color.poinku_color_support_highlight_secondary_weak,
                textColorRes = R.color.poinku_color_support_highlight_secondary_strong,
                iconColorRes = R.color.poinku_color_support_highlight_secondary_strong,
                borderColorRes = R.color.poinku_color_secondary_orange_30
            )

            override val disabledPalette = BadgePalette(
                backgroundColorRes = R.color.poinku_color_grey_10,
                textColorRes = R.color.poinku_color_grey_40,
                iconColorRes = R.color.poinku_color_grey_40,
                borderColorRes = R.color.poinku_color_grey_40
            )
        }

        object Lite : Currency() {
            override val preset = BadgePreset(
                horizontalPadding = 0,
                verticalPadding = 0,
                iconSize = 16,
                drawablePadding = 4,
                textAppearance = R.style.TextAppearance_Poinku_Body_B4_Medium
            )

            override val palette = BadgePalette(
                backgroundColorRes = android.R.color.transparent,
                textColorRes = R.color.poinku_color_secondary_orange_30
            )

            override val disabledPalette = BadgePalette(
                backgroundColorRes = android.R.color.transparent,
                textColorRes = R.color.poinku_color_grey_40
            )
        }
    }

    object Freeform : PoinkuVariant() {
        override val shape: BadgeShape get() = PoinkuShape.Freeform

        override val preset = BadgePreset(
            horizontalPadding = 2,
            verticalPadding = 2,
            iconSize = 12,
            textAppearance = R.style.TextAppearance_Poinku_Body_B5_Medium,
            drawablePadding = 2
        )

        override val palette = BadgePalette(
            backgroundColorRes = R.color.poinku_color_primary_blue_30,
            textColorRes = R.color.poinku_color_neutral_white,
            iconColorRes = R.color.poinku_color_neutral_white
        )

        override val disabledPalette = BadgePalette(
            backgroundColorRes = R.color.poinku_color_grey_20,
            textColorRes = R.color.poinku_color_grey_50,
            iconColorRes = R.color.poinku_color_grey_50
        )
    }
}
