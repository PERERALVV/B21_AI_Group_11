package qatraining.pages;

import net.serenitybdd.annotations.DefaultUrl;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

@DefaultUrl("http://localhost:8080/ui/plants/add")
public class AddEditPlantPage extends PageObject {

    @FindBy(name = "name")
    private WebElementFacade nameInput;

    @FindBy(name = "categoryId")
    private WebElementFacade categoryDropdown;

    @FindBy(name = "price")
    private WebElementFacade priceInput;

    @FindBy(name = "quantity")
    private WebElementFacade quantityInput;

    @FindBy(css = "button[type='submit'], button.btn-primary")
    private WebElementFacade saveButton;

    @FindBy(css = "a.btn-secondary")
    private WebElementFacade cancelButton;

    @FindBy(xpath = "//input[@name='name']/following-sibling::div[@class='text-danger']")
    private WebElementFacade nameError;

    @FindBy(xpath = "//select[@name='categoryId']/following-sibling::div[@class='text-danger']")
    private WebElementFacade categoryError;

    @FindBy(xpath = "//input[@name='price']/following-sibling::div[@class='text-danger']")
    private WebElementFacade priceError;

    @FindBy(xpath = "//input[@name='quantity']/following-sibling::div[@class='text-danger']")
    private WebElementFacade quantityError;

    public void enterName(String name) {
        nameInput.clear();
        nameInput.type(name);
    }

    public void clearName() {
        nameInput.clear();
    }

    public void selectFirstSubCategory() {
        Select select = new Select(categoryDropdown);
        if (select.getOptions().size() > 1) {
            select.selectByIndex(1);
        }
    }

    public void selectCategoryById(Long id) {
        Select select = new Select(categoryDropdown);
        try {
            select.selectByValue(String.valueOf(id));
        } catch (Exception e) {
            if (select.getOptions().size() > 1) {
                select.selectByIndex(1);
            }
        }
    }

    public boolean hasSubCategories() {
        Select select = new Select(categoryDropdown);
        return select.getOptions().size() > 1;
    }

    public String getSelectedCategoryText() {
        Select select = new Select(categoryDropdown);
        return select.getFirstSelectedOption().getText();
    }

    public void enterPrice(String price) {
        priceInput.clear();
        priceInput.type(price);
    }

    public void enterQuantity(String quantity) {
        quantityInput.clear();
        quantityInput.type(quantity);
    }

    public void clickSave() {
        WebElement form = getDriver().findElement(By.tagName("form"));
        ((JavascriptExecutor) getDriver()).executeScript("arguments[0].submit();", form);
    }

    public void clickCancel() {
        cancelButton.click();
    }

    public String getNameError() {
        if (nameError.isPresent() && nameError.isCurrentlyVisible()) {
            String text = nameError.getText().trim();
            if (!text.isEmpty()) return text;
        }
        String msg = nameInput.getAttribute("validationMessage");
        return msg != null ? msg : "";
    }

    public String getCategoryError() {
        if (categoryError.isPresent() && categoryError.isCurrentlyVisible()) {
            String text = categoryError.getText().trim();
            if (!text.isEmpty()) return text;
        }
        return "";
    }

    public String getPriceError() {
        if (priceError.isPresent() && priceError.isCurrentlyVisible()) {
            String text = priceError.getText().trim();
            if (!text.isEmpty()) return text;
        }
        String msg = priceInput.getAttribute("validationMessage");
        return msg != null ? msg : "";
    }

    public String getQuantityError() {
        if (quantityError.isPresent() && quantityError.isCurrentlyVisible()) {
            String text = quantityError.getText().trim();
            if (!text.isEmpty()) return text;
        }
        String msg = quantityInput.getAttribute("validationMessage");
        return msg != null ? msg : "";
    }

    public boolean isOnAddPlantPage() {
        return getDriver().getCurrentUrl().contains("/ui/plants/add")
                || getDriver().getCurrentUrl().contains("/ui/plants/edit/");
    }

    public List<WebElementFacade> getAllValidationErrors() {
        return findAll(By.cssSelector("div.text-danger"));
    }
}
