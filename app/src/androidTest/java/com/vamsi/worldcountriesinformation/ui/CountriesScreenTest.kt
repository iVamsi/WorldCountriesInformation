package com.vamsi.worldcountriesinformation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vamsi.worldcountriesinformation.HiltTestActivity
import com.vamsi.worldcountriesinformation.core.common.testing.TestTags
import com.vamsi.worldcountriesinformation.fake.FakeCountriesRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * UI tests for the Countries List screen.
 *
 * These tests verify:
 * - Countries list displays correctly
 * - Loading and error states are shown appropriately
 * - Navigation to settings works
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class CountriesScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject
    lateinit var fakeRepository: FakeCountriesRepository

    @Before
    fun setup() {
        hiltRule.inject()
        fakeRepository.reset()
    }

    @Test
    fun countriesScreen_displaysScreen() {
        // When: Countries screen is displayed
        composeTestRule.setContent {
            com.vamsi.worldcountriesinformation.feature.countries.CountriesScreen(
                onNavigateToDetails = {},
                onNavigateToSettings = {}
            )
        }

        // Then: Screen should be visible
        composeTestRule
            .onNodeWithTag(TestTags.Countries.SCREEN)
            .assertIsDisplayed()
    }

    @Test
    fun countriesScreen_displaysCountriesListOrLoading() {
        // When: Countries screen is displayed
        composeTestRule.setContent {
            com.vamsi.worldcountriesinformation.feature.countries.CountriesScreen(
                onNavigateToDetails = {},
                onNavigateToSettings = {}
            )
        }

        // Wait for either list or loading to appear
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            val hasCountriesList = composeTestRule
                .onAllNodes(hasTestTag(TestTags.Countries.COUNTRIES_LIST))
                .fetchSemanticsNodes()
                .isNotEmpty()
            val hasLoading = composeTestRule
                .onAllNodes(hasTestTag(TestTags.Countries.LOADING_INDICATOR))
                .fetchSemanticsNodes()
                .isNotEmpty()
            hasCountriesList || hasLoading
        }
    }

    @Test
    fun countriesScreen_showsErrorState_whenLoadFails() {
        // Given: Repository returns error
        fakeRepository.shouldReturnError = true
        fakeRepository.errorMessage = "Network error"

        // When: Countries screen is displayed
        composeTestRule.setContent {
            com.vamsi.worldcountriesinformation.feature.countries.CountriesScreen(
                onNavigateToDetails = {},
                onNavigateToSettings = {}
            )
        }

        // Wait for error state to appear
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodes(hasTestTag(TestTags.Countries.ERROR_STATE))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Then: Error state should be visible
        composeTestRule
            .onNodeWithTag(TestTags.Countries.ERROR_STATE)
            .assertIsDisplayed()

        // And: Retry button should be visible
        composeTestRule
            .onNodeWithTag(TestTags.Countries.ERROR_RETRY_BUTTON)
            .assertIsDisplayed()
    }

    @Test
    fun settingsButton_isDisplayed() {
        // When: Countries screen is displayed
        composeTestRule.setContent {
            com.vamsi.worldcountriesinformation.feature.countries.CountriesScreen(
                onNavigateToDetails = {},
                onNavigateToSettings = {}
            )
        }

        // Then: Settings button should be visible
        composeTestRule
            .onNodeWithTag(TestTags.Countries.SETTINGS_BUTTON)
            .assertIsDisplayed()
    }

    @Test
    fun countriesScreen_navigatesToSettings_whenSettingsButtonClicked() {
        var navigatedToSettings = false

        // When: Countries screen is displayed
        composeTestRule.setContent {
            com.vamsi.worldcountriesinformation.feature.countries.CountriesScreen(
                onNavigateToDetails = {},
                onNavigateToSettings = { navigatedToSettings = true }
            )
        }

        // And: User clicks settings button
        composeTestRule
            .onNodeWithTag(TestTags.Countries.SETTINGS_BUTTON)
            .performClick()

        // Then: Navigation callback should be invoked
        assert(navigatedToSettings) { "Expected navigation to settings" }
    }

    @Test
    fun searchField_isDisplayed() {
        // When: Countries screen is displayed
        composeTestRule.setContent {
            com.vamsi.worldcountriesinformation.feature.countries.CountriesScreen(
                onNavigateToDetails = {},
                onNavigateToSettings = {}
            )
        }

        // Then: Search field should be visible
        composeTestRule
            .onNodeWithTag(TestTags.Countries.SEARCH_FIELD)
            .assertIsDisplayed()
    }
}
