package id.co.edtslib.edtsuikit.demo.promo

internal data class PromoDemoState(
    val products: List<DemoProduct>,
    val rule: RewardRule = RewardRule.ONE_TIME,
    val availability: PromoAvailability = PromoAvailability.ACTIVE,
    val claimedGifts: List<String> = emptyList(),
    val threshold: Long = 100_000L,
    val otherCartCount: Int = 0,
    val otherCartTotal: Long = 0L,
    val giftOffers: List<GiftOffer> = emptyList(),
    val fairName: String = "Paragon Fair"
) {
    init { require(threshold > 0) }

    private val maxLaps: Int get() = if (rule == RewardRule.ONE_TIME) 1 else MAX_REPEAT_LAPS

    val giftLines: List<GiftLine> get() = giftOffers.mapNotNull { offer ->
        val quantity = claimedGifts.count { it == offer.id }
        if (quantity > 0) GiftLine(offer, quantity) else null
    }
    val giftCount: Int get() = giftLines.sumOf { it.quantity }
    val giftTotal: Long get() = giftLines.sumOf { it.offer.price * it.quantity }
    val cartCount: Int get() = otherCartCount + products.sumOf { it.quantity } + giftCount
    val cartTotal: Long get() = otherCartTotal + products.sumOf { it.price * it.quantity } + giftTotal
    val qualifyingSubtotal: Long get() = products.filter { it.qualifies }.sumOf { it.price * it.quantity }
    val earnedGifts: Int get() {
        if (availability != PromoAvailability.ACTIVE) return 0
        return (qualifyingSubtotal / threshold).toInt().coerceAtMost(maxLaps)
    }
    val unclaimedGifts: Int get() = (earnedGifts - claimedGifts.size).coerceAtLeast(0)
    val remainingSpend: Long get() = when {
        earnedGifts >= maxLaps -> 0L
        else -> threshold - qualifyingSubtotal % threshold
    }
    val progressPercent: Float get() = when {
        availability != PromoAvailability.ACTIVE -> 0f
        rule == RewardRule.ONE_TIME -> (qualifyingSubtotal * 100f / threshold).coerceAtMost(100f)
        else -> (qualifyingSubtotal % threshold) * 100f / threshold
    }

    val barLimit: Float get() = if (rule == RewardRule.ONE_TIME) 100f else maxLaps * 100f

    val barProgress: Float get() = when {
        availability != PromoAvailability.ACTIVE -> 0f
        rule == RewardRule.ONE_TIME -> (qualifyingSubtotal * 100f / threshold).coerceAtMost(99.99f)
        else -> (qualifyingSubtotal * 100f / threshold).coerceAtMost(barLimit)      // stops at 200
    }

    fun withQuantity(id: String, quantity: Int): PromoDemoState {
        if (availability != PromoAvailability.ACTIVE) return this
        val updated = copy(products = products.map {
            if (it.id == id) it.copy(quantity = quantity.coerceIn(0, it.stock)) else it
        })
        return updated.copy(claimedGifts = updated.claimedGifts.take(updated.earnedGifts))
    }

    fun claimGift(id: String): PromoDemoState = if (unclaimedGifts > 0) {
        copy(claimedGifts = claimedGifts + id)
    } else this

    fun withClaimedGifts(ids: List<String>): PromoDemoState = copy(claimedGifts = ids.take(earnedGifts))

    companion object { private const val MAX_REPEAT_LAPS = 2 }
}

internal data class DemoProduct(
    val id: String,
    val name: String,
    val image: Int,
    val price: Long = 50_000L,
    val originalPrice: Long = 100_000L,
    val quantity: Int = 0,
    val stock: Int = 99,
    val qualifies: Boolean = true,
    val discounted: Boolean = true,
    val promotion: String = "Paket Bundling",
    val benefit: String = "+10 Poin",
    val highlight: String = "Gratis Hadiah"
)

internal data class GiftOffer(
    val id: String,
    val name: String,
    val image: Int,
    val packSize: Int = 1,
    val originalPrice: Long = 10_000L,
    val price: Long = 5_000L,
    val restocking: Boolean = false
)

internal data class GiftLine(val offer: GiftOffer, val quantity: Int)
internal enum class RewardRule { ONE_TIME, REPEATABLE }
internal enum class PromoAvailability { ACTIVE, QUOTA_EXHAUSTED, LIMIT_REACHED, NEW_CUSTOMER_ONLY }
internal enum class DemoScenario(val label: String) {
    SEARCH_GIFT("Search · Hadiah Gratis · Rp50.000 di keranjang"),
    SEARCH_REDEMPTION("Search · Tebus Murah · Rp50.000 di keranjang"),
    SEARCH_ERROR("Search · simulasi gagal sekali lalu coba lagi"),
    START("Tidak berlaku kelipatan · belum ada produk"),
    HALFWAY("Tambah Rp50.000 lagi"),
    ELIGIBLE("Bisa ambil 1 hadiah"),
    CLAIMED("Sudah ambil 1 hadiah"),
    REPEATABLE("Berlaku kelipatan · mulai belanja"),
    REPEATABLE_PARTIAL("Berlaku kelipatan · 1 dari 2 hadiah"),
    REPEATABLE_THREE("Berlaku kelipatan · 3 hadiah"),
    QUOTA("Kuota promo habis"),
    LIMIT("Limit fair habis"),
    NEW_USER("Hanya transaksi pertama"),
    ZERO_PRODUCTS("Semua produk unpublished")
}
