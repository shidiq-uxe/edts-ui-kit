package id.co.edtslib.edtsuikit.tmh

import id.co.edtslib.uikit.stepper.StepperDragRepeatConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StepperDragRepeatConfigTest {

    @Test
    fun stationaryAndDeadZonePauseInBothDirections() {
        val config = StepperDragRepeatConfig()
        for (distance in listOf(-12f, -1f, 0f, 1f, 12f)) assertNull(config.repeatIntervalMillis(distance))
    }

    @Test
    fun fartherDisplacementAcceleratesSymmetricallyAndCapsSpeed() {
        val config = StepperDragRepeatConfig()
        assertEquals(200L, config.repeatIntervalMillis(60f))
        assertEquals(125L, config.repeatIntervalMillis(108f))
        assertEquals(125L, config.repeatIntervalMillis(1000f))
        assertEquals(config.repeatIntervalMillis(60f), config.repeatIntervalMillis(-60f))
        assertTrue(requireNotNull(config.repeatIntervalMillis(13f)) > requireNotNull(config.repeatIntervalMillis(60f)))
    }

    @Test
    fun configurableDistancesAndRatesDoNotAcceptNonFiniteInput() {
        val config = StepperDragRepeatConfig(4f, 20f, 1f, 5f)
        assertNull(config.repeatIntervalMillis(4f))
        assertEquals(333L, config.repeatIntervalMillis(12f))
        assertEquals(200L, config.repeatIntervalMillis(20f))
        assertNull(config.repeatIntervalMillis(Float.NaN))
        assertNull(config.repeatIntervalMillis(Float.POSITIVE_INFINITY))
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidDistanceRangeIsRejected() {
        StepperDragRepeatConfig(deadZoneDp = 12f, maximumDistanceDp = 12f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidSpeedRangeIsRejected() {
        StepperDragRepeatConfig(minimumChangesPerSecond = 4f, maximumChangesPerSecond = 2f)
    }
}
