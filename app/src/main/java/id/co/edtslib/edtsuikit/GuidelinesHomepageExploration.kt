package id.co.edtslib.edtsuikit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesHomepageExplorationBinding
import id.co.edtslib.uikit.searchbar.SearchBar
import id.co.edtslib.uikit.switcher.HomeSwitcher
import id.co.edtslib.uikit.switcher.HomeSwitcherDelegate
import id.co.edtslib.uikit.utils.color
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GuidelinesHomepageExploration : AppCompatActivity() {

    private val binding by viewBinding<ActivityGuidelinesHomepageExplorationBinding>()

    private var job: Job = Job()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guidelines_homepage_exploration)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setStatusBarColor()
        onSwitcherChange()

        binding.sbHome.placeholderAnimationType = SearchBar.PlaceholderAnimationType.TypeWriterWithPrefix
    }

    private fun onSwitcherChange() {
        binding.homeSwitcher.delegate = object : HomeSwitcherDelegate {
            override fun setOnSwitchChangedListener(selectedTab: HomeSwitcher.Tab) {
                // Cancelling job to ensure no multiple job created
                job.cancel()

                job = lifecycleScope.launch {
                    swapPlaceholderVisibility()
                }
            }
        }
    }

    private fun setStatusBarColor() {
        window.statusBarColor = color(id.co.edtslib.uikit.R.color.primary_30)
    }

    private suspend fun swapPlaceholderVisibility() {
        binding.homeSkeleton.root.isVisible = true
        binding.ivContent.isVisible = false
        // 3 Seconds Delay
        delay(3000)
        binding.homeSkeleton.root.isVisible = false
        binding.ivContent.isVisible = true
    }

}