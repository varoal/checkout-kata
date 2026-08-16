package checkout

import checkout.promotions.BuyNGetOneFree
import checkout.promotions.MultiPrice
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CheckoutTest {

    // No promotions, base-pricing tests.
    private val basePricing = PricingRules(
        unitPrices = mapOf(
            "A" to Money(50),
            "B" to Money(75),
            "C" to Money(25),
            "D" to Money(150),
            "E" to Money(200)
        )
    )

    @Nested
    inner class BasePricing {

        @Test
        fun `each SKU is priced independently`() {
            listOf("A" to 50, "B" to 75, "C" to 25, "D" to 150, "E" to 200).forEach { (sku, price) ->
                val checkout = Checkout(basePricing)
                checkout.scan(sku)
                assertEquals(Money(price), checkout.totalPrice())
            }
        }

        @Test
        fun `multiple different items sum their unit prices`() {
            val checkout = Checkout(basePricing)
            checkout.scan("A")
            checkout.scan("C")
            assertEquals(Money(75), checkout.totalPrice())
        }

        @Test
        fun `different checkouts can use different pricing rules`() {
            val cheapPricing = PricingRules(
                unitPrices = mapOf("A" to Money(50))
            )
            val expensivePricing = PricingRules(
                unitPrices = mapOf("A" to Money(100))
            )

            val cheapCheckout = Checkout(cheapPricing)
            cheapCheckout.scan("A")

            val expensiveCheckout = Checkout(expensivePricing)
            expensiveCheckout.scan("A")

            assertEquals(Money(50), cheapCheckout.totalPrice())
            assertEquals(Money(100), expensiveCheckout.totalPrice())
        }
    }

    @Nested
    inner class MultiPricePromotion {

        // B: 75p each, 2 for 125p
        private val pricing = PricingRules(
            unitPrices = mapOf("B" to Money(75)),
            promotions = listOf(MultiPrice(sku = "B", quantityForSpecialPrice = 2, specialPrice = Money(125)))
        )

        private fun totalFor(quantity: Int): Money {
            val checkout = Checkout(pricing)
            repeat(quantity) { checkout.scan("B") }
            return checkout.totalPrice()
        }

        @Test
        fun `below the threshold is priced normally`() {
            assertEquals(Money(75), totalFor(1))
        }

        @Test
        fun `exactly at the threshold applies the special price`() {
            assertEquals(Money(125), totalFor(2))
        }

        @Test
        fun `above the threshold charges the remainder at unit price`() {
            assertEquals(Money(200), totalFor(3))
        }

        @Test
        fun `promotion applies twice for two complete groups`() {
            assertEquals(Money(250), totalFor(4))
        }

        @Test
        fun `promotion applies multiple times plus a remainder`() {
            // 3 groups of 2 (375) + 1 remaining at unit price (75)
            assertEquals(Money(450), totalFor(7))
        }
    }

    @Nested
    inner class BuyNGetOneFreePromotion {

        // C: 25p each, buy 3 get 1 free
        private val pricing = PricingRules(
            unitPrices = mapOf("C" to Money(25)),
            promotions = listOf(BuyNGetOneFree(sku = "C", buyQuantity = 3, freeQuantity = 1))
        )

        private fun totalFor(quantity: Int): Money {
            val checkout = Checkout(pricing)
            repeat(quantity) { checkout.scan("C") }
            return checkout.totalPrice()
        }

        @Test
        fun `one item is priced normally`() {
            assertEquals(Money(25), totalFor(1))
        }

        @Test
        fun `two items are priced normally`() {
            assertEquals(Money(50), totalFor(2))
        }

        @Test
        fun `below the free threshold no discount applies`() {
            assertEquals(Money(75), totalFor(3))
        }

        @Test
        fun `exactly at the free threshold the extra item is free`() {
            assertEquals(Money(75), totalFor(4))
        }

        @Test
        fun `above the threshold the remainder is priced normally`() {
            assertEquals(Money(100), totalFor(5))
        }

        @Test
        fun `promotion applies multiple times for multiple complete cycles`() {
            // 2 complete cycles of 4 (8 items, 2 free) + 1 remainder = 7 paid units
            assertEquals(Money(175), totalFor(9))
        }
    }

    @Nested
    inner class EdgeCases {

        @Test
        fun `empty checkout totals zero`() {
            val checkout = Checkout(basePricing)
            assertEquals(Money.ZERO, checkout.totalPrice())
        }

        @Test
        fun `scanning an unknown SKU is rejected`() {
            val checkout = Checkout(basePricing)
            assertThrows(IllegalArgumentException::class.java) { checkout.scan("Z") }
        }
    }
}
