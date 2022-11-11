package com.w2sv.wifiwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.text.SpannableStringBuilder
import android.text.format.Formatter
import android.view.View
import android.widget.RemoteViews
import androidx.core.text.color
import androidx.core.text.italic
import slimber.log.i
import java.text.DateFormat
import java.util.Date

class WidgetProvider : AppWidgetProvider() {

    companion object {
        fun restartPendingIntent(context: Context): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                69,
                Intent(context, AppWidgetProvider::class.java)
                    .setAction(ACTION_RESTART),
                PendingIntent.FLAG_IMMUTABLE
            )

        private const val ACTION_RESTART = "com.w2sv.wifiwidget.ACTION_RESTART"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        i { "onReceive" }

        if (intent?.action == ACTION_RESTART)
            with(context!!) {
                i { "onReceive.ACTION_RESTART" }

                val appWidgetManager = AppWidgetManager.getInstance(this)

                updateWidgets(
                    appWidgetManager,
                    appWidgetManager.getAppWidgetIds(
                        ComponentName(
                            packageName,
                            this@WidgetProvider::class.java.name
                        )
                    )
                )
            }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        context.updateWidgets(appWidgetManager, appWidgetIds)
    }
}

private fun Context.updateWidgets(
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray
) {
    for (appWidgetId in appWidgetIds) {
        updateWidget(appWidgetManager, appWidgetId)
    }
}

private fun Context.updateWidget(
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    i { "updateWidget" }

    appWidgetManager.updateAppWidget(
        appWidgetId,
        RemoteViews(packageName, R.layout.widget)
            .apply {
                // set onClickListener
                setOnClickPendingIntent(
                    R.id.widget_layout,
                    WidgetProvider.restartPendingIntent(this@updateWidget)
                )

                val wifiManager = getSystemService(WifiManager::class.java)

                if (wifiManager.isWifiEnabled) {
                    setTextViewText(
                        R.id.ip_tv,
                        SpannableStringBuilder()
                            .italic {
                                color(getColor(R.color.mischka)) {
                                    append((getString(R.string.ip_address)))
                                }
                            }
                            .append(wifiManager.ipAddress())
                    )
                    setViewVisibility(R.id.ip_tv, View.VISIBLE)
                    setViewVisibility(R.id.no_wifi_connection_tv, View.INVISIBLE)
                } else {
                    setViewVisibility(R.id.ip_tv, View.INVISIBLE)
                    setViewVisibility(R.id.no_wifi_connection_tv, View.VISIBLE)
                }

                setTextViewText(
                    R.id.last_updated_tv,
                    getString(
                        R.string.updated,
                        DateFormat.getTimeInstance(DateFormat.SHORT).format(Date())
                    )
                )
            }
    )
}

@Suppress("DEPRECATION")
fun WifiManager.ipAddress(): String =
    Formatter
        .formatIpAddress(
            connectionInfo
                .ipAddress
        )