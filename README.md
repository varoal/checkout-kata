# Checkout Kata

A supermarket checkout that totals scanned items, applying the pricing
promotions configured for the transaction.

## Running

Requires JDK 21+ on your `PATH`. Maven does not need to be installed,
the Maven Wrapper is included. On Windows, `JAVA_HOME` must also be set.

Run the tests:

```bash
./mvnw test        # macOS/Linux
mvnw.cmd test       # Windows
```

Build the project:

```bash
./mvnw package
```

## Notes

The checkout logic is verified through the test suite. No UI, API, CLI,
or persistence layer is included, as these are outside the scope of the
brief.

Pricing rules (including three promotion types: `MultiPrice`,
`BuyNGetOneFree`, and `MealDeal`) are supplied to each checkout
transaction. Promotions are kept separate from the checkout logic.
