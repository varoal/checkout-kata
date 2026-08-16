package checkout

import checkout.promotions.BuyNGetOneFree
import checkout.promotions.MealDeal
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
    inner class MealDealPromotion {

        // D: 150p, E: 200p, D+E together for 300p
        private val pricing = PricingRules(
            unitPrices = mapOf("D" to Money(150), "E" to Money(200)),
            promotions = listOf(MealDeal(skus = setOf("D", "E"), bundlePrice = Money(300)))
        )

        @Test
        fun `D alone is priced normally`() {
            val checkout = Checkout(pricing)
            checkout.scan("D")
            assertEquals(Money(150), checkout.totalPrice())
        }

        @Test
        fun `E alone is priced normally`() {
            val checkout = Checkout(pricing)
            checkout.scan("E")
            assertEquals(Money(200), checkout.totalPrice())
        }

        @Test
        fun `one D and one E form a bundle`() {
            val checkout = Checkout(pricing)
            checkout.scan("D")
            checkout.scan("E")
            assertEquals(Money(300), checkout.totalPrice())
        }

        @Test
        fun `two of each form two bundles`() {
            val checkout = Checkout(pricing)
            checkout.scan("D")
            checkout.scan("D")
            checkout.scan("E")
            checkout.scan("E")
            assertEquals(Money(600), checkout.totalPrice())
        }

        @Test
        fun `unbalanced quantities only bundle as many sets as the scarcer item allows`() {
            // 2 D, 1 E: one bundle (300) + one D at unit price (150)
            val checkout = Checkout(pricing)
            checkout.scan("D")
            checkout.scan("D")
            checkout.scan("E")
            assertEquals(Money(450), checkout.totalPrice())
        }
    }

    @Nested
    inner class OrderIndependenceAndMixedItems {

        // This week's full published pricing: base prices + all three promotions.
        private val weeklyPricing = PricingRules(
            unitPrices = mapOf(
                "A" to Money(50),
                "B" to Money(75),
                "C" to Money(25),
                "D" to Money(150),
                "E" to Money(200)
            ),
            promotions = listOf(
                MultiPrice(sku = "B", quantityForSpecialPrice = 2, specialPrice = Money(125)),
                BuyNGetOneFree(sku = "C", buyQuantity = 3, freeQuantity = 1),
                MealDeal(skus = setOf("D", "E"), bundlePrice = Money(300))
            )
        )

        private fun totalFor(skus: List<String>): Money {
            val checkout = Checkout(weeklyPricing)
            skus.forEach { checkout.scan(it) }
            return checkout.totalPrice()
        }

        @Test
        fun `B, A, B recognises the two Bs and applies the promotion`() {
            // B + A + B = 175p
            assertEquals(Money(175), totalFor(listOf("B", "A", "B")))
        }

        @Test
        fun `scan order does not affect the total`() {
            val expected = Money(175)
            val permutations = listOf(
                listOf("B", "A", "B"),
                listOf("A", "B", "B"),
                listOf("B", "B", "A")
            )
            permutations.forEach { items ->
                assertEquals(expected, totalFor(items))
            }
        }

        @Test
        fun `combining multiple different promotions in one basket`() {
            // A(50) + BB(125) + CCCC(75) + D+E(300) = 550
            val total = totalFor(listOf("A", "B", "C", "D", "B", "C", "E", "C", "C"))
            assertEquals(Money(550), total)
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
