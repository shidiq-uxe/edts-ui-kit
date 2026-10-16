package id.co.edtslib.edtsuikit.tmh

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import id.co.edtslib.edtsuikit.R

enum class TmhFairState(@StringRes val labelRes: Int) {
    ELIGIBLE(R.string.tmh_scenario_eligible),
    PARTIALLY_SELECTED(R.string.tmh_scenario_partial),
    REQUIREMENT_UNMET(R.string.tmh_scenario_unmet),
    FULLY_SELECTED(R.string.tmh_scenario_full),
    OUT_OF_STOCK(R.string.tmh_scenario_stock),
    QUOTA_EXHAUSTED(R.string.tmh_scenario_quota),
    LIMIT_EXHAUSTED(R.string.tmh_scenario_limit),
    FIRST_TRANSACTION_ONLY(R.string.tmh_scenario_first_transaction);

    val isEligible: Boolean
        get() = this == ELIGIBLE || this == PARTIALLY_SELECTED || this == FULLY_SELECTED

    val isUnavailable: Boolean
        get() = this == OUT_OF_STOCK || this == QUOTA_EXHAUSTED ||
            this == LIMIT_EXHAUSTED || this == FIRST_TRANSACTION_ONLY
}

enum class TmhScenario(@StringRes val labelRes: Int, val fairState: TmhFairState? = null) {
    DEFAULT(R.string.tmh_scenario_default),
    ELIGIBLE(R.string.tmh_scenario_eligible, TmhFairState.ELIGIBLE),
    PARTIALLY_SELECTED(R.string.tmh_scenario_partial, TmhFairState.PARTIALLY_SELECTED),
    REQUIREMENT_UNMET(R.string.tmh_scenario_unmet, TmhFairState.REQUIREMENT_UNMET),
    FULLY_SELECTED(R.string.tmh_scenario_full, TmhFairState.FULLY_SELECTED),
    OUT_OF_STOCK(R.string.tmh_scenario_stock, TmhFairState.OUT_OF_STOCK),
    QUOTA_EXHAUSTED(R.string.tmh_scenario_quota, TmhFairState.QUOTA_EXHAUSTED),
    LIMIT_EXHAUSTED(R.string.tmh_scenario_limit, TmhFairState.LIMIT_EXHAUSTED),
    FIRST_TRANSACTION_ONLY(R.string.tmh_scenario_first_transaction, TmhFairState.FIRST_TRANSACTION_ONLY),
    EMPTY(R.string.tmh_scenario_empty),
    LOADING(R.string.tmh_scenario_loading),
    ERROR(R.string.tmh_scenario_error);
}

data class TmhProduct(
    val id: String,
    @StringRes val nameRes: Int,
    @DrawableRes val imageRes: Int,
    @StringRes val priceRes: Int = R.string.tmh_price_5000,
    @StringRes val originalPriceRes: Int = R.string.tmh_price_10000,
    val isInStock: Boolean = true,
)

data class TmhFair(
    val id: String,
    @StringRes val nameRes: Int,
    @DrawableRes val bannerRes: Int,
    val products: List<TmhProduct>,
    val state: TmhFairState,
    val savedQuantities: Map<String, Int> = emptyMap(),
    val redemptionMaximum: Int = 2,
    val remainingSpend: Long = 100_000L,
) {

    val savedTotal: Int
        get() = savedQuantities.values.sum()

    val maximum: Int
        get() = if (state.isEligible) redemptionMaximum else 0

    fun selection(): RedemptionSelection = RedemptionSelection(
        originalQuantities = savedQuantities,
        availableProductIds = products.filter { it.isInStock }.map { it.id }.toSet(),
        maximum = maximum,
        isEligible = state.isEligible,
    )

    fun withSelection(quantities: Map<String, Int>): TmhFair = copy(
        savedQuantities = quantities.toMap(),
        state = when (quantities.values.sum()) {
            0 -> TmhFairState.ELIGIBLE
            in 1 until maximum -> TmhFairState.PARTIALLY_SELECTED
            else -> TmhFairState.FULLY_SELECTED
        },
    )
}

object TmhFixtures {

    const val BASE_CART_QUANTITY = 11

    fun fairs(scenario: TmhScenario): List<TmhFair> {
        val state = scenario.fairState ?: return defaultFairs()
        return List(3) { index ->
            val products = products(index).map {
                if (state == TmhFairState.OUT_OF_STOCK) it.copy(isInStock = false) else it
            }
            fair(index, index, state, products)
        }
    }

    private fun defaultFairs(): List<TmhFair> = listOf(
        fair(0, 1, TmhFairState.PARTIALLY_SELECTED, products(1)),
        fair(1, 2, TmhFairState.FULLY_SELECTED, products(2)).let { fair ->
            fair.copy(savedQuantities = fair.products.associate { it.id to 1 })
        },
        fair(2, 0, TmhFairState.ELIGIBLE, products(0).let { products ->
            listOf(products[1], products[2].copy(isInStock = true), products[0])
        }),
        fair(3, 3, TmhFairState.REQUIREMENT_UNMET, listOf(
            TmhProduct(
                id = "softex-gravity",
                nameRes = R.string.tmh_softex_gravity,
                imageRes = R.drawable.tmh_product_softex_gravity,
                priceRes = R.string.tmh_price_15000,
                originalPriceRes = R.string.tmh_price_30000,
            ),
            TmhProduct(
                id = "softex-beary",
                nameRes = R.string.tmh_softex_beary,
                imageRes = R.drawable.tmh_product_softex_beary,
                priceRes = R.string.tmh_price_15000,
                originalPriceRes = R.string.tmh_price_30000,
            ),
        )),
        fair(4, 4, TmhFairState.OUT_OF_STOCK, listOf(
            TmhProduct(
                id = "tissue",
                nameRes = R.string.tmh_tissue_product,
                imageRes = R.drawable.tmh_product_tissue,
                priceRes = R.string.tmh_price_11000,
                originalPriceRes = R.string.tmh_price_22000,
                isInStock = false,
            ),
        )),
        fair(5, 5, TmhFairState.QUOTA_EXHAUSTED, listOf(
            TmhProduct(
                id = "vanish",
                nameRes = R.string.tmh_vanish_product,
                imageRes = R.drawable.tmh_product_vanish,
                priceRes = R.string.tmh_price_15000,
                originalPriceRes = R.string.tmh_price_30000,
            ),
        )),
        fair(6, 6, TmhFairState.LIMIT_EXHAUSTED, listOf(
            TmhProduct(
                id = "five-days-banana",
                nameRes = R.string.tmh_banana_product,
                imageRes = R.drawable.tmh_product_five_days_banana,
                priceRes = R.string.tmh_price_3500,
                originalPriceRes = R.string.tmh_price_7000,
            ),
            TmhProduct(
                id = "five-days-cheese",
                nameRes = R.string.tmh_cheese_product,
                imageRes = R.drawable.tmh_product_five_days_cheese,
                priceRes = R.string.tmh_price_3500,
                originalPriceRes = R.string.tmh_price_7000,
            ),
        )),
        fair(7, 7, TmhFairState.FIRST_TRANSACTION_ONLY, listOf(
            TmhProduct(
                id = "chupa-mix",
                nameRes = R.string.tmh_mix_product,
                imageRes = R.drawable.tmh_product_chupa_mix,
                priceRes = R.string.tmh_price_7500,
                originalPriceRes = R.string.tmh_price_15000,
            ),
            TmhProduct(
                id = "chupa-ocean",
                nameRes = R.string.tmh_ocean_product,
                imageRes = R.drawable.tmh_product_chupa_ocean,
                priceRes = R.string.tmh_price_7500,
                originalPriceRes = R.string.tmh_price_15000,
            ),
        )),
    )

    private fun fair(index: Int, campaign: Int, state: TmhFairState, products: List<TmhProduct>): TmhFair {
        val quantity = when (state) {
            TmhFairState.PARTIALLY_SELECTED -> 1
            TmhFairState.FULLY_SELECTED -> 2
            else -> 0
        }
        val names = listOf(
            R.string.tmh_paragon,
            R.string.tmh_toples_fair,
            R.string.tmh_luminarc,
            R.string.tmh_softex,
            R.string.tmh_tissue,
            R.string.tmh_vanish,
            R.string.tmh_five_days,
            R.string.tmh_chupa,
        )
        val banners = listOf(
            R.drawable.tmh_banner_paragon,
            R.drawable.tmh_banner_toples,
            R.drawable.tmh_banner_luminarc,
            R.drawable.tmh_banner_softex,
            R.drawable.tmh_banner_tissue,
            R.drawable.tmh_banner_vanish,
            R.drawable.tmh_banner_five_days,
            R.drawable.tmh_banner_chupa,
        )
        return TmhFair(
            id = "fair-$index",
            nameRes = names[campaign],
            bannerRes = banners[campaign],
            products = products,
            state = state,
            savedQuantities = if (quantity == 0) emptyMap() else mapOf(products.first().id to quantity),
        )
    }

    private fun products(campaign: Int): List<TmhProduct> = when (campaign) {
        0 -> listOf(
            TmhProduct("emina", R.string.tmh_emina, R.drawable.tmh_product_emina),
            TmhProduct("kahf", R.string.tmh_kahf, R.drawable.tmh_product_kahf),
            TmhProduct("wardah", R.string.tmh_wardah, R.drawable.tmh_product_wardah, isInStock = false),
        )
        1 -> listOf(
            TmhProduct(
                id = "toples-1000",
                nameRes = R.string.tmh_toples_1000,
                imageRes = R.drawable.tmh_product_toples_1000,
                priceRes = R.string.tmh_price_15000,
                originalPriceRes = R.string.tmh_price_30000,
            ),
            TmhProduct(
                id = "toples-750",
                nameRes = R.string.tmh_toples_750,
                imageRes = R.drawable.tmh_product_toples_750,
                priceRes = R.string.tmh_price_15000,
                originalPriceRes = R.string.tmh_price_30000,
            ),
        )
        else -> listOf(
            TmhProduct("soup", R.string.tmh_soup, R.drawable.tmh_product_soup_bowl),
            TmhProduct("noodle", R.string.tmh_noodle, R.drawable.tmh_product_noodle_bowl),
        )
    }
}
