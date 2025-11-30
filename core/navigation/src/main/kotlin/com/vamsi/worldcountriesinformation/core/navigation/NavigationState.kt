package com.vamsi.worldcountriesinformation.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator

/**
 * Create a navigation state that persists config changes and process death.
 *
 * This function creates a [NavigationState] with a single back stack that starts
 * with the provided [startRoute]. The state is automatically saved and restored
 * across configuration changes and process death using [rememberNavBackStack].
 *
 * **Usage:**
 * ```kotlin
 * val navigationState = rememberNavigationState(
 *     startRoute = CountriesRoute
 * )
 * ```
 *
 * @param startRoute The initial route to display when the app starts
 * @return A [NavigationState] instance that manages the navigation back stack
 *
 * @see NavigationState
 * @see rememberNavBackStack
 */
@Composable
fun rememberNavigationState(
    startRoute: NavKey
): NavigationState {
    val backStack = rememberNavBackStack(startRoute)

    return remember(startRoute) {
        NavigationState(
            startRoute = startRoute,
            backStack = backStack
        )
    }
}

/**
 * State holder for navigation state.
 *
 * This class holds the navigation back stack and provides access to it.
 * It follows the principles of Unidirectional Data Flow (UDF):
 * - State is held in a single place
 * - State changes are made through the [Navigator] class
 * - UI observes state changes and updates accordingly
 *
 * **Architecture:**
 * - Immutable startRoute defines the entry point
 * - Mutable backStack holds current navigation state
 * - State is preserved across configuration changes via [rememberNavBackStack]
 *
 * @param startRoute The start route. The user will exit the app through this route.
 * @param backStack The navigation back stack
 *
 * @see Navigator for modifying navigation state
 * @see rememberNavigationState for creating instances
 */
class NavigationState(
    val startRoute: NavKey,
    val backStack: NavBackStack<NavKey>
)

/**
 * Convert NavigationState into NavEntries for display.
 *
 * This function transforms the back stack into a list of [NavEntry] objects
 * that can be displayed by [NavDisplay]. It applies decorators to preserve
 * state for each entry while it remains in the back stack.
 *
 * **Decorators Applied:**
 * - SaveableStateHolderNavEntryDecorator: Preserves composable state (remember, etc.)
 *
 * **Usage:**
 * ```kotlin
 * NavDisplay(
 *     entries = navigationState.toEntries { key ->
 *         when (key) {
 *             is CountriesRoute -> NavEntry(key) { CountriesScreen(...) }
 *             // ...
 *         }
 *     },
 *     onBack = { navigator.goBack() }
 * )
 * ```
 *
 * @param entryProvider Function that resolves a [NavKey] to a [NavEntry]
 * @return A list of [NavEntry] objects representing the current navigation state
 */
@Composable
fun NavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>
): SnapshotStateList<NavEntry<NavKey>> {
    val decorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
    )

    val decoratedEntries = rememberDecoratedNavEntries(
        backStack = backStack,
        entryDecorators = decorators,
        entryProvider = entryProvider
    )

    return decoratedEntries.toMutableStateList()
}
