package com.w2sv.ipaddresswidget

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

class IPAddressWidgetProvider : AppWidgetProvider() {

    companion object {
        fun restartPendingIntent(context: Context): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                69,
                Intent(context, IPAddressWidgetProvider::class.java)
                    .setAction(ACTION_RESTART),
                PendingIntent.FLAG_IMMUTABLE
            )

        private const val ACTION_RESTART = "com.w2sb.ipaddresswidget.ACTION_RESTART"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        i { "onReceive" }

        if (intent?.action == ACTION_RESTART)
            with(context!!) {
                i { "onReceive.ACTION_RESTART" }

                val appWidgetManager = AppWidgetManager.getInstance(this)

                updateAppWidgets(
                    applicationContext,
                    appWidgetManager,
                    appWidgetManager.getAppWidgetIds(
                        ComponentName(
                            packageName,
                            this@IPAddressWidgetProvider::class.java.name
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
        updateAppWidgets(context, appWidgetManager, appWidgetIds)
    }

    private fun updateAppWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    i { "updateAppWidget" }

    appWidgetManager.updateAppWidget(
        appWidgetId,
        RemoteViews(context.packageName, R.layout.widget_ipaddress)
            .apply {
                // set onClickListener
                setOnClickPendingIntent(
                    R.id.widget_layout,
                    IPAddressWidgetProvider.restartPendingIntent(context)
                )

                if (context.wifiEnabled()) {
                    setTextViewText(
                        R.id.ip_tv,
                        SpannableStringBuilder()
                            .italic {
                                color(context.getColor(R.color.purple_500)) {
                                    append((context.getString(R.string.ip_address)))
                                }
                            }
                            .append(context.ipAddress())
                    )
                    setViewVisibility(R.id.ip_tv, View.VISIBLE)
                    setViewVisibility(R.id.no_wifi_connection_tv, View.INVISIBLE)
                } else {
                    setViewVisibility(R.id.ip_tv, View.INVISIBLE)
                    setViewVisibility(R.id.no_wifi_connection_tv, View.VISIBLE)
                }

                setTextViewText(
                    R.id.last_updated_tv,
                    context.getString(
                        R.string.updated,
                        DateFormat.getTimeInstance(DateFormat.SHORT).format(Date())
                    )
                )
            }
    )
}

@Suppress("DEPRECATION")
fun Context.ipAddress(): String =
    Formatter
        .formatIpAddress(
            getSystemService(WifiManager::class.java)
                .connectionInfo
                .ipAddress
        )

fun Context.wifiEnabled(): Boolean =
    getSystemService(WifiManager::class.java)
        .isWifiEnabled