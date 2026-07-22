package id.co.edtslib.edtsuikit

import android.os.Bundle
import id.co.edtslib.edtsuikit.databinding.ActivityOtpBinding

class GuidelinesOtpActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityOtpBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        binding.ogError.isError = true

        binding.btnTriggerError.setOnClickListener {
            binding.ogError.isError = true
        }
    }
}