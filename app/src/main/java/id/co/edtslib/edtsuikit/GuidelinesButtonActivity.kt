package id.co.edtslib.edtsuikit

import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import id.co.edtslib.edtsuikit.databinding.ActivityButtonBinding
import id.co.edtslib.uikit.button.Button
import id.co.edtslib.uikit.utils.setLightStatusBar

class GuidelinesButtonActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityButtonBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_button)
        setLightStatusBar()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}