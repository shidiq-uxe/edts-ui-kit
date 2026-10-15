package id.co.edtslib.edtsuikit.tmh

import android.content.Context
import android.graphics.Color
import android.view.Gravity
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
import androidx.test.platform.app.InstrumentationRegistry
import id.co.edtslib.edtsuikit.GuidelinesTmhActivity
import id.co.edtslib.edtsuikit.R
import id.co.edtslib.uikit.stepper.StepperButton
import id.co.edtslib.uikit.stepper.StepperButtonTouchTargetMode
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
class StepperFeedbackTest {

    private lateinit var activity: ActivityScenario<GuidelinesTmhActivity>
    private lateinit var stepper: HapticStepper

    @Before
    fun launch() {
        activity = ActivityScenario.launch(GuidelinesTmhActivity::class.java)
        activity.onActivity { screen ->
            val host = FrameLayout(screen).apply { setBackgroundColor(Color.WHITE) }
            stepper = HapticStepper(screen).apply {
                id = View.generateViewId()
                setAllowTextEditing(false)
                isAlwaysExpanded = true
                setMaxValue(100)
                setCount(50, animate = false)
            }
            host.addView(stepper, FrameLayout.LayoutParams(90.dp.toInt(), 32.dp.toInt(), Gravity.CENTER))
            screen.findViewById<ViewGroup>(R.id.main).addView(host,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
    }

    @After
    fun close() { activity.close() }

    private fun exercise(block: (UiController) -> Unit) {
        onView(withId(stepper.id)).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> = isDisplayed()
            override fun getDescription(): String = "Check expanded targets, protected editing, and gesture feedback"
            override fun perform(uiController: UiController, view: View) { block(uiController) }
        })
    }

    @Test
    fun compactEdgeTapsImproveTargetsWithoutResizing() = exercise { ui ->
        assertDp(90, stepper.width)
        assertDp(32, stepper.height)
        assertDp(24, stepper.binding.plusButton.width)
        DragGesture(ui, stepper.binding.plusButton, offsetXDp = 15f).tap()
        assertEquals(51, stepper.getCount())
        DragGesture(ui, stepper.binding.minusButton, offsetXDp = -15f).tap()
        assertEquals(50, stepper.getCount())
        assertTrue(stepper.effects.isEmpty())
        assertDp(90, stepper.width)
        assertDp(32, stepper.height)
    }

    @Test
    fun editorOwnsTheBoundaryAndTypingRetainsItsArea() = exercise { ui ->
        stepper.setAllowTextEditing(true)
        stepper.isStepperHapticFeedbackEnabled = true
        ui.loopMainThreadForAtLeast(100)
        val editorWidth = stepper.binding.countEditText.width
        assertDp(34, editorWidth)
        // x=29dp is inside the editor; a naive centered 32dp minus target would steal it.
        DragGesture(ui, stepper.binding.minusButton, offsetXDp = 13f).tap()
        ui.loopMainThreadForAtLeast(150)
        assertTrue(stepper.binding.countEditText.hasFocus())
        assertEquals(50, stepper.getCount())
        assertTrue(stepper.effects.isEmpty())
        assertEquals(editorWidth, stepper.binding.countEditText.width)
        stepper.binding.countEditText.setText("41")
        ui.loopMainThreadForAtLeast(650)
        assertEquals(41, stepper.getCount())
        assertEquals(1, stepper.effects.size)
        stepper.binding.countEditText.clearFocus()
    }

    @Test
    fun expandedHostIsOptInAndKeepsPillAndEditorUnchanged() = exercise { ui ->
        stepper.buttonTouchTargetMode = StepperButtonTouchTargetMode.EXPANDED
        stepper.layoutParams = (stepper.layoutParams as FrameLayout.LayoutParams).apply {
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        ui.loopMainThreadForAtLeast(150)
        assertDp(106, stepper.width)
        assertDp(48, stepper.height)
        assertDp(90, stepper.binding.stepperContainer.width)
        // Outside the visible pill, but inside the 48dp button target.
        DragGesture(ui, stepper.binding.plusButton, offsetXDp = 23f, offsetYDp = 23f).tap()
        assertEquals(51, stepper.getCount())
        stepper.setAllowTextEditing(true)
        ui.loopMainThreadForAtLeast(150)
        assertDp(130, stepper.width)
        assertDp(34, stepper.binding.countEditText.width)
        DragGesture(ui, stepper.binding.minusButton, offsetXDp = -35f).tap()
        assertEquals(50, stepper.getCount())
        DragGesture(ui, stepper.binding.countEditText).tap()
        assertTrue(stepper.binding.countEditText.hasFocus())
        stepper.binding.countEditText.clearFocus()
        stepper.layoutParams = (stepper.layoutParams as FrameLayout.LayoutParams).apply {
            width = 90.dp.toInt()
            height = 32.dp.toInt()
        }
        ui.loopMainThreadForAtLeast(100)
        assertDp(90, stepper.width)
        assertDp(32, stepper.height)
    }

    @Test
    fun dragFeedbackTicksOnlyOnActivationAndAcceptedChanges() = exercise { ui ->
        stepper.isStepperHapticFeedbackEnabled = true
        stepper.isDragFeedbackEnabled = true
        stepper.isDragRepeatEnabled = true
        stepper.setMaxValue(51)
        val originalStroke = stepper.binding.stepperContainer.strokeColor
        // Start outside the 24dp icon to exercise the shared extended tap/drag routing.
        val gesture = DragGesture(ui, stepper.binding.plusButton, offsetXDp = 15f)
        gesture.activate()
        assertEquals(50, stepper.getCount())
        assertEquals(1, stepper.effects.size)
        capture("drag-paused")
        gesture.move(108f)
        gesture.waitFor(350)
        assertEquals(51, stepper.getCount())
        assertEquals(2, stepper.effects.size)
        capture("drag-limit")
        gesture.waitFor(350)
        assertEquals(2, stepper.effects.size)
        gesture.move(0f)
        gesture.waitFor(250)
        assertEquals(2, stepper.effects.size)
        gesture.move(-60f)
        gesture.waitFor(250)
        assertEquals(50, stepper.getCount())
        assertEquals(3, stepper.effects.size)
        capture("drag-decreasing")
        gesture.release()
        gesture.waitFor(250)
        assertEquals(originalStroke, stepper.binding.stepperContainer.strokeColor)
        val effects = stepper.effects.size
        stepper.setCount(40, animate = false)
        assertEquals(effects, stepper.effects.size)
        stepper.isHapticFeedbackEnabled = false
        DragGesture(ui, stepper.binding.plusButton).tap()
        assertEquals(41, stepper.getCount())
        assertEquals(effects, stepper.effects.size)
    }

    @Test
    fun disabledButtonsAndCancelledDragsStaySilent() = exercise { ui ->
        stepper.isStepperHapticFeedbackEnabled = true
        stepper.isDragFeedbackEnabled = true
        stepper.isDragRepeatEnabled = true
        stepper.setPlusButtonEnabled(false)
        DragGesture(ui, stepper.binding.plusButton, offsetXDp = 15f).tap()
        assertEquals(50, stepper.getCount())
        assertTrue(stepper.effects.isEmpty())
        val drag = DragGesture(ui, stepper.binding.minusButton, offsetXDp = -15f)
        drag.activate()
        assertEquals(1, stepper.effects.size)
        stepper.cancelDragRepeat()
        drag.move(-108f)
        drag.waitFor(350)
        drag.release()
        assertEquals(50, stepper.getCount())
        assertEquals(1, stepper.effects.size)
        assertFalse(stepper.binding.minusButton.isPressed)
    }

    @Test
    fun cancellingAPendingButtonPressDoesNotApplyItToReboundContent() = exercise { ui ->
        stepper.isDragRepeatEnabled = true
        val tap = DragGesture(ui, stepper.binding.plusButton, offsetXDp = 15f)
        tap.press()
        tap.waitFor(40)
        stepper.cancelDragRepeat()
        stepper.setCount(20, animate = false)
        tap.release()
        tap.waitFor(100)
        assertEquals(20, stepper.getCount())
        DragGesture(ui, stepper.binding.plusButton, offsetXDp = 15f).tap()
        assertEquals(21, stepper.getCount())
    }

    private fun assertDp(expected: Int, actual: Int) {
        assertTrue("Expected ${expected}dp, actual ${actual}px", kotlin.math.abs(expected.dp - actual) <= 1f)
    }

    private fun capture(name: String) {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        for (command in listOf(
            "mkdir -p /sdcard/Download/tmh-review-v6",
            "screencap -p /sdcard/Download/tmh-review-v6/$name.png",
        )) {
            automation.executeShellCommand(command).use { descriptor ->
                java.io.FileInputStream(descriptor.fileDescriptor).use { it.readBytes() }
            }
        }
    }

    private class HapticStepper(context: Context) : StepperButton(context) {
        val effects = mutableListOf<Int>()

        override fun performHapticFeedback(feedbackConstant: Int): Boolean {
            effects += feedbackConstant
            return true
        }
    }
}
