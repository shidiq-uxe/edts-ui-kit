package id.co.edtslib.uikit.stepper

import kotlin.math.abs
import kotlin.math.roundToLong

/** Distances are dp; speed is accepted quantity changes per second, independent of animation scale. */
data class StepperDragRepeatConfig(
    val deadZoneDp: Float = 12f,
    val maximumDistanceDp: Float = 108f,
    val minimumChangesPerSecond: Float = 2f,
    val maximumChangesPerSecond: Float = 8f,
) {

    init {
        require(deadZoneDp.isFinite() && deadZoneDp >= 0f)
        require(maximumDistanceDp.isFinite() && maximumDistanceDp > deadZoneDp)
        require(minimumChangesPerSecond.isFinite() && minimumChangesPerSecond > 0f)
        require(maximumChangesPerSecond.isFinite() && maximumChangesPerSecond >= minimumChangesPerSecond)
    }

    /** Null means paused. The caller determines whether this displacement permits increases. */
    fun repeatIntervalMillis(displacementDp: Float): Long? {
        val distance = abs(displacementDp)
        if (!distance.isFinite() || distance <= deadZoneDp) return null
        val progress = ((distance - deadZoneDp) / (maximumDistanceDp - deadZoneDp)).coerceIn(0f, 1f)
        val speed = minimumChangesPerSecond + (maximumChangesPerSecond - minimumChangesPerSecond) * progress
        return (1000.0 / speed).roundToLong().coerceAtLeast(1L)
    }
}
