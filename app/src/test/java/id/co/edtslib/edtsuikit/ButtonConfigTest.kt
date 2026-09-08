package id.co.edtslib.edtsuikit

import org.junit.Assert.assertEquals
import org.junit.Test

class ButtonConfigTest {

    @Test
    fun allFilterShowsEveryButtonVariant() {
        assertEquals(12, ButtonConfig().visibleVariants.size)
    }

    @Test
    fun sizeFilterShowsOneVariantPerButtonSection() {
        assertEquals(4, ButtonConfig(sizeFilter = SizeFilter.SMALL).visibleVariants.size)
        assertEquals(4, ButtonConfig(sizeFilter = SizeFilter.MEDIUM).visibleVariants.size)
        assertEquals(4, ButtonConfig(sizeFilter = SizeFilter.LARGE).visibleVariants.size)
    }

    @Test
    fun defaultPaletteContainsAllDefinedColors() {
        assertEquals(8, ButtonConfig.defaultColors.size)
    }
}
