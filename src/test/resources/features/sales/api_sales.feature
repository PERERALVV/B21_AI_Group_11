Feature: Sales Module API Testing

  Scenario: T-API-1 - Check GET /api/sales endpoint with Admin token
    Given I have a valid Admin authorization token
    When I send a GET request to "/api/sales"
    Then the API response status code should be 200
    And the response body should contain a list of sales records

  Scenario: T-API-2 - Test POST /api/sales/plant/{plantId} (Happy path)
    Given I have a valid Admin authorization token
    And I have a valid plant ID with stock
    When I send a POST request to "/api/sales/plant/{plantId}" with quantity 2
    Then the API response status code should be 201
    And the response body should contain the created sale details
    And the sale record should have quantity 2

  Scenario: T-API-3 - Test POST /api/sales/plant/{plantId} with quantity 0
    Given I have a valid Admin authorization token
    And I have a valid plant ID with stock
    When I send a POST request to "/api/sales/plant/{plantId}" with quantity 0
    Then the API response status code should be 400
    And the response body should contain the validation error

  Scenario: T-API-4 - Check GET /api/sales/{id} endpoint
    Given I have a valid Admin authorization token
    And I have a valid sale ID
    When I send a GET request to "/api/sales/{id}" for that sale ID
    Then the API response status code should be 200
    And the response body should contain the sale details matching the requested sale ID

  Scenario: T-API-5 - Check DELETE /api/sales/{id} endpoint
    Given I have a valid Admin authorization token
    And I have a valid sale ID to delete
    When I send a DELETE request to "/api/sales/{id}" for that sale ID
    Then the API response status code should be 204
    When I send a GET request to "/api/sales/{id}" for the deleted sale ID
    Then the API response status code should be 404

  Scenario: T-API-6 - Check GET /api/sales with normal user token
    Given I have a valid User authorization token
    When I send a GET request to "/api/sales"
    Then the API response status code should be 200
    And the response body should contain a list of sales records

  Scenario: T-API-7 - Check GET /api/sales/page with pagination
    Given I have a valid User authorization token
    When I send a GET request to "/api/sales/page" with page 0, size 5, sort field "soldAt", and sort direction "desc"
    Then the API response status code should be 200
    And the response body should contain a paginated Page JSON object
    And the page object should show size 5 and sorted details

  Scenario: T-API-8 - Check GET /api/sales/{id} with normal user token
    Given I have a valid User authorization token
    And I have a valid sale ID
    When I send a GET request to "/api/sales/{id}" for that sale ID
    Then the API response status code should be 200
    And the response body should contain the sale details matching the requested sale ID

  Scenario: T-API-9 - Test 403 Forbidden on POST (Security)
    Given I have a valid User authorization token
    And I have a valid plant ID with stock
    When I send a POST request to "/api/sales/plant/{plantId}" with quantity 1
    Then the API response status code should be 403

  Scenario: T-API-10 - Test 403 Forbidden on DELETE (Security)
    Given I have a valid User authorization token
    And I have a valid sale ID
    When I send a DELETE request to "/api/sales/{id}" for that sale ID
    Then the API response status code should be 403
