package checkout

/**
 * Accepts scanned items in any order and totals them using [pricingRules].
 *
 * Has no built-in knowledge of specific SKUs or promotions, allowing
 * different transactions to use different pricing without code changes.
 */
class Checkout(private val pricingRules: PricingRules) {

    private val scannedQuantities = mutableMapOf<String, Int>()

    fun scan(sku: String) {
        require(pricingRules.contains(sku)) { "Unknown SKU: $sku" }
        scannedQuantities.merge(sku, 1, Int::plus)
    }

    fun totalPrice(): Money {
        val subtotal = scannedQuantities.entries.fold(Money.ZERO) { total, (sku, quantity) ->
            total + pricingRules.unitPriceOf(sku) * quantity
        }
        val discount = pricingRules.promotions.fold(Money.ZERO) { total, promotion ->
            total + promotion.discount(scannedQuantities, pricingRules.unitPrices)
        }
        return subtotal - discount
    }
}
