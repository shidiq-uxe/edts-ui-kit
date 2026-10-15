package id.co.edtslib.edtsuikit.utils

import android.graphics.Typeface
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.MetricAffectingSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import id.co.edtslib.uikit.utils.TextStyleKey
import id.co.edtslib.uikit.utils.TextStyleProvider.get
import id.co.edtslib.uikit.utils.buildHighlightedMessage
import id.co.edtslib.uikit.utils.font
import id.co.edtslib.uikit.utils.html.TypefaceSpan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import id.co.edtslib.uikit.R as UIKitR

@RunWith(AndroidJUnit4::class)
class HighlightedMessageTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val emphasis = TextStyleKey.P2_SEMIBOLD.get(context, UIKitR.color.black_70)

    private fun assertSemibold(text: Spanned, start: Int, end: Int) {
        val span = text.getSpans(start, end, TypefaceSpan::class.java)
            .last { text.getSpanStart(it) == start && text.getSpanEnd(it) == end }
        val paint = TextPaint()
        span.updateMeasureState(paint)
        assertSame(context.font(UIKitR.font.inter_semibold), paint.typeface)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            assertEquals(600, paint.typeface.weight)
        }
    }

    @Test
    fun realHtmlPreservesRangesEntitiesItalicAndLinks() {
        val result = buildHighlightedMessage(
            context = context,
            htmlMessage = "2 tersedia: Sudah ambil <b>2</b> dari <strong>2</strong> tebus murah &amp; " +
                "<b><i>Paragon</i></b><br><a href=\"https://example.com/syarat\">Lihat Syarat</a>",
            highlightedTextAppearance = emphasis,
            defaultTextAppearance = TextStyleKey.P2.get(context, UIKitR.color.black_60),
        )
        assertEquals("2 tersedia: Sudah ambil 2 dari 2 tebus murah & Paragon\nLihat Syarat", result.toString())
        for (word in listOf("2 dari", "2 tebus", "Paragon")) {
            val start = result.indexOf(word)
            val length = if (word == "Paragon") word.length else 1
            assertSemibold(result, start, start + length)
        }
        val links = result.getSpans(0, result.length, URLSpan::class.java)
        assertEquals(1, links.size)
        assertEquals("https://example.com/syarat", links.single().url)
        assertEquals(1, result.getSpans(0, result.length, ClickableSpan::class.java).size)
        assertTrue(result.getSpans(0, result.length, StyleSpan::class.java)
            .any { it.style and Typeface.ITALIC != 0 })
        assertTrue(result.getSpans(0, result.length, StyleSpan::class.java)
            .none { it.style and Typeface.BOLD != 0 })
        val paragon = result.indexOf("Paragon")
        val paint = TextPaint()
        result.getSpans(paragon, paragon + 7, MetricAffectingSpan::class.java)
            .forEach { it.updateMeasureState(paint) }
        assertTrue(paint.typeface.isItalic || paint.textSkewX != 0f)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            assertEquals(600, paint.typeface.weight)
        }
    }

    @Test
    fun legacyHighlightKeepsItsClickActionAndExactFont() {
        var clicks = 0
        val result = buildHighlightedMessage(
            context = context,
            message = "Lihat syarat promo",
            highlightedMessages = listOf("syarat", "absent"),
            highlightedTextAppearance = listOf(emphasis),
            highlightClickAction = listOf({ clicks++ }, {}),
        )
        assertEquals("Lihat syarat promo", result.toString())
        assertSemibold(result, 6, 12)
        result.getSpans(6, 12, ClickableSpan::class.java).single().onClick(View(context))
        assertEquals(1, clicks)
    }
}
