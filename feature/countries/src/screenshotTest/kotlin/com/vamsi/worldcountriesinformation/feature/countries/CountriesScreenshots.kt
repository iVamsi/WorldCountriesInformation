package com.vamsi.worldcountriesinformation.feature.countries

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.tools.screenshot.PreviewTest
import com.vamsi.worldcountriesinformation.core.designsystem.AppBackground
import com.vamsi.worldcountriesinformation.core.designsystem.WorldCountriesTheme

@Composable
private fun Screenshot(state: CountriesContract.State, cacheAge: String? = null) {
    WorldCountriesTheme(dynamicColor = false) {
        AppBackground {
            CountriesScreenContent(
                state = state,
                listState = rememberLazyListState(),
                cacheAge = cacheAge,
                onNavigateToSettings = {},
                onIntent = {},
                onMicClick = {},
            )
        }
    }
}

@PreviewTest
@PreviewLightDark
@Preview(name = "200%", fontScale = 2f)
@Preview(name = "Expanded", widthDp = 840, heightDp = 600)
@Composable
private fun CountriesList() = Screenshot(previewListState(), cacheAge = "2 hours ago")

@PreviewTest
@PreviewLightDark
@Composable
private fun CountriesSelection() = Screenshot(previewSelectionState())

@PreviewTest
@Preview
@Composable
private fun CountriesSearchHistory() = Screenshot(previewSearchHistoryState())

@PreviewTest
@Preview
@Preview(name = "200%", fontScale = 2f)
@Composable
private fun CountriesEmptySearch() = Screenshot(previewEmptySearchState())
