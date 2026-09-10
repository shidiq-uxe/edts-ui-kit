package id.co.edtslib.uikit.recipes.textbadge.klik

import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.core.textbadge.BadgeShape

/**
 * Klik shape definitions — geometry (radii) only.
 *
 * All presets, palettes, and disabled palettes live in [KlikVariant].
 */
sealed class KlikShape(name: String) : BadgeShape(name) {

    object Promo : KlikShape("promo") {
        override val topLeftRadius = 4f
        override val topRightRadius = 4f
        override val bottomLeftRadius = 4f
        override val bottomRightRadius = 4f
    }

    object Fair : KlikShape("fair") {
        override val topLeftRadius = 4f
        override val topRightRadius = 4f
        override val bottomLeftRadius = 4f
        override val bottomRightRadius = 4f
    }

    object Highlight : KlikShape("highlight") {
        override val topLeftRadius = 0f
        override val topRightRadius = 0f
        override val bottomLeftRadius = 0f
        override val bottomRightRadius = 8f
    }

    object Quota : KlikShape("quota") {
        override val topLeftRadius = 4f
        override val topRightRadius = 4f
        override val bottomLeftRadius = 4f
        override val bottomRightRadius = 4f
    }
}
