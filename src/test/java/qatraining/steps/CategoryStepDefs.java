package qatraining.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.annotations.Steps;

/**
 * Cucumber glue class for the Category module.
 *
 * FIXES in this version:
 *  1. ROOT CAUSE of TC-API-CAT-U03 / U04 / U05 failures:
 *     The step text "the response status is 403" matched TWO definitions –
 *     the parameterised @Then("the response status is {int}") AND the literal
 *     @Then("the response status is 403"). Cucumber throws
 *     AmbiguousStepDefinitionsException for every scenario using that line,
 *     which is exactly the three @User 403 scenarios (3/10 = the 30% failing
 *     in the API report). The literal duplicate has been REMOVED; the {int}
 *     definition handles 200, 400 and 403 alike. ("200 or 201" / "200 or 204"
 *     are kept – they do not collide with {int}.)
 *  2. "Given category with id {int} exists" was an empty no-op. It now really
 *     verifies the precondition via the API and fails with a clear message if
 *     the dataset is missing that id.
 *  3. "Given a deletable category exists in the system" was an empty no-op.
 *     It now creates a fresh leaf category via the API so the list is never
 *     empty when the delete scenario runs.
 *  4. Create scenarios are made repeatable: before creating a category by
 *     name (API or UI), any category with the same name is removed via the
 *     API, so re-running the suite does not fail on duplicates.
 *  5. The UI parent-filter scenario gets its data guaranteed via the API
 *     before filtering (and the list page is re-opened so the dropdown
 *     contains the parent).
 *
 * NOTE: Any @When/@Then text that contains a literal '/' must use a regex
 * string (starting with ^) rather than a Cucumber Expression.
 */
public class CategoryStepDefs {

    @Steps
    ApiCategorySteps apiSteps;

    @Steps
    UiCategorySteps uiSteps;

    // ── State shared between Given/When/Then within one scenario ───────────
    private int storedCategoryId;

    // ═══════════════════════════════════════════════════════════════════════
    // API – Admin steps
    // ═══════════════════════════════════════════════════════════════════════

    @When("^the admin sends GET /api/categories$")
    public void adminGetAllCategories() {
        apiSteps.adminGetAllCategories();
    }

    @When("^the admin sends POST /api/categories with name \"([^\"]*)\"$")
    public void adminCreateCategory(String name) {
        // FIX: make the scenario repeatable – remove any leftover category
        // with the same name from a previous run before creating it again.
        apiSteps.ensureCategoryAbsent(name);
        apiSteps.adminCreateCategory(name);
    }

    @When("^the admin sends POST /api/categories with invalid name \"([^\"]*)\"$")
    public void adminCreateCategoryInvalidName(String name) {
        apiSteps.adminCreateCategoryInvalidName(name);
    }

    @Given("category with id {int} exists")
    public void categoryWithIdExists(int id) {
        // FIX: actually verify the precondition instead of assuming it.
        apiSteps.verifyCategoryExists(id);
    }

    @When("^the admin sends PUT /api/categories/(\\d+) with name \"([^\"]*)\"$")
    public void adminUpdateCategory(int id, String name) {
        apiSteps.adminUpdateCategory(id, name);
    }

    @Given("a test category is created via API")
    public void createTestCategory() {
        storedCategoryId = apiSteps.createTestCategoryAndGetId("TempCat");
    }

    @When("^the admin sends DELETE /api/categories with stored id$")
    public void adminDeleteStoredCategory() {
        apiSteps.adminDeleteCategory(storedCategoryId);
    }

    @Then("validate GET for deleted category returns 404")
    @Then("GET for deleted category returns 404")
    public void verifyDeletedCategoryReturns404() {
        apiSteps.verifyDeletedCategoryReturns404(storedCategoryId);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // API – User steps
    // ═══════════════════════════════════════════════════════════════════════

    @When("^the user sends GET /api/categories$")
    public void userGetAllCategories() {
        apiSteps.userGetAllCategories();
    }

    @When("^the user sends GET /api/categories/(\\d+)$")
    public void userGetCategoryById(int id) {
        apiSteps.userGetCategoryById(id);
    }

    @When("^the user attempts POST /api/categories with name \"([^\"]*)\"$")
    public void userAttemptCreate(String name) {
        apiSteps.userAttemptCreateCategory(name);
    }

    @When("^the user attempts PUT /api/categories/(\\d+) with name \"([^\"]*)\"$")
    public void userAttemptUpdate(int id, String name) {
        apiSteps.userAttemptUpdateCategory(id, name);
    }

    @When("^the user attempts DELETE /api/categories/(\\d+)$")
    public void userAttemptDelete(int id) {
        apiSteps.userAttemptDeleteCategory(id);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // API – Shared assertions
    // FIX: the literal @Then("the response status is 403") was REMOVED – it
    //      was ambiguous with the {int} definition below and broke all three
    //      403 scenarios. The {int} definition now covers 200, 400 and 403.
    // ═══════════════════════════════════════════════════════════════════════

    @Then("validate the response status is {int}")
    @Then("the response status is {int}")
    public void verifyStatusCode(int code) {
        apiSteps.verifyStatusCode(code);
    }

    @Then("validate the response status is 200 or 201")
    @Then("the response status is 200 or 201")
    public void verifyStatusIs200Or201() {
        apiSteps.verifyStatusIsSuccessCreate();
    }

    @Then("validate the response status is 200 or 204")
    @Then("the response status is 200 or 204")
    public void verifyStatusIs200Or204() {
        apiSteps.verifyStatusIsSuccessDelete();
    }

    @Then("validate the response body is a list of categories")
    @Then("the response body is a list of categories")
    public void verifyResponseIsList() {
        apiSteps.verifyResponseIsList();
    }

    @Then("validate the response contains the name {string}")
    @Then("the response contains the name {string}")
    public void verifyResponseContainsName(String name) {
        apiSteps.verifyResponseContainsName(name);
    }

    @Then("validate the response contains an id")
    @Then("the response contains an id")
    public void verifyResponseHasId() {
        apiSteps.verifyResponseHasId();
    }

    @Then("validate the response contains a name length validation error")
    @Then("the response contains a name length validation error")
    public void verifyNameLengthError() {
        apiSteps.verifyNameLengthError();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UI – Navigation
    // ═══════════════════════════════════════════════════════════════════════

    @When("the admin opens the Category List page")
    public void adminOpenCategoryList() {
        uiSteps.openCategoryList();
    }

    @When("the user opens the Category List page")
    public void userOpenCategoryList() {
        uiSteps.openCategoryList();
    }

    @When("the admin opens the Add Category page")
    public void adminOpenAddCategoryPage() {
        uiSteps.openAddCategoryDirectly();
    }

    @When("the user opens the Add Category page")
    public void userOpenAddCategoryPage() {
        uiSteps.openAddCategoryDirectly();
    }

    @When("the admin clicks Add Category button")
    public void clickAddCategory() {
        uiSteps.clickAddCategoryButton();
    }

    @When("the admin enters category name {string}")
    public void adminEnterCategoryName(String name) {
        // FIX: make the UI create scenario repeatable – if the category was
        // created by a previous run, remove it via the API first so saving
        // does not fail with a duplicate-name error.
        apiSteps.ensureCategoryAbsent(name);
        uiSteps.enterCategoryName(name);
    }

    @When("the admin leaves category name empty")
    public void adminLeaveCategoryNameEmpty() {
        uiSteps.leaveCategoryNameEmpty();
    }

    @When("the admin clicks Save")
    public void adminClickSave() {
        uiSteps.clickSave();
    }

    @Given("a parent category {string} with a child exists")
    public void parentCategoryWithChildExists(String parentName) {
        apiSteps.ensureParentWithChildExists(parentName);
    }

    @Given("a deletable category exists in the system")
    public void deletableCategoryExists() {
        // FIX: guarantee the precondition via the API instead of assuming it.
        // A fresh leaf category (no children) is always safe to delete.
        apiSteps.createTestCategoryAndGetId("TmpDel");
    }

    @Given("a category named {string} exists in the system")
    public void categoryNamedExists(String name) {
        apiSteps.ensureCategoryExists(name);
    }

    @When("the admin clicks Delete for row {int}")
    public void clickDeleteForRow(int rowIndex) {
        uiSteps.clickDeleteForRow(rowIndex);
    }

    @When("the admin clicks Delete for {string} category")
    public void adminClickDeleteForCategory(String name) {
        uiSteps.clickDeleteForCategoryNamed(name);
    }

    @When("the user directly navigates to the Edit Category page for category {int}")
    public void userOpenEditCategory(int id) {
        uiSteps.openEditCategoryDirectly(id);
    }

    @When("the admin confirms the deletion")
    public void adminConfirmDeletion() {
        uiSteps.confirmDeletion();
    }

    @When("the user searches for {string}")
    public void searchByName(String name) {
        uiSteps.searchByName(name);
    }

    @When("the user filters by parent {string}")
    public void filterByParent(String parentName) {
        // FIX: guarantee the parent (with at least one child) exists via the
        // API, then re-open the list page so the dropdown is freshly rendered
        // and actually contains that parent option.
        apiSteps.ensureParentWithChildExists(parentName);
        uiSteps.openCategoryList();
        uiSteps.filterByParent(parentName);
    }

    @When("the user clicks sort by Name")
    public void clickSortByName() {
        uiSteps.clickSortByName();
    }

    @When("the user clicks Cancel")
    public void userClickCancel() {
        uiSteps.clickCancel();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UI – Assertions
    // ═══════════════════════════════════════════════════════════════════════

    @Then("validate the category list is displayed with at least one record")
    @Then("the category list is displayed with at least one record")
    public void categoryListDisplayed() {
        uiSteps.verifyCategoryListIsDisplayed();
    }

    @Then("validate the success message is displayed")
    @Then("the success message is displayed")
    public void successMessageDisplayed() {
        uiSteps.verifySuccessMessage();
    }

    @Then("validate the category {string} appears in the list")
    @Then("the category {string} appears in the list")
    public void categoryAppearsInList(String name) {
        uiSteps.verifyCategoryInList(name);
    }

    @Then("validate the required name validation error is shown")
    @Then("the required name validation error is shown")
    public void requiredNameError() {
        uiSteps.verifyRequiredNameError();
    }

    @Then("validate the name length validation error is shown")
    @Then("the name length validation error is shown")
    public void nameLengthError() {
        uiSteps.verifyNameLengthError();
    }

    @Then("validate the Add Category button is NOT visible")
    @Then("the Add Category button is NOT visible")
    public void addButtonNotVisible() {
        uiSteps.verifyAddButtonNotVisible();
    }

    @Then("validate Edit and Delete buttons are hidden")
    @Then("Edit and Delete buttons are hidden")
    public void editDeleteHidden() {
        uiSteps.verifyEditDeleteHidden();
    }

    @Then("validate only categories matching {string} are shown")
    @Then("only categories matching {string} are shown")
    public void onlyMatchingCategoriesShown(String name) {
        uiSteps.verifyOnlyMatchingCategories(name);
    }

    @Then("validate the user is on the Category List page")
    @Then("the user is on the Category List page")
    public void userIsOnCategoryListPage() {
        uiSteps.verifyCategoryListPageIsOpen();
    }

    @Then("validate no confirmation dialog should appear")
    @Then("no confirmation dialog should appear")
    public void noConfirmationDialogShouldAppear() {
        uiSteps.verifyNoConfirmationDialog();
    }

    @Then("validate an error message about sub-categories is shown")
    @Then("an error message about sub-categories is shown")
    public void subCategoryDeletionErrorShown() {
        uiSteps.verifyCannotDeleteParentError();
    }

    @Then("validate the user sees a 403 Access Denied page")
    @Then("the user sees a 403 Access Denied page")
    public void userSees403Page() {
        uiSteps.verify403Page();
    }
}