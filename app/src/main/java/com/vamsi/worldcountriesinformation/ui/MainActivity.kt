package com.vamsi.worldcountriesinformation.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.vamsi.snapnotify.SnapNotifyProvider
import com.vamsi.worldcountriesinformation.R
import com.vamsi.worldcountriesinformation.core.designsystem.GradientBackground
import com.vamsi.worldcountriesinformation.core.designsystem.WorldCountriesTheme
import com.vamsi.worldcountriesinformation.core.navigation.CountriesRoute
import com.vamsi.worldcountriesinformation.core.navigation.CountryDetailsRoute
import com.vamsi.worldcountriesinformation.core.navigation.Navigator
import com.vamsi.worldcountriesinformation.core.navigation.rememberNavigationState
import com.vamsi.worldcountriesinformation.ui.compose.navigation.WorldCountriesNavigation
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * Main entry point for the World Countries Information application.
 * Sets up Compose UI, navigation, and theme with Hilt dependency injection.
 * Handles deep links from widgets to navigate to specific country details.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        /**
         * Intent extra key for country code passed from widgets
         */
        const val EXTRA_COUNTRY_CODE = "extra_country_code"
    }

    // State to trigger recomposition when a new intent arrives
    private var currentIntent by mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        enableEdgeToEdge()
        currentIntent = intent

        setContent {
            WorldCountriesTheme {
                SnapNotifyProvider {
                    GradientBackground(modifier = Modifier.fillMaxSize()) {
                        // Create navigation state and navigator
                        val navigationState = rememberNavigationState(startRoute = CountriesRoute)
                        val navigator = remember { Navigator(navigationState) }

                        // Handle deep link navigation from widget
                        // Observes currentIntent to re-trigger on new intents
                        LaunchedEffect(currentIntent) {
                            currentIntent?.let { intent ->
                                handleDeepLink(intent, navigator)
                            }
                        }

                        WorldCountriesNavigation(
                            navigationState = navigationState,
                            navigator = navigator
                        )
                    }
                }
            }
        }
    }

    /**
     * Handles new intents for deep link navigation
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Update the state to trigger recomposition and navigation
        currentIntent = intent
    }

    /**
     * Handles deep link navigation from widgets and other sources
     */
    private fun handleDeepLink(intent: Intent, navigator: Navigator) {
        val countryCode = intent.getStringExtra(EXTRA_COUNTRY_CODE)
        if (countryCode != null) {
            Timber.d("Deep link: Navigating to country details for code: $countryCode")
            try {
                // Navigate to country details, clearing any intermediate screens
                navigator.navigateAndClear(CountryDetailsRoute(countryCode))
            } catch (e: Exception) {
                Timber.e(e, "Failed to navigate to country details")
            }
        }
    }
}
