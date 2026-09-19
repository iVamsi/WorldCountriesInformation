package com.vamsi.worldcountriesinformation.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.tools.screenshot.PreviewTest
import com.vamsi.worldcountriesinformation.core.designsystem.component.ErrorState

@PreviewTest
@PreviewLightDark
@Preview(name = "200%", fontScale = 2f, showBackground = true)
@Composable
private fun ErrorStateScreenshot() {
    WorldCountriesTheme(dynamicColor = false) {
        ErrorState(message = "Couldn't load countries. Check your connection.", onRetry = {})
    }
}
