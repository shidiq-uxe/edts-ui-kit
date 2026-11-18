package id.co.edtslib.uikit.recipes.textfield.poinku

import android.content.Context
import android.util.AttributeSet
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.core.textfield.CoreTextField

class PoinkuTextField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.coreTextFieldStyle
) : CoreTextField(
    context,
    attrs,
    defStyleAttr,
    R.style.ThemeOverlay_EDTS_UIKit_CoreTextField_Poinku
)
