package qatraining.api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.rest.SerenityRest;

import java.util.HashMap;
import java.util.Map;

public class AuthApiActions {

    private static final String BASE_URL  = "http://localhost:8080";
    private static final String LOGIN_URL = BASE_URL + "/api/auth/login";

    // ── Credentials (kept inline) ───────────────────────────────────────────
    // The latest run PROVED that admin/admin123 logs in successfully but the
    // regular-user login below is rejected with 401 by POST /api/auth/login.
    // Find the real seeded regular-user account (check the app's seed data /
    // README, or test with:
    //   curl -i -X POST http://localhost:8080/api/auth/login \
    //        -H "Content-Type: application/json" \
    //        -d "{\"username\":\"user\",\"password\":\"user123\"}"
    // ) and update USER_USERNAME / USER_PASSWORD here.
    //
    // NOTE: UiCategorySteps has its own copy of these constants for the UI
    // login — if you change them here, change them there too.
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin123";
    public static final String USER_USERNAME  = "testuser";
    public static final String USER_PASSWORD  = "test123";

    /**
     * TOKEN PATH — verified working: the admin login extracts the JWT from
     * "token" successfully (admin-authenticated scenarios pass). The fallbacks
     * keep the extraction robust and guarantee a loud, descriptive failure
     * instead of silently sending "Bearer null".
     */
    private static final String[] TOKEN_JSON_PATHS = {
            "token", "accessToken", "access_token", "jwt", "id_token", "data.token"
    };

    @Step("Get JWT Token for user '{0}'")
    public String getJwtToken(String username, String password) {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", password);

        Response response = SerenityRest.given()
                .contentType(ContentType.JSON)
                .body(credentials)
                .when()
                .post(LOGIN_URL);

        if (response.getStatusCode() != 200) {
            throw new IllegalStateException(String.format(
                    "Login failed for user '%s' – POST %s returned %d. Body: %s%n"
                    + "=> The credentials for this account are likely wrong. "
                    + "Verify the seeded account and update the constants at the "
                    + "top of AuthApiActions (and UiCategorySteps).",
                    username, LOGIN_URL, response.getStatusCode(),
                    response.getBody().asString()));
        }

        for (String path : TOKEN_JSON_PATHS) {
            try {
                String token = response.jsonPath().getString(path);
                if (token != null && !token.trim().isEmpty()) {
                    return token;
                }
            } catch (Exception ignored) {
                // path not present in this response shape – try the next one
            }
        }

        throw new IllegalStateException(String.format(
                "Login for user '%s' returned 200 but no JWT was found under any of "
                + "the expected JSON paths %s. Actual body: %s",
                username, String.join(", ", TOKEN_JSON_PATHS),
                response.getBody().asString()));
    }

    private String adminToken;
    private String userToken;

    @Step("Login as admin")
    public void loginAsAdmin() {
        adminToken = getJwtToken(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    @Step("Login as user")
    public void loginAsUser() {
        userToken = getJwtToken(USER_USERNAME, USER_PASSWORD);
    }

    public String getAdminToken() {
        if (adminToken == null) {
            loginAsAdmin();
        }
        return adminToken;
    }

    public String getUserToken() {
        if (userToken == null) {
            loginAsUser();
        }
        return userToken;
    }
}