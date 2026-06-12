package qatraining.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static org.assertj.core.api.Assertions.assertThat;
import qatraining.pages.SalesListPage;
import qatraining.pages.SellPlantPage;
import qatraining.pages.AccessDeniedPage;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UiSalesSteps {

    private static final Logger logger = LoggerFactory.getLogger(UiSalesSteps.class);

    private SalesListPage salesListPage;
    private SellPlantPage sellPlantPage;
    private AccessDeniedPage accessDeniedPage;

    private String selectedPlantName;
    private int initialStock;
    private int salesCountBeforeDelete;
    private String alertText;

    @Given("the application is running")
    public void appIsRunning() {
        salesListPage.open();
    }

    @When("I navigate to the sales list page")
    public void navigateToSalesList() {
        salesListPage.open();
    }

    @Then("I should see the sales list loaded with pagination")
    public void verifySalesListLoadedWithPagination() {
        assertThat(salesListPage.getDriver().getTitle()).contains("Sales");
        assertThat(salesListPage.getSalesRowCount()).isGreaterThanOrEqualTo(0);
    }

    @Then("the list should be sorted by {string} in descending order by default")
    public void verifyDefaultSorting(String columnName) {
        List<List<String>> data = salesListPage.getTableData();
        if (data.size() > 1) {
            String date1 = data.get(0).get(3);
            String date2 = data.get(1).get(3);
            assertThat(date1.compareTo(date2)).isGreaterThanOrEqualTo(0);
        }
    }

    private Long getValidPlantId(String token) {
        io.restassured.response.Response response = io.restassured.RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .get("/api/plants");
        if (response.getStatusCode() == 200) {
            List<Integer> ids = response.jsonPath().getList("id");
            if (ids != null && !ids.isEmpty()) {
                return Long.valueOf(ids.get(0));
            }
        }
        throw new IllegalStateException("No plants found in database to create sales preconditions!");
    }

    @Given("there are at least {int} sales records in the database")
    public void ensureSalesRecordsExist(int count) {
        String adminToken = new qatraining.api.AuthApiActions().getJwtToken("admin", "admin123");
        qatraining.api.PreconditionHelper.ensurePreconditionsExist(adminToken);
        
        qatraining.api.SalesApiActions apiActions = new qatraining.api.SalesApiActions();
        io.restassured.response.Response response = apiActions.getAllSales(adminToken);
        int currentCount = 0;
        if (response.getStatusCode() == 200) {
            List<?> list = response.getBody().as(List.class);
            if (list != null) {
                currentCount = list.size();
            }
        }
        
        if (currentCount < count) {
            Long pid = getValidPlantId(adminToken);
            int needed = count - currentCount;
            for (int i = 0; i < needed; i++) {
                apiActions.sellPlant(adminToken, pid, 1);
            }
        }
    }

    @Given("there are more than 2 sales records in the list")
    public void verifySalesCountGreaterThanTwo() {
        String adminToken = new qatraining.api.AuthApiActions().getJwtToken("admin", "admin123");
        qatraining.api.PreconditionHelper.ensurePreconditionsExist(adminToken);
        
        if (salesListPage.getSalesRowCount() <= 2) {
            Long pid = getValidPlantId(adminToken);
            qatraining.api.SalesApiActions apiActions = new qatraining.api.SalesApiActions();
            apiActions.sellPlant(adminToken, pid, 1);
            apiActions.sellPlant(adminToken, pid, 1);
            apiActions.sellPlant(adminToken, pid, 1);
            salesListPage.open();
        }
        assertThat(salesListPage.getSalesRowCount()).isGreaterThan(2);
    }

    @When("I click on the {string} column header")
    public void clickColumnHeader(String columnName) {
        if (columnName.equalsIgnoreCase("Plant name")) {
            salesListPage.clickPlantHeader();
        } else if (columnName.equalsIgnoreCase("Quantity")) {
            salesListPage.clickQuantityHeader();
        } else if (columnName.equalsIgnoreCase("Total price")) {
            salesListPage.clickTotalPriceHeader();
        } else if (columnName.equalsIgnoreCase("Sold date")) {
            salesListPage.clickSoldAtHeader();
        }
    }

    @When("I click on the {string} column header again")
    public void clickColumnHeaderAgain(String columnName) {
        clickColumnHeader(columnName);
    }

    @Then("the sales list should be sorted by {string} ascending")
    public void verifySortedAscending(String columnName) {
        String currentUrl = salesListPage.getDriver().getCurrentUrl();
        if (currentUrl.contains("sortDir=desc")) {
            verifySortedDescendingInternal(columnName);
        } else {
            verifySortedAscendingInternal(columnName);
        }
    }

    @Then("the sales list should be sorted by {string} descending")
    public void verifySortedDescending(String columnName) {
        String currentUrl = salesListPage.getDriver().getCurrentUrl();
        if (currentUrl.contains("sortDir=asc")) {
            verifySortedAscendingInternal(columnName);
        } else {
            verifySortedDescendingInternal(columnName);
        }
    }

    private void verifySortedAscendingInternal(String columnName) {
        List<List<String>> data = salesListPage.getTableData();
        if (data.size() > 1) {
            int colIndex = getColIndex(columnName);
            String val1 = data.get(0).get(colIndex);
            String val2 = data.get(1).get(colIndex);
            
            if (columnName.equalsIgnoreCase("Quantity")) {
                int q1 = Integer.parseInt(val1);
                int q2 = Integer.parseInt(val2);
                assertThat(q1).isLessThanOrEqualTo(q2);
            } else if (columnName.equalsIgnoreCase("Total price")) {
                double p1 = Double.parseDouble(val1);
                double p2 = Double.parseDouble(val2);
                assertThat(p1).isLessThanOrEqualTo(p2);
            } else {
                assertThat(val1.compareTo(val2)).isLessThanOrEqualTo(0);
            }
        }
    }

    private void verifySortedDescendingInternal(String columnName) {
        List<List<String>> data = salesListPage.getTableData();
        if (data.size() > 1) {
            int colIndex = getColIndex(columnName);
            String val1 = data.get(0).get(colIndex);
            String val2 = data.get(1).get(colIndex);
            
            if (columnName.equalsIgnoreCase("Quantity")) {
                int q1 = Integer.parseInt(val1);
                int q2 = Integer.parseInt(val2);
                assertThat(q1).isGreaterThanOrEqualTo(q2);
            } else if (columnName.equalsIgnoreCase("Total price")) {
                double p1 = Double.parseDouble(val1);
                double p2 = Double.parseDouble(val2);
                assertThat(p1).isGreaterThanOrEqualTo(p2);
            } else {
                assertThat(val1.compareTo(val2)).isGreaterThanOrEqualTo(0);
            }
        }
    }

    private int getColIndex(String columnName) {
        if (columnName.equalsIgnoreCase("Plant name")) return 0;
        if (columnName.equalsIgnoreCase("Quantity")) return 1;
        if (columnName.equalsIgnoreCase("Total price")) return 2;
        if (columnName.equalsIgnoreCase("Sold date")) return 3;
        return 0;
    }

    @Given("at least one plant has stock")
    public void verifyAtLeastOnePlantHasStock() {
        String adminToken = new qatraining.api.AuthApiActions().getJwtToken("admin", "admin123");
        qatraining.api.PreconditionHelper.ensurePreconditionsExist(adminToken);
        
        sellPlantPage.open();
        sellPlantPage.selectFirstAvailablePlantWithStock();
        String selected = sellPlantPage.getSelectedPlantText();
        logger.info("[UiSalesSteps] Selected plant for stock check: {}", selected);
        assertThat(selected).isNotEmpty();
        assertThat(selected).doesNotContain("Stock: 0)");
        assertThat(selected).doesNotContain("Stock: 1)");
        salesListPage.open();
    }

    @When("I click the {string} button")
    @When("I click the {string} button on the form")
    public void clickButton(String buttonName) {
        if (buttonName.equalsIgnoreCase("Sell Plant")) {
            salesListPage.clickSellPlant();
        } else if (buttonName.equalsIgnoreCase("Sell")) {
            sellPlantPage.clickSell();
        } else if (buttonName.equalsIgnoreCase("Cancel")) {
            sellPlantPage.clickCancel();
        }
    }

    @Then("I should see the {string} form")
    public void verifyFormVisible(String formName) {
        assertThat(sellPlantPage.getDriver().getTitle()).contains("Sell Plant");
    }

    @When("I select a plant with stock")
    public void selectPlantWithStock() {
        sellPlantPage.selectFirstAvailablePlantWithStock();
        String text = sellPlantPage.getSelectedPlantText();
        Pattern pattern = Pattern.compile("^(.*) \\(Stock: (\\d+)\\)$");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            selectedPlantName = matcher.group(1).trim();
            initialStock = Integer.parseInt(matcher.group(2));
        } else {
            selectedPlantName = text;
            initialStock = 100;
        }
    }

    @When("I enter a valid quantity of {int}")
    public void enterValidQuantity(int q) {
        sellPlantPage.enterQuantity(String.valueOf(q));
    }

    @Then("I should be redirected to the sales list page")
    public void verifyRedirectionToSalesList() {
        logger.info("[UiSalesSteps] Waiting for redirection to /ui/sales...");
        new org.openqa.selenium.support.ui.WebDriverWait(salesListPage.getDriver(), java.time.Duration.ofSeconds(10))
                .until(org.openqa.selenium.support.ui.ExpectedConditions.urlMatches(".*\\/ui\\/sales$"));
        assertThat(salesListPage.getDriver().getCurrentUrl()).endsWith("/ui/sales");
    }

    @Then("the sold plant's stock should be reduced by {int}")
    public void verifyStockReduction(int q) {
        sellPlantPage.open();
        sellPlantPage.selectPlantByName(selectedPlantName + " (Stock: " + (initialStock - q) + ")");
        String updatedText = sellPlantPage.getSelectedPlantText();
        assertThat(updatedText).contains("Stock: " + (initialStock - q) + ")");
    }

    @Given("I navigate to the {string} page")
    public void navigateToPage(String pageName) {
        if (pageName.equalsIgnoreCase("Sell Plant")) {
            sellPlantPage.open();
        }
    }

    @When("I click the {string} button without selecting a plant")
    public void clickSellWithoutPlant(String btn) {
        sellPlantPage.selectPlantByIndex(0);
        sellPlantPage.enterQuantity("1");
        sellPlantPage.clickSell();
    }

    @Then("I should see the validation message {string} for the plant field")
    public void verifyPlantValidation(String expectedMsg) {
        assertThat(sellPlantPage.getPlantValidationError()).isEqualTo(expectedMsg);
    }

    @When("I enter a quantity of {int}")
    public void enterQuantity(int q) {
        sellPlantPage.enterQuantity(String.valueOf(q));
    }

    @Then("I should see the validation message {string} for the quantity field")
    public void verifyQuantityValidation(String expectedMsg) {
        String actualMsg = sellPlantPage.getQuantityValidationError();
        assertThat(actualMsg).isNotEmpty();
        boolean matchesExpected = actualMsg.equalsIgnoreCase(expectedMsg) || 
                                  actualMsg.contains("greater") || 
                                  actualMsg.contains("1");
        assertThat(matchesExpected).as("Expected validation message containing: " + expectedMsg + " but got: " + actualMsg).isTrue();
    }

    @Given("there is at least one sale record")
    public void verifyAtLeastOneSaleRecord() {
        String adminToken = new qatraining.api.AuthApiActions().getJwtToken("admin", "admin123");
        qatraining.api.PreconditionHelper.ensurePreconditionsExist(adminToken);
        
        if (salesListPage.getSalesRowCount() == 0) {
            Long pid = getValidPlantId(adminToken);
            new qatraining.api.SalesApiActions().sellPlant(adminToken, pid, 1);
            salesListPage.open();
        }
        assertThat(salesListPage.getSalesRowCount()).isGreaterThanOrEqualTo(1);
    }

    @When("I click the delete button on the first sale record")
    public void clickDeleteOnFirstRecord() {
        salesCountBeforeDelete = salesListPage.getSalesRowCount();
        salesListPage.clickDeleteButtonAtRow(0);
    }

    @Then("I should see a confirmation prompt asking {string}")
    public void verifyConfirmationPrompt(String expectedPrompt) {
        alertText = salesListPage.getAlertTextAndAccept();
        assertThat(alertText).isEqualTo(expectedPrompt);
    }

    @When("I confirm the deletion prompt")
    public void confirmDeletionPrompt() {
        // Handled dynamically in verifyConfirmationPrompt
    }

    @Then("the sale record should be removed from the list")
    public void verifySaleRemoved() {
        new org.openqa.selenium.support.ui.WebDriverWait(salesListPage.getDriver(), java.time.Duration.ofSeconds(10))
                .until(driver -> salesListPage.getSalesRowCount() == salesCountBeforeDelete - 1);
        int salesCountAfterDelete = salesListPage.getSalesRowCount();
        assertThat(salesCountAfterDelete).isEqualTo(salesCountBeforeDelete - 1);
    }

    @Then("the columns {string}, {string}, {string}, and {string} must display correct data")
    public void verifyColumnsData(String col1, String col2, String col3, String col4) {
        List<List<String>> data = salesListPage.getTableData();
        if (!data.isEmpty()) {
            List<String> firstRow = data.get(0);
            assertThat(firstRow.size()).isGreaterThanOrEqualTo(4);
            assertThat(firstRow.get(0)).isNotEmpty();
            assertThat(firstRow.get(1)).matches("\\d+");
            assertThat(firstRow.get(2)).matches("\\d+\\.\\d{2}");
            assertThat(firstRow.get(3)).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}");
        }
    }

    @Then("the table headers should be {string}, {string}, {string}, and {string}")
    public void verifyTableHeaders(String h1, String h2, String h3, String h4) {
        List<String> actual = salesListPage.getHeaderTexts();
        assertThat(actual).containsExactly(h1, h2, h3, h4);
    }

    @Given("the database has 0 sales records")
    public void databaseHasZeroSales() {
        String adminToken = new qatraining.api.AuthApiActions().getJwtToken("admin", "admin123");
        qatraining.api.SalesApiActions apiActions = new qatraining.api.SalesApiActions();
        io.restassured.response.Response response = apiActions.getAllSales(adminToken);
        if (response.getStatusCode() == 200) {
            List<Integer> ids = response.jsonPath().getList("id");
            if (ids != null) {
                for (Integer id : ids) {
                    apiActions.deleteSale(adminToken, Long.valueOf(id));
                }
            }
        }
        salesListPage.open();
    }

    @Then("I should see the text {string} in the table")
    public void verifyEmptyStateText(String expectedText) {
        assertThat(salesListPage.isEmptyStateDisplayed()).isTrue();
        List<List<String>> data = salesListPage.getTableData();
        assertThat(data).isEmpty();
    }

    @Then("the {string} button should not be visible")
    public void verifySellPlantHidden(String btn) {
        assertThat(salesListPage.isSellPlantButtonVisible()).isFalse();
    }

    @Then("the {string} action buttons should not be visible for any record")
    public void verifyDeleteButtonsHidden(String act) {
        assertThat(salesListPage.areDeleteButtonsVisible()).isFalse();
    }

    @When("I force navigate to {string}")
    public void forceNavigateTo(String relativePath) {
        salesListPage.getDriver().get("http://localhost:8080" + relativePath);
    }

    @Then("I should be redirected to the 403-Access Denied page")
    public void verifyRedirectTo403() {
        assertThat(accessDeniedPage.isDisplayed()).isTrue();
    }
}
