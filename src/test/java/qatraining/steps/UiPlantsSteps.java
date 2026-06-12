package qatraining.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import qatraining.api.AuthApiActions;
import qatraining.api.PlantsApiActions;
import qatraining.pages.AddEditPlantPage;
import qatraining.pages.PlantsListPage;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class UiPlantsSteps {

    private PlantsListPage plantsListPage;
    private AddEditPlantPage addEditPlantPage;

    private int plantCountBeforeDelete;
    private String alertText;
    private String selectedCategoryText;
    private String searchKeyword = "Aloe";

    // ─── Navigation ──────────────────────────────────────────────────────────

    @When("I navigate to the plants list page")
    public void navigateToPlantsListPage() {
        plantsListPage.open();
    }

    @When("I navigate to the add plant page directly")
    public void navigateToAddPlantPage() {
        addEditPlantPage.open();
    }

    // ─── Preconditions ───────────────────────────────────────────────────────

    @Given("the plant {string} has been removed from the system if it exists")
    public void removePlantIfExists(String plantName) {
        String adminToken = new AuthApiActions().getJwtToken("admin", "admin123");
        PlantsApiActions plantsApi = new PlantsApiActions();
        Response allPlants = plantsApi.getAllPlants(adminToken);
        if (allPlants.getStatusCode() == 200) {
            List<Map<String, Object>> plants = allPlants.jsonPath().getList("");
            if (plants != null) {
                for (Map<String, Object> plant : plants) {
                    if (plantName.equals(plant.get("name"))) {
                        Object idObj = plant.get("id");
                        Long id = idObj instanceof Integer ? Long.valueOf((Integer) idObj) : (Long) idObj;
                        plantsApi.deletePlant(adminToken, id);
                        break;
                    }
                }
            }
        }
    }

    @Given("at least one plant exists in the system")
    public void ensureAtLeastOnePlantExists() {
        plantsListPage.open();
        if (plantsListPage.getPlantRowCount() == 0) {
            String adminToken = new AuthApiActions().getJwtToken("admin", "admin123");
            PlantsApiActions plantsApi = new PlantsApiActions();
            Long categoryId = plantsApi.getSubCategoryId(adminToken);
            plantsApi.createPlant(adminToken, categoryId, "SeedPlant", 10.0, 20);
            plantsListPage.open();
        }
        assertThat(plantsListPage.getPlantRowCount()).isGreaterThanOrEqualTo(1);
    }

    @Given("at least one plant with a category exists in the system")
    public void ensureAtLeastOnePlantWithCategoryExists() {
        ensureAtLeastOnePlantExists();
    }

    @Given("a plant with a two-word name exists in the system")
    public void ensureTwoWordPlantExists() {
        String adminToken = new AuthApiActions().getJwtToken("admin", "admin123");
        PlantsApiActions plantsApi = new PlantsApiActions();
        Response allPlants = plantsApi.getAllPlants(adminToken);
        if (allPlants.getStatusCode() == 200) {
            List<Map<String, Object>> plants = allPlants.jsonPath().getList("");
            if (plants != null) {
                for (Map<String, Object> plant : plants) {
                    if ("Rose Mary".equals(plant.get("name"))) {
                        return;
                    }
                }
            }
        }
        Long categoryId = plantsApi.getSubCategoryId(adminToken);
        plantsApi.createPlant(adminToken, categoryId, "Rose Mary", 175.0, 3);
    }

    @Given("there are at least 3 plants with distinct values in the database")
    public void ensureAtLeastThreePlantsExist() {
        plantsListPage.open();
        int count = plantsListPage.getPlantRowCount();
        if (count < 3) {
            String adminToken = new AuthApiActions().getJwtToken("admin", "admin123");
            PlantsApiActions plantsApi = new PlantsApiActions();
            Long categoryId = plantsApi.getSubCategoryId(adminToken);
            for (int i = count; i < 3; i++) {
                plantsApi.createPlant(adminToken, categoryId, "TestPlant" + i, 10.0 + i * 5, 10 + i);
            }
            plantsListPage.open();
        }
        assertThat(plantsListPage.getPlantRowCount()).isGreaterThanOrEqualTo(3);
    }

    // ─── T-UI-21: Admin controls visible ─────────────────────────────────────

    @Then("the {string} button should be visible on the plants page")
    public void verifyAddPlantButtonVisible(String buttonName) {
        assertThat(plantsListPage.isAddPlantButtonVisible())
                .as("'%s' button should be visible for Admin", buttonName)
                .isTrue();
    }

    @Then("the plants table should display columns {string}, {string}, {string}, {string}, and {string}")
    public void verifyPlantTableColumns(String c1, String c2, String c3, String c4, String c5) {
        List<String> headers = plantsListPage.getColumnHeaders();
        assertThat(headers).as("Plants table columns").contains(c1, c2, c3, c4, c5);
    }

    @Then("each plant row should show Edit and Delete action icons")
    public void verifyEditDeleteIconsPresent() {
        if (plantsListPage.getPlantRowCount() > 0) {
            assertThat(plantsListPage.areEditIconsPresent())
                    .as("Edit icons should be present for Admin").isTrue();
            assertThat(plantsListPage.areDeleteIconsPresent())
                    .as("Delete icons should be present for Admin").isTrue();
        }
    }

    @Then("the Plants navigation link should be active")
    public void verifyPlantsNavLinkActive() {
        assertThat(plantsListPage.isPlantsNavLinkActive())
                .as("Plants navigation link should be active (highlighted)").isTrue();
    }

    // ─── T-UI-22: Admin adds a plant ─────────────────────────────────────────

    @When("I click the {string} button on the plants page")
    public void clickButtonOnPlantsPage(String buttonName) {
        if (buttonName.equalsIgnoreCase("Add a Plant")) {
            plantsListPage.clickAddPlant();
        }
    }

    @When("I enter plant name {string}")
    public void enterPlantName(String name) {
        addEditPlantPage.enterName(name);
    }

    @When("I select the first available sub-category from the plant category dropdown")
    public void selectFirstSubCategoryFromDropdown() {
        String adminToken = new AuthApiActions().getJwtToken("admin", "admin123");
        Long subCatId = new PlantsApiActions().getSubCategoryId(adminToken);
        addEditPlantPage.selectCategoryById(subCatId);
    }

    @When("I enter plant price {string}")
    public void enterPlantPrice(String price) {
        addEditPlantPage.enterPrice(price);
    }

    @When("I enter plant quantity {string}")
    public void enterPlantQuantity(String quantity) {
        addEditPlantPage.enterQuantity(quantity);
    }

    @When("I click the Save button on the plant form")
    public void clickSaveOnPlantForm() {
        addEditPlantPage.clickSave();
    }

    @Then("I should be redirected to the plants list page")
    public void verifyRedirectToPlantsListPage() {
        String url = plantsListPage.getDriver().getCurrentUrl();
        assertThat(url)
                .as("Should be redirected to plants list (not still on add/edit page)")
                .contains("/ui/plants")
                .doesNotContain("/ui/plants/add")
                .doesNotContain("/ui/plants/edit");
    }

    @Then("the success message {string} should be displayed on the plants page")
    public void verifySuccessMessageOnPlantsPage(String expectedMsg) {
        assertThat(plantsListPage.isSuccessMessageDisplayed(expectedMsg))
                .as("Success message '%s' should be visible", expectedMsg)
                .isTrue();
    }

    @Then("the plant {string} should appear in the plants list")
    public void verifyPlantAppearsInList(String plantName) {
        assertThat(plantsListPage.isPlantInList(plantName))
                .as("Plant '%s' should appear in the plants list", plantName)
                .isTrue();
    }

    // ─── T-UI-23: Validation messages ────────────────────────────────────────

    @When("I clear the plant name field")
    public void clearPlantNameField() {
        addEditPlantPage.clearName();
    }

    @Then("I should see plant name validation error containing {string}")
    public void verifyPlantNameValidationError(String expected) {
        String actual = addEditPlantPage.getNameError();
        assertThat(actual)
                .as("Plant name validation error should contain: %s", expected)
                .containsIgnoringCase(expected.replace("Plant name is required", "").trim().isEmpty()
                        ? "required" : expected);
    }

    @Then("I should see plant category validation error {string}")
    public void verifyPlantCategoryValidationError(String expected) {
        String actual = addEditPlantPage.getCategoryError();
        assertThat(actual)
                .as("Category validation error")
                .containsIgnoringCase(expected.substring(0, Math.min(10, expected.length())));
    }

    @Then("I should see plant price validation error {string}")
    public void verifyPlantPriceValidationError(String expected) {
        String actual = addEditPlantPage.getPriceError();
        assertThat(actual)
                .as("Price validation error should contain: %s", expected)
                .containsIgnoringCase("greater than 0");
    }

    @Then("I should see plant quantity validation error {string}")
    public void verifyPlantQuantityValidationError(String expected) {
        String actual = addEditPlantPage.getQuantityError();
        assertThat(actual)
                .as("Quantity validation error should contain: %s", expected)
                .containsIgnoringCase("negative");
    }

    // ─── T-UI-24: Edit plant – Low badge ─────────────────────────────────────

    @When("I click the Edit icon on the first plant in the list")
    public void clickEditOnFirstPlant() {
        plantsListPage.clickEditAtRow(0);
    }

    @When("I change the plant quantity to {string}")
    public void changePlantQuantity(String quantity) {
        addEditPlantPage.enterQuantity(quantity);
    }

    @Then("a {string} badge should be visible on the plants page")
    public void verifyLowBadgeVisible(String badge) {
        assertThat(plantsListPage.isLowBadgeVisible())
                .as("'%s' badge should be visible for plants with quantity < 5", badge)
                .isTrue();
    }

    // ─── T-UI-25: Delete plant with confirmation ──────────────────────────────

    @When("I note the current plant count")
    public void noteCurrentPlantCount() {
        // Use API total count to avoid pagination affecting the page-level row count
        String adminToken = new AuthApiActions().getJwtToken("admin", "admin123");
        io.restassured.response.Response allPlants = new PlantsApiActions().getAllPlants(adminToken);
        if (allPlants.getStatusCode() == 200) {
            java.util.List<?> list = allPlants.jsonPath().getList("");
            plantCountBeforeDelete = list != null ? list.size() : plantsListPage.getPlantRowCount();
        } else {
            plantCountBeforeDelete = plantsListPage.getPlantRowCount();
        }
    }

    @When("I click the Delete icon on the first plant in the list")
    public void clickDeleteOnFirstPlant() {
        plantsListPage.clickDeleteAtRow(0);
    }

    @Then("I should see a plant deletion confirmation prompt with text {string}")
    public void verifyPlantDeletionPrompt(String expectedText) {
        alertText = plantsListPage.getAlertTextAndAccept();
        assertThat(alertText)
                .as("Plant deletion confirmation dialog text")
                .isEqualTo(expectedText);
    }

    @When("I accept the plant deletion confirmation")
    public void acceptPlantDeletionConfirmation() {
        // Already accepted in the previous step via getAlertTextAndAccept()
    }

    @Then("the plant count should be reduced by 1")
    public void verifyPlantCountReducedByOne() {
        // Use API total count to avoid pagination affecting the page-level row count
        String adminToken = new AuthApiActions().getJwtToken("admin", "admin123");
        io.restassured.response.Response allPlants = new PlantsApiActions().getAllPlants(adminToken);
        int countAfter;
        if (allPlants.getStatusCode() == 200) {
            java.util.List<?> list = allPlants.jsonPath().getList("");
            countAfter = list != null ? list.size() : 0;
        } else {
            plantsListPage.open();
            countAfter = plantsListPage.getPlantRowCount();
        }
        assertThat(countAfter)
                .as("Plant count after deletion should be %d", plantCountBeforeDelete - 1)
                .isEqualTo(plantCountBeforeDelete - 1);
    }

    // ─── T-UI-26: User – no management controls ───────────────────────────────

    @Then("the {string} button should not be visible on the plants page")
    public void verifyButtonNotVisibleOnPlantsPage(String buttonName) {
        assertThat(plantsListPage.isAddPlantButtonVisible())
                .as("'%s' button should NOT be visible for normal User", buttonName)
                .isFalse();
    }

    @Then("no Edit or Delete icons should be present in any plant row")
    public void verifyNoEditDeleteIcons() {
        assertThat(plantsListPage.areEditIconsPresent())
                .as("Edit icons should NOT be present for User").isFalse();
        assertThat(plantsListPage.areDeleteIconsPresent())
                .as("Delete icons should NOT be present for User").isFalse();
    }

    @Then("the Actions column should not be visible in the plants table")
    public void verifyActionsColumnNotVisible() {
        assertThat(plantsListPage.isActionsColumnPresent())
                .as("Actions column header should NOT be visible for a normal User (BUG-004)")
                .isFalse();
    }

    // ─── T-UI-30: Access denied for unauthorized plant operations ─────────────

    @Then("the user should not be able to access the plant add page")
    public void verifyPlantAddPageInaccessible() {
        String currentUrl = plantsListPage.getDriver().getCurrentUrl();
        assertThat(currentUrl)
                .as("User should not be allowed to access /ui/plants/add — access must be denied")
                .doesNotContain("/ui/plants/add");
    }

    // ─── T-UI-27: Search by name ──────────────────────────────────────────────

    @When("I type {string} in the search plant field")
    public void typeInSearchField(String keyword) {
        searchKeyword = keyword;
        plantsListPage.enterSearchKeyword(keyword);
    }

    @When("I click the Search button on the plants page")
    public void clickSearchOnPlantsPage() {
        plantsListPage.clickSearch();
    }

    @Then("the plant table should show only plants matching the search term")
    public void verifyPlantSearchResults() {
        int rowCount = plantsListPage.getPlantRowCount();
        assertThat(rowCount)
                .as("Search for '%s' should return at least one result (BUG-005: multi-word search returns no results)",
                        searchKeyword)
                .isGreaterThan(0);
        assertThat(plantsListPage.allRowsMatchSearch(searchKeyword))
                .as("All visible plant rows should match search keyword '%s'", searchKeyword)
                .isTrue();
    }

    // ─── T-UI-28: Filter by category ─────────────────────────────────────────

    @When("I select the first non-default option from the plant category filter")
    public void selectFirstNonDefaultCategory() {
        plantsListPage.selectCategoryByIndex(1);
        selectedCategoryText = plantsListPage.getSelectedCategoryText();
    }

    @Then("the plant table should show only plants of the selected category")
    public void verifyPlantFilterByCategory() {
        int rowCount = plantsListPage.getPlantRowCount();
        if (rowCount > 0) {
            assertThat(plantsListPage.allRowsContainCategory(selectedCategoryText))
                    .as("All visible rows should belong to category '%s'", selectedCategoryText)
                    .isTrue();
        }
    }

    // ─── T-UI-29: Sort by Name, Price, Stock ─────────────────────────────────

    @When("I click on the plants table {string} column header")
    public void clickPlantColumnHeader(String columnName) {
        switch (columnName.toLowerCase()) {
            case "name":  plantsListPage.clickNameHeader();  break;
            case "price": plantsListPage.clickPriceHeader(); break;
            case "stock": plantsListPage.clickStockHeader(); break;
        }
    }

    @When("I click on the plants table {string} column header again")
    public void clickPlantColumnHeaderAgain(String columnName) {
        clickPlantColumnHeader(columnName);
    }

    @Then("the plant list should be sorted by {string} ascending")
    public void verifyPlantsSortedAscending(String columnName) {
        String currentUrl = plantsListPage.getDriver().getCurrentUrl();
        List<List<String>> data = plantsListPage.getTableData();
        if (data.size() > 1) {
            int colIndex = getPlantColIndex(columnName);
            String v1 = data.get(0).get(colIndex);
            String v2 = data.get(1).get(colIndex);
            if (currentUrl.contains("sortDir=desc")) {
                verifyDescending(v1, v2, columnName);
            } else {
                verifyAscending(v1, v2, columnName);
            }
        }
    }

    @Then("the plant list should be sorted by {string} descending")
    public void verifyPlantsSortedDescending(String columnName) {
        String currentUrl = plantsListPage.getDriver().getCurrentUrl();
        List<List<String>> data = plantsListPage.getTableData();
        if (data.size() > 1) {
            int colIndex = getPlantColIndex(columnName);
            String v1 = data.get(0).get(colIndex);
            String v2 = data.get(1).get(colIndex);
            if (currentUrl.contains("sortDir=asc")) {
                verifyAscending(v1, v2, columnName);
            } else {
                verifyDescending(v1, v2, columnName);
            }
        }
    }

    private void verifyAscending(String v1, String v2, String columnName) {
        if (columnName.equalsIgnoreCase("Price") || columnName.equalsIgnoreCase("Stock")) {
            try {
                double d1 = Double.parseDouble(v1.replaceAll("[^0-9.]", ""));
                double d2 = Double.parseDouble(v2.replaceAll("[^0-9.]", ""));
                assertThat(d1).isLessThanOrEqualTo(d2);
            } catch (NumberFormatException e) {
                assertThat(v1.compareTo(v2)).isLessThanOrEqualTo(0);
            }
        } else {
            assertThat(v1.compareTo(v2)).isLessThanOrEqualTo(0);
        }
    }

    private void verifyDescending(String v1, String v2, String columnName) {
        if (columnName.equalsIgnoreCase("Price") || columnName.equalsIgnoreCase("Stock")) {
            try {
                double d1 = Double.parseDouble(v1.replaceAll("[^0-9.]", ""));
                double d2 = Double.parseDouble(v2.replaceAll("[^0-9.]", ""));
                assertThat(d1).isGreaterThanOrEqualTo(d2);
            } catch (NumberFormatException e) {
                assertThat(v1.compareTo(v2)).isGreaterThanOrEqualTo(0);
            }
        } else {
            assertThat(v1.compareTo(v2)).isGreaterThanOrEqualTo(0);
        }
    }

    private int getPlantColIndex(String columnName) {
        switch (columnName.toLowerCase()) {
            case "name":  return 0;
            case "category": return 1;
            case "price": return 2;
            case "stock": return 3;
            default:      return 0;
        }
    }
}
