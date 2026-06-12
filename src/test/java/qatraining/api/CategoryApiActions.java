package qatraining.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;

/**
 * Low-level API call methods for Category endpoints.
 * Follows the same pattern as SalesApiActions.java
 */
public class CategoryApiActions {

    private static final String BASE_URL       = "http://localhost:8080";
    private static final String CATEGORIES_URL = BASE_URL + "/api/categories";

    public Response getAllCategories(String token) {
        return SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .get(CATEGORIES_URL);
    }

    public Response getCategoryById(String token, int id) {
        return SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .get(CATEGORIES_URL + "/" + id);
    }

    public Response createCategory(String token, String name, Integer parentId) {
        String body = parentId == null
                ? String.format("{\"name\":\"%s\"}", name)
                : String.format("{\"name\":\"%s\",\"parentId\":%d}", name, parentId);

        return SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(body)
                .post(CATEGORIES_URL);
    }

    public Response updateCategory(String token, int id, String name) {
        String body = String.format("{\"name\":\"%s\"}", name);
        return SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(body)
                .put(CATEGORIES_URL + "/" + id);
    }

    public Response deleteCategory(String token, int id) {
        return SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .delete(CATEGORIES_URL + "/" + id);
    }
}
