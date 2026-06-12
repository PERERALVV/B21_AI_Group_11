Feature: Plants Module UI Testing

  Background:
    Given the application is running

  @T-UI-21 @Admin @UI @215533M @BugDetection
  Scenario: T-UI-21 - Admin views Plants page with all management controls visible
    Given I log in as Admin with username "admin" and password "admin123"
    When I navigate to the plants list page
    Then the "Add a Plant" button should be visible on the plants page
    And the plants table should display columns "Name", "Category", "Price", "Stock", and "Actions"
    And each plant row should show Edit and Delete action icons
    And the Plants navigation link should be active

  @T-UI-22 @Admin @UI @215533M
  Scenario: T-UI-22 - Admin successfully adds a new plant with valid data
    Given I log in as Admin with username "admin" and password "admin123"
    And the plant "Cactuspl" has been removed from the system if it exists
    When I navigate to the plants list page
    And I click the "Add a Plant" button on the plants page
    And I enter plant name "Cactuspl"
    And I select the first available sub-category from the plant category dropdown
    And I enter plant price "150.00"
    And I enter plant quantity "30"
    And I click the Save button on the plant form
    Then I should be redirected to the plants list page
    And the success message "Plant added successfully" should be displayed on the plants page
    And the plant "Cactuspl" should appear in the plants list

  @T-UI-23 @Admin @UI @215533M
  Scenario: T-UI-23 - Add Plant form shows field-level validation errors for invalid inputs
    Given I log in as Admin with username "admin" and password "admin123"
    When I navigate to the add plant page directly
    And I enter plant price "0"
    And I enter plant quantity "-1"
    And I click the Save button on the plant form
    Then I should see plant name validation error containing "Plant name is required"
    And I should see plant category validation error "Category is required"
    And I should see plant price validation error "Price must be greater than 0"
    And I should see plant quantity validation error "Quantity cannot be negative"

  @T-UI-24 @Admin @UI @215533M
  Scenario: T-UI-24 - Admin edits a plant and the Low badge appears when quantity drops below 5
    Given I log in as Admin with username "admin" and password "admin123"
    And at least one plant exists in the system
    When I navigate to the plants list page
    And I click the Edit icon on the first plant in the list
    And I change the plant quantity to "3"
    And I click the Save button on the plant form
    Then I should be redirected to the plants list page
    And a "Low" badge should be visible on the plants page

  @T-UI-25 @Admin @UI @215533M
  Scenario: T-UI-25 - Admin deletes a plant via confirmation prompt
    Given I log in as Admin with username "admin" and password "admin123"
    And at least one plant exists in the system
    When I navigate to the plants list page
    And I note the current plant count
    And I click the Delete icon on the first plant in the list
    Then I should see a plant deletion confirmation prompt with text "Delete this plant?"
    When I accept the plant deletion confirmation
    Then the plant count should be reduced by 1

  @T-UI-26 @User @UI @215533M @BugDetection
  Scenario: T-UI-26 - User views Plants page and all management controls are hidden
    Given I log in as User with username "testuser" and password "test123"
    When I navigate to the plants list page
    Then the "Add a Plant" button should not be visible on the plants page
    And no Edit or Delete icons should be present in any plant row
    And the Actions column should not be visible in the plants table

  @T-UI-27 @User @UI @215533M @BugDetection
  Scenario: T-UI-27 - User searches for a plant by name
    Given I log in as User with username "testuser" and password "test123"
    And a plant with a two-word name exists in the system
    When I navigate to the plants list page
    And I type "Rose Mary" in the search plant field
    And I click the Search button on the plants page
    Then the plant table should show only plants matching the search term

  @T-UI-28 @User @UI @215533M
  Scenario: T-UI-28 - User filters plants by category
    Given I log in as User with username "testuser" and password "test123"
    And at least one plant with a category exists in the system
    When I navigate to the plants list page
    And I select the first non-default option from the plant category filter
    And I click the Search button on the plants page
    Then the plant table should show only plants of the selected category

  @T-UI-29 @User @UI @215533M
  Scenario: T-UI-29 - User sorts plant list by Name, Price, and Stock ascending then descending
    Given I log in as User with username "testuser" and password "test123"
    And at least one plant exists in the system
    When I navigate to the plants list page
    And I click on the plants table "Name" column header
    Then the plant list should be sorted by "Name" ascending
    When I click on the plants table "Name" column header again
    Then the plant list should be sorted by "Name" descending
    When I click on the plants table "Price" column header
    Then the plant list should be sorted by "Price" ascending
    When I click on the plants table "Stock" column header
    Then the plant list should be sorted by "Stock" ascending

  @T-UI-30 @User @UI @215533M
  Scenario: T-UI-30 - User directly navigates to /ui/plants/add and is denied access
    Given I log in as User with username "testuser" and password "test123"
    When I force navigate to "/ui/plants/add"
    Then the user should not be able to access the plant add page

  @T-UI-31 @Admin @UI @215533M @BugDetection
  Scenario: T-UI-31 - Plants table fourth column header should be named Quantity not Stock
    Given I log in as Admin with username "admin" and password "admin123"
    When I navigate to the plants list page
    Then the fourth column header in the plants table should be "Quantity"
