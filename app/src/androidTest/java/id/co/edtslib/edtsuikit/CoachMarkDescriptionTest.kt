package id.co.edtslib.edtsuikit

import android.content.Intent
import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.text.style.MetricAffectingSpan
import android.text.style.UnderlineSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import id.co.edtslib.uikit.R
import id.co.edtslib.uikit.coachmark.CoachMarkData
import id.co.edtslib.uikit.coachmark.CoachMarkOverlay
import id.co.edtslib.uikit.utils.font
import id.co.edtslib.uikit.utils.html.TypefaceSpan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.FutureTask

@RunWith(AndroidJUnit4::class)
class CoachMarkDescriptionTest {

    @Test
    fun htmlStylesOnlyTaggedOccurrencesAndPreservesLinks() {
        withActivity { activity ->
            val label = CoachMarkOverlay.Builder(activity)
                .setContainer(FrameLayout(activity))
                .setCoachmarkDescriptionHtmlEnabled(true)
                .setCoachmarkDescriptionTextAppearance(R.style.TextAppearance_Inter_Regular_P1)
                .setCoachMarkItems(
                    CoachMarkData(
                        target = View(activity),
                        title = "Quantity",
                        description = """Karton &amp; <b>Karton</b> atau <a href="https://example.com"><strong><em>Satuan</em></strong></a>"""
                    )
                )
                .build()
                .findViewById<TextView>(R.id.tvDescription)

            val text = label.text as Spanned
            assertEquals("Karton & Karton atau Satuan", text.toString())
            val emphasizedWords = text.getSpans(0, text.length, TypefaceSpan::class.java)
                .sortedBy { text.getSpanStart(it) }
            assertEquals(listOf(9, 21), emphasizedWords.map { text.getSpanStart(it) })
            assertEquals(listOf(15, 27), emphasizedWords.map { text.getSpanEnd(it) })
            emphasizedWords.forEach { span ->
                val paint = TextPaint(label.paint)
                span.updateDrawState(paint)
                assertEquals(label.context.font(R.font.inter_semibold), paint.typeface)
            }
            val nestedPaint = TextPaint(label.paint)
            text.getSpans(21, 27, MetricAffectingSpan::class.java)
                .forEach { it.updateDrawState(nestedPaint) }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                assertTrue(nestedPaint.typeface.isItalic)
                assertEquals(600, nestedPaint.typeface.weight)
            } else {
                assertTrue(nestedPaint.textSkewX < 0f)
            }
            assertEquals(
                label.resources.getDimension(R.dimen.p1_text_size),
                label.textSize,
                0.5f
            )
            assertTrue(text.getSpans(0, text.length, AbsoluteSizeSpan::class.java).isEmpty())
            assertEquals(
                "https://example.com",
                text.getSpans(0, text.length, URLSpan::class.java).single().url
            )
            assertSame(LinkMovementMethod.getInstance(), label.movementMethod)
        }
    }

    @Test
    fun htmlCanOverrideEmphasisSizeWithoutChangingTheBaseAppearance() {
        withActivity { activity ->
            val item = CoachMarkData(View(activity), "Quantity", "Pilih <b>Karton</b>.")
            val label = CoachMarkOverlay.Builder(activity)
                .setContainer(FrameLayout(activity))
                .setCoachmarkDescriptionHtmlEnabled(true)
                .setCoachmarkDescriptionEmphasisTextAppearance(R.style.TextAppearance_Inter_SemiBold_P1)
                .setCoachMarkItems(item)
                .build()
                .findViewById<TextView>(R.id.tvDescription)

            val text = label.text as Spanned
            val span = text.getSpans(0, text.length, AbsoluteSizeSpan::class.java).single()
            assertEquals(label.resources.getDimensionPixelSize(R.dimen.p1_text_size), span.size)
            assertEquals(label.resources.getDimension(R.dimen.p2_text_size), label.textSize, 0.5f)
            assertEquals("Karton", text.subSequence(text.getSpanStart(span), text.getSpanEnd(span)).toString())
        }
    }

    @Test
    fun plainTextAndExistingSpansKeepTheirInputFormatting() {
        withActivity { activity ->
            val item = CoachMarkData(View(activity), "Quantity", "<b>Karton</b>")
            val overlay = CoachMarkOverlay.Builder(activity)
                .setContainer(FrameLayout(activity))
                .setCoachMarkItems(item)
                .build()
            val label = overlay.findViewById<TextView>(R.id.tvDescription)
            assertEquals("<b>Karton</b>", label.text.toString())

            val underlined = SpannableString("Satuan").apply {
                setSpan(UnderlineSpan(), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            overlay.setCoachMarkItems(listOf(item.copy(description = underlined)))
            val text = label.text as Spanned
            assertEquals("Satuan", text.toString())
            val span = text.getSpans(0, text.length, UnderlineSpan::class.java).single()
            assertEquals(0, text.getSpanStart(span))
            assertEquals(text.length, text.getSpanEnd(span))
        }
    }

    private fun withActivity(action: (GuidelineCoachmarkActivity) -> Unit) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val intent = Intent(instrumentation.targetContext, GuidelineCoachmarkActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val activity = instrumentation.startActivitySync(intent) as GuidelineCoachmarkActivity
        val test = FutureTask { action(activity) }
        try {
            instrumentation.runOnMainSync(test)
            test.get()
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
            instrumentation.waitForIdleSync()
        }
    }
}
