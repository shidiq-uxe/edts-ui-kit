package id.co.edtslib.edtsuikit.demo.promo

/** Search presentation never owns quantities or the qualifying subtotal. */
internal data class PromoSearchState(
    val mode: SearchMode = SearchMode.DETAIL,
    val draft: String = "",
    val submitted: String = "",
    val generation: Long = 0,
    val fallbackAvailable: Boolean = false,
    val hasSubmission: Boolean = false
) {
    fun edit(query: String = draft) = copy(mode = SearchMode.EDITING, draft = query, generation = generation + 1)

    fun submit(): PromoSearchState {
        val query = normalize(draft)
        return if (query.isEmpty()) this else copy(
            mode = SearchMode.LOADING, draft = query, submitted = query,
            generation = generation + 1, fallbackAvailable = false, hasSubmission = true
        )
    }

    fun complete(request: Long, resultCount: Int, fallback: Boolean): PromoSearchState =
        if (request != generation || mode != SearchMode.LOADING) this
        else copy(mode = if (resultCount == 0) SearchMode.EMPTY else SearchMode.RESULTS,
            fallbackAvailable = resultCount == 0 && fallback)

    fun fail(request: Long): PromoSearchState =
        if (request != generation || mode != SearchMode.LOADING) this else copy(mode = SearchMode.ERROR)

    companion object {
        fun normalize(query: String) = query.trim().replace(Regex("\\s+"), " ")

        fun matching(products: List<DemoProduct>, query: String): List<DemoProduct> {
            val words = normalize(query).split(" ").filter { it.isNotEmpty() }
            if (words.isEmpty()) return emptyList()
            // The original demo repeats fixture artwork; search exposes one catalog entry per name.
            return products.distinctBy { it.name }.filter { product -> words.all { product.name.contains(it, ignoreCase = true) } }
        }

        // Explicit Figma demo fixture, not a production policy or failure counter.
        fun websiteFallback(query: String) = normalize(query).equals("Rokok", ignoreCase = true)
    }
}

internal enum class SearchMode { DETAIL, EDITING, LOADING, RESULTS, EMPTY, ERROR }
internal enum class DemoPromotion { FREE_GIFT, REDEMPTION }
