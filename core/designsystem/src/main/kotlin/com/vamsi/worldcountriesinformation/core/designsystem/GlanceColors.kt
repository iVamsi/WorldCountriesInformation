package com.vamsi.worldcountriesinformation.core.designsystem

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.glance.GlanceTheme
import androidx.glance.material3.ColorProviders

/** Wallpaper colors on Android 12+, the static Explorer palette everywhere else. */
@Composable
fun explorerGlanceColors() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    GlanceTheme.colors
} else {
    ColorProviders(light = ExplorerLightColorScheme, dark = ExplorerDarkColorScheme)
}
