package com.vamsi.worldcountriesinformation.feature.countries

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vamsi.worldcountriesinformation.domain.core.UiState
import com.vamsi.worldcountriesinformation.domainmodel.Country
import com.vamsi.worldcountriesinformation.domainmodel.Currency
import com.vamsi.worldcountriesinformation.domainmodel.Language
import java.util.Locale

/**
 * Countries screen with pull-to-refresh, cache age indicator, and search.
 *
 * ## Phase 3 Enhancements
 * - Pull-to-refresh support for manual data updates
 * - Cache age indicator showing when data was last updated
 * - Manual refresh button in TopAppBar
 * - Enhanced error messages with retry
 * - **Search functionality with debounced input (Phase 3.8)**
 *
 * ## Phase 3.8: Search Features
 * - Real-time search with 300ms debounce
 * - Case-insensitive partial matching
 * - Clear search button
 * - Search result count
 * - Empty state for no results
 *
 * @param onCountryClick Callback when a country is clicked
 * @param viewModel The ViewModel managing the screen state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountriesScreen(
    onCountryClick: (Country) -> Unit,
    viewModel: CountriesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearchActive by viewModel.isSearchActive.collectAsStateWithLifecycle()
    val cacheAge = viewModel.getCacheAge()
    val isCacheFresh = viewModel.isCacheFresh()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("World Countries")
                        // Cache age indicator
                        if (cacheAge != "Never") {
                            Text(
                                text = "Updated $cacheAge",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCacheFresh) {
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                } else {
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                }
                            )
                        }
                    }
                },
                actions = {
                    // Manual refresh button
                    IconButton(
                        onClick = { viewModel.refresh() },
                        enabled = !isRefreshing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh countries",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is UiState.Idle -> {
                // Initial state - show nothing or placeholder
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Initializing...")
                }
            }

            is UiState.Loading -> {
                LoadingContent(modifier = Modifier.padding(paddingValues))
            }

            is UiState.Success -> {
                // Pull-to-refresh wrapper
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.padding(paddingValues)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search bar (Phase 3.8)
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { viewModel.onSearchQueryChange(it) },
                            onClearClick = { viewModel.clearSearch() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )

                        // Show search results count if searching
                        if (isSearchActive) {
                            Text(
                                text = "${searchResults.size} result${if (searchResults.size != 1) "s" else ""} found",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }

                        // Show search results or all countries
                        CountriesListContent(
                            countries = if (isSearchActive) searchResults else state.data,
                            onCountryClick = onCountryClick,
                            showEmptyState = isSearchActive && searchResults.isEmpty()
                        )
                    }
                }
            }

            is UiState.Error -> {
                ErrorContent(
                    message = state.message ?: "An error occurred",
                    onRetry = { viewModel.retry() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

/**
 * Search bar composable for filtering countries.
 *
 * ## Phase 3.8 Enhancement
 * Provides real-time search with debounced input and clear functionality.
 *
 * @param query Current search query
 * @param onQueryChange Callback when query changes
 * @param onClearClick Callback when clear button is clicked
 * @param modifier Modifier for the search bar
 */
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Search countries...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search icon",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearClick) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { keyboardController?.hide() }
        ),
        shape = MaterialTheme.shapes.medium
    )
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

/**
 * Countries list content with optional empty state.
 *
 * ## Phase 3.8 Enhancement
 * Added showEmptyState parameter to distinguish between:
 * - No data loaded yet
 * - No search results found
 *
 * @param countries List of countries to display
 * @param onCountryClick Callback when a country is clicked
 * @param showEmptyState Whether to show "No results" message
 * @param modifier Modifier for the list
 */
@Composable
private fun CountriesListContent(
    countries: List<Country>,
    onCountryClick: (Country) -> Unit,
    showEmptyState: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (countries.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Text(
                    text = if (showEmptyState) "No countries match your search" else "No countries found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (showEmptyState) {
                    Text(
                        text = "Try a different search term",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = countries,
                key = { it.threeLetterCode }
            ) { country ->
                CountryCard(
                    country = country,
                    onClick = { onCountryClick(country) }
                )
            }
        }
    }
}

@Composable
private fun CountryCard(
    country: Country,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val flagResourceName = "${country.twoLetterCode.lowercase(Locale.US)}_flag"
    val flagResourceId = context.resources.getIdentifier(
        flagResourceName,
        "drawable",
        context.packageName
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Country flag using Coil
            Surface(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                if (flagResourceId != 0) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(flagResourceId)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Flag of ${country.name}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    // Fallback if flag not found
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = country.twoLetterCode,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = country.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${country.capital} • ${country.region}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

// Preview Data
private fun getSampleCountries() = listOf(
    Country(
        name = "United States",
        capital = "Washington, D.C.",
        region = "Americas",
        population = 331002651,
        twoLetterCode = "US",
        threeLetterCode = "USA",
        callingCode = "+1",
        currencies = listOf(Currency(code = "USD", name = "United States dollar", symbol = "$")),
        languages = listOf(Language(name = "English")),
        latitude = 38.8951,
        longitude = -77.0364
    ),
    Country(
        name = "United Kingdom",
        capital = "London",
        region = "Europe",
        population = 67886011,
        twoLetterCode = "GB",
        threeLetterCode = "GBR",
        callingCode = "+44",
        currencies = listOf(Currency(code = "GBP", name = "British pound", symbol = "£")),
        languages = listOf(Language(name = "English")),
        latitude = 51.5074,
        longitude = -0.1278
    ),
    Country(
        name = "India",
        capital = "New Delhi",
        region = "Asia",
        population = 1380004385,
        twoLetterCode = "IN",
        threeLetterCode = "IND",
        callingCode = "+91",
        currencies = listOf(Currency(code = "INR", name = "Indian rupee", symbol = "₹")),
        languages = listOf(Language(name = "Hindi"), Language(name = "English")),
        latitude = 28.6139,
        longitude = 77.2090
    )
)

// Previews
@Preview(name = "Countries List - Light", showBackground = true)
@Composable
private fun CountriesListPreview() {
    MaterialTheme {
        CountriesListContent(
            countries = getSampleCountries(),
            onCountryClick = {}
        )
    }
}

@Preview(name = "Country Card", showBackground = true)
@Composable
private fun CountryCardPreview() {
    MaterialTheme {
        CountryCard(
            country = getSampleCountries().first(),
            onClick = {}
        )
    }
}

@Preview(name = "Loading State", showBackground = true)
@Composable
private fun LoadingContentPreview() {
    MaterialTheme {
        LoadingContent()
    }
}

@Preview(name = "Error State", showBackground = true)
@Composable
private fun ErrorContentPreview() {
    MaterialTheme {
        ErrorContent(
            message = "Failed to load countries. Please check your internet connection.",
            onRetry = {}
        )
    }
}

@Preview(name = "Empty List", showBackground = true)
@Composable
private fun EmptyListPreview() {
    MaterialTheme {
        CountriesListContent(
            countries = emptyList(),
            onCountryClick = {}
        )
    }
}
