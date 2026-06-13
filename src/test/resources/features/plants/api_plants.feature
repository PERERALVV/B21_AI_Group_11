Feature: Plants Module API Testing

  @T-API-21 @Admin @API @215533M
  Scenario: T-API-21 - Verify that Test POST /api/plants/category/{categoryId} endpoint as Admin
    Given I am authorized as Admin for the plants API
    And I have a valid sub-category ID for plant creation
    When I send a POST request to create a plant with name "BambooAPI" price 99.99 and quantity 20
    Then validate the plants API response status code should be 201
    And the created plant response should contain name "BambooAPI"
    And the GET all plants response should include the newly created plant

  @T-API-22 @Admin @API @215533M
  Scenario: T-API-22 - Verify that Test POST /api/plants with plant name shorter than 3 characters
    Given I am authorized as Admin for the plants API
    And I have a valid sub-category ID for plant creation
    When I send a POST request to create a plant with name "AB" price 50.00 and quantity 10
    Then validate the plants API response status code should be 400
    And the plants response body should contain validation message "Plant name must be between 3 and 25 characters"

  @T-API-23 @Admin @API @215533M
  Scenario: T-API-23 - Verify that Test POST /api/plants with price equal to zero
    Given I am authorized as Admin for the plants API
    And I have a valid sub-category ID for plant creation
    When I send a POST request to create a plant with name "ValidName" price 0.0 and quantity 10
    Then validate the plants API response status code should be 400
    And the plants response body should contain validation message "Price must be greater than 0"

  @T-API-24 @Admin @API @215533M
  Scenario: T-API-24 - Verify that Test PUT /api/plants/{id} updates an existing plant successfully
    Given I am authorized as Admin for the plants API
    And I have a valid plant ID for update operation
    When I send a PUT request to update the plant with name "UpdatedPlant" price 75.00 and quantity 15
    Then validate the plants API response status code should be 200

  @T-API-25 @Admin @API @215533M
  Scenario: T-API-25 - Verify that Test DELETE /api/plants/{id} removes the plant
    Given I am authorized as Admin for the plants API
    And I have created a plant specifically for deletion
    When I send a DELETE request to remove the plant by ID
    Then validate the plants API response status code should be 204

  @T-API-26 @User @API @215533M
  Scenario: T-API-26 - Verify that Test GET /api/plants as User
    Given I am authorized as User for the plants API
    When I send a GET request to retrieve all plants via API
    Then validate the plants API response status code should be 200
    And the response body should be a JSON array containing plant records
    And each plant record should contain id name price quantity and category fields

  @T-API-27 @User @API @215533M
  Scenario: T-API-27 - Verify that Test GET /api/plants/{id} as User
    Given I am authorized as User for the plants API
    And I have a valid plant ID to retrieve via API
    When I send a GET request to retrieve a single plant by its ID
    Then validate the plants API response status code should be 200
    And the single plant response body should contain the correct plant ID

  @T-API-28 @User @API @215533M
  Scenario: T-API-28 - Verify that Test GET /api/plants/paged as User
    Given I am authorized as User for the plants API
    When I send a GET request to the plants paged endpoint with page 0 size 5
    Then validate the plants API response status code should be 200
    And the paged plants response should contain pagination fields content totalElements totalPages number and size

  @T-API-29 @User @API @215533M
  Scenario: T-API-29 - Verify that Test POST /api/plants/category/{categoryId} as User returns 403 Forbidden
    Given I am authorized as User for the plants API
    And I have a valid sub-category ID for plant creation
    When I send a POST request to create a plant with name "ForbidPlant" price 50.00 and quantity 5
    Then validate the plants API response status code should be 403

  @T-API-30 @User @API @215533M
  Scenario: T-API-30 - Verify that Test DELETE /api/plants/{id} as User returns 403 Forbidden
    Given I am authorized as User for the plants API
    And I have a valid plant ID to retrieve via API
    When I send a DELETE request to remove the plant by ID
    Then validate the plants API response status code should be 403

  @T-API-32 @Admin @API @215533M @BugDetection
  Scenario: T-API-32 - Verify that Test PUT /api/plants/{id} with full swagger request body including id and category object
    Given I am authorized as Admin for the plants API
    And I have a valid sub-category ID for plant creation
    And I have a valid plant ID for update operation
    When I send a PUT request with the full swagger body to update the plant with name "UpdatedPlant" price 75.00 and quantity 5
    Then validate the plants API response status code should be 200
    And the plants API response body should contain the updated plant name "UpdatedPlant"

  @T-API-31 @Admin @API @215533M @BugDetection
  Scenario: T-API-31 - Verify that Test DELETE /api/plants/{id} on already-deleted plant returns 404 not 204
    Given I am authorized as Admin for the plants API
    And I have created a plant specifically for deletion
    When I send a DELETE request to remove the plant by ID
    Then validate the plants API response status code should be 204
    When I send a DELETE request to remove the plant by ID
    Then validate the plants API response status code should be 404
