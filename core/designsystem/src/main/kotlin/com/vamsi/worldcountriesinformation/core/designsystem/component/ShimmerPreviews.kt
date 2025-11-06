package com.vamsi.worldcountriesinformation.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Preview file for ShimmerEffect component.
 *
 * Demonstrates various use cases of the shimmer effect including:
 * - Simple rectangular shimmer
 * - Circular shimmer (for avatars/images)
 * - Complex card layouts with multiple shimmers
 */

@Preview(name = "Simple Shimmer", showBackground = true)
@Composable
private fun SimpleShimmerPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Simple rectangular shimmer:")
            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
    }
}

@Preview(name = "Circular Shimmer", showBackground = true)
@Composable
private fun CircularShimmerPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Circular shimmer (for avatars):")
            ShimmerEffect(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            )
        }
    }
}

@Preview(name = "Card Shimmer Layout", showBackground = true)
@Composable
private fun CardShimmerPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar shimmer
                ShimmerEffect(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Text content shimmer
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShimmerEffect(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    ShimmerEffect(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}

@Preview(name = "Multiple Shimmers", showBackground = true)
@Composable
private fun MultipleShimmersPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Multiple shimmer placeholders:")

            repeat(3) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShimmerEffect(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ShimmerEffect(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )

                            ShimmerEffect(
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "List Item Shimmer", showBackground = true)
@Composable
private fun ListItemShimmerPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("List item shimmer pattern:")

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Title
                    ShimmerEffect(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    // Content rows
                    repeat(3) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ShimmerEffect(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )

                            ShimmerEffect(
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}
