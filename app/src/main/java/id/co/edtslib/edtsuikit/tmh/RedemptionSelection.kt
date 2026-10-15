package id.co.edtslib.edtsuikit.tmh

/** Keeps pending quantities separate from the cart until the customer saves. */
class RedemptionSelection(
    originalQuantities: Map<String, Int>,
    private val availableProductIds: Set<String>,
    val maximum: Int,
    val isEligible: Boolean,
) {

    val quantities: Map<String, Int>
        get() = pending.toMap()

    val total: Int
        get() = pending.values.sum()

    val hasChanges: Boolean
        get() = pending != original

    val canSave: Boolean
        get() = isEligible && hasChanges

    val action: Action
        get() = when {
            original.isEmpty() -> Action.ADD
            total == 0 -> Action.REMOVE
            else -> Action.UPDATE
        }

    private val original = originalQuantities.filterValues { it > 0 }.toMap()
    private val pending = original.toMutableMap()

    fun quantity(productId: String): Int = pending[productId] ?: 0

    fun canIncrement(productId: String): Boolean =
        isEligible && productId in availableProductIds && total < maximum

    fun canDecrement(productId: String): Boolean =
        isEligible && productId in availableProductIds && quantity(productId) > 0

    fun update(productId: String, quantity: Int): Boolean {
        if (!isEligible || productId !in availableProductIds || quantity < 0) return false
        if (total - quantity(productId) + quantity > maximum) return false

        if (quantity == 0) pending.remove(productId) else pending[productId] = quantity
        return true
    }

    enum class Action { ADD, UPDATE, REMOVE }
}
