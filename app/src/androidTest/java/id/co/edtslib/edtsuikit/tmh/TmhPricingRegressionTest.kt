package id.co.edtslib.edtsuikit.tmh

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import id.co.edtslib.edtsuikit.GuidelinesTmhActivity
import id.co.edtslib.edtsuikit.R
import id.co.edtslib.edtsuikit.demo.promo.DemoProduct
import id.co.edtslib.edtsuikit.demo.promo.ParagonFairDemoActivity
import id.co.edtslib.edtsuikit.demo.promo.PromoAvailability
import id.co.edtslib.uikit.stepper.StepperButton
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class TmhPricingRegressionTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val prices = listOf(
        Triple(15_000L, 30_000L, 50),
        Triple(35_000L, 45_000L, 22),
        Triple(45_000L, 67_000L, 33),
        Triple(9_900L, 19_800L, 50),
        Triple(16_000L, 22_000L, 27),
        Triple(21_900L, 29_900L, 27),
        Triple(4_900L, 9_800L, 50),
        Triple(10_000L, 20_000L, 50),
    )

    private fun settle() {
        SystemClock.sleep(2400)
        instrumentation.waitForIdleSync()
    }

    private fun descendants(view: View): List<View> = listOf(view) +
        if (view is ViewGroup) {
            (0 until view.childCount).flatMap { descendants(view.getChildAt(it)) }
        } else emptyList()

    private fun capture(name: String) {
        val directory = File(context.getExternalFilesDir(null), "tmh-pricing").apply { mkdirs() }
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    @Test
    fun campaignPricesAndCartTotalsStayConsistentAfterRestoration() {
        val flow = TmhFlowState.create(context, TmhScenario.DEFAULT)
        flow.fairs.forEachIndexed { index, fair ->
            val (price, original, discount) = prices[index]
            val offers = flow.modelFor(fair.id).giftOffers.associateBy { it.id }
            fair.products.forEach { product ->
                assertEquals(price, context.getString(product.priceRes).rupiahAmount())
                assertEquals(original, context.getString(product.originalPriceRes).rupiahAmount())
                assertEquals(discount, product.discountPercent(context))
                assertEquals(price, offers.getValue(product.id).price)
                assertEquals(original, offers.getValue(product.id).originalPrice)
            }
        }
        val added = flow.withSelection("fair-2", mapOf("kahf" to 1))
        assertEquals(flow.cartTotal + 45_000L, added.cartTotal)
        assertEquals(flow.cartCount + 1, added.cartCount)
        val removed = added.withSelection("fair-1", emptyMap())
        assertEquals(added.cartTotal - 70_000L, removed.cartTotal)
        assertEquals(removed, TmhFlowState.restore(context, removed.toBundle()))
    }

    @Test
    fun everyCampaignEarnsAtItsOwnThresholdAndBenefitsDoNotQualifyThemselves() {
        val flow = TmhFlowState.create(context, TmhScenario.DEFAULT)
        val thresholds = listOf(100_000L, 100_000L, 115_000L, 30_000L, 30_000L, 40_000L, 50_000L, 50_000L)
        for ((index, fair) in flow.fairs.withIndex()) {
            val model = flow.modelFor(fair.id)
            assertEquals(thresholds[index], fair.spendThreshold)
            assertEquals(fair.spendThreshold, model.threshold)
            val qualifying = DemoProduct(
                id = "threshold-product",
                name = "Qualifying product",
                image = 0,
                price = 1,
                quantity = model.threshold.toInt() - 1,
                stock = Int.MAX_VALUE,
            )
            val before = model.copy(products = listOf(qualifying), availability = PromoAvailability.ACTIVE,
                claimedGifts = emptyList(), otherCartCount = 0, otherCartTotal = 0)
            assertEquals(0, before.earnedGifts)
            assertEquals(1L, before.remainingSpend)
            val one = before.withQuantity(qualifying.id, model.threshold.toInt())
            assertEquals(1, one.earnedGifts)
            val claimed = one.withClaimedGifts(listOf(model.giftOffers.first().id))
            assertEquals(one.qualifyingSubtotal, claimed.qualifyingSubtotal)
            assertEquals(1, claimed.earnedGifts)
            val two = claimed.withQuantity(qualifying.id, model.threshold.toInt() * 2)
            assertEquals(2, two.earnedGifts)
            assertEquals(2, two.withQuantity(qualifying.id, model.threshold.toInt() * 3).earnedGifts)
            val reduced = two.withQuantity(qualifying.id, model.threshold.toInt() - 1)
            assertTrue(reduced.claimedGifts.isEmpty())
        }
    }

    @Test
    fun listingAndBothTrayEntrypointsShowTheSamePricesAndRoundedDiscounts() {
        val flow = TmhFlowState.create(context, TmhScenario.DEFAULT)
        ActivityScenario.launch(GuidelinesTmhActivity::class.java).use { screen ->
            settle()
            flow.fairs.forEachIndexed { index, fair ->
                screen.onActivity { activity ->
                    val list = activity.findViewById<RecyclerView>(R.id.rvFairs)
                    (list.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(index, 0)
                }
                settle()
                screen.onActivity { activity ->
                    val list = activity.findViewById<RecyclerView>(R.id.rvFairs)
                    val card = list.findViewHolderForAdapterPosition(index)!!.itemView
                    val products = card.findViewById<RecyclerView>(R.id.rvProducts)
                    val product = products.findViewHolderForAdapterPosition(0)!!.itemView
                    assertEquals(prices[index].first.rupiahText(), product.findViewById<TextView>(R.id.tvPrice).text.toString())
                    assertEquals("${prices[index].third}%", product.findViewById<TextView>(R.id.tvDiscount).text.toString())
                }
                capture("listing-${fair.id}")
            }
        }
        for ((id, discount) in listOf("fair-1" to 22, "fair-2" to 33, "fair-4" to 27)) {
            for (isPromoDetail in listOf(false, true)) {
                ActivityScenario.launch(GuidelinesTmhActivity::class.java).use { screen ->
                    settle()
                    screen.onActivity { activity ->
                        TmhRedemptionBottomSheet.newInstance(flow.fair(id), TmhScenario.DEFAULT, isPromoDetail)
                            .showNow(activity.supportFragmentManager, TmhRedemptionBottomSheet.TAG)
                    }
                    settle()
                    screen.onActivity { activity ->
                        val tray = activity.supportFragmentManager.findFragmentByTag(TmhRedemptionBottomSheet.TAG)!!
                        val discounts = descendants(tray.requireView()).filterIsInstance<TextView>()
                            .filter { it.id == R.id.tvDiscount }
                        assertTrue(discounts.isNotEmpty())
                        assertTrue(discounts.all { it.text.toString() == "$discount%" })
                        val description = tray.requireView().findViewById<TextView>(R.id.tvDescription)
                        if (!isPromoDetail) assertTrue(description.text.contains(flow.fair(id).spendThreshold.rupiahText()))
                        else assertEquals(View.GONE, description.visibility)
                    }
                    capture("tray-$id-$isPromoDetail")
                }
            }
        }
    }

    @Test
    fun pppMinimumTermsSavedSelectionAndCartTotalUseTheCampaignValues() {
        val flow = TmhFlowState.create(context, TmhScenario.DEFAULT)
        val model = flow.modelFor("fair-2")
        val intent = Intent(context, ParagonFairDemoActivity::class.java)
            .putExtra(TmhFlowState.EXTRA_FLOW, flow.toBundle())
            .putExtra(TmhFlowState.EXTRA_FAIR_ID, "fair-2")
        ActivityScenario.launch<ParagonFairDemoActivity>(intent).use { screen ->
            settle()
            screen.onActivity { activity ->
                assertEquals(model.threshold.rupiahText(), activity.findViewById<TextView>(R.id.minimumValue).text.toString())
                assertTrue(activity.findViewById<TextView>(R.id.promoTitle).text.contains(model.threshold.rupiahText()))
                activity.findViewById<View>(R.id.terms).performClick()
            }
            settle()
            screen.onActivity { activity ->
                val terms = activity.supportFragmentManager.findFragmentByTag(TmhTermsBottomSheet.TAG)!!
                val text = terms.requireView().findViewById<TextView>(R.id.tvTermsContent).text.toString()
                assertTrue(text.contains("minimal ${model.threshold.rupiahText()}"))
                assertTrue(text.contains("Rp45.000"))
                assertFalse(text.contains("[Nama Produk]"))
                (terms as TmhTermsBottomSheet).dismiss()
            }
            settle()
            screen.onActivity { it.findViewById<View>(R.id.giftsAction).performClick() }
            settle()
            screen.onActivity { activity ->
                val tray = activity.supportFragmentManager.findFragmentByTag(TmhRedemptionBottomSheet.TAG)!!
                descendants(tray.requireView()).filterIsInstance<StepperButton>().first().binding.plusButton.performClick()
            }
            settle()
            screen.onActivity { activity ->
                activity.supportFragmentManager.findFragmentByTag(TmhRedemptionBottomSheet.TAG)!!
                    .requireView().findViewById<View>(R.id.btnSave).performClick()
            }
            settle()
            screen.onActivity { activity ->
                assertEquals(View.GONE, activity.findViewById<View>(R.id.skeleton).visibility)
                assertEquals((flow.cartTotal + 45_000L).rupiahText(), activity.findViewById<TextView>(R.id.cartTotal).text.toString())
                assertEquals("Kamu sudah ambil 1 dari 2 tebus murah. Yuk pilih produk lagi",
                    activity.findViewById<TextView>(R.id.message).text.toString())
            }
            capture("ppp-saved-selection")
            screen.recreate()
            settle()
            screen.onActivity { activity ->
                assertEquals(View.GONE, activity.findViewById<View>(R.id.skeleton).visibility)
                assertEquals((flow.cartTotal + 45_000L).rupiahText(), activity.findViewById<TextView>(R.id.cartTotal).text.toString())
            }
        }
    }
}
