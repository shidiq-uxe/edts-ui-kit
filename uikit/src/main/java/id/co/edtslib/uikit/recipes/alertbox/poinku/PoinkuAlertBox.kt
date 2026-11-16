package id.co.edtslib.uikit.recipes.alertbox.poinku

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.view.ContextThemeWrapper
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.core.alertbox.CoreAlertBox

class PoinkuAlertBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.coreAlertBoxStyle
) : CoreAlertBox(ContextThemeWrapper(context, R.style.ThemeOverlay_EDTS_UIKit_AlertBox_Poinku), attrs, defStyleAttr)
