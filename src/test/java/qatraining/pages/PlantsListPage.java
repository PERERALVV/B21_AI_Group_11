package qatraining.pages;

import net.serenitybdd.annotations.DefaultUrl;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@DefaultUrl("http://localhost:8080/ui/plants")
public class PlantsListPage extends PageObject {

    @FindBy(linkText = "Add a Plant")
    private WebElementFacade addPlantButton;

    @FindBy(name = "name")
    private WebElementFacade searchInput;

    @FindBy(name = "categoryId")
    private WebElementFacade categoryDropdown;

    @FindBy(xpath = "//form[.//input[@name='name']]//button[@type='submit']")
    private WebElementFacade searchButton;

    @FindBy(css = "table.table")
    private WebElementFacade plantsTable;

    @FindBy(css = "tbody tr")
    private List<WebElementFacade> tableRows;

    @FindBy(css = "ul.pagination")
    private WebElementFacade paginationNav;

    @FindBy(partialLinkText = "Name")
    private WebElementFacade nameHeader;

    @FindBy(partialLinkText = "Price")
    private WebElementFacade priceHeader;

    @FindBy(partialLinkText = "Stock")
    private WebElementFacade stockHeader;

    public boolean isAddPlantButtonVisible() {
        return addPlantButton.isPresent() && addPlantButton.isCurrentlyVisible();
    }

    public void clickAddPlant() {
        addPlantButton.click();
    }

    public void enterSearchKeyword(String keyword) {
        searchInput.clear();
        searchInput.type(keyword);
    }

    public void selectCategoryByIndex(int index) {
        Select select = new Select(categoryDropdown);
        if (select.getOptions().size() > index) {
            select.selectByIndex(index);
        }
    }

    public String getSelectedCategoryText() {
        Select select = new Select(categoryDropdown);
        return select.getFirstSelectedOption().getText();
    }

    public void clickSearch() {
        WebElement form = getDriver().findElement(
                By.xpath("//form[.//input[@name='name']]"));
        ((JavascriptExecutor) getDriver()).executeScript("arguments[0].submit();", form);
    }

    public int getPlantRowCount() {
        if (isEmptyStateDisplayed()) {
            return 0;
        }
        return tableRows.size();
    }

    public boolean isEmptyStateDisplayed() {
        List<WebElementFacade> emptyCells = findAll(By.xpath("//td[contains(text(),'No plants found')]"));
        return !emptyCells.isEmpty() && emptyCells.get(0).isCurrentlyVisible();
    }

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

    public List<String> getColumnHeaders() {
        List<WebElementFacade> headers = findAll(By.cssSelector("table thead th"));
        List<String> texts = new ArrayList<>();
        for (WebElementFacade h : headers) {
            // Strip sort direction arrows (↑ ↓ ▲ ▼) that appear in sortable column headers
            String text = h.getText().trim().replaceAll("[↑↓▲▼]", "").trim();
            if (!text.isEmpty()) {
                texts.add(text);
            }
        }
        return texts;
    }

    public boolean areEditIconsPresent() {
        List<WebElementFacade> editLinks = findAll(By.cssSelector("a[href*='/ui/plants/edit/']"));
        return !editLinks.isEmpty();
    }

    public boolean areDeleteIconsPresent() {
        List<WebElementFacade> deleteForms = findAll(By.cssSelector("form[action*='/ui/plants/delete'] button"));
        if (!deleteForms.isEmpty()) return true;
        List<WebElementFacade> actionBtns = findAll(By.cssSelector("tbody td button[onclick*='confirm']"));
        return !actionBtns.isEmpty();
    }

    public boolean isActionsColumnPresent() {
        List<WebElementFacade> headers = findAll(By.cssSelector("table thead th"));
        for (WebElementFacade h : headers) {
            if (h.getText().trim().equalsIgnoreCase("Actions")) {
                return true;
            }
        }
        return false;
    }

    public boolean isPlantsNavLinkActive() {
        List<WebElementFacade> navLinks = findAll(By.cssSelector("a[href*='/ui/plants']"));
        for (WebElementFacade link : navLinks) {
            String classes = link.getAttribute("class");
            if (classes != null && classes.contains("active")) {
                return true;
            }
        }
        return false;
    }

    public void clickEditAtRow(int index) {
        List<WebElementFacade> editLinks = findAll(By.cssSelector("a[href*='/ui/plants/edit/']"));
        if (index < editLinks.size()) {
            editLinks.get(index).click();
        } else {
            throw new IndexOutOfBoundsException("No edit link at row index " + index);
        }
    }

    public void clickDeleteAtRow(int index) {
        List<WebElementFacade> deleteButtons = findAll(By.cssSelector("form[action*='/ui/plants/delete'] button"));
        if (deleteButtons.isEmpty()) {
            deleteButtons = findAll(By.cssSelector("tbody button[class*='btn-danger']"));
        }
        if (index < deleteButtons.size()) {
            deleteButtons.get(index).click();
        } else {
            throw new IndexOutOfBoundsException("No delete button at row index " + index);
        }
    }

    public String getAlertTextAndAccept() {
        Alert alert = getDriver().switchTo().alert();
        String text = alert.getText();
        alert.accept();
        return text;
    }

    public boolean isLowBadgeVisible() {
        List<WebElementFacade> lowBadges = findAll(
                By.xpath("//*[normalize-space(text())='Low' or normalize-space(text())='low']"));
        if (!lowBadges.isEmpty()) return true;
        List<WebElementFacade> badges = findAll(By.cssSelector(".badge, .badge-danger, .badge-warning"));
        for (WebElementFacade badge : badges) {
            if (badge.getText().trim().equalsIgnoreCase("Low")) {
                return true;
            }
        }
        return false;
    }

    public void clickNameHeader() {
        nameHeader.click();
    }

    public void clickPriceHeader() {
        priceHeader.click();
    }

    public void clickStockHeader() {
        stockHeader.click();
    }

    public boolean isPaginationVisible() {
        return paginationNav.isPresent() && paginationNav.isCurrentlyVisible();
    }

    public boolean isSuccessMessageDisplayed(String expected) {
        List<WebElementFacade> alerts = findAll(By.cssSelector(".alert-success, .alert.alert-success, div.alert"));
        for (WebElementFacade alert : alerts) {
            if (alert.isCurrentlyVisible() && alert.getText().contains(expected)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPlantInList(String plantName) {
        List<List<String>> data = getTableData();
        for (List<String> row : data) {
            if (!row.isEmpty() && row.get(0).equalsIgnoreCase(plantName)) {
                return true;
            }
        }
        return false;
    }

    public boolean allRowsContainCategory(String categoryName) {
        List<List<String>> data = getTableData();
        if (data.isEmpty()) return false;
        for (List<String> row : data) {
            if (row.size() < 2 || !row.get(1).equalsIgnoreCase(categoryName)) {
                return false;
            }
        }
        return true;
    }

    public boolean allRowsMatchSearch(String keyword) {
        List<List<String>> data = getTableData();
        if (data.isEmpty()) return false;
        for (List<String> row : data) {
            if (!row.isEmpty() && !row.get(0).toLowerCase().contains(keyword.toLowerCase())) {
                return false;
            }
        }
        return true;
    }
}
