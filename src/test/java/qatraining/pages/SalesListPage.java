package qatraining.pages;

import net.serenitybdd.annotations.DefaultUrl;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@DefaultUrl("http://localhost:8080/ui/sales")
public class SalesListPage extends PageObject {

    @FindBy(linkText = "Sell Plant")
    private WebElementFacade sellPlantButton;

    @FindBy(css = "table.table")
    private WebElementFacade salesTable;

    @FindBy(linkText = "Plant")
    private WebElementFacade plantHeader;

    @FindBy(linkText = "Quantity")
    private WebElementFacade quantityHeader;

    @FindBy(linkText = "Total Price")
    private WebElementFacade totalPriceHeader;

    @FindBy(linkText = "Sold At")
    private WebElementFacade soldAtHeader;

    @FindBy(css = "tbody tr")
    private List<WebElementFacade> tableRows;

    @FindBy(css = "ul.pagination")
    private WebElementFacade paginationNav;

    public boolean isSellPlantButtonVisible() {
        return sellPlantButton.isPresent() && sellPlantButton.isCurrentlyVisible();
    }

    public void clickSellPlant() {
        sellPlantButton.click();
    }

    public void clickPlantHeader() {
        plantHeader.click();
    }

    public void clickQuantityHeader() {
        quantityHeader.click();
    }

    public void clickTotalPriceHeader() {
        totalPriceHeader.click();
    }

    public void clickSoldAtHeader() {
        soldAtHeader.click();
    }

    public int getSalesRowCount() {
        if (isEmptyStateDisplayed()) {
            return 0;
        }
        return tableRows.size();
    }

    public boolean isEmptyStateDisplayed() {
        List<WebElementFacade> emptyCells = findAll(By.xpath("//td[contains(text(),'No sales found')]"));
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

    public boolean isPaginationVisible() {
        return paginationNav.isPresent() && paginationNav.isCurrentlyVisible();
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

    public boolean areDeleteButtonsVisible() {
        List<WebElementFacade> deleteButtons = findAll(By.cssSelector("form[action*='/ui/sales/delete'] button"));
        return !deleteButtons.isEmpty() && deleteButtons.get(0).isCurrentlyVisible();
    }

    public void clickDeleteButtonAtRow(int index) {
        List<WebElementFacade> deleteButtons = findAll(By.cssSelector("form[action*='/ui/sales/delete'] button"));
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

    public String getAlertTextAndDismiss() {
        Alert alert = getDriver().switchTo().alert();
        String text = alert.getText();
        alert.dismiss();
        return text;
    }
}
