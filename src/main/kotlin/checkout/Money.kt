package checkout

/**
 * An amount of money in whole pence.
 *
 * We use integer pence rather than [Double]/[Float] to avoid floating-point
 * rounding errors when adding up prices and discounts.
 */
@JvmInline
value class Money(val pence: Int) {

    operator fun plus(other: Money): Money = Money(pence + other.pence)

    operator fun minus(other: Money): Money = Money(pence - other.pence)

    operator fun times(quantity: Int): Money = Money(pence * quantity)

    companion object {
        val ZERO = Money(0)
    }
}
