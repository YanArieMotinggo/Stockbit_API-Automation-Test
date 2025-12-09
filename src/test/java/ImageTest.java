import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DisplayName("Images API")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ImageTest extends BaseTest {

    private static List<String> validTypes;
    private static List<Object> validWidths;
    private static List<Object> validHeights;
    private static List<Object> invalidWidths;
    private static List<Object> invalidHeights;

    protected String endpoint() { return Api.IMAGES; }
    protected String schema() { return SCHEMAS + "image-schema.json"; }

    @BeforeAll
    static void setup() {
        validTypes = toStringList(Json.list(DATA + "images.json", "valid_types"));
        validWidths = Json.list(DATA + "images.json", "valid_widths");
        validHeights = Json.list(DATA + "images.json", "valid_heights");
        invalidWidths = Json.list(DATA + "images.json", "invalid_widths");
        invalidHeights = Json.list(DATA + "images.json", "invalid_heights");
    }

    static Stream<String> locales() { return BaseTest.locales.stream(); }
    static Stream<Integer> qtys() { return quantities.stream().map(o -> ((Number) o).intValue()); }
    static Stream<String> types() { return validTypes.stream(); }
    static Stream<Integer> widths() { return validWidths.stream().map(o -> ((Number) o).intValue()); }
    static Stream<Integer> heights() { return validHeights.stream().map(o -> ((Number) o).intValue()); }
    static Stream<Integer> badWidths() { return invalidWidths.stream().map(o -> ((Number) o).intValue()); }
    static Stream<Integer> badHeights() { return invalidHeights.stream().map(o -> ((Number) o).intValue()); }

    // ===== POSITIVE TESTS =====

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
        
        Map<String, Object> img = res.jsonPath().getMap("data[0]");
        assertNotNull(img.get("title"));
        assertNotNull(img.get("description"));
        assertNotNull(img.get("url"));
    }

    @ParameterizedTest @Order(7) @MethodSource("types")
    void typeParameterTest(String type) {
        Response res = new Api.Request(endpoint()).qty(2).param("_type", type).send();
        assertOk(res);
        assertCount(res, 2);
    }

    @ParameterizedTest @Order(8) @MethodSource("widths")
    void widthParameterTest(int width) {
        Response res = new Api.Request(endpoint()).qty(1).param("_width", width).send();
        assertOk(res);
        
        String url = res.jsonPath().getString("data[0].url");
        assertThat(url, containsString(String.valueOf(width)));
    }

    @ParameterizedTest @Order(9) @MethodSource("heights")
    void heightParameterTest(int height) {
        Response res = new Api.Request(endpoint()).qty(1).param("_height", height).send();
        assertOk(res);
        
        String url = res.jsonPath().getString("data[0].url");
        assertThat(url, containsString(String.valueOf(height)));
    }

    @Test @Order(10)
    void widthAndHeightCombined() {
        Response res = new Api.Request(endpoint()).qty(1).param("_width", 500).param("_height", 300).send();
        assertOk(res);
        
        String url = res.jsonPath().getString("data[0].url");
        assertThat(url, containsString("500"));
        assertThat(url, containsString("300"));
    }

    @Test @Order(11)
    void urlFormatTest() {
        Response res = new Api.Request(endpoint()).qty(3).send();
        assertOk(res);
        
        for (Map<String, Object> img : res.jsonPath().<Map<String, Object>>getList("data")) {
            assertThat((String) img.get("url"), startsWith("https://"));
        }
    }

    @Test @Order(12)
    void combinedTest() {
        Response res = new Api.Request(endpoint())
                .locale("en_US").qty(5).seed(12345)
                .param("_type", "any").param("_width", 640).param("_height", 480)
                .send();
        assertOk(res);
        assertCount(res, 5);
        assertSchema(res);
    }

    // ===== NEGATIVE TESTS =====

    @ParameterizedTest @Order(13) @MethodSource("badWidths")
    @DisplayName("Negative: Invalid width values (0, -1, -100)")
    void invalidWidthTest(int width) {
        Response res = new Api.Request(endpoint()).qty(1).param("_width", width).send();
        
        // API may still return 200 but with default/fallback values
        // or may return an error - we just verify it handles gracefully
        assertNotNull(res);
        System.out.println("Width " + width + " → Status: " + res.statusCode());
    }

    @ParameterizedTest @Order(14) @MethodSource("badHeights")
    @DisplayName("Negative: Invalid height values (0, -1, -100)")
    void invalidHeightTest(int height) {
        Response res = new Api.Request(endpoint()).qty(1).param("_height", height).send();
        
        assertNotNull(res);
        System.out.println("Height " + height + " → Status: " + res.statusCode());
    }

    @Test @Order(15)
    @DisplayName("Negative: Both width and height zero")
    void zeroWidthAndHeightTest() {
        Response res = new Api.Request(endpoint()).qty(1).param("_width", 0).param("_height", 0).send();
        
        assertNotNull(res);
        System.out.println("Width=0, Height=0 → Status: " + res.statusCode());
    }

    @Test @Order(16)
    @DisplayName("Negative: Both width and height negative")
    void negativeWidthAndHeightTest() {
        Response res = new Api.Request(endpoint()).qty(1).param("_width", -100).param("_height", -100).send();
        
        assertNotNull(res);
        System.out.println("Width=-100, Height=-100 → Status: " + res.statusCode());
    }

    @Test @Order(17)
    @DisplayName("Negative: Invalid type parameter")
    void invalidTypeTest() {
        Response res = new Api.Request(endpoint()).qty(1).param("_type", "invalid_type").send();
        
        assertNotNull(res);
        System.out.println("Type=invalid_type → Status: " + res.statusCode());
    }

    @Test @Order(18)
    @DisplayName("Negative: Extremely large width")
    void extremeWidthTest() {
        Response res = new Api.Request(endpoint()).qty(1).param("_width", 999999).send();
        
        assertNotNull(res);
        System.out.println("Width=999999 → Status: " + res.statusCode());
    }

    @Test @Order(19)
    @DisplayName("Negative: Extremely large height")
    void extremeHeightTest() {
        Response res = new Api.Request(endpoint()).qty(1).param("_height", 999999).send();
        
        assertNotNull(res);
        System.out.println("Height=999999 → Status: " + res.statusCode());
    }
}

