package id.co.edtslib.edtsuikit

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope
import id.co.edtslib.edtsuikit.databinding.ActivitySearchbarBinding
import id.co.edtslib.uikit.searchbar.SearchBar
import id.co.edtslib.uikit.searchbar.SearchBarDelegate
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GuidelinesSearchbarActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivitySearchbarBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searchbar)

        textSwitcherExperimental()
        experimentalTextSwitcher2()

        bindDefaultSearchBarDelegate()

        binding.sbDefault.shouldAnimatePlaceholder = false

        binding.sbSlideUp.placeholderAnimationType = SearchBar.PlaceholderAnimationType.SlideUp
        binding.sbTypeWriter.placeholderAnimationType = SearchBar.PlaceholderAnimationType.TypeWriter
        binding.sbPrefixTypeWriter.placeholderAnimationType = SearchBar.PlaceholderAnimationType.TypeWriterWithPrefix

        binding.btnImpl.setOnClickListener {
            Intent(this, GuidelinesSharedSearchBarActivity::class.java).apply {
                this.putExtra("Shared_Title", binding.sbPrefixTypeWriter.placeholder)

                startActivity(
                    this,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(this@GuidelinesSearchbarActivity, binding.sbPrefixTypeWriter, "shared_searchbar").toBundle()
                )
            }
        }
    }

    private fun bindDefaultSearchBarDelegate() {
        binding.sbDefault.searchBarDelegate = object : SearchBarDelegate {
            override fun onCloseIconClicked(view: View) {
                Toast.makeText(this@GuidelinesSearchbarActivity, "Close Icon Clicked", Toast.LENGTH_SHORT).show()

                binding.sbDefault.editText.text?.clear()
            }

            override fun onFocusChange(view: View, hasFocus: Boolean) {

            }

            override fun onTextChange(text: String) {

            }
        }
    }

    private val placeholderItems = listOf("Search Skincare", "Search Frozen Food", "Search Milk", "Search Noodles")

    private var textSwitcherJob: Job = Job()

    private fun textSwitcherExperimental() {
        val textAppearance = id.co.edtslib.uikit.R.style.TextAppearance_Inter_Regular_P1

        binding.tsSlideUp.setFactory {
            TextView(this).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setTextAppearance(textAppearance)
                }
            }
        }

        binding.tsSlideUp.setCurrentText(placeholderItems.first())

        setAnimations()

        startTextSwitchingLoop(placeholderItems)
    }

    private fun setAnimations() {
        val inAnimation = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 1.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f
        )
        inAnimation.duration = 300

        val outAnimation = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, -1.0f
        )
        outAnimation.duration = 300

        binding.tsSlideUp.inAnimation = inAnimation
        binding.tsSlideUp.outAnimation = outAnimation
    }

    private fun startTextSwitchingLoop(placeholders: List<String>) {
        textSwitcherJob = lifecycleScope.launch {
            textSwitcherJob.cancel()

            var index = 0
            while (isActive) {
                delay(2000L) // Delay between text changes (2 seconds)
                index = (index + 1) % placeholders.size // Ensure index loops back to 0
                binding.tsSlideUp.setText(placeholders[index]) // Change the text with animation
            }
        }
    }


    private var tsJob2: Job = Job()

    private fun experimentalTextSwitcher2() {
        val textAppearance = id.co.edtslib.uikit.R.style.TextAppearance_Inter_Regular_P1

        binding.tsTypeWriter.setFactory {
            TextView(this).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setTextAppearance(textAppearance)
                } else {
                    setTextAppearance(context, textAppearance)
                }
            }
        }

        binding.tsTypeWriter.apply {
            setCurrentText(placeholderItems.first())

            // Animation
            inAnimation = AlphaAnimation(0f, 1f).apply {
                duration = 300
            }
            outAnimation = AlphaAnimation(1f, 0f).apply {
                duration = 300
            }
        }

        startTextSwitchingLoop2()
    }

    private var tsIndex2 = 0

    private fun startTextSwitchingLoop2() {
        if (tsIndex2 > placeholderItems.size.minus(1)) {
            tsIndex2 = 0
        }

        tsJob2 = lifecycleScope.launch {
            tsJob2.cancel()

            typeWriterEffect(placeholderItems[tsIndex2]) {
                tsIndex2++

                startTextSwitchingLoop2()
            }
        }
    }

    private suspend fun typeWriterEffect(text: String, onTypeWriterEndAction: () -> Unit) {
        binding.tsTypeWriter.setText("") // Clear the previous text
        var charIndex = 0

        delay(250)

        (binding.tsTypeWriter.currentView as TextView).append("")
        while (charIndex < text.length) {
            (binding.tsTypeWriter.currentView as TextView).append(text[charIndex].toString())
            charIndex++
            delay(80) // Use Coroutine delay for each character
        }

        delay(300)
        onTypeWriterEndAction.invoke()
    }

}