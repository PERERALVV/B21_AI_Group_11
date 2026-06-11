package qatraining.steps;

import io.cucumber.java.en.Given;
import qatraining.pages.LoginPage;

public class LoginSteps {

    private LoginPage loginPage;

    @Given("I log in as Admin with username {string} and password {string}")
    public void loginAsAdmin(String username, String password) {
        loginPage.open();
        loginPage.login(username, password);
    }

    @Given("I log in as User with username {string} and password {string}")
    public void loginAsUser(String username, String password) {
        loginPage.open();
        loginPage.login(username, password);
    }
}
