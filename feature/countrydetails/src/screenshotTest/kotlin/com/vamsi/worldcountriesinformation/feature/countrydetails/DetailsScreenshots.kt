package com.vamsi.worldcountriesinformation.feature.countrydetails

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.tools.screenshot.PreviewTest
import com.vamsi.worldcountriesinformation.core.designsystem.AppBackground
import com.vamsi.worldcountriesinformation.core.designsystem.WorldCountriesTheme

@Composable
private fun Screenshot(
    aiSummary: CountryDetailsContract.AiSummaryState = CountryDetailsContract.AiSummaryState.Disabled,
    isCacheFresh: Boolean = true,
) {
    WorldCountriesTheme(dynamicColor = false) {
        AppBackground {
            CountryDetailsScreen(
                country = sampleCountry(),
                isFavorite = true,
                nearbyCountries = sampleNearbyCountries(),
                onNavigateBack = {},
                aiSummary = aiSummary,
                cacheAge = "2 hours ago",
                isCacheFresh = isCacheFresh,
            )
        }
    }
}

@PreviewTest
@PreviewLightDark
@Preview(name = "200%", fontScale = 2f, heightDp = 1200)
@Preview(name = "Expanded", widthDp = 900, heightDp = 700)
@Composable
private fun Details() = Screenshot()

@PreviewTest
@Preview(heightDp = 1000)
@Composable
private fun DetailsWithAiSummary() = Screenshot(
    aiSummary = CountryDetailsContract.AiSummaryState.Ready(
        "The United States is a federal republic of 50 states in North America, with Washington, D.C. as its capital.",
    ),
    isCacheFresh = false,
)
