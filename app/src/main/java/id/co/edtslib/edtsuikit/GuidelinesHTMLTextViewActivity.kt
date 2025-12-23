package id.co.edtslib.edtsuikit

import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesHtmltextViewBinding

class GuidelinesHTMLTextViewActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityGuidelinesHtmltextViewBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guidelines_htmltext_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dummies()
    }

    private fun dummies() {
        binding.tvHtmlOptions1.setHtmlText("This Text Is <b>Bold</b> Bold2")
    }
}