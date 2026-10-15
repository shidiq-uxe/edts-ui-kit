package id.co.edtslib.edtsuikit.tmh

import android.animation.ValueAnimator
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import id.co.edtslib.edtsuikit.GuidelinesTmhActivity
import id.co.edtslib.edtsuikit.R
import id.co.edtslib.edtsuikit.databinding.ItemTmhRedemptionProductBinding
import id.co.edtslib.uikit.utils.color
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import id.co.edtslib.uikit.R as UIKitR

@RunWith(AndroidJUnit4::class)
class TmhProductSelectionAnimatorTest {

    private lateinit var activity: ActivityScenario<GuidelinesTmhActivity>
    private lateinit var parent: FrameLayout
    private lateinit var row: ItemTmhRedemptionProductBinding
    private lateinit var animator: TmhProductSelectionAnimator

    @Before
    fun launch() {
        activity = ActivityScenario.launch(GuidelinesTmhActivity::class.java)
        activity.onActivity { screen ->
            parent = FrameLayout(screen)
            row = ItemTmhRedemptionProductBinding.inflate(screen.layoutInflater, parent, false)
            row.root.id = View.generateViewId()
            row.tvName.setText(R.string.tmh_emina)
            row.ivProduct.setImageResource(R.drawable.tmh_product_emina)
            row.tvPrice.setText(R.string.tmh_price_5000)
            row.stepper.setAllowTextEditing(false)
            row.stepper.isAlwaysExpanded = true
            parent.addView(row.root, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, android.view.Gravity.CENTER))
            screen.findViewById<ViewGroup>(R.id.main).addView(parent,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            animator = TmhProductSelectionAnimator(row)
        }
    }

    @After
    fun close() {
        activity.onActivity { animator.release() }
        activity.close()
    }

    private fun exercise(block: (UiController) -> Unit) {
        onView(withId(row.root.id)).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> = isDisplayed()
            override fun getDescription(): String = "Exercise interrupted card animation and row lifecycle"
            override fun perform(uiController: UiController, view: View) { block(uiController) }
        })
    }

    @Test
    fun rapidSelectionReversalRetargetsAndStaleExitCannotHideTheCorner() = exercise { ui ->
        animator.bind("emina", false)
        animator.bind("emina", true)
        assertEquals(0f, row.ivSelected.alpha, 0.01f)
        assertEquals(-row.ivSelected.layoutParams.width.toFloat(), row.ivSelected.translationX, 0.01f)
        ui.loopMainThreadForAtLeast(70)
        val alpha = row.ivSelected.alpha
        val translation = row.ivSelected.translationX
        animator.bind("emina", false)
        animator.bind("emina", true)
        assertEquals(alpha, row.ivSelected.alpha, 0.01f)
        assertEquals(translation, row.ivSelected.translationX, 0.01f)
        ui.loopMainThreadForAtLeast(400)
        assertTrue(row.ivSelected.isShown)
        assertEquals(1f, row.ivSelected.alpha, 0.01f)
        assertEquals(0f, row.ivSelected.translationX, 0.01f)
        assertEquals(row.root.context.color(UIKitR.color.primary_30), row.root.strokeColor)
        animator.bind("emina", true)
        assertEquals(1f, row.ivSelected.alpha, 0.01f)
        animator.bind("emina", false)
        ui.loopMainThreadForAtLeast(300)
        assertEquals(View.GONE, row.ivSelected.visibility)
        assertEquals(row.root.context.color(UIKitR.color.black_30), row.root.strokeColor)
    }

    @Test
    fun newProductAndDetachedRowsSettleImmediatelyWithoutReplayingEntry() = exercise { ui ->
        animator.bind("emina", true)
        assertEquals(1f, row.ivSelected.alpha, 0.01f)
        animator.bind("emina", false)
        animator.bind("kahf", true)
        assertEquals(1f, row.ivSelected.alpha, 0.01f)
        ui.loopMainThreadForAtLeast(300)
        assertTrue(row.ivSelected.isShown)
        animator.bind("kahf", false)
        parent.removeView(row.root)
        assertEquals(View.GONE, row.ivSelected.visibility)
        assertEquals(row.root.context.color(UIKitR.color.black_30), row.root.strokeColor)
        parent.addView(row.root)
        ui.loopMainThreadForAtLeast(300)
        assertFalse(row.ivSelected.isShown)
    }

    @Test
    fun disabledSystemAnimationsApplyTheFinalStateSynchronously() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val resolver = instrumentation.targetContext.contentResolver
        val original = Settings.Global.getString(resolver, Settings.Global.ANIMATOR_DURATION_SCALE)
        fun setScale(value: String?) {
            val command = if (value == null) "settings delete global animator_duration_scale"
                else "settings put global animator_duration_scale $value"
            instrumentation.uiAutomation.executeShellCommand(command).use { descriptor ->
                java.io.FileInputStream(descriptor.fileDescriptor).use { it.readBytes() }
            }
        }
        try {
            setScale("0")
            exercise { ui ->
                ui.loopMainThreadForAtLeast(200)
                assertFalse(ValueAnimator.areAnimatorsEnabled())
                animator.bind("emina", false)
                animator.bind("emina", true)
                assertEquals(1f, row.ivSelected.alpha, 0.01f)
                assertEquals(0f, row.ivSelected.translationX, 0.01f)
                assertEquals(row.root.context.color(UIKitR.color.primary_30), row.root.strokeColor)
                animator.bind("emina", false)
                assertEquals(View.GONE, row.ivSelected.visibility)
                assertEquals(row.root.context.color(UIKitR.color.black_30), row.root.strokeColor)
            }
        } finally {
            setScale(original)
        }
    }
}
