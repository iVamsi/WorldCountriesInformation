@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.vamsi.worldcountriesinformation.feature.compare

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vamsi.snapnotify.SnapNotify
import com.vamsi.worldcountriesinformation.core.common.error.AppError
import com.vamsi.worldcountriesinformation.core.common.error.message
import com.vamsi.worldcountriesinformation.core.common.testing.UiTestTags
import com.vamsi.worldcountriesinformation.core.designsystem.WorldCountriesTheme
import com.vamsi.worldcountriesinformation.core.designsystem.component.ErrorState
import com.vamsi.worldcountriesinformation.core.designsystem.component.FactTile
import com.vamsi.worldcountriesinformation.core.designsystem.component.FlagImage
import com.vamsi.worldcountriesinformation.domainmodel.Country
import com.vamsi.worldcountriesinformation.domainmodel.Currency
import com.vamsi.worldcountriesinformation.domainmodel.Language
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CompareRoute(
    countryCodes: List<String>,
    onNavigateBack: () -> Unit,
    viewModel: CompareViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is CompareContract.Effect.NavigateBack -> onNavigateBack()

                is CompareContract.Effect.ShowError -> {
                    SnapNotify.showError(context.message(effect.error))
                }
            }
        }
    }

    LaunchedEffect(countryCodes) {
        viewModel.processIntent(CompareContract.Intent.LoadCountries(countryCodes))
    }

    CompareScreen(
        state = state,
        onRetry = { viewModel.processIntent(CompareContract.Intent.RetryLoading(countryCodes)) },
        onNavigateBack = { viewModel.processIntent(CompareContract.Intent.NavigateBack) },
    )
}

@Composable
internal fun CompareScreen(
    state: CompareContract.State,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = Modifier.testTag(UiTestTags.COMPARE_SCREEN),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.compare_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.compare_navigate_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.showLoading -> LoadingState()

                state.showError -> ErrorState(
                    message = state.error?.let { errorMessage(it) }
                        ?: stringResource(R.string.compare_error_load_failed),
                    onRetry = onRetry,
                    retryLabel = stringResource(R.string.compare_retry),
                )

                state.hasData -> Column(modifier = Modifier.fillMaxSize()) {
                    if (state.insight != null) {
                        InsightCard(
                            insight = state.insight,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                    CompareTable(
                        countries = state.countries,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun errorMessage(error: AppError): String = LocalResources.current.message(error)

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularWavyProgressIndicator(modifier = Modifier.size(48.dp))
    }
}

@Composable
private fun InsightCard(insight: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
            Column {
                Text(
                    text = stringResource(R.string.compare_insight_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = insight,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

private data class CompareRow(val label: String, val values: List<String>)

@Composable
private fun CompareTable(
    countries: List<Country>,
    modifier: Modifier = Modifier,
) {
    val rows = buildRows(countries)
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        if (maxWidth >= GRID_MIN_WIDTH) {
            CompareGridLayout(rows = rows, countries = countries)
        } else {
            val available = maxWidth - TABLE_PADDING * 2 - LABEL_COLUMN_WIDTH - CELL_GAP * countries.size
            CompareScrollTable(
                rows = rows,
                countries = countries,
                valueColumnWidth = (available / countries.size).coerceAtLeast(MIN_VALUE_COLUMN_WIDTH),
            )
        }
    }
}

/** One card per country on wide layouts; every card lists the same facts in the same order. */
@Composable
private fun CompareGridLayout(rows: List<CompareRow>, countries: List<Country>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(countries.size.coerceAtMost(MAX_COLUMNS)),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(countries, key = { it.threeLetterCode }) { country ->
            val index = countries.indexOf(country)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CountryHeader(country = country, modifier = Modifier.padding(bottom = 4.dp))
                rows.forEach { row ->
                    FactTile(label = row.label, value = row.values.getOrNull(index).orEmpty())
                }
            }
        }
    }
}

/** Compact layout: a striped table that scrolls sideways as one unit. */
@Composable
private fun CompareScrollTable(rows: List<CompareRow>, countries: List<Country>, valueColumnWidth: Dp) {
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = TABLE_PADDING, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(CELL_GAP),
        ) {
            Spacer(Modifier.width(LABEL_COLUMN_WIDTH))
            countries.forEach { country ->
                CountryHeader(country = country, modifier = Modifier.width(valueColumnWidth))
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            itemsIndexed(rows, key = { _, row -> row.label }) { index, row ->
                val stripe = if (index % 2 == 0) {
                    MaterialTheme.colorScheme.surfaceContainerLow
                } else {
                    Color.Transparent
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(stripe)
                        .horizontalScroll(scrollState)
                        .padding(horizontal = TABLE_PADDING, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(CELL_GAP),
                ) {
                    Text(
                        text = row.label.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(LABEL_COLUMN_WIDTH),
                    )
                    row.values.forEach { value ->
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.width(valueColumnWidth),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CountryHeader(country: Country, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        FlagImage(
            twoLetterCode = country.twoLetterCode,
            contentDescription = null,
            modifier = Modifier.width(HEADER_FLAG_WIDTH),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = country.name,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun buildRows(countries: List<Country>): List<CompareRow> {
    val nf = NumberFormat.getNumberInstance(Locale.getDefault())
    return listOf(
        CompareRow(
            label = stringResource(R.string.compare_label_capital),
            values = countries.map { it.capital.ifEmpty { "—" } },
        ),
        CompareRow(
            label = stringResource(R.string.compare_label_region),
            values = countries.map { it.region.ifEmpty { "—" } },
        ),
        CompareRow(
            label = stringResource(R.string.compare_label_population),
            values = countries.map { nf.format(it.population) },
        ),
        CompareRow(
            label = stringResource(R.string.compare_label_languages),
            values = countries.map { country ->
                country.languages
                    .mapNotNull { it.name }
                    .joinToString(", ")
                    .ifEmpty { "—" }
            },
        ),
        CompareRow(
            label = stringResource(R.string.compare_label_currencies),
            values = countries.map { country ->
                country.currencies
                    .mapNotNull { c -> c.name?.let { name -> c.code?.let { "$name ($it)" } ?: name } }
                    .joinToString(", ")
                    .ifEmpty { "—" }
            },
        ),
        CompareRow(
            label = stringResource(R.string.compare_label_calling_code),
            values = countries.map { it.callingCode.ifEmpty { "—" } },
        ),
    )
}

private const val MAX_COLUMNS = 3
private val GRID_MIN_WIDTH = 600.dp
private val LABEL_COLUMN_WIDTH: Dp = 96.dp
private val MIN_VALUE_COLUMN_WIDTH: Dp = 120.dp
private val TABLE_PADDING = 16.dp
private val CELL_GAP = 12.dp
private val HEADER_FLAG_WIDTH = 36.dp

// -----------------------------------------------------------------------------
// Previews
// -----------------------------------------------------------------------------

internal fun previewCompareState(count: Int = 2, insight: String? = null) = CompareContract.State(
    countries = listOf(
        sampleCountry("United States", "US", "USA", "Washington, D.C.", 331_002_651),
        sampleCountry("Canada", "CA", "CAN", "Ottawa", 38_005_238),
        sampleCountry("Mexico", "MX", "MEX", "Mexico City", 128_932_753),
    ).take(count),
    insight = insight,
)

private fun sampleCountry(name: String, code2: String, code3: String, capital: String, population: Int) = Country(
    name = name,
    capital = capital,
    languages = listOf(Language(name = "English"), Language(name = "Spanish")),
    twoLetterCode = code2,
    threeLetterCode = code3,
    population = population,
    region = "Americas",
    currencies = listOf(Currency(code = "USD", name = "Dollar", symbol = "$")),
    callingCode = "+1",
    latitude = 0.0,
    longitude = 0.0,
)

@PreviewLightDark
@Preview(name = "Compare screen", showBackground = true)
@Composable
private fun CompareScreenPreview() {
    WorldCountriesTheme(dynamicColor = false) {
        CompareScreen(
            state = previewCompareState(count = 3, insight = "Mexico has the largest population of the three."),
            onRetry = {},
            onNavigateBack = {},
        )
    }
}
