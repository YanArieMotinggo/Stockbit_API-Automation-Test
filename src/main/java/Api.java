import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;

public class Api {
    
    public static final String BASE = "https://fakerapi.it/api/v2";
    public static final String PRODUCTS = "/products";
    public static final String USERS = "/users";
    public static final int DEFAULT_QTY = 10;
    public static final int MAX_QTY = 1000;

    public static Response get(String endpoint, Map<String, Object> params) {
        var req = RestAssured.given().baseUri(BASE).contentType("application/json");
        if (params != null && !params.isEmpty()) req.queryParams(params);
        return req.get(endpoint);
    }

    public static class Request {
        private final String endpoint;
        private final Map<String, Object> params = new HashMap<>();

        public Request(String endpoint) { this.endpoint = endpoint; }
        public Request locale(String v) { params.put("_locale", v); return this; }
        public Request qty(int v) { params.put("_quantity", v); return this; }
        public Request seed(int v) { params.put("_seed", v); return this; }
        public Request param(String k, Object v) { params.put(k, v); return this; }
        public Response send() { return Api.get(endpoint, params); }
    }
}

