package checkout.promotions

import checkout.Money

/**
 * "Buy one of each of [skus] together for [bundlePrice]".
 *
 * Complete sets = the smallest quantity among [skus], so unbalanced
 * quantities (e.g. more Ds than Es) only discount as many sets as the
 * scarcest item allows.
 */
class MealDeal(
    private val skus: Set<String>,
    private val bundlePrice: Money
) : Promotion {

    override fun discount(quantities: Map<String, Int>, unitPrices: Map<String, Money>): Money {
        val completeSets = skus.minOf { quantities[it] ?: 0 }
        if (completeSets == 0) return Money.ZERO

        val normalPriceForSet = skus.fold(Money.ZERO) { total, sku -> total + unitPrices.getValue(sku) }
        return (normalPriceForSet - bundlePrice) * completeSets
    }
}
