import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DisplayName("Companies API")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CompanyTest extends BaseTest {

    protected String endpoint() { return Api.COMPANIES; }
    protected String schema() { return SCHEMAS + "company-schema.json"; }

    static Stream<String> locales() { return BaseTest.locales.stream(); }
    static Stream<Integer> qtys() { return quantities.stream().map(o -> ((Number) o).intValue()); }

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

    @Test @Order(6)
    void fieldsTest() {
        Response res = new Api.Request(endpoint()).qty(1).send();
        assertOk(res);
        
        Map<String, Object> c = res.jsonPath().getMap("data[0]");
        assertNotNull(c.get("id"));
        assertNotNull(c.get("name"));
        assertNotNull(c.get("email"));
        assertNotNull(c.get("vat"));
        assertNotNull(c.get("phone"));
        assertNotNull(c.get("country"));
        assertNotNull(c.get("addresses"));
        assertNotNull(c.get("website"));
        assertNotNull(c.get("image"));
        assertNotNull(c.get("contact"));
    }

    @Test @Order(7)
    void addressesArrayTest() {
        Response res = new Api.Request(endpoint()).qty(1).send();
        assertOk(res);
        
        List<Map<String, Object>> addresses = res.jsonPath().getList("data[0].addresses");
        assertFalse(addresses.isEmpty());
        
        Map<String, Object> addr = addresses.get(0);
        assertNotNull(addr.get("street"));
        assertNotNull(addr.get("city"));
        assertNotNull(addr.get("country"));
        assertNotNull(addr.get("country_code"));
        assertNotNull(addr.get("latitude"));
        assertNotNull(addr.get("longitude"));
    }

    @Test @Order(8)
    void contactObjectTest() {
        Response res = new Api.Request(endpoint()).qty(1).send();
        assertOk(res);
        
        Map<String, Object> contact = res.jsonPath().getMap("data[0].contact");
        assertNotNull(contact.get("id"));
        assertNotNull(contact.get("firstname"));
        assertNotNull(contact.get("lastname"));
        assertNotNull(contact.get("email"));
        assertNotNull(contact.get("phone"));
        assertNotNull(contact.get("gender"));
    }

    @Test @Order(9)
    void emailFormatTest() {
        Response res = new Api.Request(endpoint()).qty(3).send();
        assertOk(res);
        
        for (Map<String, Object> c : res.jsonPath().<Map<String, Object>>getList("data")) {
            assertThat((String) c.get("email"), containsString("@"));
        }
    }

    @Test @Order(10)
    void phoneFormatTest() {
        Response res = new Api.Request(endpoint()).qty(3).send();
        assertOk(res);
        
        for (Map<String, Object> c : res.jsonPath().<Map<String, Object>>getList("data")) {
            String phone = (String) c.get("phone");
            assertThat(phone, startsWith("+"));
        }
    }

    @Test @Order(11)
    void websiteFormatTest() {
        Response res = new Api.Request(endpoint()).qty(3).send();
        assertOk(res);
        
        for (Map<String, Object> c : res.jsonPath().<Map<String, Object>>getList("data")) {
            assertThat((String) c.get("website"), startsWith("http"));
        }
    }

    @Test @Order(12)
    void combinedTest() {
        Response res = new Api.Request(endpoint()).locale("en_US").qty(5).seed(12345).send();
        assertOk(res);
        assertCount(res, 5);
        assertSchema(res);
    }
}

