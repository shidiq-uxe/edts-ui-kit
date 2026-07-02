package id.co.edtslib.edtsuikit

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesHomeSwitcherBinding
import id.co.edtslib.uikit.switcher.HomeSwitcher
import id.co.edtslib.uikit.switcher.HomeSwitcherDelegate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GuidelinesHomeSwitcherActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityGuidelinesHomeSwitcherBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        binding.switcher.delegate = object : HomeSwitcherDelegate {
            override fun onSwitchChangedListener(selectedTab: HomeSwitcher.Tab) {
                Log.e(this.javaClass.simpleName, "onSwitchChangedListener: $selectedTab & ${binding.switcher.selectedTab}")
            }

            override fun onSwitchAnimationEndListener(selectedTab: HomeSwitcher.Tab) {
                Log.e(this.javaClass.simpleName, "onSwitchAnimationEndListener: $selectedTab & ${binding.switcher.selectedTab}")
            }
        }
    }
}