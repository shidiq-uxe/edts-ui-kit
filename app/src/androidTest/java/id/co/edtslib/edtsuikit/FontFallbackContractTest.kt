package id.co.edtslib.edtsuikit

import android.content.ContextWrapper
import android.content.res.Resources
import android.graphics.Typeface
import android.util.TypedValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import id.co.edtslib.uikit.utils.font
import id.co.edtslib.uikit.utils.typography.FontUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import id.co.edtslib.uikit.R as UIKitR

@RunWith(AndroidJUnit4::class)
class FontFallbackContractTest {

    @Test
    fun unavailableRubikProvidersResolveToBundledWeights() {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val weights = listOf(
            UIKitR.font.rubik to UIKitR.font.rubik_fallback,
            UIKitR.font.rubik_medium to UIKitR.font.rubik_medium_fallback,
            UIKitR.font.rubik_semibold to UIKitR.font.rubik_semibold_fallback,
            UIKitR.font.rubik_bold to UIKitR.font.rubik_bold_fallback
        )
        val providerIds = weights.map { it.first }.toSet()
        val offlineResources = object : Resources(target.assets, target.resources.displayMetrics, target.resources.configuration) {
            override fun getValue(id: Int, outValue: TypedValue, resolveRefs: Boolean) {
                if (id in providerIds) throw NotFoundException("Font provider unavailable")
                super.getValue(id, outValue, resolveRefs)
            }
        }
        val unavailableProvider = object : ContextWrapper(target) {
            override fun getResources(): Resources = offlineResources
        }
        FontUtils.clearCache()
        try {
            for ((provider, fallback) in weights) {
                val expected = target.font(fallback)
                val actual = unavailableProvider.font(provider)
                assertNotEquals(Typeface.DEFAULT, actual)
                assertEquals(expected, actual)
            }
        } finally {
            FontUtils.clearCache()
        }
    }

    @Test
    fun publishedButtonFontNamesResolveToSharedFiles() {
        val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
        for ((alias, shared) in listOf(
            UIKitR.font.button_klik_inter_semibold to UIKitR.font.inter_semibold_fallback,
            UIKitR.font.button_poinku_rubik_medium to UIKitR.font.rubik_medium_fallback
        )) {
            val oldValue = TypedValue()
            val sharedValue = TypedValue()
            resources.getValue(alias, oldValue, true)
            resources.getValue(shared, sharedValue, true)
            assertEquals(sharedValue.string.toString(), oldValue.string.toString())
        }
    }
}
