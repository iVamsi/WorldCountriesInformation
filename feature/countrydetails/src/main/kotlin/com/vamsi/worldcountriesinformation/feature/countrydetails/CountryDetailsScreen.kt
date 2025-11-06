package com.vamsi.worldcountriesinformation.feature.countrydetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vamsi.worldcountriesinformation.domain.core.UiState
import com.vamsi.worldcountriesinformation.domainmodel.Country
import com.vamsi.worldcountriesinformation.domainmodel.Currency
import com.vamsi.worldcountriesinformation.domainmodel.Language
import com.vamsi.worldcountriesinformation.feature.countrydetails.component.CountryDetailsShimmer
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import java.util.Locale

/**
 * Country details route with pull-to-refresh support.
 *
 * ## Phase 3 Enhancement
 * - Pull-to-refresh for manual data updates
 * - Cache age indicator in TopAppBar
 * - Manual refresh button
 * - Enhanced error handling
 */
@Composable
fun CountryDetailsRoute(
    countryCode: String,
    onNavigateBack: () -> Unit,
    viewModel: CountryDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    LaunchedEffect(countryCode) {
        viewModel.loadCountryDetails(countryCode)
    }

    when (val state = uiState) {
        is UiState.Idle -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Initializing...")
            }
        }

        is UiState.Loading -> {
            CountryDetailsShimmer()
        }

        is UiState.Success -> {
            CountryDetailsScreen(
                country = state.data,
                onNavigateBack = onNavigateBack,
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refresh(countryCode) },
                cacheAge = viewModel.getCacheAge(),
                isCacheFresh = viewModel.isCacheFresh()
            )
        }

        is UiState.Error -> {
            CountryDetailsErrorContent(
                message = state.message ?: "An error occurred",
                onRetry = { viewModel.retry(countryCode) },
                onNavigateBack = onNavigateBack
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryDetailsScreen(
    country: Country,
    onNavigateBack: () -> Unit,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    cacheAge: String = "Never",
    isCacheFresh: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(country.name)
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
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                actions = {
                    // Manual refresh button
                    IconButton(
                        onClick = onRefresh,
                        enabled = !isRefreshing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh country details",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        // Pull-to-refresh wrapper
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.padding(paddingValues)
        ) {
            val detailsList = getCountryDetailsList(country)

            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Country Flag
                item {
                    CountryFlagCard(country = country)
                }

                // Map
                item {
                    CountryMapCard(country = country)
                }

                // Country Details
                item {
                    Text(
                        text = "Country Information",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(detailsList) { detail ->
                    CountryDetailItem(
                        label = detail.label,
                        value = detail.value
                    )
                }
            }
        }
    }
}

@Composable
private fun CountryFlagCard(country: Country) {
    val context = LocalContext.current
    val flagResourceName = "${country.twoLetterCode.lowercase(Locale.US)}_flag"
    val flagResourceId = context.resources.getIdentifier(
        flagResourceName,
        "drawable",
        context.packageName
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = country.twoLetterCode,
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CountryMapCard(country: Country) {
    LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        if (country.latitude != 0.0 && country.longitude != 0.0) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    // Configure osmdroid
                    Configuration.getInstance().userAgentValue = ctx.packageName

                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)

                        // Set initial position and zoom
                        val countryLocation = GeoPoint(country.latitude, country.longitude)
                        controller.setZoom(5.0)
                        controller.setCenter(countryLocation)

                        // Add copyright overlay (required by OpenStreetMap license)
                        val copyrightOverlay = CopyrightOverlay(ctx)
                        overlays.add(copyrightOverlay)

                        // Add marker
                        val marker = Marker(this).apply {
                            position = countryLocation
                            title = country.name
                            snippet = country.capital
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        overlays.add(marker)
                    }
                },
                update = { mapView ->
                    // Update map if country changes
                    val countryLocation = GeoPoint(country.latitude, country.longitude)
                    mapView.controller.setCenter(countryLocation)
                }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Location data not available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CountryDetailItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private data class CountryDetail(
    val label: String,
    val value: String,
)

@Composable
private fun getCountryDetailsList(country: Country): List<CountryDetail> {
    return remember(country) {
        listOf(
            CountryDetail("Country Name", country.name),
            CountryDetail("Capital City", country.capital),
            CountryDetail("Population", country.population.toString()),
            CountryDetail("Calling Code", country.callingCode),
            CountryDetail("Languages", country.languages.joinToString(", ") { it.name ?: "" }),
            CountryDetail("Currencies", country.currencies.joinToString(", ") { it.name ?: "" }),
            CountryDetail("Region", country.region),
            CountryDetail("Two Letter Code", country.twoLetterCode),
            CountryDetail("Three Letter Code", country.threeLetterCode)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryDetailsErrorContent(
    message: String,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Error") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
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
}

// Preview Data
private fun getSampleCountry() = Country(
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
)

// Previews
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Country Details Screen", showBackground = true)
@Composable
private fun CountryDetailsScreenPreview() {
    MaterialTheme {
        // Preview version without map (to avoid osmdroid rendering issues)
        val country = getSampleCountry()
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(country.name) },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Navigate back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { paddingValues ->
            val detailsList = getCountryDetailsList(country)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Country Flag
                item {
                    CountryFlagCard(country = country)
                }

                // Skip map in preview to avoid osmdroid errors

                // Country Details
                item {
                    Text(
                        text = "Country Information",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(detailsList) { detail ->
                    CountryDetailItem(
                        label = detail.label,
                        value = detail.value
                    )
                }
            }
        }
    }
}

@Preview(name = "Country Flag Card", showBackground = true)
@Composable
private fun CountryFlagCardPreview() {
    MaterialTheme {
        CountryFlagCard(country = getSampleCountry())
    }
}

@Preview(name = "Country Detail Item", showBackground = true)
@Composable
private fun CountryDetailItemPreview() {
    MaterialTheme {
        CountryDetailItem(
            label = "Capital City",
            value = "Washington, D.C."
        )
    }
}

@Preview(name = "Error Content", showBackground = true)
@Composable
private fun CountryDetailsErrorContentPreview() {
    MaterialTheme {
        CountryDetailsErrorContent(
            message = "Failed to load country details. Please try again.",
            onRetry = {},
            onNavigateBack = {}
        )
    }
}
