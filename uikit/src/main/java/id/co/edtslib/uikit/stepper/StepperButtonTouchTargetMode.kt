package id.co.edtslib.uikit.stepper

/** Target sizes never change the 24dp icons or the 90 × 32dp expanded pill. */
enum class StepperButtonTouchTargetMode {
    /** Use available space inside the existing host, protecting the count editor. */
    COMPACT,

    /** Reserve a larger wrap-content host for 48dp targets. Exact parent constraints still win. */
    EXPANDED,
}
