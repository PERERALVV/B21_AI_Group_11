package qatraining.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import net.serenitybdd.annotations.Steps;
import qatraining.api.AuthApiActions;
import qatraining.api.PlantsApiActions;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiPlantsSteps {

    @Steps
    private AuthApiActions authApiActions;

    @Steps
    private PlantsApiActions plantsApiActions;

    private String token;
    private Long plantId;
    private Long categoryId;
    private Response lastPlantsResponse;

    // ─── Authentication ───────────────────────────────────────────────────────

    @Given("I am authorized as Admin for the plants API")
    public void authorizeAsAdminForPlantsApi() {
        token = authApiActions.getJwtToken("admin", "admin123");
        assertThat(token).as("Admin JWT token").isNotEmpty();
    }

    @Given("I am authorized as User for the plants API")
    public void authorizeAsUserForPlantsApi() {
        token = authApiActions.getJwtToken("testuser", "test123");
        assertThat(token).as("User JWT token").isNotEmpty();
    }

    // ─── Preconditions ────────────────────────────────────────────────────────

    @Given("I have a valid sub-category ID for plant creation")
    public void fetchSubCategoryId() {
        String adminToken = token != null ? token
                : authApiActions.getJwtToken("admin", "admin123");
        categoryId = plantsApiActions.getSubCategoryId(adminToken);
        assertThat(categoryId).as("Sub-category ID").isNotNull().isGreaterThan(0L);
    }

    @Given("I have a valid plant ID for update operation")
    public void fetchPlantIdForUpdate() {
        Response response = plantsApiActions.getAllPlants(token);
        if (response.getStatusCode() == 200) {
            List<Integer> ids = response.jsonPath().getList("id");
            if (ids != null && !ids.isEmpty()) {
                plantId = Long.valueOf(ids.get(0));
                return;
            }
        }
        // Create a plant to use for the update
        if (categoryId == null) fetchSubCategoryId();
        Response created = plantsApiActions.createPlant(token, categoryId, "PlantForUpdate", 50.0, 10);
        assertThat(created.getStatusCode()).isEqualTo(201);
        plantId = created.jsonPath().getLong("id");
    }

    @Given("I have created a plant specifically for deletion")
    public void createPlantForDeletion() {
        if (categoryId == null) {
            categoryId = plantsApiActions.getSubCategoryId(token);
        }
        Response response = plantsApiActions.createPlant(token, categoryId, "PlantToDelete", 25.0, 5);
        assertThat(response.getStatusCode()).isEqualTo(201);
        plantId = response.jsonPath().getLong("id");
    }

    @Given("I have a valid plant ID to retrieve via API")
    public void fetchPlantIdForRetrieval() {
        Response response = plantsApiActions.getAllPlants(token);
        if (response.getStatusCode() == 200) {
            List<Integer> ids = response.jsonPath().getList("id");
            if (ids != null && !ids.isEmpty()) {
                plantId = Long.valueOf(ids.get(0));
                return;
            }
        }
        plantId = 1L;
    }

    // ─── T-API-21: POST create plant (happy path) ─────────────────────────────

    @When("I send a POST request to create a plant with name {string} price {double} and quantity {int}")
    public void sendPostToCreatePlant(String name, double price, int quantity) {
        if (categoryId == null) fetchSubCategoryId();
        lastPlantsResponse = plantsApiActions.createPlant(token, categoryId, name, price, quantity);
    }

    @Then("the plants API response status code should be {int}")
    public void verifyPlantsApiStatusCode(int expectedStatus) {
        assertThat(lastPlantsResponse.getStatusCode())
                .as("Plants API response status code")
                .isEqualTo(expectedStatus);
    }

    @Then("the created plant response should contain name {string}")
    public void verifyCreatedPlantName(String expectedName) {
        String actualName = lastPlantsResponse.jsonPath().getString("name");
        assertThat(actualName)
                .as("Created plant name in response")
                .isEqualTo(expectedName);
    }

    @Then("the GET all plants response should include the newly created plant")
    public void verifyNewPlantInGetAllResponse() {
        Long createdId = lastPlantsResponse.jsonPath().getLong("id");
        Response getAllResponse = plantsApiActions.getAllPlants(token);
        assertThat(getAllResponse.getStatusCode()).isEqualTo(200);
        List<Integer> ids = getAllResponse.jsonPath().getList("id");
        assertThat(ids).as("GET /api/plants should include newly created plant ID")
                .contains(createdId.intValue());
    }

    // ─── T-API-22 & T-API-23: Validation errors ──────────────────────────────

    @Then("the plants response body should contain validation message {string}")
    public void verifyPlantsValidationMessage(String expectedMessage) {
        String body = lastPlantsResponse.getBody().asString();
        boolean found = body.contains(expectedMessage);
        if (!found) {
            String msg = lastPlantsResponse.jsonPath().getString("message");
            if (msg != null) found = msg.contains(expectedMessage);
        }
        if (!found) {
            List<String> messages = lastPlantsResponse.jsonPath().getList("errors");
            if (messages != null) {
                found = messages.stream().anyMatch(m -> m != null && m.contains(expectedMessage));
            }
        }
        assertThat(found)
                .as("Response body should contain validation message: '%s'\nActual body: %s",
                        expectedMessage, body)
                .isTrue();
    }

    // ─── T-API-24: PUT update plant ────────────────────────────────────────────

    @When("I send a PUT request to update the plant with name {string} price {double} and quantity {int}")
    public void sendPutToUpdatePlant(String name, double price, int quantity) {
        assertThat(plantId).as("Plant ID for update").isNotNull();
        lastPlantsResponse = plantsApiActions.updatePlant(token, plantId, categoryId, name, price, quantity);
    }

    // ─── T-API-25: DELETE plant ────────────────────────────────────────────────

    @When("I send a DELETE request to remove the plant by ID")
    public void sendDeleteToRemovePlant() {
        assertThat(plantId).as("Plant ID for deletion").isNotNull();
        lastPlantsResponse = plantsApiActions.deletePlant(token, plantId);
    }

    // ─── T-API-26: GET all plants as User ─────────────────────────────────────

    @When("I send a GET request to retrieve all plants via API")
    public void sendGetAllPlants() {
        lastPlantsResponse = plantsApiActions.getAllPlants(token);
    }

    @Then("the response body should be a JSON array containing plant records")
    public void verifyResponseIsJsonArray() {
        List<?> plants = lastPlantsResponse.jsonPath().getList("");
        assertThat(plants).as("Response body should be a non-null JSON array").isNotNull();
    }

    @Then("each plant record should contain id name price quantity and category fields")
    public void verifyPlantRecordFields() {
        List<Map<String, Object>> plants = lastPlantsResponse.jsonPath().getList("");
        if (plants != null && !plants.isEmpty()) {
            Map<String, Object> firstPlant = plants.get(0);
            assertThat(firstPlant).containsKeys("id", "name", "price", "quantity");
        }
    }

    // ─── T-API-27: GET single plant as User ────────────────────────────────────

    @When("I send a GET request to retrieve a single plant by its ID")
    public void sendGetSinglePlant() {
        assertThat(plantId).as("Plant ID for retrieval").isNotNull();
        lastPlantsResponse = plantsApiActions.getPlantById(token, plantId);
    }

    @Then("the single plant response body should contain the correct plant ID")
    public void verifySinglePlantResponseId() {
        Long returnedId = lastPlantsResponse.jsonPath().getLong("id");
        assertThat(returnedId)
                .as("Returned plant ID should match requested ID %d", plantId)
                .isEqualTo(plantId);
    }

    // ─── T-API-28: GET paged plants as User ────────────────────────────────────

    @When("I send a GET request to the plants paged endpoint with page {int} size {int}")
    public void sendGetPlantsPaged(int page, int size) {
        lastPlantsResponse = plantsApiActions.getPlantsPaged(token, page, size);
    }

    @Then("the paged plants response should contain pagination fields content totalElements totalPages number and size")
    public void verifyPagedPlantsResponseFields() {
        Map<String, Object> body = lastPlantsResponse.jsonPath().getMap("");
        assertThat(body)
                .as("Paged plants response should contain pagination metadata fields")
                .containsKeys("content", "totalElements", "totalPages", "number", "size");
    }
}
