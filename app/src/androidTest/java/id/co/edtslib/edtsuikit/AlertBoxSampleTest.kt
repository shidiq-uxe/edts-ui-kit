package id.co.edtslib.edtsuikit

import android.os.SystemClock
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import id.co.edtslib.uikit.core.alertbox.CoreAlertBox
import id.co.edtslib.uikit.recipes.alertbox.klik.KlikInfoBox
import id.co.edtslib.uikit.recipes.alertbox.poinku.PoinkuAlertBox
import org.hamcrest.Matchers.`is`
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import id.co.edtslib.uikit.R as UIKitR

@RunWith(AndroidJUnit4::class)
class AlertBoxSampleTest {
    private fun first(activity: GuidelinesAlertBoxActivity): CoreAlertBox? {
        val row = activity.findViewById<RecyclerView>(R.id.rvAlertBoxes).findViewHolderForAdapterPosition(0)?.itemView
        return row?.findViewById<FrameLayout>(R.id.flAlert)?.getChildAt(0) as? CoreAlertBox
    }

    private fun awaitState(scenario: ActivityScenario<GuidelinesAlertBoxActivity>, predicate: (GuidelinesAlertBoxActivity) -> Boolean) {
        val ready = CountDownLatch(1)
        scenario.onActivity { activity ->
            val view = activity.findViewById<RecyclerView>(R.id.rvAlertBoxes)
            val deadline = SystemClock.uptimeMillis() + 5000
            val check = object : Runnable {
                override fun run() {
                    if (predicate(activity)) ready.countDown()
                    else if (SystemClock.uptimeMillis() < deadline) view.postDelayed(this, 16)
                }
            }
            check.run()
        }
        assertTrue("Sample reached expected state", ready.await(6, TimeUnit.SECONDS))
    }

    private fun select(name: String) {
        onView(withId(R.id.tvProduct)).perform(click())
        onData(`is`(name)).perform(click())
    }

    @Test
    fun productSelectionConfigurationDismissalResetAndRecreationStayConsistent() {
        ActivityScenario.launch(GuidelinesAlertBoxActivity::class.java).use { scenario ->
            awaitState(scenario) { first(it)?.javaClass == CoreAlertBox::class.java && it.hasWindowFocus() }
            scenario.onActivity {
                assertEquals("Alert Box", it.findViewById<TextView>(R.id.tvTitle).text.toString())
            }
            select("Klik")
            awaitState(scenario) { first(it) is KlikInfoBox && it.findViewById<RecyclerView>(R.id.rvAlertBoxes).adapter?.itemCount == 6 }
            scenario.onActivity { it.onConfigMenuClicked() }
            onView(withId(R.id.swInlineAction)).perform(scrollTo(), click())
            onView(withId(R.id.swDisabled)).perform(scrollTo(), click())
            onView(withId(UIKitR.id.btnClose)).perform(click())
            awaitState(scenario) { it.hasWindowFocus() && first(it)?.isEnabled == false && first(it)?.isActionVisible == true }
            select("Poinku")
            awaitState(scenario) { first(it) is PoinkuAlertBox && first(it)?.isEnabled == true && first(it)?.isButtonVisible == false }
            scenario.recreate()
            awaitState(scenario) { first(it) is PoinkuAlertBox && it.hasWindowFocus() }
            scenario.onActivity { assertEquals("Poinku", it.findViewById<TextView>(R.id.tvProduct).text.toString()) }
            select("Klik")
            awaitState(scenario) { first(it) is KlikInfoBox && first(it)?.isEnabled == false && first(it)?.isActionVisible == true }
            onView(withId(R.id.btnResetAlerts)).perform(click())
            awaitState(scenario) { first(it)?.isEnabled == true && first(it)?.isActionVisible == false && first(it)?.isButtonVisible == true }
            scenario.onActivity { first(it)!!.findViewById<android.view.View>(UIKitR.id.ivAlertClose).performClick() }
            awaitState(scenario) { it.findViewById<RecyclerView>(R.id.rvAlertBoxes).adapter?.itemCount == 5 }
            onView(withId(R.id.btnResetAlerts)).perform(click())
            awaitState(scenario) { it.findViewById<RecyclerView>(R.id.rvAlertBoxes).adapter?.itemCount == 6 }
        }
    }
}
