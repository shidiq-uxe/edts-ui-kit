package id.co.edtslib.edtsuikit.tmh

import android.content.Context
import android.os.Bundle
import id.co.edtslib.edtsuikit.R
import id.co.edtslib.edtsuikit.demo.promo.DemoProduct
import id.co.edtslib.edtsuikit.demo.promo.GiftOffer
import id.co.edtslib.edtsuikit.demo.promo.PromoAvailability
import id.co.edtslib.edtsuikit.demo.promo.PromoDemoState
import id.co.edtslib.edtsuikit.demo.promo.RewardRule
import kotlin.math.ceil

/** One immutable shopping snapshot, passed between TMH and PPP and saved with each Activity. */
internal data class TmhFlowState(
    val scenario: TmhScenario,
    val templates: List<TmhFair>,
    val campaigns: Map<String, PromoDemoState>,
) {

    val fairs: List<TmhFair>
        get() = templates.map { template ->
            val model = campaigns.getValue(template.id)
            val maximum = model.earnedGifts
            val quantities = model.claimedGifts.groupingBy { it }.eachCount()
            template.copy(
                savedQuantities = quantities,
                redemptionMaximum = maximum,
                remainingSpend = model.remainingSpend,
                spendThreshold = model.threshold,
                state = when {
                    template.state.isUnavailable -> template.state
                    maximum == 0 -> TmhFairState.REQUIREMENT_UNMET
                    quantities.values.sum() == maximum -> TmhFairState.FULLY_SELECTED
                    quantities.isNotEmpty() -> TmhFairState.PARTIALLY_SELECTED
                    else -> TmhFairState.ELIGIBLE
                },
            )
        }

    val cartCount: Int
        get() = TmhFixtures.BASE_CART_QUANTITY + campaigns.values.sumOf { it.cartCount }

    val cartTotal: Long
        get() = BACKGROUND_CART_TOTAL + campaigns.values.sumOf { it.cartTotal }

    fun fair(id: String): TmhFair = fairs.first { it.id == id }

    fun modelFor(id: String): PromoDemoState {
        val model = campaigns.getValue(id)
        return model.copy(
            otherCartCount = cartCount - model.cartCount,
            otherCartTotal = cartTotal - model.cartTotal,
        )
    }

    fun withModel(id: String, model: PromoDemoState): TmhFlowState = copy(
        campaigns = campaigns + (id to model.copy(otherCartCount = 0, otherCartTotal = 0)),
    )

    fun withSelection(id: String, quantities: Map<String, Int>): TmhFlowState {
        val model = campaigns.getValue(id)
        val claims = model.giftOffers.filterNot { it.isRestocking }.flatMap { offer ->
            List((quantities[offer.id] ?: 0).coerceIn(0, MAXIMUM_REDEMPTIONS)) { offer.id }
        }
        return withModel(id, model.withClaimedGifts(claims))
    }

    fun toBundle(): Bundle = Bundle().apply {
        putString(KEY_SCENARIO, scenario.name)
        campaigns.forEach { (id, model) ->
            putBundle(id, Bundle().apply {
                putBundle(KEY_PRODUCTS, model.products.associate { it.id to it.quantity }.toQuantityBundle())
                putStringArrayList(KEY_CLAIMS, ArrayList(model.claimedGifts))
            })
        }
    }

    companion object {
        const val EXTRA_FLOW = "tmh_flow"
        const val EXTRA_FAIR_ID = "tmh_fair_id"
        const val THRESHOLD = 100_000L
        const val MAXIMUM_REDEMPTIONS = 2
        private const val BACKGROUND_CART_TOTAL = 110_000L
        private const val KEY_SCENARIO = "scenario"
        private const val KEY_PRODUCTS = "products"
        private const val KEY_CLAIMS = "claims"

        fun create(context: Context, scenario: TmhScenario): TmhFlowState {
            val templates = TmhFixtures.fairs(scenario)
            val campaigns = templates.associate { fair ->
                val products = TmhCatalogFixtures.products(context, fair).map {
                    if (fair.state == TmhFairState.OUT_OF_STOCK) it.copy(stock = 0) else it
                }
                val first = products.firstOrNull { it.stock > 0 }
                // Qualifying items are explicit fixtures; benefit items never earn their own allowance.
                val seedQuantity = if (fair.state.isEligible && first != null) {
                    ceil(fair.spendThreshold * MAXIMUM_REDEMPTIONS.toDouble() / first.price).toInt()
                } else 0
                fair.id to PromoDemoState(
                    products = products.map { if (it.id == first?.id) it.copy(quantity = seedQuantity) else it },
                    rule = RewardRule.REPEATABLE,
                    availability = when (fair.state) {
                        TmhFairState.QUOTA_EXHAUSTED -> PromoAvailability.QUOTA_EXHAUSTED
                        TmhFairState.LIMIT_EXHAUSTED -> PromoAvailability.LIMIT_REACHED
                        TmhFairState.FIRST_TRANSACTION_ONLY -> PromoAvailability.NEW_CUSTOMER_ONLY
                        else -> PromoAvailability.ACTIVE
                    },
                    claimedGifts = fair.savedQuantities.flatMap { (id, quantity) -> List(quantity) { id } },
                    threshold = fair.spendThreshold,
                    giftOffers = fair.products.map { product ->
                        GiftOffer(
                            id = product.id,
                            name = context.getString(product.nameRes),
                            image = product.imageRes,
                            originalPrice = context.getString(product.originalPriceRes).rupiahAmount(),
                            price = context.getString(product.priceRes).rupiahAmount(),
                            isRestocking = !product.isInStock,
                        )
                    },
                    fairName = context.getString(fair.nameRes),
                )
            }
            return TmhFlowState(scenario, templates, campaigns)
        }

        fun restore(context: Context, bundle: Bundle): TmhFlowState {
            val scenario = TmhScenario.valueOf(bundle.getString(KEY_SCENARIO) ?: TmhScenario.DEFAULT.name)
            val initial = create(context, scenario)
            return initial.copy(campaigns = initial.campaigns.mapValues { (id, model) ->
                val saved = bundle.getBundle(id) ?: return@mapValues model
                val quantities = saved.getBundle(KEY_PRODUCTS)?.quantities().orEmpty()
                val updated = model.copy(products = model.products.map { product ->
                    product.copy(quantity = (quantities[product.id] ?: 0).coerceIn(0, product.stock))
                })
                updated.withClaimedGifts(saved.getStringArrayList(KEY_CLAIMS).orEmpty()
                    .filter { claim -> updated.giftOffers.any { it.id == claim && !it.isRestocking } })
            })
        }
    }
}

/** Campaign-specific local catalog fixtures. No production price/catalog API is implied. */
internal object TmhCatalogFixtures {

    fun products(context: Context, fair: TmhFair): List<DemoProduct> {
        if (fair.nameRes == R.string.tmh_paragon) {
            val redemptionNames = fair.products.map { context.getString(it.nameRes) }.toSet()
            return paragonProducts(context).filterNot { it.name in redemptionNames }
        }
        // Local qualifying groceries are separate from the campaign's benefit catalog.
        // Rotate their order per campaign so restored seed quantities remain deterministic.
        val groceries = listOf(
            DemoProduct("aceh", context.getString(R.string.tmh_catalog_aceh), R.drawable.promo_product_aceh,
                price = 10_000, originalPrice = 20_000, promotion = "", benefit = "+99 Poin", highlight = ""),
            DemoProduct("special", context.getString(R.string.tmh_catalog_special), R.drawable.promo_product_mi_goreng,
                price = 10_000, isDiscounted = false, promotion = "Banyak Lebih Hemat",
                benefit = "Beli 3, +10 Stamp", highlight = "Paket Bundling"),
            DemoProduct("ayam", context.getString(R.string.tmh_catalog_ayam), R.drawable.promo_product_ayam,
                price = 10_000, originalPrice = 20_000, promotion = "", benefit = "", highlight = ""),
            DemoProduct("jumbo", context.getString(R.string.tmh_catalog_jumbo), R.drawable.promo_product_jumbo,
                price = 10_000, isDiscounted = false, promotion = "Paket Bundling",
                benefit = "Beli 3, +10 Stamp", highlight = "Banyak Lebih Hemat"),
        )
        val offset = fair.nameRes % groceries.size
        return (groceries.drop(offset) + groceries.take(offset)).map {
            it.copy(id = "${fair.id}-catalog-${it.id}")
        }
    }

    fun paragonProducts(context: Context): List<DemoProduct> = listOf(
        DemoProduct("wash", context.getString(R.string.tmh_catalog_emina_wash), R.drawable.fair_emina_wash),
        DemoProduct(
            id = "gel",
            name = context.getString(R.string.tmh_emina),
            image = R.drawable.fair_emina_gel,
            isDiscounted = false,
            promotion = "Banyak Lebih Hemat",
            benefit = "+2 Stamp",
        ),
        DemoProduct("cooling", context.getString(R.string.tmh_catalog_kahf_cooling), R.drawable.fair_kahf_cooling),
        DemoProduct("dry", context.getString(R.string.tmh_catalog_kahf_dry), R.drawable.fair_kahf_dry,
            promotion = "Banyak Lebih Hemat"),
        DemoProduct("dry-second", context.getString(R.string.tmh_catalog_kahf_dry), R.drawable.fair_kahf_dry),
        DemoProduct("cooling-second", context.getString(R.string.tmh_catalog_kahf_cooling), R.drawable.fair_kahf_cooling),
        DemoProduct("wash-second", context.getString(R.string.tmh_catalog_emina_wash), R.drawable.fair_emina_wash),
        DemoProduct(
            id = "gel-second",
            name = context.getString(R.string.tmh_emina),
            image = R.drawable.fair_emina_gel,
            isDiscounted = false,
            promotion = "Banyak Lebih Hemat",
            benefit = "+2 Stamp",
        ),
    )
}
