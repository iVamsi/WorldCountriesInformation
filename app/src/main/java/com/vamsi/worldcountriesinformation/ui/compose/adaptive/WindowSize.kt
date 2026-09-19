package com.vamsi.worldcountriesinformation.ui.compose.adaptive

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowWidthSizeClass

/** True on medium and expanded widths, where the list and detail panes sit side by side. */
@Composable
fun isExpandedWidth(): Boolean {
    val windowInfo = currentWindowAdaptiveInfo()
    return windowInfo.windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT
}
