package id.co.edtslib.edtsuikit

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.snackbar.Snackbar
import id.co.edtslib.uikit.core.snackbar.CoreSnackbar
import id.co.edtslib.uikit.recipes.snackbar.klik.KlikToast
import id.co.edtslib.uikit.recipes.snackbar.poinku.PoinkuToast
import id.co.edtslib.uikit.utils.AlertType
import id.co.edtslib.uikit.utils.alertSnack
import id.co.edtslib.uikit.utils.font
import id.co.edtslib.uikit.utils.snack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import id.co.edtslib.uikit.R as UIKitR

@RunWith(AndroidJUnit4::class)
class SnackbarContractTest {

    @Test
    fun productRecipesKeepGeometryTypographyAndMaterialActions() {
        ActivityScenario.launch(GuidelinesSnackbarActivity::class.java).use { scenario ->
            val products = listOf(
                Triple(KlikToast, UIKitR.font.inter_fallback, UIKitR.font.inter_semibold_fallback),
                Triple(PoinkuToast, UIKitR.font.rubik_fallback, UIKitR.font.rubik_medium_fallback)
            )
            for ((presenter, messageFont, actionFont) in products) {
                for (variant in CoreSnackbar.Variant.entries) {
                    val shown = CountDownLatch(1)
                    val dismissed = CountDownLatch(1)
                    var bar: Snackbar? = null
                    var clicks = 0
                    scenario.onActivity { activity ->
                        val anchor = activity.findViewById<View>(R.id.btnShowSnackbar)
                        bar = presenter.show(
                            host = anchor,
                            message = "Saved",
                            variant = variant,
                            iconRes = UIKitR.drawable.ic_placeholder_medium_24,
                            actionText = "Button",
                            anchor = anchor,
                            duration = Snackbar.LENGTH_INDEFINITE
                        ) { clicks++ }.addCallback(object : Snackbar.Callback() {
                            override fun onShown(transientBottomBar: Snackbar) {
                                shown.countDown()
                            }

                            override fun onDismissed(transientBottomBar: Snackbar, event: Int) {
                                if (event == DISMISS_EVENT_ACTION) dismissed.countDown()
                            }
                        })
                    }
                    assertTrue("Snackbar shown", shown.await(6, TimeUnit.SECONDS))
                    scenario.onActivity { activity ->
                        val snackbar = requireNotNull(bar)
                        val message = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                        val action = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_action)
                        val density = activity.resources.displayMetrics.density
                        val inset = (16 * density).roundToInt()
                        val margins = snackbar.view.layoutParams as ViewGroup.MarginLayoutParams
                        assertEquals("Bar height", 48 * density, snackbar.view.height.toFloat(), 1f)
                        assertEquals("Leading bar margin", inset, margins.leftMargin)
                        assertEquals("Trailing bar margin", inset, margins.rightMargin)
                        assertEquals("Leading content inset", inset, (message.parent as View).left + message.left + message.paddingLeft)
                        assertEquals("Icon bounds", (16 * density).roundToInt(), message.compoundDrawablesRelative[0].bounds.width())
                        assertEquals("Message face", activity.font(messageFont), message.typeface)
                        assertEquals("Action face", activity.font(actionFont), action.typeface)
                        assertEquals("Button", action.text.toString())
                        assertSame(activity.findViewById<View>(R.id.btnShowSnackbar), snackbar.anchorView)

                        val bitmap = Bitmap.createBitmap(snackbar.view.width, snackbar.view.height, Bitmap.Config.ARGB_8888)
                        snackbar.view.draw(Canvas(bitmap))
                        val expected = if (variant == CoreSnackbar.Variant.INFO) "#434755" else "#EE2B2E"
                        assertEquals("Surface color", Color.parseColor(expected), bitmap.getPixel(bitmap.width / 2, (4 * density).roundToInt()))
                        bitmap.recycle()

                        assertTrue(action.performClick())
                        assertEquals(1, clicks)
                    }
                    assertTrue("Material action dismissed the bar", dismissed.await(6, TimeUnit.SECONDS))
                }
            }
        }
    }

    @Test
    fun optionalSlotsAndLegacyCallersRetainTheirContracts() {
        val missingHost: View? = null
        assertNull(missingHost.snack("No host"))
        assertNull(missingHost.alertSnack("No host"))

        ActivityScenario.launch(GuidelinesSnackbarActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val host = activity.findViewById<View>(R.id.btnShowSnackbar)
                val recipe = KlikToast.show(host, "Saved")
                assertNull(recipe.anchorView)
                assertNull(recipe.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).compoundDrawablesRelative[0])
                assertEquals(View.GONE, recipe.view.findViewById<View>(com.google.android.material.R.id.snackbar_action).visibility)
                recipe.dismiss()

                val legacy = requireNotNull(host.alertSnack(
                    message = "Legacy",
                    alertType = AlertType.ERROR,
                    startIconRes = UIKitR.drawable.ic_placeholder_medium_24,
                    isAnchored = true
                ))
                val legacyMessage = legacy.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                assertSame(host, legacy.anchorView)
                assertEquals(Snackbar.LENGTH_LONG, legacy.duration)
                assertEquals(Gravity.CENTER, legacyMessage.gravity)
                assertEquals(2, legacyMessage.maxLines)
                assertNotNull(legacyMessage.compoundDrawables[0])
                legacy.dismiss()

                var configured: Snackbar? = null
                val generic = requireNotNull(host.snack("Legacy", actionText = "Retry", onViewCreated = {
                    configured = it
                    assertEquals(View.GONE, it.view.findViewById<View>(com.google.android.material.R.id.snackbar_action).visibility)
                }))
                assertSame(generic, configured)
                assertEquals(View.VISIBLE, generic.view.findViewById<View>(com.google.android.material.R.id.snackbar_action).visibility)
                generic.dismiss()
            }
        }
    }
}
