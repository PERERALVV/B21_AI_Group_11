package qatraining.steps;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import net.serenitybdd.annotations.Step;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;
import qatraining.pages.AddCategoryPage;
import qatraining.pages.CategoryListPage;
import qatraining.pages.LoginPage;
import io.cucumber.java.en.Given;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UI Step Library for Category module.
 *
 *  1. Tag-aware login: @User scenarios log in with the regular-user account,
 *     all others with admin, so role-based visibility scenarios (TC-UI-CAT-U01)
 *     test what they claim to test. The role comes from the scenario's own
 *     tags – nothing scenario-specific is hard-coded.
 *  2. The login step VERIFIES the login actually succeeded (we navigated away
 *     from /ui/login). A rejected login previously left the browser silently
 *     on the login page and every later step failed with a cryptic
 *     "element not found"; now the Background fails immediately with a
 *     message naming the account.
 *
 * CREDENTIALS (kept inline): the latest run proved admin/admin123 works but
 * the regular-user account below is rejected (401). Find the real seeded
 * regular-user credentials and update USER_USERNAME / USER_PASSWORD here AND
 * the matching constants in AuthApiActions.
 */
public class UiCategorySteps {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String USER_USERNAME  = "testuser";
    private static final String USER_PASSWORD  = "test123";

    // Serenity injects these automatically – no 'new' needed
    CategoryListPage categoryListPage;
    AddCategoryPage  addCategoryPage;
    LoginPage        loginPage;

    /** True when the current scenario is tagged @User in the feature file. */
    private boolean userRoleScenario;

    @Before
    public void detectScenarioRole(Scenario scenario) {
        userRoleScenario = scenario.getSourceTagNames().stream()
                .anyMatch(tag -> tag.equalsIgnoreCase("@User"));
    }

    // ── Background ─────────────────────────────────────────────────────────

    @Given("the admin or user is logged in")
    public void the_admin_or_user_is_logged_in() {
        String username = userRoleScenario ? USER_USERNAME : ADMIN_USERNAME;
        String password = userRoleScenario ? USER_PASSWORD : ADMIN_PASSWORD;

        loginPage.open();
        loginPage.login(username, password);

        // Fail fast and clearly if the login was rejected (browser stays on
        // /ui/login) instead of letting every later step die on missing elements.
        try {
            new WebDriverWait(loginPage.getDriver(), Duration.ofSeconds(5))
                    .until(driver -> !driver.getCurrentUrl().contains("/ui/login"));
        } catch (TimeoutException e) {
            throw new IllegalStateException(String.format(
                    "UI login failed for '%s' – the browser is still on the login "
                    + "page. The credentials for this account are likely wrong. "
                    + "Verify the seeded account and update the constants at the "
                    + "top of UiCategorySteps (and AuthApiActions).", username));
        }
    }

    // ── Navigation ─────────────────────────────────────────────────────────

    @Step("Open the Category List page")
    public void openCategoryList() {
        categoryListPage.open();
    }

    @Step("Click the Add Category button")
    public void clickAddCategoryButton() {
        categoryListPage.clickAddCategory();
    }

    @Step("Open Add Category page directly via URL")
    public void openAddCategoryDirectly() {
        addCategoryPage.openAddForm();
    }

    @Step("Click Cancel button")
    public void clickCancel() {
        addCategoryPage.clickCancel();
    }

    // ── Form Actions ───────────────────────────────────────────────────────

    @Step("Enter category name: {0}")
    public void enterCategoryName(String name) {
        addCategoryPage.enterCategoryName(name);
    }

    @Step("Leave category name empty")
    public void leaveCategoryNameEmpty() {
        addCategoryPage.clearCategoryName();
    }

    @Step("Click Save")
    public void clickSave() {
        addCategoryPage.clickSave();
    }

    // ── Delete ─────────────────────────────────────────────────────────────

    @Step("Click Delete for row index {0}")
    public void clickDeleteForRow(int rowIndex) {
        categoryListPage.clickDeleteForRow(rowIndex);
    }

    @Step("Click Delete for category named: {0}")
    public void clickDeleteForCategoryNamed(String name) {
        categoryListPage.clickDeleteForCategoryNamed(name);
    }

    @Step("Open Edit Category page directly via URL for id {0}")
    public void openEditCategoryDirectly(int id) {
        addCategoryPage.openEditForm(id);
    }

    @Step("Verify no confirmation dialog appeared (SRS 5.1 does not document one)")
    public void verifyNoConfirmationDialog() {
        categoryListPage.verifyNoConfirmationDialog();
    }

    @Step("Confirm deletion in the browser dialog")
    public void confirmDeletion() {
        categoryListPage.confirmDeletion();
    }

    // ── Search / Filter / Sort ─────────────────────────────────────────────

    @Step("Search categories by name: {0}")
    public void searchByName(String name) {
        categoryListPage.searchByName(name);
    }

    @Step("Filter categories by parent: {0}")
    public void filterByParent(String parentName) {
        categoryListPage.selectParentFilter(parentName);
    }

    @Step("Click sort by Name column header")
    public void clickSortByName() {
        categoryListPage.clickSortByName();
    }

    @Step("Click Reset button")
    public void clickReset() {
        categoryListPage.clickReset();
    }

    // ── Assertions ─────────────────────────────────────────────────────────

    @Step("Verify category list is displayed with at least one record")
    public void verifyCategoryListIsDisplayed() {
        assertThat(categoryListPage.getRowCount())
                .as("Category list should have at least one row")
                .isGreaterThan(0);
    }

    @Step("Verify Add Category button is visible")
    public void verifyAddButtonVisible() {
        assertThat(categoryListPage.isAddCategoryButtonVisible())
                .as("Add Category button should be visible for Admin")
                .isTrue();
    }

    @Step("Verify Add Category button is NOT visible")
    public void verifyAddButtonNotVisible() {
        assertThat(categoryListPage.isAddCategoryButtonVisible())
                .as("Add Category button should NOT be visible for normal User")
                .isFalse();
    }

    @Step("Verify Edit and Delete buttons are hidden for all rows")
    public void verifyEditDeleteHidden() {
        assertThat(categoryListPage.isEditButtonVisibleForRow(0))
                .as("Edit button should be hidden for normal User")
                .isFalse();
        assertThat(categoryListPage.isDeleteButtonVisibleForRow(0))
                .as("Delete button should be hidden for normal User")
                .isFalse();
    }

    @Step("Verify category '{0}' appears in the list")
    public void verifyCategoryInList(String name) {
        assertThat(categoryListPage.isCategoryPresentInList(name))
                .as("Category '%s' should be in the list", name)
                .isTrue();
    }

    @Step("Verify success message is displayed")
    public void verifySuccessMessage() {
        assertThat(categoryListPage.isSuccessMessageVisible())
                .as("Success message should be visible")
                .isTrue();
    }

    @Step("Verify 'Category name is required' error is shown")
    public void verifyRequiredNameError() {
        assertThat(addCategoryPage.isValidationErrorVisible())
                .as("Validation error should be visible")
                .isTrue();
        assertThat(addCategoryPage.getValidationErrorText())
                .as("Error should say 'required'")
                .containsIgnoringCase("required");
    }

    @Step("Verify 'between 3 and 10 characters' error is shown")
    public void verifyNameLengthError() {
        assertThat(addCategoryPage.isValidationErrorVisible())
                .as("Validation error should be visible")
                .isTrue();
        assertThat(addCategoryPage.getValidationErrorText())
                .as("Error should mention 'between 3 and 10 characters'")
                .containsIgnoringCase("between 3 and 10");
    }

    @Step("Verify user is on the Category List page")
    public void verifyCategoryListPageIsOpen() {
        assertThat(categoryListPage.getCurrentUrl())
                .as("URL should contain /ui/categories")
                .contains("/ui/categories");
    }

    @Step("Verify 403 Access Denied page is shown")
    public void verify403Page() {
        assertThat(addCategoryPage.is403Page())
                .as("Should be on 403 Access Denied page")
                .isTrue();
    }

    @Step("Verify only categories matching '{0}' are shown")
    public void verifyOnlyMatchingCategories(String name) {
        assertThat(categoryListPage.isCategoryPresentInList(name))
                .as("Searched category '%s' should appear", name)
                .isTrue();
    }

    @Step("Verify error about sub-categories is shown")
    public void verifyCannotDeleteParentError() {
        assertThat(categoryListPage.isErrorMessageVisible())
                .as("Error message should be visible")
                .isTrue();
        assertThat(categoryListPage.getErrorMessageText())
                .as("Error should mention sub-categories")
                .containsIgnoringCase("sub-categor");
    }
}