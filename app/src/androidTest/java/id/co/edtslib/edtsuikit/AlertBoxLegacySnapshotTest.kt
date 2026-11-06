package id.co.edtslib.edtsuikit

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Rect
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/** Reflection keeps this same fixture runnable against the baseline APK and the refactored APK. */
@RunWith(AndroidJUnit4::class)
class AlertBoxLegacySnapshotTest {
    @Test
    fun legacyDefaultsAndRenderedGeometry() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val target = instrumentation.targetContext
            val context = ContextThemeWrapper(target, target.resources.getIdentifier("Theme.EDTS.UIKit", "style", target.packageName))
            val type = Class.forName("id.co.edtslib.uikit.alert.AlertBox")
            val enum = Class.forName("id.co.edtslib.uikit.alert.AlertBox\$AlertType")
            val values = enum.enumConstants!!
            assertEquals(listOf("SUCCESS", "ERROR", "WARNING", "INFO", "LIGHT"), values.map { it.toString() })
            // Downloadable-font caching can change the first checkbox measurement during startup.
            // Compare settled widget geometry, after constructing the same warm-up instance on both APKs.
            type.getConstructor(Context::class.java).newInstance(context)
            val snapshots = JSONArray()
            for (tone in values) for (footer in listOf(false, true)) {
                val alert = type.getConstructor(Context::class.java).newInstance(context) as ViewGroup
                type.getMethod("setText", CharSequence::class.java).invoke(alert, "A promotion notice with two lines of text for the customer.")
                type.getMethod("setButtonText", CharSequence::class.java).invoke(alert, "Continue")
                type.getMethod("setButtonVisible", Boolean::class.javaPrimitiveType).invoke(alert, footer)
                type.getMethod("setAlertType", enum).invoke(alert, tone)
                val width = (328 * context.resources.displayMetrics.density).toInt()
                alert.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
                alert.layout(0, 0, alert.measuredWidth, alert.measuredHeight)
                val state = JSONObject().put("tone", tone.toString()).put("footer", footer)
                for (getter in listOf("getText", "getButtonText", "getButtonType", "getStrokeColor", "getAlertBackgroundColor", "getStartIconTint", "getCloseIconTint", "getCornerRadius", "getStrokeWidth", "isButtonVisible", "isStartIconVisible", "isCloseIconVisible", "isHtml")) {
                    state.put(getter, type.getMethod(getter).invoke(alert)?.toString() ?: "null")
                }
                val card = alert.getChildAt(0)
                state.put("fill", (card.javaClass.getMethod("getCardBackgroundColor").invoke(card) as ColorStateList).defaultColor)
                state.put("stroke", (card.javaClass.getMethod("getStrokeColorStateList").invoke(card) as ColorStateList).defaultColor)
                state.put("height", alert.height)
                for (name in listOf("ivAlertIcon", "tvAlertMessage", "ivAlertClose", "btnAction")) {
                    val view = alert.findViewById<View>(target.resources.getIdentifier(name, "id", target.packageName))
                    val rect = Rect().also { view.getDrawingRect(it); alert.offsetDescendantRectToMyCoords(view, it) }
                    state.put(name, rect.flattenToString())
                    if (view is TextView) state.put(name + "Color", view.currentTextColor).put(name + "Size", view.textSize)
                }
                snapshots.put(state)
            }
            target.filesDir.resolve("alertbox-legacy-snapshot.json").writeText(snapshots.toString(2))
        }
    }
}
