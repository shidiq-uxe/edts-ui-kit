@file:Suppress("DEPRECATION")

package id.co.edtslib.edtsuikit

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.RectF
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import id.co.edtslib.uikit.button.Button
import id.co.edtslib.uikit.button.ButtonAttrsFactory
import id.co.edtslib.uikit.core.button.CoreButton
import id.co.edtslib.uikit.recipes.button.klik.KlikButton
import id.co.edtslib.uikit.recipes.button.poinku.PoinkuButton
import id.co.edtslib.uikit.utils.drawable
import id.co.edtslib.uikit.utils.color
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import id.co.edtslib.uikit.R as UIKitR

/** Expectations are snapshots of the supplied Figma components, not values read from our styles. */
@RunWith(AndroidJUnit4::class)
class ButtonContractTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    private fun context(fontScale: Float = 1f): Context {
        val target = instrumentation.targetContext
        val configuration = Configuration(target.resources.configuration).apply { this.fontScale = fontScale }
        return ContextThemeWrapper(target.createConfigurationContext(configuration), UIKitR.style.Theme_EDTS_UIKit)
    }

    private fun measure(button: CoreButton) {
        val unspecified = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        button.measure(unspecified, unspecified)
        button.layout(0, 0, button.measuredWidth, button.measuredHeight)
    }

    private fun touch(button: CoreButton) {
        val time = SystemClock.uptimeMillis()
        for (action in listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP)) {
            MotionEvent.obtain(time, time, action, button.width / 2f, button.height / 2f, 0).also {
                button.dispatchTouchEvent(it)
                it.recycle()
            }
        }
    }

    @Test
    fun bothProductsMatchAllInspectedFigmaStates() {
        val source = instrumentation.context.assets.open("button_design_contract.json").bufferedReader().use { it.readText() }
        val expectations = JSONArray(source)
        assertEquals(153, expectations.length())
        instrumentation.runOnMainSync {
            val context = context()
            val density = context.resources.displayMetrics.density
            for (index in 0 until expectations.length()) {
                val expected = expectations.getJSONObject(index)
                val label = expected.getString("source")
                val button = if (expected.getString("product") == "Klik") KlikButton(context) else PoinkuButton(context)
                val style = context.resources.getIdentifier(expected.getString("style"), "style", context.packageName)
                assertTrue("$label recipe exists", style != 0)
                button.buttonRecipe = style
                button.text = if (expected.getBoolean("icon")) "" else "Button"
                button.startIcon = context.drawable(UIKitR.drawable.ic_placeholder_medium_24)
                button.endIcon = if (expected.getBoolean("icon")) null else context.drawable(UIKitR.drawable.ic_placeholder_medium_24)
                button.isEnabled = !expected.getBoolean("disabled")
                button.isDestructive = expected.getBoolean("destructive")
                button.isPressed = expected.getBoolean("pressed")
                button.stateListAnimator?.jumpToCurrentState()
                button.jumpDrawablesToCurrentState()
                measure(button)
                val state = button.drawableState
                assertEquals("$label fill", Color.parseColor(expected.getString("fill")), button.backgroundTintList!!.getColorForState(state, 0))
                assertEquals("$label content", Color.parseColor(expected.getString("content")), button.iconTint!!.getColorForState(state, 0))
                assertEquals("$label stroke", Color.parseColor(expected.getString("stroke")), button.strokeColor!!.getColorForState(state, 0))
                assertEquals("$label height (text=${button.textSize}, line=${button.lineHeight}, fontScale=${button.resources.configuration.fontScale})", expected.getDouble("height").toFloat(), button.measuredHeight / density, 0.6f)
                assertEquals("$label corner", expected.getDouble("radius").toFloat(), button.cornerRadius / density, 0.6f)
                val bounds = RectF(0f, 0f, button.measuredWidth.toFloat(), button.measuredHeight.toFloat())
                val shape = button.shapeAppearanceModel
                for (corner in listOf(shape.topLeftCornerSize, shape.topRightCornerSize, shape.bottomLeftCornerSize, shape.bottomRightCornerSize)) {
                    assertEquals("$label rendered corner", expected.getDouble("radius").toFloat(), corner.getCornerSize(bounds) / density, 0.6f)
                }
                assertEquals("$label icon", expected.getDouble("iconSize").toFloat(), button.startIcon!!.bounds.width() / density, 0.6f)
                if (expected.getBoolean("icon")) {
                    assertEquals("$label width", expected.getDouble("width").toFloat(), button.measuredWidth / density, 0.6f)
                } else {
                    val designTextSize = expected.getDouble("textSize").toFloat()
                    assertEquals("$label text size", designTextSize * button.resources.displayMetrics.scaledDensity, button.textSize, 1f)
                    assertEquals("$label text color", Color.parseColor(expected.getString("content")), button.currentTextColor)
                    assertEquals("$label font metrics", expected.getDouble("textWidth").toFloat(), button.paint.measureText("Button") * designTextSize / button.textSize, 1f)
                }
            }
        }
    }

    @Test
    fun xmlInflatesBothProductsCoreAndProductionEntryPoint() {
        instrumentation.runOnMainSync {
            val testContext = context()
            val root = LayoutInflater.from(testContext).inflate(R.layout.view_button_recipe_examples, null)
            val klik = root.findViewById<KlikButton>(R.id.klikButton)
            val poinku = root.findViewById<PoinkuButton>(R.id.poinkuButton)
            val core = root.findViewById<CoreButton>(R.id.coreButton)
            val legacy = root.findViewById<Button>(R.id.legacyButton)
            assertEquals(UIKitR.style.Widget_EDTS_UIKit_Button_Klik_Secondary_Medium, klik.buttonRecipe)
            assertNotNull(klik.startIcon)
            assertNotNull(klik.endIcon)
            assertEquals(18f * klik.resources.displayMetrics.scaledDensity, klik.textSize, 1f)
            assertEquals(klik.context.color(android.R.color.holo_green_dark), klik.currentTextColor)
            assertEquals(UIKitR.style.Widget_EDTS_UIKit_Button_Poinku_Text_Small, poinku.buttonRecipe)
            assertEquals(UIKitR.style.Widget_EDTS_UIKit_Button_Poinku_Outline_Large, core.buttonRecipe)
            assertEquals(Button.ButtonType.TERTIARY_LARGE, legacy.buttonType)
            assertTrue(legacy.isDestructive)
            assertEquals(0.8f, legacy.pressedScale, 0f)
        }
    }

    @Test
    fun legacyEnumsConstructorsSettersAndFactoryRemainUsable() {
        instrumentation.runOnMainSync {
            val context = context()
            val variants = listOf("FILLED", "FILLED_MEDIUM", "FILLED_LARGE", "SECONDARY", "SECONDARY_MEDIUM", "SECONDARY_LARGE", "TERTIARY", "TERTIARY_MEDIUM", "TERTIARY_LARGE", "TEXT", "TEXT_MEDIUM", "TEXT_LARGE")
            assertEquals(variants, Button.ButtonType.values().map { it.name })
            listOf(Button(context), Button(context, null), Button(context, null, UIKitR.attr.buttonStyle)).forEach { button ->
                assertEquals(0.95f, button.pressedScale, 0f)
                assertFalse(button.shouldShowShimmer)
                for ((ordinal, variant) in Button.ButtonType.values().withIndex()) {
                    assertEquals(ordinal, variant.ordinal)
                    button.buttonType = variant
                    assertEquals(variant, button.buttonType)
                    ButtonAttrsFactory.applyAttributes(context, button, variant.styleRes)
                }
                button.pressedScale = 0.85f
                button.isDestructive = true
                assertTrue(button.drawableState.contains(UIKitR.attr.state_destructive))
                button.backgroundTintList = ColorStateList.valueOf(Color.MAGENTA)
                assertEquals(Color.MAGENTA, button.backgroundTintList!!.defaultColor)
            }
        }
    }

    @Test
    fun disabledButtonsDoNotDispatchUserClicksAndResetPressedScale() {
        instrumentation.runOnMainSync {
            for (button in listOf(KlikButton(context()), PoinkuButton(context()), Button(context()))) {
                var clicks = 0
                button.setOnClickListener { clicks++ }
                button.text = "Action"
                measure(button)
                touch(button)
                // Unattached Views dispatch a posted click only after being attached; test disabled directly.
                val previousClicks = clicks
                button.scaleX = 0.8f
                button.isPressed = true
                button.isEnabled = false
                touch(button)
                assertEquals(previousClicks, clicks)
                assertFalse(button.isPressed)
                assertEquals(1f, button.scaleX, 0f)
            }
        }
    }

    @Test
    fun iconOnlyButtonsKeepTheirRoleAndScreenSuppliedAccessibleLabel() {
        ActivityScenario.launch(GuidelinesButtonActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val host = FrameLayout(activity)
                activity.setContentView(host)
                for (button in listOf(KlikButton(activity), PoinkuButton(activity))) {
                    button.text = ""
                    button.startIcon = button.context.drawable(UIKitR.drawable.ic_placeholder_medium_24)
                    button.contentDescription = "Open details"
                    button.setOnClickListener { }
                    host.addView(button)
                    val node = button.createAccessibilityNodeInfo()
                    assertEquals("android.widget.Button", node.className.toString())
                    assertEquals("Open details", node.contentDescription.toString())
                    assertTrue(node.isClickable)
                    assertTrue(button.isFocusable)
                    button.isEnabled = false
                    assertFalse(button.createAccessibilityNodeInfo().isEnabled)
                    host.removeView(button)
                }
            }
        }
    }

    @Test
    fun changingRecipeKeepsContentCallbacksAndSlotGeometryInRtl() {
        instrumentation.runOnMainSync {
            val context = context()
            val button = KlikButton(context)
            var clicks = 0
            button.text = "Continue"
            button.startIcon = context.drawable(UIKitR.drawable.ic_placeholder_medium_24)
            button.endIcon = context.drawable(UIKitR.drawable.ic_placeholder_medium_24)
            button.setOnClickListener { clicks++ }
            button.buttonRecipe = UIKitR.style.Widget_EDTS_UIKit_Button_Klik_Secondary_Large
            button.layoutDirection = View.LAYOUT_DIRECTION_RTL
            measure(button)
            assertSame(button.startIcon, button.compoundDrawablesRelative[0])
            assertSame(button.endIcon, button.compoundDrawablesRelative[2])
            assertEquals(button.iconSize, button.endIcon!!.bounds.width())
            button.iconSize = (16 * context.resources.displayMetrics.density).toInt()
            assertEquals(button.iconSize, button.startIcon!!.bounds.width())
            button.performClick()
            assertEquals(1, clicks)
            assertEquals("Continue", button.text.toString())
            button.startIcon = null
            button.endIcon = null
            assertTrue(button.compoundDrawablesRelative.all { it == null })
        }
    }

    @Test
    fun largeFontScaleGrowsTextButtonsInsteadOfClipping() {
        instrumentation.runOnMainSync {
            for (button in listOf(KlikButton(context(1.5f)), PoinkuButton(context(1.5f)))) {
                button.text = "Continue"
                measure(button)
                assertTrue(button.measuredHeight > button.minHeight)
                val contentHeight = button.measuredHeight - button.paddingTop - button.paddingBottom
                assertTrue(contentHeight >= button.paint.fontMetricsInt.descent - button.paint.fontMetricsInt.ascent)
            }
        }
    }

    @Test
    fun shimmerIsIdempotentAndRestoresParentWithoutLosingClickListener() {
        ActivityScenario.launch(GuidelinesButtonActivity::class.java).use { scenario ->
            lateinit var button: Button
            lateinit var host: FrameLayout
            var clicks = 0
            scenario.onActivity { activity ->
                host = FrameLayout(activity)
                button = Button(activity).apply {
                    text = "Action"
                    setOnClickListener { clicks++ }
                }
                host.addView(button)
                activity.setContentView(host)
                button.shouldShowShimmer = true
            }
            instrumentation.runOnMainSync {
                val shimmer = button.shimmerFrameLayout
                assertNotNull(shimmer)
                assertSame(shimmer, button.parent)
                button.shouldShowShimmer = true
            }
            instrumentation.runOnMainSync {
                assertSame(host, button.shimmerFrameLayout!!.parent)
                assertEquals(1, host.childCount)
                val shimmer = button.shimmerFrameLayout!!
                host.removeAllViews()
                assertEquals(null, button.shimmerFrameLayout)
                host.addView(shimmer)
                assertSame(shimmer, button.shimmerFrameLayout)
                button.shouldShowShimmer = false
            }
            instrumentation.runOnMainSync {
                assertSame(host, button.parent)
                assertEquals(1, host.childCount)
                button.performClick()
                assertEquals(1, clicks)
            }
        }
    }
}
