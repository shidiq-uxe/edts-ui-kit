package id.co.edtslib.edtsuikit.tmh

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import id.co.edtslib.edtsuikit.GuidelinesTmhActivity
import id.co.edtslib.edtsuikit.R
import id.co.edtslib.edtsuikit.demo.promo.ParagonFairDemoActivity
import id.co.edtslib.uikit.pulltorefresh.LiteRefreshDelegate
import id.co.edtslib.uikit.pulltorefresh.LiteRefreshLayout
import id.co.edtslib.uikit.utils.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import id.co.edtslib.uikit.R as UIKitR

@RunWith(AndroidJUnit4::class)
class TmhPullRefreshTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    private fun settle(duration: Long = 2200L) {
        SystemClock.sleep(duration)
        instrumentation.waitForIdleSync()
    }

    private fun pppIntent(): Intent {
        val flow = TmhFlowState.create(context, TmhScenario.ELIGIBLE)
            .withSelection("fair-0", mapOf("emina" to 1))
        return Intent(context, ParagonFairDemoActivity::class.java)
            .putExtra(TmhFlowState.EXTRA_FLOW, flow.toBundle())
            .putExtra(TmhFlowState.EXTRA_FAIR_ID, "fair-0")
    }

    private fun pull(layout: LiteRefreshLayout, distance: Float = 240.dp, cancelled: Boolean = false) {
        val down = SystemClock.uptimeMillis()
        val x = layout.width * 0.5f
        val y = 40.dp
        fun send(action: Int, time: Long, newX: Float, newY: Float) {
            MotionEvent.obtain(down, time, action, newX, newY, 0).also {
                layout.dispatchTouchEvent(it)
                it.recycle()
            }
        }
        send(MotionEvent.ACTION_DOWN, down, x, y)
        for (step in 1..12) send(MotionEvent.ACTION_MOVE, down + step * 16, x, y + distance * step / 12)
        send(if (cancelled) MotionEvent.ACTION_CANCEL else MotionEvent.ACTION_UP, down + 208, x, y + distance)
    }

    private fun capture(name: String) {
        val directory = File(context.getExternalFilesDir(null), "pull-refresh").apply { mkdirs() }
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    @Test
    fun tmhAndPppRefreshOnceAndPreserveSelectionsAndCart() {
        ActivityScenario.launch(GuidelinesTmhActivity::class.java).use { scenario ->
            settle()
            var cart = ""
            var count = 0
            scenario.onActivity { screen ->
                cart = screen.findViewById<TextView>(R.id.tvCartCount).text.toString()
                val layout = screen.findViewById<LiteRefreshLayout>(R.id.refreshLayout)
                val delegate = layout.refreshDelegate!!
                layout.refreshDelegate = object : LiteRefreshDelegate {
                    override fun onPull(percent: Float, offset: Float) = Unit
                    override fun onRefreshing() { count++; delegate.onRefreshing() }
                    override fun onFinish() = Unit
                }
                pull(layout)
                assertTrue(layout.isRefreshing)
                assertFalse(layout.isEnabled)
                pull(layout)
                assertEquals(1, count)
            }
            settle(350)
            capture("tmh-refreshing")
            settle()
            scenario.onActivity { screen ->
                val layout = screen.findViewById<LiteRefreshLayout>(R.id.refreshLayout)
                assertFalse(layout.isRefreshing)
                assertTrue(layout.isEnabled)
                assertEquals(cart, screen.findViewById<TextView>(R.id.tvCartCount).text.toString())
                assertEquals(0f, layout.getChildAt(1).translationY, 0.1f)
            }
        }
        ActivityScenario.launch<ParagonFairDemoActivity>(pppIntent()).use { scenario ->
            settle()
            var total = ""
            scenario.onActivity { screen ->
                total = screen.findViewById<TextView>(R.id.cartTotal).text.toString()
                val layout = screen.findViewById<LiteRefreshLayout>(R.id.refreshLayout)
                pull(layout)
                assertTrue(layout.isRefreshing)
                assertFalse(layout.isEnabled)
                assertEquals(View.VISIBLE, screen.findViewById<View>(R.id.skeleton).visibility)
            }
            settle(350)
            capture("ppp-refreshing")
            settle()
            scenario.onActivity { screen ->
                val layout = screen.findViewById<LiteRefreshLayout>(R.id.refreshLayout)
                assertFalse(layout.isRefreshing)
                assertTrue(layout.isEnabled)
                assertEquals(View.GONE, screen.findViewById<View>(R.id.skeleton).visibility)
                assertEquals(total, screen.findViewById<TextView>(R.id.cartTotal).text.toString())
                screen.findViewById<View>(R.id.giftsAction).performClick()
            }
            settle(500)
            scenario.onActivity { screen ->
                val tray = screen.supportFragmentManager.findFragmentByTag(TmhRedemptionBottomSheet.TAG)!!
                assertEquals("1/2", tray.requireView().findViewById<TextView>(R.id.tvCounter).text.toString())
            }
        }
    }

    @Test
    fun shortCancelledAndScrolledPullsDoNotRefresh() {
        ActivityScenario.launch<ParagonFairDemoActivity>(pppIntent()).use { scenario ->
            settle()
            scenario.onActivity { screen ->
                val layout = screen.findViewById<LiteRefreshLayout>(R.id.refreshLayout)
                pull(layout, distance = 80.dp)
                assertFalse(layout.isRefreshing)
            }
            settle(350)
            scenario.onActivity { screen ->
                val layout = screen.findViewById<LiteRefreshLayout>(R.id.refreshLayout)
                assertEquals(0f, layout.getChildAt(1).translationY, 0.1f)
                pull(layout, cancelled = true)
                assertFalse(layout.isRefreshing)
            }
            settle(350)
            scenario.onActivity { screen ->
                val products = screen.findViewById<RecyclerView>(R.id.products)
                products.scrollBy(0, 600.dp.toInt())
            }
            settle(350)
            scenario.onActivity { screen ->
                val products = screen.findViewById<RecyclerView>(R.id.products)
                assertTrue(products.canScrollVertically(-1))
                val layout = screen.findViewById<LiteRefreshLayout>(R.id.refreshLayout)
                pull(layout, distance = 160.dp)
                assertFalse(layout.isRefreshing)
                assertEquals(View.GONE, screen.findViewById<View>(R.id.skeleton).visibility)
            }
        }
    }

    @Test
    fun disabledMotionStaysStaticAndHiddenOrDetachedHeadersStopSpinning() {
        val scale = shell("settings get global animator_duration_scale").trim()
        try {
            shell("settings put global animator_duration_scale 0")
            ActivityScenario.launch<ParagonFairDemoActivity>(pppIntent()).use { scenario ->
                settle()
                var rotation = 0f
                scenario.onActivity { screen ->
                    val layout = screen.findViewById<LiteRefreshLayout>(R.id.refreshLayout)
                    pull(layout)
                    assertTrue(layout.isRefreshing)
                    rotation = layout.findViewById<ImageView>(UIKitR.id.refreshIndicator).rotation
                }
                settle(300)
                capture("ppp-refreshing-motion-disabled")
                scenario.onActivity { screen ->
                    val image = screen.findViewById<ImageView>(UIKitR.id.refreshIndicator)
                    assertEquals(rotation, image.rotation, 0.1f)
                    assertEquals(1f, image.scaleX, 0.1f)
                }
                settle()
                scenario.onActivity { screen ->
                    assertFalse(screen.findViewById<LiteRefreshLayout>(R.id.refreshLayout).isRefreshing)
                }
            }
            shell("settings put global animator_duration_scale 1")
            ActivityScenario.launch<ParagonFairDemoActivity>(pppIntent()).use { scenario ->
                settle()
                lateinit var image: ImageView
                var rotation = 0f
                scenario.onActivity { screen ->
                    val layout = screen.findViewById<LiteRefreshLayout>(R.id.refreshLayout)
                    pull(layout)
                    image = layout.findViewById(UIKitR.id.refreshIndicator)
                }
                settle(300)
                scenario.onActivity { screen ->
                    val layout = screen.findViewById<LiteRefreshLayout>(R.id.refreshLayout)
                    assertTrue(image.rotation != 360f)
                    layout.visibility = View.INVISIBLE
                    rotation = image.rotation
                }
                settle(300)
                scenario.onActivity { screen ->
                    assertEquals(rotation, image.rotation, 0.1f)
                    val layout = screen.findViewById<LiteRefreshLayout>(R.id.refreshLayout)
                    layout.visibility = View.VISIBLE
                }
                settle(100)
                scenario.onActivity { screen ->
                    val layout = screen.findViewById<LiteRefreshLayout>(R.id.refreshLayout)
                    (layout.parent as ViewGroup).removeView(layout)
                    rotation = image.rotation
                    assertFalse(layout.isRefreshing)
                }
                settle(300)
                assertEquals(rotation, image.rotation, 0.1f)
            }
        } finally {
            if (scale == "null") shell("settings delete global animator_duration_scale")
            else shell("settings put global animator_duration_scale $scale")
        }
    }

    private fun shell(command: String): String = instrumentation.uiAutomation.executeShellCommand(command).use {
        java.io.FileInputStream(it.fileDescriptor).bufferedReader().readText()
    }
}
