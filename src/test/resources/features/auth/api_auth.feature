Feature: Authentication Module - API
  # Member: Janith (215543T) - Authentication.
  # Responses are derived from the Swagger contract (/v3/api-docs): POST
  # /api/auth/login documents only 200 -> JwtLoginResponse {token, tokenType}
  # and 400 -> ErrorResponse {status, error, message, timestamp}.
  # @bug @Contract scenarios assert that contract and currently FAIL because a
  # bad login returns an undocumented 401 whose body omits "timestamp" (BUG-003).

  @T-API-POST-AU-ADM-1 @Admin @API @215543T @Contract
  Scenario: T-API-POST-AU-ADM-1 - Login with valid admin credentials returns a token
    When I log in to the API with username "admin" and password "admin123"
    Then the auth response status should be 200
    And the success response should match the JwtLoginResponse schema

  # Swagger documents only 400 for a bad login; the app returns an undocumented 401
  # whose body also omits the required "timestamp" (BUG-003).
  @T-API-POST-AU-ADM-2 @Admin @API @215543T @bug @Contract
  Scenario: T-API-POST-AU-ADM-2 - Login with a wrong password must return a 400 ErrorResponse
    When I log in to the API with username "admin" and password "wrongpassword"
    Then the auth response status should be 400
    And the error response should match the Swagger ErrorResponse schema
    And the auth response should not contain a token

  # CSV expected 200/204 with the session invalidated. The app is stateless and has
  # no /api/auth/logout endpoint, so the JWT keeps working afterwards (divergence / bug candidate).
  @T-API-POST-AU-ADM-3 @Admin @API @215543T
  Scenario: T-API-POST-AU-ADM-3 - Logout does not invalidate the stateless JWT
    Given I have an admin API token
    When I call the logout endpoint with the token
    Then the token should still be accepted for "/api/categories"

  @T-API-POST-AU-ADM-4 @Admin @User @API @215543T
  Scenario: T-API-POST-AU-ADM-4 - Login token carries the correct role per user
    When I log in to the API with username "admin" and password "admin123"
    Then the auth response status should be 200
    And the token in the response should have role "ROLE_ADMIN"
    When I log in to the API with username "testuser" and password "test123"
    Then the auth response status should be 200
    And the token in the response should have role "ROLE_USER"

  @T-API-GET-AU-USR-1 @User @API @215543T
  Scenario: T-API-GET-AU-USR-1 - A user token works across protected GET endpoints
    Given I have a user API token
    When I send a GET to "/api/categories" using the token
    Then the auth response status should be 200
    When I send a GET to "/api/plants" using the token
    Then the auth response status should be 200

  @T-API-POST-AU-USR-2 @User @API @215543T
  Scenario: T-API-POST-AU-USR-2 - A user token is forbidden on an admin-only endpoint
    Given I have a user API token
    When I send a POST to "/api/categories" using the token with category name "qauser1"
    Then the auth response status should be 403

  # Per the Swagger contract a blank-credential login is a 400 ErrorResponse;
  # the app returns an undocumented 401 (BUG-003).
  @T-API-POST-AU-ANON-1 @Anonymous @API @215543T @bug @Contract
  Scenario: T-API-POST-AU-ANON-1 - Login with a blank username must return a 400 ErrorResponse
    When I log in to the API with username "" and password "anypassword"
    Then the auth response status should be 400
    And the error response should match the Swagger ErrorResponse schema
    And the auth response should not contain a token

  @T-API-POST-AU-ANON-2 @Anonymous @API @215543T @bug @Contract
  Scenario: T-API-POST-AU-ANON-2 - Login with a blank password must return a 400 ErrorResponse
    When I log in to the API with username "admin" and password ""
    Then the auth response status should be 400
    And the error response should match the Swagger ErrorResponse schema
    And the auth response should not contain a token

  @T-API-GET-AU-ANON-3 @Anonymous @API @215543T
  Scenario: T-API-GET-AU-ANON-3 - A protected endpoint without a token is unauthorized
    When I send a GET to "/api/categories" without a token
    Then the auth response status should be 401

  @T-API-POST-AU-ANON-4 @Anonymous @API @215543T @bug @Contract
  Scenario: T-API-POST-AU-ANON-4 - Login with a non-existent user must return a 400 ErrorResponse
    When I log in to the API with username "nobody" and password "anypassword"
    Then the auth response status should be 400
    And the error response should match the Swagger ErrorResponse schema
    And the auth response should not contain a token
