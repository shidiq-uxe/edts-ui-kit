package id.co.edtslib.uikit.recipes.button.klik

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.view.ContextThemeWrapper
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.core.button.CoreButton

/** Selects Klik's XML button recipes; behavior belongs to [CoreButton]. */
class KlikButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.coreButtonStyle
) : CoreButton(ContextThemeWrapper(context, R.style.ThemeOverlay_EDTS_UIKit_Button_Klik), attrs, defStyleAttr)
