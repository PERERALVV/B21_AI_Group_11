package qatraining.api;

import io.restassured.http.ContentType;
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
}
