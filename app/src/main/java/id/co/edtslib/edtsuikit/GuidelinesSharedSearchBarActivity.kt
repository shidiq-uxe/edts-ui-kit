package id.co.edtslib.edtsuikit

import android.os.Bundle
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesSharedSearchBarBinding
import id.co.edtslib.uikit.utils.array

class GuidelinesSharedSearchBarActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityGuidelinesSharedSearchBarBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guidelines_shared_search_bar)

        intent.getStringExtra("Shared_Title")?.let {
            binding.sbShared.placeholderTexts = arrayOf(it)
        }

        binding.sbShared.shouldAnimatePlaceholder = false
    }
}