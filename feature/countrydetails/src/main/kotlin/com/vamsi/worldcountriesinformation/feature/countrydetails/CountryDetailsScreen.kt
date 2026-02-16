package com.vamsi.worldcountriesinformation.feature.countrydetails

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vamsi.snapnotify.SnapNotify
import com.vamsi.worldcountriesinformation.core.designsystem.component.pressScaleEffect
import com.vamsi.worldcountriesinformation.core.designsystem.component.rememberPressScaleInteractionSource
import com.vamsi.worldcountriesinformation.domainmodel.Country
import com.vamsi.worldcountriesinformation.domainmodel.Currency
import com.vamsi.worldcountriesinformation.domainmodel.Language
import com.vamsi.worldcountriesinformation.feature.countrydetails.component.CountryDetailsShimmer
import kotlinx.coroutines.flow.collectLatest
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import java.util.Locale

/**
 * Country details route with pull-to-refresh, cache age indicators, sharing,
 * maps integration, nearby countries, and simple error handling hooks.
 */
@Composable
fun CountryDetailsRoute(
    countryCode: String,
    onNavigateBack: () -> Unit,
    onNavigateToCountry: (String) -> Unit = {},
    viewModel: CountryDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is CountryDetailsContract.Effect.NavigateBack -> {
                    onNavigateBack()
                }

                is CountryDetailsContract.Effect.ShowToast -> {
                    SnapNotify.show(effect.message)
                }

                is CountryDetailsContract.Effect.ShowError -> {
                    SnapNotify.showError(effect.message)
                }

                is CountryDetailsContract.Effect.ShowSuccess -> {
                    SnapNotify.showSuccess(effect.message)
                }

                is CountryDetailsContract.Effect.ShareCountryCard -> {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, effect.shareText)
                        putExtra(Intent.EXTRA_SUBJECT, "Country Information")
                    }
                    context.startActivity(
                        Intent.createChooser(shareIntent, "Share country info")
                    )
                }

                is CountryDetailsContract.Effect.OpenInMaps -> {
                    val geoUri = Uri.parse(
                        "geo:${effect.latitude},${effect.longitude}?q=${effect.latitude},${effect.longitude}(${
                            Uri.encode(
                                effect.countryName
                            )
                        })"
                    )
                    val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                        setPackage("com.google.android.apps.maps")
                    }
                    // Fallback to any maps app if Google Maps is not installed
                    if (mapIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(mapIntent)
                    } else {
                        val fallbackIntent = Intent(Intent.ACTION_VIEW, geoUri)
                        if (fallbackIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(fallbackIntent)
                        } else {
                            // Ultimate fallback: open Google Maps in browser
                            val browserUri = Uri.parse(
                                "https://www.google.com/maps/search/?api=1&query=${effect.latitude},${effect.longitude}"
                            )
                            context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
                        }
                    }
                }

                is CountryDetailsContract.Effect.NavigateToCountryDetails -> {
                    onNavigateToCountry(effect.countryCode)
                }
            }
        }
    }

    LaunchedEffect(countryCode) {
        viewModel.processIntent(CountryDetailsContract.Intent.LoadCountryDetails(countryCode))
    }

    CountryDetailsScreenContent(
        state = state,
        countryCode = countryCode,
        onIntent = { intent -> viewModel.processIntent(intent) },
        cacheAge = viewModel.getCacheAge(),
        isCacheFresh = viewModel.isCacheFresh()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryDetailsScreenContent(
    state: CountryDetailsContract.State,
    countryCode: String,
    onIntent: (CountryDetailsContract.Intent) -> Unit,
    cacheAge: String,
    isCacheFresh: Boolean,
    modifier: Modifier = Modifier,
) {
    when {
        state.showLoading -> {
            CountryDetailsShimmer()
        }

        state.showError -> {
            CountryDetailsErrorContent(
                message = state.errorMessage ?: "An error occurred",
                onRetry = { onIntent(CountryDetailsContract.Intent.RetryLoading(countryCode)) },
                onNavigateBack = { onIntent(CountryDetailsContract.Intent.NavigateBack) }
            )
        }

        state.hasData && state.country != null -> {
            CountryDetailsScreen(
                country = state.country,
                isFavorite = state.isFavorite,
                nearbyCountries = state.nearbyCountries,
                isLoadingNearby = state.isLoadingNearby,
                onNavigateBack = { onIntent(CountryDetailsContract.Intent.NavigateBack) },
                isRefreshing = state.isRefreshing,
                onRefresh = { onIntent(CountryDetailsContract.Intent.RefreshCountry(countryCode)) },
                onFavoriteClick = { onIntent(CountryDetailsContract.Intent.ToggleFavorite) },
                onShareClick = { onIntent(CountryDetailsContract.Intent.ShareCountry) },
                onOpenInMapsClick = { onIntent(CountryDetailsContract.Intent.OpenInMaps) },
                onNearbyCountryClick = { code ->
                    onIntent(CountryDetailsContract.Intent.NearbyCountryClicked(code))
                },
                cacheAge = cacheAge,
                isCacheFresh = isCacheFresh,
                modifier = modifier
            )
        }

        else -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Initializing...")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryDetailsScreen(
    country: Country,
    isFavorite: Boolean,
    nearbyCountries: List<Country> = emptyList(),
    isLoadingNearby: Boolean = false,
    onNavigateBack: () -> Unit,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onOpenInMapsClick: () -> Unit = {},
    onNearbyCountryClick: (String) -> Unit = {},
    cacheAge: String = "Never",
    isCacheFresh: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
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
                    // Share button
                    IconButton(onClick = onShareClick) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share country information",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    // Favorite button
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
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
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
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

                // Map with "Open in Maps" button
                item {
                    CountryMapCard(country = country)
                }

                // "Open in Maps" action button
                if (country.latitude != 0.0 || country.longitude != 0.0) {
                    item {
                        OpenInMapsButton(onClick = onOpenInMapsClick)
                    }
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

                // Nearby Countries section
                item {
                    NearbyCountriesSection(
                        region = country.region,
                        nearbyCountries = nearbyCountries,
                        isLoading = isLoadingNearby,
                        onCountryClick = onNearbyCountryClick
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
            .height(220.dp),
        shape = MaterialTheme.shapes.large,
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
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.large),
                    contentScale = ContentScale.FillBounds
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
        shape = MaterialTheme.shapes.large,
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

/**
 * "Open in Maps" button placed below the map card.
 */
@Composable
private fun OpenInMapsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Icon(
            imageVector = Icons.Default.Map,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Open in Maps")
    }
}

/**
 * Section showing nearby countries in the same region.
 */
@Composable
private fun NearbyCountriesSection(
    region: String,
    nearbyCountries: List<Country>,
    isLoading: Boolean,
    onCountryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Nearby Countries ($region)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }

            nearbyCountries.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No nearby countries found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(end = 4.dp)
                ) {
                    items(
                        items = nearbyCountries,
                        key = { it.threeLetterCode }
                    ) { country ->
                        NearbyCountryCard(
                            country = country,
                            onClick = { onCountryClick(country.threeLetterCode) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Compact country card for the nearby countries horizontal list.
 */
@Composable
private fun NearbyCountryCard(
    country: Country,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val flagResourceName = "${country.twoLetterCode.lowercase(Locale.US)}_flag"
    val flagResourceId = context.resources.getIdentifier(
        flagResourceName,
        "drawable",
        context.packageName
    )
    val interactionSource = rememberPressScaleInteractionSource()

    Card(
        modifier = modifier
            .width(120.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .pressScaleEffect(interactionSource),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Flag
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = CountryDetailsViewModel.countryCodeToFlagEmoji(country.twoLetterCode),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Country name
            Text(
                text = country.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Capital
            if (country.capital.isNotEmpty()) {
                Text(
                    text = country.capital,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
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
        shape = MaterialTheme.shapes.medium,
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
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
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
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
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
        val country = getSampleCountry()
        CountryDetailsScreen(
            country = country,
            isFavorite = false,
            nearbyCountries = getSampleNearbyCountries(),
            onNavigateBack = {},
            onFavoriteClick = {},
            onShareClick = {},
            onOpenInMapsClick = {},
            onNearbyCountryClick = {}
        )
    }
}

@Preview(name = "Country Details Screen - Favorite", showBackground = true)
@Composable
private fun CountryDetailsScreenFavoritePreview() {
    MaterialTheme {
        val country = getSampleCountry()
        CountryDetailsScreen(
            country = country,
            isFavorite = true,
            nearbyCountries = getSampleNearbyCountries(),
            onNavigateBack = {},
            onFavoriteClick = {},
            onShareClick = {},
            onOpenInMapsClick = {},
            onNearbyCountryClick = {}
        )
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

@Preview(name = "Nearby Countries Section", showBackground = true)
@Composable
private fun NearbyCountriesSectionPreview() {
    MaterialTheme {
        NearbyCountriesSection(
            region = "Americas",
            nearbyCountries = getSampleNearbyCountries(),
            isLoading = false,
            onCountryClick = {}
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

private fun getSampleNearbyCountries() = listOf(
    Country(
        name = "Canada",
        capital = "Ottawa",
        region = "Americas",
        population = 38005238,
        twoLetterCode = "CA",
        threeLetterCode = "CAN",
        callingCode = "+1",
        currencies = listOf(Currency(code = "CAD", name = "Canadian dollar", symbol = "$")),
        languages = listOf(Language(name = "English"), Language(name = "French")),
        latitude = 56.1304,
        longitude = -106.3468
    ),
    Country(
        name = "Mexico",
        capital = "Mexico City",
        region = "Americas",
        population = 128932753,
        twoLetterCode = "MX",
        threeLetterCode = "MEX",
        callingCode = "+52",
        currencies = listOf(Currency(code = "MXN", name = "Mexican peso", symbol = "$")),
        languages = listOf(Language(name = "Spanish")),
        latitude = 23.6345,
        longitude = -102.5528
    ),
    Country(
        name = "Brazil",
        capital = "Brasília",
        region = "Americas",
        population = 212559417,
        twoLetterCode = "BR",
        threeLetterCode = "BRA",
        callingCode = "+55",
        currencies = listOf(Currency(code = "BRL", name = "Brazilian real", symbol = "R$")),
        languages = listOf(Language(name = "Portuguese")),
        latitude = -14.235,
        longitude = -51.9253
    )
)
