package id.co.edtslib.edtsuikit.tmh

import android.content.Context
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import id.co.edtslib.edtsuikit.GuidelinesTmhActivity
import id.co.edtslib.edtsuikit.R
import id.co.edtslib.uikit.stepper.StepperButton
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.dimenPixelSize
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StepperDragRepeatTest {

    private lateinit var activity: ActivityScenario<GuidelinesTmhActivity>
    private lateinit var parent: CaptureParent
    private lateinit var stepper: StepperButton

    @Before
    fun launch() {
        activity = ActivityScenario.launch(GuidelinesTmhActivity::class.java)
        activity.onActivity { screen ->
            parent = CaptureParent(screen)
            stepper = StepperButton(screen).apply {
                id = View.generateViewId()
                setAllowTextEditing(false)
                isAlwaysExpanded = true
                isDragRepeatEnabled = true
                setMaxValue(100)
                setCount(50, animate = false)
            }
            parent.addView(stepper, FrameLayout.LayoutParams(180.dp.toInt(), 72.dp.toInt(), android.view.Gravity.CENTER))
            screen.findViewById<ViewGroup>(R.id.main).addView(parent,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
    }

    @After
    fun close() { activity.close() }

    private fun exercise(block: (UiController) -> Unit) {
        onView(withId(stepper.id)).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> = isDisplayed()
            override fun getDescription(): String = "Exercise long-press repetition using injected screen touch events"
            override fun perform(uiController: UiController, view: View) { block(uiController) }
        })
    }

    @Test
    fun stationaryHoldPausesAndDistanceAcceleratesRepetition() = exercise { ui ->
        val gesture = DragGesture(ui, stepper.binding.plusButton)
        gesture.activate()
        assertEquals(50, stepper.getCount())
        assertTrue(parent.isInterceptBlocked)
        gesture.move(13f)
        gesture.waitFor(800)
        val slowChanges = stepper.getCount() - 50
        assertTrue(slowChanges in 1..2)
        gesture.move(0f)
        val paused = stepper.getCount()
        gesture.waitFor(550)
        assertEquals(paused, stepper.getCount())
        gesture.move(108f)
        gesture.waitFor(800)
        val fastChanges = stepper.getCount() - paused
        assertTrue("slow=$slowChanges, fast=$fastChanges", fastChanges > slowChanges * 2)
        gesture.release()
        assertFalse(parent.isInterceptBlocked)
        val released = stepper.getCount()
        gesture.waitFor(350)
        assertEquals(released, stepper.getCount())
    }

    @Test
    fun plusReversesAfterTheCapAndKeepsExpandedAtZero() = exercise { ui ->
        stepper.setMaxValue(2)
        stepper.setCount(1, animate = false)
        stepper.setOnCountChangeListener { stepper.setPlusButtonEnabled(it < 2) }
        val gesture = DragGesture(ui, stepper.binding.plusButton)
        gesture.activate()
        gesture.move(108f)
        gesture.waitFor(450)
        assertEquals(2, stepper.getCount())
        assertFalse(stepper.binding.plusButton.isEnabled)
        gesture.move(-108f)
        gesture.waitFor(450)
        assertEquals(0, stepper.getCount())
        gesture.move(108f)
        gesture.waitFor(450)
        assertEquals(2, stepper.getCount())
        gesture.release()
        assertEquals(stepper.context.dimenPixelSize(id.co.edtslib.uikit.R.dimen.dimen_90), stepper.binding.stepperContainer.width)
        assertTrue(stepper.binding.minusButton.isShown)
        assertFalse(stepper.binding.singlePlusButton.isShown)
    }

    @Test
    fun minusNeverIncreasesAndCancellationStopsPendingRepeats() = exercise { ui ->
        val gesture = DragGesture(ui, stepper.binding.minusButton)
        gesture.activate()
        gesture.move(108f)
        gesture.waitFor(500)
        assertEquals(50, stepper.getCount())
        gesture.move(-108f)
        gesture.waitFor(450)
        assertTrue(stepper.getCount() < 50)
        val accepted = stepper.getCount()
        stepper.cancelDragRepeat()
        gesture.waitFor(550)
        gesture.move(-108f)
        gesture.release()
        assertEquals(accepted, stepper.getCount())
    }

    @Test
    fun disablingAndDetachingStopsRepeatsAndReattachedViewCanStartAgain() = exercise { ui ->
        val first = DragGesture(ui, stepper.binding.plusButton)
        first.activate()
        first.move(108f)
        first.waitFor(300)
        stepper.isEnabled = false
        val accepted = stepper.getCount()
        first.waitFor(500)
        first.release()
        assertEquals(accepted, stepper.getCount())
        stepper.isEnabled = true
        val second = DragGesture(ui, stepper.binding.plusButton)
        second.activate()
        second.move(108f)
        second.waitFor(300)
        val beforeDetach = stepper.getCount()
        val params = stepper.layoutParams
        parent.removeView(stepper)
        second.waitFor(500)
        second.cancel()
        assertEquals(beforeDetach, stepper.getCount())
        parent.addView(stepper, params)
        second.waitFor(100)
        val third = DragGesture(ui, stepper.binding.plusButton)
        third.activate()
        third.move(108f)
        third.waitFor(300)
        third.release()
        assertTrue(stepper.getCount() > beforeDetach)
    }

    @Test
    fun optOutPreservesLegacyTapExpansionAndCollapse() = exercise { ui ->
        stepper.isDragRepeatEnabled = false
        stepper.setCount(0, animate = false)
        stepper.isAlwaysExpanded = false
        ui.loopMainThreadForAtLeast(100)
        val add = DragGesture(ui, stepper.binding.singlePlusButton)
        add.tap()
        add.waitFor(500)
        assertEquals(1, stepper.getCount())
        assertTrue(stepper.binding.minusButton.isShown)
        val remove = DragGesture(ui, stepper.binding.minusButton)
        remove.tap()
        remove.waitFor(500)
        assertEquals(0, stepper.getCount())
        assertTrue(stepper.binding.singlePlusButton.isShown)
        assertEquals(32.dp.toInt(), stepper.binding.stepperContainer.width)
    }

    private class CaptureParent(context: Context) : FrameLayout(context) {
        var isInterceptBlocked = false
            private set

        override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
            isInterceptBlocked = disallowIntercept
            super.requestDisallowInterceptTouchEvent(disallowIntercept)
        }
    }
}

/** Screen-coordinate injection exercises ancestor interception, rather than calling child listeners. */
internal class DragGesture(
    private val ui: UiController,
    button: View,
    offsetXDp: Float = 0f,
    offsetYDp: Float = 0f,
) {

    private val downTime = SystemClock.uptimeMillis()
    private val density = button.resources.displayMetrics.density
    private val x: Float
    private val originY: Float
    private var currentY: Float

    init {
        val location = IntArray(2)
        button.getLocationOnScreen(location)
        x = location[0] + button.width / 2f + offsetXDp * density
        originY = location[1] + button.height / 2f + offsetYDp * density
        currentY = originY
    }

    fun activate() {
        press()
        waitFor(ViewConfiguration.getLongPressTimeout().toLong() + 100)
    }

    fun press() { send(MotionEvent.ACTION_DOWN) }

    fun tap() {
        send(MotionEvent.ACTION_DOWN)
        waitFor(40)
        send(MotionEvent.ACTION_UP)
    }

    fun move(displacementDp: Float) {
        currentY = originY - displacementDp * density
        send(MotionEvent.ACTION_MOVE)
    }

    fun release() { send(MotionEvent.ACTION_UP) }
    fun cancel() { send(MotionEvent.ACTION_CANCEL) }
    fun waitFor(duration: Long) { ui.loopMainThreadForAtLeast(duration) }

    private fun send(action: Int) {
        val event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action, x, currentY, 0)
        event.source = InputDevice.SOURCE_TOUCHSCREEN
        try {
            assertTrue("Touch injection failed", ui.injectMotionEvent(event))
        } finally {
            event.recycle()
        }
    }
}
