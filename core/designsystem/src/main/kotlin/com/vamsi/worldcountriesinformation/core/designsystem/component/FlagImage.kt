package com.vamsi.worldcountriesinformation.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import java.util.Locale

/** Drawable name for a country's flag, e.g. `ca_flag`. */
fun flagDrawableName(twoLetterCode: String): String = "${twoLetterCode.lowercase(Locale.US)}_flag"

/**
 * A flag drawn as a plate: fixed 3:2 ratio, clipped to [shape], with a hairline `outlineVariant`
 * border so white or pale flags keep an edge on paper. Falls back to the ISO code when no
 * drawable exists, and in previews (Coil does not load there). Callers set the width (or height);
 * the other axis follows.
 */
@Composable
fun FlagImage(
    twoLetterCode: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
) {
    val context = LocalContext.current
    val resId = remember(twoLetterCode) {
        context.resources.getIdentifier(flagDrawableName(twoLetterCode), "drawable", context.packageName)
    }
    Box(
        modifier = modifier
            .aspectRatio(FLAG_ASPECT_RATIO)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
        contentAlignment = Alignment.Center,
    ) {
        if (resId != 0 && !LocalInspectionMode.current) {
            AsyncImage(
                model = ImageRequest.Builder(context).data(resId).crossfade(true).build(),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )
        } else {
            Text(
                text = twoLetterCode,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private const val FLAG_ASPECT_RATIO = 3f / 2f
