package id.co.edtslib.edtsuikit

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import id.co.edtslib.uikit.utils.html.FontManager
import id.co.edtslib.uikit.utils.html.HtmlListConfig
import id.co.edtslib.uikit.utils.html.HtmlRenderer
import id.co.edtslib.uikit.utils.html.HtmlRendererConfig
import id.co.edtslib.uikit.utils.html.ListStyle
import id.co.edtslib.uikit.utils.html.ListStyles
import id.co.edtslib.uikit.utils.html.applyHtmlConfig
import id.co.edtslib.uikit.utils.html.boldStyle
import id.co.edtslib.uikit.utils.html.mediumStyle
import id.co.edtslib.uikit.utils.html.renderHtml
import id.co.edtslib.uikit.utils.html.semiBoldStyle
import id.co.edtslib.uikit.utils.html.strongStyle
import id.co.edtslib.uikit.utils.html.withColor
import id.co.edtslib.uikit.utils.html.withGap
import id.co.edtslib.uikit.utils.html.withIndent
import androidx.core.graphics.toColorInt

class HTMLTag : GuidelinesBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_htmltag)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}

class HtmlListDemoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv = TextView(this).apply {
            setLineSpacing(0f, 1.15f)
            textSize = 16f
            setPadding(24, 24, 24, 24)
        }
        setContentView(tv)

        val rawHtml = """
            <p>This is a paragraph with an emoji 😊. It can be a short intro.</p>

            <p>Unordered list with bullet point:</p>
            <ul>
              <li>Bullet item one</li>
              <li>Bullet item two with emoji 🚀</li>
              <li>Bullet item three that is a bit longer so we can see wrapping across lines.</li>
            </ul>

            <p>Parenthesized ordered list (1) (2):</p>
            <ol>
              <li>Parenthesized item one</li>
              <li>Parenthesized item two</li>
            </ol>
            
            <p>Example of custom styling:</p>
            <p><b>This is bold text in magenta</b></p>
            <p><strong>This is strong text in black</strong></p>
            <p><em>This is custom em tag in red</em></p>

            <p>End paragraph — done ✅</p>
        """.trimIndent()

        val fontManager = FontManager(this)

        val listConfig = HtmlListConfig(
            indentDp = 16,
            bulletGapDp = 12,
            textSizeSp = 15f,
            lineSpacing = 1.3f,
            padding = 32
        )

        val unorderedStyle = ListStyles.default
            .withColor("#2E7D32".toColorInt())
            .withIndent(12)
            .withGap(10)

        val orderedStyle = ListStyle(
            numberFormat = "(%d)",
            indentDp = 12,
            gapDp = 10
        ).withColor("#1565C0".toColorInt())

        val config = HtmlRendererConfig(
            listConfig = listConfig,
            unorderedStyle = unorderedStyle,
            orderedStyle = orderedStyle,
            fontStyles = mapOf(
                "b"      to fontManager.boldStyle(Color.BLUE),
                "strong" to fontManager.strongStyle(Color.YELLOW),
                "mystrong" to fontManager.strongStyle(Color.BLACK),
                "em"     to fontManager.semiBoldStyle(Color.GREEN),
                "myem"     to fontManager.semiBoldStyle(Color.RED),
                "myb"    to fontManager.mediumStyle(Color.MAGENTA)
            ),
            customTagMappings = mapOf(
                "<em>"  to "<myem>",
                "</em>" to "</myem>",
                "<strong>"  to "<mystrong>",
                "</strong>" to "</mystrong>"
            )
        )

        tv.applyHtmlConfig(listConfig)
            .renderHtml(rawHtml, HtmlRenderer(config, fontManager))
    }
}

