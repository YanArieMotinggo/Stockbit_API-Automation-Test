# FakerAPI Tests

API tests for [FakerAPI.it](https://fakerapi.it/api/v2)

## Structure

```
src/
├── main/java/
│   ├── Api.java        ← Makes API calls
│   └── Json.java       ← Reads JSON files & validates schemas
│
└── test/
    ├── java/
    │   ├── BaseTest.java     ← Common test methods (inherit this)
    │   ├── ProductTest.java  ← Tests /products
    │   └── UserTest.java     ← Tests /users
    │
    └── resources/
        ├── data/             ← Test inputs
        │   ├── common.json
        │   ├── products.json
        │   └── users.json
        └── schemas/          ← Expected response shapes
            ├── product-schema.json
            └── user-schema.json
```

## Flow

```
1. Run: mvn test

2. BaseTest loads common.json (locales, quantities, seeds)

3. ProductTest/UserTest loads their own data

4. Each test:
   → Build request: new Api.Request("/products").qty(5).send()
   → API returns JSON response
   → Check: assertOk(), assertSchema(), assertCount()
   → Pass ✓ or Fail ✗
```

## Run

```bash
mvn test                     # All
mvn test -Dtest=ProductTest  # Products only
mvn test -Dtest=UserTest     # Users only
```

## Add New Endpoint

1. Add in `Api.java`: `public static final String BOOKS = "/books";`
2. Create: `resources/schemas/book-schema.json`
3. Create test:

```java
class BookTest extends BaseTest {
    protected String endpoint() { return Api.BOOKS; }
    protected String schema() { return SCHEMAS + "book-schema.json"; }
    
    @Test void defaultRequest() { assertCount(fetchDefault(), 10); }
}
```
