package id.co.edtslib.edtsuikit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesSharedSearchBarBinding
import id.co.edtslib.uikit.utils.array

class GuidelinesSharedSearchBarActivity : AppCompatActivity() {

    private val binding by viewBinding<ActivityGuidelinesSharedSearchBarBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guidelines_shared_search_bar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        intent.getStringExtra("Shared_Title")?.let {
            binding.sbShared.placeholderTexts = arrayOf(it)
        }

        binding.sbShared.shouldAnimatePlaceholder = false

        postponeEnterTransition()
        startPostponedEnterTransition()
    }
}