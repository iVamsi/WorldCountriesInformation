package com.vamsi.worldcountriesinformation.feature.countrydetails.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.vamsi.worldcountriesinformation.core.designsystem.component.ShimmerEffect

/**
 * Shimmer loading placeholder for country details screen.
 *
 * Displays an animated shimmer effect that mimics the layout of the
 * country details content while data is being loaded.
 *
 * @param modifier Modifier to be applied to the component
 */
@Composable
fun CountryDetailsShimmer(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Flag placeholder
        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        // Country name placeholder
        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(32.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        // Basic info card placeholder
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section title
                ShimmerEffect(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                // Info rows
                repeat(4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ShimmerEffect(
                            modifier = Modifier
                                .width(100.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        ShimmerEffect(
                            modifier = Modifier
                                .width(120.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
        }

        // Languages card placeholder
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section title
                ShimmerEffect(
                    modifier = Modifier
                        .fillMaxWidth(0.3f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                // Language items
                repeat(2) {
                    ShimmerEffect(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }
        }

        // Map placeholder
        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(8.dp))
        )
    }
}

/**
 * Compact shimmer placeholder for detail sections.
 *
 * @param modifier Modifier to be applied to the component
 */
@Composable
fun DetailSectionShimmer(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title
            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(18.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            // Content rows
            repeat(3) {
                ShimmerEffect(
                    modifier = Modifier
                        .fillMaxWidth(0.7f - (it * 0.1f))
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        }
    }
}
