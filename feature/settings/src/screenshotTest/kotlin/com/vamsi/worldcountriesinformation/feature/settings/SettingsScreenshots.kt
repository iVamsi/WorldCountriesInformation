package com.vamsi.worldcountriesinformation.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.tools.screenshot.PreviewTest
import com.vamsi.worldcountriesinformation.core.designsystem.AppBackground
import com.vamsi.worldcountriesinformation.core.designsystem.WorldCountriesTheme

@PreviewTest
@PreviewLightDark
@Preview(name = "200%", fontScale = 2f, heightDp = 1400)
@Preview(name = "Expanded", widthDp = 840, heightDp = 1200)
@Composable
private fun Settings() {
    WorldCountriesTheme(dynamicColor = false) {
        AppBackground {
            SettingsScreenContent(
                state = SettingsContract.State(
                    cacheStats = CacheStats(entryCount = 250, estimatedSizeKB = 512, oldestEntryAgeMs = 7_200_000),
                ),
                onIntent = {},
                onNavigateBack = {},
            )
        }
    }
}
