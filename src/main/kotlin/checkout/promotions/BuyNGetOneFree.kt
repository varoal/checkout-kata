package checkout.promotions

import checkout.Money

/**
 * "Buy [buyQuantity] of [sku], get [freeQuantity] free".
 *
 * Only complete cycles of (buyQuantity + freeQuantity) earn a free item.
 * A partial cycle is charged in full. E.g. buy 3 get 1 free: CCC = 75p
 * (no discount yet), CCCC = 75p (the 4th is free).
 */
class BuyNGetOneFree(
    private val sku: String,
    private val buyQuantity: Int,
    private val freeQuantity: Int = 1
) : Promotion {

    private val cycleSize = buyQuantity + freeQuantity

    override fun discount(quantities: Map<String, Int>, unitPrices: Map<String, Money>): Money {
        val quantity = quantities[sku] ?: 0
        val completeCycles = quantity / cycleSize
        val freeItems = completeCycles * freeQuantity
        return unitPrices.getValue(sku) * freeItems
    }
}
