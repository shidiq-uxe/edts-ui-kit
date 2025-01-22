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
import id.co.edtslib.uikit.utils.TextStyle
import id.co.edtslib.uikit.utils.alertSnack
import id.co.edtslib.uikit.utils.buildHighlightedMessage
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dimen
import id.co.edtslib.uikit.utils.dimenPixelSize
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
                // Set something during tab click
            }

            override fun setOnSwitchAnimationEndListener(selectedTab: HomeSwitcher.Tab) {
                // Cancelling job to ensure no multiple job created
                job.cancel()

                job = lifecycleScope.launch {
                    swapPlaceholderVisibility(selectedTab)
                }
            }
        }
    }

    private fun setStatusBarColor() {
        window.statusBarColor = color(id.co.edtslib.uikit.R.color.primary_30)
    }

    private suspend fun swapPlaceholderVisibility(tab: HomeSwitcher.Tab) {
        binding.homeSkeleton.root.isVisible = true
        binding.viewGroup.isVisible = false
        // 3 Seconds Delay
        delay(3000)
        binding.homeSkeleton.root.isVisible = false
        binding.viewGroup.isVisible = true

        val alertMessage = buildHighlightedMessage(
            context = this,
            message = "Belanja $tab Dipilih.",
            defaultTextAppearance = TextStyle.b3Style(context = this, color(R.color.white)),
            highlightedMessages = listOf("Belanja $tab"),
            highlightedTextAppearance = listOf(
                TextStyle.h3Style(
                    context = this,
                    color = color(id.co.edtslib.uikit.R.color.white),
                    fontFamily = id.co.edtslib.uikit.R.font.inter_black
                )
            )
        )

        binding.root.alertSnack(
            message = alertMessage,
            bottomMargin = dimenPixelSize(id.co.edtslib.uikit.R.dimen.dimen_96),
            messageHasStyle = true
        )
    }

}