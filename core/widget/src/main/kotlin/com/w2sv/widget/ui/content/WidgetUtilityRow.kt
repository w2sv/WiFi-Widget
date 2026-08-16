package com.w2sv.widget.ui.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.w2sv.core.common.R as CommonR
import com.w2sv.domain.model.widget.WidgetUtility
import com.w2sv.widget.actions.RefreshWidgetAction
import com.w2sv.widget.actions.widgetSettingsIntent
import com.w2sv.widget.actions.wifiSettingsIntent
import com.w2sv.widget.ui.model.WifiWidgetState
import com.w2sv.widget.ui.theme.toColorProvider
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

@Composable
internal fun WidgetUtilityRow(state: WifiWidgetState) {
    val context = LocalContext.current
    val config = state.config
    val enabledUtilities = config.enabledUtilities()
    if (enabledUtilities.isEmpty()) return

    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        if (WidgetUtility.RefreshTimeDisplay in enabledUtilities) {
            Text(
                text = formattedDateTime(state.renderedAt),
                style = TextStyle(
                    color = state.colors.secondary.toColorProvider(),
                    fontSize = config.appearance.fontSize.value.sp
                )
            )
        }

        Spacer(GlanceModifier.defaultWeight())

        if (WidgetUtility.RefreshButton in enabledUtilities) {
            UtilityButton(
                iconRes = CommonR.drawable.ic_refresh_24,
                contentDescription = context.getString(CommonR.string.refresh_data),
                tint = state.colors.primary,
                action = actionRunCallback<RefreshWidgetAction>()
            )
        }
        if (WidgetUtility.GoToWifiSettingsButton in enabledUtilities) {
            UtilityButton(
                iconRes = CommonR.drawable.ic_wifi_settings_24,
                contentDescription = context.getString(CommonR.string.open_wifi_settings_button),
                tint = state.colors.primary,
                action = actionStartActivity(wifiSettingsIntent())
            )
        }
        if (WidgetUtility.GoToWidgetSettingsButton in enabledUtilities) {
            UtilityButton(
                iconRes = CommonR.drawable.ic_settings_24,
                contentDescription = context.getString(CommonR.string.open_widget_settings_button),
                tint = state.colors.primary,
                action = actionStartActivity(widgetSettingsIntent(context))
            )
        }
    }
}

@Composable
private fun UtilityButton(
    iconRes: Int,
    contentDescription: String,
    tint: Int,
    action: Action
) {
    Image(
        provider = ImageProvider(iconRes),
        contentDescription = contentDescription,
        modifier = GlanceModifier
            .size(48.dp)
            .padding(8.dp)
            .clickable(action),
        colorFilter = ColorFilter.tint(tint.toColorProvider())
    )
}

private fun formattedDateTime(instant: Instant): String {
    val date = Date.from(instant)
    val time = DateFormat.getTimeInstance(DateFormat.SHORT).format(date)
    val day = SimpleDateFormat("EE", Locale.getDefault()).format(date)
    return "$time $day"
}
