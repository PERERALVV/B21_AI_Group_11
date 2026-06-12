package qatraining.pages;

import net.serenitybdd.annotations.DefaultUrl;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

@DefaultUrl("http://localhost:8080/ui/sales/new")
public class SellPlantPage extends PageObject {

    @FindBy(name = "plantId")
    private WebElementFacade plantDropdown;

    @FindBy(name = "quantity")
    private WebElementFacade quantityInput;

    @FindBy(css = "button.btn-primary")
    private WebElementFacade sellButton;

    @FindBy(css = "a.btn-secondary")
    private WebElementFacade cancelButton;

    @FindBy(xpath = "//select[@name='plantId']/following-sibling::div[@class='text-danger']")
    private WebElementFacade plantError;

    @FindBy(xpath = "//input[@name='quantity']/following-sibling::div[@class='text-danger']")
    private WebElementFacade quantityError;

    public void selectPlantByIndex(int index) {
        Select select = new Select(plantDropdown);
        select.selectByIndex(index);
    }

    public void selectPlantByName(String name) {
        Select select = new Select(plantDropdown);
        select.selectByVisibleText(name);
    }

    public void selectFirstAvailablePlantWithStock() {
        Select select = new Select(plantDropdown);
        for (int i = 1; i < select.getOptions().size(); i++) {
            String text = select.getOptions().get(i).getText();
            if (text.contains("Stock:")) {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile("Stock:\\s*(\\d+)");
                java.util.regex.Matcher m = p.matcher(text);
                if (m.find()) {
                    int stock = Integer.parseInt(m.group(1));
                    if (stock >= 2) {
                        select.selectByIndex(i);
                        return;
                    }
                } else if (!text.contains("Stock: 0)") && !text.contains("Stock: 1)")) {
                    select.selectByIndex(i);
                    return;
                }
            }
        }
        if (select.getOptions().size() > 1) {
            select.selectByIndex(1);
        }
    }

    public String getSelectedPlantText() {
        Select select = new Select(plantDropdown);
        return select.getFirstSelectedOption().getText();
    }

    public void enterQuantity(String quantity) {
        quantityInput.type(quantity);
    }

    public void clickSell() {
        sellButton.click();
    }

    public void clickCancel() {
        cancelButton.click();
    }

    public String getPlantValidationError() {
        return plantError.isPresent() ? plantError.getText().trim() : "";
    }

    public String getQuantityValidationError() {
        if (quantityError.isPresent() && !quantityError.getText().trim().isEmpty()) {
            return quantityError.getText().trim();
        }
        return quantityInput.isPresent() ? quantityInput.getAttribute("validationMessage") : "";
    }
}
