package id.co.edtslib.uikit.recipes.button.poinku

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.view.ContextThemeWrapper
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.core.button.CoreButton

/** Selects Poinku's XML button recipes; behavior belongs to [CoreButton]. */
class PoinkuButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.coreButtonStyle
) : CoreButton(ContextThemeWrapper(context, R.style.ThemeOverlay_EDTS_UIKit_Button_Poinku), attrs, defStyleAttr)
