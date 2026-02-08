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
import com.vamsi.worldcountriesinformation.feature.countries.CountriesScreen
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * BDD-style UI tests for the Countries Screen using Kotlin DSL.
 *
 * These tests demonstrate the Gherkin-like syntax:
 * - Given: Setup/preconditions
 * - When: Actions
 * - Then: Assertions
 *
 * Run with: ./gradlew connectedDebugAndroidTest --tests "*CountriesScreenBddTest*"
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class CountriesScreenBddTest {

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
    fun displayCountriesScreen() {
        composeTestRule.bdd {
            Feature("Countries Screen") {
                Scenario("Display countries screen with all elements") {

                    Given("the countries screen is displayed") {
                        setContent {
                            CountriesScreen(
                                onNavigateToDetails = {},
                                onNavigateToSettings = {}
                            )
                        }
                    }

                    Then("I should see the countries screen") {
                        onNodeWithTag(TestTags.Countries.SCREEN)
                            .assertIsDisplayed()
                    }

                    And("I should see the settings button") {
                        onNodeWithTag(TestTags.Countries.SETTINGS_BUTTON)
                            .assertIsDisplayed()
                    }

                    And("I should see the search field") {
                        onNodeWithTag(TestTags.Countries.SEARCH_FIELD)
                            .assertIsDisplayed()
                    }
                }
            }
        }
    }

    @Test
    fun navigateToSettings() {
        var navigatedToSettings = false

        composeTestRule.bdd {
            Feature("Countries Screen") {
                Scenario("User navigates to settings") {

                    Given("the countries screen is displayed") {
                        setContent {
                            CountriesScreen(
                                onNavigateToDetails = {},
                                onNavigateToSettings = { navigatedToSettings = true }
                            )
                        }
                    }

                    When("I click the settings button") {
                        onNodeWithTag(TestTags.Countries.SETTINGS_BUTTON)
                            .performClick()
                    }

                    Then("navigation to settings should be triggered") {
                        assert(navigatedToSettings) { "Expected navigation to settings" }
                    }
                }
            }
        }
    }

    @Test
    fun showErrorState() {
        composeTestRule.bdd {
            Feature("Countries Screen") {
                Scenario("Show error state when loading fails") {

                    Given("the repository returns an error") {
                        fakeRepository.shouldReturnError = true
                        fakeRepository.errorMessage = "Network error"
                    }

                    And("the countries screen is displayed") {
                        setContent {
                            CountriesScreen(
                                onNavigateToDetails = {},
                                onNavigateToSettings = {}
                            )
                        }
                    }

                    When("I wait for error state to appear") {
                        waitUntil(timeoutMillis = 10000) {
                            onAllNodes(hasTestTag(TestTags.Countries.ERROR_STATE))
                                .fetchSemanticsNodes()
                                .isNotEmpty()
                        }
                    }

                    Then("I should see the error state") {
                        onNodeWithTag(TestTags.Countries.ERROR_STATE)
                            .assertIsDisplayed()
                    }

                    And("I should see the retry button") {
                        onNodeWithTag(TestTags.Countries.ERROR_RETRY_BUTTON)
                            .assertIsDisplayed()
                    }
                }
            }
        }
    }

    @Test
    fun showLoadingOrList() {
        composeTestRule.bdd {
            Feature("Countries Screen") {
                Scenario("Display loading or countries list") {

                    Given("the countries screen is displayed") {
                        setContent {
                            CountriesScreen(
                                onNavigateToDetails = {},
                                onNavigateToSettings = {}
                            )
                        }
                    }

                    When("I wait for content to load") {
                        waitUntil(timeoutMillis = 10000) {
                            val hasCountriesList =
                                onAllNodes(hasTestTag(TestTags.Countries.COUNTRIES_LIST))
                                    .fetchSemanticsNodes()
                                    .isNotEmpty()
                            val hasLoading =
                                onAllNodes(hasTestTag(TestTags.Countries.LOADING_INDICATOR))
                                    .fetchSemanticsNodes()
                                    .isNotEmpty()
                            hasCountriesList || hasLoading
                        }
                    }

                    Then("I should see either loading or countries list") {
                        // Verified by waitUntil above
                    }
                }
            }
        }
    }
}
