package com.vamsi.worldcountriesinformation.feature.countries.component

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.vamsi.worldcountriesinformation.core.designsystem.component.ShimmerEffect

/**
 * Shimmer loading placeholder for a country card.
 *
 * Displays an animated shimmer effect that mimics the layout of a CountryCard
 * while data is being loaded. This provides better visual feedback than a
 * simple loading spinner.
 *
 * @param modifier Modifier to be applied to the card
 */
@Composable
fun CountryCardShimmer(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flag placeholder
            ShimmerEffect(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Text content placeholder
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Country name placeholder
                ShimmerEffect(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                // Capital and region placeholder
                ShimmerEffect(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

/**
 * Shimmer loading state for the entire countries list.
 *
 * Displays multiple shimmer country cards in a scrollable list to indicate
 * that data is being loaded. The number of shimmer cards can be customized.
 *
 * @param modifier Modifier to be applied to the list
 * @param itemCount Number of shimmer cards to display (default: 8)
 */
@Composable
fun CountriesListShimmer(
    modifier: Modifier = Modifier,
    itemCount: Int = 8,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(itemCount) {
            CountryCardShimmer()
        }
    }
}

/**
 * Compact shimmer loading placeholder for search results.
 *
 * A smaller version of the country card shimmer, useful for displaying
 * in search result dropdowns or compact lists.
 *
 * @param modifier Modifier to be applied to the component
 */
@Composable
fun SearchResultShimmer(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Flag placeholder
        ShimmerEffect(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Text content placeholder
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Country name placeholder
            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            // Additional info placeholder
            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}
