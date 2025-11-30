package com.vamsi.worldcountriesinformation.ui.compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.vamsi.worldcountriesinformation.core.navigation.CountriesRoute
import com.vamsi.worldcountriesinformation.core.navigation.CountryDetailsRoute
import com.vamsi.worldcountriesinformation.core.navigation.NavigationState
import com.vamsi.worldcountriesinformation.core.navigation.Navigator
import com.vamsi.worldcountriesinformation.core.navigation.SettingsRoute
import com.vamsi.worldcountriesinformation.core.navigation.rememberNavigationState
import com.vamsi.worldcountriesinformation.core.navigation.toEntries
import com.vamsi.worldcountriesinformation.feature.countries.CountriesScreen
import com.vamsi.worldcountriesinformation.feature.settings.SettingsScreen
import com.vamsi.worldcountriesinformation.feature.countrydetails.CountryDetailsRoute as CountryDetailsScreen

/**
 * Main navigation configuration for the World Countries application using Navigation 3.
 *
 * This composable sets up the navigation graph using Jetpack Navigation 3.
 * It serves as the single source of navigation logic in the app, connecting
 * feature modules while keeping them independent of each other.
 *
 * **Navigation 3 Architecture:**
 * - Uses [NavigationState] to hold the back stack
 * - Uses [Navigator] to handle navigation events
 * - Uses [NavDisplay] to display the current destination
 * - Uses [entryProvider] DSL to define destinations
 *
 * **Navigation Flow:**
 * ```
 * Countries List Screen
 *      ↓ (click country)
 * Country Details Screen
 *      ↓ (back button)
 * Countries List Screen
 * ```
 *
 * **Type Safety:**
 * - Uses sealed data classes implementing NavKey for compile-time route safety
 * - Navigation arguments are type-safe through data class properties
 * - No string-based route errors
 *
 * @param navigationState The navigation state holder (optional, created internally if not provided)
 * @param navigator The navigator for handling navigation events (optional, created internally if not provided)
 *
 * @see NavigationState for state management
 * @see Navigator for navigation events
 * @see CountriesRoute for the countries list screen route
 * @see CountryDetailsRoute for the country details screen route
 *
 * @since 2.0.0
 *
 * Example usage:
 * ```kotlin
 * @Composable
 * fun MyApp() {
 *     WorldCountriesNavigation()
 * }
 * ```
 */
@Composable
fun WorldCountriesNavigation(
    navigationState: NavigationState = rememberNavigationState(startRoute = CountriesRoute),
    navigator: Navigator = remember { Navigator(navigationState) }
) {
    // Define the entry provider that maps routes to content
    val entryProvider = entryProvider<NavKey> {
        // Countries List Screen
        entry<CountriesRoute> {
            CountriesScreen(
                onNavigateToDetails = { countryCode ->
                    navigator.navigate(CountryDetailsRoute(countryCode))
                },
                onNavigateToSettings = {
                    navigator.navigate(SettingsRoute)
                }
            )
        }

        // Settings Screen
        entry<SettingsRoute> {
            SettingsScreen(
                onNavigateBack = {
                    navigator.goBack()
                }
            )
        }

        // Country Details Screen
        entry<CountryDetailsRoute> { key ->
            CountryDetailsScreen(
                countryCode = key.countryCode,
                onNavigateBack = {
                    navigator.goBack()
                }
            )
        }
    }

    // Display the navigation stack
    NavDisplay(
        entries = navigationState.toEntries(entryProvider),
        onBack = { navigator.goBack() }
    )
}
