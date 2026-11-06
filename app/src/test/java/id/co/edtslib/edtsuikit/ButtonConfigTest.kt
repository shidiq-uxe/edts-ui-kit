package id.co.edtslib.edtsuikit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ButtonConfigTest {

    @Test
    fun allFilterShowsEachProductsExamples() {
        val config = ButtonConfig()
        assertEquals(3, config.examples(ButtonProduct.CORE).size)
        assertEquals(18, config.examples(ButtonProduct.KLIK).size)
        assertEquals(21, config.examples(ButtonProduct.POINKU).size)
        assertNull(config.cornerRadius)
        assertNull(config.tintColor)
    }

    @Test
    fun sizeFilterIncludesMatchingTextAndIconRecipes() {
        val small = ButtonConfig(sizeFilter = SizeFilter.SMALL)
        val medium = ButtonConfig(sizeFilter = SizeFilter.MEDIUM)
        val large = ButtonConfig(sizeFilter = SizeFilter.LARGE)
        assertEquals(6, small.examples(ButtonProduct.KLIK).size)
        assertEquals(6, medium.examples(ButtonProduct.KLIK).size)
        assertEquals(6, large.examples(ButtonProduct.KLIK).size)
        assertEquals(9, small.examples(ButtonProduct.POINKU).size)
        assertEquals(3, medium.examples(ButtonProduct.POINKU).size)
        assertEquals(9, large.examples(ButtonProduct.POINKU).size)
        assertEquals(3, small.examples(ButtonProduct.CORE).size)
    }

    @Test
    fun defaultPaletteContainsAllDefinedColors() {
        assertEquals(8, ButtonConfig.defaultColors.size)
    }
}
