package qatraining.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import net.serenitybdd.annotations.Steps;
import qatraining.api.AuthApiActions;

import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions for the Authentication API tests (T-API-11..T-API-20).
 * Distinct step wording from ApiSalesSteps so Cucumber glue does not collide.
 */
public class ApiAuthSteps {

    @Steps
    private AuthApiActions authApi;

    private String token;
    private Response response;

    @Given("I have an admin API token")
    public void adminToken() {
        token = authApi.getJwtToken("admin", "admin123");
        assertThat(token).isNotEmpty();
    }

    @Given("I have a user API token")
    public void userToken() {
        token = authApi.getJwtToken("testuser", "test123");
        assertThat(token).isNotEmpty();
    }

    @When("I log in to the API with username {string} and password {string}")
    public void loginToApi(String username, String password) {
        response = authApi.login(username, password);
    }

    @When("I send a GET to {string} using the token")
    public void getUsingToken(String path) {
        response = authApi.getWithToken(path, token);
    }

    @When("I send a GET to {string} without a token")
    public void getWithoutToken(String path) {
        response = authApi.getNoAuth(path);
    }

    @When("I send a POST to {string} using the token with category name {string}")
    public void postCategoryUsingToken(String path, String name) {
        response = authApi.postWithToken(path, "{\"name\":\"" + name + "\"}", token);
    }

    @When("I call the logout endpoint with the token")
    public void callLogout() {
        response = authApi.logout(token);
    }

    @Then("the auth response status should be {int}")
    public void statusShouldBe(int expected) {
        assertThat(response.getStatusCode()).isEqualTo(expected);
    }

    @Then("the auth response should contain a token")
    public void shouldContainToken() {
        String t = response.jsonPath().getString("token");
        assertThat(t).isNotNull().isNotEmpty();
    }

    @Then("the auth response should not contain a token")
    public void shouldNotContainToken() {
        String t = response.jsonPath().getString("token");
        assertThat(t).isNull();
    }

    @Then("the token in the response should have role {string}")
    public void tokenShouldHaveRole(String expectedRole) {
        String jwt = response.jsonPath().getString("token");
        assertThat(jwt).as("login response must contain a token to decode").isNotNull();
        assertThat(decodeRoles(jwt)).contains(expectedRole);
    }

    @Then("the token should be rejected for {string}")
    public void tokenRejectedAfterLogout(String path) {
        Response check = authApi.getWithToken(path, token);
        assertThat(check.getStatusCode())
                .as("after logout the session token must be rejected (401)")
                .isEqualTo(401);
    }

    // ── Swagger contract: response-schema assertions ────────────────────────
    @Then("the success response should match the JwtLoginResponse schema")
    public void successMatchesSchema() {
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("token")).isNotNull().isNotEmpty();
        assertThat(response.jsonPath().getString("tokenType")).isEqualTo("Bearer");
    }

    @Then("the error response should match the Swagger ErrorResponse schema")
    public void errorMatchesSchema() {
        JsonPath jp = response.jsonPath();
        assertThat(jp.getString("error")).as("ErrorResponse.error present").isNotNull();
        assertThat(jp.getString("message")).as("ErrorResponse.message present").isNotNull();
        assertThat(jp.getString("timestamp")).as("ErrorResponse.timestamp present (Swagger schema)").isNotNull();
        assertThat(jp.getInt("status")).as("ErrorResponse.status equals the HTTP status code").isEqualTo(response.getStatusCode());
    }

    /** Decode the {@code roles} claim from a JWT payload (middle segment, base64url). */
    private List<String> decodeRoles(String jwt) {
        String payload = jwt.split("\\.")[1];
        byte[] decoded = Base64.getUrlDecoder().decode(payload);
        return new JsonPath(new String(decoded)).getList("roles");
    }
}
