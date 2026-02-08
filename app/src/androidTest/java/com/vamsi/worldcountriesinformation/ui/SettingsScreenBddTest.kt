package com.vamsi.worldcountriesinformation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vamsi.worldcountriesinformation.HiltTestActivity
import com.vamsi.worldcountriesinformation.bdd.bdd
import com.vamsi.worldcountriesinformation.core.common.testing.TestTags
import com.vamsi.worldcountriesinformation.feature.settings.SettingsScreen
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * BDD-style UI tests for the Settings Screen using Kotlin DSL.
 *
 * These tests demonstrate the Gherkin-like syntax:
 * - Given: Setup/preconditions
 * - When: Actions
 * - Then: Assertions
 *
 * Run with: ./gradlew connectedDebugAndroidTest --tests "*SettingsScreenBddTest*"
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class SettingsScreenBddTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun displaySettingsScreen() {
        composeTestRule.bdd {
            Feature("Settings Screen") {
                Scenario("Display settings screen with all elements") {

                    Given("the settings screen is displayed") {
                        setContent {
                            SettingsScreen(onNavigateBack = {})
                        }
                    }

                    Then("I should see the settings screen") {
                        onNodeWithTag(TestTags.Settings.SCREEN)
                            .assertIsDisplayed()
                    }

                    And("I should see the back button") {
                        onNodeWithTag(TestTags.Settings.BACK_BUTTON)
                            .assertIsDisplayed()
                    }

                    And("I should see the offline mode switch") {
                        onNodeWithTag(TestTags.Settings.OFFLINE_MODE_SWITCH)
                            .assertIsDisplayed()
                    }

                    And("I should see the clear cache button") {
                        onNodeWithTag(TestTags.Settings.CLEAR_CACHE_BUTTON)
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
            Feature("Settings Screen") {
                Scenario("User navigates back from settings") {

                    Given("the settings screen is displayed") {
                        setContent {
                            SettingsScreen(onNavigateBack = { navigatedBack = true })
                        }
                    }

                    When("I click the back button") {
                        onNodeWithTag(TestTags.Settings.BACK_BUTTON)
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
    fun showClearCacheDialog() {
        composeTestRule.bdd {
            Feature("Settings Screen") {
                Scenario("Show clear cache confirmation dialog") {

                    Given("the settings screen is displayed") {
                        setContent {
                            SettingsScreen(onNavigateBack = {})
                        }
                    }

                    When("I click the clear cache button") {
                        onNodeWithTag(TestTags.Settings.CLEAR_CACHE_BUTTON)
                            .performClick()
                    }

                    Then("I should see the clear cache dialog") {
                        onNodeWithTag(TestTags.Settings.CLEAR_CACHE_DIALOG)
                            .assertIsDisplayed()
                    }

                    And("I should see the confirm button") {
                        onNodeWithTag(TestTags.Settings.CLEAR_CACHE_CONFIRM)
                            .assertIsDisplayed()
                    }

                    And("I should see the cancel button") {
                        onNodeWithTag(TestTags.Settings.CLEAR_CACHE_CANCEL)
                            .assertIsDisplayed()
                    }
                }
            }
        }
    }

    @Test
    fun dismissClearCacheDialog() {
        composeTestRule.bdd {
            Feature("Settings Screen") {
                Scenario("Dismiss clear cache dialog by clicking cancel") {

                    Given("the settings screen is displayed") {
                        setContent {
                            SettingsScreen(onNavigateBack = {})
                        }
                    }

                    When("I click the clear cache button") {
                        onNodeWithTag(TestTags.Settings.CLEAR_CACHE_BUTTON)
                            .performClick()
                    }

                    And("I click the cancel button") {
                        onNodeWithTag(TestTags.Settings.CLEAR_CACHE_CANCEL)
                            .performClick()
                    }

                    Then("the clear cache dialog should be dismissed") {
                        onNodeWithTag(TestTags.Settings.CLEAR_CACHE_DIALOG)
                            .assertDoesNotExist()
                    }
                }
            }
        }
    }
}
