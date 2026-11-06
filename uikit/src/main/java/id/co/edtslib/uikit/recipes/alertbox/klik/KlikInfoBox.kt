package id.co.edtslib.uikit.recipes.alertbox.klik

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.view.ContextThemeWrapper
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.core.alertbox.CoreAlertBox

class KlikInfoBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.coreAlertBoxStyle
) : CoreAlertBox(ContextThemeWrapper(context, R.style.ThemeOverlay_EDTS_UIKit_AlertBox_Klik), attrs, defStyleAttr)
