package id.co.edtslib.edtsuikit

import android.os.Bundle
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesRadioButtonBinding

class GuidelinesRadioButtonActivity: GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityGuidelinesRadioButtonBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}