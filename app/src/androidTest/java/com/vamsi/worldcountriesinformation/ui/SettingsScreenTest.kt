package com.vamsi.worldcountriesinformation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vamsi.worldcountriesinformation.HiltTestActivity
import com.vamsi.worldcountriesinformation.core.common.testing.TestTags
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the Settings screen.
 *
 * These tests verify:
 * - Settings screen displays correctly
 * - Navigation back works
 * - Clear cache dialog works
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class SettingsScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun settingsScreen_displaysScreen() {
        // When: Settings screen is displayed
        composeTestRule.setContent {
            com.vamsi.worldcountriesinformation.feature.settings.SettingsScreen(
                onNavigateBack = {}
            )
        }

        // Then: Settings screen should be visible
        composeTestRule
            .onNodeWithTag(TestTags.Settings.SCREEN)
            .assertIsDisplayed()

        // And: Settings title should be visible
        composeTestRule
            .onNodeWithText("Settings")
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysBackButton() {
        // When: Settings screen is displayed
        composeTestRule.setContent {
            com.vamsi.worldcountriesinformation.feature.settings.SettingsScreen(
                onNavigateBack = {}
            )
        }

        // Then: Back button should be visible
        composeTestRule
            .onNodeWithTag(TestTags.Settings.BACK_BUTTON)
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_navigatesBack_whenBackButtonClicked() {
        var navigatedBack = false

        // When: Settings screen is displayed
        composeTestRule.setContent {
            com.vamsi.worldcountriesinformation.feature.settings.SettingsScreen(
                onNavigateBack = { navigatedBack = true }
            )
        }

        // And: User clicks back button
        composeTestRule
            .onNodeWithTag(TestTags.Settings.BACK_BUTTON)
            .performClick()

        // Then: Navigation callback should be invoked
        assert(navigatedBack) { "Expected navigation back" }
    }

    @Test
    fun settingsScreen_displaysOfflineModeSwitch() {
        // When: Settings screen is displayed
        composeTestRule.setContent {
            com.vamsi.worldcountriesinformation.feature.settings.SettingsScreen(
                onNavigateBack = {}
            )
        }

        // Then: Offline mode switch should be visible
        composeTestRule
            .onNodeWithTag(TestTags.Settings.OFFLINE_MODE_SWITCH)
            .assertIsDisplayed()

        // And: Offline Mode label should be visible
        composeTestRule
            .onNodeWithText("Offline Mode")
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysClearCacheButton() {
        // When: Settings screen is displayed
        composeTestRule.setContent {
            com.vamsi.worldcountriesinformation.feature.settings.SettingsScreen(
                onNavigateBack = {}
            )
        }

        // Then: Clear cache button should be visible
        composeTestRule
            .onNodeWithTag(TestTags.Settings.CLEAR_CACHE_BUTTON)
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_showsClearCacheDialog_whenClearCacheButtonClicked() {
        // When: Settings screen is displayed
        composeTestRule.setContent {
            com.vamsi.worldcountriesinformation.feature.settings.SettingsScreen(
                onNavigateBack = {}
            )
        }

        // And: User clicks clear cache button
        composeTestRule
            .onNodeWithTag(TestTags.Settings.CLEAR_CACHE_BUTTON)
            .performClick()

        // Then: Clear cache dialog should be visible
        composeTestRule
            .onNodeWithTag(TestTags.Settings.CLEAR_CACHE_DIALOG)
            .assertIsDisplayed()

        // And: Confirm button should be visible
        composeTestRule
            .onNodeWithTag(TestTags.Settings.CLEAR_CACHE_CONFIRM)
            .assertIsDisplayed()

        // And: Cancel button should be visible
        composeTestRule
            .onNodeWithTag(TestTags.Settings.CLEAR_CACHE_CANCEL)
            .assertIsDisplayed()
    }

    @Test
    fun settingsScreen_dismissesClearCacheDialog_whenCancelClicked() {
        // When: Settings screen is displayed
        composeTestRule.setContent {
            com.vamsi.worldcountriesinformation.feature.settings.SettingsScreen(
                onNavigateBack = {}
            )
        }

        // And: User clicks clear cache button
        composeTestRule
            .onNodeWithTag(TestTags.Settings.CLEAR_CACHE_BUTTON)
            .performClick()

        // And: User clicks cancel
        composeTestRule
            .onNodeWithTag(TestTags.Settings.CLEAR_CACHE_CANCEL)
            .performClick()

        // Then: Dialog should be dismissed
        composeTestRule
            .onNodeWithTag(TestTags.Settings.CLEAR_CACHE_DIALOG)
            .assertDoesNotExist()
    }
}
