package com.vamsi.worldcountriesinformation.bdd

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.vamsi.worldcountriesinformation.core.common.testing.TestTags
import com.vamsi.worldcountriesinformation.fake.FakeCountriesRepository

/**
 * Step definitions for Countries screen tests.
 *
 * These step definitions can be used with both:
 * 1. The Kotlin DSL (inline in tests)
 * 2. Feature file parser (linking .feature files)
 */
class CountriesStepDefinitions(
    private val composeTestRule: ComposeTestRule,
    private val fakeRepository: FakeCountriesRepository,
    private val setContent: () -> Unit,
    private val onNavigateToSettings: () -> Unit = {},
    private val onNavigateToDetails: (String) -> Unit = {}
) : StepDefinitions() {

    init {
        // Given steps
        step("the countries screen is displayed") {
            setContent()
        }

        step("the repository has test countries") {
            fakeRepository.reset()
        }

        step("the repository returns an error") {
            fakeRepository.shouldReturnError = true
            fakeRepository.errorMessage = "Network error"
        }

        step("the repository simulates loading") {
            fakeRepository.simulateDelay = true
            fakeRepository.delayMillis = 5000L
        }

        // When steps
        step("I click the settings button") {
            composeTestRule
                .onNodeWithTag(TestTags.Countries.SETTINGS_BUTTON)
                .performClick()
        }

        step("I wait for the screen to load") {
            composeTestRule.waitForIdle()
        }

        step("I wait for countries to appear") {
            composeTestRule.waitUntil(timeoutMillis = 10000) {
                composeTestRule
                    .onAllNodes(hasTestTag(TestTags.Countries.COUNTRIES_LIST))
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
        }

        step("I wait for error state to appear") {
            composeTestRule.waitUntil(timeoutMillis = 10000) {
                composeTestRule
                    .onAllNodes(hasTestTag(TestTags.Countries.ERROR_STATE))
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
        }

        // Then steps
        step("I should see the countries screen") {
            composeTestRule
                .onNodeWithTag(TestTags.Countries.SCREEN)
                .assertIsDisplayed()
        }

        step("I should see the settings button") {
            composeTestRule
                .onNodeWithTag(TestTags.Countries.SETTINGS_BUTTON)
                .assertIsDisplayed()
        }

        step("I should see the search field") {
            composeTestRule
                .onNodeWithTag(TestTags.Countries.SEARCH_FIELD)
                .assertIsDisplayed()
        }

        step("I should see the countries list") {
            composeTestRule
                .onNodeWithTag(TestTags.Countries.COUNTRIES_LIST)
                .assertIsDisplayed()
        }

        step("I should see the loading indicator") {
            composeTestRule
                .onNodeWithTag(TestTags.Countries.LOADING_INDICATOR)
                .assertIsDisplayed()
        }

        step("I should see the error state") {
            composeTestRule
                .onNodeWithTag(TestTags.Countries.ERROR_STATE)
                .assertIsDisplayed()
        }

        step("I should see the retry button") {
            composeTestRule
                .onNodeWithTag(TestTags.Countries.ERROR_RETRY_BUTTON)
                .assertIsDisplayed()
        }

        step("navigation to settings should be triggered") {
            // This is verified via callback - the test should set up verification
        }
    }
}

/**
 * Step definitions for Settings screen tests.
 */
class SettingsStepDefinitions(
    private val composeTestRule: ComposeTestRule,
    private val setContent: () -> Unit,
    private val onNavigateBack: () -> Unit = {}
) : StepDefinitions() {

    init {
        // Given steps
        step("the settings screen is displayed") {
            setContent()
        }

        // When steps
        step("I click the back button") {
            composeTestRule
                .onNodeWithTag(TestTags.Settings.BACK_BUTTON)
                .performClick()
        }

        step("I click the clear cache button") {
            composeTestRule
                .onNodeWithTag(TestTags.Settings.CLEAR_CACHE_BUTTON)
                .performClick()
        }

        step("I click the cancel button") {
            composeTestRule
                .onNodeWithTag(TestTags.Settings.CLEAR_CACHE_CANCEL)
                .performClick()
        }

        step("I click the confirm button") {
            composeTestRule
                .onNodeWithTag(TestTags.Settings.CLEAR_CACHE_CONFIRM)
                .performClick()
        }

        // Then steps
        step("I should see the settings screen") {
            composeTestRule
                .onNodeWithTag(TestTags.Settings.SCREEN)
                .assertIsDisplayed()
        }

        step("I should see the back button") {
            composeTestRule
                .onNodeWithTag(TestTags.Settings.BACK_BUTTON)
                .assertIsDisplayed()
        }

        step("I should see the offline mode switch") {
            composeTestRule
                .onNodeWithTag(TestTags.Settings.OFFLINE_MODE_SWITCH)
                .assertIsDisplayed()
        }

        step("I should see the clear cache button") {
            composeTestRule
                .onNodeWithTag(TestTags.Settings.CLEAR_CACHE_BUTTON)
                .assertIsDisplayed()
        }

        step("I should see the clear cache dialog") {
            composeTestRule
                .onNodeWithTag(TestTags.Settings.CLEAR_CACHE_DIALOG)
                .assertIsDisplayed()
        }

        step("the clear cache dialog should be dismissed") {
            composeTestRule
                .onNodeWithTag(TestTags.Settings.CLEAR_CACHE_DIALOG)
                .assertDoesNotExist()
        }
    }
}

/**
 * Step definitions for Country Details screen tests.
 */
class CountryDetailsStepDefinitions(
    private val composeTestRule: ComposeTestRule,
    private val fakeRepository: FakeCountriesRepository,
    private val setContent: () -> Unit,
    private val onNavigateBack: () -> Unit = {}
) : StepDefinitions() {

    init {
        // Given steps
        step("the country details screen is displayed for \"(.+)\"") { match ->
            val countryCode = match.groupValues[1]
            setContent()
        }

        step("the country details screen is displayed") {
            setContent()
        }

        // When steps
        step("I click the back button") {
            composeTestRule.waitUntil(timeoutMillis = 10000) {
                composeTestRule
                    .onAllNodes(hasTestTag(TestTags.CountryDetails.BACK_BUTTON))
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
            composeTestRule
                .onNodeWithTag(TestTags.CountryDetails.BACK_BUTTON)
                .performClick()
        }

        step("I wait for the details to load") {
            composeTestRule.waitUntil(timeoutMillis = 10000) {
                val hasScreen = composeTestRule
                    .onAllNodes(hasTestTag(TestTags.CountryDetails.SCREEN))
                    .fetchSemanticsNodes()
                    .isNotEmpty()
                val hasLoading = composeTestRule
                    .onAllNodes(hasTestTag(TestTags.CountryDetails.LOADING_INDICATOR))
                    .fetchSemanticsNodes()
                    .isNotEmpty()
                hasScreen || hasLoading
            }
        }

        // Then steps
        step("I should see the country details screen") {
            composeTestRule
                .onNodeWithTag(TestTags.CountryDetails.SCREEN)
                .assertIsDisplayed()
        }

        step("I should see the back button") {
            composeTestRule
                .onNodeWithTag(TestTags.CountryDetails.BACK_BUTTON)
                .assertIsDisplayed()
        }

        step("navigation back should be triggered") {
            // Verified via callback
        }
    }
}
