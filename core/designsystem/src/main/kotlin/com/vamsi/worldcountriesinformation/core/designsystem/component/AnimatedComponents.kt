@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.vamsi.worldcountriesinformation.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Use with Modifier.clickable(interactionSource = interactionSource) for press scale effect.
 */
@Composable
fun rememberPressScaleInteractionSource(): MutableInteractionSource =
    remember { MutableInteractionSource() }

/**
 * Scale modifier that animates on press. Pass the same interactionSource to clickable.
 */
fun Modifier.pressScaleEffect(
    interactionSource: MutableInteractionSource,
    scaleDown: Float = 0.97f
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "pressScale"
    )
    scale(scale)
}

/**
 * Fade-in modifier for staggered list animations.
 */
fun Modifier.fadeInScaleUp(
    visible: Boolean,
    initialAlpha: Float = 0f,
    initialScale: Float = 0.9f
): Modifier = composed {
    val spatial = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else initialAlpha,
        animationSpec = spatial,
        label = "fadeInAlpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else initialScale,
        animationSpec = spatial,
        label = "fadeInScale"
    )
    graphicsLayer {
        this.alpha = alpha
        scaleX = scale
        scaleY = scale
    }
}
