package id.co.edtslib.edtsuikit

import android.graphics.Color
import id.co.edtslib.uikit.utils.span.OrderedSpan
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.text.Editable
import android.text.Html
import android.text.Spanned
import android.text.style.BulletSpan
import android.text.style.LeadingMarginSpan
import android.widget.TextView
import org.xml.sax.XMLReader
import java.util.Stack
import android.util.TypedValue
import id.co.edtslib.uikit.utils.html.FontManager
import id.co.edtslib.uikit.utils.html.HtmlListConfig
import id.co.edtslib.uikit.utils.html.HtmlRenderer
import id.co.edtslib.uikit.utils.html.HtmlRendererConfig
import id.co.edtslib.uikit.utils.html.applyHtmlConfig
import id.co.edtslib.uikit.utils.html.boldStyle
import id.co.edtslib.uikit.utils.html.renderHtml

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
            // allow multiline wrapping etc
            setLineSpacing(0f, 1.15f)
            textSize = 16f
            setPadding(24, 24, 24, 24)
        }
        setContentView(tv)

        val rawHtml = """
            <p>This is a paragraph with an emoji 😊. It can be a short intro.</p>
            <ul>
              <li>Bullet item one</li>
              <li>Bullet item two with emoji 🚀</li>
              <li>Bullet item three that is a bit longer so we can see wrapping across lines.</li>
            </ul>

            <p>Another paragraph between lists.</p>

            <ol>
              <li>Numbered item one</li>
              <li>Numbered item two with at least <b>2 lines</b> that should be shown on the demo</li>
            </ol>

            <p>End paragraph — done ✅</p>
        """.trimIndent()

        val fontManager = FontManager(this)
        val config = HtmlRendererConfig(
            fontStyles = mapOf("b" to fontManager.boldStyle(Color.CYAN))
        )

       tv.applyHtmlConfig(HtmlListConfig())
           .renderHtml(rawHtml.toString(), HtmlRenderer(config, fontManager))
    }
}

