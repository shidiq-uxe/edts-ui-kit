package id.co.edtslib.edtsuikit

import android.graphics.Bitmap
import android.os.SystemClock
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.materialswitch.MaterialSwitch
import id.co.edtslib.uikit.core.otp.CoreOtpGroup
import id.co.edtslib.uikit.core.textfield.CoreTextField
import id.co.edtslib.uikit.recipes.textfield.poinku.PoinkuTextField
import id.co.edtslib.uikit.textfield.TextField
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import id.co.edtslib.uikit.R as UIKitR

@RunWith(AndroidJUnit4::class)
class TextFieldGuideConfigTest {


    @Test
    fun sheetEditsExistingPreviewAndReconcilesLengthAndAction() {
        ActivityScenario.launch(GuidelinesTextfieldActivity::class.java).use { scenario ->
            var preview: CoreTextField? = null
            scenario.onActivity {
                preview = field(it)
                it.onConfigMenuClicked()
            }
            awaitSheetExpanded(scenario)
            onView(withId(R.id.etLabel)).check(matches(isDisplayed()))
            onView(withId(R.id.etLabel)).perform(replaceText("Email"), closeSoftKeyboard())
            capture("textfield_config_top")
            onView(withId(R.id.etValue)).perform(scrollTo(), replaceText("abcdef"), closeSoftKeyboard())
            onView(withId(R.id.etMaxLength)).perform(scrollTo(), replaceText("3"), closeSoftKeyboard())
            scenario.onActivity { activity ->
                assertSame(preview, field(activity))
                assertEquals("Email", field(activity).label)
                assertEquals("abc", field(activity).text.toString())
                assertEquals("3/3", field(activity).findViewById<TextView>(UIKitR.id.tvCounter).text.toString())
                val controls = sheet(activity).requireView()
                assertEquals("abc", controls.findViewById<EditText>(R.id.etValue).text.toString())
                controls.findViewById<Spinner>(R.id.spStartIcon).setSelection(TextFieldIcon.NONE.ordinal)
            }
            onView(withId(R.id.spStartIcon)).perform(scrollTo())
            scenario.onActivity { activity ->
                assertNull(field(activity).startIcon)
                val controls = sheet(activity).requireView()
                controls.findViewById<Spinner>(R.id.spEndIcon).setSelection(TextFieldIcon.CLOSE.ordinal)
                controls.findViewById<MaterialSwitch>(R.id.swClearAction).isChecked = true
            }
            onView(withId(R.id.spEndIcon)).perform(scrollTo())
            scenario.onActivity { activity ->
                assertNotNull(field(activity).endIcon)
            }
            onView(withId(UIKitR.id.btnClose)).perform(click())
            awaitSheetDismissed(scenario)
            onView(withId(R.id.flPreview)).check(matches(isDisplayed()))
            scenario.onActivity { activity ->
                assertTrue(field(activity).findViewById<View>(UIKitR.id.btnEnd).performClick())
                assertEquals("", field(activity).text.toString())
                activity.onConfigMenuClicked()
            }
            awaitSheetExpanded(scenario)
            onView(withId(R.id.etLabel)).check(matches(isDisplayed()))
            scenario.onActivity { activity ->
                assertEquals("", sheet(activity).requireView().findViewById<EditText>(R.id.etValue).text.toString())
            }
            onView(withId(R.id.nsvSettings)).perform(swipeUp(), swipeUp())
            onView(withId(R.id.etActionDescription)).check(matches(isDisplayed()))
            capture("textfield_config_bottom")
            onView(withId(R.id.btnResetConfig)).perform(click())
            awaitSheetDismissed(scenario)
            scenario.onActivity { activity ->
                assertEquals("Label", field(activity).label)
                assertEquals(250, field(activity).maxLength)
                assertNotNull(field(activity).startIcon)
            }
        }
    }

    @Test
    fun productSettingsAndOpenSheetSurviveRecreation() {
        ActivityScenario.launch(GuidelinesTextfieldActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                field(activity).text = "Klik value"
            }
            selectProduct("Poinku")
            scenario.onActivity {
                assertTrue(field(it) is PoinkuTextField)
                assertEquals("", field(it).text.toString())
                it.onConfigMenuClicked()
            }
            awaitSheetExpanded(scenario)
            onView(withId(R.id.etLabel)).perform(replaceText("Poinku label"), closeSoftKeyboard())
            onView(withId(R.id.etValue)).perform(scrollTo(), replaceText("123456"), closeSoftKeyboard())
            scenario.recreate()
            awaitSheetExpanded(scenario)
            onView(withId(R.id.etLabel)).check(matches(withText("Poinku label")))
            scenario.onActivity {
                assertEquals("Poinku", it.findViewById<TextView>(R.id.tvProduct).text.toString())
                assertEquals("123456", field(it).text.toString())
            }
            onView(withId(UIKitR.id.btnClose)).perform(click())
            awaitSheetDismissed(scenario)
            selectProduct("Klik")
            scenario.onActivity { assertEquals("Klik value", field(it).text.toString()) }
            selectProduct("Poinku")
            scenario.onActivity {
                assertEquals("123456", field(it).text.toString())
                assertEquals("Poinku label", field(it).label)
            }
        }
    }

    @Test
    fun passwordToggleOwnsTrailingSlotAndReleasesItForText() {
        ActivityScenario.launch(GuidelinesTextfieldActivity::class.java).use { scenario ->
            selectProduct("Poinku")
            scenario.onActivity { it.onConfigMenuClicked() }
            awaitSheetExpanded(scenario)
            onView(withId(R.id.etLabel)).check(matches(isDisplayed()))
            scenario.onActivity {
                sheet(it).requireView().findViewById<Spinner>(R.id.spInput).setSelection(TextFieldInput.PASSWORD.ordinal)
            }
            onView(withId(R.id.spInput)).perform(scrollTo())
            scenario.onActivity {
                assertFalse(sheet(it).requireView().findViewById<Spinner>(R.id.spEndIcon).isEnabled)
                assertTrue(field(it).passwordToggleEnabled)
                sheet(it).requireView().findViewById<Spinner>(R.id.spInput).setSelection(TextFieldInput.TEXT.ordinal)
            }
            onView(withId(R.id.spInput)).perform(scrollTo())
            scenario.onActivity {
                assertTrue(sheet(it).requireView().findViewById<Spinner>(R.id.spEndIcon).isEnabled)
                assertFalse(field(it).findViewById<View>(UIKitR.id.btnEnd).isClickable)
            }
        }
    }

    @Test
    fun legacyPreviewReplacesLimitsAndStylesOnlyTheRequiredMarker() {
        ActivityScenario.launch(GuidelinesTextfieldActivity::class.java).use { scenario ->
            selectProduct("Legacy")
            scenario.onActivity { it.onConfigMenuClicked() }
            awaitSheetExpanded(scenario)
            onView(withId(R.id.etLabel)).perform(replaceText("Name *"), closeSoftKeyboard())
            onView(withId(R.id.etValue)).perform(scrollTo(), replaceText("abcdef"), closeSoftKeyboard())
            onView(withId(R.id.etMaxLength)).perform(scrollTo(), replaceText("3"), closeSoftKeyboard())
            scenario.onActivity {
                val preview = it.findViewById<FrameLayout>(R.id.flPreview).getChildAt(0) as TextField
                assertEquals("abc", preview.editText?.text.toString())
                val hint = preview.hint as Spanned
                assertEquals("Name * *", hint.toString())
                val colors = hint.getSpans(0, hint.length, ForegroundColorSpan::class.java)
                assertEquals(1, colors.size)
                assertEquals(hint.length - 1, hint.getSpanStart(colors.single()))
            }
            onView(withId(R.id.etMaxLength)).perform(scrollTo(), replaceText("8"), closeSoftKeyboard())
            onView(withId(R.id.etValue)).perform(scrollTo(), replaceText("abcdefgh"), closeSoftKeyboard())
            onView(withId(R.id.swRequired)).perform(scrollTo(), click())
            scenario.onActivity {
                val preview = it.findViewById<FrameLayout>(R.id.flPreview).getChildAt(0) as TextField
                assertEquals("abcdefgh", preview.editText?.text.toString())
                assertEquals("Name *", preview.hint.toString())
            }
        }
    }

    @Test
    fun otpToolbarSelectsAvailableRecipesAndRestoresSelectionAndInput() {
        ActivityScenario.launch(GuidelinesOtpActivity::class.java).use { scenario ->
            scenario.onActivity {
                assertEquals("Poinku", it.findViewById<TextView>(R.id.tvProduct).text.toString())
                assertTrue(it.findViewById<View>(R.id.ogPoinkuDefault).isShown)
                assertFalse(it.findViewById<View>(R.id.ogDefault).isShown)
            }
            selectProduct("Core")
            scenario.onActivity {
                it.findViewById<CoreOtpGroup>(R.id.ogCoreDefault).otp = "12"
                assertFalse(it.findViewById<View>(R.id.ogPoinkuDefault).isShown)
            }
            scenario.recreate()
            scenario.onActivity {
                assertEquals("Core", it.findViewById<TextView>(R.id.tvProduct).text.toString())
                assertEquals("12", it.findViewById<CoreOtpGroup>(R.id.ogCoreDefault).otp)
            }
            selectProduct("Legacy")
            scenario.onActivity {
                assertTrue(it.findViewById<View>(R.id.ogDefault).isShown)
                assertFalse(it.findViewById<View>(R.id.llCoreExamples).isShown)
            }
        }
    }

    private fun capture(name: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val rendered = CountDownLatch(1)
        instrumentation.runOnMainSync {
            val activity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).single()
            activity.window.decorView.postOnAnimation {
                activity.window.decorView.postOnAnimation { rendered.countDown() }
            }
        }
        assertTrue("The capture waits for rendered frames", rendered.await(2, TimeUnit.SECONDS))
        val screenshot = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        File(instrumentation.targetContext.filesDir, "$name.png").outputStream().use {
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        screenshot.recycle()
    }

    private fun awaitSheetExpanded(scenario: ActivityScenario<GuidelinesTextfieldActivity>) {
        val ready = CountDownLatch(1)
        scenario.onActivity { activity ->
            val deadline = SystemClock.uptimeMillis() + 5000
            val check = object : Runnable {
                override fun run() {
                    val sheet = activity.supportFragmentManager.findFragmentByTag(TextFieldConfigBottomSheet.TAG)
                        as? TextFieldConfigBottomSheet
                    if (sheet?.getBottomSheetBehavior()?.state == BottomSheetBehavior.STATE_EXPANDED &&
                        sheet.requireView().hasWindowFocus()) {
                        ready.countDown()
                    } else if (SystemClock.uptimeMillis() < deadline) {
                        activity.window.decorView.postDelayed(this, 16)
                    }
                }
            }
            check.run()
        }
        assertTrue("The configuration sheet expands before editing", ready.await(6, TimeUnit.SECONDS))
    }

    private fun awaitSheetDismissed(scenario: ActivityScenario<GuidelinesTextfieldActivity>) {
        val ready = CountDownLatch(1)
        scenario.onActivity { activity ->
            val deadline = SystemClock.uptimeMillis() + 5000
            val check = object : Runnable {
                override fun run() {
                    val sheet = activity.supportFragmentManager.findFragmentByTag(TextFieldConfigBottomSheet.TAG)
                    if (sheet == null && activity.hasWindowFocus()) {
                        ready.countDown()
                    } else if (SystemClock.uptimeMillis() < deadline) {
                        activity.window.decorView.postDelayed(this, 16)
                    }
                }
            }
            check.run()
        }
        assertTrue("The sheet closes and returns focus to the guide", ready.await(6, TimeUnit.SECONDS))
    }

    private fun field(activity: GuidelinesTextfieldActivity): CoreTextField {
        return activity.findViewById<FrameLayout>(R.id.flPreview).getChildAt(0) as CoreTextField
    }

    private fun sheet(activity: GuidelinesTextfieldActivity): TextFieldConfigBottomSheet {
        return activity.supportFragmentManager.findFragmentByTag(TextFieldConfigBottomSheet.TAG) as TextFieldConfigBottomSheet
    }

    private fun selectProduct(name: String) {
        onView(withId(R.id.tvProduct)).perform(click())
        try {
            onData(`is`(name)).inRoot(isDialog()).perform(click())
        } catch (failure: Throwable) {
            capture("textfield_product_failure")
            throw failure
        }
        val ready = CountDownLatch(1)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val activity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).single()
            val label = activity.findViewById<TextView>(R.id.tvProduct)
            val deadline = SystemClock.uptimeMillis() + 5000
            val check = object : Runnable {
                override fun run() {
                    if (label.hasWindowFocus() && label.text.toString() == name) {
                        ready.countDown()
                    } else if (SystemClock.uptimeMillis() < deadline) {
                        label.postOnAnimation(this)
                    }
                }
            }
            label.postOnAnimation(check)
        }
        assertTrue("Product selection returns focus to the guide", ready.await(6, TimeUnit.SECONDS))
    }
}
