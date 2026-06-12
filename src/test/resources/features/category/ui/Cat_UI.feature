Feature: Admin & User UI – Category Management

  Background:
    Given the admin or user is logged in

  @UI @Admin @TC-UI-CAT-A01
  Scenario: TC-UI-CAT-A01 Admin can view the category list
    When the admin opens the Category List page
    Then the category list is displayed with at least one record

  @UI @Admin @TC-UI-CAT-A02
  Scenario: TC-UI-CAT-A02 Admin can add a new main category with valid name
    When the admin opens the Category List page
    And the admin clicks Add Category button
    And the admin enters category name "Roses"
    And the admin clicks Save
    Then the success message is displayed
    And the category "Roses" appears in the list

  @UI @Admin @TC-UI-CAT-A03
  Scenario: TC-UI-CAT-A03 Validation error when category name is empty
    When the admin opens the Add Category page
    And the admin leaves category name empty
    And the admin clicks Save
    Then the required name validation error is shown

  @UI @Admin @TC-UI-CAT-A04
  Scenario: TC-UI-CAT-A04 Validation error when name is shorter than 3 characters
    When the admin opens the Add Category page
    And the admin enters category name "AB"
    And the admin clicks Save
    Then the name length validation error is shown

  @UI @Admin @TC-UI-CAT-A05
  Scenario: TC-UI-CAT-A05 Admin can delete a category - no confirmation dialog expected per SRS
    Given a deletable category exists in the system
    When the admin opens the Category List page
    And the admin clicks Delete for row 0
    Then no confirmation dialog should appear
    And the success message is displayed


  @UI @User @TC-UI-CAT-U01
  Scenario: TC-UI-CAT-U01 User can view category list in read-only mode
    When the user opens the Category List page
    Then the category list is displayed with at least one record
    And the Add Category button is NOT visible
    And Edit and Delete buttons are hidden

  @UI @User @TC-UI-CAT-U02
  Scenario: TC-UI-CAT-U02 User can search categories by name
    Given a category named "Roses" exists in the system
    When the user opens the Category List page
    And the user searches for "Roses"
    Then only categories matching "Roses" are shown

  @UI @User @TC-UI-CAT-U03
  Scenario: TC-UI-CAT-U03 User can filter categories by parent
    When the user opens the Category List page
    And the user filters by parent "Flowers"
    Then the category list is displayed with at least one record

  @UI @Admin @TC-UI-CAT-U04
  Scenario: TC-UI-CAT-U04 Cancel button returns user to category list
    When the admin opens the Add Category page
    And the user clicks Cancel
    Then the user is on the Category List page

  @UI @User @TC-UI-CAT-U05
  Scenario: TC-UI-CAT-U05 User can sort category list by Name
    When the user opens the Category List page
    And the user clicks sort by Name
    Then the category list is displayed with at least one record
