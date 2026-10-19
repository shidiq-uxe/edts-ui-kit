package id.co.edtslib.edtsuikit

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import id.co.edtslib.uikit.tray.BottomSheetTray
import id.co.edtslib.uikit.utils.dp
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrayDismissMotionTest {

    private lateinit var activity: ActivityScenario<GuidelinesBottomTrayActivity>
    private lateinit var screen: GuidelinesBottomTrayActivity

    @Before
    fun launch() {
        activity = ActivityScenario.launch(GuidelinesBottomTrayActivity::class.java)
        activity.onActivity { screen = it }
    }

    @After
    fun close() { activity.close() }

    @Test
    fun shortAndTallSheetsStayOpaqueUntilTheyReachTheBottom() = exercise { ui ->
        for (height in listOf(160.dp.toInt(), (screen.window.decorView.height * 0.8f).toInt())) {
            val tray = showTray(ui, height)
            val sheet = requireNotNull(tray.dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet))
            val bottom = (sheet.parent as View).height
            val start = sheet.top
            val positions = mutableListOf<Int>()
            tray.getBottomSheetBehavior()?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) = Unit
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    positions += bottomSheet.top
                    assertEquals(1f, bottomSheet.alpha)
                    assertEquals(1f, tray.view?.alpha ?: 1f)
                }
            })
            tray.dismiss()
            assertTrue("Fragment must remain until the slide finishes", tray.isAdded)
            ui.loopMainThreadForAtLeast(900)
            assertFalse(tray.isAdded)
            assertTrue("Sheet must reach the parent's bottom edge", sheet.top >= bottom)
            assertTrue("Expected visible downward movement", positions.any { it > start && it < bottom })
            assertEquals(1, tray.dismissCount)
            assertEquals(0, tray.cancelCount)
        }
    }

    @Test
    fun stateLossDirectDialogAndOutsideDismissalsFinishAtTheBottom() = exercise { ui ->
        for (mode in listOf("stateLoss", "dialog", "outside")) {
            val tray = showTray(ui, 240.dp.toInt())
            val sheet = requireNotNull(tray.dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet))
            val bottom = (sheet.parent as View).height
            when (mode) {
                "stateLoss" -> tray.dismissAllowingStateLoss()
                "dialog" -> tray.dialog?.dismiss()
                else -> tray.dialog?.findViewById<View>(com.google.android.material.R.id.touch_outside)?.performClick()
            }
            ui.loopMainThreadForAtLeast(900)
            assertFalse("Dismissal path: $mode", tray.isAdded)
            assertTrue("Dismissal path: $mode must finish offscreen", sheet.top >= bottom)
            assertEquals(1, tray.dismissCount)
            assertEquals(if (mode == "outside") 1 else 0, tray.cancelCount)
        }
    }

    @Test
    fun explicitCloseStillSlidesWhenUserDismissalIsDisabled() = exercise { ui ->
        val tray = showTray(ui, 240.dp.toInt(), allowUserDismissal = false)
        assertFalse(requireNotNull(tray.getBottomSheetBehavior()).isHideable)
        val sheet = requireNotNull(tray.dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet))
        val bottom = (sheet.parent as View).height
        tray.binding.btnClose.performClick()
        ui.loopMainThreadForAtLeast(900)
        assertFalse(tray.isAdded)
        assertTrue(sheet.top >= bottom)
        assertEquals(0, tray.cancelCount)
        assertEquals(1, tray.dismissCount)
    }

    @Test
    fun subclassesWithTheirOwnDialogUseTheSameNativeExit() = exercise { ui ->
        val tray = showTray(ui, 240.dp.toInt(), useCustomDialogFactory = true)
        assertTrue((tray.dialog as BottomSheetDialog).dismissWithAnimation)
        val sheet = requireNotNull(tray.dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet))
        val bottom = (sheet.parent as View).height
        tray.dismiss()
        ui.loopMainThreadForAtLeast(900)
        assertFalse(tray.isAdded)
        assertTrue(sheet.top >= bottom)
        assertEquals(0, tray.cancelCount)
        assertEquals(1, tray.dismissCount)
    }

    private fun showTray(
        ui: UiController,
        contentHeight: Int,
        allowUserDismissal: Boolean = true,
        useCustomDialogFactory: Boolean = false
    ): ObservedTray {
        val tray = ObservedTray().apply {
            useOwnDialog = useCustomDialogFactory
            title = "Native slide dismissal"
            shouldShowClose = true
            dragHandleVisibility = true
            isCancelable = allowUserDismissal
            dismissOnOutsideTouch = allowUserDismissal
            isCancelableOnTouchOutside = allowUserDismissal
            contentView = FrameLayout(screen).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, contentHeight)
                minimumHeight = contentHeight
            }
        }
        tray.showNow(screen.supportFragmentManager, "native_slide_test")
        ui.loopMainThreadForAtLeast(500)
        tray.getBottomSheetBehavior()?.state = BottomSheetBehavior.STATE_EXPANDED
        ui.loopMainThreadForAtLeast(700)
        return tray
    }

    private fun exercise(block: (UiController) -> Unit) {
        onView(withId(R.id.stepperAnimated)).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> = isDisplayed()
            override fun getDescription(): String = "Verify opaque full-distance tray dismissal"
            override fun perform(uiController: UiController, view: View) { block(uiController) }
        })
    }

    class ObservedTray : BottomSheetTray() {

        var useOwnDialog = false
        var cancelCount = 0
        var dismissCount = 0

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
            if (useOwnDialog) BottomSheetDialog(requireContext(), theme) else super.onCreateDialog(savedInstanceState)

        override fun onCancel(dialog: DialogInterface) {
            cancelCount++
            super.onCancel(dialog)
        }

        override fun onDismiss(dialog: DialogInterface) {
            dismissCount++
            super.onDismiss(dialog)
        }
    }
}
