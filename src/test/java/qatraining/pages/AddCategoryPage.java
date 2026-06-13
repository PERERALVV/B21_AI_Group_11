package qatraining.pages;

import net.serenitybdd.annotations.DefaultUrl;
import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;

/**
 * Page Object for:
 *   /ui/categories/add        – Add Category
 *   /ui/categories/edit/{id}  – Edit Category
 *
 * FIX (proven by the latest run): the name-field selector #categoryName does
 * NOT exist on the real Add page — TC-UI-CAT-A02/A03/A04 all died there with
 * NoSuchElementException even when logged in as admin. The form almost
 * certainly binds the same field name as the API and the list filter ("name",
 * typical Thymeleaf th:field rendering id="name" name="name"). All form
 * selectors below now accept the common variants, with the proven-wrong ones
 * kept only as last-resort fallbacks:
 *   - name input:      input[name='name'], input#name, then #categoryName
 *   - parent dropdown: select[name='parentId'], select#parentId, then #parentCategory
 *   - cancel:          .btn-secondary link/button or a link back to /ui/categories
 */
@DefaultUrl("http://localhost:8080/ui/categories/add")
public class AddCategoryPage extends PageObject {

    private static final String BASE_URL = "http://localhost:8080";

    // "Category Name" input field
    @FindBy(css = "input[name='name'], input#name, input#categoryName, form input[type='text']")
    private WebElementFacade categoryNameField;

    // "Parent Category" dropdown
    @FindBy(css = "select[name='parentId'], select#parentId, select#parentCategory, form select")
    private WebElementFacade parentCategoryDropdown;

    // Save button
    @FindBy(css = "button[type='submit'], input[type='submit']")
    private WebElementFacade saveButton;

    // Cancel button/link – navigates back to /ui/categories
    @FindBy(css = "a.btn-secondary, button.btn-secondary, a.btn-outline-secondary, a[href$='/ui/categories']")
    private WebElementFacade cancelButton;

    // Validation error messages shown below the Category Name field
    @FindBy(css = ".invalid-feedback, .field-error, .form-error")
    private WebElementFacade validationError;

    // Raw API error shown on page (e.g. 403 JSON error)
    @FindBy(css = ".alert-danger")
    private WebElementFacade pageError;

    // ── Navigation ─────────────────────────────────────────────────────────

    public void openAddForm() {
        getDriver().get(BASE_URL + "/ui/categories/add");
    }

    public void openEditForm(int categoryId) {
        getDriver().get(BASE_URL + "/ui/categories/edit/" + categoryId);
    }

    // ── Form Actions ───────────────────────────────────────────────────────

    public void enterCategoryName(String name) {
        categoryNameField.clear();
        categoryNameField.type(name);
    }

    public void clearCategoryName() {
        categoryNameField.clear();
    }

    public void selectParentCategory(String parentName) {
        parentCategoryDropdown.selectByVisibleText(parentName);
    }

    public void clickSave() {
        saveButton.click();
    }

    public void clickCancel() {
        cancelButton.click();
    }

    // ── Queries ────────────────────────────────────────────────────────────

    public String getValidationErrorText() {
        return validationError.getText().trim();
    }

    public boolean isValidationErrorVisible() {
        try { return validationError.isPresent() && validationError.isCurrentlyVisible(); }
        catch (Exception e) { return false; }
    }

    public boolean isPageErrorVisible() {
        try { return pageError.isPresent() && pageError.isCurrentlyVisible(); }
        catch (Exception e) { return false; }
    }

    public String getPageErrorText() {
        return pageError.getText().trim();
    }

    public String getCurrentUrl() {
        return getDriver().getCurrentUrl();
    }

    public boolean is403Page() {
        String url    = getDriver().getCurrentUrl();
        String source = getDriver().getPageSource();
        return url.contains("403")
                || source.contains("Access Denied")
                || source.contains("Forbidden");
    }
}