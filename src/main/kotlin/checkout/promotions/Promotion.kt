package checkout.promotions

import checkout.Money

/**
 * Computes a discount off the naive (unit price × quantity) subtotal.
 *
 * Takes the full [unitPrices] map, not just its own SKU's price, since a
 * promotion may span multiple SKUs (e.g. a meal deal).
 */
interface Promotion {
    fun discount(quantities: Map<String, Int>, unitPrices: Map<String, Money>): Money
}
