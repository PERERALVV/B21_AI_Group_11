package qatraining.steps;

import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import qatraining.pages.DashboardPage;
import qatraining.pages.LoginPage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions for the Authentication UI tests (T-UI-11..T-UI-20).
 * Reuses LoginSteps (login Givens) and UiSalesSteps ("I force navigate to ..."
 * and the 403 assertion); only auth-specific wording is defined here.
 */
public class UiAuthSteps {

    private LoginPage loginPage;
    private DashboardPage dashboardPage;

    @When("I submit the login form with username {string} and password {string}")
    public void submitLoginForm(String username, String password) {
        loginPage.open();
        if (!username.isEmpty()) {
            loginPage.enterUsername(username);
        }
        if (!password.isEmpty()) {
            loginPage.enterPassword(password);
        }
        loginPage.clickLogin();
    }

    @Then("validate the username field should show validation error {string}")
    @Then("the username field should show validation error {string}")
    public void usernameValidationError(String expected) {
        assertThat(loginPage.getUsernameValidationError()).isEqualTo(expected);
    }

    @Then("validate the password field should show validation error {string}")
    @Then("the password field should show validation error {string}")
    public void passwordValidationError(String expected) {
        assertThat(loginPage.getPasswordValidationError()).isEqualTo(expected);
    }

    @Then("validate a global login error {string} should be displayed")
    @Then("a global login error {string} should be displayed")
    public void globalLoginError(String expected) {
        assertThat(loginPage.getGlobalError()).isEqualTo(expected);
    }

    @Then("validate I should be on the dashboard page")
    @Then("I should be on the dashboard page")
    public void shouldBeOnDashboard() {
        assertThat(dashboardPage.isAt()).isTrue();
    }

    @When("I log out")
    public void logOut() {
        dashboardPage.clickLogout();
    }

    @Then("validate a logout success message {string} should be displayed")
    @Then("a logout success message {string} should be displayed")
    public void logoutSuccessMessage(String expected) {
        assertThat(loginPage.getLogoutSuccessMessage()).isEqualTo(expected);
    }

    @Then("validate I should be redirected to the login page")
    @Then("I should be redirected to the login page")
    public void redirectedToLoginPage() {
        assertThat(loginPage.getDriver().getCurrentUrl()).contains("/ui/login");
    }

    @Then("validate the dashboard should display summary cards for {string}, {string} and {string}")
    @Then("the dashboard should display summary cards for {string}, {string} and {string}")
    public void dashboardSummaryCards(String first, String second, String third) {
        List<String> titles = dashboardPage.getSummaryCardTitles();
        assertThat(titles).contains(first, second, third);
    }

    @Then("validate the {string} navigation link should be active")
    @Then("the {string} navigation link should be active")
    public void navigationLinkActive(String expected) {
        assertThat(dashboardPage.getActiveNavText()).isEqualTo(expected);
    }

    @Then("validate the dashboard summary cards should show numeric values")
    @Then("the dashboard summary cards should show numeric values")
    public void dashboardSummaryValues() {
        List<String> values = dashboardPage.getSummaryValues();
        assertThat(values).isNotEmpty();
        assertThat(values).allMatch(v -> v.matches(".*\\d.*"),
                "each summary value should contain a number");
    }
}
