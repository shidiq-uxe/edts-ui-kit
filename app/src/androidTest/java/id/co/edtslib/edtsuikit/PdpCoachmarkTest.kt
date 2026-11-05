package id.co.edtslib.edtsuikit

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.os.SystemClock
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.CompoundButton
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import id.co.edtslib.edtsuikit.helper.PDPSelectionGroup
import id.co.edtslib.uikit.coachmark.CoachMarkData
import id.co.edtslib.uikit.coachmark.CoachMarkOverlay
import id.co.edtslib.uikit.footer.PDPFooter
import id.co.edtslib.uikit.utils.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs
import id.co.edtslib.uikit.R as UIKitR

@RunWith(AndroidJUnit4::class)
class PdpCoachmarkTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @Test
    fun dismissalRestoresSatuanAndSubtotal() {
        verifyPdpDismissal(isEarlyDismissal = false, usesBack = false)
    }

    @Test
    fun earlyDismissalCancelsPendingKartonSwitch() {
        verifyPdpDismissal(isEarlyDismissal = true, usesBack = false)
    }

    @Test
    fun backDismissalRestoresSatuan() {
        verifyPdpDismissal(isEarlyDismissal = false, usesBack = true)
    }

    @Test
    fun highlightAndCardFollowMovementWithoutRestartingEntrance() {
        withOverlay { host, target, _, overlay ->
            var originalCardY = 0f
            var originalTargetY = 0
            instrumentation.runOnMainSync {
                originalCardY = overlay.getChildAt(0).translationY
                originalTargetY = centerInOverlay(target, overlay).second
                target.translationY = 100.dp
            }
            awaitCondition("Highlight and card must follow the moved target") {
                val center = centerInOverlay(target, overlay)
                isClearAt(overlay, center.first, center.second) &&
                    !isClearAt(overlay, center.first, originalTargetY) &&
                    overlay.getChildAt(0).translationY == originalCardY + 100.dp &&
                    overlay.getChildAt(0).alpha == 1f
            }

            instrumentation.runOnMainSync { target.translationY = 0f }
            awaitCondition("Highlight must follow the target back down") {
                val center = centerInOverlay(target, overlay)
                isClearAt(overlay, center.first, center.second) &&
                    overlay.getChildAt(0).translationY == originalCardY
            }

            instrumentation.runOnMainSync {
                host.removeView(overlay)
                target.translationY = 100.dp
                host.addView(overlay)
            }
            awaitCondition("Tracking must resume after reattachment") {
                val center = centerInOverlay(target, overlay)
                isClearAt(overlay, center.first, center.second) &&
                    overlay.getChildAt(0).translationY == originalCardY + 100.dp
            }

            val rawRect = RectF(40.dp, 60.dp, 160.dp, 120.dp)
            instrumentation.runOnMainSync { overlay.setTargetRect(rawRect) }
            awaitCondition("Raw rectangle entrance must finish") {
                overlay.getChildAt(0).alpha == 1f &&
                    isClearAt(overlay, rawRect.centerX().toInt(), rawRect.centerY().toInt())
            }
            instrumentation.runOnMainSync { target.translationY = 200.dp }
            instrumentation.waitForIdleSync()
            instrumentation.runOnMainSync {
                assertTrue(isClearAt(overlay, rawRect.centerX().toInt(), rawRect.centerY().toInt()))
                val center = centerInOverlay(target, overlay)
                assertTrue(!isClearAt(overlay, center.first, center.second))
            }
        }
    }

    @Test
    fun multipleStepTransitionTracksTheNewTarget() {
        withOverlay { _, _, nextTarget, overlay ->
            instrumentation.runOnMainSync { overlay.showNextCoachMark() }
            awaitCondition("Second target transition must finish") {
                val center = centerInOverlay(nextTarget, overlay)
                overlay.getCurrentCoachmarkIndex() == 1 && isClearAt(overlay, center.first, center.second)
            }
            // Let the morph finish before moving the newly selected target.
            SystemClock.sleep(600)
            instrumentation.runOnMainSync { nextTarget.translationY = 100.dp }
            awaitCondition("Highlight must track the second target after its transition") {
                val center = centerInOverlay(nextTarget, overlay)
                isClearAt(overlay, center.first, center.second)
            }
        }
    }

    @Test
    fun goneScenarioReturnsToSatuanWhileSpotlightStaysVisible() {
        withPdp { activity, _ ->
            dismissCoachmark(activity)
            val overlay = playScenario(activity, shouldKeepSpace = false)
            verifyTimedCycle(activity, overlay, shouldKeepSpace = false)
        }
    }

    @Test
    fun invisibleScenarioKeepsPositionsAndOnlyAppliesOnPlay() {
        withPdp { activity, _ ->
            dismissCoachmark(activity)
            val overlay = playScenario(activity, shouldKeepSpace = true)
            verifyTimedCycle(activity, overlay, shouldKeepSpace = true)
            dismissCoachmark(activity)
            instrumentation.runOnMainSync {
                val selection = activity.findViewById<PDPSelectionGroup>(R.id.pdpSelectionGroup)
                assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.discountBadge).visibility)
                assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.tvProductPriceCutOff).visibility)
                selection.getChildAt(1).performClick()
                assertEquals(View.GONE, activity.findViewById<View>(R.id.discountBadge).visibility)
                assertEquals(View.GONE, activity.findViewById<View>(R.id.tvProductPriceCutOff).visibility)
                selection.getChildAt(0).performClick()
                assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.discountBadge).visibility)
                assertEquals(View.VISIBLE, activity.findViewById<View>(R.id.tvProductPriceCutOff).visibility)
            }
            openScenarioSheet(activity)
            instrumentation.runOnMainSync {
                val sheet = scenarioSheet(activity)
                assertTrue(sheet.requireView().findViewById<CompoundButton>(R.id.rbKeepSpace).isChecked)
                sheet.requireView().findViewById<View>(R.id.rbCollapseSpace).performClick()
                sheet.requireView().findViewById<View>(UIKitR.id.btnClose).performClick()
            }
            awaitCondition("Closing without Play must dismiss the sheet") { scenarioSheetOrNull(activity) == null }
            openScenarioSheet(activity)
            instrumentation.runOnMainSync {
                assertTrue(scenarioSheet(activity).requireView().findViewById<CompoundButton>(R.id.rbKeepSpace).isChecked)
            }
        }
    }

    @Test
    fun scenarioChoiceAndPendingSheetSelectionSurviveRotation() {
        withPdp { initialActivity, scenario ->
            dismissCoachmark(initialActivity)
            openScenarioSheet(initialActivity)
            instrumentation.runOnMainSync {
                scenarioSheet(initialActivity).requireView().findViewById<View>(R.id.rbKeepSpace).performClick()
            }
            scenario.recreate()
            lateinit var activity: GuidelinePDPActivity
            scenario.onActivity { activity = it }
            awaitCondition("Scenario sheet must restore after rotation") {
                scenarioSheetOrNull(activity)?.dialog?.isShowing == true
            }
            instrumentation.runOnMainSync {
                val sheet = scenarioSheet(activity)
                assertTrue(sheet.requireView().findViewById<CompoundButton>(R.id.rbKeepSpace).isChecked)
                sheet.requireView().findViewById<View>(R.id.btnPlaySpotlight).performClick()
            }
            val overlay = awaitPdpOverlay(activity)
            verifyTimedCycle(activity, overlay, shouldKeepSpace = true)
            dismissCoachmark(activity)
            scenario.recreate()
            scenario.onActivity { activity = it }
            openScenarioSheet(activity)
            instrumentation.runOnMainSync {
                assertTrue(scenarioSheet(activity).requireView().findViewById<CompoundButton>(R.id.rbKeepSpace).isChecked)
            }
        }
    }

    @Test
    fun animatedScenarioFadesAndTracksIntermediateLayoutFrames() {
        withPdp { activity, _ ->
            dismissCoachmark(activity)
            val overlay = playScenario(activity, PdpDiscountScenario.ANIMATE_SPACE)
            val selection = activity.findViewById<PDPSelectionGroup>(R.id.pdpSelectionGroup)
            val badge = activity.findViewById<View>(R.id.discountBadge)
            val cutOff = activity.findViewById<View>(R.id.tvProductPriceCutOff)
            val container = selection.parent as ViewGroup
            val selectorPositions = mutableSetOf<Int>()
            val fadeSamples = mutableListOf<String>()
            var originalSelectorY = 0
            var originalCardY = 0f
            var cardOffset = 0f
            var maximumAlignmentError = 0f
            var hasFaded = false
            var hasCheckedHighlight = false
            var isHighlightAligned = false
            val listener = ViewTreeObserver.OnDrawListener {
                if (selection.getSelectedPosition() == 1) {
                    val center = centerInOverlay(selection, overlay)
                    selectorPositions.add(center.second)
                    fadeSamples.add("${badge.alpha}/${cutOff.alpha}")
                    val error = abs(overlay.getChildAt(0).translationY - center.second - cardOffset)
                    maximumAlignmentError = maxOf(maximumAlignmentError, error)
                    hasFaded = hasFaded || (badge.alpha > 0f && badge.alpha < 1f) ||
                        (cutOff.alpha > 0f && cutOff.alpha < 1f)
                    if (!hasCheckedHighlight && center.second < originalSelectorY - 8.dp) {
                        val insideTopEdge = center.second - selection.height / 2 - 4.dp.toInt()
                        isHighlightAligned = isClearAt(overlay, center.first, insideTopEdge)
                        hasCheckedHighlight = true
                    }
                }
            }
            instrumentation.runOnMainSync {
                originalSelectorY = centerInOverlay(selection, overlay).second
                originalCardY = overlay.getChildAt(0).translationY
                cardOffset = originalCardY - originalSelectorY
                overlay.viewTreeObserver.addOnDrawListener(listener)
            }
            try {
                awaitCondition("Animated Kartonan collapse must finish") {
                    selection.getSelectedPosition() == 1 && badge.visibility == View.GONE &&
                        container.layoutTransition?.isRunning == false &&
                        centerInOverlay(selection, overlay).second < originalSelectorY
                }
                instrumentation.runOnMainSync {
                    assertTrue("Selector must interpolate through multiple positions", selectorPositions.size > 2)
                    assertTrue("Discounts must fade; samples: $fadeSamples", hasFaded)
                    assertTrue("Highlight must follow the animated top edge", hasCheckedHighlight && isHighlightAligned)
                    assertTrue("Card drifted by $maximumAlignmentError px", maximumAlignmentError <= 2f)
                }
                awaitCondition("Animated Satuan return must finish with spotlight open") {
                    selection.getSelectedPosition() == 0 && overlay.isVisible &&
                        badge.visibility == View.VISIBLE && cutOff.visibility == View.VISIBLE &&
                        badge.alpha == 1f && cutOff.alpha == 1f &&
                        container.layoutTransition?.isRunning == false &&
                        centerInOverlay(selection, overlay).second == originalSelectorY &&
                        overlay.getChildAt(0).translationY == originalCardY
                }
            } finally {
                instrumentation.runOnMainSync {
                    overlay.viewTreeObserver.takeIf { it.isAlive }?.removeOnDrawListener(listener)
                }
            }
        }
    }

    @Test
    fun animatedScenarioHandlesBackDuringCollapseAndRestoresAfterRotation() {
        withPdp { initialActivity, scenario ->
            dismissCoachmark(initialActivity)
            playScenario(initialActivity, PdpDiscountScenario.ANIMATE_SPACE)
            val selection = initialActivity.findViewById<PDPSelectionGroup>(R.id.pdpSelectionGroup)
            awaitCondition("Layout animation must begin") {
                selection.getSelectedPosition() == 1 && (selection.parent as ViewGroup).layoutTransition?.isRunning == true
            }
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            awaitCondition("Back must dismiss the animated spotlight") { findOverlay(initialActivity.window.decorView) == null }
            instrumentation.runOnMainSync {
                assertEquals(0, selection.getSelectedPosition())
                listOf(R.id.discountBadge, R.id.tvProductPriceCutOff).forEach { id ->
                    val view = initialActivity.findViewById<View>(id)
                    assertEquals(View.VISIBLE, view.visibility)
                    assertEquals(1f, view.alpha)
                }
                assertEquals(null, (selection.parent as ViewGroup).layoutTransition)
            }
            scenario.recreate()
            lateinit var activity: GuidelinePDPActivity
            scenario.onActivity { activity = it }
            openScenarioSheet(activity)
            instrumentation.runOnMainSync {
                val sheet = scenarioSheet(activity)
                assertTrue(sheet.requireView().findViewById<CompoundButton>(R.id.rbAnimateSpace).isChecked)
                sheet.requireView().findViewById<View>(R.id.btnPlaySpotlight).performClick()
            }
            val replay = awaitPdpOverlay(activity)
            verifyTimedCycle(activity, replay, shouldKeepSpace = false)
        }
    }

    private fun withPdp(test: (GuidelinePDPActivity, ActivityScenario<GuidelinePDPActivity>) -> Unit) {
        ActivityScenario.launch(GuidelinePDPActivity::class.java).use { scenario ->
            lateinit var activity: GuidelinePDPActivity
            scenario.onActivity { activity = it }
            awaitCondition("Initial spotlight must appear") { findOverlay(activity.window.decorView)?.isVisible == true }
            test(activity, scenario)
        }
    }

    private fun openScenarioSheet(activity: GuidelinePDPActivity) {
        instrumentation.runOnMainSync {
            val favorite = activity.findViewById<CompoundButton>(R.id.ivLove)
            val wasChecked = favorite.isChecked
            assertTrue(favorite.performLongClick())
            assertEquals(wasChecked, favorite.isChecked)
        }
        awaitCondition("Favorite long press must open the scenario sheet") {
            scenarioSheetOrNull(activity)?.dialog?.isShowing == true
        }
    }

    private fun scenarioSheetOrNull(activity: GuidelinePDPActivity): PdpScenarioBottomSheet? =
        activity.supportFragmentManager.findFragmentByTag(PdpScenarioBottomSheet.TAG) as? PdpScenarioBottomSheet

    private fun scenarioSheet(activity: GuidelinePDPActivity): PdpScenarioBottomSheet =
        requireNotNull(scenarioSheetOrNull(activity))

    private fun playScenario(activity: GuidelinePDPActivity, shouldKeepSpace: Boolean): CoachMarkOverlay =
        playScenario(activity, if (shouldKeepSpace) PdpDiscountScenario.KEEP_SPACE else PdpDiscountScenario.COLLAPSE_SPACE)

    private fun playScenario(activity: GuidelinePDPActivity, scenario: PdpDiscountScenario): CoachMarkOverlay {
        openScenarioSheet(activity)
        instrumentation.runOnMainSync {
            val sheet = scenarioSheet(activity)
            val radioId = when (scenario) {
                PdpDiscountScenario.COLLAPSE_SPACE -> R.id.rbCollapseSpace
                PdpDiscountScenario.KEEP_SPACE -> R.id.rbKeepSpace
                PdpDiscountScenario.ANIMATE_SPACE -> R.id.rbAnimateSpace
            }
            sheet.requireView().findViewById<View>(radioId).performClick()
            sheet.requireView().findViewById<View>(R.id.btnPlaySpotlight).performClick()
        }
        return awaitPdpOverlay(activity)
    }

    private fun awaitPdpOverlay(activity: GuidelinePDPActivity): CoachMarkOverlay {
        awaitCondition("Replay must start after the sheet closes") {
            scenarioSheetOrNull(activity) == null && findOverlay(activity.window.decorView)?.isVisible == true
        }
        lateinit var overlay: CoachMarkOverlay
        instrumentation.runOnMainSync { overlay = requireNotNull(findOverlay(activity.window.decorView)) }
        awaitCondition("Spotlight entrance must finish") { overlay.getChildAt(0).alpha == 1f }
        return overlay
    }

    private fun dismissCoachmark(activity: GuidelinePDPActivity) {
        instrumentation.runOnMainSync {
            findOverlay(activity.window.decorView)!!.findViewById<View>(UIKitR.id.btnSingleAction).performClick()
        }
        awaitCondition("Dismissal must remove the spotlight") { findOverlay(activity.window.decorView) == null }
    }

    private fun verifyTimedCycle(
        activity: GuidelinePDPActivity,
        overlay: CoachMarkOverlay,
        shouldKeepSpace: Boolean
    ) {
        val selection = activity.findViewById<PDPSelectionGroup>(R.id.pdpSelectionGroup)
        var originalSelectorY = 0
        var originalCardY = 0f
        instrumentation.runOnMainSync {
            assertEquals(0, selection.getSelectedPosition())
            originalSelectorY = centerInOverlay(selection, overlay).second
            originalCardY = overlay.getChildAt(0).translationY
        }
        awaitCondition("Spotlight must select Kartonan") { selection.getSelectedPosition() == 1 }
        val kartonSelectedAt = SystemClock.uptimeMillis()
        awaitCondition("Discount mode must position the selector and card correctly") {
            val expectedVisibility = if (shouldKeepSpace) View.INVISIBLE else View.GONE
            val selectorY = centerInOverlay(selection, overlay).second
            val cardY = overlay.getChildAt(0).translationY
            activity.findViewById<View>(R.id.discountBadge).visibility == expectedVisibility &&
                activity.findViewById<View>(R.id.tvProductPriceCutOff).visibility == expectedVisibility &&
                (if (shouldKeepSpace) selectorY == originalSelectorY else selectorY < originalSelectorY) &&
                (if (shouldKeepSpace) cardY == originalCardY else cardY < originalCardY)
        }
        awaitCondition("Spotlight must return to Satuan without dismissal") {
            selection.getSelectedPosition() == 0 && overlay.isVisible
        }
        val kartonDuration = SystemClock.uptimeMillis() - kartonSelectedAt
        assertTrue("Kartonan duration was $kartonDuration ms", kartonDuration in 1000..1600)
        awaitCondition("Both views and original highlight position must return") {
            activity.findViewById<View>(R.id.discountBadge).visibility == View.VISIBLE &&
                activity.findViewById<View>(R.id.tvProductPriceCutOff).visibility == View.VISIBLE &&
                centerInOverlay(selection, overlay).second == originalSelectorY &&
                overlay.getChildAt(0).translationY == originalCardY
        }
        instrumentation.runOnMainSync {
            val center = centerInOverlay(selection, overlay)
            assertTrue(isClearAt(overlay, center.first, center.second))
        }
    }

    private fun verifyPdpDismissal(isEarlyDismissal: Boolean, usesBack: Boolean) {
        val intent = Intent(instrumentation.targetContext, GuidelinePDPActivity::class.java).apply {
            putExtra(GuidelinesSearchProductActivity.QUANTITY_KEY, 2)
        }
        ActivityScenario.launch<GuidelinePDPActivity>(intent).use { scenario ->
            lateinit var activity: GuidelinePDPActivity
            scenario.onActivity { activity = it }
            awaitCondition("PDP coachmark must appear") { findOverlay(activity.window.decorView)?.isVisible == true }
            val selection = activity.findViewById<PDPSelectionGroup>(R.id.pdpSelectionGroup)
            if (!isEarlyDismissal) {
                awaitCondition("Demo must select Karton and hide discounts") {
                    selection.getSelectedPosition() == 1 &&
                        !activity.findViewById<View>(R.id.discountBadge).isVisible &&
                        !activity.findViewById<View>(R.id.tvProductPriceCutOff).isVisible
                }
            }

            if (usesBack) {
                instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            } else {
                instrumentation.runOnMainSync {
                    findOverlay(activity.window.decorView)!!.findViewById<View>(UIKitR.id.btnSingleAction).performClick()
                }
            }
            awaitCondition("Dismissal must finish") { findOverlay(activity.window.decorView)?.isVisible != true }
            if (isEarlyDismissal) SystemClock.sleep(1500)

            instrumentation.runOnMainSync {
                assertEquals(0, selection.getSelectedPosition())
                assertTrue(activity.findViewById<View>(R.id.discountBadge).isVisible)
                assertTrue(activity.findViewById<View>(R.id.tvProductPriceCutOff).isVisible)
                assertEquals("Rp2.000", activity.findViewById<TextView>(R.id.tvProductPrice).text.toString().withoutSpaces())
                val footer = activity.findViewById<PDPFooter>(R.id.pdpFooter)
                assertEquals("Rp4.000", footer.binding.tvPrice.text.toString().withoutSpaces())
                footer.binding.sbQuantity.findViewById<View>(UIKitR.id.plus_button).performClick()
                assertEquals(3, footer.binding.sbQuantity.getCount())
                assertEquals("Rp6.000", footer.binding.tvPrice.text.toString().withoutSpaces())
            }
        }
    }

    private fun withOverlay(test: (FrameLayout, View, View, CoachMarkOverlay) -> Unit) {
        ActivityScenario.launch(GuidelinesPDPEntryActivity::class.java).use { scenario ->
            lateinit var host: FrameLayout
            lateinit var target: View
            lateinit var nextTarget: View
            lateinit var overlay: CoachMarkOverlay
            scenario.onActivity { activity ->
                host = FrameLayout(activity)
                activity.setContentView(host)
                target = View(activity)
                nextTarget = View(activity)
                host.addView(target, FrameLayout.LayoutParams(120.dp.toInt(), 60.dp.toInt()).apply {
                    leftMargin = 40.dp.toInt()
                    topMargin = 160.dp.toInt()
                })
                host.addView(nextTarget, FrameLayout.LayoutParams(120.dp.toInt(), 60.dp.toInt()).apply {
                    leftMargin = 40.dp.toInt()
                    topMargin = 300.dp.toInt()
                })
                overlay = CoachMarkOverlay.Builder(activity)
                    .setContainer(host)
                    .setVerticalPosition(CoachMarkOverlay.CoachMarkVerticalPosition.Below)
                    .setCoachMarkItems(
                        CoachMarkData(target, "First target", "Moving target regression"),
                        CoachMarkData(nextTarget, "Second target", "Transition regression")
                    )
                    .build()
            }
            awaitCondition("Overlay entrance must finish") {
                overlay.width > 0 && overlay.getChildAt(0).alpha == 1f &&
                    centerInOverlay(target, overlay).let { isClearAt(overlay, it.first, it.second) }
            }
            test(host, target, nextTarget, overlay)
        }
    }

    private fun findOverlay(view: View): CoachMarkOverlay? {
        if (view is CoachMarkOverlay) return view
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                findOverlay(view.getChildAt(index))?.let { return it }
            }
        }
        return null
    }

    private fun centerInOverlay(target: View, overlay: View): Pair<Int, Int> {
        val targetLocation = IntArray(2)
        val overlayLocation = IntArray(2)
        target.getLocationInWindow(targetLocation)
        overlay.getLocationInWindow(overlayLocation)
        return Pair(
            targetLocation[0] - overlayLocation[0] + target.width / 2,
            targetLocation[1] - overlayLocation[1] + target.height / 2
        )
    }

    private fun isClearAt(overlay: CoachMarkOverlay, x: Int, y: Int): Boolean {
        val bitmap = Bitmap.createBitmap(overlay.width, overlay.height, Bitmap.Config.ARGB_8888)
        overlay.draw(Canvas(bitmap))
        val isClear = Color.alpha(bitmap.getPixel(x, y)) == 0
        bitmap.recycle()
        return isClear
    }

    private fun awaitCondition(message: String, condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 5000
        while (SystemClock.uptimeMillis() < deadline) {
            var isSatisfied = false
            instrumentation.runOnMainSync { isSatisfied = condition() }
            if (isSatisfied) return
            SystemClock.sleep(25)
        }
        fail(message)
    }

    private fun String.withoutSpaces(): String = replace("\\s|\u00a0".toRegex(), "")
}
