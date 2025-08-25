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

class HTMLTag : AppCompatActivity() {
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

/**
 * TagHandler that handles <myul>, <myol>, <myli> (we renamed ul/ol/li to these).
 * Adds
 *  - bullet + tab for unordered lists
 *  - "N. " numbering for ordered lists
 * And applies a LeadingMarginSpan so each list item has an indentation of at least `indentDp`.
 */
class ListTagHandler(
    private val textView: TextView,
    private val indentDp: Int = 8,        // minimum left inset for all lists
    private val bulletGapDp: Int = 8,     // BulletSpan gap between bullet and text
) : Html.TagHandler {
    private val listStack = Stack<String>()     // "ul" or "ol"
    private val olCounters = Stack<Int>()       // counters for each ol level
    private val liStartIndex = Stack<Int>()

    private fun dpToPx(dp: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), textView.resources.displayMetrics).toInt()

    override fun handleTag(opening: Boolean, tag: String, output: Editable, xmlReader: XMLReader) {
        val t = tag.lowercase().trim()
        when (t) {
            "myul" -> {
                if (opening) listStack.push("ul") else if (listStack.isNotEmpty()) listStack.pop()
            }
            "myol" -> {
                if (opening) {
                    listStack.push("ol")
                    olCounters.push(0)
                } else {
                    if (listStack.isNotEmpty()) listStack.pop()
                    if (olCounters.isNotEmpty()) olCounters.pop()
                }
            }
            "myli" -> {
                if (opening) {
                    liStartIndex.push(output.length)
                } else {
                    if (liStartIndex.isEmpty()) return
                    val start = liStartIndex.pop()
                    var end = output.length

                    val baseIndent = dpToPx(indentDp)
                    val listType = if (listStack.isNotEmpty()) listStack.peek() else "ul"

                    // ensure paragraph ends with newline (paragraph spans require boundary)
                    if (end == 0 || output[end - 1] != '\n') {
                        output.insert(end, "\n")
                        end = output.length
                    }

                    if (listType == "ul") {
                        // LeadingMarginSpan with same first/rest means wrapped lines align to the text (and bullet is inset).
                        output.setSpan(LeadingMarginSpan.Standard(baseIndent, baseIndent), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        // Apply BulletSpan (draws bullet) and LeadingMarginSpan to move everything right by baseIndent.
                        val gap = dpToPx(bulletGapDp)
                        output.setSpan(BulletSpan(gap), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    } else {
                        // Ordered list - use custom NumberedBulletSpan
                        val count = if (olCounters.isNotEmpty()) {
                            val top = olCounters.pop() + 1
                            olCounters.push(top)
                            top
                        } else 1

                        // Apply base indent (same as bullet lists for consistency)
                        output.setSpan(LeadingMarginSpan.Standard(baseIndent, baseIndent), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                        // Apply numbered bullet with same gap as regular bullets
                        val gap = dpToPx(bulletGapDp)
                        output.setSpan(OrderedSpan("${count}. ", gap, textSizePx = textView.textSize), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            }
        }
    }
}

