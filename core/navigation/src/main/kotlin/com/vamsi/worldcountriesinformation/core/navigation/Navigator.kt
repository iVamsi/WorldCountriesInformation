package com.vamsi.worldcountriesinformation.core.navigation

import androidx.navigation3.runtime.NavKey

/**
 * Handles navigation events (forward and back) by updating the navigation state.
 *
 * This class provides a clean API for navigation operations and encapsulates
 * the logic for modifying the back stack. It follows the principles of
 * Unidirectional Data Flow (UDF):
 * - Receives navigation events as method calls
 * - Updates the [NavigationState] accordingly
 * - UI observes state changes via [NavDisplay]
 *
 * **Thread Safety:**
 * All navigation operations must be called on the main thread.
 *
 * **Usage:**
 * ```kotlin
 * val navigationState = rememberNavigationState(startRoute = CountriesRoute)
 * val navigator = remember { Navigator(navigationState) }
 *
 * // Navigate forward
 * navigator.navigate(CountryDetailsRoute("USA"))
 *
 * // Navigate back
 * navigator.goBack()
 * ```
 *
 * @param state The [NavigationState] to modify
 *
 * @see NavigationState
 * @see NavKey
 */
class Navigator(val state: NavigationState) {

    /**
     * Navigate to a new destination by pushing a route onto the back stack.
     *
     * This adds the [route] to the top of the back stack, making it the
     * current destination. The previous destination remains in the stack
     * and can be returned to via [goBack].
     *
     * **Behavior:**
     * - Pushes the route onto the back stack
     * - The new route becomes the current destination
     * - Previous destinations are preserved for back navigation
     *
     * **Examples:**
     * ```kotlin
     * // Navigate to country details
     * navigator.navigate(CountryDetailsRoute("USA"))
     *
     * // Navigate to settings
     * navigator.navigate(SettingsRoute)
     * ```
     *
     * @param route The [NavKey] representing the destination to navigate to
     */
    fun navigate(route: NavKey) {
        state.backStack.add(route)
    }

    /**
     * Navigate back to the previous destination.
     *
     * This removes the current destination from the back stack, revealing
     * the previous destination. If the current destination is the start
     * route (the only item in the stack), this function does nothing and
     * the system back handler should close the app.
     *
     * **Behavior:**
     * - Removes the topmost route from the back stack
     * - The previous route becomes the current destination
     * - Returns true if navigation occurred, false if at start route
     *
     * **Usage:**
     * ```kotlin
     * // In a back button handler
     * IconButton(onClick = { navigator.goBack() }) {
     *     Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
     * }
     *
     * // In NavDisplay onBack callback
     * NavDisplay(
     *     entries = navigationState.toEntries(entryProvider),
     *     onBack = { navigator.goBack() }
     * )
     * ```
     *
     * @return `true` if a destination was popped, `false` if at start route
     */
    fun goBack(): Boolean {
        val currentRoute = state.backStack.lastOrNull()

        // If we're at the start route, don't pop (let system handle app exit)
        if (currentRoute == state.startRoute) {
            return false
        }

        // Pop the current route
        state.backStack.removeLastOrNull()
        return true
    }

    /**
     * Check if back navigation is possible.
     *
     * Returns true if there are destinations to navigate back to,
     * false if currently at the start route.
     *
     * **Usage:**
     * ```kotlin
     * // Conditionally show back button
     * if (navigator.canGoBack()) {
     *     IconButton(onClick = { navigator.goBack() }) {
     *         Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
     *     }
     * }
     * ```
     *
     * @return `true` if back navigation is possible, `false` otherwise
     */
    fun canGoBack(): Boolean {
        return state.backStack.size > 1
    }

    /**
     * Navigate to a destination, clearing the back stack up to the start route.
     *
     * This is useful for navigating to a destination while ensuring the user
     * can go back directly to the start route (e.g., home screen).
     *
     * **Behavior:**
     * - Clears all routes except the start route
     * - Pushes the new route onto the stack
     * - Back navigation will go directly to start route
     *
     * **Usage:**
     * ```kotlin
     * // Navigate to details from deep link, clearing intermediate screens
     * navigator.navigateAndClear(CountryDetailsRoute("USA"))
     * ```
     *
     * @param route The destination to navigate to
     */
    fun navigateAndClear(route: NavKey) {
        // Clear all routes except the start route
        while (state.backStack.size > 1) {
            state.backStack.removeLastOrNull()
        }
        // Add the new route
        state.backStack.add(route)
    }
}
