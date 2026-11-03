package id.co.edtslib.edtsuikit

import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import id.co.edtslib.uikit.core.button.CoreButton
import id.co.edtslib.uikit.recipes.button.klik.KlikButton
import id.co.edtslib.uikit.recipes.button.poinku.PoinkuButton
import id.co.edtslib.uikit.utils.dp
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@RunWith(AndroidJUnit4::class)
class ButtonSampleTest {

    private fun selectProduct(name: String) {
        onView(withId(R.id.tvProduct)).perform(click())
        onData(`is`(name)).perform(click())
    }

    private fun awaitCount(scenario: ActivityScenario<GuidelinesButtonActivity>, count: Int) {
        val updated = CountDownLatch(1)
        scenario.onActivity { activity ->
            val adapter = activity.findViewById<RecyclerView>(R.id.rvButtons).adapter!!
            val observer = object : RecyclerView.AdapterDataObserver() {
                private fun checkCount() {
                    if (adapter.itemCount == count) {
                        adapter.unregisterAdapterDataObserver(this)
                        updated.countDown()
                    }
                }

                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = checkCount()
                override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = checkCount()
                override fun onChanged() = checkCount()
            }
            if (adapter.itemCount == count) updated.countDown() else adapter.registerAdapterDataObserver(observer)
        }
        assertTrue("Button list updated", updated.await(5, TimeUnit.SECONDS))
        onView(withId(R.id.rvButtons)).check(matches(isDisplayed()))
    }

    private fun firstButton(activity: GuidelinesButtonActivity): CoreButton {
        val list = activity.findViewById<RecyclerView>(R.id.rvButtons)
        val row = list.findViewHolderForAdapterPosition(0)!!.itemView
        return row.findViewById<FrameLayout>(R.id.buttonHost).tag as CoreButton
    }

    @Test
    fun sampleUsesNeutralCoreAndProductRecipesWithoutDefaultOverrides() {
        ActivityScenario.launch(GuidelinesButtonActivity::class.java).use { scenario ->
            awaitCount(scenario, 3)
            scenario.onActivity { activity ->
                assertEquals(CoreButton::class.java, firstButton(activity).javaClass)
                assertEquals("Core", activity.findViewById<TextView>(R.id.tvProduct).text.toString())
                activity.onConfigMenuClicked()
            }
            onView(withId(R.id.tvSizeFilterLabel)).check(matches(not(isDisplayed())))
            onView(withId(R.id.swDestructive)).check(matches(not(isDisplayed())))
            scenario.onActivity { activity ->
                (activity.supportFragmentManager.findFragmentByTag(ButtonConfigBottomSheet.TAG) as ButtonConfigBottomSheet).dismiss()
            }
            selectProduct("Klik")
            awaitCount(scenario, 18)
            scenario.onActivity { activity ->
                assertTrue(firstButton(activity) is KlikButton)
                assertEquals(6.dp.roundToInt(), firstButton(activity).cornerRadius)
            }
            selectProduct("Poinku")
            awaitCount(scenario, 21)
            scenario.onActivity { activity ->
                assertTrue(firstButton(activity) is PoinkuButton)
                assertEquals(4.dp.roundToInt(), firstButton(activity).cornerRadius)
                activity.onConfigMenuClicked()
            }
            onView(withId(R.id.swDestructive)).check(matches(not(isDisplayed())))
            onView(withId(R.id.tvSizeFilterLabel)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun sharedSheetUpdatesResetsAndRestoresEachProductsSettings() {
        ActivityScenario.launch(GuidelinesButtonActivity::class.java).use { scenario ->
            selectProduct("Klik")
            awaitCount(scenario, 18)
            scenario.onActivity { it.onConfigMenuClicked() }
            onView(withId(R.id.swPressed)).perform(click())
            onView(withId(R.id.swDestructive)).perform(click())
            scenario.onActivity { activity ->
                (activity.supportFragmentManager.findFragmentByTag(ButtonConfigBottomSheet.TAG) as ButtonConfigBottomSheet).dismiss()
            }
            onView(withId(R.id.rvButtons)).check(matches(isDisplayed()))
            scenario.onActivity { activity ->
                assertTrue(firstButton(activity).isPressed)
                assertTrue(firstButton(activity).isDestructive)
                val overridden = ButtonConfig(pressed = true, cornerRadius = 12f, tintColor = 0xFF1A9F67.toInt())
                activity.supportFragmentManager.setFragmentResult(ButtonConfigBottomSheet.RESULT_CONFIG, overridden.toBundle())
            }
            selectProduct("Poinku")
            awaitCount(scenario, 21)
            scenario.onActivity { activity ->
                assertFalse(firstButton(activity).isPressed)
                assertFalse(firstButton(activity).isDestructive)
                assertEquals(4.dp.roundToInt(), firstButton(activity).cornerRadius)
                activity.onConfigMenuClicked()
            }
            onView(withId(R.id.swDisabled)).perform(click())
            onView(withId(R.id.btnReset)).perform(click())
            onView(withId(R.id.rvButtons)).check(matches(isDisplayed()))
            scenario.onActivity { activity -> assertTrue(firstButton(activity).isEnabled) }
            selectProduct("Klik")
            awaitCount(scenario, 18)
            scenario.recreate()
            awaitCount(scenario, 18)
            scenario.onActivity { activity ->
                val button = firstButton(activity)
                assertTrue(button.isPressed)
                assertEquals(12.dp.roundToInt(), button.cornerRadius)
                assertEquals(0xFF1A9F67.toInt(), button.backgroundTintList!!.defaultColor)
                assertEquals("Klik", activity.findViewById<TextView>(R.id.tvProduct).text.toString())
                activity.onConfigMenuClicked()
            }
            onView(withId(R.id.btnReset)).perform(click())
            onView(withId(R.id.rvButtons)).check(matches(isDisplayed()))
            scenario.onActivity { activity ->
                assertFalse(firstButton(activity).isPressed)
                assertEquals(6.dp.roundToInt(), firstButton(activity).cornerRadius)
                assertNotNull(firstButton(activity).startIcon)
            }
        }
    }
}
