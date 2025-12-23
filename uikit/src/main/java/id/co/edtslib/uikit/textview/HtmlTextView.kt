package id.co.edtslib.uikit.textview

import android.content.Context
import android.text.Editable
import android.text.Html
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.use
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.html.FontManager
import id.co.edtslib.uikit.utils.html.FontStyle
import id.co.edtslib.uikit.utils.html.HtmlListConfig
import id.co.edtslib.uikit.utils.html.HtmlRenderer
import id.co.edtslib.uikit.utils.html.HtmlRendererConfig
import id.co.edtslib.uikit.utils.html.ListStyle
import id.co.edtslib.uikit.utils.html.toSpanned
import id.co.edtslib.uikit.utils.px
import org.xml.sax.XMLReader
import java.util.*

class HtmlTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val styleConfig = HtmlStyleConfig()
    private var listConfig = HtmlListConfig()
    private lateinit var fontManager: FontManager
    private var htmlRenderer: HtmlRenderer? = null

    private var rawHtmlText: String = ""

    init {
        fontManager = FontManager(context)
        parseAttributes(attrs, defStyleAttr)
        setupRenderer()
    }

    private fun parseAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        context.obtainStyledAttributes(attrs, R.styleable.HtmlTextView, defStyleAttr, 0).use { a ->
            // Heading styles
            styleConfig.h1Appearance = a.getResourceId(R.styleable.HtmlTextView_h1TextAppearance, 0)
            styleConfig.h2Appearance = a.getResourceId(R.styleable.HtmlTextView_h2TextAppearance, 0)
            styleConfig.h3Appearance = a.getResourceId(R.styleable.HtmlTextView_h3TextAppearance, 0)
            styleConfig.h4Appearance = a.getResourceId(R.styleable.HtmlTextView_h4TextAppearance, 0)
            styleConfig.h5Appearance = a.getResourceId(R.styleable.HtmlTextView_h5TextAppearance, 0)
            styleConfig.h6Appearance = a.getResourceId(R.styleable.HtmlTextView_h6TextAppearance, 0)

            // Paragraph style
            styleConfig.pAppearance = a.getResourceId(R.styleable.HtmlTextView_pTextAppearance, 0)

            // Text formatting
            styleConfig.bAppearance = a.getResourceId(R.styleable.HtmlTextView_bTextAppearance, 0)
            styleConfig.strongAppearance = a.getResourceId(R.styleable.HtmlTextView_strongTextAppearance, 0)
            styleConfig.iAppearance = a.getResourceId(R.styleable.HtmlTextView_iTextAppearance, 0)
            styleConfig.emAppearance = a.getResourceId(R.styleable.HtmlTextView_emTextAppearance, 0)
            styleConfig.uAppearance = a.getResourceId(R.styleable.HtmlTextView_uTextAppearance, 0)
            styleConfig.aAppearance = a.getResourceId(R.styleable.HtmlTextView_aTextAppearance, 0)

            // List styling
            val bulletChar = a.getString(R.styleable.HtmlTextView_bulletChar) ?: "•"
            val bulletColor = if (a.hasValue(R.styleable.HtmlTextView_bulletColor)) {
                a.getColor(R.styleable.HtmlTextView_bulletColor, currentTextColor)
            } else null

            val bulletGapPx = a.getDimensionPixelSize(R.styleable.HtmlTextView_bulletGap, 8.px.toInt())
            val orderedFormat = a.getString(R.styleable.HtmlTextView_orderedFormat) ?: "%d."
            val orderedColor = if (a.hasValue(R.styleable.HtmlTextView_orderedColor)) {
                a.getColor(R.styleable.HtmlTextView_orderedColor, currentTextColor)
            } else null

            val listIndentPx = a.getDimensionPixelSize(R.styleable.HtmlTextView_listIndent, 16.px.toInt())
            val lineSpacing = a.getFloat(R.styleable.HtmlTextView_htmlLineSpacing, 1.15f)
            val padding = a.getDimensionPixelSize(R.styleable.HtmlTextView_htmlPadding, 0)

            // Create ListConfig
            listConfig = HtmlListConfig(
                indentDp = listIndentPx.px.toInt(),
                bulletGapDp = bulletGapPx.px.toInt(),
                lineSpacing = lineSpacing,
                padding = padding
            )

            // Create ListStyles
            styleConfig.unorderedStyle = ListStyle(
                bulletChar = bulletChar,
                indentDp = listIndentPx.px.toInt(),
                gapDp = bulletGapPx.px.toInt(),
                textColor = bulletColor
            )

            styleConfig.orderedStyle = ListStyle(
                numberFormat = orderedFormat,
                indentDp = listIndentPx.px.toInt(),
                gapDp = bulletGapPx.px.toInt(),
                textColor = orderedColor
            )
        }

        // Apply config
        if (listConfig.padding > 0) {
            setPadding(listConfig.padding, listConfig.padding, listConfig.padding, listConfig.padding)
        }
        setLineSpacing(0f, listConfig.lineSpacing)
    }

    private fun setupRenderer() {
        // Use your existing HtmlRendererConfig with additional styleConfig
        val rendererConfig = HtmlRendererConfig(
            listConfig = listConfig,
            unorderedStyle = styleConfig.unorderedStyle,
            orderedStyle = styleConfig.orderedStyle,
            fontStyles = emptyMap(),
            customTagMappings = emptyMap(),
            /*styleConfig = styleConfig, // Pass styleConfig to existing config
            context = context // Pass context for TextAppearanceSpan*/
        )

        // Use your existing HtmlRenderer
        htmlRenderer = HtmlRenderer(rendererConfig, fontManager)
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        if (text.isNullOrEmpty()) {
            super.setText(text, type)
            return
        }

        rawHtmlText = text.toString()

        // Preprocess HTML tags
        val processedHtml = rawHtmlText.preprocessHtmlTags()

        // Use your existing renderer - it will call TagHandler internally
        val spanned = htmlRenderer?.render(processedHtml, this) ?: text

        super.setText(spanned, BufferType.SPANNABLE)
    }

    private fun String.preprocessHtmlTags(): String {
        val tags = listOf("h1", "h2", "h3", "h4", "h5", "h6", "p", "b", "strong", "i", "em", "u", "a")

        var result = this
        tags.forEach { tag ->
            result = result.replace("<$tag>", "<my$tag>", ignoreCase = true)
            result = result.replace("</$tag>", "</my$tag>", ignoreCase = true)
            result = result.replace(Regex("<$tag\\s+", RegexOption.IGNORE_CASE), "<my$tag ")
        }

        return result
    }

    fun setHtmlText(html: String) {
        setText(html)
    }

    fun getHtmlText(): String = rawHtmlText
}

data class HtmlStyleConfig(
    var h1Appearance: Int = 0,
    var h2Appearance: Int = 0,
    var h3Appearance: Int = 0,
    var h4Appearance: Int = 0,
    var h5Appearance: Int = 0,
    var h6Appearance: Int = 0,
    var pAppearance: Int = 0,
    var bAppearance: Int = 0,
    var strongAppearance: Int = 0,
    var iAppearance: Int = 0,
    var emAppearance: Int = 0,
    var uAppearance: Int = 0,
    var aAppearance: Int = 0,
    var unorderedStyle: ListStyle = ListStyle(),
    var orderedStyle: ListStyle = ListStyle()
) {
    fun getAppearanceForTag(tag: String): Int {
        return when (tag.lowercase()) {
            "h1" -> h1Appearance
            "h2" -> h2Appearance
            "h3" -> h3Appearance
            "h4" -> h4Appearance
            "h5" -> h5Appearance
            "h6" -> h6Appearance
            "p" -> pAppearance
            "b" -> bAppearance
            "strong" -> strongAppearance
            "i" -> iAppearance
            "em" -> emAppearance
            "u" -> uAppearance
            "a" -> aAppearance
            else -> 0
        }
    }
}