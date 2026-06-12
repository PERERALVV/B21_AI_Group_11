package qatraining.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.rest.SerenityRest;

import java.util.ArrayList;
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

    @Step("Update plant {1} with full swagger body: name={3}, price={4}, quantity={5}, categoryId={2}")
    public Response updatePlantWithFullBody(String token, Long plantId, Long categoryId,
                                            String name, double price, int quantity) {
        Map<String, Object> category = new HashMap<>();
        category.put("id", categoryId != null ? categoryId : 0);
        category.put("name", "");
        category.put("parent", "");
        category.put("subCategories", new ArrayList<>());

        Map<String, Object> body = new HashMap<>();
        body.put("id", plantId);
        body.put("name", name);
        body.put("price", price);
        body.put("quantity", quantity);
        body.put("category", category);

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

    @Step("Get sub-category ID from the categories list, creating one if none exists")
    public Long getSubCategoryId(String token) {
        Response response = SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .baseUri("http://localhost:8080")
                .when()
                .get("/api/categories");

        if (response.getStatusCode() == 200) {
            java.util.List<java.util.Map<String, Object>> categories = response.jsonPath().getList("");
            if (categories != null) {
                for (java.util.Map<String, Object> cat : categories) {
                    Object parentName = cat.get("parentName");
                    if (parentName != null && !"-".equals(parentName.toString())) {
                        Object id = cat.get("id");
                        if (id instanceof Integer) return Long.valueOf((Integer) id);
                        if (id instanceof Long) return (Long) id;
                    }
                }
            }
        }

        // No sub-category found — create one
        return createSubCategory(token);
    }

    private Long createSubCategory(String token) {
        // Find or create a root category first
        Long rootId = findOrCreateRootCategory(token);

        // Create sub-category under that root
        Map<String, Object> parent = new HashMap<>();
        parent.put("id", rootId);
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Indoor");
        body.put("parent", parent);

        Response response = SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .baseUri("http://localhost:8080")
                .body(body)
                .when()
                .post("/api/categories");

        if (response.getStatusCode() == 201) {
            return response.jsonPath().getLong("id");
        }
        throw new IllegalStateException(
                "Failed to create sub-category. Status: " + response.getStatusCode()
                + " Body: " + response.getBody().asString());
    }

    private Long findOrCreateRootCategory(String token) {
        Response response = SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .baseUri("http://localhost:8080")
                .when()
                .get("/api/categories/main");

        if (response.getStatusCode() == 200) {
            java.util.List<java.util.Map<String, Object>> mains = response.jsonPath().getList("");
            if (mains != null && !mains.isEmpty()) {
                Object id = mains.get(0).get("id");
                if (id instanceof Integer) return Long.valueOf((Integer) id);
                if (id instanceof Long) return (Long) id;
            }
        }

        // No root category — create one
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Plants");
        Response created = SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .baseUri("http://localhost:8080")
                .body(body)
                .when()
                .post("/api/categories");

        if (created.getStatusCode() == 201) {
            return created.jsonPath().getLong("id");
        }
        throw new IllegalStateException(
                "Failed to create root category. Status: " + created.getStatusCode()
                + " Body: " + created.getBody().asString());
    }
}
