package id.co.edtslib.uikit.textview

import android.content.Context
import android.graphics.Typeface
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import androidx.annotation.StyleRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.use
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.html.FontManager
import id.co.edtslib.uikit.utils.html.FontStyle
import id.co.edtslib.uikit.utils.html.HtmlListConfig
import id.co.edtslib.uikit.utils.html.HtmlRenderer
import id.co.edtslib.uikit.utils.html.HtmlRendererConfig
import id.co.edtslib.uikit.utils.html.ListStyle
import id.co.edtslib.uikit.utils.html.boldStyle
import id.co.edtslib.uikit.utils.html.strongStyle

class HtmlTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.htmlTextViewStyle
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
        movementMethod = LinkMovementMethod.getInstance()
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

            val bulletGapDp = a.getDimensionPixelSize(R.styleable.HtmlTextView_bulletGap, 8)
            val orderedFormat = a.getString(R.styleable.HtmlTextView_orderedFormat) ?: "%d."
            val orderedColor = if (a.hasValue(R.styleable.HtmlTextView_orderedColor)) {
                a.getColor(R.styleable.HtmlTextView_orderedColor, currentTextColor)
            } else null

            val listIndentDp = a.getDimensionPixelSize(R.styleable.HtmlTextView_listIndent, 16)
            val lineSpacing = a.getFloat(R.styleable.HtmlTextView_htmlLineSpacing, 1.15f)
            val padding = a.getDimensionPixelSize(R.styleable.HtmlTextView_htmlPadding, 0)

            // Create ListConfig
            listConfig = HtmlListConfig(
                indentDp = listIndentDp,
                bulletGapDp = bulletGapDp,
                lineSpacing = lineSpacing,
                padding = padding
            )

            // Create ListStyles
            styleConfig.unorderedStyle = ListStyle(
                bulletChar = bulletChar,
                indentDp = listIndentDp,
                gapDp = bulletGapDp,
                textColor = bulletColor
            )

            styleConfig.orderedStyle = ListStyle(
                numberFormat = orderedFormat,
                indentDp = listIndentDp,
                gapDp = bulletGapDp,
                textColor = orderedColor
            )
        }

        attrs?.let {
            val textAttrs = context.obtainStyledAttributes(it, intArrayOf(android.R.attr.text))
            val xmlText = textAttrs.getString(0)
            textAttrs.recycle()
            if (!xmlText.isNullOrEmpty()) {
                rawHtmlText = xmlText
            }
        }

        if (listConfig.padding > 0) {
            setPadding(listConfig.padding, listConfig.padding, listConfig.padding, listConfig.padding)
        }
        setLineSpacing(0f, listConfig.lineSpacing)
    }

    private fun setupRenderer() {
        val rendererConfig = HtmlRendererConfig(
            listConfig = listConfig,
            unorderedStyle = styleConfig.unorderedStyle,
            orderedStyle = styleConfig.orderedStyle,
            fontStyles = buildFontStyles(),
            customTagMappings = emptyMap(),
            styleConfig = styleConfig,
            context = context
        )
        htmlRenderer = HtmlRenderer(rendererConfig, fontManager)
    }

    private fun buildFontStyles(): Map<String, FontStyle> {
        return mutableMapOf<String, FontStyle>().apply {
            put("myb",      fontManager.boldStyle())
            put("mystrong", fontManager.strongStyle())

            listOf("h1", "h2", "h3", "h4", "h5", "h6", "p").forEach { tag ->
                val appearanceRes = styleConfig.getAppearanceForTag(tag)
                if (appearanceRes != 0) {
                    put("my$tag", resolveFontStyleFromAppearance(appearanceRes))
                }
            }
        }
    }

    private fun resolveFontStyleFromAppearance(@StyleRes resId: Int): FontStyle {
        val ta = context.obtainStyledAttributes(resId, intArrayOf(
            android.R.attr.textSize,
            android.R.attr.textColor,
            android.R.attr.textStyle,
            android.R.attr.fontFamily,
        ))

        val textSize = if (ta.hasValue(0)) ta.getDimensionPixelSize(0, 0).toFloat() else null
        val textColor = if (ta.hasValue(1)) ta.getColor(1, 0) else null
        val textStyle = ta.getInt(2, Typeface.NORMAL)
        val typeface = if (ta.hasValue(3)) {
            val fontResId = ta.getResourceId(3, 0)
            if (fontResId != 0) fontManager.loadFont(fontResId) else null
        } else null

        ta.recycle()

        return FontStyle(
            fontFamily = typeface,
            textColor = textColor,
            textSize = textSize,
            style = textStyle
        )
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        if (!text.isNullOrEmpty()) {
            rawHtmlText = text.toString()
        }

        if (rawHtmlText.isEmpty() || htmlRenderer == null) {
            if (text.isNullOrEmpty()) return
            super.setText(text, type)
            return
        }

        val processedHtml = rawHtmlText.preprocessHtmlTags()
        val spanned = htmlRenderer?.render(processedHtml, this) ?: text
        super.setText(spanned, BufferType.SPANNABLE)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (rawHtmlText.isNotEmpty()) {
            setHtmlText(rawHtmlText)
        }
    }

    private fun String.preprocessHtmlTags(): String {
        val tags = listOf("h1", "h2", "h3", "h4", "h5", "h6", "p", "strong")


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