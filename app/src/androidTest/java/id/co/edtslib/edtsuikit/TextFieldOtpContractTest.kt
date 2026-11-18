package id.co.edtslib.edtsuikit

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Parcelable
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.util.SparseArray
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import id.co.edtslib.uikit.R as UIKitR
import id.co.edtslib.uikit.core.textfield.CoreTextField
import id.co.edtslib.uikit.otp.OtpDelegate
import id.co.edtslib.uikit.otp.OtpGroup
import id.co.edtslib.uikit.recipes.otp.poinku.PoinkuOtpGroup
import id.co.edtslib.uikit.recipes.textfield.klik.KlikTextField
import id.co.edtslib.uikit.recipes.textfield.poinku.PoinkuTextField
import id.co.edtslib.uikit.textfield.TextField
import id.co.edtslib.uikit.textfield.TextFieldDelegate
import id.co.edtslib.uikit.utils.dp
import id.co.edtslib.uikit.utils.drawable
import java.io.File
import kotlin.math.roundToInt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TextFieldOtpContractTest {

    @Test
    fun textFieldInflatesAndUpdatesItsCounterAndError() = withActivity { activity ->
        val root = activity.layoutInflater.inflate(R.layout.view_text_field_contract, null)
        activity.setContentView(root)
        val field = root.findViewById<CoreTextField>(R.id.fieldKlik)
        val editor = field.findViewById<EditText>(UIKitR.id.etInput)
        val counter = field.findViewById<TextView>(UIKitR.id.tvCounter)
        val supporting = field.findViewById<TextView>(UIKitR.id.tvSupporting)
        val input = field.findViewById<View>(UIKitR.id.llInput)
        val events = mutableListOf<String>()
        field.setOnTextChangedListener { events.add(it) }

        assertFalse(editor.isEnabled)
        field.isEnabled = true
        assertTrue(editor.isEnabled)
        assertEquals("Email *", field.findViewById<TextView>(UIKitR.id.tvLabel).text.toString())
        assertEquals("name", field.text.toString())
        assertEquals("4/5", counter.text.toString())
        assertEquals(EditorInfo.IME_ACTION_DONE, field.imeOptions)
        assertEquals(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, field.inputType)

        field.text = "123456"
        assertEquals(listOf("12345"), events)
        assertEquals("5/5", counter.text.toString())
        field.maxLength = 3
        assertEquals("123", field.text.toString())
        assertEquals("3/3", counter.text.toString())
        field.maxLength = 8
        field.text = "12345678"
        assertEquals("8/8", counter.text.toString())

        field.errorText = "Enter a valid email"
        val node = editor.createAccessibilityNodeInfo()
        assertEquals(field.errorText, node.error)
        assertTrue(node.isContentInvalid)
        assertTrue(input.isActivated)
        assertEquals("Enter a valid email", supporting.text.toString())
        assertEquals(Color.rgb(238, 43, 46), counter.currentTextColor)
        field.errorText = null
        assertFalse(input.isActivated)
        assertEquals("Supporting text", supporting.text.toString())
        field.isEnabled = false
        assertFalse(editor.isEnabled)
        assertFalse(editor.requestFocus())
    }

    @Test
    fun passwordMasksAndPreservesSelectionAcrossVisibilityChanges() = withActivity { activity ->
        val root = activity.layoutInflater.inflate(R.layout.view_text_field_contract, null)
        activity.setContentView(root)
        val field = root.findViewById<PoinkuTextField>(R.id.fieldPoinku)
        val editor = field.findViewById<EditText>(UIKitR.id.etInput)
        val toggle = field.findViewById<View>(UIKitR.id.btnEnd)
        editor.setSelection(2, 4)
        assertTrue(editor.transformationMethod is PasswordTransformationMethod)
        assertEquals("Show password", toggle.contentDescription.toString())
        assertTrue(toggle.performClick())
        assertEquals("123456", field.text.toString())
        assertFalse(editor.transformationMethod is PasswordTransformationMethod)
        assertEquals("Hide password", toggle.contentDescription.toString())
        assertEquals(2, editor.selectionStart)
        assertEquals(4, editor.selectionEnd)

        val state = SparseArray<Parcelable>()
        field.saveHierarchyState(state)
        val restoredRoot = activity.layoutInflater.inflate(R.layout.view_text_field_contract, null)
        val restored = restoredRoot.findViewById<PoinkuTextField>(R.id.fieldPoinku)
        restored.restoreHierarchyState(state)
        val restoredEditor = restored.findViewById<EditText>(UIKitR.id.etInput)
        assertTrue(restoredEditor.transformationMethod is PasswordTransformationMethod)
        assertEquals(2, restoredEditor.selectionStart)
        assertEquals(4, restoredEditor.selectionEnd)
    }

    @Test
    fun otpPastesOnceMovesFocusBackwardAndRestoresQuietly() = withActivity { activity ->
        val group = PoinkuOtpGroup(activity).apply {
            id = View.generateViewId()
            shouldAnimateError = false
            shouldVibrate = false
        }
        activity.setContentView(group)
        val events = mutableListOf<String>()
        group.setOnOtpCompleteListener { events.add("complete:$it") }
        group.setOnTextChangedListener { events.add("change:$it") }
        val first = group.getChildAt(0) as EditText
        val second = group.getChildAt(1) as EditText
        val last = group.getChildAt(3) as EditText

        first.setText("1234")
        assertEquals("1234", group.otp)
        assertEquals(listOf("complete:1234", "change:1234"), events)
        assertTrue(last.hasFocus())
        group.clear()
        assertEquals("", group.otp)
        assertEquals("change:", events.last())
        assertEquals(3, events.size)
        assertTrue(first.hasFocus())
        first.setText("1")
        assertTrue(second.hasFocus())
        val connection = requireNotNull(second.onCreateInputConnection(EditorInfo()))
        assertTrue(connection.deleteSurroundingText(1, 0))
        assertTrue(first.hasFocus())

        group.otp = "9876"
        group.isError = true
        val state = SparseArray<Parcelable>()
        group.saveHierarchyState(state)
        val count = events.size
        group.clear()
        val restored = PoinkuOtpGroup(activity).apply { id = group.id }
        restored.setOnTextChangedListener { events.add("restored:$it") }
        activity.setContentView(restored)
        restored.restoreHierarchyState(state)
        assertEquals(count + 1, events.size)
        assertEquals("9876", restored.otp)
        assertTrue(restored.isError)
        val restoredFirst = restored.getChildAt(0) as EditText
        assertEquals(Color.rgb(238, 43, 46), restoredFirst.currentTextColor)
        assertEquals("Digit 1 of 4", restoredFirst.contentDescription)
        assertEquals("Invalid code", restoredFirst.createAccessibilityNodeInfo().error)
        restored.isEnabled = false
        assertFalse(restored.getChildAt(0).isEnabled)
        assertEquals(Color.rgb(112, 114, 125), (restored.getChildAt(0) as EditText).currentTextColor)
    }

    @Test
    fun largeTextAndNarrowWidthKeepTheLabelAndSupportingRowUsable() = withActivity { activity ->
        val scaledContext = ContextThemeWrapper(activity, UIKitR.style.Theme_EDTS_UIKit_ActionBar).apply {
            applyOverrideConfiguration(Configuration().apply { fontScale = 1.4f })
        }
        val field = PoinkuTextField(scaledContext).apply {
            label = "Kota/Kabupaten"
            isFieldRequired = true
            placeholder = "Pilih Kota atau Kabupaten"
            supportingText = "Please choose a city or district to continue"
            maxLength = 250
            counterEnabled = true
            startIcon = activity.drawable(UIKitR.drawable.ic_placeholder_medium_24)
            endIcon = activity.drawable(UIKitR.drawable.ic_placeholder_medium_24)
        }
        activity.setContentView(field)
        render(field, 180, "poinku_narrow_large_text")
        val box = field.findViewById<View>(UIKitR.id.llInput)
        val label = field.findViewById<TextView>(UIKitR.id.tvLabel)
        val editor = field.findViewById<EditText>(UIKitR.id.etInput)
        val supporting = field.findViewById<TextView>(UIKitR.id.tvSupporting)
        val counter = field.findViewById<TextView>(UIKitR.id.tvCounter)

        assertTrue(field.measuredHeight > 80.dp)
        assertTrue(label.layout.getEllipsisCount(0) > 0)
        assertTrue(editor.width > 0)
        assertTrue(supporting.lineCount > 1)
        assertEquals("0/250", counter.text.toString())
        assertTrue(box.performClick())
        assertTrue(editor.hasFocus())
    }

    @Test
    fun legacyXmlAndLengthLimitsRetainTheirSemantics() = withActivity { activity ->
        val root = activity.layoutInflater.inflate(R.layout.view_text_field_contract, null)
        activity.setContentView(root)
        val legacy = root.findViewById<TextField>(R.id.fieldLegacy)
        val events = mutableListOf<String?>()
        legacy.delegate = object : TextFieldDelegate {
            override fun onValueChange(value: String?) {
                events.add(value)
            }
        }
        assertEquals(TextField.InputType.Email, legacy.inputType)
        assertEquals(TextField.ImeOption.Done, legacy.imeOption)
        val editor = requireNotNull(legacy.editText)
        editor.setText("123456")
        assertEquals(listOf("12345"), events)
        legacy.maxLength = 7
        editor.setText("1234567")
        assertEquals("12345", editor.text.toString())
        legacy.maxLength = 0
        editor.setText("1234567")
        assertEquals("1234567", editor.text.toString())
        legacy.isLoading = true
        assertTrue(legacy.isLoading)
    }

    @Test
    fun legacyOtpRetainsSequentialEntryCompletionAndClear() = withActivity { activity ->
        val group = OtpGroup(activity)
        activity.setContentView(group)
        val events = mutableListOf<String>()
        group.delegate = object : OtpDelegate {
            override fun setOnOtpCompleteListener(otp: String) {
                events.add("complete:$otp")
            }

            override fun setOnTextChangeListener(otp: String) {
                events.add("change:$otp")
            }
        }
        val cells = (0 until group.childCount).map { requireNotNull((group.getChildAt(it) as TextField).editText) }
        cells[0].setText("1")
        assertTrue(cells[1].hasFocus())
        cells[1].dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
        assertTrue(cells[0].hasFocus())
        cells[1].setText("2")
        cells[2].setText("3")
        cells[3].setText("4")
        assertEquals(
            listOf("change:1", "change:12", "change:123", "complete:1234", "change:1234"),
            events
        )
        group.clear()
        assertEquals("", group.otp)
        assertTrue(cells[0].hasFocus())
    }

    @Test
    fun recipeRenderingsMatchReferenceGeometryAndStateColors() = withActivity { activity ->
        val host = FrameLayout(activity)
        activity.setContentView(host)
        listOf(
            Recipe(KlikTextField(activity), 64, 84, Color.rgb(108, 165, 224), "klik"),
            Recipe(PoinkuTextField(activity), 62, 80, Color.rgb(17, 120, 212), "poinku")
        ).forEach { (field, boxHeight, totalHeight, focus, name) ->
            host.removeAllViews()
            host.addView(field, FrameLayout.LayoutParams(328.dp.roundToInt(), WRAP_CONTENT))
            field.label = "Label"
            field.isFieldRequired = true
            field.placeholder = "Placeholder Text"
            field.supportingText = "Supporting text"
            field.startIcon = activity.drawable(UIKitR.drawable.ic_placeholder_medium_24)
            field.endIcon = activity.drawable(UIKitR.drawable.ic_placeholder_medium_24)
            field.maxLength = 250
            field.counterEnabled = true

            val editor = field.findViewById<EditText>(UIKitR.id.etInput)
            val box = field.findViewById<View>(UIKitR.id.llInput)
            val default = render(field, 328, "${name}_empty")
            assertHeight(field, totalHeight)
            assertHeight(box, boxHeight)
            assertEquals(Color.rgb(220, 222, 227), default.getPixel(default.width / 2, 0))

            field.text = "Placeholder Text"
            editor.requestFocus()
            val focused = render(field, 328, "${name}_focused")
            assertEquals(focus, focused.getPixel(focused.width / 2, 0))

            field.errorText = "Supporting text"
            val error = render(field, 328, "${name}_error")
            assertEquals(Color.rgb(238, 43, 46), error.getPixel(error.width / 2, 0))
            assertEquals(Color.rgb(255, 237, 238), error.getPixel(error.width / 2, 5.dp.roundToInt()))
            field.errorText = null
            field.isEnabled = false
            val disabled = render(field, 328, "${name}_disabled")
            assertEquals(Color.rgb(239, 243, 246), disabled.getPixel(disabled.width / 2, 5.dp.roundToInt()))
        }

        host.removeAllViews()
        val otp = PoinkuOtpGroup(activity).apply { this.otp = "0000" }
        host.addView(otp)
        val default = render(otp, 168, "poinku_otp_default")
        assertHeight(otp, 46)
        assertEquals("0000", otp.otp)
        assertDigitVisible(default, Color.rgb(21, 24, 35))
        assertEquals(Color.rgb(220, 222, 227), default.getPixel(18.dp.roundToInt(), default.height - 1))
        otp.isError = true
        val error = render(otp, 168, "poinku_otp_error")
        assertEquals(Color.rgb(238, 43, 46), error.getPixel(18.dp.roundToInt(), error.height - 1))
        assertDigitVisible(error, Color.rgb(238, 43, 46))
        otp.isEnabled = false
        val disabled = render(otp, 168, "poinku_otp_disabled")
        assertDigitVisible(disabled, Color.rgb(112, 114, 125))
        otp.isError = false
        otp.isEnabled = true
        otp.clear()
        val active = render(otp, 168, "poinku_otp_active")
        assertEquals(Color.rgb(17, 120, 212), active.getPixel(18.dp.roundToInt(), active.height - 1))
    }

    @Test
    fun samplesExposeBothProductsAndPoinkuOtpStates() {
        ActivityScenario.launch(GuidelinesTextfieldActivity::class.java).use { scenario ->
            scenario.onActivity {
                assertTrue(it.findViewById<ViewGroup>(R.id.llRecipes).getChildAt(0) is KlikTextField)
            }
            onView(withId(R.id.tvProduct)).perform(click())
            onView(withText("Poinku")).perform(click())
            scenario.onActivity {
                val examples = it.findViewById<ViewGroup>(R.id.llRecipes)
                assertTrue(examples.getChildAt(0) is PoinkuTextField)
                assertTrue((examples.getChildAt(examples.childCount - 1) as PoinkuTextField).passwordToggleEnabled)
            }
            onView(withId(R.id.tvProduct)).perform(click())
            onView(withText("Legacy")).perform(click())
            scenario.onActivity { assertTrue(it.findViewById<TextField>(R.id.tilLabelInside).isShown) }
        }
        ActivityScenario.launch(GuidelinesOtpActivity::class.java).use { scenario ->
            scenario.onActivity {
                assertEquals("", it.findViewById<PoinkuOtpGroup>(R.id.ogPoinkuDefault).otp)
                assertTrue(it.findViewById<PoinkuOtpGroup>(R.id.ogPoinkuError).isError)
                val disabled = it.findViewById<PoinkuOtpGroup>(R.id.ogPoinkuDisabled)
                assertEquals("0000", disabled.otp)
                assertFalse(disabled.getChildAt(0).isEnabled)
            }
        }
    }

    private fun assertDigitVisible(bitmap: Bitmap, color: Int) {
        val digitsHeight = bitmap.height - 3.dp.roundToInt()
        val pixels = IntArray(bitmap.width * digitsHeight)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, digitsHeight)
        assertTrue("Expected a visible OTP digit with color $color", pixels.contains(color))
    }

    private fun assertHeight(view: View, expectedDp: Int) {
        val actualDp = view.measuredHeight / view.resources.displayMetrics.density
        assertTrue("Expected $expectedDp dp, measured $actualDp dp", kotlin.math.abs(actualDp - expectedDp) < 1.5f)
    }

    private fun render(view: View, widthDp: Int, name: String): Bitmap {
        val width = widthDp.dp.roundToInt()
        val mode = View.MeasureSpec.EXACTLY
        view.measure(View.MeasureSpec.makeMeasureSpec(width, mode), View.MeasureSpec.UNSPECIFIED)
        view.layout(0, 0, width, view.measuredHeight)
        val bitmap = Bitmap.createBitmap(width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        File(view.context.filesDir, "textfield_$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        return bitmap
    }

    private fun withActivity(action: (GuidelinesTextfieldActivity) -> Unit) {
        ActivityScenario.launch(GuidelinesTextfieldActivity::class.java).use { scenario ->
            scenario.onActivity { action(it) }
        }
    }

    private data class Recipe(
        val field: CoreTextField,
        val boxHeight: Int,
        val totalHeight: Int,
        val focusedStroke: Int,
        val name: String
    )
}
