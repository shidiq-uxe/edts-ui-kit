package id.co.edtslib.edtsuikit.tmh

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import id.co.edtslib.edtsuikit.R
import id.co.edtslib.edtsuikit.demo.promo.ParagonFairDemoActivity
import id.co.edtslib.uikit.stepper.StepperButton
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StandaloneRedemptionReuseTest {

    private fun launch(scenario: String): ActivityScenario<ParagonFairDemoActivity> {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return ActivityScenario.launch(Intent(context, ParagonFairDemoActivity::class.java)
            .putExtra(ParagonFairDemoActivity.EXTRA_SCENARIO, scenario))
    }

    private fun settle() {
        SystemClock.sleep(1600)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    private fun capture(name: String) {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        for (command in listOf(
            "mkdir -p /sdcard/Download/tmh-component-reuse",
            "screencap -p /sdcard/Download/tmh-component-reuse/$name.png",
        )) {
            automation.executeShellCommand(command).use { descriptor ->
                java.io.FileInputStream(descriptor.fileDescriptor).use { it.readBytes() }
            }
        }
    }

    private fun open(activity: ActivityScenario<ParagonFairDemoActivity>) {
        activity.onActivity { it.findViewById<View>(R.id.giftsAction).performClick() }
        settle()
    }

    private fun tray(screen: ParagonFairDemoActivity): TmhRedemptionBottomSheet =
        screen.supportFragmentManager.findFragmentByTag(TmhRedemptionBottomSheet.TAG) as TmhRedemptionBottomSheet

    private fun product(sheet: TmhRedemptionBottomSheet, index: Int = 0): View =
        requireNotNull(requireNotNull(sheet.view).findViewById<RecyclerView>(R.id.rvProducts)
            .findViewHolderForAdapterPosition(index)).itemView

    @Test
    fun standaloneUsesSharedTrayAndPreservesPackPricesLimitsAndPendingRecreation() {
        launch("ELIGIBLE").use { activity ->
            settle()
            open(activity)
            capture("standalone-fresh")
            activity.onActivity { screen ->
                val sheet = tray(screen)
                val root = requireNotNull(sheet.view)
                assertEquals(View.GONE, root.findViewById<View>(R.id.tvTerms).visibility)
                assertEquals(View.GONE, root.findViewById<View>(R.id.tvDescription).visibility)
                assertEquals("0/1", root.findViewById<TextView>(R.id.tvCounter).text.toString())
                val card = product(sheet)
                assertEquals("Isi 2 pcs", card.findViewById<TextView>(R.id.tvPack).text.toString())
                assertEquals("Rp25.000", card.findViewById<TextView>(R.id.tvPrice).text.toString())
                assertFalse(card.isClickable)
                card.findViewById<StepperButton>(R.id.stepper).binding.plusButton.performClick()
            }
            settle()
            activity.recreate()
            settle()
            capture("standalone-selected")
            activity.onActivity { screen ->
                val sheet = tray(screen)
                val card = product(sheet)
                assertEquals("1/1", sheet.view?.findViewById<TextView>(R.id.tvCounter)?.text.toString())
                assertTrue(card.findViewById<TextView>(R.id.tvTotal).text.toString().contains("2 pcs"))
                assertFalse(card.findViewById<StepperButton>(R.id.stepper).binding.plusButton.isEnabled)
                assertTrue(card.findViewById<StepperButton>(R.id.stepper).binding.minusButton.isEnabled)
                sheet.view?.findViewById<View>(R.id.btnSave)?.performClick()
            }
            settle()
            open(activity)
            activity.onActivity { screen ->
                val sheet = tray(screen)
                assertEquals("1/1", sheet.view?.findViewById<TextView>(R.id.tvCounter)?.text.toString())
                assertFalse(requireNotNull(sheet.view?.findViewById<View>(R.id.btnSave)).isEnabled)
            }
        }
    }

    @Test
    fun standaloneRemovalKeepsDiscardGuardAndCommitsOnlyAfterSaving() {
        launch("CLAIMED").use { activity ->
            settle()
            open(activity)
            activity.onActivity { screen ->
                product(tray(screen)).findViewById<StepperButton>(R.id.stepper).binding.minusButton.performClick()
            }
            settle()
            activity.onActivity { screen ->
                val sheet = tray(screen)
                assertEquals("Hapus", sheet.view?.findViewById<TextView>(R.id.btnSave)?.text.toString())
                sheet.binding.btnClose.performClick()
            }
            settle()
            activity.onActivity { screen ->
                val sheet = tray(screen)
                val guard = sheet.childFragmentManager.findFragmentByTag(TmhUnsavedBottomSheet.TAG)
                assertTrue(guard is TmhUnsavedBottomSheet)
                guard?.view?.findViewById<View>(R.id.btnStay)?.performClick()
            }
            settle()
            activity.onActivity { screen -> tray(screen).view?.findViewById<View>(R.id.btnSave)?.performClick() }
            settle()
            open(activity)
            activity.onActivity { screen ->
                assertEquals("0/1", tray(screen).view?.findViewById<TextView>(R.id.tvCounter)?.text.toString())
            }
        }
    }

    @Test
    fun standaloneRemovingOneSelectedLineKeepsTheOtherLineAndUsesHapus() {
        launch("REPEATABLE_PARTIAL").use { activity ->
            settle()
            open(activity)
            activity.onActivity { screen ->
                product(tray(screen), 1).findViewById<StepperButton>(R.id.stepper).binding.plusButton.performClick()
            }
            settle()
            activity.onActivity { screen -> tray(screen).view?.findViewById<View>(R.id.btnSave)?.performClick() }
            settle()
            open(activity)
            activity.onActivity { screen ->
                product(tray(screen)).findViewById<StepperButton>(R.id.stepper).binding.minusButton.performClick()
            }
            settle()
            activity.onActivity { screen ->
                val sheet = tray(screen)
                assertEquals("Hapus", sheet.view?.findViewById<TextView>(R.id.btnSave)?.text.toString())
                assertEquals("1/2", sheet.view?.findViewById<TextView>(R.id.tvCounter)?.text.toString())
                sheet.view?.findViewById<View>(R.id.btnSave)?.performClick()
            }
            settle()
            open(activity)
            activity.onActivity { screen ->
                val sheet = tray(screen)
                assertEquals(0, product(sheet).findViewById<StepperButton>(R.id.stepper).getCount())
                assertEquals(1, product(sheet, 1).findViewById<StepperButton>(R.id.stepper).getCount())
            }
        }
    }

    @Test
    fun standaloneLockedScenarioAndRedemptionPricesRemainDistinct() {
        for ((scenario, price, counter) in listOf(
            Triple("QUOTA", "Rp25.000", "0/0"),
            Triple("SEARCH_REDEMPTION", "Rp5.000", "0/1"),
        )) {
            launch(scenario).use { activity ->
                settle()
                open(activity)
                activity.onActivity { screen ->
                    val sheet = tray(screen)
                    val card = product(sheet)
                    assertEquals(price, card.findViewById<TextView>(R.id.tvPrice).text.toString())
                    assertEquals(counter, sheet.view?.findViewById<TextView>(R.id.tvCounter)?.text.toString())
                    if (scenario == "QUOTA") {
                        assertFalse(card.findViewById<StepperButton>(R.id.stepper).isEnabled)
                        assertFalse(requireNotNull(sheet.view?.findViewById<View>(R.id.btnSave)).isEnabled)
                    }
                }
            }
        }
    }
}
