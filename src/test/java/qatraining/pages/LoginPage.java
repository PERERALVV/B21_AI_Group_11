package qatraining.pages;

import net.serenitybdd.annotations.DefaultUrl;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import org.openqa.selenium.support.FindBy;

@DefaultUrl("http://localhost:8080/ui/login")
public class LoginPage extends PageObject {

    @FindBy(name = "username")
    private WebElementFacade usernameInput;

    @FindBy(name = "password")
    private WebElementFacade passwordInput;

    @FindBy(css = "button[type='submit']")
    private WebElementFacade loginButton;

    public void enterUsername(String username) {
        usernameInput.type(username);
    }

    public void enterPassword(String password) {
        passwordInput.type(password);
    }

    public void clickLogin() {
        loginButton.click();
    }

    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
    }
}
