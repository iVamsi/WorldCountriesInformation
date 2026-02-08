Feature: Settings Screen
  As a user
  I want to configure app settings
  So that I can customize my experience

  Scenario: Display settings screen
    Given the settings screen is displayed
    Then I should see the settings screen
    And I should see the back button
    And I should see the offline mode switch
    And I should see the clear cache button

  Scenario: Navigate back from settings
    Given the settings screen is displayed
    When I click the back button
    Then navigation back should be triggered

  Scenario: Show clear cache dialog
    Given the settings screen is displayed
    When I click the clear cache button
    Then I should see the clear cache dialog

  Scenario: Dismiss clear cache dialog
    Given the settings screen is displayed
    When I click the clear cache button
    And I click the cancel button
    Then the clear cache dialog should be dismissed
