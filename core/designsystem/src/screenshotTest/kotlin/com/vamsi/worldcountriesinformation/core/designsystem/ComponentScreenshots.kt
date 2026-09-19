package com.vamsi.worldcountriesinformation.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.vamsi.worldcountriesinformation.core.designsystem.component.EmptyState
import com.vamsi.worldcountriesinformation.core.designsystem.component.ErrorState
import com.vamsi.worldcountriesinformation.core.designsystem.component.FactTile
import com.vamsi.worldcountriesinformation.core.designsystem.component.FlagImage
import com.vamsi.worldcountriesinformation.core.designsystem.component.SectionHeader

@PreviewTest
@PreviewLightDark
@Preview(name = "200%", fontScale = 2f)
@Composable
private fun ErrorStateScreenshot() {
    WorldCountriesTheme(dynamicColor = false) {
        AppBackground {
            ErrorState(message = "Couldn't load countries. Check your connection.", onRetry = {})
        }
    }
}

@PreviewTest
@PreviewLightDark
@Preview(name = "200%", fontScale = 2f)
@Composable
private fun EmptyStateScreenshot() {
    WorldCountriesTheme(dynamicColor = false) {
        AppBackground {
            EmptyState(
                title = "No results for “Atlantis”",
                message = "Check the spelling or try a region filter.",
                icon = Icons.Outlined.Public,
                action = { TextButton(onClick = {}) { Text("Clear search") } },
            )
        }
    }
}

@PreviewTest
@PreviewLightDark
@Preview(name = "200%", fontScale = 2f)
@Composable
private fun PlatesAndTilesScreenshot() {
    WorldCountriesTheme(dynamicColor = false) {
        AppBackground {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader(text = "Country information")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // No flag drawables live in this module, so both render the fallback code.
                    FlagImage(twoLetterCode = "CA", contentDescription = "Flag of Canada", modifier = Modifier.width(56.dp))
                    FlagImage(
                        twoLetterCode = "JP",
                        contentDescription = "Flag of Japan",
                        modifier = Modifier.width(120.dp),
                        shape = MaterialTheme.shapes.large,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FactTile(label = "Capital", value = "Ottawa", modifier = Modifier.weight(1f))
                    FactTile(label = "Population", value = "38,000,000", modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FactTile(label = "Languages", value = "English, French", modifier = Modifier.weight(1f))
                    FactTile(label = "Calling code", value = "", modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
