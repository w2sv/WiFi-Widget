package com.w2sv.widget.ui.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.w2sv.core.common.R as CommonR
import com.w2sv.domain.model.networking.WifiStatus
import com.w2sv.domain.model.widget.FontSize
import com.w2sv.domain.model.widget.WidgetColors
import com.w2sv.widget.actions.wifiSettingsIntent
import com.w2sv.widget.ui.theme.toColorProvider

@Composable
internal fun DisconnectedContent(
    modifier: GlanceModifier,
    status: WifiStatus,
    colors: WidgetColors,
    fontSize: FontSize
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(actionStartActivity(wifiSettingsIntent())),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
            Image(
                provider = ImageProvider(status.iconRes),
                contentDescription = null,
                modifier = GlanceModifier.size(32.dp),
                colorFilter = ColorFilter.tint(colors.secondary.toColorProvider())
            )
            Spacer(GlanceModifier.height(8.dp))
            Text(
                text = context.getString(
                    if (status == WifiStatus.Disabled) CommonR.string.wifi_disabled else CommonR.string.no_wifi_connection
                ),
                style = TextStyle(
                    color = colors.secondary.toColorProvider(),
                    fontSize = fontSize.value.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}
