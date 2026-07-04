package com.vamsi.worldcountriesinformation.feature.compare

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vamsi.snapnotify.SnapNotify
import com.vamsi.worldcountriesinformation.core.common.error.message
import com.vamsi.worldcountriesinformation.core.common.testing.UiTestTags
import com.vamsi.worldcountriesinformation.core.designsystem.WorldCountriesTheme
import com.vamsi.worldcountriesinformation.core.designsystem.component.ErrorState
import com.vamsi.worldcountriesinformation.domainmodel.Country
import com.vamsi.worldcountriesinformation.domainmodel.Currency
import com.vamsi.worldcountriesinformation.domainmodel.Language
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CompareScreen(
    state: CompareContract.State,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = Modifier.testTag(UiTestTags.COMPARE_SCREEN),
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
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
                    message = state.error?.let { LocalContextMessage(it) }
                        ?: stringResource(R.string.compare_error_load_failed),
                    onRetry = onRetry,
                    retryLabel = stringResource(R.string.compare_retry),
                )

                state.hasData -> Column(modifier = Modifier.fillMaxSize()) {
                    state.insight?.let { insight ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = stringResource(R.string.compare_insight_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = insight,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
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
private fun LocalContextMessage(error: com.vamsi.worldcountriesinformation.core.common.error.AppError): String {
    val resources = androidx.compose.ui.platform.LocalResources.current
    return resources.message(error)
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularWavyProgressIndicator(modifier = Modifier.size(48.dp))
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
        if (maxWidth >= 600.dp) {
            CompareGridLayout(rows = rows, countries = countries)
        } else {
            CompareScrollTable(rows = rows, countries = countries)
        }
    }
}

@Composable
private fun CompareGridLayout(rows: List<CompareRow>, countries: List<Country>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(countries.size.coerceAtMost(3)),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(countries, key = { it.threeLetterCode }) { country ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = country.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                    rows.forEach { row ->
                        val index = countries.indexOf(country)
                        Text(
                            text = "${row.label}: ${row.values.getOrNull(index).orEmpty()}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompareScrollTable(rows: List<CompareRow>, countries: List<Country>) {
    val scrollState = rememberScrollState()
    val columnWidth = 160.dp

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 12.dp, horizontal = 16.dp),
        ) {
            HeaderCell(text = "", width = columnWidth)
            countries.forEach { country ->
                HeaderCell(text = country.name, width = columnWidth)
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            items(rows, key = { it.label }) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                ) {
                    BodyCell(text = row.label, width = columnWidth, emphasized = true)
                    row.values.forEach { value ->
                        BodyCell(text = value, width = columnWidth)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.width(width),
    )
}

@Composable
private fun BodyCell(text: String, width: androidx.compose.ui.unit.Dp, emphasized: Boolean = false) {
    Text(
        text = text,
        style = if (emphasized) {
            MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        } else {
            MaterialTheme.typography.bodyMedium
        },
        modifier = Modifier.width(width),
    )
}

@Composable
private fun buildRows(countries: List<Country>): List<CompareRow> {
    val nf = NumberFormat.getNumberInstance(LocalLocale.current.platformLocale)
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

@Preview(name = "Compare screen", showBackground = true)
@Composable
private fun CompareScreenPreview() {
    WorldCountriesTheme {
        CompareScreen(
            state = CompareContract.State(
                countries = listOf(sample("United States", "USA"), sample("Canada", "CAN")),
            ),
            onRetry = {},
            onNavigateBack = {},
        )
    }
}

private fun sample(name: String, code: String) = Country(
    name = name,
    capital = "Capital",
    languages = listOf(Language(name = "English")),
    twoLetterCode = code.take(2),
    threeLetterCode = code,
    population = 1_000_000,
    region = "Region",
    currencies = listOf(Currency(code = "XYZ", name = "Sample", symbol = "$")),
    callingCode = "+1",
    latitude = 0.0,
    longitude = 0.0,
)
