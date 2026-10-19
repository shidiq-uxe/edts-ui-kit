package id.co.edtslib.edtsuikit.tmh

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ActivityScenario
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.card.MaterialCardView
import id.co.edtslib.uikit.button.Button
import id.co.edtslib.uikit.utils.color
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.dimenPixelSize
import id.co.edtslib.edtsuikit.GuidelinesTmhActivity
import id.co.edtslib.edtsuikit.R
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import id.co.edtslib.uikit.R as UIKitR

@RunWith(AndroidJUnit4::class)
class TmhPrototypeTest {

    private lateinit var activity: ActivityScenario<GuidelinesTmhActivity>

    private fun openFreshTray() {
        activity.onActivity { screen ->
            val list = screen.findViewById<RecyclerView>(R.id.rvFairs)
            (list.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(0, 0)
        }
        settleUi()
        clickParagonAction(R.string.tmh_choose)
    }

    private fun clickParagonAction(label: Int) {
        onView(allOf(
            withId(UIKitR.id.tvAction),
            withText(label),
            isDisplayed(),
            isDescendantOfA(allOf(
                isAssignableFrom(MaterialCardView::class.java),
                hasDescendant(allOf(withId(R.id.tvFairName), withText(R.string.tmh_paragon))),
            )),
        )).perform(click())
        settleUi()
    }

    private fun captureReview(name: String, settleDuration: Long = 650) {
        if (settleDuration > 0) settleUi(settleDuration)
        val automation = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().uiAutomation
        for (command in listOf(
            "mkdir -p /sdcard/Download/tmh-review-v8",
            "screencap -p /sdcard/Download/tmh-review-v8/$name.png",
        )) {
            automation.executeShellCommand(command).use { descriptor ->
                java.io.FileInputStream(descriptor.fileDescriptor).use { it.readBytes() }
            }
        }
    }

    private fun chooseScenario(label: Int) {
        onView(withId(R.id.tvTitle)).perform(longClick())
        settleUi()
        onView(withText(label)).perform(click())
        settleUi()
    }

    private fun settleUi(duration: Long = 450) {
        onView(isRoot()).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> = isRoot()
            override fun getDescription(): String = "Wait for sheet layout and asynchronous adapter updates"
            override fun perform(uiController: UiController, view: View) {
                uiController.loopMainThreadForAtLeast(duration)
            }
        })
    }

    private fun incrementEmina() {
        onView(allOf(
            withId(UIKitR.id.plus_button),
            isDisplayed(),
            isDescendantOfA(allOf(isAssignableFrom(MaterialCardView::class.java), hasDescendant(withText(R.string.tmh_emina)))),
        )).perform(click())
        settleUi()
    }

    private fun decrementEmina() {
        onView(allOf(
            withId(UIKitR.id.minus_button),
            isDisplayed(),
            isDescendantOfA(allOf(isAssignableFrom(MaterialCardView::class.java), hasDescendant(withText(R.string.tmh_emina)))),
        )).perform(click())
        settleUi()
    }

    private fun assertUnsavedAndStay() {
        onView(withText(R.string.tmh_unsaved_title)).check(matches(isDisplayed()))
        onView(withId(R.id.btnStay)).perform(click())
        settleUi()
        onView(withId(R.id.tvCounter)).check(matches(withText("1/2")))
    }

    private fun assertFooter(label: Int, enabled: Boolean, destructive: Boolean, hasIcon: Boolean) {
        onView(withId(R.id.btnSave)).check { view, error ->
            if (error != null) throw error
            val button = view as Button
            assertEquals(button.context.getString(label), button.text.toString())
            assertEquals(enabled, button.isEnabled)
            assertEquals(destructive, button.isDestructive)
            assertEquals(hasIcon, button.icon != null)
            assertEquals(40.dp.toInt(), button.height)
            val color = when {
                !enabled -> UIKitR.color.black_30
                destructive -> UIKitR.color.red_30
                else -> UIKitR.color.primary_30
            }
            assertEquals(button.context.color(color), button.backgroundTintList?.getColorForState(button.drawableState, 0))
        }
    }

    private fun assertLoadingStructure() {
        onView(withId(R.id.loadingFairTabs)).check { view, error ->
            if (error != null) throw error
            assertTrue("Loading strip should be visible", view.isShown)
            val screen = view.rootView
            assertTrue(screen.findViewById<View>(R.id.promotionTabs).isShown)
            assertEquals(View.INVISIBLE, screen.findViewById<View>(R.id.fairTabs).visibility)
            val list = screen.findViewById<RecyclerView>(R.id.rvFairs)
            assertEquals(3, list.adapter?.itemCount)
            val row = requireNotNull(list.getChildAt(0))
            val title = row.findViewById<View>(R.id.titlePlaceholder)
            val banner = row.findViewById<View>(R.id.bannerPlaceholder)
            val products = row.findViewById<android.widget.HorizontalScrollView>(R.id.productsPlaceholder)
            val tiles = products.getChildAt(0) as ViewGroup
            assertEquals(banner.width, title.width)
            assertEquals(16.dp.toInt(), title.height)
            assertEquals(80.dp.toInt(), products.height)
            assertEquals(3, tiles.childCount)
            assertEquals(216.dp.toInt(), tiles.getChildAt(0).width)
            assertEquals(40.dp.toInt(), row.findViewById<View>(R.id.alertPlaceholder).height)
        }
    }

    @Before
    fun launch() {
        activity = ActivityScenario.launch(GuidelinesTmhActivity::class.java)
        Thread.sleep(1500)
        chooseScenario(R.string.tmh_scenario_eligible)
    }

    @After
    fun close() {
        activity.close()
    }

    @Test
    fun initialAndRefreshLoadingKeepHeaderAndUseFigmaStructure() {
        activity.close()
        activity = ActivityScenario.launch(GuidelinesTmhActivity::class.java)
        settleUi(120)
        assertLoadingStructure()
        captureReview("initial-loading", 0)
        settleUi(1600)
        onView(withId(R.id.rvFairs)).perform(androidx.test.espresso.action.ViewActions.swipeDown())
        settleUi(120)
        assertLoadingStructure()
        captureReview("refresh-loading", 0)
        settleUi(1600)
        onView(withId(R.id.fairTabs)).check(matches(isDisplayed()))
    }

    @Test
    fun addAndRemoveReloadOnlyAfterTheTrayIsDismissed() {
        openFreshTray()
        assertFooter(R.string.tmh_add_cart, false, false, true)
        incrementEmina()
        assertFooter(R.string.tmh_add_cart, true, false, true)
        captureReview("add-footer")
        onView(withId(R.id.btnSave)).perform(click())
        settleUi(120)
        assertLoadingStructure()
        activity.onActivity { screen ->
            assertTrue(screen.supportFragmentManager.findFragmentByTag(TmhRedemptionBottomSheet.TAG) == null)
        }
        captureReview("after-add-loading", 0)
        settleUi(1600)
        clickParagonAction(R.string.tmh_edit)
        assertFooter(R.string.tmh_update_cart, false, false, false)
        decrementEmina()
        assertFooter(R.string.tmh_remove_cart, true, true, false)
        captureReview("remove-footer")
        onView(withId(R.id.btnSave)).perform(click())
        settleUi(120)
        assertLoadingStructure()
        captureReview("after-remove-loading", 0)
        settleUi(1600)
        clickParagonAction(R.string.tmh_choose)
        assertFooter(R.string.tmh_add_cart, false, false, true)
    }

    @Test
    fun sameTotalProductSwapEnablesUpdateAndRevertingDisablesIt() {
        chooseScenario(R.string.tmh_scenario_partial)
        clickParagonAction(R.string.tmh_edit)
        assertFooter(R.string.tmh_update_cart, false, false, false)
        decrementEmina()
        assertFooter(R.string.tmh_remove_cart, true, true, false)
        onView(allOf(withId(UIKitR.id.plus_button), isDisplayed(),
            isDescendantOfA(allOf(isAssignableFrom(MaterialCardView::class.java),
                hasDescendant(withText(R.string.tmh_kahf)))))).perform(click())
        settleUi()
        onView(withId(R.id.tvCounter)).check(matches(withText("1/2")))
        assertFooter(R.string.tmh_update_cart, true, false, false)
        captureReview("same-total-swap")
        onView(allOf(withId(UIKitR.id.minus_button), isDisplayed(),
            isDescendantOfA(allOf(isAssignableFrom(MaterialCardView::class.java),
                hasDescendant(withText(R.string.tmh_kahf)))))).perform(click())
        settleUi()
        incrementEmina()
        assertFooter(R.string.tmh_update_cart, false, false, false)
    }

    @Test
    fun updateReloadRestoresCampaignAndScrollOffset() {
        chooseScenario(R.string.tmh_scenario_default)
        activity.onActivity { screen ->
            val list = screen.findViewById<RecyclerView>(R.id.rvFairs)
            (list.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(1, 0)
        }
        settleUi()
        onView(allOf(withId(UIKitR.id.tvAction), withText(R.string.tmh_edit), isDisplayed(),
            isDescendantOfA(allOf(isAssignableFrom(MaterialCardView::class.java),
                hasDescendant(withText(R.string.tmh_luminarc)))))).perform(click())
        settleUi()
        onView(allOf(withId(UIKitR.id.minus_button), isDisplayed(),
            isDescendantOfA(allOf(isAssignableFrom(MaterialCardView::class.java),
                hasDescendant(withText(R.string.tmh_soup)))))).perform(click())
        settleUi()
        assertFooter(R.string.tmh_update_cart, true, false, false)
        captureReview("update-footer")
        onView(withId(R.id.btnSave)).perform(click())
        settleUi(120)
        assertLoadingStructure()
        captureReview("after-update-loading", 0)
        settleUi(1700)
        activity.onActivity { screen ->
            val list = screen.findViewById<RecyclerView>(R.id.rvFairs)
            val manager = list.layoutManager as LinearLayoutManager
            assertEquals(1, manager.findFirstVisibleItemPosition())
            assertEquals(list.paddingTop, manager.getDecoratedTop(requireNotNull(manager.findViewByPosition(1))))
            assertEquals(1, screen.findViewById<TabLayout>(R.id.fairTabs).selectedTabPosition)
            assertEquals((TmhFlowState.create(screen, TmhScenario.DEFAULT).cartCount - 1).toString(),
                screen.findViewById<android.widget.TextView>(R.id.tvCartCount).text.toString())
        }
    }

    @Test
    fun lastTabAndManualBottomHaveOneTwelveDpGap() {
        activity.onActivity { screen ->
            val tabs = screen.findViewById<TabLayout>(R.id.fairTabs)
            tabs.getTabAt(tabs.tabCount - 1)?.select()
        }
        settleUi(1800)
        activity.onActivity { screen ->
            val list = screen.findViewById<RecyclerView>(R.id.rvFairs)
            val manager = list.layoutManager as LinearLayoutManager
            val lastIndex = requireNotNull(list.adapter).itemCount - 1
            val last = requireNotNull(manager.findViewByPosition(lastIndex))
            assertTrue("Last tab should stop at the bottom: first=${manager.findFirstVisibleItemPosition()}, last=$lastIndex, " +
                "lastBottom=${manager.getDecoratedBottom(last)}, viewport=${list.height}, padding=${list.paddingBottom}",
                !list.canScrollVertically(1))
            assertTrue(manager.findFirstVisibleItemPosition() < lastIndex)
            assertEquals(lastIndex, screen.findViewById<TabLayout>(R.id.fairTabs).selectedTabPosition)
            assertEquals(12.dp.toInt(), list.paddingBottom)
            assertEquals(0, (last.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin)
            assertTrue(kotlin.math.abs(list.height - list.paddingBottom - manager.getDecoratedBottom(last)) <= 1)
            val location = IntArray(2)
            last.getLocationOnScreen(location)
            val bottomInset = ViewCompat.getRootWindowInsets(list)
                ?.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())?.bottom ?: 0
            assertTrue(kotlin.math.abs(screen.window.decorView.height - location[1] - last.height -
                bottomInset - 12.dp.toInt()) <= 1)
            list.scrollBy(0, -24.dp.toInt())
        }
        settleUi()
        activity.onActivity { screen ->
            val list = screen.findViewById<RecyclerView>(R.id.rvFairs)
            assertTrue(list.canScrollVertically(1))
            assertEquals((list.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition(),
                screen.findViewById<TabLayout>(R.id.fairTabs).selectedTabPosition)
        }
        onView(withId(R.id.rvFairs)).perform(swipeUp())
        settleUi()
        activity.onActivity { screen ->
            val tabs = screen.findViewById<TabLayout>(R.id.fairTabs)
            assertTrue(!screen.findViewById<RecyclerView>(R.id.rvFairs).canScrollVertically(1))
            assertEquals(tabs.tabCount - 1, tabs.selectedTabPosition)
        }
        captureReview("natural-bottom")
    }

    @Test
    fun eachStackedSheetDimsItsOwnWindowBySeventyFivePercent() {
        openFreshTray()
        activity.onActivity { screen ->
            val sheet = screen.supportFragmentManager.findFragmentByTag(TmhRedemptionBottomSheet.TAG) as TmhRedemptionBottomSheet
            assertEquals(0.75f, requireNotNull(sheet.dialog?.window).attributes.dimAmount, 0.001f)
        }
        incrementEmina()
        pressBack()
        settleUi()
        activity.onActivity { screen ->
            val sheet = screen.supportFragmentManager.findFragmentByTag(TmhRedemptionBottomSheet.TAG) as TmhRedemptionBottomSheet
            val unsaved = sheet.childFragmentManager.findFragmentByTag(TmhUnsavedBottomSheet.TAG) as TmhUnsavedBottomSheet
            assertEquals(0.75f, requireNotNull(unsaved.dialog?.window).attributes.dimAmount, 0.001f)
        }
        captureReview("stacked-dimming")
    }

    @Test
    fun termsHtmlScrollsWithNumberingEmphasisAndRestoredFairTitle() {
        activity.onActivity { screen ->
            val fair = TmhFlowState.create(screen, TmhScenario.ELIGIBLE).fairs.first()
            TmhTermsBottomSheet.newInstance(
                fairName = screen.getString(fair.nameRes),
                isIntegrated = true,
                offerDescriptions = fair.products.map { "${screen.getString(it.nameRes)} (${screen.getString(it.priceRes)})" },
            ).show(screen.supportFragmentManager, TmhTermsBottomSheet.TAG)
        }
        settleUi()
        activity.onActivity { screen ->
            val terms = screen.supportFragmentManager.findFragmentByTag(TmhTermsBottomSheet.TAG) as TmhTermsBottomSheet
            assertEquals(0.75f, requireNotNull(terms.dialog?.window).attributes.dimAmount, 0.001f)
            val content = terms.requireView().findViewById<android.widget.TextView>(R.id.tvTermsContent)
            val text = content.text as android.text.Spanned
            assertEquals(screen.getString(R.string.tmh_terms_fair_title, screen.getString(R.string.tmh_paragon)),
                terms.title)
            assertEquals(10, text.getSpans(0, text.length, android.text.style.LeadingMarginSpan::class.java)
                .count { it !is android.text.ParcelableSpan })
            assertTrue(text.toString().contains("\"Kuota Promo Sudah Habis\""))
            assertTrue(!text.toString().contains("<strong>"))
            assertTrue(text.getSpans(0, text.length, id.co.edtslib.uikit.utils.html.TypefaceSpan::class.java).isNotEmpty())
            val firstLine = content.layout.getLineForOffset(0)
            val lineBottom = content.compoundPaddingTop + content.layout.getLineBottom(firstLine)
            val bitmap = android.graphics.Bitmap.createBitmap(content.width, lineBottom,
                android.graphics.Bitmap.Config.ARGB_8888)
            try {
                content.draw(android.graphics.Canvas(bitmap))
                val textStart = content.compoundPaddingLeft + content.layout.getPrimaryHorizontal(0).toInt()
                val markerRight = textStart - 4.dp.toInt()
                val markerTop = content.compoundPaddingTop + content.layout.getLineTop(firstLine)
                val hasMarkerPixels = (content.compoundPaddingLeft until markerRight).any { x ->
                    (markerTop until lineBottom).any { y -> android.graphics.Color.alpha(bitmap.getPixel(x, y)) > 0 }
                }
                assertTrue("The first ordered-list number must actually be drawn", hasMarkerPixels)
            } finally {
                bitmap.recycle()
            }
            val linkStart = text.indexOf("syarat dan ketentuan")
            assertTrue(text.getSpans(linkStart, linkStart + 19, android.text.style.ForegroundColorSpan::class.java)
                .any { it.foregroundColor == screen.color(UIKitR.color.klik_color_blue_50) })
            val sheet = requireNotNull(terms.dialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            assertTrue(requireNotNull(sheet).height <= screen.window.decorView.height * 0.9f + 1)
            val scroll = terms.requireView().findViewById<androidx.core.widget.NestedScrollView>(R.id.termsScroll)
            assertTrue(scroll.canScrollVertically(1))
        }
        captureReview("terms-html-top")
        onView(withId(R.id.termsScroll)).perform(swipeUp())
        onView(withId(R.id.termsScroll)).perform(swipeUp())
        settleUi()
        activity.onActivity { screen ->
            val terms = screen.supportFragmentManager.findFragmentByTag(TmhTermsBottomSheet.TAG) as TmhTermsBottomSheet
            terms.requireView().findViewById<androidx.core.widget.NestedScrollView>(R.id.termsScroll).fullScroll(View.FOCUS_DOWN)
        }
        settleUi()
        onView(withId(R.id.termsScroll)).check { view, _ -> assertTrue(!view.canScrollVertically(1)) }
        captureReview("terms-html-bottom")
        activity.recreate()
        settleUi()
        activity.onActivity { screen ->
            val terms = screen.supportFragmentManager.findFragmentByTag(TmhTermsBottomSheet.TAG) as TmhTermsBottomSheet
            assertEquals(screen.getString(R.string.tmh_terms_fair_title, screen.getString(R.string.tmh_paragon)),
                terms.title)
            val text = terms.requireView().findViewById<android.widget.TextView>(R.id.tvTermsContent).text as android.text.Spanned
            assertEquals(10, text.getSpans(0, text.length, android.text.style.LeadingMarginSpan::class.java)
                .count { it !is android.text.ParcelableSpan })
        }
    }

    @Test
    fun imageBoundsFollowDiscountBottomAndCapAtFiftySixDp() {
        activity.onActivity { screen ->
            val row = id.co.edtslib.edtsuikit.databinding.ItemTmhRedemptionProductBinding.inflate(screen.layoutInflater)
            row.root.tmhTypography()
            row.tvPrice.text = screen.getString(R.string.tmh_price_5000)
            row.tvOriginalPrice.text = screen.getString(R.string.tmh_price_10000)
            row.ivProduct.setImageResource(R.drawable.tmh_product_emina)
            for (name in listOf("Emina", "Baris pertama\nBaris kedua", "Emina")) {
                row.tvName.text = name
                row.root.measure(View.MeasureSpec.makeMeasureSpec(360.dp.toInt(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
                row.root.layout(0, 0, row.root.measuredWidth, row.root.measuredHeight)
                assertEquals(row.tvDiscount.bottom, row.ivProduct.bottom)
                assertTrue(row.ivProduct.height <= 56.dp + 1)
                assertTrue(kotlin.math.abs(row.ivProduct.width - 56.dp) <= 1)
                assertEquals(android.widget.ImageView.ScaleType.FIT_CENTER, row.ivProduct.scaleType)
                if (name.contains('\n')) {
                    assertEquals(2, row.tvName.lineCount)
                    assertTrue(kotlin.math.abs(row.ivProduct.height - 56.dp) <= 1)
                    assertTrue(row.ivProduct.top > row.tvName.top)
                } else {
                    assertEquals(row.tvName.top, row.ivProduct.top)
                    assertTrue(row.ivProduct.height < 56.dp)
                }
            }
            val preview = id.co.edtslib.edtsuikit.databinding.ItemTmhProductPreviewBinding.inflate(screen.layoutInflater)
            preview.root.tmhTypography()
            preview.tvName.text = "Emina"
            preview.tvPrice.text = screen.getString(R.string.tmh_price_5000)
            preview.tvOriginalPrice.text = screen.getString(R.string.tmh_price_10000)
            preview.ivProduct.setImageResource(R.drawable.tmh_product_emina)
            preview.root.measure(View.MeasureSpec.makeMeasureSpec(
                requireNotNull(screen.dimenPixelSize(R.dimen.tmh_product_preview_width)), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(80.dp.toInt(), View.MeasureSpec.EXACTLY))
            preview.root.layout(0, 0, preview.root.measuredWidth, preview.root.measuredHeight)
            assertEquals(preview.tvDiscount.bottom, preview.ivProduct.bottom)
            assertTrue(preview.ivProduct.height <= 56.dp + 1)
            assertTrue(kotlin.math.abs(preview.ivProduct.width - 56.dp) <= 1)
            assertEquals(1, preview.tvName.maxLines)
            assertEquals(android.widget.ImageView.ScaleType.FIT_CENTER, preview.ivProduct.scaleType)
        }
        openFreshTray()
        captureReview("product-images-redemption")
    }

    @Test
    fun saveThenRemovePromotionalItem() {
        openFreshTray()
        onView(withId(R.id.btnSave)).check(matches(not(isEnabled())))
        incrementEmina()
        onView(withId(R.id.tvCounter)).check(matches(withText("1/2")))
        onView(withId(R.id.btnSave)).perform(click())
        settleUi(1700)
        clickParagonAction(R.string.tmh_edit)
        decrementEmina()
        onView(withId(R.id.tvCounter)).check(matches(withText("0/2")))
        onView(withId(R.id.btnSave)).check(matches(isEnabled())).perform(click())
        settleUi(1700)
        onView(allOf(withId(UIKitR.id.tvAction), withText(R.string.tmh_choose), isDisplayed(),
            isDescendantOfA(allOf(isAssignableFrom(MaterialCardView::class.java),
                hasDescendant(withText(R.string.tmh_paragon)))))).check(matches(isDisplayed()))
    }

    @Test
    fun partialCounterRemainsFullyVisibleWhenIncremented() {
        chooseScenario(R.string.tmh_scenario_partial)
        clickParagonAction(R.string.tmh_edit)
        for (expected in listOf("1/2", "2/2", "1/2")) {
            if (expected == "2/2") incrementEmina()
            else if (expected == "1/2" && counterValue() == "2/2") decrementEmina()
            captureReview("counter-${expected.replace('/', '-')}")
            onView(withId(R.id.tvCounter)).check { view, error ->
                if (error != null) throw error
                val counter = view as android.widget.TextView
                val layout = requireNotNull(counter.layout)
                val availableWidth = counter.width - counter.compoundPaddingLeft - counter.compoundPaddingRight
                val details = "text=${counter.text}, width=${counter.width}, measured=${counter.measuredWidth}, " +
                    "available=$availableWidth, lineWidth=${layout.getLineWidth(0)}, lineEnd=${layout.getLineEnd(0)}"
                android.util.Log.i("TmhCounterReview", details)
                assertEquals(expected, counter.text.toString())
                assertEquals(details, 1, layout.lineCount)
                assertEquals(details, counter.text.length, layout.getLineEnd(0))
                assertEquals(details, 0, layout.getEllipsisCount(0))
                assertEquals(details, counter.measuredWidth, counter.width)
                assertTrue(details, layout.getLineWidth(0) <= availableWidth + 1)
            }
        }
    }

    private fun counterValue(): String {
        var value = ""
        onView(withId(R.id.tvCounter)).check { view, error ->
            if (error != null) throw error
            value = (view as android.widget.TextView).text.toString()
        }
        return value
    }

    @Test
    fun movementBeforeLongPressScrollsTheTrayWithoutChangingQuantity() {
        openFreshTray()
        onView(allOf(withId(UIKitR.id.plus_button), isDisplayed(), isDescendantOfA(
            allOf(isAssignableFrom(MaterialCardView::class.java), hasDescendant(withText(R.string.tmh_emina)))
        ))).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> = isDisplayed()
            override fun getDescription(): String = "Scroll before long-press activation without changing quantity"
            override fun perform(uiController: UiController, view: View) {
                val stepper = generateSequence(view.parent as? View) { it.parent as? View }
                    .filterIsInstance<id.co.edtslib.uikit.stepper.StepperButton>().first()
                val scroll = generateSequence(stepper.parent as? View) { it.parent as? View }
                    .filterIsInstance<androidx.core.widget.NestedScrollView>().first()
                val gesture = DragGesture(uiController, view)
                gesture.press()
                gesture.move(30f)
                gesture.waitFor(50)
                gesture.move(108f)
                gesture.waitFor(650)
                gesture.release()
                assertEquals(0, stepper.getCount())
                assertTrue("The tray should retain normal scrolling before activation", scroll.scrollY > 0)
            }
        })
        assertFooter(R.string.tmh_add_cart, false, false, true)
    }

    @Test
    fun longPressDragOwnsTheTrayAndReversesAtTheSharedLimit() {
        openFreshTray()
        onView(allOf(withId(UIKitR.id.plus_button), isDisplayed(), isDescendantOfA(
            allOf(isAssignableFrom(MaterialCardView::class.java), hasDescendant(withText(R.string.tmh_emina)))
        ))).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> = isDisplayed()
            override fun getDescription(): String = "Drag plus to the fair cap and reverse without moving the sheet"
            override fun perform(uiController: UiController, view: View) {
                val stepper = generateSequence(view.parent as? View) { it.parent as? View }
                    .filterIsInstance<id.co.edtslib.uikit.stepper.StepperButton>().first()
                val scroll = generateSequence(stepper.parent as? View) { it.parent as? View }
                    .filterIsInstance<androidx.core.widget.NestedScrollView>().first()
                val root = view.rootView.findViewById<View>(UIKitR.id.flRoot)
                val initialTop = IntArray(2).also { root.getLocationOnScreen(it) }[1]
                val initialScroll = scroll.scrollY
                val gesture = DragGesture(uiController, view)
                gesture.activate()
                assertEquals(0, stepper.getCount())
                gesture.waitFor(100)
                captureReview("tray-drag-paused", 0)
                gesture.move(108f)
                gesture.waitFor(650)
                assertEquals(2, stepper.getCount())
                assertEquals("2/2", root.findViewById<android.widget.TextView>(R.id.tvCounter).text.toString())
                assertTrue(!stepper.binding.plusButton.isEnabled)
                captureReview("tray-drag-limit", 0)
                assertEquals(initialScroll, scroll.scrollY)
                assertEquals(initialTop, IntArray(2).also { root.getLocationOnScreen(it) }[1])
                gesture.move(-108f)
                gesture.waitFor(650)
                assertEquals(0, stepper.getCount())
                assertEquals("0/2", root.findViewById<android.widget.TextView>(R.id.tvCounter).text.toString())
                assertEquals(initialScroll, scroll.scrollY)
                assertEquals(initialTop, IntArray(2).also { root.getLocationOnScreen(it) }[1])
                gesture.release()
                gesture.waitFor(250)
                assertEquals(0, stepper.getCount())
            }
        })
        captureReview("after-drag-reversal")
        assertFooter(R.string.tmh_add_cart, false, false, true)
        pressBack()
        settleUi()
        onView(withId(R.id.tvTitle)).check(matches(isDisplayed()))
    }

    @Test
    fun dragRepetitionUsesOnlyTheFairAllowanceRemainingAfterAnotherProduct() {
        openFreshTray()
        incrementEmina()
        onView(allOf(withId(UIKitR.id.plus_button), isDisplayed(), isDescendantOfA(
            allOf(isAssignableFrom(MaterialCardView::class.java), hasDescendant(withText(R.string.tmh_kahf)))
        ))).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> = isDisplayed()
            override fun getDescription(): String = "Respect the shared fair cap while another product already uses one unit"
            override fun perform(uiController: UiController, view: View) {
                val stepper = generateSequence(view.parent as? View) { it.parent as? View }
                    .filterIsInstance<id.co.edtslib.uikit.stepper.StepperButton>().first()
                val counter = view.rootView.findViewById<android.widget.TextView>(R.id.tvCounter)
                val gesture = DragGesture(uiController, view)
                gesture.activate()
                gesture.move(108f)
                gesture.waitFor(650)
                assertEquals(1, stepper.getCount())
                assertEquals("2/2", counter.text.toString())
                assertTrue("Quantity rebinding must keep the touched row attached", stepper.isAttachedToWindow)
                // This control is near the bottom: reverse inside the app's touchable safe area.
                gesture.move(-16f)
                gesture.waitFor(800)
                assertEquals(0, stepper.getCount())
                gesture.release()
                assertEquals(0, stepper.getCount())
                assertEquals("1/2", counter.text.toString())
            }
        })
        captureReview("shared-limit-after-drag")
        assertFooter(R.string.tmh_add_cart, true, false, true)
    }

    @Test
    fun maximumDisablesPlusAndAllowsMinus() {
        openFreshTray()
        incrementEmina()
        incrementEmina()
        captureReview("redemption")
        onView(withId(R.id.tvCounter)).check(matches(withText("2/2")))
        onView(allOf(withId(UIKitR.id.plus_button), isDisplayed(), isDescendantOfA(
            allOf(isAssignableFrom(MaterialCardView::class.java), hasDescendant(withText(R.string.tmh_emina)))
        ))).check(matches(not(isEnabled())))
        decrementEmina()
        onView(withId(R.id.tvCounter)).check(matches(withText("1/2")))
    }

    @Test
    fun closeAndBackGuardChangedQuantities() {
        openFreshTray()
        incrementEmina()
        onView(withId(UIKitR.id.btnClose)).perform(click())
        settleUi()
        assertUnsavedAndStay()
        pressBack()
        settleUi()
        assertUnsavedAndStay()
        decrementEmina()
        onView(withId(UIKitR.id.btnClose)).perform(click())
        onView(withId(R.id.tvTitle)).check(matches(isDisplayed()))
    }

    @Test
    fun scrimGuardsChangedQuantities() {
        openFreshTray()
        incrementEmina()
        onView(isRoot()).perform(GeneralClickAction(
            Tap.SINGLE,
            { view -> floatArrayOf(view.rootView.width / 2f, 150f) },
            Press.FINGER,
        ))
        settleUi()
        assertUnsavedAndStay()
    }

    @Test
    fun swipeGuardsChangedQuantitiesAndDiscardLeavesCartUnchanged() {
        openFreshTray()
        incrementEmina()
        onView(withId(UIKitR.id.dragHandleView)).perform(GeneralSwipeAction(
            Swipe.SLOW,
            { view ->
                val location = IntArray(2)
                view.getLocationOnScreen(location)
                floatArrayOf(location[0] + view.width / 2f, location[1] + view.height / 2f)
            },
            { view -> floatArrayOf(view.rootView.width / 2f, view.rootView.height * 0.95f) },
            Press.FINGER,
        ))
        settleUi()
        onView(withText(R.string.tmh_unsaved_title)).check(matches(isDisplayed()))
        captureReview("unsaved-before-discard")
        onView(withId(R.id.btnLeave)).perform(click())
        settleUi(900)
        captureReview("after-discard")
        openFreshTray()
        onView(withId(R.id.tvCounter)).check(matches(withText("0/2")))
    }

    @Test
    fun scenarioSelectorAndHeightCap() {
        onView(withId(R.id.tvTitle)).perform(longClick())
        settleUi()
        captureReview("scenarios")
        onView(withText(R.string.tmh_scenario_full)).perform(click())
        settleUi()
        clickParagonAction(R.string.tmh_edit)
        onView(withId(R.id.tvCounter)).check(matches(withText("2/2")))
        var windowHeight = 0
        activity.onActivity { windowHeight = it.window.decorView.height }
        onView(withId(UIKitR.id.flRoot)).check { view, error ->
            if (error != null) throw error
            val sheet = view.parent as View
            assertTrue("Sheet height must stay within 90% of the window", sheet.height <= (windowHeight * 0.9f).toInt())
        }
    }

    @Test
    fun pendingRemovalSurvivesActivityRecreation() {
        openFreshTray()
        incrementEmina()
        onView(withId(R.id.btnSave)).perform(click())
        settleUi(1700)
        clickParagonAction(R.string.tmh_edit)
        decrementEmina()
        activity.recreate()
        settleUi()
        onView(withId(R.id.tvCounter)).check(matches(withText("0/2")))
        onView(withId(R.id.btnSave)).check(matches(isEnabled()))
    }

    @Test
    fun tabTapAndManualScrollStaySynchronized() {
        onView(allOf(
            withText(R.string.tmh_toples_fair),
            isDescendantOfA(withId(R.id.fairTabs)),
        )).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> = isDisplayed()
            override fun getDescription(): String = "Select the partially visible campaign tab"
            override fun perform(uiController: UiController, view: View) {
                (view.parent as View).performClick()
                uiController.loopMainThreadForAtLeast(1200)
            }
        })
        activity.onActivity { screen ->
            val list = screen.findViewById<RecyclerView>(R.id.rvFairs)
            val manager = list.layoutManager as LinearLayoutManager
            assertEquals(1, manager.findFirstVisibleItemPosition())
            assertEquals(list.paddingTop, manager.getDecoratedTop(requireNotNull(manager.findViewByPosition(1))))
            list.smoothScrollBy(0, list.height)
        }
        settleUi(1000)
        activity.onActivity { screen ->
            val list = screen.findViewById<RecyclerView>(R.id.rvFairs)
            val first = (list.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            val tabs = screen.findViewById<TabLayout>(R.id.fairTabs)
            val expected = if (!list.canScrollVertically(1) && list.canScrollVertically(-1)) tabs.tabCount - 1 else first
            assertEquals(expected, tabs.selectedTabPosition)
            val active = requireNotNull(tabs.getTabAt(expected)).view
            assertTrue(active.isSelected)
            val left = active.left - tabs.scrollX + tabs.paddingLeft
            val right = left + active.width
            val alignmentError = when (expected) {
                0 -> left - tabs.paddingLeft
                tabs.tabCount - 1 -> right - (tabs.width - tabs.paddingRight)
                else -> left + active.width / 2 - tabs.width / 2
            }
            assertTrue(kotlin.math.abs(alignmentError) <= 2)
        }
    }

    @Test
    fun defaultFixturesAndTabEndpointsMatchReference() {
        chooseScenario(R.string.tmh_scenario_default)
        captureReview("listing")
        activity.onActivity { screen ->
            val tabs = screen.findViewById<TabLayout>(R.id.fairTabs)
            assertEquals(0, tabs.selectedTabPosition)
            assertEquals(0, tabs.scrollX)
            assertEquals(TmhFlowState.create(screen, TmhScenario.DEFAULT).cartCount.toString(),
                screen.findViewById<android.widget.TextView>(R.id.tvCartCount).text.toString())
            val list = screen.findViewById<RecyclerView>(R.id.rvFairs)
            assertEquals(8, list.adapter?.itemCount)
            (list.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(7, 0)
        }
        settleUi(1800)
        activity.onActivity { screen ->
            val tabs = screen.findViewById<TabLayout>(R.id.fairTabs)
            val last = requireNotNull(tabs.getTabAt(7)).view
            assertEquals(7, tabs.selectedTabPosition)
            assertTrue(last.isSelected)
            assertTrue(kotlin.math.abs(last.right - tabs.scrollX + tabs.paddingLeft - (tabs.width - tabs.paddingRight)) <= 2)
        }
        captureReview("campaigns-last")
        for ((position, name) in listOf(3 to "campaigns-middle", 5 to "campaigns-unavailable")) {
            activity.onActivity { screen ->
                val list = screen.findViewById<RecyclerView>(R.id.rvFairs)
                (list.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 0)
            }
            settleUi(700)
            captureReview(name)
        }
    }

    @Test
    fun shortSheetWrapsContentAndFooterHasSafeArea() {
        chooseScenario(R.string.tmh_scenario_partial)
        activity.onActivity { screen ->
            val list = screen.findViewById<RecyclerView>(R.id.rvFairs)
            (list.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(1, 0)
        }
        settleUi()
        onView(allOf(withId(UIKitR.id.tvAction), withText(R.string.tmh_edit), isDisplayed(),
            isDescendantOfA(allOf(isAssignableFrom(MaterialCardView::class.java),
                hasDescendant(withText(R.string.tmh_toples_fair)))))).perform(click())
        settleUi()
        captureReview("short-sheet")
        onView(withId(UIKitR.id.flRoot)).check { view, error ->
            if (error != null) throw error
            val sheet = view.parent as View
            assertTrue("A short sheet must not fill the 90% cap", sheet.height < (sheet.rootView.height * 0.9f).toInt() - 20)
            val footer = view.findViewById<View>(R.id.footer)
            val safeBottom = androidx.core.view.ViewCompat.getRootWindowInsets(view)
                ?.getInsets(androidx.core.view.WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0
            assertTrue("Footer must include the navigation safe area", footer.paddingBottom >= safeBottom)
        }
        pressBack()
    }

    @Test
    fun landscapeSheetCapAndFooterStayInsideSafeBounds() {
        openFreshTray()
        activity.onActivity { screen ->
            screen.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        settleUi(1400)
        onView(withId(UIKitR.id.flRoot)).check { view, error ->
            if (error != null) throw error
            val sheet = view.parent as View
            assertTrue(sheet.height <= (sheet.rootView.height * 0.9f).toInt())
            val button = view.findViewById<View>(R.id.btnSave)
            val location = IntArray(2)
            button.getLocationOnScreen(location)
            val insets = androidx.core.view.ViewCompat.getRootWindowInsets(view)
                ?.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            assertTrue(location[0] >= (insets?.left ?: 0))
            assertTrue(location[0] + button.width <= sheet.rootView.width - (insets?.right ?: 0))
        }
        captureReview("landscape")
        activity.onActivity { screen ->
            screen.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        settleUi(1000)
    }

    @Test
    fun inlineAlertActionCentersIconAndPreservesLegacyButton() {
        var actionClicks = 0
        var buttonClicks = 0
        lateinit var alert: id.co.edtslib.uikit.alert.AlertBox
        activity.onActivity { screen ->
            alert = id.co.edtslib.uikit.alert.AlertBox(screen)
            assertEquals(id.co.edtslib.uikit.alert.AlertBox.StartIconVerticalAlignment.TOP, alert.startIconVerticalAlignment)
            assertTrue(!alert.isActionVisible)
            alert.text = "A multiline alert message that wraps across several lines and requires a centered start icon beside all of its text."
            alert.startIconVerticalAlignment = id.co.edtslib.uikit.alert.AlertBox.StartIconVerticalAlignment.CENTER
            alert.isCloseIconVisible = false
            alert.actionText = "Ubah"
            alert.isActionVisible = true
            alert.buttonText = "Legacy button"
            alert.isButtonVisible = true
            alert.delegate = object : id.co.edtslib.uikit.alert.AlertBoxDelegate {
                override fun onCloseClickListener(view: View) = Unit
                override fun onButtonClickListener(view: View) { buttonClicks++ }
                override fun onActionClickListener(view: View) { actionClicks++ }
            }
            screen.findViewById<android.view.ViewGroup>(R.id.main).addView(alert,
                android.view.ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        settleUi()
        activity.onActivity {
            val icon = alert.findViewById<View>(UIKitR.id.ivAlertIcon)
            val text = alert.findViewById<android.widget.TextView>(UIKitR.id.tvAlertMessage)
            assertTrue(text.lineCount > 1)
            assertTrue(kotlin.math.abs(icon.top + icon.height / 2 - (text.top + text.height / 2)) <= 1)
            val widthWithAction = text.width
            alert.findViewById<View>(UIKitR.id.tvAction).performClick()
            alert.findViewById<View>(UIKitR.id.btnAction).performClick()
            assertEquals(1, actionClicks)
            assertEquals(1, buttonClicks)
            alert.isActionVisible = false
            alert.tag = widthWithAction
        }
        settleUi()
        activity.onActivity {
            assertTrue(alert.findViewById<View>(UIKitR.id.tvAlertMessage).width > alert.tag as Int)
        }
    }

    @Test
    fun emptyScenarioAndErrorRetryReturnToCampaigns() {
        onView(withId(R.id.tvTitle)).perform(longClick())
        settleUi()
        onView(withText(R.string.tmh_scenario_empty)).perform(click())
        settleUi()
        onView(withText(R.string.tmh_empty_title)).check(matches(isDisplayed()))
        onView(withId(R.id.btnAllPromos)).perform(click())
        settleUi()
        onView(withId(R.id.tvTitle)).perform(longClick())
        settleUi()
        onView(withText(R.string.tmh_scenario_error)).perform(click())
        settleUi()
        onView(withText(R.string.tmh_load_error)).check(matches(isDisplayed()))
        onView(withText(R.string.tmh_retry)).perform(click())
        settleUi(1600)
        activity.onActivity { screen ->
            assertEquals(8, screen.findViewById<RecyclerView>(R.id.rvFairs).adapter?.itemCount)
        }
        onView(allOf(withId(UIKitR.id.tvAction), withText(R.string.tmh_edit), isDisplayed(),
            isDescendantOfA(allOf(isAssignableFrom(MaterialCardView::class.java),
                hasDescendant(withText(R.string.tmh_toples_fair)))))).check(matches(isDisplayed()))
    }
}
