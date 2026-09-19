package com.vamsi.worldcountriesinformation.ui.compose.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
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
import com.vamsi.worldcountriesinformation.feature.countries.CountriesScreen
import com.vamsi.worldcountriesinformation.feature.countries.R
import com.vamsi.worldcountriesinformation.feature.settings.SettingsScreen
import com.vamsi.worldcountriesinformation.ui.compose.adaptive.isExpandedWidth
import com.vamsi.worldcountriesinformation.feature.compare.CompareRoute as CompareScreen
import com.vamsi.worldcountriesinformation.feature.countrydetails.CountryDetailsRoute as CountryDetailsScreen
import com.vamsi.worldcountriesinformation.feature.quiz.QuizRoute as QuizScreen

/**
 * Main navigation configuration for the World Countries application using Navigation 3.
 */
@Composable
fun WorldCountriesNavigation(
    navigationState: NavigationState = rememberNavigationState(startRoute = CountriesRoute),
    navigator: Navigator = remember { Navigator(navigationState) },
    onDailyNotificationChanged: (Boolean) -> Unit = {},
    onOpenLicenses: () -> Unit = {},
) {
    val expanded = isExpandedWidth()
    val currentRoute = navigationState.backStack.lastOrNull()

    if (expanded) {
        ExpandedWidthNavigation(
            navigator = navigator,
            currentRoute = currentRoute,
            onDailyNotificationChanged = onDailyNotificationChanged,
            onOpenLicenses = onOpenLicenses,
        )
        return
    }

    CompactNavigation(
        navigationState = navigationState,
        navigator = navigator,
        onDailyNotificationChanged = onDailyNotificationChanged,
        onOpenLicenses = onOpenLicenses,
    )
}

@Composable
private fun ExpandedWidthNavigation(
    navigator: Navigator,
    currentRoute: NavKey?,
    onDailyNotificationChanged: (Boolean) -> Unit,
    onOpenLicenses: () -> Unit,
) {
    val secondaryRoute = currentRoute.takeIf { it !is CountriesRoute }

    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .then(if (secondaryRoute != null) Modifier.width(LIST_PANE_WIDTH) else Modifier.weight(1f))
                .fillMaxHeight(),
        ) {
            CountriesScreen(
                onNavigateToDetails = { countryCode ->
                    navigator.navigate(CountryDetailsRoute(countryCode))
                },
                onNavigateToSettings = { navigator.navigate(SettingsRoute) },
                onNavigateToCompare = { codes ->
                    navigator.navigate(CompareRoute(codes.joinToString(",")))
                },
                onNavigateToQuiz = { navigator.navigate(QuizRoute) },
            )
        }

        if (secondaryRoute != null) {
            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                SecondaryDestination(
                    route = secondaryRoute,
                    navigator = navigator,
                    onDailyNotificationChanged = onDailyNotificationChanged,
                    onOpenLicenses = onOpenLicenses,
                )
            }
        }
    }
}

@Composable
private fun SecondaryDestination(
    route: NavKey,
    navigator: Navigator,
    onDailyNotificationChanged: (Boolean) -> Unit,
    onOpenLicenses: () -> Unit,
) {
    when (route) {
        is CountryDetailsRoute -> CountryDetailsScreen(
            countryCode = route.countryCode,
            onNavigateBack = { navigator.goBack() },
            onNavigateToCountry = { countryCode ->
                navigator.navigate(CountryDetailsRoute(countryCode))
            },
        )

        is SettingsRoute -> SettingsScreen(
            onNavigateBack = { navigator.goBack() },
            onDailyNotificationChanged = onDailyNotificationChanged,
            onOpenLicenses = onOpenLicenses,
        )

        is QuizRoute -> QuizScreen(onNavigateBack = { navigator.goBack() })

        is CompareRoute -> CompareScreen(
            countryCodes = route.codes,
            onNavigateBack = { navigator.goBack() },
        )

        else -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.countries_select_hint),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(24.dp),
            )
        }
    }
}

@Composable
private fun CompactNavigation(
    navigationState: NavigationState,
    navigator: Navigator,
    onDailyNotificationChanged: (Boolean) -> Unit,
    onOpenLicenses: () -> Unit,
) {
    val entryProvider = entryProvider {
        entry<CountriesRoute> {
            CountriesScreen(
                onNavigateToDetails = { countryCode ->
                    navigator.navigate(CountryDetailsRoute(countryCode))
                },
                onNavigateToSettings = { navigator.navigate(SettingsRoute) },
                onNavigateToCompare = { codes ->
                    navigator.navigate(CompareRoute(codes.joinToString(",")))
                },
                onNavigateToQuiz = { navigator.navigate(QuizRoute) },
            )
        }

        entry<QuizRoute> {
            QuizScreen(onNavigateBack = { navigator.goBack() })
        }

        entry<CompareRoute> { key ->
            CompareScreen(
                countryCodes = key.codes,
                onNavigateBack = { navigator.goBack() },
            )
        }

        entry<SettingsRoute> {
            SettingsScreen(
                onNavigateBack = { navigator.goBack() },
                onDailyNotificationChanged = onDailyNotificationChanged,
                onOpenLicenses = onOpenLicenses,
            )
        }

        entry<CountryDetailsRoute> { key ->
            CountryDetailsScreen(
                countryCode = key.countryCode,
                onNavigateBack = { navigator.goBack() },
                onNavigateToCountry = { countryCode ->
                    navigator.navigate(CountryDetailsRoute(countryCode))
                },
            )
        }
    }

    // Shared-axis X: forward slides in from the end, back slides in from the start.
    val enter = slideInHorizontally { it / SLIDE_FRACTION } + fadeIn()
    val exit = slideOutHorizontally { -it / SLIDE_FRACTION } + fadeOut()
    val popEnter = slideInHorizontally { -it / SLIDE_FRACTION } + fadeIn()
    val popExit = slideOutHorizontally { it / SLIDE_FRACTION } + fadeOut()
    NavDisplay(
        entries = navigationState.toEntries(entryProvider),
        onBack = { navigator.goBack() },
        transitionSpec = { enter togetherWith exit },
        popTransitionSpec = { popEnter togetherWith popExit },
        predictivePopTransitionSpec = { popEnter togetherWith popExit },
    )
}

private val LIST_PANE_WIDTH = 380.dp
private const val SLIDE_FRACTION = 8
