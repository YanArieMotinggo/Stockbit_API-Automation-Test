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

@DisplayName("Users API")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserTest extends BaseTest {

    private static List<String> genders;

    protected String endpoint() { return Api.USERS; }
    protected String schema() { return SCHEMAS + "user-schema.json"; }

    @BeforeAll
    static void setup() {
        genders = toStringList(Json.list(DATA + "users.json", "genders"));
    }

    static Stream<String> locales() { return BaseTest.locales.stream(); }
    static Stream<Integer> qtys() { return quantities.stream().map(o -> ((Number) o).intValue()); }
    static Stream<String> genderValues() { return genders.stream(); }

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

    @ParameterizedTest @Order(6) @MethodSource("genderValues")
    void genderTest(String gender) {
        Response res = new Api.Request(endpoint()).qty(3).param("_gender", gender).send();
        assertOk(res);
        assertCount(res, 3);
    }

    @Test @Order(7)
    void fieldsTest() {
        Response res = new Api.Request(endpoint()).qty(1).send();
        assertOk(res);
        Map<String, Object> u = res.jsonPath().getMap("data[0]");
        assertNotNull(u.get("id"));
        assertNotNull(u.get("uuid"));
        assertNotNull(u.get("firstname"));
        assertNotNull(u.get("email"));
    }

    @Test @Order(8)
    void uuidTest() {
        Response res = new Api.Request(endpoint()).qty(3).send();
        assertOk(res);
        String pattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        for (Map<String, Object> u : res.jsonPath().<Map<String, Object>>getList("data")) {
            assertThat((String) u.get("uuid"), matchesPattern(pattern));
        }
    }

    @Test @Order(9)
    void emailTest() {
        Response res = new Api.Request(endpoint()).qty(3).send();
        assertOk(res);
        for (Map<String, Object> u : res.jsonPath().<Map<String, Object>>getList("data")) {
            assertThat((String) u.get("email"), containsString("@"));
        }
    }

    @Test @Order(10)
    void combinedTest() {
        Response res = new Api.Request(endpoint()).locale("en_US").qty(5).seed(54321).param("_gender", "male").send();
        assertOk(res);
        assertCount(res, 5);
        assertSchema(res);
    }
}

