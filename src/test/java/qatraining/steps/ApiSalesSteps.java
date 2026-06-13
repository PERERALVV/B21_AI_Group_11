package qatraining.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import net.serenitybdd.annotations.Steps;
import qatraining.api.AuthApiActions;
import qatraining.api.SalesApiActions;
import net.serenitybdd.rest.SerenityRest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiSalesSteps {

    @Steps
    private AuthApiActions authApiActions;

    @Steps
    private SalesApiActions salesApiActions;

    private String token;
    private Long plantId;
    private Long saleId;
    private Response lastResponse;

    @Given("I have a valid Admin authorization token")
    public void getAdminToken() {
        token = authApiActions.getJwtToken("admin", "admin123");
        assertThat(token).isNotEmpty();
    }

    @Given("I have a valid User authorization token")
    public void getUserToken() {
        token = authApiActions.getJwtToken("testuser", "test123");
        assertThat(token).isNotEmpty();
    }

    @When("I send a GET request to {string}")
    public void sendGetRequest(String path) {
        if (path.equals("/api/sales")) {
            lastResponse = salesApiActions.getAllSales(token);
        }
    }

    @Then("validate the API response status code should be {int}")
    @Then("the API response status code should be {int}")
    public void verifyStatusCode(int expectedStatus) {
        assertThat(lastResponse.getStatusCode()).isEqualTo(expectedStatus);
    }

    @Then("validate the response body should contain a list of sales records")
    @Then("the response body should contain a list of sales records")
    public void verifySalesList() {
        List<?> salesList = lastResponse.getBody().as(List.class);
        assertThat(salesList).isNotNull();
    }

    @Given("I have a valid plant ID with stock")
    public void fetchValidPlantId() {
        qatraining.api.PreconditionHelper.ensurePreconditionsExist(token);
        
        Response response = SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/plants");
        
        if (response.getStatusCode() == 200) {
            List<Integer> ids = response.jsonPath().getList("id");
            if (ids != null && !ids.isEmpty()) {
                plantId = Long.valueOf(ids.get(0));
                return;
            }
        }
        
        // Final fallback if something failed, but PreconditionHelper ensures this won't happen
        plantId = 1L;
    }

    @When("I send a POST request to {string} with quantity {int}")
    public void sendPostRequestSellPlant(String path, int quantity) {
        lastResponse = salesApiActions.sellPlant(token, plantId, quantity);
    }

    @Then("validate the response body should contain the created sale details")
    @Then("the response body should contain the created sale details")
    public void verifyCreatedSaleDetails() {
        Long id = lastResponse.jsonPath().getLong("id");
        assertThat(id).isNotNull().isGreaterThan(0L);
    }

    @Then("validate the sale record should have quantity {int}")
    @Then("the sale record should have quantity {int}")
    public void verifySaleQuantity(int expectedQty) {
        int qty = lastResponse.jsonPath().getInt("quantity");
        assertThat(qty).isEqualTo(expectedQty);
    }

    @Then("validate the response body should contain the validation error")
    @Then("the response body should contain the validation error")
    public void verifyValidationError() {
        String errorMsg = lastResponse.jsonPath().getString("message");
        if (errorMsg == null) {
            errorMsg = lastResponse.getBody().asString();
        }
        assertThat(errorMsg).isNotEmpty();
    }

    @Given("I have a valid sale ID")
    public void fetchValidSaleId() {
        String adminToken = authApiActions.getJwtToken("admin", "admin123");
        qatraining.api.PreconditionHelper.ensurePreconditionsExist(adminToken);
        
        Response response = salesApiActions.getAllSales(adminToken);
        if (response.getStatusCode() == 200) {
            List<Integer> ids = response.jsonPath().getList("id");
            if (ids != null && !ids.isEmpty()) {
                saleId = Long.valueOf(ids.get(0));
                return;
            }
        }
        
        Long pid = 1L;
        Response plantResponse = SerenityRest.given()
                .header("Authorization", "Bearer " + adminToken)
                .get("/api/plants");
        if (plantResponse.getStatusCode() == 200) {
            List<Integer> ids = plantResponse.jsonPath().getList("id");
            if (ids != null && !ids.isEmpty()) {
                pid = Long.valueOf(ids.get(0));
            }
        }
        
        Response postResponse = salesApiActions.sellPlant(adminToken, pid, 1);
        if (postResponse.getStatusCode() == 201) {
            saleId = postResponse.jsonPath().getLong("id");
        } else {
            saleId = 1L;
        }
    }

    @Given("there is at least one sale record in the database")
    public void ensureAtLeastOneSaleRecordInDb() {
        String adminToken = authApiActions.getJwtToken("admin", "admin123");
        qatraining.api.PreconditionHelper.ensurePreconditionsExist(adminToken);
        
        Response response = salesApiActions.getAllSales(adminToken);
        int currentCount = 0;
        if (response.getStatusCode() == 200) {
            List<?> list = response.getBody().as(List.class);
            if (list != null) {
                currentCount = list.size();
            }
        }
        
        if (currentCount == 0) {
            Long pid = 1L;
            Response plantResponse = SerenityRest.given()
                    .header("Authorization", "Bearer " + adminToken)
                    .get("/api/plants");
            if (plantResponse.getStatusCode() == 200) {
                List<Integer> ids = plantResponse.jsonPath().getList("id");
                if (ids != null && !ids.isEmpty()) {
                    pid = Long.valueOf(ids.get(0));
                }
            }
            salesApiActions.sellPlant(adminToken, pid, 1);
        }
    }

    @When("I send a GET request to {string} for that sale ID")
    public void sendGetRequestForSaleId(String path) {
        lastResponse = salesApiActions.getSaleById(token, saleId);
    }

    @Then("validate the response body should contain the sale details matching the requested sale ID")
    @Then("the response body should contain the sale details matching the requested sale ID")
    public void verifySaleDetailsMatch() {
        Long id = lastResponse.jsonPath().getLong("id");
        assertThat(id).isEqualTo(saleId);
    }

    @Given("I have a valid sale ID to delete")
    public void createSaleToDelete() {
        fetchValidPlantId();
        Response response = salesApiActions.sellPlant(token, plantId, 1);
        assertThat(response.getStatusCode()).isEqualTo(201);
        saleId = response.jsonPath().getLong("id");
    }

    @When("I send a DELETE request to {string} for that sale ID")
    public void sendDeleteRequest(String path) {
        lastResponse = salesApiActions.deleteSale(token, saleId);
    }

    @When("I send a GET request to {string} for the deleted sale ID")
    public void sendGetRequestForDeletedSaleId(String path) {
        lastResponse = salesApiActions.getSaleById(token, saleId);
    }

    @When("I send a GET request to {string} with page {int}, size {int}, sort field {string}, and sort direction {string}")
    public void sendGetRequestPaged(String path, int page, int size, String sortField, String sortDir) {
        lastResponse = salesApiActions.getSalesPaged(token, page, size, sortField, sortDir);
    }

    @Then("validate the response body should contain a paginated Page JSON object")
    @Then("the response body should contain a paginated Page JSON object")
    public void verifyPageObject() {
        assertThat(lastResponse.jsonPath().getMap("")).containsKeys("totalPages", "totalElements", "content");
    }

    @Then("validate the page object should show size {int} and sorted details")
    @Then("the page object should show size {int} and sorted details")
    public void verifyPageSizeAndSorted(int expectedSize) {
        int size = lastResponse.jsonPath().getInt("size");
        assertThat(size).isEqualTo(expectedSize);
        Boolean sorted = lastResponse.jsonPath().getBoolean("pageable.sort.sorted");
        if (sorted != null) {
            assertThat(sorted).isTrue();
        }
    }
}
