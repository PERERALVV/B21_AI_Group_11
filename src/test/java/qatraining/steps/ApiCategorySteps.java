package qatraining.steps;

import io.restassured.response.Response;
import net.serenitybdd.annotations.Step;
import qatraining.api.CategoryApiActions;
import qatraining.api.AuthApiActions;
import io.cucumber.java.en.Given;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * API Step Library for Category module.
 *
 * FIX (regression repair): the Background previously logged in BOTH accounts
 * eagerly. Because the regular-user credentials are invalid in the target app
 * (POST /api/auth/login returns 401 for 'user'), that single broken account
 * took down all 10 API scenarios, including the admin ones. Authentication is
 * now LAZY again: each role's token is obtained on first use, so a problem
 * with one account only affects the scenarios that genuinely need it — and
 * fails there with a descriptive credential error instead of a silent
 * "Bearer null" request.
 */
public class ApiCategorySteps {

    private final AuthApiActions     authApi;
    private final CategoryApiActions categoryApi;
    private Response lastResponse;

    public ApiCategorySteps() {
        this.authApi = new AuthApiActions();
        this.categoryApi = new CategoryApiActions();
    }

    @Given("the user is authenticated via API")
    public void the_user_is_authenticated_via_api() {
        // Intentionally lazy: tokens are fetched on first use per role.
        // AuthApiActions.getJwtToken() fails loudly with the server response
        // and a credential hint if a login is rejected, so authentication
        // problems are still impossible to miss — but only in the scenarios
        // that actually use the affected account.
    }

    @Step("Authenticate as Admin")
    public void authenticateAsAdmin() {
        authApi.loginAsAdmin();
    }

    @Step("Authenticate as normal User")
    public void authenticateAsUser() {
        authApi.loginAsUser();
    }

    // ── Admin API Steps ────────────────────────────────────────────────────

    @Step("Admin sends GET /api/categories")
    public void adminGetAllCategories() {
        lastResponse = categoryApi.getAllCategories(authApi.getAdminToken());
    }

    @Step("Admin sends POST /api/categories with valid name '{0}'")
    public void adminCreateCategory(String name) {
        lastResponse = categoryApi.createCategory(authApi.getAdminToken(), name, null);
    }

    @Step("Admin sends POST /api/categories with invalid name '{0}'")
    public void adminCreateCategoryInvalidName(String name) {
        lastResponse = categoryApi.createCategory(authApi.getAdminToken(), name, null);
    }

    @Step("Admin sends PUT /api/categories/{0} with name '{1}'")
    public void adminUpdateCategory(int id, String name) {
        lastResponse = categoryApi.updateCategory(authApi.getAdminToken(), id, name);
    }

    @Step("Admin sends DELETE /api/categories/{0}")
    public void adminDeleteCategory(int id) {
        lastResponse = categoryApi.deleteCategory(authApi.getAdminToken(), id);
    }

    @Step("Admin sends GET /api/categories/{0}")
    public Response adminGetCategoryById(int id) {
        return categoryApi.getCategoryById(authApi.getAdminToken(), id);
    }

    // ── User API Steps ─────────────────────────────────────────────────────

    @Step("User sends GET /api/categories")
    public void userGetAllCategories() {
        lastResponse = categoryApi.getAllCategories(authApi.getUserToken());
    }

    @Step("User sends GET /api/categories/{0}")
    public void userGetCategoryById(int id) {
        lastResponse = categoryApi.getCategoryById(authApi.getUserToken(), id);
    }

    @Step("User attempts POST /api/categories – expects 403")
    public void userAttemptCreateCategory(String name) {
        lastResponse = categoryApi.createCategory(authApi.getUserToken(), name, null);
    }

    @Step("User attempts PUT /api/categories/{0} – expects 403")
    public void userAttemptUpdateCategory(int id, String name) {
        lastResponse = categoryApi.updateCategory(authApi.getUserToken(), id, name);
    }

    @Step("User attempts DELETE /api/categories/{0} – expects 403")
    public void userAttemptDeleteCategory(int id) {
        lastResponse = categoryApi.deleteCategory(authApi.getUserToken(), id);
    }

    // ── Assertions ─────────────────────────────────────────────────────────

    @Step("Verify response status code is {0}")
    public void verifyStatusCode(int expected) {
        assertThat(Integer.valueOf(lastResponse.getStatusCode()))
            .as("HTTP response status code")
            .isEqualTo(expected);
    }

    @Step("Verify response status is 200 or 201")
    public void verifyStatusIsSuccessCreate() {
        assertThat(Integer.valueOf(lastResponse.getStatusCode()))
            .as("Status should be 200 or 201")
            .isIn(200, 201);
    }

    @Step("Verify response status is 200 or 204")
    public void verifyStatusIsSuccessDelete() {
        assertThat(Integer.valueOf(lastResponse.getStatusCode()))
            .as("Status should be 200 or 204")
            .isIn(200, 204);
    }

    @Step("Verify response body is a list of categories")
    public void verifyResponseIsList() {
        assertThat(lastResponse.jsonPath().getList("$"))
                .as("Response should be a non-empty list")
                .isNotEmpty();
    }

    @Step("Verify response body contains category name '{0}'")
    public void verifyResponseContainsName(String expected) {
        assertThat(lastResponse.jsonPath().getString("name"))
                .as("Category name in response body")
                .isEqualToIgnoringCase(expected);
    }

    @Step("Verify response body contains an id field")
    public void verifyResponseHasId() {
        assertThat((Object) lastResponse.jsonPath().get("id"))
            .as("Response should contain an id field")
            .isNotNull();
    }

    @Step("Verify response status is 403 Forbidden")
    public void verifyForbidden() {
        assertThat(Integer.valueOf(lastResponse.getStatusCode()))
            .as("User should receive 403 Forbidden")
            .isEqualTo(403);
    }

    @Step("Verify response contains name length validation error")
    public void verifyNameLengthError() {
        assertThat(lastResponse.getBody().asString().toLowerCase())
                .as("Response should mention length constraint")
                .containsAnyOf("between 3 and 10", "length", "size");
    }

    @Step("Verify GET /api/categories/{0} returns 404 after deletion")
    public void verifyDeletedCategoryReturns404(int id) {
        Response check = adminGetCategoryById(id);
        assertThat(Integer.valueOf(check.getStatusCode()))
            .as("Deleted category should return 404")
            .isEqualTo(404);
    }

    @Step("Verify category with id {0} exists")
    public void verifyCategoryExists(int id) {
        Response check = adminGetCategoryById(id);
        assertThat(Integer.valueOf(check.getStatusCode()))
            .as("Precondition: category with id %d must exist in the system", id)
            .isEqualTo(200);
    }

    // ── Test-data utilities (API used for setup/cleanup, not for assertions) ─

    @Step("Create a category via API and return its ID")
    public int createTestCategoryAndGetId(String name) {
        ensureCategoryAbsent(name);
        adminCreateCategory(name);
        verifyStatusIsSuccessCreate();
        return lastResponse.jsonPath().getInt("id");
    }

    /**
     * Ensures a category with the given name exists; creates it if missing.
     * Used as a precondition so search/filter scenarios are self-contained.
     */
    @Step("Ensure a category named '{0}' exists")
    public void ensureCategoryExists(String name) {
        for (Map<String, Object> cat : fetchAllCategories()) {
            Object catName = cat.get("name");
            if (catName != null && catName.toString().equalsIgnoreCase(name)) {
                return;
            }
        }
        Response created = categoryApi.createCategory(authApi.getAdminToken(), name, null);
        assertThat(Integer.valueOf(created.getStatusCode()))
                .as("Setup: creating category '%s'", name)
                .isIn(200, 201);
    }

    /**
     * Deletes every category whose name matches (case-insensitive), so a
     * scenario that creates that category is repeatable. Failed deletions
     * (e.g. parent with children) are ignored – the create step will then
     * report the real problem.
     */
    @Step("Ensure no category named '{0}' exists (test-data cleanup)")
    public void ensureCategoryAbsent(String name) {
        for (Map<String, Object> cat : fetchAllCategories()) {
            Object catName = cat.get("name");
            Integer id = asInt(cat.get("id"));
            if (catName != null && id != null
                    && catName.toString().equalsIgnoreCase(name)) {
                categoryApi.deleteCategory(authApi.getAdminToken(), id);
            }
        }
    }

    /**
     * Guarantees a parent category with the given name exists AND has at least
     * one child, so the UI parent-filter scenario does not depend on leftover
     * seed data (e.g. when another scenario has renamed the original parent).
     */
    @Step("Ensure parent category '{0}' with at least one child exists")
    public void ensureParentWithChildExists(String parentName) {
        List<Map<String, Object>> all = fetchAllCategories();

        Integer parentId = null;
        for (Map<String, Object> cat : all) {
            Object catName = cat.get("name");
            if (catName != null && catName.toString().equalsIgnoreCase(parentName)) {
                parentId = asInt(cat.get("id"));
                break;
            }
        }

        if (parentId == null) {
            Response created = categoryApi.createCategory(
                    authApi.getAdminToken(), parentName, null);
            assertThat(Integer.valueOf(created.getStatusCode()))
                    .as("Setup: creating parent category '%s'", parentName)
                    .isIn(200, 201);
            parentId = created.jsonPath().getInt("id");
        }

        boolean hasChild = false;
        for (Map<String, Object> cat : all) {
            Integer pid = extractParentId(cat);
            if (pid != null && pid.equals(parentId)) {
                hasChild = true;
                break;
            }
        }

        if (!hasChild) {
            Response child = categoryApi.createCategory(
                    authApi.getAdminToken(), "Lilies", parentId);
            assertThat(Integer.valueOf(child.getStatusCode()))
                    .as("Setup: creating a child under parent '%s'", parentName)
                    .isIn(200, 201);
        }
    }

    private List<Map<String, Object>> fetchAllCategories() {
        Response all = categoryApi.getAllCategories(authApi.getAdminToken());
        assertThat(Integer.valueOf(all.getStatusCode()))
                .as("Setup: GET /api/categories must succeed")
                .isEqualTo(200);
        List<Map<String, Object>> list = all.jsonPath().getList("$");
        return list == null ? List.of() : list;
    }

    /** Tolerates both flat {parentId: n} and nested {parent: {id: n}} shapes. */
    private Integer extractParentId(Map<String, Object> cat) {
        Integer pid = asInt(cat.get("parentId"));
        if (pid != null) {
            return pid;
        }
        Object parent = cat.get("parent");
        if (parent instanceof Map) {
            return asInt(((Map<?, ?>) parent).get("id"));
        }
        return null;
    }

    private Integer asInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return value == null ? null : Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}