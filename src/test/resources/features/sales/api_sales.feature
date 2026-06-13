Feature: Sales Module API Testing

  @T-API-1 @Admin @API @215542N
  Scenario: T-API-1 - Verify that Check GET /api/sales endpoint with Admin token
    Given I have a valid Admin authorization token
    And there is at least one sale record in the database
    When I send a GET request to "/api/sales"
    Then validate the API response status code should be 200
    And the response body should contain a list of sales records

  @T-API-2 @Admin @API @215542N
  Scenario: T-API-2 - Verify that Test POST /api/sales/plant/{plantId} (Happy path)
    Given I have a valid Admin authorization token
    And I have a valid plant ID with stock
    When I send a POST request to "/api/sales/plant/{plantId}" with quantity 2
    Then validate the API response status code should be 201
    And the response body should contain the created sale details
    And the sale record should have quantity 2

  @T-API-3 @Admin @API @215542N
  Scenario: T-API-3 - Verify that Test POST /api/sales/plant/{plantId} with quantity 0
    Given I have a valid Admin authorization token
    And I have a valid plant ID with stock
    When I send a POST request to "/api/sales/plant/{plantId}" with quantity 0
    Then validate the API response status code should be 400
    And the response body should contain the validation error

  @T-API-4 @Admin @API @215542N
  Scenario: T-API-4 - Verify that Check GET /api/sales/{id} endpoint
    Given I have a valid Admin authorization token
    And I have a valid sale ID
    When I send a GET request to "/api/sales/{id}" for that sale ID
    Then validate the API response status code should be 200
    And the response body should contain the sale details matching the requested sale ID

  @T-API-5 @Admin @API @215542N
  Scenario: T-API-5 - Verify that Check DELETE /api/sales/{id} endpoint
    Given I have a valid Admin authorization token
    And I have a valid sale ID to delete
    When I send a DELETE request to "/api/sales/{id}" for that sale ID
    Then validate the API response status code should be 204
    When I send a GET request to "/api/sales/{id}" for the deleted sale ID
    Then validate the API response status code should be 404

  @T-API-6 @User @API @215542N
  Scenario: T-API-6 - Verify that Check GET /api/sales with normal user token
    Given I have a valid User authorization token
    And there is at least one sale record in the database
    When I send a GET request to "/api/sales"
    Then validate the API response status code should be 200
    And the response body should contain a list of sales records

  @T-API-7 @User @API @215542N
  Scenario: T-API-7 - Verify that Check GET /api/sales/page with pagination
    Given I have a valid User authorization token
    And there are at least 5 sales records in the database
    When I send a GET request to "/api/sales/page" with page 0, size 5, sort field "soldAt", and sort direction "desc"
    Then validate the API response status code should be 200
    And the response body should contain a paginated Page JSON object
    And the page object should show size 5 and sorted details

  @T-API-8 @User @API @215542N
  Scenario: T-API-8 - Verify that Check GET /api/sales/{id} with normal user token
    Given I have a valid User authorization token
    And there is at least one sale record in the database
    And I have a valid sale ID
    When I send a GET request to "/api/sales/{id}" for that sale ID
    Then validate the API response status code should be 200
    And the response body should contain the sale details matching the requested sale ID

  @T-API-9 @User @API @215542N @bug
  Scenario: T-API-9 - Verify that Test 403 Forbidden on POST (Security)
    Given I have a valid User authorization token
    And I have a valid plant ID with stock
    When I send a POST request to "/api/sales/plant/{plantId}" with quantity 1
    Then validate the API response status code should be 403

  @T-API-10 @User @API @215542N @bug
  Scenario: T-API-10 - Verify that Test 403 Forbidden on DELETE (Security)
    Given I have a valid User authorization token
    And there is at least one sale record in the database
    And I have a valid sale ID
    When I send a DELETE request to "/api/sales/{id}" for that sale ID
    Then validate the API response status code should be 403