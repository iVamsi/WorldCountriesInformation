Feature: Countries Screen
  As a user
  I want to see a list of countries
  So that I can explore country information

  Scenario: Display countries screen
    Given the countries screen is displayed
    Then I should see the countries screen
    And I should see the settings button
    And I should see the search field

  Scenario: Navigate to settings
    Given the countries screen is displayed
    When I click the settings button
    Then navigation to settings should be triggered

  Scenario: Show error state when loading fails
    Given the repository returns an error
    And the countries screen is displayed
    When I wait for error state to appear
    Then I should see the error state
    And I should see the retry button
