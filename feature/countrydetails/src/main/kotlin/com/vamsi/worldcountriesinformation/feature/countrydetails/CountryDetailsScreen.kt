@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.vamsi.worldcountriesinformation.feature.countrydetails

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vamsi.snapnotify.SnapNotify
import com.vamsi.worldcountriesinformation.core.common.error.message
import com.vamsi.worldcountriesinformation.core.common.testing.UiTestTags
import com.vamsi.worldcountriesinformation.core.designsystem.WorldCountriesTheme
import com.vamsi.worldcountriesinformation.core.designsystem.component.EmptyState
import com.vamsi.worldcountriesinformation.core.designsystem.component.ErrorState
import com.vamsi.worldcountriesinformation.core.designsystem.component.FactTile
import com.vamsi.worldcountriesinformation.core.designsystem.component.FlagImage
import com.vamsi.worldcountriesinformation.core.designsystem.component.SectionHeader
import com.vamsi.worldcountriesinformation.core.designsystem.component.pressScaleEffect
import com.vamsi.worldcountriesinformation.core.designsystem.component.rememberPressScaleInteractionSource
import com.vamsi.worldcountriesinformation.domainmodel.Country
import com.vamsi.worldcountriesinformation.domainmodel.CountrySummary
import com.vamsi.worldcountriesinformation.domainmodel.Currency
import com.vamsi.worldcountriesinformation.domainmodel.Language
import com.vamsi.worldcountriesinformation.feature.countrydetails.component.CountryDetailsShimmer
import com.vamsi.worldcountriesinformation.feature.countrydetails.map.CountryBorderOverlay
import kotlinx.coroutines.flow.collectLatest
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import java.text.NumberFormat
import java.util.Locale
import com.vamsi.worldcountriesinformation.core.common.R as CommonR

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
    val resources = LocalResources.current

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

                is CountryDetailsContract.Effect.ShowMessage -> {
                    SnapNotify.show(
                        resources.getString(effect.messageRes, *effect.formatArgs.toTypedArray()),
                    )
                }

                is CountryDetailsContract.Effect.ShowError -> {
                    val text = effect.error?.let { resources.message(it) }
                        ?: resources.getString(
                            effect.messageRes!!,
                            *effect.formatArgs.toTypedArray(),
                        )
                    SnapNotify.showError(text)
                }

                is CountryDetailsContract.Effect.ShowSuccess -> {
                    SnapNotify.showSuccess(effect.message)
                }

                is CountryDetailsContract.Effect.ShareCountryCard -> {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, effect.shareText)
                        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.details_share_subject))
                    }
                    context.startActivity(
                        Intent.createChooser(shareIntent, context.getString(R.string.details_share_chooser)),
                    )
                }

                is CountryDetailsContract.Effect.OpenInMaps -> {
                    val geoUri = Uri.parse(
                        "geo:${effect.latitude},${effect.longitude}?q=${effect.latitude},${effect.longitude}(${
                            Uri.encode(
                                effect.countryName,
                            )
                        })",
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
                                "https://www.google.com/maps/search/?api=1&query=${effect.latitude},${effect.longitude}",
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
        isCacheFresh = viewModel.isCacheFresh(),
    )
}

@Composable
internal fun CountryDetailsScreenContent(
    state: CountryDetailsContract.State,
    countryCode: String,
    onIntent: (CountryDetailsContract.Intent) -> Unit,
    cacheAge: String?,
    isCacheFresh: Boolean,
    modifier: Modifier = Modifier,
) {
    when {
        state.showLoading -> {
            CountryDetailsShimmer()
        }

        state.showError -> {
            val errorContext = LocalContext.current
            CountryDetailsErrorContent(
                message = state.error?.let { errorContext.message(it) }
                    ?: stringResource(CommonR.string.error_unknown),
                onRetry = { onIntent(CountryDetailsContract.Intent.RetryLoading(countryCode)) },
                onNavigateBack = { onIntent(CountryDetailsContract.Intent.NavigateBack) },
            )
        }

        state.hasData && state.country != null -> {
            CountryDetailsScreen(
                country = state.country,
                aiSummary = state.aiSummary,
                showMapBorders = state.showMapBorders,
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
                modifier = modifier,
            )
        }

        else -> {
            CountryDetailsShimmer()
        }
    }
}

@Composable
internal fun CountryDetailsScreen(
    country: Country,
    isFavorite: Boolean,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    aiSummary: CountryDetailsContract.AiSummaryState = CountryDetailsContract.AiSummaryState.Disabled,
    showMapBorders: Boolean = true,
    nearbyCountries: List<CountrySummary> = emptyList(),
    isLoadingNearby: Boolean = false,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onOpenInMapsClick: () -> Unit = {},
    onNearbyCountryClick: (String) -> Unit = {},
    cacheAge: String? = null,
    isCacheFresh: Boolean = false,
) {
    val widthDp = LocalConfiguration.current.screenWidthDp
    val fontScale = LocalDensity.current.fontScale
    // Large text breaks tracked labels mid-word in narrow tiles, so fall back to one column.
    val factColumns = when {
        fontScale >= LARGE_FONT_SCALE -> 1
        widthDp >= EXPANDED_WIDTH_DP -> 3
        else -> 2
    }
    val sideBySide = widthDp >= LARGE_WIDTH_DP
    val hasLocation = country.latitude != 0.0 || country.longitude != 0.0

    Scaffold(
        modifier = modifier.testTag(UiTestTags.COUNTRY_DETAILS_SCREEN),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = country.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.details_navigate_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onShareClick) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = stringResource(R.string.details_share),
                        )
                    }
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) {
                                stringResource(R.string.details_favorite_remove)
                            } else {
                                stringResource(R.string.details_favorite_add)
                            },
                            tint = if (isFavorite) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                    IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = stringResource(R.string.details_refresh),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.padding(paddingValues),
        ) {
            val facts = countryFacts(country)

            Column(modifier = Modifier.fillMaxSize()) {
                if (isRefreshing) {
                    LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item(key = "hero", contentType = "hero") {
                        CountryHero(country = country, cacheAge = cacheAge, isCacheFresh = isCacheFresh)
                    }

                    if (aiSummary !is CountryDetailsContract.AiSummaryState.Disabled) {
                        item(key = "ai-summary", contentType = "ai-summary") {
                            AiSummaryCard(aiSummary = aiSummary)
                        }
                    }

                    if (sideBySide) {
                        item(key = "facts-and-map", contentType = "facts-and-map") {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    SectionHeader(text = stringResource(R.string.details_section_information))
                                    FactGrid(facts = facts, columns = factColumns.coerceAtMost(2))
                                }
                                CountryMapCard(
                                    country = country,
                                    showBorders = showMapBorders,
                                    hasLocation = hasLocation,
                                    onOpenInMapsClick = onOpenInMapsClick,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    } else {
                        item(key = "map-card", contentType = "map-card") {
                            CountryMapCard(
                                country = country,
                                showBorders = showMapBorders,
                                hasLocation = hasLocation,
                                onOpenInMapsClick = onOpenInMapsClick,
                            )
                        }
                        item(key = "details-header", contentType = "section-header") {
                            SectionHeader(text = stringResource(R.string.details_section_information))
                        }
                        items(
                            items = facts.chunked(factColumns),
                            key = { row -> row.first().label },
                            contentType = { "fact-row" },
                        ) { row ->
                            FactRow(row = row, columns = factColumns)
                        }
                    }

                    item(key = "nearby-countries", contentType = "nearby-countries") {
                        NearbyCountriesSection(
                            region = country.region,
                            nearbyCountries = nearbyCountries,
                            isLoading = isLoadingNearby,
                            onCountryClick = onNearbyCountryClick,
                        )
                    }
                }
            }
        }
    }
}

/** Flag plate, serif name, region and capital, and the cache-age chip. */
@Composable
private fun CountryHero(
    country: Country,
    cacheAge: String?,
    isCacheFresh: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        FlagImage(
            twoLetterCode = country.twoLetterCode,
            contentDescription = stringResource(R.string.details_flag_desc, country.name),
            modifier = Modifier
                .widthIn(max = HERO_FLAG_MAX_WIDTH)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = country.name,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        val subtitle = listOf(country.region, country.capital).filter { it.isNotBlank() }.joinToString(" · ")
        if (subtitle.isNotEmpty()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        if (cacheAge != null) {
            CacheAgeChip(cacheAge = cacheAge, isFresh = isCacheFresh, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun CacheAgeChip(
    cacheAge: String,
    isFresh: Boolean,
    modifier: Modifier = Modifier,
) {
    val dotColor = if (isFresh) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
    AssistChip(
        onClick = {},
        enabled = false,
        label = { Text(stringResource(R.string.details_updated, cacheAge)) },
        leadingIcon = {
            Surface(
                modifier = Modifier.size(8.dp),
                shape = MaterialTheme.shapes.extraSmall,
                color = dotColor,
                content = {},
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = false,
            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
        ),
        modifier = modifier,
    )
}

private data class CountryFact(val label: String, val value: String)

@Composable
private fun countryFacts(country: Country): List<CountryFact> {
    val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
    return listOf(
        CountryFact(stringResource(R.string.details_label_capital), country.capital),
        // The mledoze dataset has no population; 0 means unknown, which FactTile renders as a dash.
        CountryFact(
            stringResource(R.string.details_label_population),
            if (country.population > 0) numberFormat.format(country.population) else "",
        ),
        CountryFact(stringResource(R.string.details_label_region), country.region),
        CountryFact(
            stringResource(R.string.details_label_languages),
            country.languages.mapNotNull { it.name }.joinToString(", "),
        ),
        CountryFact(
            stringResource(R.string.details_label_currencies),
            country.currencies.mapNotNull { it.name }.joinToString(", "),
        ),
        CountryFact(stringResource(R.string.details_label_calling_code), country.callingCode),
        CountryFact(stringResource(R.string.details_label_two_letter), country.twoLetterCode),
        CountryFact(stringResource(R.string.details_label_three_letter), country.threeLetterCode),
    )
}

@Composable
private fun FactGrid(facts: List<CountryFact>, columns: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        facts.chunked(columns).forEach { row -> FactRow(row = row, columns = columns) }
    }
}

@Composable
private fun FactRow(row: List<CountryFact>, columns: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        row.forEach { fact ->
            FactTile(label = fact.label, value = fact.value, modifier = Modifier.weight(1f))
        }
        repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
    }
}

@Composable
private fun AiSummaryCard(
    aiSummary: CountryDetailsContract.AiSummaryState,
    modifier: Modifier = Modifier,
) {
    var expanded by remember(aiSummary) { mutableStateOf(true) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.details_ai_summary_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(R.string.details_ai_summary_on_device),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) {
                            stringResource(R.string.details_ai_summary_collapse)
                        } else {
                            stringResource(R.string.details_ai_summary_expand)
                        },
                    )
                }
            }

            if (expanded) {
                val bodyModifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                when (aiSummary) {
                    CountryDetailsContract.AiSummaryState.Loading -> {
                        Row(
                            modifier = bodyModifier,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            ContainedLoadingIndicator(modifier = Modifier.size(32.dp))
                            Text(
                                text = stringResource(R.string.details_ai_summary_loading),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }

                    is CountryDetailsContract.AiSummaryState.Ready -> {
                        Text(
                            text = aiSummary.summary,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = bodyModifier,
                        )
                    }

                    CountryDetailsContract.AiSummaryState.Unavailable -> {
                        Text(
                            text = stringResource(R.string.details_ai_summary_unavailable),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = bodyModifier,
                        )
                    }

                    CountryDetailsContract.AiSummaryState.Disabled -> Unit
                }
            }
        }
    }
}

@Composable
private fun CountryMapCard(
    country: Country,
    showBorders: Boolean,
    hasLocation: Boolean,
    onOpenInMapsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(MAP_HEIGHT)
                .clipToBounds(),
            contentAlignment = Alignment.Center,
        ) {
            if (hasLocation && LocalInspectionMode.current) {
                // osmdroid cannot render in previews or screenshot tests.
                Icon(
                    imageVector = Icons.Outlined.Map,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (hasLocation) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        Configuration.getInstance().userAgentValue = ctx.packageName

                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

                            val countryLocation = GeoPoint(country.latitude, country.longitude)
                            controller.setZoom(MAP_ZOOM)
                            controller.setCenter(countryLocation)

                            // Required by the OpenStreetMap license.
                            overlays.add(CopyrightOverlay(ctx))

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
                        mapView.controller.setCenter(GeoPoint(country.latitude, country.longitude))
                        if (showBorders) {
                            CountryBorderOverlay.applyBorder(
                                context = context,
                                mapView = mapView,
                                alpha3Code = country.threeLetterCode,
                                latitude = country.latitude,
                                longitude = country.longitude,
                            )
                        }
                    },
                )
            } else {
                Text(
                    text = stringResource(R.string.details_location_unavailable, country.name),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
        if (hasLocation) {
            FilledTonalButton(
                onClick = onOpenInMapsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Map,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.details_open_in_maps))
            }
        }
    }
}

@Composable
private fun NearbyCountriesSection(
    region: String,
    nearbyCountries: List<CountrySummary>,
    isLoading: Boolean,
    onCountryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(text = stringResource(R.string.details_nearby_title, region))
        Spacer(Modifier.height(8.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(NEARBY_PLACEHOLDER_HEIGHT),
                    contentAlignment = Alignment.Center,
                ) {
                    ContainedLoadingIndicator(modifier = Modifier.size(48.dp))
                }
            }

            nearbyCountries.isEmpty() -> {
                EmptyState(
                    message = stringResource(R.string.details_nearby_empty),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = NEARBY_PLACEHOLDER_HEIGHT),
                )
            }

            else -> {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(
                        items = nearbyCountries,
                        key = { it.threeLetterCode },
                    ) { country ->
                        NearbyCountryPlate(
                            country = country,
                            onClick = { onCountryClick(country.threeLetterCode) },
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun NearbyCountryPlate(
    country: CountrySummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = rememberPressScaleInteractionSource()
    Surface(
        onClick = onClick,
        modifier = modifier
            .width(NEARBY_PLATE_WIDTH * LocalDensity.current.fontScale)
            .pressScaleEffect(interactionSource),
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            FlagImage(
                twoLetterCode = country.twoLetterCode,
                contentDescription = stringResource(R.string.details_flag_desc, country.name),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = country.name,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (country.capital.isNotEmpty()) {
                Text(
                    text = country.capital,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun CountryDetailsErrorContent(
    message: String,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.details_error_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.details_navigate_back),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        ErrorState(
            message = message,
            onRetry = onRetry,
            retryLabel = stringResource(R.string.details_retry),
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
        )
    }
}

private const val EXPANDED_WIDTH_DP = 600
private const val LARGE_WIDTH_DP = 840
private const val LARGE_FONT_SCALE = 1.5f
private const val MAP_ZOOM = 5.0
private val MAP_HEIGHT = 220.dp
private val HERO_FLAG_MAX_WIDTH = 480.dp
private val NEARBY_PLATE_WIDTH = 120.dp
private val NEARBY_PLACEHOLDER_HEIGHT = 96.dp

// -----------------------------------------------------------------------------
// Previews
// -----------------------------------------------------------------------------

internal fun sampleCountry() = Country(
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
    longitude = -77.0364,
)

internal fun sampleNearbyCountries() = listOf(
    CountrySummary(
        name = "Canada",
        capital = "Ottawa",
        region = "Americas",
        population = 38005238,
        twoLetterCode = "CA",
        threeLetterCode = "CAN",
        latitude = 56.1304,
        longitude = -106.3468,
    ),
    CountrySummary(
        name = "Mexico",
        capital = "Mexico City",
        region = "Americas",
        population = 128932753,
        twoLetterCode = "MX",
        threeLetterCode = "MEX",
        latitude = 23.6345,
        longitude = -102.5528,
    ),
    CountrySummary(
        name = "Brazil",
        capital = "Brasília",
        region = "Americas",
        population = 212559417,
        twoLetterCode = "BR",
        threeLetterCode = "BRA",
        latitude = -14.235,
        longitude = -51.9253,
    ),
)

@PreviewLightDark
@Preview(name = "Country details", showBackground = true)
@Composable
private fun CountryDetailsScreenPreview() {
    WorldCountriesTheme(dynamicColor = false) {
        CountryDetailsScreen(
            country = sampleCountry(),
            isFavorite = true,
            nearbyCountries = sampleNearbyCountries(),
            onNavigateBack = {},
            cacheAge = "2 hours ago",
            isCacheFresh = true,
        )
    }
}

@Preview(name = "Error content", showBackground = true)
@Composable
private fun CountryDetailsErrorContentPreview() {
    WorldCountriesTheme(dynamicColor = false) {
        CountryDetailsErrorContent(
            message = "Failed to load country details. Please try again.",
            onRetry = {},
            onNavigateBack = {},
        )
    }
}
