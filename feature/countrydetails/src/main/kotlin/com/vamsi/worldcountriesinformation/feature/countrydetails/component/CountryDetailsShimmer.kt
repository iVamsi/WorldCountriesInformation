package com.vamsi.worldcountriesinformation.feature.countrydetails.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.vamsi.worldcountriesinformation.core.designsystem.component.ShimmerEffect

/** Loading placeholder that mirrors the details hero, map card, and fact grid. */
@Composable
fun CountryDetailsShimmer(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ShimmerEffect(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .aspectRatio(3f / 2f)
                .clip(MaterialTheme.shapes.large),
        )
        Spacer(Modifier.height(4.dp))
        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(32.dp)
                .clip(MaterialTheme.shapes.extraSmall),
        )
        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(18.dp)
                .clip(MaterialTheme.shapes.extraSmall),
        )
        Spacer(Modifier.height(4.dp))
        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(MaterialTheme.shapes.large),
        )
        repeat(2) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(2) {
                    ShimmerEffect(
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                            .clip(MaterialTheme.shapes.medium),
                    )
                }
            }
        }
    }
}
