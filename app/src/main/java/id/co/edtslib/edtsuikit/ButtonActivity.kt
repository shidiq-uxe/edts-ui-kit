package id.co.edtslib.edtsuikit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import id.co.edtslib.uikit.otp.OtpDelegate
import id.co.edtslib.uikit.otp.OtpGroup
import id.co.edtslib.uikit.utils.snack

class ButtonActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_button)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val otpGroup = findViewById<OtpGroup>(R.id.otpGroup)

        otpGroup.delegate = object : OtpDelegate {
            override fun setOnOtpCompleteListener(otp: String) {
                otpGroup.isError = otp != "2024"

                otpGroup.snack("Snack : $otp")
            }
        }
    }
}