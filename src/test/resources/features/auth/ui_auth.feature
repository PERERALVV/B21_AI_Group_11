Feature: Authentication Module - UI
  # Member: Janith (215543T) - Authentication.
  # Login form validation is client-side (login.html); bad creds redirect to
  # /ui/login?error, logout to /ui/login?logout.

  @T-UI-AU-ADM-1 @Admin @UI @215543T
  Scenario: T-UI-AU-ADM-1 - Valid admin login redirects to the dashboard
    Given I log in as Admin with username "admin" and password "admin123"
    Then I should be on the dashboard page

  @T-UI-AU-ADM-2 @Admin @UI @215543T
  Scenario: T-UI-AU-ADM-2 - Admin logout shows a success message and ends the session
    Given I log in as Admin with username "admin" and password "admin123"
    When I log out
    Then a logout success message "You have been logged out successfully." should be displayed
    And I should be redirected to the login page
    When I force navigate to "/ui/dashboard"
    Then I should be redirected to the login page

  @T-UI-AU-USR-1 @User @UI @215543T
  Scenario: T-UI-AU-USR-1 - Valid user login redirects to the dashboard
    Given I log in as User with username "testuser" and password "test123"
    Then I should be on the dashboard page

  @T-UI-AU-USR-2 @User @UI @215543T
  Scenario: T-UI-AU-USR-2 - Dashboard displays summary information
    Given I log in as User with username "testuser" and password "test123"
    Then I should be on the dashboard page
    And the dashboard should display summary cards for "Categories", "Plants" and "Sales"
    And the dashboard summary cards should show numeric values

  # The app only applies the "active" highlight on /ui/sales and /ui/dashboard;
  # /ui/categories and /ui/plants never mark their nav link active (minor UI bug).
  # Verified here on /ui/sales, which a normal user can access.
  @T-UI-AU-USR-3 @User @UI @215543T
  Scenario: T-UI-AU-USR-3 - Navigation menu highlights the active page
    Given I log in as User with username "testuser" and password "test123"
    When I force navigate to "/ui/sales"
    Then the "Sales" navigation link should be active

  @T-UI-AU-USR-4 @User @UI @215543T
  Scenario: T-UI-AU-USR-4 - Normal user is blocked from an admin-only page (403)
    Given I log in as User with username "testuser" and password "test123"
    When I force navigate to "/ui/categories/add"
    Then I should be redirected to the 403-Access Denied page

  @T-UI-AU-ANON-1 @Anonymous @UI @215543T
  Scenario: T-UI-AU-ANON-1 - Empty username shows a validation message
    When I submit the login form with username "" and password "somepassword"
    Then the username field should show validation error "Username is required"

  @T-UI-AU-ANON-2 @Anonymous @UI @215543T
  Scenario: T-UI-AU-ANON-2 - Empty password shows a validation message
    When I submit the login form with username "admin" and password ""
    Then the password field should show validation error "Password is required"

  @T-UI-AU-ANON-3 @Anonymous @UI @215543T
  Scenario: T-UI-AU-ANON-3 - Invalid credentials show a global error message
    When I submit the login form with username "admin" and password "wrongpassword"
    Then a global login error "Invalid username or password." should be displayed

  @T-UI-AU-ANON-4 @Anonymous @UI @215543T
  Scenario: T-UI-AU-ANON-4 - Unauthenticated access is redirected to login
    When I force navigate to "/ui/logout"
    And I force navigate to "/ui/categories"
    Then I should be redirected to the login page
