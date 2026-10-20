package id.co.edtslib.edtsuikit.tmh

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import id.co.edtslib.edtsuikit.GuidelinesTmhActivity
import id.co.edtslib.edtsuikit.R
import id.co.edtslib.edtsuikit.databinding.ItemTmhRedemptionProductBinding
import id.co.edtslib.uikit.adapter.BaseAdapter
import id.co.edtslib.uikit.stepper.StepperButton
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import id.co.edtslib.uikit.R as UIKitR

@RunWith(AndroidJUnit4::class)
class TmhPppTrayRegressionTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    private fun settle() {
        SystemClock.sleep(2200)
        instrumentation.waitForIdleSync()
    }

    private fun capture(name: String) {
        val directory = File(context.getExternalFilesDir(null), "ppp-tray-fixed").apply { mkdirs() }
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    private fun tray(screen: GuidelinesTmhActivity): TmhRedemptionBottomSheet =
        screen.supportFragmentManager.findFragmentByTag(TmhRedemptionBottomSheet.TAG) as TmhRedemptionBottomSheet

    private fun product(root: View, index: Int = 0): View =
        root.findViewById<RecyclerView>(R.id.rvProducts).findViewHolderForAdapterPosition(index)!!.itemView

    private fun stepper(card: View): StepperButton = card.findViewById(R.id.stepper)

    private fun assertNonClickable(card: View) {
        assertFalse(card.isClickable)
        assertFalse(card.hasOnClickListeners())
        assertFalse(card.performClick())
    }

    private fun position(view: View): Rect {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        return Rect(location[0], location[1], location[0] + view.width, location[1] + view.height)
    }

    private fun assertFooter(root: View, actionId: Int) {
        val footer = root.findViewById<View>(R.id.footer)
        val shadow = root.findViewById<View>(R.id.footerShadow)
        assertTrue(shadow.isShown)
        assertEquals(4.dp.toInt(), shadow.height)
        assertEquals(footer.width, shadow.width)
        assertEquals(position(footer).top, position(shadow).bottom)
        val visible = Rect()
        assertTrue(shadow.getGlobalVisibleRect(visible))
        assertEquals(shadow.height, visible.height())
        val safeBottom = ViewCompat.getRootWindowInsets(root)
            ?.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())?.bottom ?: 0
        assertTrue(footer.paddingBottom >= 16.dp.toInt() + safeBottom)
        val button = root.findViewById<View>(actionId)
        assertTrue(position(button).bottom <= position(footer).bottom - safeBottom)
    }

    @Test
    fun adapterOptOutKeepsChildActionsAndExistingDefaultClicksAcrossRebinding() {
        ActivityScenario.launch(GuidelinesTmhActivity::class.java).use { scenario ->
            settle()
            scenario.onActivity { screen ->
                for (enabled in listOf(true, false)) {
                    var rootClicks = 0
                    var childClicks = 0
                    val adapter = BaseAdapter.adapterOf<Int, ItemTmhRedemptionProductBinding>(
                        register = BaseAdapter.Register(onBindHolder = { _, _, row, _ ->
                            row.root.setOnClickListener { rootClicks++ }
                            row.stepper.binding.plusButton.setOnClickListener { childClicks++ }
                        }),
                        diff = BaseAdapter.Diff(
                            areItemsTheSame = { old, new -> old == new },
                            areContentsTheSame = { old, new -> old == new },
                        ),
                        itemList = listOf(1),
                    )
                    if (!enabled) adapter.isItemClickEnabled = false
                    adapter.setOnItemClickListener { _, _, _ -> rootClicks++ }
                    val holder = adapter.onCreateViewHolder(FrameLayout(screen), 0)
                    repeat(2) {
                        adapter.onBindViewHolder(holder, 0)
                        assertEquals(enabled, holder.itemView.isClickable)
                        assertEquals(enabled, holder.itemView.hasOnClickListeners())
                        assertEquals(enabled, holder.itemView.performClick())
                        holder.itemView.findViewById<StepperButton>(R.id.stepper).binding.plusButton.performClick()
                    }
                    assertEquals(if (enabled) 2 else 0, rootClicks)
                    assertEquals(2, childClicks)
                }
                val fairs = screen.findViewById<RecyclerView>(R.id.rvFairs)
                val fair = fairs.findViewHolderForAdapterPosition(0)!!.itemView
                assertTrue("Fair cards retain their click actions", fair.isClickable && fair.hasOnClickListeners())
                val preview = fair.findViewById<RecyclerView>(R.id.rvProducts).getChildAt(0)
                assertTrue("Product previews outside the tray retain their click actions", preview.isClickable)
            }
        }
    }

    @Test
    fun unmetPppHidesAlertAndBothContextsGrayZeroAllowanceWithoutDisablingActiveCounters() {
        val initial = TmhFlowState.create(context, TmhScenario.DEFAULT)
        for (isPromoDetail in listOf(false, true)) {
            ActivityScenario.launch(GuidelinesTmhActivity::class.java).use { scenario ->
                settle()
                scenario.onActivity { screen ->
                    TmhRedemptionBottomSheet.newInstance(initial.fair("fair-3"), TmhScenario.DEFAULT, isPromoDetail)
                        .showNow(screen.supportFragmentManager, TmhRedemptionBottomSheet.TAG)
                }
                settle()
                scenario.onActivity { screen ->
                    val root = tray(screen).requireView()
                    assertEquals(!isPromoDetail, root.findViewById<View>(R.id.alertBox).isShown)
                    val counter = root.findViewById<TextView>(R.id.tvCounter)
                    assertEquals("0/0", counter.text.toString())
                    assertEquals(screen.color(UIKitR.color.black_40), counter.currentTextColor)
                    assertEquals(screen.color(UIKitR.color.black_20), counter.backgroundTintList!!.defaultColor)
                    assertNonClickable(product(root))
                    assertFalse(stepper(product(root)).binding.plusButton.isEnabled)
                    assertFalse(root.findViewById<View>(R.id.btnSave).isEnabled)
                    assertFooter(root, R.id.btnSave)
                }
                capture("unmet-$isPromoDetail")
            }
            val model = initial.modelFor("fair-3")
            val eligible = initial.withModel("fair-3", model.withQuantity(model.products.first().id, 3)).fair("fair-3")
            ActivityScenario.launch(GuidelinesTmhActivity::class.java).use { scenario ->
                settle()
                scenario.onActivity { screen ->
                    TmhRedemptionBottomSheet.newInstance(eligible, TmhScenario.DEFAULT, isPromoDetail)
                        .showNow(screen.supportFragmentManager, TmhRedemptionBottomSheet.TAG)
                }
                settle()
                scenario.onActivity { screen ->
                    val root = tray(screen).requireView()
                    val counter = root.findViewById<TextView>(R.id.tvCounter)
                    assertEquals("0/1", counter.text.toString())
                    assertEquals(screen.color(UIKitR.color.primary_30), counter.currentTextColor)
                    assertEquals(screen.color(UIKitR.color.support_selected), counter.backgroundTintList!!.defaultColor)
                    assertNonClickable(product(root))
                    stepper(product(root)).binding.plusButton.performClick()
                }
                settle()
                scenario.onActivity { screen ->
                    val root = tray(screen).requireView()
                    assertEquals("1/1", root.findViewById<TextView>(R.id.tvCounter).text.toString())
                    assertNonClickable(product(root))
                    assertTrue(root.findViewById<View>(R.id.btnSave).isEnabled)
                    assertFooter(root, R.id.btnSave)
                    stepper(product(root)).binding.minusButton.performClick()
                }
                settle()
                scenario.onActivity { screen ->
                    val root = tray(screen).requireView()
                    assertEquals("0/1", root.findViewById<TextView>(R.id.tvCounter).text.toString())
                    assertNonClickable(product(root))
                    assertTrue(stepper(product(root)).binding.plusButton.isEnabled)
                }
                capture("eligible-$isPromoDetail")
            }
        }
    }

    @Test
    fun footerStaysFixedDuringScrollAndUnsavedActionsKeepShadowAndSafeArea() {
        ActivityScenario.launch(GuidelinesTmhActivity::class.java).use { scenario ->
            settle()
            scenario.onActivity { screen ->
                val fair = TmhFlowState.create(screen, TmhScenario.DEFAULT).fair("fair-2")
                TmhRedemptionBottomSheet.newInstance(fair, TmhScenario.DEFAULT)
                    .showNow(screen.supportFragmentManager, TmhRedemptionBottomSheet.TAG)
            }
            settle()
            lateinit var footerBefore: Rect
            scenario.onActivity { screen ->
                val root = tray(screen).requireView()
                assertFooter(root, R.id.btnSave)
                footerBefore = position(root.findViewById(R.id.footer))
                stepper(product(root)).binding.plusButton.performClick()
                val scroll = root.findViewById<NestedScrollView>(R.id.scrollContent)
                assertTrue("Use overflowing content to verify the fixed footer", scroll.canScrollVertically(1))
                scroll.fullScroll(View.FOCUS_DOWN)
            }
            settle()
            scenario.onActivity { screen ->
                val root = tray(screen).requireView()
                assertEquals(footerBefore, position(root.findViewById(R.id.footer)))
                assertTrue(root.findViewById<NestedScrollView>(R.id.scrollContent).scrollY > 0)
                assertFooter(root, R.id.btnSave)
            }
            capture("scrolled-redemption")
            pressBack()
            settle()
            scenario.onActivity { screen ->
                val sheet = tray(screen).childFragmentManager.findFragmentByTag(TmhUnsavedBottomSheet.TAG)!!
                assertFooter(sheet.requireView(), R.id.btnLeave)
            }
            capture("unsaved")
            scenario.onActivity { screen ->
                tray(screen).childFragmentManager.findFragmentByTag(TmhUnsavedBottomSheet.TAG)!!
                    .requireView().findViewById<View>(R.id.btnStay).performClick()
            }
            settle()
            scenario.onActivity { screen ->
                val sheet = tray(screen)
                assertEquals(null, sheet.childFragmentManager.findFragmentByTag(TmhUnsavedBottomSheet.TAG))
                assertEquals("1/2", sheet.requireView().findViewById<TextView>(R.id.tvCounter).text.toString())
            }
            pressBack()
            settle()
            scenario.onActivity { screen ->
                tray(screen).childFragmentManager.findFragmentByTag(TmhUnsavedBottomSheet.TAG)!!
                    .requireView().findViewById<View>(R.id.btnLeave).performClick()
            }
            settle()
            scenario.onActivity { screen ->
                assertEquals(null, screen.supportFragmentManager.findFragmentByTag(TmhRedemptionBottomSheet.TAG))
            }
        }
    }
}
