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

    // Inline field-validation messages rendered by the login.html client-side script
    @FindBy(css = "input[name='username'] + .invalid-feedback")
    private WebElementFacade usernameError;

    @FindBy(css = "input[name='password'] + .invalid-feedback")
    private WebElementFacade passwordError;

    // Server-rendered alerts on /ui/login (?error and ?logout)
    @FindBy(css = ".alert-danger")
    private WebElementFacade globalError;

    @FindBy(css = ".alert-success")
    private WebElementFacade successAlert;

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
        // Wait for the login redirect to complete and URL to change from /ui/login
        for (int i = 0; i < 10; i++) {
            if (!getDriver().getCurrentUrl().contains("/ui/login")) {
                break;
            }
            try { Thread.sleep(500); } catch (InterruptedException e) {}
        }
    }

    public String getUsernameValidationError() {
        return usernameError.waitUntilVisible().getText().trim();
    }

    public String getPasswordValidationError() {
        return passwordError.waitUntilVisible().getText().trim();
    }

    public String getGlobalError() {
        return globalError.waitUntilVisible().getText().trim();
    }

    public String getLogoutSuccessMessage() {
        return successAlert.waitUntilVisible().getText().trim();
    }

    public boolean isOnLoginPage() {
        return getDriver().getCurrentUrl().contains("/ui/login");
    }
}
