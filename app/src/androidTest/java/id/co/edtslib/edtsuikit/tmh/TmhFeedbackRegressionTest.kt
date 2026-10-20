package id.co.edtslib.edtsuikit.tmh

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.SystemClock
import android.text.Spanned
import android.text.TextPaint
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import id.co.edtslib.edtsuikit.GuidelinesTmhActivity
import id.co.edtslib.edtsuikit.R
import id.co.edtslib.edtsuikit.demo.promo.ParagonFairDemoActivity
import id.co.edtslib.uikit.stepper.StepperButton
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.font
import id.co.edtslib.uikit.utils.html.TypefaceSpan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import id.co.edtslib.uikit.R as UIKitR

@RunWith(AndroidJUnit4::class)
class TmhFeedbackRegressionTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    private fun settle() {
        SystemClock.sleep(2200)
        instrumentation.waitForIdleSync()
    }

    private fun capture(name: String) {
        if (InstrumentationRegistry.getArguments().getString("captureTmhFeedback") != "true") return
        val directory = File(context.getExternalFilesDir(null), "tmh-feedback-fixed").apply { mkdirs() }
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        File(directory, "$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }

    private fun descendants(view: View): List<View> = listOf(view) +
        if (view is ViewGroup) {
            (0 until view.childCount).flatMap { descendants(view.getChildAt(it)) }
        } else emptyList()

    private fun assertSemibold(text: Spanned, value: String) {
        val start = text.indexOf(value)
        val end = start + value.length
        val span = text.getSpans(start, end, TypefaceSpan::class.java)
            .last { text.getSpanStart(it) == start && text.getSpanEnd(it) == end }
        val paint = TextPaint()
        span.updateMeasureState(paint)
        assertSame(context.font(UIKitR.font.inter_semibold), paint.typeface)
    }

    @Test
    fun eligibleArtworkKeepsColorAndUnavailableFiltersCanBeCleared() {
        for (id in listOf(R.drawable.tmh_product_soup_bowl, R.drawable.tmh_product_noodle_bowl,
                R.drawable.tmh_product_softex_gravity, R.drawable.tmh_product_softex_beary)) {
            val bitmap = BitmapFactory.decodeResource(context.resources, id)
            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            assertTrue("${context.resources.getResourceEntryName(id)} must retain original color", pixels.any {
                val red = it shr 16 and 255
                val green = it shr 8 and 255
                val blue = it and 255
                it ushr 24 > 16 && maxOf(red, green, blue) - minOf(red, green, blue) > 10
            })
            bitmap.recycle()
            instrumentation.runOnMainSync {
                val image = ImageView(context)
                image.setImageResource(id)
                image.setUnavailable(true)
                assertTrue(image.colorFilter != null)
                image.setUnavailable(false)
                assertEquals(null, image.colorFilter)
            }
        }
    }

    @Test
    fun alertCopyDistinguishesPartialFullAndUnmetWithHtmlEmphasis() {
        val flow = TmhFlowState.create(context, TmhScenario.DEFAULT)
        val partial = flow.fair("fair-0").alertText(context) as Spanned
        assertEquals("Sudah ambil 1 dari 2 tebus murah", partial.toString())
        assertSemibold(partial, "1")
        assertSemibold(partial, "2")
        assertEquals("Sudah ambil 2 tebus murah", flow.fair("fair-1").alertText(context).toString())
        val unmet = flow.fair("fair-3").alertText(context) as Spanned
        assertEquals(30_000L, flow.fair("fair-3").remainingSpend)
        val remaining = flow.fair("fair-3").remainingSpend.rupiahText()
        assertEquals("Tambah $remaining untuk ambil promo tebus murah ini.", unmet.toString())
        assertSemibold(unmet, remaining)
        val eligible = flow.fair("fair-2").alertText(context) as Spanned
        assertEquals("Bisa ambil 2 tebus murah", eligible.toString())
        assertSemibold(eligible, "2")
    }

    @Test
    fun pppChipsStayInsideTheirPanelAndProgressRespectsAllRestrictions() {
        val initial = TmhFlowState.create(context, TmhScenario.DEFAULT)
        for (id in listOf("fair-1", "fair-0", "fair-4", "fair-5", "fair-6", "fair-7", "fair-3")) {
            var flow = initial
            if (id == "fair-3") {
                val model = flow.modelFor(id)
                flow = flow.withModel(id, model.withQuantity(model.products.first().id, 10))
            }
            val fair = flow.fair(id)
            val intent = Intent(context, ParagonFairDemoActivity::class.java)
                .putExtra(TmhFlowState.EXTRA_FLOW, flow.toBundle())
                .putExtra(TmhFlowState.EXTRA_FAIR_ID, id)
            ActivityScenario.launch<ParagonFairDemoActivity>(intent).use { scenario ->
                settle()
                scenario.onActivity { screen ->
                    assertEquals(View.GONE, screen.findViewById<View>(R.id.skeleton).visibility)
                    val panel = screen.findViewById<ViewGroup>(R.id.progressPanel)
                    assertTrue(panel.isShown)
                    val badge = panel.findViewById<View>(R.id.fairBadge)
                    val quota = panel.findViewById<View>(R.id.quotaBadge)
                    val info = panel.findViewById<View>(R.id.info)
                    for (chip in listOf(badge, quota)) {
                        assertTrue("$id chip must remain inside the panel", chip.left >= panel.paddingLeft &&
                            chip.right <= panel.width - panel.paddingRight)
                        val overlapsInfo = chip.left < info.right && chip.right > info.left &&
                            chip.top < info.bottom && chip.bottom > info.top
                        assertFalse("$id chip must not overlap the info action", overlapsInfo)
                    }
                    assertEquals(!fair.state.isUnavailable, panel.findViewById<View>(R.id.progressBar).isEnabled)
                    val compact = screen.findViewById<View>(R.id.compactMeter)
                    assertEquals(!fair.state.isUnavailable, compact.findViewById<View>(R.id.progressBar).isEnabled)
                    if (fair.state.isEligible) {
                        assertEquals(null, screen.findViewById<ImageView>(R.id.giftOne).colorFilter)
                    }
                }
                capture("ppp-$id")
                if (id == "fair-3") {
                    scenario.onActivity { it.findViewById<View>(R.id.giftsAction).performClick() }
                    settle()
                    scenario.onActivity { screen ->
                        val tray = screen.supportFragmentManager.findFragmentByTag(TmhRedemptionBottomSheet.TAG)
                            as TmhRedemptionBottomSheet
                        val images = descendants(tray.requireView()).filterIsInstance<ImageView>()
                            .filter { it.id == R.id.ivProduct }
                        assertEquals(2, images.size)
                        assertTrue(images.all { it.colorFilter == null })
                    }
                    capture("softex-ppp-tray")
                }
            }
        }
    }

    @Test
    fun zeroCountDimsAtTheLimitAndRecoversWhenAllowanceReturnsInBothTrays() {
        for (isPromoDetail in listOf(false, true)) {
            ActivityScenario.launch(GuidelinesTmhActivity::class.java).use { scenario ->
                settle()
                scenario.onActivity { screen ->
                    val fair = TmhFlowState.create(screen, TmhScenario.DEFAULT).fair("fair-2")
                    TmhRedemptionBottomSheet.newInstance(fair, TmhScenario.DEFAULT, isPromoDetail = isPromoDetail)
                        .showNow(screen.supportFragmentManager, TmhRedemptionBottomSheet.TAG)
                }
                settle()
                scenario.onActivity { screen -> controls(screen)[0].binding.plusButton.performClick() }
                settle()
                scenario.onActivity { screen -> controls(screen)[0].binding.plusButton.performClick() }
                settle()
                scenario.onActivity { screen ->
                    val controls = controls(screen)
                    val selected = controls[0]
                    val zero = controls[1]
                    assertEquals(2, selected.getCount())
                    assertTrue(selected.binding.minusButton.isEnabled)
                    assertEquals(screen.color(UIKitR.color.black_60), selected.binding.countText.currentTextColor)
                    assertEquals(0, zero.getCount())
                    assertFalse(zero.binding.plusButton.isEnabled)
                    assertFalse(zero.binding.minusButton.isEnabled)
                    assertEquals(screen.color(UIKitR.color.black_30), zero.binding.countText.currentTextColor)
                }
                capture("zero-count-at-limit-$isPromoDetail")
                scenario.onActivity { screen -> controls(screen)[0].binding.minusButton.performClick() }
                settle()
                scenario.onActivity { screen ->
                    val zero = controls(screen)[1]
                    assertTrue(zero.binding.plusButton.isEnabled)
                    assertEquals(screen.color(UIKitR.color.black_60), zero.binding.countText.currentTextColor)
                }
            }
        }
    }

    private fun controls(screen: GuidelinesTmhActivity): List<StepperButton> {
        val tray = screen.supportFragmentManager.findFragmentByTag(TmhRedemptionBottomSheet.TAG)
            as TmhRedemptionBottomSheet
        return descendants(tray.requireView()).filterIsInstance<StepperButton>()
    }
}
