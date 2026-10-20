package id.co.edtslib.edtsuikit.tmh

import org.junit.Assert.assertEquals
import org.junit.Test

class TmhPricingTest {

    @Test
    fun discountsRoundToTheDesignValuesRatherThanAlwaysRoundingUp() {
        for ((price, original, expected) in listOf(
            Triple(15_000L, 30_000L, 50),
            Triple(35_000L, 45_000L, 22),
            Triple(45_000L, 67_000L, 33),
            Triple(9_900L, 19_800L, 50),
            Triple(16_000L, 22_000L, 27),
            Triple(21_900L, 29_900L, 27),
            Triple(4_900L, 9_800L, 50),
            Triple(10_000L, 20_000L, 50),
        )) {
            assertEquals("$original → $price", expected, redemptionDiscountPercent(price, original))
        }
    }

    @Test
    fun zeroAndInvalidOriginalPricesDoNotProduceInvalidDiscounts() {
        assertEquals(100, redemptionDiscountPercent(0, 20_000))
        assertEquals(0, redemptionDiscountPercent(20_000, 20_000))
        assertEquals(0, redemptionDiscountPercent(30_000, 20_000))
        assertEquals(0, redemptionDiscountPercent(5_000, 0))
    }
}
