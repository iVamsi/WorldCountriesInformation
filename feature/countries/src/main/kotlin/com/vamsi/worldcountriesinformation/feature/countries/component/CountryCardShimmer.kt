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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.vamsi.worldcountriesinformation.core.designsystem.component.ShimmerEffect

/** Loading placeholder that mirrors the search field and the first rows of the countries list. */
@Composable
fun CountriesListShimmer(
    modifier: Modifier = Modifier,
    itemCount: Int = 10,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        item {
            ShimmerEffect(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(CircleShape),
            )
            Spacer(Modifier.height(8.dp))
        }
        items(itemCount) {
            CountryRowShimmer()
        }
    }
}

@Composable
private fun CountryRowShimmer(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShimmerEffect(
                modifier = Modifier
                    .size(width = 56.dp, height = 37.dp)
                    .clip(MaterialTheme.shapes.small),
            )
            Spacer(Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ShimmerEffect(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(18.dp)
                        .clip(MaterialTheme.shapes.extraSmall),
                )
                ShimmerEffect(
                    modifier = Modifier
                        .fillMaxWidth(0.35f)
                        .height(14.dp)
                        .clip(MaterialTheme.shapes.extraSmall),
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(start = 88.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}
