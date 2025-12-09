# FakerAPI Tests

API tests for [FakerAPI.it](https://fakerapi.it/api/v2)

## What We Test

###  Products (`/products`)
Testing fake product data generation:
- Can we get products with different tax rates?
- Does price filtering work (min/max)?
- Are categories returned as integers, strings, or UUIDs correctly?
- Do all products have required fields like name, price, EAN code?

###  Users (`/users`)
Testing fake user data generation:
- Does the gender filter work (male/female)?
- Are UUIDs in the correct format?
- Do emails contain @ symbol?
- Are IP addresses valid format (xxx.xxx.xxx.xxx)?
- Are MAC addresses properly formatted?

###  Companies (`/companies`)
Testing fake company data generation:
- Does each company have contact information?
- Are company addresses properly structured?
- Do phone numbers start with +?
- Are emails and websites in valid format?

###  Images (`/images`)
Testing fake image URL generation:
- Does the type parameter work (any, pokemon)?
- Can we set custom width and height?
- **Negative tests:** What happens with width=0, height=-1, or extremely large values?

### Common Tests (All Endpoints)
Every endpoint is tested for:
- Default request returns 10 items
- Response matches expected JSON schema
- Locale/language parameter works
- Quantity parameter returns correct count
- Seed parameter gives reproducible results

---

## Project Structure

```
src/
├── main/java/
│   ├── Api.java        ← Makes API calls
│   └── Json.java       ← Reads JSON & validates schemas
│
└── test/
    ├── java/
    │   ├── BaseTest.java     ← Shared test methods
    │   ├── ProductTest.java
    │   ├── UserTest.java
    │   ├── CompanyTest.java
    │   └── ImageTest.java
    │
    └── resources/
        ├── data/             ← Test inputs (JSON)
        └── schemas/          ← Expected response shapes
```

## How to Run

```bash
mvn test                      # Run everything
mvn test -Dtest=ProductTest   # Just products
mvn test -Dtest=UserTest      # Just users
mvn test -Dtest=CompanyTest   # Just companies
mvn test -Dtest=ImageTest     # Just images
```
