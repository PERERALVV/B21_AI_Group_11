package qatraining.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.rest.SerenityRest;
import java.util.HashMap;
import java.util.Map;

public class AuthApiActions {

    @Step("Get JWT Token for user '{0}'")
    public String getJwtToken(String username, String password) {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", password);

        return SerenityRest.given()
                .contentType(ContentType.JSON)
                .body(credentials)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    @Step("POST /api/auth/login as '{0}' (raw response, no status assertion)")
    public Response login(String username, String password) {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", password);

        return SerenityRest.given()
                .contentType(ContentType.JSON)
                .body(credentials)
                .when()
                .post("/api/auth/login");
    }

    @Step("POST /api/auth/logout with bearer token")
    public Response logout(String token) {
        return SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .when()
                .post("/api/auth/logout");
    }

    @Step("GET {0} with bearer token")
    public Response getWithToken(String path, String token) {
        return SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get(path);
    }

    @Step("GET {0} without authentication")
    public Response getNoAuth(String path) {
        return SerenityRest.given()
                .when()
                .get(path);
    }

    @Step("POST {0} with bearer token")
    public Response postWithToken(String path, String jsonBody, String token) {
        return SerenityRest.given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post(path);
    }
}
