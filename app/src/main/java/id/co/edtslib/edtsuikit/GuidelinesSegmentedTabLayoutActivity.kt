package id.co.edtslib.edtsuikit

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesSegmentedTabLayoutBinding
import id.co.edtslib.uikit.tablayout.HomeTabLayout
import id.co.edtslib.uikit.tablayout.HomeTabLayoutDelegate
import id.co.edtslib.uikit.utils.AlertType
import id.co.edtslib.uikit.utils.alertSnack
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.setLightStatusBar
import id.co.edtslib.uikit.utils.snack

class GuidelinesSegmentedTabLayoutActivity : AppCompatActivity() {

    private val binding by viewBinding<ActivityGuidelinesSegmentedTabLayoutBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guidelines_segmented_tab_layout)

        setStatusBarColor()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.segmentedTabLayout.delegate = object : HomeTabLayoutDelegate {
            override fun onTabSelected(tab: HomeTabLayout.HomeTab) {
                binding.root.alertSnack("Selected : ${tab.value}. $tab", alertType = AlertType.DEFAULT)
            }
        }
    }

    private fun setStatusBarColor() {
        window.statusBarColor = color(id.co.edtslib.uikit.R.color.primary_30)
    }
}