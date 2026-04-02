package id.co.edtslib.edtsuikit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesGreyScaleViewGroupBinding

class GuidelinesGreyScaleViewGroupActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityGuidelinesGreyScaleViewGroupBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guidelines_grey_scale_view_group)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        addGreyScale()
    }

    private fun addGreyScale() {
        with(binding.disabledViewGroup) {
            disabled = true
            autoDisableTouch = true
            enableAnimation = false
            // animationDuration = 1000L

            //
            exemptViewIds
        }
    }
}