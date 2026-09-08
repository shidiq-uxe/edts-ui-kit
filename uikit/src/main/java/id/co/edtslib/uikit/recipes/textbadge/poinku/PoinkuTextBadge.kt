package id.co.edtslib.uikit.recipes.textbadge.poinku

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.StyleRes
import androidx.appcompat.view.ContextThemeWrapper
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.core.textbadge.BadgeVariant
import id.co.edtslib.uikit.core.textbadge.CoreTextBadge

class PoinkuTextBadge @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CoreTextBadge(ContextThemeWrapper(context, R.style.ThemeOverlay_EDTS_UIKit_TextBadge_Poinku), attrs) {

    init {
        if (!hasBadgeVariantFromXml) {
            badgeVariant = PoinkuVariant.Loyalty.Full
        }
    }

    var poinkuVariant: PoinkuVariant
        get() = badgeVariant as? PoinkuVariant ?: PoinkuVariant.Loyalty.Full
        set(value) { badgeVariant = value }

    override fun variantFromStyle(@StyleRes resId: Int): BadgeVariant? = when (resId) {
        R.style.Widget_EDTS_UIKit_TextBadge_Poinku_Loyalty_Full -> PoinkuVariant.Loyalty.Full
        R.style.Widget_EDTS_UIKit_TextBadge_Poinku_Loyalty_Lite -> PoinkuVariant.Loyalty.Lite
        R.style.Widget_EDTS_UIKit_TextBadge_Poinku_Coupon_Full -> PoinkuVariant.Coupon.Full
        R.style.Widget_EDTS_UIKit_TextBadge_Poinku_Coupon_Lite -> PoinkuVariant.Coupon.Lite
        R.style.Widget_EDTS_UIKit_TextBadge_Poinku_Currency_Full -> PoinkuVariant.Currency.Full
        R.style.Widget_EDTS_UIKit_TextBadge_Poinku_Currency_Lite -> PoinkuVariant.Currency.Lite
        R.style.Widget_EDTS_UIKit_TextBadge_Poinku_Freeform -> PoinkuVariant.Freeform
        else -> null
    }
}
