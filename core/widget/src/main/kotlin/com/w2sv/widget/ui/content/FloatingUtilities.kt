package com.w2sv.widget.ui.content

import android.graphics.Color
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.ColorUtils
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.w2sv.widget.ui.actions.WidgetAction
import com.w2sv.widget.ui.layout.ActionLayout
import com.w2sv.widget.ui.layout.TimestampLayout
import com.w2sv.widget.ui.state.WidgetPresentationState
import com.w2sv.widget.ui.theme.WidgetDimens
import com.w2sv.widget.ui.theme.toColorProvider
import com.w2sv.widget.ui.util.thenIfNotNull

/** The timestamp and action row are separate Box children, so only buttons receive their click actions. */
@Composable
internal fun FloatingUtilities(
    state: WidgetPresentationState,
    actions: ActionLayout,
    timestamp: TimestampLayout?
) {
    val surface = floatingSurfaceColor(state)
    timestamp?.let { time ->
        Box(
            GlanceModifier
                .fillMaxSize()
                .padding(start = actions.edge, end = actions.edge + time.endClearance, bottom = actions.edge),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = time.text,
                modifier = GlanceModifier
                    .background(surface.toColorProvider())
                    .cornerRadius(WidgetDimens.Timestamp.cornerRadius)
                    .padding(horizontal = time.horizontalPadding, vertical = time.verticalPadding),
                style = TextStyle(color = state.colors.secondary.toColorProvider(), fontSize = time.fontSize),
                maxLines = 1
            )
        }
    }
    if (state.utilities.actions.isNotEmpty()) {
        Box(
            GlanceModifier
                .fillMaxSize()
                .padding(actions.edge),
            contentAlignment = Alignment.BottomEnd
        ) {
            Row(verticalAlignment = Alignment.Vertical.Bottom) {
                state.utilities.actions.forEachIndexed { index, action ->
                    if (index > 0) Spacer(GlanceModifier.width(actions.spacing))
                    UtilityButton(
                        action = action,
                        tint = state.colors.primary,
                        surface = surface,
                        size = actions.buttonSize,
                        iconSize = actions.iconSize
                    )
                }
            }
        }
    }
}

/** Blend opaque colors once, then apply the resolved widget background alpha once. */
@VisibleForTesting
internal fun floatingSurfaceColor(state: WidgetPresentationState): Int =
    ColorUtils.setAlphaComponent(
        ColorUtils.blendARGB(state.colors.background or -0x1000000, state.colors.primary or -0x1000000, 0.12f),
        Color.alpha(state.backgroundColor)
    )

@Composable
private fun UtilityButton(
    action: WidgetAction,
    tint: Int,
    surface: Int,
    size: Dp,
    iconSize: Dp
) {
    val context = LocalContext.current
    Box(
        modifier = GlanceModifier
            .size(size)
            .background(surface.toColorProvider())
            .cornerRadius(size / 2)
            .thenIfNotNull(action.createAction(context)) { clickable(it) },
        contentAlignment = Alignment.Center
    ) {
        Image(
            provider = ImageProvider(action.iconRes),
            contentDescription = context.getString(action.descriptionRes),
            modifier = GlanceModifier.size(iconSize),
            colorFilter = ColorFilter.tint(tint.toColorProvider())
        )
    }
}
