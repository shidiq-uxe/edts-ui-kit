package id.co.edtslib.uikit.textfield

import id.co.edtslib.uikit.R

enum class TextFieldStyle(val styleResId: Int) {
    LABEL_INSIDE(R.style.Widget_EDTS_UIKit_TextInputLayout_LabelInside),
    FLAT_START(R.style.Widget_EDTS_UIKit_TextInputLayout_LabelInside_Flat_Start),
    FLAT_END(R.style.Widget_EDTS_UIKit_TextInputLayout_LabelInside_Flat_End),
    WITHOUT_LABEL(R.style.Widget_EDTS_UIKit_TextInputLayout_WithoutLabel),
    OTP(R.style.Widget_EDTS_UIKit_TextInputLayout_Otp)
}
