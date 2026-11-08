package com.w2sv.widget.ui.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.semantics.semantics
import androidx.glance.semantics.testTag
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.w2sv.core.resources.R as ResourcesR
import com.w2sv.domain.networking.WifiStatus
import com.w2sv.widget.ui.actions.wifiSettingsAction
import com.w2sv.widget.ui.layout.StatusLayout
import com.w2sv.widget.ui.layout.StatusPresentation
import com.w2sv.widget.ui.state.WidgetPresentationState
import com.w2sv.widget.ui.theme.textSize
import com.w2sv.widget.ui.theme.toColorProvider
import com.w2sv.widget.ui.util.thenIfNotNull

@Composable
internal fun DisconnectedContent(state: WidgetPresentationState, layout: StatusLayout) {
    val context = LocalContext.current
    val message = context.getString(
        if (state.wifiState.status == WifiStatus.Disabled) ResourcesR.string.wifi_disabled else ResourcesR.string.no_wifi_connection
    )
    val icon = ImageProvider(state.wifiState.status.iconRes)
    val tint = ColorFilter.tint(state.colors.secondary.toColorProvider())
    val messageStyle = TextStyle(
        color = state.colors.secondary.toColorProvider(),
        fontSize = state.fontSize.textSize.sp,
        textAlign = TextAlign.Center
    )

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(start = layout.startPadding, end = layout.endPadding, bottom = layout.bottomPadding)
            .thenIfNotNull(wifiSettingsAction(context)) { clickable(it) },
        contentAlignment = layout.contentAlignment
    ) {
        when (layout.presentation) {
            StatusPresentation.TextOnly -> Text(message, style = messageStyle, maxLines = 2)

            StatusPresentation.Horizontal -> Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                Image(
                    provider = icon,
                    contentDescription = null,
                    modifier = GlanceModifier.size(layout.iconSize).semantics { testTag = STATUS_ICON_TAG },
                    colorFilter = tint
                )
                Spacer(GlanceModifier.width(layout.spacing))
                Text(message, style = messageStyle, maxLines = 2)
            }

            StatusPresentation.Vertical -> Column(horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
                Image(
                    provider = icon,
                    contentDescription = null,
                    modifier = GlanceModifier.size(layout.iconSize).semantics { testTag = STATUS_ICON_TAG },
                    colorFilter = tint
                )
                Spacer(GlanceModifier.height(layout.spacing))
                Text(message, style = messageStyle, maxLines = 2)
            }
        }
    }
}

internal const val STATUS_ICON_TAG = "disconnected-status-icon"
