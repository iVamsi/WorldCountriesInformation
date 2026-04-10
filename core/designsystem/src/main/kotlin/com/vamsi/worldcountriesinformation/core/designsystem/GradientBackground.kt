package com.vamsi.worldcountriesinformation.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * Root background with a subtle gradient that always matches the active [MaterialTheme.colorScheme].
 *
 * Hard-coded light/dark hex values break Material You (dynamic color): the gradient no longer
 * matches the wallpaper-derived scheme, so default text and `LocalContentColor` can look
 * low-contrast or hard to see.
 *
 * A transparent [Surface] sets content color so children inherit the correct `onSurface` for both
 * static Explorer and dynamic palettes.
 */
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val gradientColors = listOf(
        scheme.background,
        lerp(scheme.background, scheme.surfaceVariant, 0.38f),
        scheme.background,
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.Transparent,
        contentColor = scheme.onSurface,
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = gradientColors,
                            start = Offset(0f, 0f),
                            end = Offset(1000f, 1000f),
                        ),
                    ),
            )
            content()
        }
    }
}
