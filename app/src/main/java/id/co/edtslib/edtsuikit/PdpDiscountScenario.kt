package id.co.edtslib.edtsuikit

enum class PdpDiscountScenario {
    COLLAPSE_SPACE,
    KEEP_SPACE,
    ANIMATE_SPACE;

    companion object {
        fun fromSavedValue(
            value: String?,
            fallback: PdpDiscountScenario = COLLAPSE_SPACE
        ): PdpDiscountScenario = entries.firstOrNull { it.name == value } ?: fallback
    }
}
