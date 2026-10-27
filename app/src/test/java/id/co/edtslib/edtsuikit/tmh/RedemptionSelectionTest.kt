package id.co.edtslib.edtsuikit.tmh

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RedemptionSelectionTest {

    @Test
    fun actionTracksAddUpdateAndRemoveWithoutChangingTheOriginalSelection() {
        val fresh = selection()
        assertEquals(RedemptionSelection.Action.ADD, fresh.action)
        fresh.update("emina", 1)
        assertEquals(RedemptionSelection.Action.ADD, fresh.action)
        assertTrue(fresh.canSave)
        fresh.update("emina", 0)
        assertEquals(RedemptionSelection.Action.ADD, fresh.action)
        assertFalse(fresh.canSave)

        val saved = selection(mapOf("emina" to 1))
        assertEquals(RedemptionSelection.Action.UPDATE, saved.action)
        assertFalse(saved.canSave)
        saved.update("emina", 0)
        assertEquals(RedemptionSelection.Action.REMOVE, saved.action)
        assertTrue(saved.canSave)
        saved.update("kahf", 1)
        assertEquals(1, saved.total)
        assertEquals(RedemptionSelection.Action.UPDATE, saved.action)
        assertTrue(saved.canSave)
        saved.update("kahf", 0)
        saved.update("emina", 1)
        assertEquals(RedemptionSelection.Action.UPDATE, saved.action)
        assertFalse(saved.canSave)
    }

    @Test
    fun removingOneOfTwoProductsUpdatesWhileRemovingBothDeletes() {
        val saved = selection(mapOf("emina" to 1, "kahf" to 1))
        saved.update("emina", 0)
        assertEquals(RedemptionSelection.Action.UPDATE, saved.action)
        assertTrue(saved.canSave)
        saved.update("kahf", 0)
        assertEquals(RedemptionSelection.Action.REMOVE, saved.action)
        assertTrue(saved.canSave)
    }

    @Test
    fun freshEmptySelectionDoesNotSaveOrWarn() {
        val selection = selection()
        assertFalse(selection.canSave)
        assertFalse(selection.hasChanges)
        assertTrue(selection.canIncrement("emina"))
        assertFalse(selection.canDecrement("emina"))
    }

    @Test
    fun removalOfSavedProductIsAValidChange() {
        val selection = selection(mapOf("emina" to 1))
        assertTrue(selection.update("emina", 0))
        assertEquals(0, selection.total)
        assertTrue(selection.canSave)
        assertTrue(selection.hasChanges)
    }

    @Test
    fun revertingToOriginalQuantitiesClearsUnsavedChanges() {
        val selection = selection(mapOf("emina" to 1))
        selection.update("emina", 0)
        selection.update("kahf", 1)
        assertTrue(selection.hasChanges)
        selection.update("kahf", 0)
        selection.update("emina", 1)
        assertFalse(selection.hasChanges)
        assertFalse(selection.canSave)
    }

    @Test
    fun entitlementIsSharedAcrossProductsAndMinusRemainsAvailable() {
        val selection = selection()
        assertTrue(selection.update("emina", 1))
        assertTrue(selection.update("kahf", 1))
        assertFalse(selection.canIncrement("emina"))
        assertFalse(selection.canIncrement("kahf"))
        assertTrue(selection.canDecrement("emina"))
        assertFalse(selection.update("kahf", 2))
        assertEquals(mapOf("emina" to 1, "kahf" to 1), selection.quantities)
        assertTrue(selection.update("emina", 0))
        assertTrue(selection.canIncrement("kahf"))
    }

    @Test
    fun unavailableAndUnknownProductsCannotChange() {
        val selection = selection()
        assertFalse(selection.canIncrement("wardah"))
        assertFalse(selection.update("wardah", 1))
        assertFalse(selection.update("unknown", 1))
        assertFalse(selection.update("emina", -1))
        assertFalse(selection.hasChanges)
    }

    @Test
    fun blockedPromotionCannotChangeOrSave() {
        val selection = RedemptionSelection(emptyMap(), setOf("emina"), maximum = 0, isEligible = false)
        assertFalse(selection.canIncrement("emina"))
        assertFalse(selection.update("emina", 1))
        assertFalse(selection.canSave)
    }

    @Test
    fun savedSelectionAndReturnedSnapshotRemainIndependent() {
        val original = mutableMapOf("emina" to 1)
        val selection = selection(original)
        original["emina"] = 2
        assertEquals(1, selection.quantity("emina"))
        val snapshot = selection.quantities
        selection.update("emina", 0)
        assertEquals(mapOf("emina" to 1), snapshot)
        assertTrue(selection.canSave)
    }

    @Test
    fun standaloneRemovalPolicyPreservesLineDeletionAndProductSwap() {
        val selection = RedemptionSelection(
            originalQuantities = mapOf("emina" to 1, "kahf" to 1),
            availableProductIds = setOf("emina", "kahf"),
            maximum = 2,
            isEligible = true,
            shouldRemoveIndividualProducts = true,
        )
        assertFalse(selection.hasChanges)
        selection.update("emina", 0)
        assertEquals(RedemptionSelection.Action.REMOVE, selection.action)
        assertTrue(selection.canSave)
        selection.update("kahf", 2)
        assertEquals(RedemptionSelection.Action.UPDATE, selection.action)
        selection.update("kahf", 1)
        selection.update("emina", 1)
        assertFalse(selection.canSave)
    }

    private fun selection(original: Map<String, Int> = emptyMap()): RedemptionSelection =
        RedemptionSelection(original, setOf("emina", "kahf"), maximum = 2, isEligible = true)
}
