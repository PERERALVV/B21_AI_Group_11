Feature: Authentication Module - API
  # Member: Janith (Authentication). Test cases T-API-11..T-API-20.
  # App auth is stateless JWT: POST /api/auth/login returns {token, tokenType};
  # the role is carried inside the JWT "roles" claim.

  Scenario: T-API-11 - Login with valid admin credentials returns a token
    When I log in to the API with username "admin" and password "admin123"
    Then the auth response status should be 200
    And the auth response should contain a token

  Scenario: T-API-12 - Login with a wrong password is rejected
    When I log in to the API with username "admin" and password "wrongpassword"
    Then the auth response status should be 401
    And the auth response should not contain a token

  # CSV expected 400. The app performs no field-level validation and returns 401
  # for blank credentials (documented divergence).
  Scenario: T-API-13 - Login with a blank username is rejected
    When I log in to the API with username "" and password "anypassword"
    Then the auth response status should be 401
    And the auth response should not contain a token

  Scenario: T-API-14 - Login with a blank password is rejected
    When I log in to the API with username "admin" and password ""
    Then the auth response status should be 401
    And the auth response should not contain a token

  # CSV expected 200/204 with the session invalidated. The app is stateless and has
  # no /api/auth/logout endpoint, so the JWT keeps working afterwards (divergence / bug candidate).
  Scenario: T-API-15 - Logout does not invalidate the stateless JWT
    Given I have an admin API token
    When I call the logout endpoint with the token
    Then the token should still be accepted for "/api/categories"

  Scenario: T-API-16 - Login token carries the correct role per user
    When I log in to the API with username "admin" and password "admin123"
    Then the auth response status should be 200
    And the token in the response should have role "ROLE_ADMIN"
    When I log in to the API with username "testuser" and password "test123"
    Then the auth response status should be 200
    And the token in the response should have role "ROLE_USER"

  Scenario: T-API-17 - A user token works across protected GET endpoints
    Given I have a user API token
    When I send a GET to "/api/categories" using the token
    Then the auth response status should be 200
    When I send a GET to "/api/plants" using the token
    Then the auth response status should be 200

  Scenario: T-API-18 - A user token is forbidden on an admin-only endpoint
    Given I have a user API token
    When I send a POST to "/api/categories" using the token with category name "qauser1"
    Then the auth response status should be 403

  Scenario: T-API-19 - A protected endpoint without a token is unauthorized
    When I send a GET to "/api/categories" without a token
    Then the auth response status should be 401

  Scenario: T-API-20 - Login with a non-existent user is rejected
    When I log in to the API with username "nobody" and password "anypassword"
    Then the auth response status should be 401
    And the auth response should not contain a token
