package id.co.edtslib.edtsuikit

import android.os.Bundle
import id.co.edtslib.edtsuikit.databinding.ActivityGuidelinesHtmltextViewBinding

class GuidelinesHTMLTextViewActivity : GuidelinesBaseActivity() {

    private val binding by viewBinding<ActivityGuidelinesHtmltextViewBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guidelines_htmltext_view)

        dummies()
    }

    private fun dummies() {
        binding.tvHtmlOptions1.setHtmlText("""
            This is normal text and emoji ✨<br>
            <b>Bold</b> this is <strong>Strong</strong><br>
            <em>Emphasis Text</em><br>
            <u>Underline Text</u><br>
            <p>Paragraph text here</p>
            <a href="https://google.com">Link Text</a><br>

            <h1>Heading 1<</h1><br>
            <h2>Heading 2</h2><br>
            <h3>Heading 3</h3><br>

            This is unordered list demo:<br>
            <ul>
            <li>Unordered item 1</li>
            <li>Unordered item 2</li>
            <li>Unordered item 3</li>
            </ul>

            This is ordered list demo:<br>
            <ol>
            <li>Ordered item 1</li>
            <li>Ordered item 2</li>
            <li>Ordered item 3</li>
            </ol>
            
            This is stacked tag demo:<br>
            <h1><b><u><i>Bold Italic Underline Heading 1</i></u></b></h1><br>
            """.trimIndent()
        )
    }
}