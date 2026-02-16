package com.vamsi.worldcountriesinformation.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * A subtle gradient background that adapts to light/dark theme.
 * Used as the app's root background for a softer look than plain solid colors.
 */
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val gradientColors = if (isDark) {
        listOf(
            Color(0xFF0F1211),
            Color(0xFF0A1614),
            Color(0xFF0F1211),
        )
    } else {
        listOf(
            Color(0xFFF6FAF8),
            Color(0xFFEFF5F3),
            Color(0xFFE8F0ED),
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f),
                ),
            ),
    ) {
        content()
    }
}
