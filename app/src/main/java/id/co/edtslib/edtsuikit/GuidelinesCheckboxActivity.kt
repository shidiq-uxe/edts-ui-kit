package id.co.edtslib.edtsuikit

import android.os.Bundle
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesCheckboxBinding

class GuidelinesCheckboxActivity: GuidelinesBaseActivity() {
    private val binding by viewBinding<ActivityGuidelinesCheckboxBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

    }
}