package id.co.edtslib.uikit.otp

interface OtpDelegate {
    fun setOnOtpCompleteListener(otp: String)

    fun setOnTextChangeListener(otp: String)
}