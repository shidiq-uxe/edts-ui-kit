package id.co.edtslib.uikit.utils.hapticfeedback

// Enum class for shared haptic feedback rules
enum class HapticFeedback(val duration: Long) {
    ERROR(200L),
    SUCCESS(100L),
    WARNING(300L),
    SELECTION(50L),
    LONG_PRESS(400L)
}