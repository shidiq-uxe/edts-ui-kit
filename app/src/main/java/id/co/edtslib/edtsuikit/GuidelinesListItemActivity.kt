package id.co.edtslib.edtsuikit

import android.os.Bundle
import android.widget.Toast
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesListItemBinding
import id.co.edtslib.uikit.utils.snack

class GuidelinesListItemActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityGuidelinesListItemBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guidelines_list_item)

        binding.liMultilineWithEndIcon.setOnClickListener {
            it.snack("Multiline With Start And End Icon Clicked", isAnchored = true)
        }
    }
}