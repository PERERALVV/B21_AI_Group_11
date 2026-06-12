Feature: Sales Module UI Testing

  Background:
    Given the application is running

  @T-UI-1 @Admin @UI @215542N
  Scenario: T-UI-1 - Check sales list pagination and default sorting
    Given I log in as Admin with username "admin" and password "admin123"
    And there are at least 6 sales records in the database
    When I navigate to the sales list page
    Then I should see the sales list loaded with pagination
    And the table headers should be "Plant name", "Quantity", "Total price", and "Sold date"
    And the list should be sorted by "Sold Date" in descending order by default

  @T-UI-2 @Admin @UI @215542N
  Scenario: T-UI-2 - Test column sorting functionality
    Given I log in as Admin with username "admin" and password "admin123"
    And I navigate to the sales list page
    And there are more than 2 sales records in the list
    When I click on the "Plant name" column header
    Then the sales list should be sorted by "Plant name" ascending
    When I click on the "Plant name" column header again
    Then the sales list should be sorted by "Plant name" descending
    When I click on the "Quantity" column header
    Then the sales list should be sorted by "Quantity" ascending
    When I click on the "Total price" column header
    Then the sales list should be sorted by "Total price" ascending
    When I click on the "Sold date" column header
    Then the sales list should be sorted by "Sold date" ascending

  @T-UI-3 @Admin @UI @215542N
  Scenario: T-UI-3 - Test selling a plant (Happy path)
    Given I log in as Admin with username "admin" and password "admin123"
    And at least one plant has stock
    When I click the "Sell Plant" button
    Then I should see the "Sell Plant" form
    When I select a plant with stock
    And I enter a valid quantity of 2
    And I click the "Sell" button
    Then I should be redirected to the sales list page
    And the sold plant's stock should be reduced by 2

  @T-UI-4 @Admin @UI @215542N
  Scenario: T-UI-4 - Test validation messages for selling a plant
    Given I log in as Admin with username "admin" and password "admin123"
    And at least one plant has stock
    And I navigate to the "Sell Plant" page
    When I click the "Sell" button without selecting a plant
    Then I should see the validation message "Plant is required" for the plant field
    When I select a plant with stock
    And I enter a quantity of 0
    And I click the "Sell" button
    Then I should see the validation message "Value must be greater than 0" for the quantity field

  @T-UI-5 @Admin @UI @215542N
  Scenario: T-UI-5 - Test Delete Sale confirmation
    Given I log in as Admin with username "admin" and password "admin123"
    And I navigate to the sales list page
    And there is at least one sale record
    When I click the delete button on the first sale record
    Then I should see a confirmation prompt asking "Are you sure you want to delete this sale?"
    When I confirm the deletion prompt
    Then the sale record should be removed from the list

  @T-UI-6 @User @UI @215542N
  Scenario: T-UI-6 - Check read-only access to sales list
    Given I log in as User with username "testuser" and password "test123"
    And there is at least one sale record
    When I navigate to the sales list page
    Then I should see the sales list loaded with pagination
    And the columns "Plant Name", "Quantity", "Total Price", and "Date" must display correct data

  @T-UI-7 @User @UI @215542N
  Scenario: T-UI-7 - Check 'No sales found' empty state
    Given the database has 0 sales records
    And I log in as User with username "testuser" and password "test123"
    When I navigate to the sales list page
    Then I should see the text "No sales found" in the table

  @T-UI-8 @User @UI @215542N
  Scenario: T-UI-8 - Check if Admin buttons are hidden
    Given I log in as User with username "testuser" and password "test123"
    And there is at least one sale record
    When I navigate to the sales list page
    Then the "Sell Plant" button should not be visible
    And the "Delete" action buttons should not be visible for any record

  @T-UI-9 @User @UI @215542N
  Scenario: T-UI-9 - Test 403 redirect by forcing URL
    Given I log in as User with username "testuser" and password "test123"
    When I force navigate to "/ui/sales/new"
    Then I should be redirected to the 403-Access Denied page

  @T-UI-10 @User @UI @215542N
  Scenario: T-UI-10 - Test if grid sorting works for normal users
    Given I log in as User with username "testuser" and password "test123"
    And I navigate to the sales list page
    And there is at least one sale record
    When I click on the "Plant name" column header
    Then the sales list should be sorted by "Plant name" ascending
