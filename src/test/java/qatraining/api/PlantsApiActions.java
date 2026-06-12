package qatraining.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.rest.SerenityRest;

import java.util.HashMap;
import java.util.Map;

public class PlantsApiActions {

    private RequestSpecification givenAuth(String token) {
        return SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .baseUri("http://localhost:8080");
    }

    @Step("Get all plants")
    public Response getAllPlants(String token) {
        return givenAuth(token)
                .when()
                .get("/api/plants");
    }

    @Step("Get plant by ID {1}")
    public Response getPlantById(String token, Long plantId) {
        return givenAuth(token)
                .when()
                .get("/api/plants/" + plantId);
    }

    @Step("Get paged plants with page={1}, size={2}")
    public Response getPlantsPaged(String token, int page, int size) {
        return givenAuth(token)
                .queryParam("page", page)
                .queryParam("size", size)
                .when()
                .get("/api/plants/paged");
    }

    @Step("Create plant under category {1} with name={2}, price={3}, quantity={4}")
    public Response createPlant(String token, Long categoryId, String name, double price, int quantity) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("price", price);
        body.put("quantity", quantity);
        return givenAuth(token)
                .body(body)
                .when()
                .post("/api/plants/category/" + categoryId);
    }

    @Step("Update plant with ID {1}: name={2}, price={3}, quantity={4}")
    public Response updatePlant(String token, Long plantId, Long categoryId, String name, double price, int quantity) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("price", price);
        body.put("quantity", quantity);
        if (categoryId != null) {
            Map<String, Object> category = new HashMap<>();
            category.put("id", categoryId);
            body.put("category", category);
        }
        return givenAuth(token)
                .body(body)
                .when()
                .put("/api/plants/" + plantId);
    }

    @Step("Delete plant with ID {1}")
    public Response deletePlant(String token, Long plantId) {
        return givenAuth(token)
                .when()
                .delete("/api/plants/" + plantId);
    }

    @Step("Get sub-category ID from the categories list")
    public Long getSubCategoryId(String token) {
        Response response = SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .baseUri("http://localhost:8080")
                .when()
                .get("/api/categories");

        if (response.getStatusCode() == 200) {
            java.util.List<java.util.Map<String, Object>> categories = response.jsonPath().getList("");
            for (java.util.Map<String, Object> cat : categories) {
                // API returns "parentName": "-" for root categories and a real name for sub-categories
                Object parentName = cat.get("parentName");
                if (parentName != null && !"-".equals(parentName.toString())) {
                    Object id = cat.get("id");
                    if (id instanceof Integer) return Long.valueOf((Integer) id);
                    if (id instanceof Long) return (Long) id;
                }
            }
            // Fallback: return first non-root category found or default
            for (java.util.Map<String, Object> cat : categories) {
                Object parentName = cat.get("parentName");
                if (parentName != null) {
                    Object id = cat.get("id");
                    if (id instanceof Integer) return Long.valueOf((Integer) id);
                    if (id instanceof Long) return (Long) id;
                }
            }
        }
        return 3L;
    }
}
