package com.vamsi.worldcountriesinformation.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Creates a shimmer loading effect with an animated gradient.
 *
 * This composable provides a smooth, animated shimmer effect that can be used
 * as a placeholder while content is loading. The shimmer effect uses Material 3
 * color scheme for consistency with the app theme.
 *
 * @param modifier Modifier to be applied to the shimmer box
 * @param shimmerColors List of colors for the shimmer gradient. If null, uses default theme colors
 *
 * Usage:
 * ```
 * ShimmerEffect(
 *     modifier = Modifier
 *         .fillMaxWidth()
 *         .height(100.dp)
 *         .clip(RoundedCornerShape(8.dp))
 * )
 * ```
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    shimmerColors: List<Color>? = null,
) {
    val colors = shimmerColors ?: listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = colors,
        start = Offset(translateAnim.value - 1000f, translateAnim.value - 1000f),
        end = Offset(translateAnim.value, translateAnim.value)
    )

    Box(
        modifier = modifier
            .background(brush)
    )
}
