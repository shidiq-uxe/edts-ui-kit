package id.co.edtslib.edtsuikit.demo.promo

import org.junit.Assert.assertEquals
import org.junit.Test

class PromoDemoStateTest {
    private val product = DemoProduct("gel", "Emina", image = 0)

    @Test fun oneTimeRewardUsesSpendAndSeparatesClaims() {
        val start = PromoDemoState(listOf(product))
        val halfway = start.withQuantity("gel", 1)
        assertEquals(0, halfway.earnedGifts)
        assertEquals(50_000L, halfway.remainingSpend)
        val eligible = halfway.withQuantity("gel", 2)
        assertEquals(1, eligible.unclaimedGifts)
        assertEquals(0, eligible.claimedGifts.size)
        val claimed = eligible.claimGift("gift").withQuantity("gel", 6)
        assertEquals(1, claimed.earnedGifts)
        assertEquals(0, claimed.unclaimedGifts)
        assertEquals(300_000L, claimed.cartTotal)
    }

    @Test fun repeatableRewardsUseCompletedIntervals() {
        val eligible = PromoDemoState(listOf(product), RewardRule.REPEATABLE).withQuantity("gel", 5)
        assertEquals(2, eligible.earnedGifts)
        assertEquals(50_000L, eligible.remainingSpend)
        assertEquals(50f, eligible.progressPercent, 0.01f)
        val claimed = eligible.claimGift("first")
        assertEquals(1, claimed.unclaimedGifts)
        val decreased = claimed.withQuantity("gel", 1)
        assertEquals(emptyList<String>(), decreased.claimedGifts)
        assertEquals(0, decreased.earnedGifts)
    }

    @Test fun unavailablePromoCannotAddOrClaim() {
        val model = PromoDemoState(listOf(product.copy(quantity = 2)), availability = PromoAvailability.QUOTA_EXHAUSTED)
        assertEquals(model, model.withQuantity("gel", 3))
        assertEquals(model, model.claimGift("gift"))
        assertEquals(100_000L, model.cartTotal)
        assertEquals(0, model.earnedGifts)
    }

    @Test fun unrelatedCartSpendDoesNotQualify() {
        val model = PromoDemoState(listOf(product), otherCartCount = 3, otherCartTotal = 999_999)
        assertEquals(3, model.cartCount)
        assertEquals(999_999L, model.cartTotal)
        assertEquals(0L, model.qualifyingSubtotal)
        assertEquals(0, model.earnedGifts)
    }
}
