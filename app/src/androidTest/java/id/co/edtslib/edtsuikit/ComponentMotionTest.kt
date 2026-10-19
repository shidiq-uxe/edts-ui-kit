package id.co.edtslib.edtsuikit

import android.animation.ValueAnimator
import android.os.Build
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import id.co.edtslib.uikit.stepper.StepperButton
import id.co.edtslib.uikit.tray.BottomSheetTray
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dp
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import id.co.edtslib.uikit.R as UIKitR

@RunWith(AndroidJUnit4::class)
class ComponentMotionTest {

    private lateinit var activity: ActivityScenario<GuidelinesBottomTrayActivity>
    private lateinit var screen: GuidelinesBottomTrayActivity
    private lateinit var stepper: StepperButton

    @Before
    fun launch() {
        activity = ActivityScenario.launch(GuidelinesBottomTrayActivity::class.java)
        activity.onActivity {
            screen = it
            stepper = it.findViewById(R.id.stepperAnimated)
        }
    }

    @After
    fun close() { activity.close() }

    @Test
    fun rapidChangesAndSameValueRebindingKeepTheLatestCount() = exercise { ui ->
        stepper.setCount(9, animate = false)
        val accepted = mutableListOf<Int>()
        stepper.setOnCountChangeListener {
            accepted += it
            stepper.setAllowTextEditing(false)
            stepper.isEnabled = true
            stepper.setCount(it, animate = false)
        }
        stepper.binding.plusButton.performClick()
        assertEquals(10, stepper.getCount())
        assertEquals("10", stepper.binding.countText.text.toString())
        if (hasMotion()) assertEquals(0f, stepper.binding.countText.alpha)
        stepper.binding.minusButton.performClick()
        stepper.binding.plusButton.performClick()
        stepper.binding.plusButton.performClick()
        assertEquals(listOf(10, 9, 10, 11), accepted)
        ui.loopMainThreadForAtLeast(250)
        assertEquals(11, stepper.getCount())
        assertEquals("11", stepper.binding.countText.text.toString())
        assertEquals(1f, stepper.binding.countText.alpha)
        assertEquals(0f, stepper.binding.countText.translationY)
    }

    @Test
    fun limitsAndExplicitAvailabilitySurviveReenabling() = exercise { _ ->
        stepper.setCount(12, animate = false)
        assertFalse(stepper.binding.plusButton.isEnabled)
        assertEquals(screen.color(UIKitR.color.black_30), stepper.binding.plusButton.backgroundTintList?.getColorForState(
            stepper.binding.plusButton.drawableState, 0))
        stepper.setCount(0, animate = false)
        assertFalse(stepper.binding.minusButton.isEnabled)
        assertTrue(stepper.binding.plusButton.isEnabled)
        stepper.setCount(8, animate = false)
        stepper.setPlusButtonEnabled(false)
        stepper.isEnabled = false
        stepper.isEnabled = true
        assertFalse(stepper.binding.plusButton.isEnabled)
        assertTrue(stepper.binding.minusButton.isEnabled)
        stepper.setPlusButtonEnabled(true)
        assertTrue(stepper.binding.plusButton.isEnabled)
    }

    @Test
    fun focusedEditorAndSilentUpdatesRemainStable() = exercise { ui ->
        stepper.setAllowTextEditing(true)
        stepper.binding.countEditText.requestFocus()
        stepper.binding.countEditText.setText("10")
        ui.loopMainThreadForAtLeast(600)
        assertEquals(10, stepper.getCount())
        assertEquals(1f, stepper.binding.countEditText.alpha)
        stepper.binding.countEditText.clearFocus()
        stepper.setCount(9)
        stepper.setCount(4, animate = false)
        assertEquals(1f, stepper.binding.countEditText.alpha)
        assertEquals("4", stepper.binding.countEditText.text.toString())
        stepper.setAllowTextEditing(false)
        assertEquals("4", stepper.binding.countText.text.toString())
    }

    @Test
    fun disablingCountMotionAndDetachingRestoreText() = exercise { ui ->
        stepper.binding.plusButton.performClick()
        stepper.isCountAnimationEnabled = false
        assertEquals(1f, stepper.binding.countText.alpha)
        stepper.binding.minusButton.performClick()
        assertEquals(1f, stepper.binding.countText.alpha)
        stepper.isCountAnimationEnabled = true
        stepper.binding.plusButton.performClick()
        val parent = stepper.parent as android.view.ViewGroup
        parent.removeView(stepper)
        assertEquals(1f, stepper.binding.countText.alpha)
        parent.addView(stepper)
        ui.loopMainThreadForAtLeast(100)
        assertEquals("9", stepper.binding.countText.text.toString())
    }

    @Test
    fun trayKeepsMaterialEntryAndUsesNativeSlideWithButtonSemantics() = exercise { ui ->
        val tray = showTray(ui, custom = true)
        assertEquals(if (hasMotion()) UIKitR.style.Animation_EDTS_UIKit_BottomSheetDialog else 0, tray.dialog?.window?.attributes?.windowAnimations)
        assertEquals(hasMotion(), (tray.dialog as com.google.android.material.bottomsheet.BottomSheetDialog).dismissWithAnimation)
        val close = tray.binding.btnClose
        val info = close.createAccessibilityNodeInfo()
        close.onInitializeAccessibilityNodeInfo(info)
        assertEquals(Button::class.java.name, info.className)
        assertFalse(info.isCheckable)
        assertFalse(close.isChecked)
        close.performClick()
        assertFalse(close.isChecked)
        ui.loopMainThreadForAtLeast(800)
        assertFalse(tray.isAdded)
        val material = showTray(ui, custom = false)
        if (hasMotion()) {
            assertEquals(com.google.android.material.R.style.Animation_Material3_BottomSheetDialog,
                material.dialog?.window?.attributes?.windowAnimations)
        }
        material.dismissAllowingStateLoss()
        ui.loopMainThreadForAtLeast(800)
        assertFalse(material.isAdded)
    }

    @Test
    fun expandedCloseTargetAndOutsideCancellationDismissOnce() = exercise { ui ->
        val tray = showTray(ui, custom = true)
        val root = tray.binding.root
        val bounds = android.graphics.Rect(0, 0, tray.binding.btnClose.width, tray.binding.btnClose.height)
        root.offsetDescendantRectToMyCoords(tray.binding.btnClose, bounds)
        val x = bounds.exactCenterX()
        val y = bounds.exactCenterY() - 20.dp
        val now = android.os.SystemClock.uptimeMillis()
        for (action in listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP)) {
            val event = MotionEvent.obtain(now, now + action * 10, action, x, y, 0)
            root.dispatchTouchEvent(event)
            event.recycle()
        }
        ui.loopMainThreadForAtLeast(800)
        assertFalse(tray.isAdded)
        val outside = showTray(ui, custom = true)
        outside.dialog?.findViewById<View>(com.google.android.material.R.id.touch_outside)?.performClick()
        ui.loopMainThreadForAtLeast(800)
        assertFalse(outside.isAdded)
    }

    @Test
    fun backAndBehaviorHideCompleteWithoutASecondCustomExit() = exercise { ui ->
        val back = showTray(ui, custom = true)
        ui.injectKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_BACK))
        ui.injectKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_BACK))
        ui.loopMainThreadForAtLeast(800)
        assertFalse(back.isAdded)
        val swipe = showTray(ui, custom = true)
        val dialog = requireNotNull(swipe.dialog)
        swipe.getBottomSheetBehavior()?.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
        ui.loopMainThreadForAtLeast(800)
        assertFalse(swipe.isAdded)
        if (hasMotion()) {
            assertEquals(UIKitR.style.Animation_EDTS_UIKit_BottomSheetDialog,
                dialog.window?.attributes?.windowAnimations)
        }
    }

    @Test
    fun recreationRestoresEachStepperWithoutSharedChildState() {
        activity.onActivity {
            it.findViewById<StepperButton>(R.id.stepperAnimated).setCount(10, animate = false)
            it.findViewById<StepperButton>(R.id.stepperInstant).setCount(3, animate = false)
            it.findViewById<StepperButton>(R.id.stepperEditable).setCount(11, animate = false)
        }
        activity.recreate()
        activity.onActivity {
            for ((id, count) in listOf(R.id.stepperAnimated to 10, R.id.stepperInstant to 3, R.id.stepperEditable to 11)) {
                val restored = it.findViewById<StepperButton>(id)
                assertEquals(count, restored.getCount())
                assertEquals(count.toString(), restored.binding.countText.text.toString())
                assertEquals(count.toString(), restored.binding.countEditText.text.toString())
            }
        }
    }

    private fun showTray(ui: UiController, custom: Boolean): BottomSheetTray {
        screen.findViewById<View>(if (custom) R.id.btnShowBottomSheet else R.id.btnShowMaterialSheet).performClick()
        ui.loopMainThreadForAtLeast(500)
        return screen.supportFragmentManager.findFragmentByTag("component_motion_tray") as BottomSheetTray
    }

    private fun hasMotion(): Boolean = if (Build.VERSION.SDK_INT >= 26) ValueAnimator.areAnimatorsEnabled()
        else Settings.Global.getFloat(screen.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f

    private fun exercise(block: (UiController) -> Unit) {
        onView(withId(R.id.stepperAnimated)).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> = isDisplayed()
            override fun getDescription(): String = "Verify count motion, boundaries, editing, and tray dismissal"
            override fun perform(uiController: UiController, view: View) { block(uiController) }
        })
    }
}
