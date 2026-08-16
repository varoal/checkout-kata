package checkout.promotions

import checkout.Money

/**
 * "Buy [quantityForSpecialPrice] of [sku] for [specialPrice]".
 *
 * Applies per complete group; a partial group is charged at unit price.
 */
class MultiPrice(
    private val sku: String,
    private val quantityForSpecialPrice: Int,
    private val specialPrice: Money
) : Promotion {

    override fun discount(quantities: Map<String, Int>, unitPrices: Map<String, Money>): Money {
        val quantity = quantities[sku] ?: 0
        val eligibleGroups = quantity / quantityForSpecialPrice
        val normalPriceForGroup = unitPrices.getValue(sku) * quantityForSpecialPrice
        return (normalPriceForGroup - specialPrice) * eligibleGroups
    }
}
