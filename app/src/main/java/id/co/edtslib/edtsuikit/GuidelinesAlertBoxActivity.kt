package id.co.edtslib.edtsuikit

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import id.co.edtslib.edtsuikit.databinding.ActivityAlertboxBinding
import id.co.edtslib.uikit.alert.AlertBox
import id.co.edtslib.uikit.alert.AlertBoxDelegate

class GuidelinesAlertBoxActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityAlertboxBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alertbox)

         val alertBoxes = listOf(binding.abDefault, binding.abSuccess, binding.abInfo, binding.abWarning, binding.abError)

        alertBoxes.forEach {
            it.delegate = object : AlertBoxDelegate {
                override fun onCloseClickListener(view: View) {
                    it.isVisible = false
                }
            }
        }
    }
}