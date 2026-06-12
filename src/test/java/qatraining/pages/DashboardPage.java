package qatraining.pages;

import net.serenitybdd.annotations.DefaultUrl;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.stream.Collectors;

@DefaultUrl("http://localhost:8080/ui/dashboard")
public class DashboardPage extends PageObject {

    // Summary card headings on dashboard.html (Categories / Plants / Sales / Inventory)
    @FindBy(css = ".dashboard-card h6")
    private List<WebElementFacade> summaryCardTitles;

    // Numeric summary values inside the cards (Main/Sub, Total/Low stock, Revenue/Sales count)
    @FindBy(css = ".dashboard-card .fw-bold.fs-5")
    private List<WebElementFacade> summaryCardValues;

    // Sidebar nav link that matches the current page gets the "active" class (layout.html)
    @FindBy(css = "a.nav-link.active")
    private WebElementFacade activeNavLink;

    @FindBy(css = "a[href='/ui/logout']")
    private WebElementFacade logoutLink;

    public boolean isAt() {
        return getDriver().getCurrentUrl().contains("/ui/dashboard")
                && getDriver().getTitle().contains("Dashboard");
    }

    public List<String> getSummaryCardTitles() {
        return summaryCardTitles.stream()
                .map(WebElementFacade::getText)
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public List<String> getSummaryValues() {
        return summaryCardValues.stream()
                .map(WebElementFacade::getText)
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public String getActiveNavText() {
        return activeNavLink.waitUntilVisible().getText().trim();
    }

    public void clickLogout() {
        logoutLink.click();
    }
}
