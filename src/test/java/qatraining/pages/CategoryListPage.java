package qatraining.pages;

import net.serenitybdd.annotations.DefaultUrl;
import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Page Object for /ui/categories  (Category List Page)
 *
 * FIX (proven by the latest run): the delete control inside each row does NOT
 * match "button.btn-danger, a.btn-danger" — TC-UI-CAT-A05 failed with
 * NoSuchElementException on exactly that selector even as a logged-in admin
 * with rows present. The edit selector in this same class already hints the
 * app uses Bootstrap OUTLINE buttons (btn-outline-primary); the CSS class
 * .btn-danger does NOT match .btn-outline-danger, so the row-action selectors
 * now cover both solid and outline variants plus delete links/forms.
 * confirmDeletion() also tolerates apps that use a Bootstrap modal instead of
 * a native window.confirm dialog.
 */
@DefaultUrl("http://localhost:8080/ui/categories")
public class CategoryListPage extends PageObject {

    // Row-action selectors – solid buttons, outline buttons, links, and forms
    private static final String DELETE_IN_ROW =
            "button.btn-danger, a.btn-danger, "
            + "button.btn-outline-danger, a.btn-outline-danger, "
            + "a[href*='delete'], form[action*='delete'] button, form[action*='delete'] input[type='submit']";

    private static final String EDIT_IN_ROW =
            "a[href*='edit'], a.btn-outline-primary, button.btn-outline-primary, "
            + "a.btn-primary:not([href*='add'])";

    // ── List Elements ──────────────────────────────────────────────────────

    @FindBy(css = "table tbody tr")
    private List<WebElementFacade> tableRows;

    @FindBy(css = "nav.pagination, ul.pagination, [aria-label='pagination']")
    private WebElementFacade paginationNav;

    // "Add A Category" button – visible only to Admin
    @FindBy(css = "a[href*='/ui/categories/add']")
    private WebElementFacade addCategoryButton;

    // Search field – URL param is 'name' so input name="name"
    @FindBy(css = "input[name='name'], input#name")
    private WebElementFacade searchField;

    // Parent filter dropdown – URL param is 'parentId'
    @FindBy(css = "select[name='parentId'], select#parentId")
    private WebElementFacade parentFilterDropdown;

    // Search button
    @FindBy(css = "button.btn-primary[type='submit'], button.btn-search, form button[type='submit']")
    private WebElementFacade searchButton;

    // Reset button
    @FindBy(css = "button.btn-secondary, button.btn-reset, a.btn-secondary")
    private WebElementFacade resetButton;

    // Column sort headers
    @FindBy(css = "th:nth-child(1)")
    private WebElementFacade idColumnHeader;

    @FindBy(css = "th:nth-child(2)")
    private WebElementFacade nameColumnHeader;

    @FindBy(css = "th:nth-child(3)")
    private WebElementFacade parentColumnHeader;

    // Messages
    @FindBy(css = ".alert-success")
    private WebElementFacade successMessage;

    @FindBy(css = ".alert-danger")
    private WebElementFacade errorMessage;

    // ── Navigation ─────────────────────────────────────────────────────────

    public void clickAddCategory() {
        addCategoryButton.click();
    }

    /**
     * Sort headers may render the clickable control as a link inside the th.
     * Click the inner link if present, otherwise the header cell itself.
     */
    public void clickSortByName() {
        clickHeaderOrInnerLink(nameColumnHeader);
    }

    public void clickSortById() {
        clickHeaderOrInnerLink(idColumnHeader);
    }

    private void clickHeaderOrInnerLink(WebElementFacade header) {
        List<org.openqa.selenium.WebElement> links =
                header.findElements(By.tagName("a"));
        if (!links.isEmpty()) {
            links.get(0).click();
        } else {
            header.click();
        }
    }

    public void clickReset() {
        resetButton.click();
    }

    public void searchByName(String name) {
        searchField.clear();
        searchField.type(name);
        searchButton.click();
    }

    public void selectParentFilter(String parentName) {
        parentFilterDropdown.selectByVisibleText(parentName);
        searchButton.click();
    }

    // Delete control in each row (solid/outline button, link, or form submit)
    public void clickDeleteForRow(int rowIndex) {
        tableRows.get(rowIndex)
                .find(By.cssSelector(DELETE_IN_ROW))
                .click();
    }

    public void clickDeleteForCategoryNamed(String name) {
        for (WebElementFacade row : tableRows) {
            String cellText = row.find(By.cssSelector("td:nth-child(2)")).getText().trim();
            if (cellText.equalsIgnoreCase(name)) {
                row.find(By.cssSelector(DELETE_IN_ROW)).click();
                return;
            }
        }
        throw new IllegalArgumentException("No row found with category name: " + name);
    }

    /**
     * Asserts that NO confirmation dialog appeared after clicking Delete.
     * SRS Section 5.1 does not document any confirmation prompt.
     * If a dialog IS found, it is dismissed (so the browser does not hang)
     * and an AssertionError is thrown — making the test FAIL and flagging
     * the dialog as an over-implementation / undocumented behaviour.
     */
    public void verifyNoConfirmationDialog() {
        try {
            new WebDriverWait(getDriver(), Duration.ofSeconds(3))
                    .until(ExpectedConditions.alertIsPresent());
            // Dialog appeared — dismiss it so the browser does not hang, then fail.
            getDriver().switchTo().alert().dismiss();
            throw new AssertionError(
                    "Unexpected confirmation dialog appeared after clicking Delete. "
                    + "SRS Section 5.1 does not document any confirmation prompt — "
                    + "this is an over-implementation (undocumented behaviour).");
        } catch (org.openqa.selenium.TimeoutException e) {
            // No dialog appeared — correct per SRS.
        }
    }

    /**
     * Confirms the deletion whether the app uses a native window.confirm
     * dialog or a Bootstrap confirmation modal.
     */
    public void confirmDeletion() {
        try {
            getDriver().switchTo().alert().accept();
        } catch (NoAlertPresentException e) {
            // Bootstrap modal fallback
            find(By.cssSelector(
                    ".modal.show .btn-danger, .modal.show button[type='submit'], "
                    + ".modal .btn-danger, .modal button[type='submit']"))
                    .click();
        }
    }

    public void cancelDeletion() {
        try {
            getDriver().switchTo().alert().dismiss();
        } catch (NoAlertPresentException e) {
            find(By.cssSelector(
                    ".modal.show .btn-secondary, .modal .btn-secondary, "
                    + ".modal button[data-bs-dismiss='modal']"))
                    .click();
        }
    }

    // ── Table Data ─────────────────────────────────────────────────────────

    public List<List<String>> getTableData() {
        List<List<String>> data = new ArrayList<>();
        if (isEmptyStateDisplayed()) {
            return data;
        }
        for (WebElementFacade row : tableRows) {
            List<String> rowData = row.findElements(By.tagName("td"))
                    .stream()
                    .map(cell -> cell.getText().trim())
                    .collect(Collectors.toList());
            data.add(rowData);
        }
        return data;
    }

    public List<String> getHeaderTexts() {
        List<WebElementFacade> headers = findAll(By.cssSelector("table thead th"));
        List<String> texts = new ArrayList<>();
        for (WebElementFacade h : headers) {
            String text = h.getText().trim();
            if (!text.isEmpty() && !text.equalsIgnoreCase("Actions")) {
                texts.add(text);
            }
        }
        return texts;
    }

    public boolean isPaginationVisible() {
        return paginationNav.isPresent() && paginationNav.isCurrentlyVisible();
    }

    // ── Visibility Checks ──────────────────────────────────────────────────

    public boolean isAddCategoryButtonVisible() {
        try { return addCategoryButton.isPresent() && addCategoryButton.isCurrentlyVisible(); }
        catch (Exception e) { return false; }
    }

    public boolean isEditButtonVisibleForRow(int rowIndex) {
        try {
            return tableRows.get(rowIndex)
                    .find(By.cssSelector(EDIT_IN_ROW))
                    .isDisplayed();
        } catch (Exception e) { return false; }
    }

    public boolean isDeleteButtonVisibleForRow(int rowIndex) {
        try {
            return tableRows.get(rowIndex)
                    .find(By.cssSelector(DELETE_IN_ROW))
                    .isDisplayed();
        } catch (Exception e) { return false; }
    }

    // ── Row / Category Queries ─────────────────────────────────────────────

    public boolean isEmptyStateDisplayed() {
        return getDriver().getPageSource().contains("No category found");
    }

    public int getRowCount() {
        return tableRows.size();
    }

    // Column order: ID | Name | Parent | Actions
    public String getCategoryNameInRow(int index) {
        return tableRows.get(index)
                .find(By.cssSelector("td:nth-child(2)"))
                .getText().trim();
    }

    public boolean isCategoryPresentInList(String name) {
        for (int i = 0; i < getRowCount(); i++) {
            if (getCategoryNameInRow(i).equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    // ── Messages ───────────────────────────────────────────────────────────

    public boolean isSuccessMessageVisible() {
        try { return successMessage.isPresent() && successMessage.isCurrentlyVisible(); }
        catch (Exception e) { return false; }
    }

    public String getSuccessMessageText() {
        return successMessage.getText().trim();
    }

    public boolean isErrorMessageVisible() {
        try { return errorMessage.isPresent() && errorMessage.isCurrentlyVisible(); }
        catch (Exception e) { return false; }
    }

    public String getErrorMessageText() {
        return errorMessage.getText().trim();
    }

    public String getCurrentUrl() {
        return getDriver().getCurrentUrl();
    }
}