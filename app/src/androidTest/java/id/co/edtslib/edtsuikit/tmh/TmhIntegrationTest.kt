package id.co.edtslib.edtsuikit.tmh

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.SystemClock
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isNotEnabled
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import id.co.edtslib.edtsuikit.GuidelinesTmhActivity
import id.co.edtslib.edtsuikit.R
import id.co.edtslib.edtsuikit.demo.promo.ParagonFairDemoActivity
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import id.co.edtslib.uikit.R as UIKitR

@RunWith(AndroidJUnit4::class)
class TmhIntegrationTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    private fun settle(duration: Long = 650) {
        SystemClock.sleep(duration)
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    private fun clickTrayLink() {
        onView(withId(R.id.tvTerms)).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> = isDisplayed()
            override fun getDescription(): String = "Wait for the tray link to stop moving and own window focus"
            override fun perform(uiController: UiController, view: View) {
                val deadline = SystemClock.uptimeMillis() + 4000
                var previous = Rect()
                var stableSince = SystemClock.uptimeMillis()
                while (SystemClock.uptimeMillis() < deadline) {
                    val bounds = Rect()
                    view.getGlobalVisibleRect(bounds)
                    val sheet = view.rootView.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                    val isExpanded = sheet == null ||
                        com.google.android.material.bottomsheet.BottomSheetBehavior.from(sheet).state ==
                        com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
                    if (bounds != previous || view.isLayoutRequested || !view.hasWindowFocus() || !isExpanded) {
                        stableSince = SystemClock.uptimeMillis()
                    } else if (SystemClock.uptimeMillis() - stableSince >= 350) {
                        return
                    }
                    previous = bounds
                    uiController.loopMainThreadForAtLeast(50)
                }
                throw AssertionError("Tray link did not stabilize before tapping")
            }
        }, click())
    }

    private fun scenario(label: Int) {
        onView(withId(R.id.tvTitle)).perform(longClick())
        settle()
        onView(withText(label)).perform(click())
        settle()
    }

    private fun firstCard() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val screen = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).single()
            val list = screen.findViewById<RecyclerView>(R.id.rvFairs)
            requireNotNull(list.getChildAt(0)).performClick()
        }
        settle()
    }

    private fun assertScreen(type: Class<out Activity>) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            assertTrue(ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
                .any { type.isInstance(it) })
        }
    }

    private fun capture(name: String) {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        for (command in listOf("mkdir -p /sdcard/Download/tmh-integration",
            "screencap -p /sdcard/Download/tmh-integration/$name.png")) {
            automation.executeShellCommand(command).use { descriptor ->
                java.io.FileInputStream(descriptor.fileDescriptor).use { it.readBytes() }
            }
        }
    }

    @Test
    fun eligibleCardAndBothTrayLinksUseTheirCorrectDestinations() {
        ActivityScenario.launch(GuidelinesTmhActivity::class.java).use {
            settle(1700)
            scenario(R.string.tmh_scenario_eligible)
            firstCard()
            onView(withId(R.id.tvTerms)).check(matches(withText(R.string.tmh_view_fair)))
            onView(withId(R.id.tvDescriptionTitle)).check(matches(isDisplayed()))
            onView(withId(R.id.tvDescription)).check(matches(isDisplayed()))
            clickTrayLink()
            settle(1800)
            assertScreen(ParagonFairDemoActivity::class.java)
            onView(withId(R.id.banner)).check(matches(withContentDescription(R.string.tmh_paragon)))
            onView(allOf(withId(R.id.message), isDisplayed())).check(matches(withText("Kamu bisa ambil 2 tebus murah")))
            capture("paragon-ppp")
            onView(withId(R.id.giftsAction)).perform(click())
            settle()
            onView(withId(R.id.tvTerms)).check(matches(withText(R.string.tmh_view_terms)))
            onView(withId(R.id.tvDescriptionTitle)).check(matches(androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility(
                androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE)))
            onView(withId(R.id.tvDescription)).check(matches(androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility(
                androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE)))
            capture("ppp-redemption")
            clickTrayLink()
            settle()
            onView(withText(context.getString(R.string.tmh_terms_fair_title,
                context.getString(R.string.tmh_paragon)))).check(matches(isDisplayed()))
            capture("ppp-terms")
            pressBack()
            settle()
            pressBack()
            settle()
            pressBack()
            settle()
            assertScreen(GuidelinesTmhActivity::class.java)
        }
    }

    @Test
    fun everyRestrictedCardOpensPppFromItsWholeCard() {
        for (label in listOf(R.string.tmh_scenario_unmet, R.string.tmh_scenario_stock,
            R.string.tmh_scenario_quota, R.string.tmh_scenario_limit, R.string.tmh_scenario_first_transaction)) {
            ActivityScenario.launch(GuidelinesTmhActivity::class.java).use {
                settle(1700)
                scenario(label)
                firstCard()
                settle(1600)
                assertScreen(ParagonFairDemoActivity::class.java)
                onView(withId(R.id.banner)).check(matches(withContentDescription(R.string.tmh_paragon)))
                pressBack()
                settle()
            }
        }
    }

    @Test
    fun dirtyLihatFairRequiresDiscardAndNeverCommitsPendingQuantity() {
        ActivityScenario.launch(GuidelinesTmhActivity::class.java).use {
            settle(1700)
            scenario(R.string.tmh_scenario_eligible)
            firstCard()
            onView(allOf(withId(UIKitR.id.plus_button), isDisplayed(),
                isDescendantOfA(allOf(isAssignableFrom(com.google.android.material.card.MaterialCardView::class.java), hasDescendant(withText(R.string.tmh_emina)))))).perform(click())
            settle()
            clickTrayLink()
            settle()
            onView(withText(R.string.tmh_unsaved_title)).check(matches(isDisplayed()))
            onView(withId(R.id.btnStay)).perform(click())
            settle()
            onView(withId(R.id.tvCounter)).check(matches(withText("1/2")))
            clickTrayLink()
            settle()
            onView(withId(R.id.btnLeave)).perform(click())
            settle(1800)
            assertScreen(ParagonFairDemoActivity::class.java)
            onView(withId(R.id.giftsAction)).perform(click())
            settle()
            onView(withId(R.id.tvCounter)).check(matches(withText("0/2")))
            pressBack()
            settle()
            pressBack()
            settle()
        }
    }

    @Test
    fun pppSaveAndActivityRecreationReturnSelectionToTmh() {
        ActivityScenario.launch(GuidelinesTmhActivity::class.java).use { tmh ->
            settle(1700)
            scenario(R.string.tmh_scenario_eligible)
            firstCard()
            clickTrayLink()
            settle(1800)
            onView(withId(R.id.giftsAction)).perform(click())
            settle()
            onView(allOf(withId(UIKitR.id.plus_button), isDisplayed(),
                isDescendantOfA(allOf(isAssignableFrom(com.google.android.material.card.MaterialCardView::class.java), hasDescendant(withText(R.string.tmh_emina)))))).perform(click())
            settle()
            onView(withId(R.id.btnSave)).perform(click())
            settle(1900)
            onView(allOf(withId(R.id.message), isDisplayed())).check(matches(withText(
                "Kamu sudah ambil 1 dari 2 tebus murah. Yuk pilih produk lagi")))
            capture("ppp-after-save")
            pressBack()
            settle()
            tmh.recreate()
            settle()
            firstCard()
            onView(withId(R.id.tvCounter)).check(matches(withText("1/2")))
            clickTrayLink()
            settle(1800)
            onView(withId(R.id.giftsAction)).perform(click())
            settle()
            onView(withId(R.id.tvCounter)).check(matches(withText("1/2")))
            pressBack()
            settle()
            pressBack()
            settle()
        }
    }

    @Test
    fun allEightFairsHaveMatchingCatalogAndSurviveBundleRestoration() {
        val initial = TmhFlowState.create(context, TmhScenario.DEFAULT)
        val restored = TmhFlowState.restore(context, initial.toBundle())
        assertEquals(8, restored.fairs.size)
        assertEquals(initial, restored)
        for (fair in restored.fairs) {
            val intent = Intent(context, ParagonFairDemoActivity::class.java)
                .putExtra(TmhFlowState.EXTRA_FLOW, restored.toBundle())
                .putExtra(TmhFlowState.EXTRA_FAIR_ID, fair.id)
            ActivityScenario.launch<ParagonFairDemoActivity>(intent).use { screen ->
                settle(1800)
                onView(withId(R.id.banner)).check(matches(withContentDescription(fair.nameRes)))
                assertTrue(restored.modelFor(fair.id).products.isNotEmpty())
                val catalog = restored.modelFor(fair.id).products
                val redemption = restored.modelFor(fair.id).giftOffers
                assertTrue(catalog.none { product -> redemption.any { it.name == product.name } })
                assertTrue(catalog.none { product -> redemption.any { it.image == product.image } })
                screen.recreate()
                settle()
                onView(withId(R.id.banner)).check(matches(withContentDescription(fair.nameRes)))
                if (fair.nameRes == R.string.tmh_softex) capture("softex-ppp")
            }
        }
    }

    @Test
    fun reducingShoppingSpendTrimsBenefitsAndRestoresTheNewAllowance() {
        val initial = TmhFlowState.create(context, TmhScenario.ELIGIBLE)
            .withSelection("fair-0", mapOf("emina" to 2))
        val intent = Intent(context, ParagonFairDemoActivity::class.java)
            .putExtra(TmhFlowState.EXTRA_FLOW, initial.toBundle())
            .putExtra(TmhFlowState.EXTRA_FAIR_ID, "fair-0")
        ActivityScenario.launch<ParagonFairDemoActivity>(intent).use { screen ->
            settle(1800)
            val wash = context.getString(R.string.tmh_catalog_emina_wash)
            onView(allOf(withId(UIKitR.id.single_plus_button), isDisplayed(),
                withContentDescription(context.getString(R.string.fair_quantity_edit_description, wash, 4))))
                .perform(click())
            settle()
            onView(allOf(withId(UIKitR.id.minus_button), isDisplayed(),
                withContentDescription(context.getString(R.string.fair_remove, wash, 4)))).perform(click())
            settle()
            onView(allOf(withId(R.id.message), isDisplayed())).check(matches(withText("Kamu sudah ambil 1 tebus murah")))
            capture("ppp-one-earned-selected")
            onView(withId(R.id.giftsAction)).perform(click())
            settle()
            onView(withId(R.id.tvCounter)).check(matches(withText("1/1")))
            capture("one-earned-one-selected")
            pressBack()
            settle()
            assertScreen(ParagonFairDemoActivity::class.java)
            screen.onActivity { activity ->
                assertTrue(activity.supportFragmentManager.findFragmentByTag(TmhRedemptionBottomSheet.TAG) == null)
            }
            // The existing PPP control collapses after its five-second focus timeout.
            settle(5500)
            onView(allOf(withId(UIKitR.id.single_plus_button), isDisplayed(),
                withContentDescription(context.getString(R.string.fair_quantity_edit_description, wash, 3))))
                .perform(click())
            settle()
            for (quantity in listOf(3, 2)) {
                onView(allOf(withId(UIKitR.id.minus_button), isDisplayed(),
                    withContentDescription(context.getString(R.string.fair_remove, wash, quantity)))).perform(click())
                settle()
            }
            screen.recreate()
            settle()
            onView(allOf(withId(R.id.message), isDisplayed())).check(matches(withSubstring("Rp50.000")))
            onView(withId(R.id.giftsAction)).perform(click())
            settle()
            onView(withId(R.id.tvCounter)).check(matches(withText("0/0")))
            onView(withId(UIKitR.id.tvAlertMessage)).check(matches(withSubstring("Rp50.000")))
            onView(withId(R.id.btnSave)).check(matches(isNotEnabled()))
            capture("eligibility-reduced")
        }
    }

    @Test
    fun pppControlsRetainCoworkersNoninteractiveBehavior() {
        val initial = TmhFlowState.create(context, TmhScenario.ELIGIBLE)
        val intent = Intent(context, ParagonFairDemoActivity::class.java)
            .putExtra(TmhFlowState.EXTRA_FLOW, initial.toBundle())
            .putExtra(TmhFlowState.EXTRA_FAIR_ID, "fair-0")
        ActivityScenario.launch<ParagonFairDemoActivity>(intent).use { screen ->
            settle(1800)
            screen.onActivity { activity ->
                for (id in listOf(R.id.search, R.id.share, R.id.cart, R.id.sort, R.id.filter, R.id.grid, R.id.list)) {
                    val view = requireNotNull(activity.findViewById<View>(id))
                    assertFalse("Control $id must remain noninteractive", view.isClickable)
                    assertFalse("Control $id must not have a click action", view.hasOnClickListeners())
                    assertFalse("Control $id must not steal keyboard focus", view.isFocusable)
                    assertFalse(view.performClick())
                }
            }
            onView(withId(R.id.cartCount)).check(matches(withText(context.getString(R.string.fair_cart_count, initial.cartCount))))
            assertScreen(ParagonFairDemoActivity::class.java)
            capture("ppp-static-controls")
        }
    }

    @Test
    fun catalogCardsContainTheirVisibleContentAndClearTheFooterInBothColumnCounts() {
        val initial = TmhFlowState.create(context, TmhScenario.ELIGIBLE)
        for (fairId in listOf("fair-0", "fair-1")) {
            val intent = Intent(context, ParagonFairDemoActivity::class.java)
                .putExtra(TmhFlowState.EXTRA_FLOW, initial.toBundle())
                .putExtra(TmhFlowState.EXTRA_FAIR_ID, fairId)
            ActivityScenario.launch<ParagonFairDemoActivity>(intent).use { screen ->
                settle(1800)
                for (columns in listOf(2, 3)) {
                    // The controls intentionally stay static; exercise supported layout sizes directly.
                    screen.onActivity { activity ->
                        ParagonFairDemoActivity::class.java.getDeclaredMethod("changeColumns", Int::class.javaPrimitiveType)
                            .apply { isAccessible = true }.invoke(activity, columns)
                    }
                    settle()
                    val productCount = initial.modelFor(fairId).products.size
                    for (position in 2 until 2 + productCount) {
                        screen.onActivity { it.findViewById<RecyclerView>(R.id.products).scrollToPosition(position) }
                        settle(200)
                        screen.onActivity { activity ->
                            val list = activity.findViewById<RecyclerView>(R.id.products)
                            for (index in 0 until list.childCount) {
                                val child = list.getChildAt(index)
                                val card = child.findViewById<android.view.ViewGroup>(R.id.card) ?: continue
                                for (id in listOf(R.id.image, R.id.name, R.id.price, R.id.discount,
                                    R.id.originalPrice, R.id.promotion, R.id.benefitStrip)) {
                                    val content = card.findViewById<View>(id)
                                    if (content.visibility != View.VISIBLE) continue
                                    val bounds = Rect(0, 0, content.width, content.height)
                                    card.offsetDescendantRectToMyCoords(content, bounds)
                                    assertTrue("$fairId/$columns clipped view $id: $bounds in ${card.width}x${card.height}",
                                        bounds.left >= 0 && bounds.top >= 0 && bounds.right <= card.width && bounds.bottom <= card.height)
                                    if (content is android.widget.TextView) {
                                        assertTrue("Text height clipped for $id", content.layout.height <=
                                            content.height - content.compoundPaddingTop - content.compoundPaddingBottom)
                                    }
                                }
                            }
                        }
                    }
                    repeat(12) {
                        screen.onActivity { activity ->
                            val list = activity.findViewById<RecyclerView>(R.id.products)
                            if (list.canScrollVertically(1)) list.scrollBy(0, list.height / 2)
                        }
                        settle(100)
                    }
                    settle()
                    screen.onActivity { activity ->
                        val list = activity.findViewById<RecyclerView>(R.id.products)
                        val last = requireNotNull(list.findViewHolderForAdapterPosition(requireNotNull(list.adapter).itemCount - 1)).itemView
                        val bounds = Rect()
                        assertTrue(last.getGlobalVisibleRect(bounds))
                        assertEquals("Last card must scroll fully above the footer", last.height, bounds.height())
                        val compact = activity.findViewById<View>(R.id.compactProgress)
                        if (compact.visibility == View.VISIBLE) {
                            val details = listOf(R.id.compactMeter, R.id.progressBar, R.id.message, R.id.seeGifts).joinToString { id ->
                                val v = compact.findViewById<View>(id)
                                "$id=${v.width}x${v.height},lp=${v.layoutParams.width}x${v.layoutParams.height}"
                            }
                            assertTrue("Compact header unexpectedly covers the catalog: ${compact.width}x${compact.height},lp=${compact.layoutParams.width}x${compact.layoutParams.height}; $details",
                                compact.width == list.width && compact.height < list.height / 2)
                        }
                        val footer = Rect()
                        activity.findViewById<View>(R.id.bottomActions).getGlobalVisibleRect(footer)
                        assertTrue("Last card needs 12dp footer clearance", footer.top - bounds.bottom >=
                            (12 * activity.resources.displayMetrics.density).toInt() - 2)
                    }
                    capture("catalog-$fairId-$columns-columns")
                }
            }
        }
    }

    @Test
    fun progressBadgeCanDrawDuringZeroAndNarrowWidthLayoutPasses() {
        ActivityScenario.launch(GuidelinesTmhActivity::class.java).use { screen ->
            screen.onActivity { activity ->
                val progress = id.co.edtslib.uikit.progressbar.LinearProgressBar(activity).apply {
                    showBadge = true
                    badgeCount = 2
                }
                val bitmap = android.graphics.Bitmap.createBitmap(200, 100, android.graphics.Bitmap.Config.ARGB_8888)
                try {
                    for (width in listOf(0, 1, 12, 200)) {
                        progress.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(80, View.MeasureSpec.EXACTLY))
                        progress.layout(0, 0, width, 80)
                        progress.draw(android.graphics.Canvas(bitmap))
                        if (width > 0) assertTrue(progress.badge.left >= 0)
                    }
                } finally {
                    bitmap.recycle()
                }
            }
        }
    }

    @Test
    fun pppFullSelectionWordingOmitsTheDenominator() {
        val initial = TmhFlowState.create(context, TmhScenario.ELIGIBLE)
            .withSelection("fair-0", mapOf("emina" to 2))
        val intent = Intent(context, ParagonFairDemoActivity::class.java)
            .putExtra(TmhFlowState.EXTRA_FLOW, initial.toBundle())
            .putExtra(TmhFlowState.EXTRA_FAIR_ID, "fair-0")
        ActivityScenario.launch<ParagonFairDemoActivity>(intent).use {
            settle(4000)
            onView(allOf(withId(R.id.message), isDisplayed())).check(matches(withText("Kamu sudah ambil 2 tebus murah")))
            capture("ppp-full-selection")
        }
    }

    @Test
    fun returningFromTheLastFairKeepsTheTabAndScrollAnchor() {
        ActivityScenario.launch(GuidelinesTmhActivity::class.java).use { screen ->
            settle(1700)
            scenario(R.string.tmh_scenario_default)
            var firstPosition = -1
            var top = 0
            screen.onActivity { activity ->
                val list = activity.findViewById<RecyclerView>(R.id.rvFairs)
                (list.layoutManager as androidx.recyclerview.widget.LinearLayoutManager).scrollToPositionWithOffset(7, 0)
            }
            settle()
            screen.onActivity { activity ->
                val list = activity.findViewById<RecyclerView>(R.id.rvFairs)
                val manager = list.layoutManager as androidx.recyclerview.widget.LinearLayoutManager
                firstPosition = manager.findFirstVisibleItemPosition()
                top = manager.getDecoratedTop(requireNotNull(manager.findViewByPosition(firstPosition)))
                assertEquals(7, activity.findViewById<com.google.android.material.tabs.TabLayout>(R.id.fairTabs).selectedTabPosition)
            }
            onView(allOf(withId(R.id.tvFairName), withText(R.string.tmh_chupa), isDisplayed())).perform(click())
            settle(1800)
            onView(withId(R.id.banner)).check(matches(withContentDescription(R.string.tmh_chupa)))
            pressBack()
            settle()
            screen.onActivity { activity ->
                val list = activity.findViewById<RecyclerView>(R.id.rvFairs)
                val manager = list.layoutManager as androidx.recyclerview.widget.LinearLayoutManager
                assertEquals(firstPosition, manager.findFirstVisibleItemPosition())
                assertEquals(top, manager.getDecoratedTop(requireNotNull(manager.findViewByPosition(firstPosition))))
                assertEquals(7, activity.findViewById<com.google.android.material.tabs.TabLayout>(R.id.fairTabs).selectedTabPosition)
            }
            capture("tmh-return-anchor")
        }
    }
}
