package id.co.edtslib.edtsuikit

import android.os.Bundle
import id.co.edtslib.edtsuikit.databinding.ActivityButtonBinding

class GuidelinesButtonActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityButtonBinding>()

    override val enableEdgeToEdge: Boolean
        get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_button)
    }
}