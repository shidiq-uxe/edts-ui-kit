package id.co.edtslib.edtsuikit

import android.os.Bundle
import com.google.android.material.button.MaterialButton
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesSegmentedTabLayoutBinding
import id.co.edtslib.uikit.tablayout.HomeTabLayout
import id.co.edtslib.uikit.tablayout.HomeTabLayoutDelegate
import id.co.edtslib.uikit.utils.AlertType
import id.co.edtslib.uikit.utils.alertSnack
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.snack

class GuidelinesSegmentedTabLayoutActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityGuidelinesSegmentedTabLayoutBinding>()

    override val statusBarScrimColor: Int
        get() = color(id.co.edtslib.uikit.R.color.primary_30)

    override val isLightStatusBar: Boolean
        get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guidelines_segmented_tab_layout)

        binding.segmentedTabLayout.delegate = object : HomeTabLayoutDelegate {
            override fun onTabSelected(tab: HomeTabLayout.HomeTab) {
                binding.root.alertSnack("Selected : ${tab.value}. $tab", alertType = AlertType.DEFAULT)
            }
        }
    }
}