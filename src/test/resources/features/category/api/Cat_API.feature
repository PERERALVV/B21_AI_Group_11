Feature: Admin & User API – Category Operations

  Background:
    Given the user is authenticated via API

  @API @Admin @TC-API-CAT-A01
  Scenario: TC-API-CAT-A01 Verify that Admin retrieves all categories
    When the admin sends GET /api/categories
    Then validate the response status is 200
    And the response body is a list of categories

  @API @Admin @TC-API-CAT-A02
  Scenario: TC-API-CAT-A02 Verify that Admin creates a category with valid name
    When the admin sends POST /api/categories with name "Herbs"
    Then validate the response status is 200 or 201
    And the response contains the name "Herbs"
    And the response contains an id

  @API @Admin @TC-API-CAT-A03
  Scenario: TC-API-CAT-A03 Verify that Admin updates an existing category
    Given category with id 1 exists
    When the admin sends PUT /api/categories/1 with name "Ferns"
    Then validate the response status is 200
    And the response contains the name "Ferns"

  @API @Admin @TC-API-CAT-A04
  Scenario: TC-API-CAT-A04 Verify that Admin deletes an existing category
    Given a test category is created via API
    When the admin sends DELETE /api/categories with stored id
    Then validate the response status is 200 or 204
    And GET for deleted category returns 404

  @API @Admin @TC-API-CAT-A05
  Scenario: TC-API-CAT-A05 Verify that Validation error for name shorter than 3 characters
    When the admin sends POST /api/categories with invalid name "AB"
    Then validate the response status is 400
    And the response contains a name length validation error


  @API @User @TC-API-CAT-U01
  Scenario: TC-API-CAT-U01 Verify that User retrieves all categories
    When the user sends GET /api/categories
    Then validate the response status is 200
    And the response body is a list of categories

  @API @User @TC-API-CAT-U02
  Scenario: TC-API-CAT-U02 Verify that User retrieves a single category by ID
    Given category with id 1 exists
    When the user sends GET /api/categories/1
    Then validate the response status is 200
    And the response contains an id

  @API @User @TC-API-CAT-U03
  Scenario: TC-API-CAT-U03 Verify that User receives 403 trying to create a category
    When the user attempts POST /api/categories with name "Shrubs"
    Then validate the response status is 403

  @API @User @TC-API-CAT-U04
  Scenario: TC-API-CAT-U04 Verify that User receives 403 trying to update a category
    When the user attempts PUT /api/categories/3 with name "Greens"
    Then validate the response status is 403

  @API @User @TC-API-CAT-U05
  Scenario: TC-API-CAT-U05 Verify that User receives 403 trying to delete a category
    When the user attempts DELETE /api/categories/4
    Then validate the response status is 403