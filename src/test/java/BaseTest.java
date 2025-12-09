import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public abstract class BaseTest {

    protected static final String DATA = "src/test/resources/data/";
    protected static final String SCHEMAS = "src/test/resources/schemas/";

    protected static List<String> locales;
    protected static List<Object> quantities;
    protected static List<Object> seeds;

    @BeforeAll
    static void loadData() {
        locales = toStringList(Json.list(DATA + "common.json", "locales"));
        quantities = Json.list(DATA + "common.json", "valid_qty");
        seeds = Json.list(DATA + "common.json", "seeds");
    }

    @SuppressWarnings("unchecked")
    protected static List<String> toStringList(List<Object> list) {
        return (List<String>) (List<?>) list;
    }

    // MUST IMPLEMENT
    protected abstract String endpoint();
    protected abstract String schema();

    // CHECKS
    protected void assertOk(Response res) {
        assertNotNull(res);
        assertEquals(200, res.statusCode());
        assertEquals("OK", res.jsonPath().getString("status"));
    }

    protected void assertStructure(Response res) {
        assertTrue(Json.hasFields(res, "status", "code", "total", "data"));
    }

    protected void assertSchema(Response res) {
        Json.assertSchema(res, schema());
    }

    protected void assertCount(Response res, int expected) {
        assertEquals(Math.min(expected, Api.MAX_QTY), res.jsonPath().getList("data").size());
    }

    // REQUESTS
    protected Response fetchDefault() {
        Response res = new Api.Request(endpoint()).send();
        assertOk(res);
        assertStructure(res);
        return res;
    }

    protected Response fetch(int qty) {
        return new Api.Request(endpoint()).qty(qty).send();
    }

    protected Response fetchLocale(String locale) {
        Response res = new Api.Request(endpoint()).locale(locale).send();
        assertOk(res);
        return res;
    }

    protected void verifySeed(int seed) {
        Response r1 = new Api.Request(endpoint()).seed(seed).qty(5).send();
        Response r2 = new Api.Request(endpoint()).seed(seed).qty(5).send();
        assertEquals(r1.asString(), r2.asString());
    }
}

