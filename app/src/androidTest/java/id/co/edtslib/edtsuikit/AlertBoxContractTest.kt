@file:Suppress("DEPRECATION")

package id.co.edtslib.edtsuikit

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Typeface
import android.text.Spanned
import android.text.TextPaint
import android.text.style.URLSpan
import android.text.style.MetricAffectingSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.card.MaterialCardView
import id.co.edtslib.uikit.alert.AlertBox
import id.co.edtslib.uikit.alert.AlertBoxDelegate
import id.co.edtslib.uikit.core.alertbox.CoreAlertBox
import id.co.edtslib.uikit.core.button.CoreButton
import id.co.edtslib.uikit.recipes.alertbox.klik.KlikInfoBox
import id.co.edtslib.uikit.recipes.alertbox.poinku.PoinkuAlertBox
import id.co.edtslib.uikit.utils.buildHighlightedMessage
import id.co.edtslib.uikit.utils.drawable
import id.co.edtslib.uikit.utils.font
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.TextStyle
import id.co.edtslib.uikit.utils.html.TypefaceSpan
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import id.co.edtslib.uikit.R as UIKitR

@RunWith(AndroidJUnit4::class)
class AlertBoxContractTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    private fun context(scale: Float = 1f): Context {
        val target = instrumentation.targetContext
        val configuration = Configuration(target.resources.configuration).apply { fontScale = scale }
        return ContextThemeWrapper(target.createConfigurationContext(configuration), UIKitR.style.Theme_EDTS_UIKit)
    }

    private fun measure(alert: CoreAlertBox) {
        val width = (328 * alert.resources.displayMetrics.density).toInt()
        alert.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        alert.layout(0, 0, alert.measuredWidth, alert.measuredHeight)
    }

    private fun captureCard(card: MaterialCardView, name: String) {
        val density = card.resources.displayMetrics.density
        val bitmap = Bitmap.createBitmap((card.width / density).toInt(), (card.height / density).toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.scale(1f / density, 1f / density)
        card.draw(canvas)
        instrumentation.targetContext.filesDir.resolve(name + ".png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }

    @Test
    fun productRecipesMatchInspectedFigmaColorsGeometryAndFooterSizes() {
        instrumentation.runOnMainSync {
            val klik = listOf(
                Triple(UIKitR.style.Widget_EDTS_UIKit_AlertBox_Klik_Border_General, "#F8FBFC", "#DCDEE3"),
                Triple(UIKitR.style.Widget_EDTS_UIKit_AlertBox_Klik_Border_Info, "#F2F9FF", "#1178D4"),
                Triple(UIKitR.style.Widget_EDTS_UIKit_AlertBox_Klik_Border_Success, "#EBFFD0", "#578F21"),
                Triple(UIKitR.style.Widget_EDTS_UIKit_AlertBox_Klik_Border_Error, "#FFEDEE", "#BB0000"),
                Triple(UIKitR.style.Widget_EDTS_UIKit_AlertBox_Klik_Border_Warning, "#FFF0E6", "#FF7D1D")
            )
            for ((style, fill, stroke) in klik) {
                val alert = KlikInfoBox(context()).apply {
                    alertRecipe = style
                    text = "If the text has 2 rows, make sure the icons is always aligned to the top."
                    isButtonVisible = true
                    buttonText = "Button"
                }
                measure(alert)
                val card = alert.getChildAt(0) as MaterialCardView
                val body = alert.findViewById<TextView>(UIKitR.id.tvAlertMessage)
                val density = alert.resources.displayMetrics.density
                if (style == UIKitR.style.Widget_EDTS_UIKit_AlertBox_Klik_Border_General) captureCard(card, "alertbox_klik")
                assertEquals(Color.parseColor(fill), card.cardBackgroundColor.defaultColor)
                assertEquals(Color.parseColor(stroke), card.strokeColorStateList!!.defaultColor)
                assertEquals(Color.parseColor("#434755"), body.currentTextColor)
                assertEquals(92f, card.height / density, 1.1f)
                assertEquals(28f, alert.findViewById<CoreButton>(UIKitR.id.btnAction).height / density, 0.6f)
                assertEquals(android.view.Gravity.CENTER, alert.findViewById<CoreButton>(UIKitR.id.btnAction).gravity)
                assertEquals(16f, alert.findViewById<View>(UIKitR.id.ivAlertIcon).height / density, 0.6f)
            }
            val poinku = listOf(
                Triple(UIKitR.style.Widget_EDTS_UIKit_AlertBox_Poinku_Success, "#F6FFEB", "#8FC742"),
                Triple(UIKitR.style.Widget_EDTS_UIKit_AlertBox_Poinku_Warning, "#FFF0E6", "#FF7D1D"),
                Triple(UIKitR.style.Widget_EDTS_UIKit_AlertBox_Poinku_Error, "#FFEDEE", "#EE2B2E"),
                Triple(UIKitR.style.Widget_EDTS_UIKit_AlertBox_Poinku_Info, "#E7F1FD", "#1178D4")
            )
            for ((style, fill, content) in poinku) {
                val alert = PoinkuAlertBox(context()).apply {
                    alertRecipe = style
                    text = "This is an example of long text, please make sure that it’ll be shown in two lines."
                }
                measure(alert)
                val card = alert.getChildAt(0) as MaterialCardView
                if (style == UIKitR.style.Widget_EDTS_UIKit_AlertBox_Poinku_Success) captureCard(card, "alertbox_poinku")
                assertEquals(Color.parseColor(fill), card.cardBackgroundColor.defaultColor)
                assertEquals(Color.parseColor(content), card.strokeColorStateList!!.defaultColor)
                assertEquals(Color.parseColor(content), alert.findViewById<TextView>(UIKitR.id.tvAlertMessage).currentTextColor)
                assertEquals(48f, card.height / alert.resources.displayMetrics.density, 1.1f)
                alert.isButtonVisible = true
                alert.buttonText = "Button"
                measure(alert)
                assertEquals(24f, alert.findViewById<CoreButton>(UIKitR.id.btnAction).height / alert.resources.displayMetrics.density, 0.6f)
            }
            val full = KlikInfoBox(context()).apply { alertRecipe = UIKitR.style.Widget_EDTS_UIKit_AlertBox_Klik_FullWidth_General }
            val fullCard = full.getChildAt(0) as MaterialCardView
            assertEquals(0, fullCard.strokeWidth)
            assertEquals(0f, fullCard.radius, 0f)
        }
    }

    @Test
    fun recipeChangesKeepContentStateAndCallbackOwnership() {
        instrumentation.runOnMainSync {
            var closes = 0
            var buttons = 0
            var actions = 0
            val alert = KlikInfoBox(context()).apply {
                text = "Notice"
                buttonText = "Continue"
                actionText = "Edit"
                isButtonVisible = true
                isActionVisible = true
                delegate = object : AlertBoxDelegate {
                    override fun onCloseClickListener(view: View) { closes++ }
                    override fun onButtonClickListener(view: View) { buttons++ }
                    override fun onActionClickListener(view: View) { actions++ }
                }
                alertRecipe = UIKitR.style.Widget_EDTS_UIKit_AlertBox_Klik_Border_Error
            }
            measure(alert)
            assertEquals("Notice", alert.text.toString())
            alert.findViewById<View>(UIKitR.id.ivAlertClose).performClick()
            alert.findViewById<View>(UIKitR.id.btnAction).performClick()
            alert.findViewById<View>(UIKitR.id.tvAlertAction).performClick()
            assertEquals(listOf(1, 1, 1), listOf(closes, buttons, actions))
            assertEquals(View.VISIBLE, alert.visibility)
            alert.isEnabled = false
            alert.findViewById<View>(UIKitR.id.ivAlertClose).performClick()
            alert.findViewById<View>(UIKitR.id.btnAction).performClick()
            alert.findViewById<View>(UIKitR.id.tvAlertAction).performClick()
            assertEquals(listOf(1, 1, 1), listOf(closes, buttons, actions))
        }
    }

    @Test
    fun tmhActionAndIconAlignmentWorkWithRtlAndNullableTint() {
        instrumentation.runOnMainSync {
            val alert = AlertBox(context()).apply {
                text = "A long promotion message that has room to wrap over several lines."
                isCloseIconVisible = false
                isActionVisible = true
                actionText = "Edit"
                actionMaxLines = 2
                actionEndIcon = context.drawable(UIKitR.drawable.ic_chevron_right_16)
                actionEndIconTint = Color.GREEN
                startIconVerticalAlignment = AlertBox.StartIconVerticalAlignment.CENTER
                layoutDirection = View.LAYOUT_DIRECTION_RTL
            }
            measure(alert)
            val icon = alert.findViewById<View>(UIKitR.id.ivAlertIcon)
            val body = alert.findViewById<TextView>(UIKitR.id.tvAlertMessage)
            assertEquals(body.top + body.height / 2f, icon.top + icon.height / 2f, 1f)
            assertTrue(icon.left > body.left)
            alert.startIconVerticalAlignment = AlertBox.StartIconVerticalAlignment.BOTTOM
            measure(alert)
            assertEquals(body.bottom, icon.bottom)
            alert.actionEndIcon = null
            alert.actionEndIconTint = null
            alert.showOpticalSpacing(false)
            alert.startIconVerticalAlignment = AlertBox.StartIconVerticalAlignment.TOP
            measure(alert)
            assertEquals(body.top, icon.top)
        }
    }

    @Test
    fun htmlEmphasisKeepsRepeatedTextEntitiesLinksListsAndRequestedTypeface() {
        instrumentation.runOnMainSync {
            val alert = KlikInfoBox(context()).apply {
                text = "<p><b>Rp10.000</b> &amp; <strong>Rp10.000</strong> <i>bonus</i> <b><i>Double</i></b><br><a href='https://example.com/details'>Details</a></p><ol><li>First</li><li>Second</li></ol>"
                isHtml = true
            }
            val body = alert.findViewById<TextView>(UIKitR.id.tvAlertMessage)
            val parsed = body.text as Spanned
            assertTrue(parsed.toString().contains("Rp10.000 & Rp10.000 bonus"))
            assertTrue(parsed.toString().contains("First"))
            assertTrue(parsed.toString().contains("Second"))
            assertEquals("https://example.com/details", parsed.getSpans(0, parsed.length, URLSpan::class.java).single().url)
            val spans = parsed.getSpans(0, parsed.length, TypefaceSpan::class.java)
            val starts = listOf(parsed.toString().indexOf("Rp10.000"), parsed.toString().lastIndexOf("Rp10.000"))
            for (start in starts) {
                val emphasis = spans.last { parsed.getSpanStart(it) == start && parsed.getSpanEnd(it) == start + 8 }
                val paint = TextPaint()
                emphasis.updateMeasureState(paint)
                assertEquals(context().font(UIKitR.font.inter_semibold_fallback), paint.typeface)
            }
            val italicStart = parsed.toString().indexOf("Double")
            val italicPaint = TextPaint(body.paint)
            parsed.getSpans(italicStart, italicStart + 6, MetricAffectingSpan::class.java)
                .forEach { it.updateMeasureState(italicPaint) }
            assertTrue(italicPaint.typeface.isItalic)
            assertNotNull(body.movementMethod)
            val poinku = PoinkuAlertBox(context()).apply {
                text = "<b>Bonus</b>"
                isHtml = true
            }
            poinku.alertRecipe = UIKitR.style.Widget_EDTS_UIKit_AlertBox_Poinku_Error
            val poinkuText = poinku.findViewById<TextView>(UIKitR.id.tvAlertMessage).text as Spanned
            val poinkuColor = poinkuText.getSpans(
                0, poinkuText.length, android.text.style.ForegroundColorSpan::class.java
            ).last().foregroundColor
            assertEquals(Color.parseColor("#EE2B2E"), poinkuColor)
            val poinkuPaint = TextPaint()
            poinkuText.getSpans(0, poinkuText.length, TypefaceSpan::class.java).last()
                .updateMeasureState(poinkuPaint)
            assertEquals(context().font(UIKitR.font.rubik_semibold_fallback), poinkuPaint.typeface)
            val plain = buildHighlightedMessage(context(), "Open Open", highlightedMessages = listOf("Open", "Open"),
                highlightedTextAppearance = listOf(TextStyle(Color.BLUE, UIKitR.font.button_klik_inter_semibold, 12f, 16f)))
            assertEquals("Open Open", plain.toString())
        }
    }

    @Test
    fun xmlRecipesAndLegacyInflationRetainExplicitOverridesAndEnumValues() {
        instrumentation.runOnMainSync {
            val root = LayoutInflater.from(context()).inflate(R.layout.view_alert_box_contract, null)
            val klik = root.findViewById<KlikInfoBox>(R.id.abContractKlik)
            assertEquals("From XML", klik.text.toString())
            assertEquals(context().color(android.R.color.holo_purple), klik.findViewById<TextView>(UIKitR.id.tvAlertMessage).currentTextColor)
            val poinku = root.findViewById<PoinkuAlertBox>(R.id.abContractPoinku)
            assertEquals(UIKitR.style.Widget_EDTS_UIKit_AlertBox_Poinku_Warning, poinku.alertRecipe)
            assertFalse(poinku.isEnabled)
            assertFalse(poinku.findViewById<View>(UIKitR.id.ivAlertClose).isEnabled)
            val legacy = root.findViewById<AlertBox>(R.id.abContractLegacy)
            assertEquals(AlertBox.AlertType.WARNING, legacy.alertType)
            assertEquals(listOf(0, 1, 2, 3, 4), AlertBox.AlertType.values().map { it.ordinal })
            legacy.showOpticalSpacing()
            assertTrue(legacy.isButtonVisible)
            for (alert in listOf(klik, legacy)) {
                assertEquals("Manage", alert.actionText.toString())
                assertTrue(alert.isActionVisible)
                assertEquals(2, alert.actionMaxLines)
                assertEquals(android.text.TextUtils.TruncateAt.MIDDLE, alert.actionEllipsize)
                assertNotNull(alert.actionEndIcon)
                assertEquals(context().color(android.R.color.holo_purple), alert.actionEndIconTint)
                assertEquals((4 * alert.resources.displayMetrics.density).toInt(), alert.actionIconPadding)
                val actions = arrayListOf<View>()
                alert.findViewsWithText(actions, "Manage", View.FIND_VIEWS_WITH_TEXT)
                assertEquals("Manage", (actions.single() as TextView).text.toString())
            }
        }
    }

    @Test
    fun largeFontsGrowAndCloseTouchTargetHasButtonSemantics() {
        instrumentation.runOnMainSync {
            val small = PoinkuAlertBox(context()).apply { text = "A long accessible message that wraps over more than one line." }
            val large = PoinkuAlertBox(context(1.6f)).apply { text = small.text }
            measure(small)
            measure(large)
            assertTrue(large.height > small.height)
        }
        ActivityScenario.launch(GuidelinesAlertBoxActivity::class.java).use { scenario ->
            lateinit var close: View
            scenario.onActivity { activity ->
                val alert = PoinkuAlertBox(activity).apply {
                    text = "Notice"
                    delegate = object : AlertBoxDelegate {
                        override fun onCloseClickListener(view: View) { view.tag = "closed" }
                        override fun onButtonClickListener(view: View) = Unit
                    }
                }
                activity.setContentView(alert)
                measure(alert)
                close = alert.findViewById<View>(UIKitR.id.ivAlertClose)
                val node = close.createAccessibilityNodeInfo()
                assertEquals("android.widget.ImageButton", node.className.toString())
                assertEquals("Close message", node.contentDescription.toString())
                val now = android.os.SystemClock.uptimeMillis()
                val x = alert.width - 2f
                val y = alert.height / 2f
                alert.dispatchTouchEvent(MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, x, y, 0))
                alert.dispatchTouchEvent(MotionEvent.obtain(now, now + 10, MotionEvent.ACTION_UP, x, y, 0))
            }
            scenario.onActivity { assertEquals("closed", close.tag) }
        }
    }

    @Test
    fun regularProductFontsDoNotFallBackToSystemTypeface() {
        instrumentation.runOnMainSync {
            for (alert in listOf(KlikInfoBox(context()), PoinkuAlertBox(context()))) {
                assertNotEquals(Typeface.DEFAULT, alert.findViewById<TextView>(UIKitR.id.tvAlertMessage).typeface)
            }
        }
    }
}
