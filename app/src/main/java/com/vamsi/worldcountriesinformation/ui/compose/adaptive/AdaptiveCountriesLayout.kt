package com.vamsi.worldcountriesinformation.ui.compose.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.window.core.layout.WindowWidthSizeClass
import com.vamsi.worldcountriesinformation.feature.countries.CountriesScreen
import com.vamsi.worldcountriesinformation.feature.countrydetails.CountryDetailsRoute

/**
 * Two-pane list + detail layout for medium/expanded widths.
 */
@Composable
fun AdaptiveCountriesLayout(
    onNavigateToSettings: () -> Unit,
    onNavigateToCompare: (List<String>) -> Unit,
    onNavigateToQuiz: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedCode by remember { mutableStateOf<String?>(null) }

    Row(modifier = modifier.fillMaxSize()) {
        Box(
            Modifier
                .weight(if (selectedCode != null) 0.42f else 1f)
                .fillMaxHeight(),
        ) {
            CountriesScreen(
                onNavigateToDetails = { code -> selectedCode = code },
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToCompare = onNavigateToCompare,
                onNavigateToQuiz = onNavigateToQuiz,
            )
        }
        if (selectedCode != null) {
            Box(
                Modifier
                    .weight(0.58f)
                    .fillMaxHeight(),
            ) {
                CountryDetailsRoute(
                    countryCode = selectedCode!!,
                    onNavigateBack = { selectedCode = null },
                    onNavigateToCountry = { code -> selectedCode = code },
                )
            }
        }
    }
}

@Composable
fun isExpandedWidth(): Boolean {
    val windowInfo = currentWindowAdaptiveInfo()
    return windowInfo.windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT
}
