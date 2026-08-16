# Checkout Kata

A supermarket checkout that totals scanned items, applying the pricing
promotions configured for the transaction.

## Running

Requires a JDK 21+.

Run the tests:

```bash
./mvnw test
```

Build the project:

```bash
./mvnw package
```

No Maven installation is required; the Maven Wrapper is included.

## Notes

The checkout logic is verified through the test suite. No UI, API, CLI,
or persistence layer is included, as these are outside the scope of the
brief.

Pricing rules (including three promotion types: `MultiPrice`,
`BuyNGetOneFree`, and `MealDeal`) are supplied to each checkout
transaction. Promotions are kept separate from the checkout logic.
