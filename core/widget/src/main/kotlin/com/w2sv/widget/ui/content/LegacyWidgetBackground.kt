package com.w2sv.widget.ui.content

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import com.w2sv.core.widget.R
import kotlin.math.roundToInt

@Composable
internal fun LegacyWidgetBackground(size: DpSize, color: Int) {
    val context = LocalContext.current
    val density = context.resources.displayMetrics.density
    val bitmap = remember(size, color, density) { legacyWidgetBackgroundBitmap(context, size, color) }

    Image(
        provider = ImageProvider(bitmap),
        contentDescription = null,
        modifier = GlanceModifier.fillMaxSize(),
        contentScale = ContentScale.FillBounds
    )
}

/** Glance 1.2 ignores background-image tint and alpha in RemoteViews, so pre-31 colors need a bitmap. */
@VisibleForTesting
internal fun legacyWidgetBackgroundBitmap(
    context: Context,
    size: DpSize,
    color: Int
): Bitmap {
    val density = context.resources.displayMetrics.density
    val width = (size.width.value * density).roundToInt().coerceAtLeast(1)
    val height = (size.height.value * density).roundToInt().coerceAtLeast(1)
    val bitmap = createBitmap(width, height)
    checkNotNull(ContextCompat.getDrawable(context, R.drawable.widget_background_legacy)).mutate()
        .apply {
            setTint(color)
            setBounds(0, 0, width, height)
            draw(Canvas(bitmap))
        }
    return bitmap
}
