package id.co.edtslib.uikit.recipes.textbadge.poinku

import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.core.textbadge.BadgeShape

sealed class PoinkuShape(name: String) : BadgeShape(name) {

    object Loyalty : PoinkuShape("loyalty") {
        override val topLeftRadius = 99f
        override val topRightRadius = 99f
        override val bottomLeftRadius = 99f
        override val bottomRightRadius = 99f
    }

    object Coupon : PoinkuShape("coupon") {
        override val topLeftRadius = 99f
        override val topRightRadius = 99f
        override val bottomLeftRadius = 99f
        override val bottomRightRadius = 99f
    }

    object Currency : PoinkuShape("currency") {
        override val topLeftRadius = 99f
        override val topRightRadius = 99f
        override val bottomLeftRadius = 99f
        override val bottomRightRadius = 99f
    }

    object Freeform : PoinkuShape("freeform") {
        override val topLeftRadius = 4f
        override val topRightRadius = 4f
        override val bottomLeftRadius = 4f
        override val bottomRightRadius = 4f
    }
}