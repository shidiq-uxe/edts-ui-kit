package id.co.edtslib.uikit.utils.html

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.Html
import android.text.Spanned
import android.text.TextPaint
import android.text.style.AbsoluteSizeSpan
import android.text.style.BackgroundColorSpan
import android.text.style.BulletSpan
import android.text.style.ForegroundColorSpan
import android.text.style.LeadingMarginSpan
import android.text.style.MetricAffectingSpan
import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.FontRes
import androidx.core.text.HtmlCompat
import androidx.core.text.parseAsHtml
import androidx.core.graphics.toColorInt
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.utils.font
import id.co.edtslib.uikit.utils.span.OrderedSpan
import org.xml.sax.XMLReader
import java.util.Stack

data class HtmlListConfig(
    val indentDp: Int = 8,
    val bulletGapDp: Int = 8,
    val textSizeSp: Float = 16f,
    val lineSpacing: Float = 1.15f,
    val padding: Int = 24
)

data class ListStyle(
    val bulletChar: String = "•",
    val numberFormat: String = "%d.",
    val indentDp: Int = 8,
    val gapDp: Int = 8,
    val textColor: Int? = null,
    val backgroundColor: Int? = null
)

data class FontStyle(
    val fontFamily: Typeface? = null,
    val textColor: Int? = null,
    val textSize: Float? = null,
    val style: Int = Typeface.NORMAL
)

// ===== Font Manager with Extension Functions =====

class FontManager(private val context: Context) {
    fun loadFont(@FontRes fontRes: Int): Typeface? {
        return context.font(fontRes)
    }

    @SuppressLint("DiscouragedApi")
    fun loadFontByName(fontName: String): Typeface? {
        val fontRes = context.resources.getIdentifier(fontName, "font", context.packageName)
        return if (fontRes != 0) loadFont(fontRes) else null
    }
}

// Extension functions for common fonts
fun FontManager.interBlack(): Typeface? = loadFont(R.font.inter_black)
fun FontManager.interBold(): Typeface? = loadFont(R.font.inter_bold)
fun FontManager.interRegular(): Typeface? = loadFont(R.font.inter)

// Extension functions for creating font styles
fun FontManager.boldStyle(color: Int? = null): FontStyle = FontStyle(
    fontFamily = interBold(),
    textColor = color,
    style = Typeface.BOLD
)

fun FontManager.strongStyle(color: Int? = null): FontStyle = FontStyle(
    fontFamily = interBlack(),
    textColor = color,
    style = Typeface.BOLD
)

// Extension functions for creating list styles
fun ListStyle.withCustomBullet(bullet: String): ListStyle = copy(bulletChar = bullet)
fun ListStyle.withColor(color: Int): ListStyle = copy(textColor = color)
fun ListStyle.withIndent(dp: Int): ListStyle = copy(indentDp = dp)
fun ListStyle.withGap(dp: Int): ListStyle = copy(gapDp = dp)

// Predefined list styles
object ListStyles {
    val default = ListStyle()
    val arrow = ListStyle(bulletChar = "→")
    val star = ListStyle(bulletChar = "★")
    val numbered = ListStyle(numberFormat = "%d.")
    val parenthesized = ListStyle(numberFormat = "(%d)")
    val romanNumerals = ListStyle(numberFormat = "%s.") // Custom handling needed

    fun green() = default.withColor("#2E7D32".toColorInt())
    fun blue() = default.withColor("#1565C0".toColorInt())
    fun red() = default.withColor("#D32F2F".toColorInt())
}

// ===== HTML Processing Extensions =====

// Extension function for HTML preprocessing
fun String.preprocessListTags(): String {
    val mappings = mapOf(
        "<ul>" to "<myul>",
        "</ul>" to "</myul>",
        "<ol>" to "<myol>",
        "</ol>" to "</myol>",
        "<li>" to "<myli>",
        "</li>" to "</myli>"
    )

    return mappings.entries.fold(this) { html, (original, replacement) ->
        html.replace(original, replacement, ignoreCase = true)
    }
}

// Extension function for custom tag mappings
fun String.withCustomTags(mappings: Map<String, String>): String {
    return mappings.entries.fold(this) { html, (original, replacement) ->
        html.replace(original, replacement, ignoreCase = true)
    }
}

// Extension function for HTML parsing
fun String.toSpanned(
    tagHandler: Html.TagHandler? = null,
    flags: Int = HtmlCompat.FROM_HTML_MODE_LEGACY
): Spanned = this.parseAsHtml(flags, null, tagHandler)

// ===== TextView Extensions =====

// Extension functions for TextView configuration
fun TextView.applyHtmlConfig(config: HtmlListConfig): TextView = apply {
    setLineSpacing(0f, config.lineSpacing)
    textSize = config.textSizeSp
    setPadding(config.padding, config.padding, config.padding, config.padding)
}

fun TextView.renderHtml(
    html: String,
    renderer: HtmlRenderer
): TextView = apply {
    text = renderer.render(html, this)
}

// Utility extension
fun TextView.dpToPx(dp: Int): Int =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        resources.displayMetrics
    ).toInt()

class HtmlRendererConfig(
    val listConfig: HtmlListConfig = HtmlListConfig(),
    val unorderedStyle: ListStyle = ListStyles.default,
    val orderedStyle: ListStyle = ListStyles.default,
    val fontStyles: Map<String, FontStyle> = emptyMap(),
    val customTagMappings: Map<String, String> = emptyMap()
) {
    // Factory methods for common configurations
    companion object {
        fun minimal() = HtmlRendererConfig()

        fun styled() = HtmlRendererConfig(
            unorderedStyle = ListStyles.arrow.withColor("#2E7D32".toColorInt()),
            orderedStyle = ListStyles.parenthesized.withColor("#1565C0".toColorInt())
        )

        fun withCustomFonts(fontManager: FontManager) = HtmlRendererConfig(
            fontStyles = mapOf(
                "b" to fontManager.boldStyle("#D32F2F".toColorInt()),
                "strong" to fontManager.strongStyle("#7B1FA2".toColorInt())
            )
        )
    }
}

// ===== Simplified Tag Handler =====

class TagHandler(
    private val textView: TextView,
    private val config: HtmlRendererConfig,
    private val fontManager: FontManager
) : Html.TagHandler {

    private val listStack = Stack<String>()
    private val olCounters = Stack<Int>()
    private val liStartIndices = Stack<Int>()
    private val fontTagStack = Stack<Pair<String, Int>>()

    override fun handleTag(opening: Boolean, tag: String, output: Editable, xmlReader: XMLReader) {
        when (tag.lowercase().trim()) {
            "myul" -> handleListContainer(opening, "ul")
            "myol" -> handleListContainer(opening, "ol")
            "myli" -> handleListItem(textView.textSize, opening, output)
            else -> handleFontTag(opening, tag, output)
        }
    }

    private fun handleListContainer(opening: Boolean, listType: String) {
        if (opening) {
            listStack.push(listType)
            if (listType == "ol") olCounters.push(0)
        } else {
            if (listStack.isNotEmpty()) listStack.pop()
            if (listType == "ol" && olCounters.isNotEmpty()) olCounters.pop()
        }
    }

    private fun handleListItem(textSize: Float, opening: Boolean, output: Editable) {
        if (opening) {
            liStartIndices.push(output.length)
        } else {
            processListItemEnd(textSize, output)
        }
    }

    private fun handleFontTag(opening: Boolean, tag: String, output: Editable) {
        val fontStyle = config.fontStyles[tag.lowercase()]
        if (fontStyle != null) {
            if (opening) {
                fontTagStack.push(tag.lowercase() to output.length)
            } else {
                applyFontSpan(output, fontStyle)
            }
        }
    }

    private fun applyFontSpan(output: Editable, fontStyle: FontStyle) {
        if (fontTagStack.isEmpty()) return

        val (_, start) = fontTagStack.pop()
        val end = output.length

        fontStyle.fontFamily?.let { typeface ->
            output.setSpan(
                TypefaceSpan(typeface),
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        fontStyle.textColor?.let { color ->
            output.setSpan(
                ForegroundColorSpan(color),
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        fontStyle.textSize?.let { size ->
            output.setSpan(
                AbsoluteSizeSpan(size.toInt(), true),
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun processListItemEnd(textSize: Float, output: Editable) {
        if (liStartIndices.isEmpty()) return

        val start = liStartIndices.pop()
        var end = output.length

        if (end == 0 || output[end - 1] != '\n') {
            output.insert(end, "\n")
            end = output.length
        }

        val listType = listStack.takeIf { it.isNotEmpty() }?.peek() ?: return

        when (listType) {
            "ul" -> applyUnorderedListStyle(output, start, end)
            "ol" -> applyOrderedListStyle(textSize, output, start, end)
        }
    }

    private fun applyUnorderedListStyle(output: Editable, start: Int, end: Int) {
        val style = config.unorderedStyle
        val baseIndent = textView.dpToPx(style.indentDp)
        val gap = textView.dpToPx(style.gapDp)

        output.applyIndentSpan(start, end, baseIndent)
        output.applyBulletSpan(start, end, gap)
        output.applyListColors(start, end, style)
    }

    private fun applyOrderedListStyle(textSize: Float, output: Editable, start: Int, end: Int) {
        val style = config.orderedStyle
        val count = incrementOrderedCounter()
        val baseIndent = textView.dpToPx(style.indentDp)

        val numberText = String.format(style.numberFormat, count)

        output.applyIndentSpan(start, end, baseIndent)
        output.applyOrderedSpan(numberText, textSize, start, end, textView.dpToPx(style.gapDp))
        output.applyListColors(start, end, style)
    }

    private fun incrementOrderedCounter(): Int {
        if (olCounters.isEmpty()) return 1
        val current = olCounters.pop() + 1
        olCounters.push(current)
        return current
    }
}

// ===== Editable Extensions for Span Application =====

fun Editable.applyIndentSpan(start: Int, end: Int, indent: Int) {
    setSpan(
        LeadingMarginSpan.Standard(indent, indent),
        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}

fun Editable.applyBulletSpan(start: Int, end: Int, gap: Int) {
    setSpan(
        BulletSpan(gap),
        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}

fun Editable.applyOrderedSpan(character: String, textSize: Float, start: Int, end: Int, gap: Int) {
    setSpan(
        OrderedSpan(
            character = character,
            gapWidth = gap,
            textSizePx = textSize,
        ),
        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}

fun Editable.applyListColors(start: Int, end: Int, style: ListStyle) {
    style.textColor?.let { color ->
        setSpan(
            ForegroundColorSpan(color),
            start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    style.backgroundColor?.let { color ->
        setSpan(
            BackgroundColorSpan(color),
            start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}


class TypefaceSpan(private val typeface: Typeface) : MetricAffectingSpan() {
    override fun updateDrawState(ds: TextPaint) {
        ds.typeface = typeface
    }

    override fun updateMeasureState(paint: TextPaint) {
        paint.typeface = typeface
    }
}

class HtmlRenderer(
    private val config: HtmlRendererConfig,
    private val fontManager: FontManager
) {

    fun render(html: String, textView: TextView): Spanned {
        val processedHtml = html
            .preprocessListTags()
            .withCustomTags(config.customTagMappings)

        val tagHandler = TagHandler(textView, config, fontManager)
        return processedHtml.toSpanned(tagHandler)
    }
}