Feature: Authentication Module - API
  # Member: Janith (215543T) - Authentication.
  # Responses are derived from the Swagger contract (/v3/api-docs): POST
  # /api/auth/login documents only 200 -> JwtLoginResponse {token, tokenType}
  # and 400 -> ErrorResponse {status, error, message, timestamp}.
  # @bug scenarios assert that contract and currently FAIL because a bad login
  # returns an undocumented 401 whose body omits "timestamp" (BUG-003).

  @T-API-11 @Admin @API @215543T
  Scenario: T-API-11 - Login with valid admin credentials returns a token
    When I log in to the API with username "admin" and password "admin123"
    Then the auth response status should be 200
    And the success response should match the JwtLoginResponse schema

  # Swagger documents only 400 for a bad login; the app returns an undocumented 401
  # whose body also omits the required "timestamp" (BUG-003).
  @T-API-12 @Admin @API @215543T @bug
  Scenario: T-API-12 - Login with a wrong password must return a 400 ErrorResponse
    When I log in to the API with username "admin" and password "wrongpassword"
    Then the auth response status should be 400
    And the error response should match the Swagger ErrorResponse schema
    And the auth response should not contain a token

  @T-API-13 @Anonymous @API @215543T @bug
  Scenario: T-API-13 - Login with a blank username must return a 400 ErrorResponse
    When I log in to the API with username "" and password "anypassword"
    Then the auth response status should be 400
    And the error response should match the Swagger ErrorResponse schema
    And the auth response should not contain a token

  @T-API-14 @Anonymous @API @215543T @bug
  Scenario: T-API-14 - Login with a blank password must return a 400 ErrorResponse
    When I log in to the API with username "admin" and password ""
    Then the auth response status should be 400
    And the error response should match the Swagger ErrorResponse schema
    And the auth response should not contain a token

  # CSV: logout must return 200/204 AND invalidate the session. The app is stateless
  # with no /api/auth/logout endpoint, so the JWT keeps working -> this asserts the
  # required invalidation and FAILS (BUG: logout does not end the session).
  @T-API-15 @Admin @API @215543T @bug
  Scenario: T-API-15 - Logout must invalidate the admin session token
    Given I have an admin API token
    When I call the logout endpoint with the token
    Then the token should be rejected for "/api/categories"

  @T-API-16 @Admin @User @API @215543T
  Scenario: T-API-16 - Login token carries the correct role per user
    When I log in to the API with username "admin" and password "admin123"
    Then the auth response status should be 200
    And the token in the response should have role "ROLE_ADMIN"
    When I log in to the API with username "testuser" and password "test123"
    Then the auth response status should be 200
    And the token in the response should have role "ROLE_USER"

  @T-API-17 @User @API @215543T
  Scenario: T-API-17 - A user token works across protected GET endpoints
    Given I have a user API token
    When I send a GET to "/api/categories" using the token
    Then the auth response status should be 200
    When I send a GET to "/api/plants" using the token
    Then the auth response status should be 200

  @T-API-18 @User @API @215543T
  Scenario: T-API-18 - A user token is forbidden on an admin-only endpoint
    Given I have a user API token
    When I send a POST to "/api/categories" using the token with category name "qauser1"
    Then the auth response status should be 403

  @T-API-19 @Anonymous @API @215543T
  Scenario: T-API-19 - A protected endpoint without a token is unauthorized
    When I send a GET to "/api/categories" without a token
    Then the auth response status should be 401

  @T-API-20 @Anonymous @API @215543T @bug
  Scenario: T-API-20 - Login with a non-existent user must return a 400 ErrorResponse
    When I log in to the API with username "nobody" and password "anypassword"
    Then the auth response status should be 400
    And the error response should match the Swagger ErrorResponse schema
    And the auth response should not contain a token
