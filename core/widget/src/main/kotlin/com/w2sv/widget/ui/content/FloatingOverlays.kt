package com.w2sv.widget.ui.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
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
import androidx.glance.unit.ColorProvider
import com.w2sv.widget.ui.actions.WidgetButtonAction
import com.w2sv.widget.ui.layout.ActionLayout
import com.w2sv.widget.ui.layout.TimestampLayout
import com.w2sv.widget.ui.state.WidgetPresentationState
import com.w2sv.widget.ui.theme.WidgetDimens
import com.w2sv.widget.ui.theme.toColorProvider
import com.w2sv.widget.ui.util.thenIfNotNull

@Composable
internal fun Timestamp(
    timestamp: TimestampLayout,
    actionLayout: ActionLayout,
    surfaceColor: ColorProvider,
    textColor: ColorProvider
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(
                start = actionLayout.edge,
                end = actionLayout.edge + timestamp.endClearance,
                bottom = actionLayout.edge
            ),
        contentAlignment = Alignment.BottomStart
    ) {
        Text(
            text = timestamp.text,
            modifier = GlanceModifier
                .background(surfaceColor)
                .cornerRadius(WidgetDimens.Timestamp.cornerRadius)
                .padding(
                    horizontal = timestamp.horizontalPadding,
                    vertical = timestamp.verticalPadding
                ),
            style = TextStyle(color = textColor, fontSize = timestamp.fontSize),
            maxLines = 1
        )
    }
}

@Composable
internal fun UtilityFABs(
    state: WidgetPresentationState,
    actions: ActionLayout,
    surfaceColor: ColorProvider
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(actions.edge),
        contentAlignment = Alignment.BottomEnd
    ) {
        Row(verticalAlignment = Alignment.Vertical.Bottom) {
            state.utilities.actions.forEachIndexed { index, action ->
                if (index > 0) {
                    Spacer(GlanceModifier.width(actions.spacing))
                }

                UtilityButton(
                    action = action,
                    tint = state.colors.primary,
                    surface = surfaceColor,
                    size = actions.buttonSize,
                    iconSize = actions.iconSize
                )
            }
        }
    }
}

@Composable
private fun UtilityButton(
    action: WidgetButtonAction,
    tint: Int,
    surface: ColorProvider,
    size: Dp,
    iconSize: Dp
) {
    val context = LocalContext.current

    Box(
        modifier = GlanceModifier
            .size(size)
            .background(surface)
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
