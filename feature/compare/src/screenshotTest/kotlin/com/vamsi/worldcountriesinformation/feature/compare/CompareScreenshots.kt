package com.vamsi.worldcountriesinformation.feature.compare

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.tools.screenshot.PreviewTest
import com.vamsi.worldcountriesinformation.core.designsystem.AppBackground
import com.vamsi.worldcountriesinformation.core.designsystem.WorldCountriesTheme

@Composable
private fun Screenshot(state: CompareContract.State) {
    WorldCountriesTheme(dynamicColor = false) {
        AppBackground { CompareScreen(state = state, onRetry = {}, onNavigateBack = {}) }
    }
}

@PreviewTest
@PreviewLightDark
@Preview(name = "200%", fontScale = 2f)
@Composable
private fun CompareThree() = Screenshot(
    previewCompareState(count = 3, insight = "Mexico has the largest population of the three."),
)

@PreviewTest
@Preview
@Preview(name = "Expanded", widthDp = 900, heightDp = 800)
@Composable
private fun CompareTwo() = Screenshot(previewCompareState(count = 2))
