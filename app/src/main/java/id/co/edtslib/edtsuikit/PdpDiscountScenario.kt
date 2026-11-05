package id.co.edtslib.edtsuikit

enum class PdpDiscountScenario {
    COLLAPSE_SPACE,
    KEEP_SPACE,
    ANIMATE_SPACE,
    HIDE_DISCOUNTS_DURING_SPOTLIGHT;

    companion object {
        fun fromSavedValue(
            value: String?,
            fallback: PdpDiscountScenario = COLLAPSE_SPACE
        ): PdpDiscountScenario = when (value) {
            "DEFER_KARTON_DISCOUNT", "REVEAL_KARTON_ON_DISMISS" -> HIDE_DISCOUNTS_DURING_SPOTLIGHT
            else -> entries.firstOrNull { it.name == value } ?: fallback
        }
    }
}
