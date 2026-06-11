package qatraining.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.rest.SerenityRest;

public class SalesApiActions {

    private io.restassured.specification.RequestSpecification givenAuth(String token) {
        return SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON);
    }

    @Step("Get all sales")
    public Response getAllSales(String token) {
        return givenAuth(token)
                .when()
                .get("/api/sales");
    }

    @Step("Sell plant with ID {1} and quantity {2}")
    public Response sellPlant(String token, Long plantId, int quantity) {
        return givenAuth(token)
                .queryParam("quantity", quantity)
                .when()
                .post("/api/sales/plant/" + plantId);
    }

    @Step("Get sale by ID {1}")
    public Response getSaleById(String token, Long saleId) {
        return givenAuth(token)
                .when()
                .get("/api/sales/" + saleId);
    }

    @Step("Delete sale by ID {1}")
    public Response deleteSale(String token, Long saleId) {
        return givenAuth(token)
                .when()
                .delete("/api/sales/" + saleId);
    }

    @Step("Get sales page with parameters: page={1}, size={2}, sortField={3}, sortDir={4}")
    public Response getSalesPaged(String token, int page, int size, String sortField, String sortDir) {
        String sortQuery = sortField + "," + sortDir;
        return givenAuth(token)
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sort", sortQuery)
                .when()
                .get("/api/sales/page");
    }
}
