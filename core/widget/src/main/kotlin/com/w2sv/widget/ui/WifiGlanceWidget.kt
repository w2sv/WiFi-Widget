package com.w2sv.widget.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.w2sv.core.common.R as CommonR
import com.w2sv.domain.model.networking.WifiStatus
import com.w2sv.domain.model.widget.FontSize
import com.w2sv.domain.model.widget.WidgetColors
import com.w2sv.domain.model.widget.WidgetUtility
import com.w2sv.domain.model.widget.WifiPropertyValueAlignment
import com.w2sv.domain.model.wifiproperty.viewdata.SubscriptableText
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewData
import com.w2sv.widget.actions.RefreshWidgetAction
import com.w2sv.widget.di.GlanceWidgetEntryPoint
import dagger.hilt.android.EntryPointAccessors
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

internal object WifiGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override val previewSizeMode = SizeMode.Responsive(PREVIEW_SIZES)

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val contentProvider = EntryPointAccessors.fromApplication(
            context,
            GlanceWidgetEntryPoint::class.java
        ).contentProvider()
        val content = contentProvider(context)

        provideContent {
            WifiWidget(content)
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent {
            WifiWidget(previewWifiWidgetState())
        }
    }
}

@Composable
internal fun WifiWidget(content: WifiWidgetState) {
    val colors = content.colors
    val fontSize = content.config.appearance.fontSize

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(content.backgroundColor))
            .cornerRadius(16.dp)
            .padding(horizontal = 10.dp)
    ) {
        if (content.status.isConnected) {
            WifiPropertyList(
                modifier = GlanceModifier.defaultWeight(),
                properties = content.properties,
                alignment = content.config.appearance.propertyValueAlignment,
                colors = colors,
                fontSize = fontSize
            )
        } else {
            DisconnectedContent(
                modifier = GlanceModifier.defaultWeight(),
                status = content.status,
                colors = colors,
                fontSize = fontSize
            )
        }

        WidgetUtilityRow(content)
    }
}

@Composable
private fun WifiPropertyList(
    modifier: GlanceModifier,
    properties: List<WifiPropertyViewData>,
    alignment: WifiPropertyValueAlignment,
    colors: WidgetColors,
    fontSize: FontSize
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        items(
            items = properties,
            itemId = { it.hashCode().toLong() }
        ) { property ->
            WifiPropertyRow(
                property = property,
                alignment = alignment,
                colors = colors,
                fontSize = fontSize
            )
        }
    }
}

@Composable
private fun WifiPropertyRow(
    property: WifiPropertyViewData,
    alignment: WifiPropertyValueAlignment,
    colors: WidgetColors,
    fontSize: FontSize
) {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable(
                actionStartActivity(
                    CopyPropertyToClipboardActivity.intent(
                        context = context,
                        propertyLabel = property.label.text,
                        propertyValue = property.value
                    )
                )
            )
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            PropertyLabel(
                label = property.label,
                color = colors.primary,
                fontSize = fontSize,
                modifier = if (alignment == WifiPropertyValueAlignment.Left) {
                    GlanceModifier.width(100.dp)
                } else {
                    GlanceModifier
                }
            )

            if (alignment == WifiPropertyValueAlignment.Right) {
                Spacer(GlanceModifier.defaultWeight())
            }

            Text(
                text = property.value,
                modifier = if (alignment == WifiPropertyValueAlignment.Left) {
                    GlanceModifier.defaultWeight()
                } else {
                    GlanceModifier
                },
                style = TextStyle(
                    color = colorProvider(colors.secondary),
                    fontSize = fontSize.value.sp,
                    textAlign = if (alignment == WifiPropertyValueAlignment.Right) TextAlign.End else TextAlign.Start
                ),
                maxLines = 3
            )
        }

        if (property.subValues.isNotEmpty()) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Horizontal.End
            ) {
                property.subValues.forEachIndexed { index, value ->
                    if (index > 0) Spacer(GlanceModifier.width(6.dp))
                    Text(
                        text = value,
                        modifier = GlanceModifier
                            .background(colorProvider(colors.subPropertyBackground))
                            .cornerRadius(4.dp)
                            .padding(horizontal = 4.dp),
                        style = TextStyle(
                            color = colorProvider(colors.secondary),
                            fontSize = fontSize.subscriptSize.sp
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun PropertyLabel(
    label: SubscriptableText,
    color: Int,
    fontSize: FontSize,
    modifier: GlanceModifier
) {
    Row(
        modifier = modifier.padding(end = 6.dp),
        verticalAlignment = Alignment.Vertical.Bottom
    ) {
        Text(
            text = label.text,
            style = TextStyle(
                color = colorProvider(color),
                fontSize = fontSize.value.sp
            )
        )
        label.subscript?.let {
            Text(
                text = it,
                style = TextStyle(
                    color = colorProvider(color),
                    fontSize = fontSize.subscriptSize.sp
                )
            )
        }
    }
}

@Composable
private fun DisconnectedContent(
    modifier: GlanceModifier,
    status: WifiStatus,
    colors: WidgetColors,
    fontSize: FontSize
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(actionStartActivity(WidgetIntents.openWifiSettings())),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
            Image(
                provider = ImageProvider(status.iconRes),
                contentDescription = null,
                modifier = GlanceModifier.size(32.dp),
                colorFilter = ColorFilter.tint(colorProvider(colors.secondary))
            )
            Spacer(GlanceModifier.height(8.dp))
            Text(
                text = context.getString(
                    if (status == WifiStatus.Disabled) CommonR.string.wifi_disabled else CommonR.string.no_wifi_connection
                ),
                style = TextStyle(
                    color = colorProvider(colors.secondary),
                    fontSize = fontSize.value.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

@Composable
private fun WidgetUtilityRow(content: WifiWidgetState) {
    val context = LocalContext.current
    val config = content.config
    val enabledUtilities = config.enabledUtilities()
    if (enabledUtilities.isEmpty()) return

    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        if (WidgetUtility.RefreshTimeDisplay in enabledUtilities) {
            Text(
                text = formattedDateTime(content.renderedAt),
                style = TextStyle(
                    color = colorProvider(content.colors.secondary),
                    fontSize = config.appearance.fontSize.value.sp
                )
            )
        }

        Spacer(GlanceModifier.defaultWeight())

        if (WidgetUtility.RefreshButton in enabledUtilities) {
            UtilityButton(
                iconRes = CommonR.drawable.ic_refresh_24,
                contentDescription = context.getString(CommonR.string.refresh_data),
                tint = content.colors.primary,
                action = WidgetUtilityAction.Refresh
            )
        }
        if (WidgetUtility.GoToWifiSettingsButton in enabledUtilities) {
            UtilityButton(
                iconRes = CommonR.drawable.ic_wifi_settings_24,
                contentDescription = context.getString(CommonR.string.open_wifi_settings_button),
                tint = content.colors.primary,
                action = WidgetUtilityAction.OpenWifiSettings
            )
        }
        if (WidgetUtility.GoToWidgetSettingsButton in enabledUtilities) {
            UtilityButton(
                iconRes = CommonR.drawable.ic_settings_24,
                contentDescription = context.getString(CommonR.string.open_widget_settings_button),
                tint = content.colors.primary,
                action = WidgetUtilityAction.OpenWidgetSettings
            )
        }
    }
}

private enum class WidgetUtilityAction {
    Refresh,
    OpenWifiSettings,
    OpenWidgetSettings
}

@Composable
private fun UtilityButton(
    iconRes: Int,
    contentDescription: String,
    tint: Int,
    action: WidgetUtilityAction
) {
    val context = LocalContext.current
    val modifier = GlanceModifier
        .size(48.dp)
        .padding(8.dp)
        .let {
            when (action) {
                WidgetUtilityAction.Refresh -> it.clickable(actionRunCallback<RefreshWidgetAction>())
                WidgetUtilityAction.OpenWifiSettings -> it.clickable(actionStartActivity(WidgetIntents.openWifiSettings()))
                WidgetUtilityAction.OpenWidgetSettings -> it.clickable(
                    actionStartActivity(WidgetIntents.openWidgetSettings(context))
                )
            }
        }

    Image(
        provider = ImageProvider(iconRes),
        contentDescription = contentDescription,
        modifier = modifier,
        colorFilter = ColorFilter.tint(colorProvider(tint))
    )
}

private fun colorProvider(color: Int): ColorProvider =
    ColorProvider(Color(color))

private fun formattedDateTime(instant: Instant): String {
    val date = Date.from(instant)
    val time = DateFormat.getTimeInstance(DateFormat.SHORT).format(date)
    val day = SimpleDateFormat("EE", Locale.getDefault()).format(date)
    return "$time $day"
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 160, heightDp = 100)
@Preview(widthDp = 250, heightDp = 140)
@Preview(widthDp = 320, heightDp = 220)
@Composable
private fun WifiWidgetPreview() {
    WifiWidget(previewWifiWidgetState())
}

private val PREVIEW_SIZES = setOf(
    DpSize(160.dp, 100.dp),
    DpSize(250.dp, 140.dp),
    DpSize(320.dp, 220.dp)
)
