package id.co.edtslib.uikit.recipes.linearprogressbar.klik

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.view.ContextThemeWrapper
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.core.linearprogressbar.CoreLinearProgressBar

class KlikLinearProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.linearProgressBarStyle
) : CoreLinearProgressBar(ContextThemeWrapper(context, R.style.ThemeOverlay_EDTS_UIKit_LinearProgressBar_Klik), attrs, defStyleAttr, KlikLinearProgressBarVariant) {

}