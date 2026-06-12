Feature: Sales Module API Testing

  @API_GET_SALES_ADM @Admin @API @215542N
  Scenario: API_GET_SALES_ADM - Check GET /api/sales endpoint with Admin token
    Given I have a valid Admin authorization token
    And there is at least one sale record in the database
    When I send a GET request to "/api/sales"
    Then the API response status code should be 200
    And the response body should contain a list of sales records

  @API_POST_SALES_PLANT_ADM @Admin @API @215542N
  Scenario: API_POST_SALES_PLANT_ADM - Test POST /api/sales/plant/{plantId} (Happy path)
    Given I have a valid Admin authorization token
    And I have a valid plant ID with stock
    When I send a POST request to "/api/sales/plant/{plantId}" with quantity 2
    Then the API response status code should be 201
    And the response body should contain the created sale details
    And the sale record should have quantity 2

  @API_POST_SALES_PLANT_ADM_BADQTY @Admin @API @215542N
  Scenario: API_POST_SALES_PLANT_ADM_BADQTY - Test POST /api/sales/plant/{plantId} with quantity 0
    Given I have a valid Admin authorization token
    And I have a valid plant ID with stock
    When I send a POST request to "/api/sales/plant/{plantId}" with quantity 0
    Then the API response status code should be 400
    And the response body should contain the validation error

  @API_GET_SALE_ADM @Admin @API @215542N
  Scenario: API_GET_SALE_ADM - Check GET /api/sales/{id} endpoint
    Given I have a valid Admin authorization token
    And I have a valid sale ID
    When I send a GET request to "/api/sales/{id}" for that sale ID
    Then the API response status code should be 200
    And the response body should contain the sale details matching the requested sale ID

  @API_DELETE_SALE_ADM @Admin @API @215542N
  Scenario: API_DELETE_SALE_ADM - Check DELETE /api/sales/{id} endpoint
    Given I have a valid Admin authorization token
    And I have a valid sale ID to delete
    When I send a DELETE request to "/api/sales/{id}" for that sale ID
    Then the API response status code should be 204
    When I send a GET request to "/api/sales/{id}" for the deleted sale ID
    Then the API response status code should be 404

  @API_GET_SALES_USR @User @API @215542N
  Scenario: API_GET_SALES_USR - Check GET /api/sales with normal user token
    Given I have a valid User authorization token
    And there is at least one sale record in the database
    When I send a GET request to "/api/sales"
    Then the API response status code should be 200
    And the response body should contain a list of sales records

  @API_GET_SALES_PAGE_USR @User @API @215542N
  Scenario: API_GET_SALES_PAGE_USR - Check GET /api/sales/page with pagination
    Given I have a valid User authorization token
    And there are at least 5 sales records in the database
    When I send a GET request to "/api/sales/page" with page 0, size 5, sort field "soldAt", and sort direction "desc"
    Then the API response status code should be 200
    And the response body should contain a paginated Page JSON object
    And the page object should show size 5 and sorted details

  @API_GET_SALE_USR @User @API @215542N
  Scenario: API_GET_SALE_USR - Check GET /api/sales/{id} with normal user token
    Given I have a valid User authorization token
    And there is at least one sale record in the database
    And I have a valid sale ID
    When I send a GET request to "/api/sales/{id}" for that sale ID
    Then the API response status code should be 200
    And the response body should contain the sale details matching the requested sale ID

  @API_POST_SALES_PLANT_USR_FORBID @User @API @215542N
  Scenario: API_POST_SALES_PLANT_USR_FORBID - Test 403 Forbidden on POST (Security)
    Given I have a valid User authorization token
    And I have a valid plant ID with stock
    When I send a POST request to "/api/sales/plant/{plantId}" with quantity 1
    Then the API response status code should be 403

  @API_DELETE_SALE_USR_FORBID @User @API @215542N
  Scenario: API_DELETE_SALE_USR_FORBID - Test 403 Forbidden on DELETE (Security)
    Given I have a valid User authorization token
    And there is at least one sale record in the database
    And I have a valid sale ID
    When I send a DELETE request to "/api/sales/{id}" for that sale ID
    Then the API response status code should be 403
