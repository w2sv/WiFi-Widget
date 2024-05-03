package com.w2sv.widget.ui

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.IdRes
import com.w2sv.androidutils.appwidgets.crossVisualize
import com.w2sv.androidutils.appwidgets.setBackgroundColor
import com.w2sv.androidutils.appwidgets.setColorFilter
import com.w2sv.androidutils.ui.getAlphaSetColor
import com.w2sv.common.constants.Extra
import com.w2sv.core.widget.R
import com.w2sv.domain.model.WifiStatus
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.networking.WifiStatusGetter
import com.w2sv.widget.PendingIntentCode
import com.w2sv.widget.WidgetProvider
import com.w2sv.widget.WifiPropertyViewsService
import com.w2sv.widget.data.appearanceBlocking
import com.w2sv.widget.model.WidgetBottomBarElement
import com.w2sv.widget.utils.goToWifiSettingsPendingIntent
import com.w2sv.widget.utils.setTextView
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class WidgetLayoutPopulator @Inject constructor(
    widgetRepository: WidgetRepository,
    @ApplicationContext private val context: Context,
    private val appWidgetManager: AppWidgetManager,
    private val wifiStatusGetter: WifiStatusGetter
) {
    private val appearance = widgetRepository.appearanceBlocking
    private val colors = appearance.getColors(context)

    fun populate(widget: RemoteViews, appWidgetId: Int): RemoteViews =
        widget
            .apply {
                setContentLayout(
                    appWidgetId = appWidgetId,
                )
                setBackgroundColor(
                    id = R.id.widget_layout,
                    color = getAlphaSetColor(colors.background, appearance.backgroundOpacity),
                )
                setBottomBar(bottomBar = appearance.bottomRow)
            }

    private fun RemoteViews.setContentLayout(appWidgetId: Int) {
        when (val wifiStatus = wifiStatusGetter()) {
            WifiStatus.Connected -> {
                crossVisualize(
                    R.id.no_connection_available_layout,
                    R.id.wifi_property_list_view,
                )

                setRemoteAdapter(
                    R.id.wifi_property_list_view,
                    Intent(context, WifiPropertyViewsService::class.java)
                        .apply {
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
                        },
                )

                appWidgetManager
                    .notifyAppWidgetViewDataChanged(appWidgetId, R.id.wifi_property_list_view)
            }

            else -> {
                crossVisualize(
                    R.id.wifi_property_list_view,
                    R.id.no_connection_available_layout,
                )

                setTextView(
                    viewId = R.id.wifi_status_tv,
                    text = context.getString(
                        if (wifiStatus == WifiStatus.Disabled) {
                            R.string.wifi_disabled
                        } else {
                            R.string.no_wifi_connection
                        },
                    ),
                    color = colors.secondary,
                )

                setOnClickPendingIntent(
                    R.id.no_connection_available_layout,
                    goToWifiSettingsPendingIntent(context)
                )
            }
        }
    }

    // ============
    // Bottom Row
    // ============

    private fun RemoteViews.setBottomBar(bottomBar: WidgetBottomBarElement) {
        if (!bottomBar.anyEnabled) {
            setViewVisibility(R.id.bottom_row, View.GONE)
        } else {
            setViewVisibility(R.id.bottom_row, View.VISIBLE)

            if (bottomBar.lastRefreshTimeDisplay) {
                setViewVisibility(R.id.last_updated_tv, View.VISIBLE)
                setTextColor(R.id.last_updated_tv, colors.secondary)

                val now = Date()
                setTextView(
                    viewId = R.id.last_updated_tv,
                    text = "${
                        DateFormat.getTimeInstance(DateFormat.SHORT).format(now)
                    } ${SimpleDateFormat("EE", Locale.getDefault()).format(now)}",
                    size = appearance.fontSize.value
                )
            } else {
                setViewVisibility(R.id.last_updated_tv, View.INVISIBLE)
            }

            setButton(
                id = R.id.refresh_button,
                show = bottomBar.refreshButton,
                pendingIntent = PendingIntent.getBroadcast(
                    context,
                    PendingIntentCode.RefreshWidgetData.ordinal,
                    WidgetProvider.getRefreshDataIntent(context),
                    PendingIntent.FLAG_IMMUTABLE,
                ),
            )
            setButton(
                id = R.id.go_to_wifi_settings_button,
                show = bottomBar.goToWifiSettingsButton,
                pendingIntent = goToWifiSettingsPendingIntent(context),
            )
            setButton(
                id = R.id.go_to_widget_settings_button,
                show = bottomBar.goToWidgetSettingsButton,
                pendingIntent = PendingIntent.getActivity(
                    context,
                    PendingIntentCode.LaunchHomeActivity.ordinal,
                    Intent.makeRestartActivityTask(
                        ComponentName(
                            context,
                            "com.w2sv.wifiwidget.MainActivity",
                        ),
                    )
                        .putExtra(
                            Extra.INVOKE_WIDGET_CONFIGURATION_SCREEN,
                            true,
                        ),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                ),
            )
        }
    }

    private fun RemoteViews.setButton(@IdRes id: Int, show: Boolean, pendingIntent: PendingIntent) {
        setViewVisibility(
            id,
            if (show) View.VISIBLE else View.GONE,
        )

        if (show) {
            setColorFilter(id, colors.primary)
        }

        setOnClickPendingIntent(id, pendingIntent)
    }
}
