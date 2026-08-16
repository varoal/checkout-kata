package checkout

import checkout.promotions.Promotion

/**
 * Unit prices and promotions applicable to a checkout transaction.
 */
data class PricingRules(
    val unitPrices: Map<String, Money>,
    val promotions: List<Promotion> = emptyList()
) {
    fun contains(sku: String): Boolean = sku in unitPrices

    fun unitPriceOf(sku: String): Money =
        unitPrices[sku] ?: throw IllegalArgumentException("Unknown SKU: $sku")
}
