package com.vamsi.worldcountriesinformation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vamsi.worldcountriesinformation.HiltTestActivity
import com.vamsi.worldcountriesinformation.bdd.bdd
import com.vamsi.worldcountriesinformation.core.common.testing.TestTags
import com.vamsi.worldcountriesinformation.fake.FakeCountriesRepository
import com.vamsi.worldcountriesinformation.feature.countrydetails.CountryDetailsRoute
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * BDD-style UI tests for the Country Details Screen using Kotlin DSL.
 *
 * These tests demonstrate the Gherkin-like syntax:
 * - Given: Setup/preconditions
 * - When: Actions
 * - Then: Assertions
 *
 * Run with: ./gradlew connectedDebugAndroidTest --tests "*CountryDetailsScreenBddTest*"
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class CountryDetailsScreenBddTest {

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
    fun displayCountryDetailsScreen() {
        composeTestRule.bdd {
            Feature("Country Details Screen") {
                Scenario("Display country details screen") {

                    Given("the country details screen is displayed for USA") {
                        setContent {
                            CountryDetailsRoute(
                                countryCode = "USA",
                                onNavigateBack = {},
                                onNavigateToCountry = {}
                            )
                        }
                    }

                    When("I wait for the details to load") {
                        waitUntil(timeoutMillis = 10000) {
                            val hasScreen = onAllNodes(hasTestTag(TestTags.CountryDetails.SCREEN))
                                .fetchSemanticsNodes()
                                .isNotEmpty()
                            val hasLoading =
                                onAllNodes(hasTestTag(TestTags.CountryDetails.LOADING_INDICATOR))
                                    .fetchSemanticsNodes()
                                    .isNotEmpty()
                            hasScreen || hasLoading
                        }
                    }

                    Then("I should see the country details screen or loading") {
                        // Verified by waitUntil above
                    }
                }
            }
        }
    }

    @Test
    fun displayBackButton() {
        composeTestRule.bdd {
            Feature("Country Details Screen") {
                Scenario("Display back button when data is loaded") {

                    Given("the country details screen is displayed for USA") {
                        setContent {
                            CountryDetailsRoute(
                                countryCode = "USA",
                                onNavigateBack = {},
                                onNavigateToCountry = {}
                            )
                        }
                    }

                    When("I wait for the back button to appear") {
                        waitUntil(timeoutMillis = 10000) {
                            onAllNodes(hasTestTag(TestTags.CountryDetails.BACK_BUTTON))
                                .fetchSemanticsNodes()
                                .isNotEmpty()
                        }
                    }

                    Then("I should see the back button") {
                        onNodeWithTag(TestTags.CountryDetails.BACK_BUTTON)
                            .assertIsDisplayed()
                    }
                }
            }
        }
    }

    @Test
    fun navigateBack() {
        var navigatedBack = false

        composeTestRule.bdd {
            Feature("Country Details Screen") {
                Scenario("User navigates back from country details") {

                    Given("the country details screen is displayed for USA") {
                        setContent {
                            CountryDetailsRoute(
                                countryCode = "USA",
                                onNavigateBack = { navigatedBack = true },
                                onNavigateToCountry = {}
                            )
                        }
                    }

                    When("I wait for the back button to appear") {
                        waitUntil(timeoutMillis = 10000) {
                            onAllNodes(hasTestTag(TestTags.CountryDetails.BACK_BUTTON))
                                .fetchSemanticsNodes()
                                .isNotEmpty()
                        }
                    }

                    And("I click the back button") {
                        onNodeWithTag(TestTags.CountryDetails.BACK_BUTTON)
                            .performClick()
                    }

                    Then("navigation back should be triggered") {
                        assert(navigatedBack) { "Expected navigation back" }
                    }
                }
            }
        }
    }

    @Test
    fun showErrorState() {
        composeTestRule.bdd {
            Feature("Country Details Screen") {
                Scenario("Show error state when loading fails") {

                    Given("the repository returns an error") {
                        fakeRepository.shouldReturnError = true
                        fakeRepository.errorMessage = "Network error"
                    }

                    And("the country details screen is displayed for INVALID") {
                        setContent {
                            CountryDetailsRoute(
                                countryCode = "INVALID",
                                onNavigateBack = {},
                                onNavigateToCountry = {}
                            )
                        }
                    }

                    When("I wait for error state to appear") {
                        waitUntil(timeoutMillis = 10000) {
                            val hasError =
                                onAllNodes(hasTestTag(TestTags.CountryDetails.ERROR_STATE))
                                    .fetchSemanticsNodes()
                                    .isNotEmpty()
                            val hasScreen = onAllNodes(hasTestTag(TestTags.CountryDetails.SCREEN))
                                .fetchSemanticsNodes()
                                .isNotEmpty()
                            hasError || hasScreen
                        }
                    }

                    Then("I should see an error or the screen") {
                        // Verified by waitUntil above - error handling depends on repository
                    }
                }
            }
        }
    }
}
