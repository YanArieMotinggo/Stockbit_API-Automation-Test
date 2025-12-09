import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Products API")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductTest extends BaseTest {

    private static List<Object> taxes;
    private static List<String> categoryTypes;

    protected String endpoint() { return Api.PRODUCTS; }
    protected String schema() { return SCHEMAS + "product-schema.json"; }

    @BeforeAll
    static void setup() {
        taxes = Json.list(DATA + "products.json", "valid_taxes");
        categoryTypes = toStringList(Json.list(DATA + "products.json", "category_types"));
    }

    static Stream<String> locales() { return BaseTest.locales.stream(); }
    static Stream<Integer> qtys() { return quantities.stream().map(o -> ((Number) o).intValue()); }
    static Stream<Integer> taxValues() { return taxes.stream().map(o -> ((Number) o).intValue()); }
    static Stream<String> catTypes() { return categoryTypes.stream(); }

    @Test @Order(1)
    void defaultRequest() {
        assertCount(fetchDefault(), Api.DEFAULT_QTY);
    }

    @Test @Order(2)
    void schemaTest() {
        Response res = new Api.Request(endpoint()).qty(3).send();
        assertOk(res);
        assertSchema(res);
    }

    @ParameterizedTest @Order(3) @MethodSource("locales")
    void localeTest(String locale) {
        assertEquals(locale, fetchLocale(locale).jsonPath().getString("locale"));
    }

    @ParameterizedTest @Order(4) @MethodSource("qtys")
    void quantityTest(int qty) {
        Response res = fetch(qty);
        assertOk(res);
        assertCount(res, qty);
    }

    @Test @Order(5)
    void seedTest() {
        verifySeed(((Number) seeds.get(0)).intValue());
    }

    @ParameterizedTest @Order(6) @MethodSource("taxValues")
    void taxTest(int tax) {
        Response res = new Api.Request(endpoint()).qty(2).param("_taxes", tax).send();
        assertOk(res);
        assertEquals(tax, res.jsonPath().getInt("data[0].taxes"));
    }

    @ParameterizedTest @Order(7) @MethodSource("catTypes")
    void categoryTypeTest(String type) {
        Response res = new Api.Request(endpoint()).qty(2).param("_categories_type", type).send();
        assertOk(res);
        List<?> cats = res.jsonPath().getList("data[0].categories");
        assertFalse(cats.isEmpty());
        Object first = cats.get(0);
        switch (type) {
            case "integer" -> assertTrue(first instanceof Number);
            case "string", "uuid" -> assertTrue(first instanceof String);
        }
    }

    @Test @Order(8)
    void priceRangeTest() {
        Response res = new Api.Request(endpoint()).qty(5).param("_price_min", 50).param("_price_max", 500).send();
        assertOk(res);
    }

    @Test @Order(9)
    void fieldsTest() {
        Response res = new Api.Request(endpoint()).qty(1).send();
        assertOk(res);
        Map<String, Object> p = res.jsonPath().getMap("data[0]");
        assertNotNull(p.get("id"));
        assertNotNull(p.get("name"));
        assertNotNull(p.get("price"));
        assertNotNull(p.get("categories"));
    }

    @Test @Order(10)
    void combinedTest() {
        Response res = new Api.Request(endpoint()).locale("en_US").qty(10).seed(12345)
                .param("_taxes", 12).param("_categories_type", "uuid").send();
        assertOk(res);
        assertCount(res, 10);
        assertSchema(res);
    }
}

