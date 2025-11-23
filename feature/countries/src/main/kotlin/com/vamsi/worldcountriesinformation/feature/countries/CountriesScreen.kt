package com.vamsi.worldcountriesinformation.feature.countries

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vamsi.snapnotify.SnapNotify
import com.vamsi.worldcountriesinformation.core.common.mvi.collectAsEffect
import com.vamsi.worldcountriesinformation.domainmodel.Country
import com.vamsi.worldcountriesinformation.domainmodel.Currency
import com.vamsi.worldcountriesinformation.domainmodel.Language
import com.vamsi.worldcountriesinformation.domainmodel.Regions
import com.vamsi.worldcountriesinformation.domainmodel.SearchHistoryEntry
import com.vamsi.worldcountriesinformation.domainmodel.SortOrder

/**
 * Countries list UI that wires search, filtering, favorites, and navigation hooks.
 *
 * Collects state from the view model, renders the appropriate surface, and listens for
 * one-off effects (navigation, toasts, errors) to notify the host screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountriesScreen(
    onNavigateToDetails: (String) -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: CountriesViewModel = hiltViewModel(),
) {
    // Collect state
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Handle effects
    viewModel.effect.collectAsEffect { effect ->
        when (effect) {
            is CountriesContract.Effect.NavigateToDetails -> {
                onNavigateToDetails(effect.countryCode)
            }

            is CountriesContract.Effect.ShowToast -> {
                SnapNotify.show(effect.message)
            }

            is CountriesContract.Effect.ShowError -> {
                SnapNotify.showError(effect.message)
            }

            is CountriesContract.Effect.ShowSuccess -> {
                SnapNotify.showSuccess(effect.message)
            }
        }
    }

    // Scroll back to top whenever filters/sort/search change
    LaunchedEffect(state.selectedRegions, state.sortOrder, state.searchQuery) {
        if (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0) {
            listState.animateScrollToItem(0)
        }
    }

    CountriesScreenContent(
        state = state,
        listState = listState,
        onNavigateToSettings = onNavigateToSettings,
        onIntent = { intent -> viewModel.processIntent(intent) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountriesScreenContent(
    state: CountriesContract.State,
    listState: LazyListState,
    onNavigateToSettings: () -> Unit,
    onIntent: (CountriesContract.Intent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Countries") },
                actions = {
                    if (state.hasActiveFilters) {
                        IconButton(
                            onClick = { onIntent(CountriesContract.Intent.ClearFilters) }
                        ) {
                            Icon(Icons.Default.FilterList, "Clear filters")
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading && state.countries.isEmpty() -> {
                LoadingContent(Modifier.padding(paddingValues))
            }

            state.showError && state.countries.isEmpty() -> {
                ErrorContent(
                    message = state.errorMessage ?: "Unknown error",
                    onRetry = { onIntent(CountriesContract.Intent.RetryLoading) },
                    modifier = Modifier.padding(paddingValues)
                )
            }

            else -> {
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { onIntent(CountriesContract.Intent.RefreshCountries) },
                    modifier = Modifier.padding(paddingValues)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search bar
                        SearchBar(
                            query = state.searchQuery,
                            onQueryChange = {
                                onIntent(CountriesContract.Intent.SearchQueryChanged(it))
                            },
                            onClearClick = {
                                onIntent(CountriesContract.Intent.ClearSearch)
                            },
                            onFocusChanged = { isFocused ->
                                onIntent(
                                    CountriesContract.Intent.SearchFocusChanged(isFocused)
                                )
                            },
                            onBackClick = {
                                onIntent(CountriesContract.Intent.SearchBackPressed)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )

                        if (state.shouldShowSearchHistory) {
                            SearchHistorySection(
                                history = state.searchHistory,
                                onHistoryItemClick = { query ->
                                    onIntent(
                                        CountriesContract.Intent.SearchHistoryItemSelected(query)
                                    )
                                },
                                onHistoryItemDelete = { query ->
                                    onIntent(
                                        CountriesContract.Intent.DeleteSearchHistoryItem(query)
                                    )
                                },
                                onClearAll = {
                                    onIntent(CountriesContract.Intent.ClearSearchHistory)
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp)
                            )
                        } else {
                            // Region filters
                            if (state.selectedRegions.isNotEmpty() || !state.isSearchActive) {
                                RegionFilters(
                                    selectedRegions = state.selectedRegions,
                                    onRegionToggle = { region ->
                                        onIntent(CountriesContract.Intent.ToggleRegion(region))
                                    }
                                )
                            }

                            // Sort selector
                            SortSelector(
                                currentSort = state.sortOrder,
                                onSortChange = { sortOrder ->
                                    onIntent(CountriesContract.Intent.ChangeSortOrder(sortOrder))
                                }
                            )

                            // Countries list or empty state
                            when {
                                state.showEmptySearchResults -> {
                                    EmptySearchResults(
                                        query = state.searchQuery,
                                        onClearSearch = {
                                            onIntent(CountriesContract.Intent.ClearSearch)
                                        }
                                    )
                                }

                                state.filteredCountries.isEmpty() && !state.isLoading -> {
                                    EmptyState()
                                }

                                else -> {
                                    CountriesList(
                                        countries = state.filteredCountries,
                                        favoriteCountryCodes = state.favoriteCountryCodes,
                                        onCountryClick = { country ->
                                            onIntent(
                                                CountriesContract.Intent.CountryClicked(country.threeLetterCode)
                                            )
                                        },
                                        onFavoriteClick = { country ->
                                            onIntent(
                                                CountriesContract.Intent.ToggleFavorite(country.threeLetterCode)
                                            )
                                        },
                                        listState = listState
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    onMicClick: () -> Unit = {},
    onFocusChanged: (Boolean) -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.animateContentSize(
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                dampingRatio = Spring.DampingRatioMediumBouncy
            )
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(
            visible = isFocused,
            enter = expandHorizontally(
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)
            ) + fadeIn(),
            exit = shrinkHorizontally(
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)
            ) + fadeOut()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        if (query.isNotEmpty()) {
                            onClearClick()
                        }
                        if (isFocused) {
                            isFocused = false
                        }
                        focusManager.clearFocus(force = true)
                        onFocusChanged(false)
                        onBackClick()
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(Modifier.width(4.dp))
            }
        }

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { focusState ->
                    if (isFocused != focusState.isFocused) {
                        isFocused = focusState.isFocused
                        onFocusChanged(focusState.isFocused)
                    }
                },
            placeholder = { Text("Search countries...") },
            leadingIcon = {
                Icon(Icons.Default.Search, "Search")
            },
            trailingIcon = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onMicClick) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice search")
                    }
                    if (query.isNotEmpty()) {
                        IconButton(onClick = onClearClick) {
                            Icon(Icons.Default.Clear, "Clear")
                        }
                    }
                }
            },
            singleLine = true
        )
    }
}

@Composable
private fun RegionFilters(
    selectedRegions: Set<String>,
    onRegionToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Filter by Region",
                style = MaterialTheme.typography.labelLarge
            )
            if (selectedRegions.isNotEmpty()) {
                Text(
                    text = "${selectedRegions.size} selected",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(Regions.ALL.toList()) { region ->
                FilterChip(
                    selected = selectedRegions.contains(region),
                    onClick = { onRegionToggle(region) },
                    label = { Text(region) }
                )
            }
        }
    }
}

@Composable
private fun SortSelector(
    currentSort: SortOrder,
    onSortChange: (SortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isMenuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Sort",
            style = MaterialTheme.typography.labelLarge
        )

        Box {
            OutlinedButton(onClick = { isMenuOpen = true }) {
                Text(currentSort.humanReadableLabel())
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Change sort order")
            }

            DropdownMenu(
                expanded = isMenuOpen,
                onDismissRequest = { isMenuOpen = false }
            ) {
                SortOrder.entries.forEach { sortOption ->
                    DropdownMenuItem(
                        text = { Text(sortOption.humanReadableLabel()) },
                        trailingIcon = {
                            if (sortOption == currentSort) {
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                        },
                        onClick = {
                            isMenuOpen = false
                            if (sortOption != currentSort) {
                                onSortChange(sortOption)
                            }
                        }
                    )
                }
            }
        }
    }
}

private fun SortOrder.humanReadableLabel(): String = when (this) {
    SortOrder.NAME_ASC -> "Name · A to Z"
    SortOrder.NAME_DESC -> "Name · Z to A"
    SortOrder.POPULATION_ASC -> "Population · Low to High"
    SortOrder.POPULATION_DESC -> "Population · High to Low"
    SortOrder.AREA_ASC -> "Area · Small to Large"
    SortOrder.AREA_DESC -> "Area · Large to Small"
}

@Composable
private fun CountriesList(
    countries: List<Country>,
    favoriteCountryCodes: Set<String>,
    onCountryClick: (Country) -> Unit,
    onFavoriteClick: (Country) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = listState
    ) {
        items(countries, key = { it.threeLetterCode }) { country ->
            CountryCard(
                country = country,
                isFavorite = favoriteCountryCodes.contains(country.threeLetterCode),
                onClick = { onCountryClick(country) },
                onFavoriteClick = { onFavoriteClick(country) }
            )
        }
    }
}

@Composable
private fun CountryCard(
    country: Country,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val flagResourceName = "${country.twoLetterCode.lowercase()}_flag"
    val flagResourceId = context.resources.getIdentifier(
        flagResourceName,
        "drawable",
        context.packageName
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flag
            if (flagResourceId != 0) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(flagResourceId)
                        .crossfade(true)
                        .build(),
                    contentDescription = "${country.name} flag",
                    modifier = Modifier.size(60.dp, 40.dp)
                )
            } else {
                // Fallback
                Box(
                    modifier = Modifier.size(60.dp, 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(country.twoLetterCode)
                }
            }

            Spacer(Modifier.width(16.dp))

            // Country info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = country.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = country.capital,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Favorite button
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SearchHistorySection(
    history: List<SearchHistoryEntry>,
    onHistoryItemClick: (String) -> Unit,
    onHistoryItemDelete: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent searches",
                style = MaterialTheme.typography.titleMedium
            )
            TextButton(onClick = onClearAll) {
                Text("Clear all")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(history, key = { it.query.lowercase() }) { entry ->
                SearchHistoryItemRow(
                    entry = entry,
                    onClick = { onHistoryItemClick(entry.query) },
                    onDelete = { onHistoryItemDelete(entry.query) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchHistoryItemRow(
    entry: SearchHistoryEntry,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(end = 24.dp)
                )
            }
        }
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = entry.query,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, null)
            Spacer(Modifier.width(8.dp))
            Text("Retry")
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No countries found",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptySearchResults(
    query: String,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "No results for \"$query\"",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onClearSearch) {
            Text("Clear search")
        }
    }
}

// -----------------------------------------------------------------------------
// Previews
// -----------------------------------------------------------------------------

@Preview(showBackground = true, name = "Countries • List")
@Composable
private fun CountriesScreenPreview() {
    val previewState = CountriesContract.State(
        countries = PreviewCountries,
        filteredCountries = PreviewCountries,
        favoriteCountryCodes = setOf("CAN"),
        lastUpdated = System.currentTimeMillis()
    )

    CountriesScreenContent(
        state = previewState,
        listState = rememberLazyListState(),
        onNavigateToSettings = {},
        onIntent = {}
    )
}

@Preview(showBackground = true, name = "Countries • Search History")
@Composable
private fun CountriesScreenSearchHistoryPreview() {
    val previewState = CountriesContract.State(
        searchQuery = "",
        isSearchActive = true,
        isSearchFocused = true,
        searchHistory = listOf(
            SearchHistoryEntry("Canada"),
            SearchHistoryEntry("Japan"),
            SearchHistoryEntry("Australia")
        )
    )

    CountriesScreenContent(
        state = previewState,
        listState = rememberLazyListState(),
        onNavigateToSettings = {},
        onIntent = {}
    )
}

private val PreviewCountries = listOf(
    Country(
        name = "Canada",
        capital = "Ottawa",
        languages = listOf(Language(name = "English")),
        twoLetterCode = "CA",
        threeLetterCode = "CAN",
        population = 38_000_000,
        region = "Americas",
        currencies = listOf(Currency(code = "CAD", name = "Canadian Dollar", symbol = "$")),
        callingCode = "+1",
        latitude = 56.0,
        longitude = -106.0
    ),
    Country(
        name = "Japan",
        capital = "Tokyo",
        languages = listOf(Language(name = "Japanese")),
        twoLetterCode = "JP",
        threeLetterCode = "JPN",
        population = 125_000_000,
        region = "Asia",
        currencies = listOf(Currency(code = "JPY", name = "Yen", symbol = "¥")),
        callingCode = "+81",
        latitude = 36.0,
        longitude = 138.0
    )
)
