package checkout

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
