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
 * UI tests for the Country Details screen.
 *
 * These tests verify:
 * - Country details display correctly
 * - Navigation controls work
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class CountryDetailsScreenTest {

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
    fun countryDetailsScreen_displaysScreenOrLoading() {
        // When: Country details screen is displayed
        composeTestRule.setContent {
            com.vamsi.worldcountriesinformation.feature.countrydetails.CountryDetailsRoute(
                countryCode = "USA",
                onNavigateBack = {},
                onNavigateToCountry = {}
            )
        }

        // Wait for either screen or loading to appear
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

    @Test
    fun countryDetailsScreen_displaysBackButton_whenDataLoaded() {
        // When: Country details screen is displayed
        composeTestRule.setContent {
            com.vamsi.worldcountriesinformation.feature.countrydetails.CountryDetailsRoute(
                countryCode = "USA",
                onNavigateBack = {},
                onNavigateToCountry = {}
            )
        }

        // Wait for back button to appear
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodes(hasTestTag(TestTags.CountryDetails.BACK_BUTTON))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Then: Back button should be visible
        composeTestRule
            .onNodeWithTag(TestTags.CountryDetails.BACK_BUTTON)
            .assertIsDisplayed()
    }

    @Test
    fun countryDetailsScreen_navigatesBack_whenBackButtonClicked() {
        var navigatedBack = false

        // When: Country details screen is displayed
        composeTestRule.setContent {
            com.vamsi.worldcountriesinformation.feature.countrydetails.CountryDetailsRoute(
                countryCode = "USA",
                onNavigateBack = { navigatedBack = true },
                onNavigateToCountry = {}
            )
        }

        // Wait for back button to appear
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodes(hasTestTag(TestTags.CountryDetails.BACK_BUTTON))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // And: User clicks back button
        composeTestRule
            .onNodeWithTag(TestTags.CountryDetails.BACK_BUTTON)
            .performClick()

        // Then: Navigation callback should be invoked
        assert(navigatedBack) { "Expected navigation back" }
    }
}
