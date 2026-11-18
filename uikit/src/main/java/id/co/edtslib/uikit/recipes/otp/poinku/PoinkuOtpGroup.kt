package id.co.edtslib.uikit.recipes.otp.poinku

import android.content.Context
import android.util.AttributeSet
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.core.otp.CoreOtpGroup

class PoinkuOtpGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.coreOtpGroupStyle
) : CoreOtpGroup(context, attrs, defStyleAttr, R.style.ThemeOverlay_EDTS_UIKit_CoreOtpGroup_Poinku)
