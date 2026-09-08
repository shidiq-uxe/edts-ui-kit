package id.co.edtslib.uikit.recipes.textbadge.klik

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.StyleRes
import androidx.appcompat.view.ContextThemeWrapper
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.core.textbadge.BadgeVariant
import id.co.edtslib.uikit.core.textbadge.CoreTextBadge

class KlikTextBadge @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CoreTextBadge(ContextThemeWrapper(context, R.style.ThemeOverlay_EDTS_UIKit_TextBadge_Klik), attrs) {

    init {
        if (!hasBadgeVariantFromXml) {
            badgeVariant = KlikVariant.Fair.SMALL
        }
    }

    var klikVariant: KlikVariant
        get() = badgeVariant as? KlikVariant ?: KlikVariant.Fair.SMALL
        set(value) { badgeVariant = value }

    override fun variantFromStyle(@StyleRes resId: Int): BadgeVariant? = when (resId) {
        R.style.Widget_EDTS_UIKit_TextBadge_Klik_Promo -> KlikVariant.Promo.DEFAULT
        R.style.Widget_EDTS_UIKit_TextBadge_Klik_Fair_Small -> KlikVariant.Fair.SMALL
        R.style.Widget_EDTS_UIKit_TextBadge_Klik_Fair_Big -> KlikVariant.Fair.BIG
        R.style.Widget_EDTS_UIKit_TextBadge_Klik_Highlight -> KlikVariant.Highlight.DEFAULT
        R.style.Widget_EDTS_UIKit_TextBadge_Klik_Quota -> KlikVariant.Quota.DEFAULT
        else -> null
    }
}
