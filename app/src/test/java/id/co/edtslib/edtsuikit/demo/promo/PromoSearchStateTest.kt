package id.co.edtslib.edtsuikit.demo.promo

import org.junit.Assert.*
import org.junit.Test

class PromoSearchStateTest {
    private val products = listOf(
        DemoProduct("emina", "Emina Face Wash", 0, quantity = 1),
        DemoProduct("cooling", "Kahf Cooling Power", 0),
        DemoProduct("dry", "Kahf Extra Dry Power", 0),
        DemoProduct("duplicate", "Kahf Cooling Power", 0)
    )

    @Test fun searchFindsTwoCatalogProductsWithoutDroppingUnmatchedCartItem() {
        val cart = PromoDemoState(products)
        val matches = PromoSearchState.matching(cart.products, "  kAhF  ")
        assertEquals(listOf("cooling", "dry"), matches.map { it.id })
        assertEquals(50_000L, cart.cartTotal)
        val added = cart.withQuantity(matches.first().id, 1)
        assertEquals(100_000L, added.cartTotal)
        assertEquals(1, added.earnedGifts)
        assertEquals(2, added.cartCount)
    }

    @Test fun staleCompletionCannotReplaceNewQueryOrReopenedEditor() {
        val first = PromoSearchState().edit("Kurma").submit()
        val second = first.edit("Kahf").submit()
        assertEquals(second, second.complete(first.generation, 0, false))
        val editor = second.edit()
        assertEquals(editor, editor.complete(second.generation, 2, false))
    }

    @Test fun clearingDraftKeepsSubmittedQueryForBackNavigation() {
        val results = PromoSearchState().edit("Kahf").submit().let { it.complete(it.generation, 2, false) }
        val cleared = results.edit("")
        assertEquals("", cleared.draft)
        assertEquals("Kahf", cleared.submitted)
        assertTrue(cleared.hasSubmission)
        assertEquals(cleared, cleared.submit())
    }

    @Test fun noResultsAndConnectionFailureAreDistinctAndRetryCanSucceed() {
        val pending = PromoSearchState().edit("Kahf").submit()
        val failure = pending.fail(pending.generation)
        assertEquals(SearchMode.ERROR, failure.mode)
        val retry = failure.submit()
        assertEquals(SearchMode.RESULTS, retry.complete(retry.generation, 2, false).mode)
        assertEquals(SearchMode.EMPTY, pending.complete(pending.generation, 0, false).mode)
    }

    @Test fun websiteBannerIsExplicitFixturePolicyAndRequiresEmptyResults() {
        assertFalse(PromoSearchState.websiteFallback("Kurma"))
        assertTrue(PromoSearchState.websiteFallback(" Rokok "))
        val pending = PromoSearchState().edit("Rokok").submit()
        assertTrue(pending.complete(pending.generation, 0, true).fallbackAvailable)
        assertFalse(pending.complete(pending.generation, 1, true).fallbackAvailable)
    }

    @Test fun repeatableRedemptionKeepsAllItemSpendAcrossQueryChanges() {
        val cart = PromoDemoState(products.take(3), rule = RewardRule.REPEATABLE, threshold = 50_000L)
        val initial = cart.claimGift("offer")
        assertEquals(1, initial.earnedGifts)
        val added = initial.withQuantity("cooling", 1)
        assertEquals(2, added.earnedGifts)
        assertEquals(1, added.unclaimedGifts)
        assertEquals(emptyList<DemoProduct>(), PromoSearchState.matching(added.products, "Kurma"))
        assertEquals(100_000L, added.qualifyingSubtotal)
    }
}
