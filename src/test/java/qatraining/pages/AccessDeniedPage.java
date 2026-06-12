package qatraining.pages;

import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import org.openqa.selenium.support.FindBy;

public class AccessDeniedPage extends PageObject {

    @FindBy(css = "h2.text-danger")
    private WebElementFacade accessDeniedHeader;

    public boolean isDisplayed() {
        accessDeniedHeader.waitUntilVisible();
        return accessDeniedHeader.getText().trim().equals("403 - Access Denied");
    }
}
