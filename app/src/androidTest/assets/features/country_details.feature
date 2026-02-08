Feature: Country Details Screen
  As a user
  I want to see detailed information about a country
  So that I can learn more about it

  Scenario: Display country details screen
    Given the country details screen is displayed for "USA"
    When I wait for the details to load
    Then I should see the country details screen

  Scenario: Display back button
    Given the country details screen is displayed for "USA"
    When I wait for the back button to appear
    Then I should see the back button

  Scenario: Navigate back from country details
    Given the country details screen is displayed for "USA"
    When I wait for the back button to appear
    And I click the back button
    Then navigation back should be triggered

  Scenario: Show error state when loading fails
    Given the repository returns an error
    And the country details screen is displayed for "INVALID"
    When I wait for error state to appear
    Then I should see the error state
