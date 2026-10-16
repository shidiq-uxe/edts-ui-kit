package id.co.edtslib.edtsuikit.tmh

import id.co.edtslib.edtsuikit.demo.promo.DemoProduct
import id.co.edtslib.edtsuikit.demo.promo.GiftOffer
import id.co.edtslib.edtsuikit.demo.promo.PromoAvailability
import id.co.edtslib.edtsuikit.demo.promo.PromoDemoState
import id.co.edtslib.edtsuikit.demo.promo.RewardRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TmhFlowStateTest {

    private fun flow(quantity: Int = 4, claims: List<String> = emptyList()): TmhFlowState {
        val fair = TmhFair("fair", 0, 0, listOf(TmhProduct("gift", 0, 0)), TmhFairState.ELIGIBLE)
        val model = PromoDemoState(
            products = listOf(DemoProduct("qualifying", "Product", 0, quantity = quantity)),
            rule = RewardRule.REPEATABLE,
            claimedGifts = claims,
            giftOffers = listOf(GiftOffer("gift", "Benefit", 0)),
        )
        return TmhFlowState(TmhScenario.DEFAULT, listOf(fair), mapOf(fair.id to model))
    }

    @Test
    fun allowanceUsesCompletedHundredThousandIntervalsAndCapsAtTwo() {
        for ((quantity, expected) in listOf(0 to 0, 1 to 0, 2 to 1, 3 to 1, 4 to 2, 99 to 2)) {
            val state = flow(quantity)
            assertEquals(expected, state.fair("fair").maximum)
        }
    }

    @Test
    fun oneEarnedAndSelectedIsFullySelectedWithOneAsTheCounterDenominator() {
        val state = flow(2).withSelection("fair", mapOf("gift" to 1))
        assertEquals(1, state.fair("fair").maximum)
        assertEquals(TmhFairState.FULLY_SELECTED, state.fair("fair").state)
        assertFalse(state.fair("fair").selection().canIncrement("gift"))
        assertTrue(state.fair("fair").selection().canDecrement("gift"))
    }

    @Test
    fun selectionDoesNotQualifyTowardAnotherRedemption() {
        val initial = flow(3)
        val selected = initial.withSelection("fair", mapOf("gift" to 2))
        assertEquals(1, selected.fair("fair").savedTotal)
        assertEquals(initial.campaigns.getValue("fair").qualifyingSubtotal,
            selected.campaigns.getValue("fair").qualifyingSubtotal)
    }

    @Test
    fun droppingBelowThresholdRemovesExcessBenefitsAndReturnsToUnmet() {
        val state = flow(4, listOf("gift", "gift"))
        val one = state.withModel("fair", state.modelFor("fair").withQuantity("qualifying", 3))
        assertEquals(1, one.fair("fair").savedTotal)
        val none = one.withModel("fair", one.modelFor("fair").withQuantity("qualifying", 1))
        assertEquals(0, none.fair("fair").savedTotal)
        assertEquals(TmhFairState.REQUIREMENT_UNMET, none.fair("fair").state)
        assertEquals(50_000L, none.fair("fair").remainingSpend)
    }

    @Test
    fun cartIncludesAllCampaignsWithoutDuplicatingOtherCartOnReturn() {
        val first = flow(2, listOf("gift"))
        val second = first.templates.single().copy(id = "second")
        val combined = first.copy(templates = first.templates + second,
            campaigns = first.campaigns + ("second" to first.campaigns.getValue("fair")))
        val returned = combined.withModel("fair", combined.modelFor("fair"))
        assertEquals(combined.cartCount, returned.cartCount)
        assertEquals(combined.cartTotal, returned.cartTotal)
        assertEquals(combined.cartCount, combined.modelFor("fair").cartCount)
        assertEquals(combined.cartCount, combined.modelFor("second").cartCount)
    }

    @Test
    fun unavailableBenefitsAndUnknownIdsCannotBeSaved() {
        val initial = flow()
        val blocked = initial.copy(campaigns = initial.campaigns.mapValues { (_, model) ->
            model.copy(giftOffers = model.giftOffers.map { it.copy(isRestocking = true) })
        })
        assertEquals(0, blocked.withSelection("fair", mapOf("gift" to 1, "unknown" to 1)).fair("fair").savedTotal)
    }

    @Test
    fun restrictedFairKeepsItsStateAndCannotEarnBenefits() {
        val initial = flow()
        for (availability in listOf(PromoAvailability.QUOTA_EXHAUSTED, PromoAvailability.LIMIT_REACHED,
            PromoAvailability.NEW_CUSTOMER_ONLY)) {
            val template = initial.templates.single().copy(state = TmhFairState.QUOTA_EXHAUSTED)
            val blocked = initial.copy(templates = listOf(template), campaigns = initial.campaigns.mapValues { (_, model) ->
                model.copy(availability = availability)
            })
            assertEquals(TmhFairState.QUOTA_EXHAUSTED, blocked.fair("fair").state)
            assertEquals(0, blocked.fair("fair").maximum)
        }
    }
}
