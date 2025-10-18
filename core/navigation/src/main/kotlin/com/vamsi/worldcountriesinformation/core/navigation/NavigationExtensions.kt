package com.vamsi.worldcountriesinformation.core.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

/**
 * Extension functions for type-safe navigation.
 *
 * These extensions provide convenient, type-safe methods for navigating between
 * screens without dealing with raw route strings. They encapsulate navigation
 * logic and make the code more readable and maintainable.
 *
 * **Benefits:**
 * - Type-safe navigation (no string errors)
 * - Centralized navigation logic
 * - Easier testing (mockable extensions)
 * - Better IDE support (autocomplete, refactoring)
 * - Consistent navigation behavior
 *
 * **Usage:**
 * ```kotlin
 * // Instead of:
 * navController.navigate("country_details/USA")
 *
 * // Use:
 * navController.navigateToCountryDetails("USA")
 * ```
 *
 * @since 1.3.0
 */

/**
 * Navigates to the Countries list screen.
 *
 * This is the main landing screen showing all countries. Use this when:
 * - Resetting navigation to home
 * - Deep link lands on a specific country but user wants to see the list
 * - After completing an action that should return to home
 *
 * **Navigation Options:**
 * By default, this navigation:
 * - Adds destination to back stack
 * - Uses default enter/exit animations
 * - Doesn't pop any destinations
 *
 * You can customize behavior using [builder]:
 * ```kotlin
 * navController.navigateToCountries {
 *     popUpTo(0) { inclusive = true }  // Clear back stack
 *     launchSingleTop = true            // Reuse existing instance
 * }
 * ```
 *
 * **Thread Safety:**
 * Must be called on the main thread (same as NavController requirements).
 *
 * @param builder Optional lambda to configure [NavOptionsBuilder]
 *                Use this to set pop behavior, animations, single top, etc.
 *
 * @see Screen.Countries
 * @see NavController.navigate
 *
 * Example:
 * ```kotlin
 * // Simple navigation
 * navController.navigateToCountries()
 *
 * // Clear back stack and navigate to home
 * navController.navigateToCountries {
 *     popUpTo(0) { inclusive = true }
 * }
 * ```
 */
fun NavController.navigateToCountries(
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    navigate(Screen.Countries.route, builder)
}

/**
 * Navigates to the Country Details screen for a specific country.
 *
 * This screen displays comprehensive information about a single country including:
 * - Flag and basic info
 * - Geographic location (map)
 * - Population and demographics
 * - Languages and currencies
 * - Calling codes and other details
 *
 * **Country Code Format:**
 * - Must be a valid 3-letter code (ISO 3166-1 alpha-3)
 * - Should be uppercase (e.g., "USA", not "usa")
 * - Examples: USA, GBR, JPN, IND, FRA, DEU
 *
 * **Validation:**
 * This function doesn't validate the country code format. Validation happens at:
 * - [GetCountryByCodeUseCase] - Domain layer validation
 * - Country Details screen - Shows error if country not found
 *
 * **Navigation Options:**
 * By default, this navigation:
 * - Adds destination to back stack (allows back navigation)
 * - Uses default enter/exit animations
 * - Can be customized via [builder] parameter
 *
 * **Error Handling:**
 * If country code is invalid or country not found:
 * - Navigation succeeds (screen loads)
 * - Screen shows "Country not found" error
 * - User can tap back to return to countries list
 *
 * **Thread Safety:**
 * Must be called on the main thread (same as NavController requirements).
 *
 * @param countryCode The three-letter country code (ISO 3166-1 alpha-3)
 *                    Should be uppercase (e.g., "USA", "GBR", "JPN")
 * @param builder Optional lambda to configure [NavOptionsBuilder]
 *                Use this to set animations, pop behavior, etc.
 *
 * @see Screen.CountryDetails
 * @see NavController.navigate
 *
 * Example:
 * ```kotlin
 * // Simple navigation
 * navController.navigateToCountryDetails("USA")
 *
 * // With custom animation
 * navController.navigateToCountryDetails("GBR") {
 *     anim {
 *         enter = R.anim.slide_in_right
 *         exit = R.anim.slide_out_left
 *     }
 * }
 *
 * // From a country click handler
 * CountriesScreen(
 *     onCountryClick = { country ->
 *         navController.navigateToCountryDetails(country.threeLetterCode)
 *     }
 * )
 * ```
 */
fun NavController.navigateToCountryDetails(
    countryCode: String,
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    val route = Screen.CountryDetails.createRoute(countryCode)
    navigate(route, builder)
}

/**
 * Navigates back to the previous destination in the back stack.
 *
 * This is a convenience function that wraps [NavController.popBackStack]
 * with a more descriptive name. Use this when:
 * - User taps back button
 * - Completing an action that should return to previous screen
 * - Canceling an operation
 *
 * **Behavior:**
 * - Removes current destination from back stack
 * - Shows previous destination with back stack state restored
 * - Returns false if back stack is empty (nothing to pop)
 *
 * **Return Value:**
 * - `true`: Successfully navigated back (destination was popped)
 * - `false`: Back stack is empty (no destination to pop)
 *
 * **Handling Empty Back Stack:**
 * ```kotlin
 * if (!navController.navigateBack()) {
 *     // Back stack is empty, finish activity or show exit dialog
 *     activity.finish()
 * }
 * ```
 *
 * **Thread Safety:**
 * Must be called on the main thread (same as NavController requirements).
 *
 * @return `true` if a destination was popped, `false` if back stack is empty
 *
 * @see NavController.popBackStack
 *
 * Example:
 * ```kotlin
 * // In a back button handler
 * IconButton(onClick = { navController.navigateBack() }) {
 *     Icon(Icons.Default.ArrowBack, "Back")
 * }
 *
 * // In a country details screen
 * CountryDetailsScreen(
 *     onNavigateBack = { navController.navigateBack() }
 * )
 * ```
 */
fun NavController.navigateBack(): Boolean {
    return popBackStack()
}

/**
 * Navigates up to a specific destination by route, popping all destinations
 * until (and optionally including) the specified destination.
 *
 * This is useful for:
 * - Resetting to a specific point in navigation
 * - Clearing intermediate screens
 * - Implementing custom back button behavior
 *
 * **Use Cases:**
 * - After completing a multi-step flow, return to main screen
 * - Deep link handling that should clear certain screens
 * - Tab switching that needs to reset a navigation stack
 *
 * **Inclusive vs Exclusive:**
 * - `inclusive = false` (default): Keep the target destination
 * - `inclusive = true`: Also pop the target destination
 *
 * @param route The route to pop up to
 * @param inclusive Whether to also pop the destination route
 *
 * @return `true` if successful, `false` if route not found in back stack
 *
 * @see NavController.popBackStack
 *
 * Example:
 * ```kotlin
 * // Return to countries list, clearing everything else
 * navController.navigateUpTo(Screen.Countries.route, inclusive = false)
 *
 * // Clear including the target
 * navController.navigateUpTo(Screen.Countries.route, inclusive = true)
 * ```
 */
fun NavController.navigateUpTo(route: String, inclusive: Boolean = false): Boolean {
    return popBackStack(route, inclusive)
}
