package com.vamsi.worldcountriesinformation.ui.compose.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.vamsi.worldcountriesinformation.core.navigation.CompareRoute
import com.vamsi.worldcountriesinformation.core.navigation.CountriesRoute
import com.vamsi.worldcountriesinformation.core.navigation.CountryDetailsRoute
import com.vamsi.worldcountriesinformation.core.navigation.NavigationState
import com.vamsi.worldcountriesinformation.core.navigation.Navigator
import com.vamsi.worldcountriesinformation.core.navigation.QuizRoute
import com.vamsi.worldcountriesinformation.core.navigation.SettingsRoute
import com.vamsi.worldcountriesinformation.core.navigation.rememberNavigationState
import com.vamsi.worldcountriesinformation.core.navigation.toEntries
import com.vamsi.worldcountriesinformation.feature.compare.CompareRoute as CompareScreen
import com.vamsi.worldcountriesinformation.feature.countries.CountriesScreen
import com.vamsi.worldcountriesinformation.feature.settings.SettingsScreen
import com.vamsi.worldcountriesinformation.feature.countrydetails.CountryDetailsRoute as CountryDetailsScreen
import com.vamsi.worldcountriesinformation.feature.quiz.QuizRoute as QuizScreen
import com.vamsi.worldcountriesinformation.ui.compose.adaptive.AdaptiveCountriesLayout
import com.vamsi.worldcountriesinformation.ui.compose.adaptive.isExpandedWidth

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
    navigator: Navigator = remember { Navigator(navigationState) },
    onDailyNotificationChanged: (Boolean) -> Unit = {},
) {
    val expanded = isExpandedWidth()
    val showAdaptiveHome = expanded &&
        navigationState.backStack.lastOrNull() is CountriesRoute

    if (showAdaptiveHome) {
        AdaptiveCountriesLayout(
            onNavigateToSettings = { navigator.navigate(SettingsRoute) },
            onNavigateToCompare = { codes ->
                navigator.navigate(CompareRoute(codes.joinToString(",")))
            },
            onNavigateToQuiz = { navigator.navigate(QuizRoute) },
        )
        return
    }

    // Define the entry provider that maps routes to content
    val entryProvider = entryProvider {
        // Countries List Screen
        entry<CountriesRoute> {
            CountriesScreen(
                onNavigateToDetails = { countryCode ->
                    navigator.navigate(CountryDetailsRoute(countryCode))
                },
                onNavigateToSettings = {
                    navigator.navigate(SettingsRoute)
                },
                onNavigateToCompare = { codes ->
                    navigator.navigate(CompareRoute(codes.joinToString(",")))
                },
                onNavigateToQuiz = {
                    navigator.navigate(QuizRoute)
                },
            )
        }

        entry<QuizRoute> {
            QuizScreen(onNavigateBack = { navigator.goBack() })
        }

        // Compare Screen
        entry<CompareRoute> { key ->
            CompareScreen(
                countryCodes = key.codes,
                onNavigateBack = { navigator.goBack() },
            )
        }

        // Settings Screen
        entry<SettingsRoute> {
            SettingsScreen(
                onNavigateBack = {
                    navigator.goBack()
                },
                onDailyNotificationChanged = onDailyNotificationChanged,
            )
        }

        // Country Details Screen
        entry<CountryDetailsRoute> { key ->
            CountryDetailsScreen(
                countryCode = key.countryCode,
                onNavigateBack = {
                    navigator.goBack()
                },
                onNavigateToCountry = { countryCode ->
                    navigator.navigate(CountryDetailsRoute(countryCode))
                }
            )
        }
    }

    // Display the navigation stack with fade transition between destinations
    val currentRoute = navigationState.backStack.lastOrNull()
    AnimatedContent(
        targetState = currentRoute,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "nav_transition",
    ) { _ ->
        NavDisplay(
            entries = navigationState.toEntries(entryProvider),
            onBack = { navigator.goBack() },
        )
    }
}
