package com.vamsi.worldcountriesinformation

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vamsi.worldcountriesinformation.core.common.testing.UiTestTags
import com.vamsi.worldcountriesinformation.ui.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class NavigationFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createEmptyComposeRule()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun settingsNavigation_showsSettingsScreen() {
        launchMainActivity().use {
            composeRule.waitForIdle()
            composeRule.onNodeWithTag(UiTestTags.COUNTRIES_SCREEN).assertIsDisplayed()
            composeRule.onNodeWithContentDescription("Settings").performClick()
            composeRule.waitUntil(timeoutMillis = 10_000) {
                composeRule.onAllNodesWithTag(UiTestTags.SETTINGS_SCREEN)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
            composeRule.onNodeWithTag(UiTestTags.SETTINGS_SCREEN).assertIsDisplayed()
            composeRule.onNodeWithText("Settings").assertIsDisplayed()
        }
    }

    @Test
    fun countryDeepLink_showsDetailsScreen() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_COUNTRY_CODE, "USA")
        }
        ActivityScenario.launch<MainActivity>(intent).use {
            composeRule.waitUntil(timeoutMillis = 20_000) {
                composeRule.onAllNodesWithTag(UiTestTags.COUNTRY_DETAILS_SCREEN)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
            composeRule.onNodeWithTag(UiTestTags.COUNTRY_DETAILS_SCREEN).assertIsDisplayed()
        }
    }

    @Test
    fun compareDeepLink_showsCompareScreen() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_COMPARE_CODES, "USA,CAN")
        }
        ActivityScenario.launch<MainActivity>(intent).use {
            composeRule.waitUntil(timeoutMillis = 15_000) {
                composeRule.onAllNodesWithTag(UiTestTags.COMPARE_SCREEN)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
            composeRule.onNodeWithTag(UiTestTags.COMPARE_SCREEN).assertIsDisplayed()
            composeRule.onNodeWithText("Compare countries", substring = true).assertIsDisplayed()
        }
    }

    private fun launchMainActivity(): ActivityScenario<MainActivity> {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return ActivityScenario.launch(Intent(context, MainActivity::class.java))
    }
}
