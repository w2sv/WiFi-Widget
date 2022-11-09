package com.w2sv.ipaddresswidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
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

class IPAddressWidget : AppWidgetProvider() {
    override fun onUpdate(
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
    i { "Called updateAppWidget" }

    appWidgetManager.updateAppWidget(
        appWidgetId,
        RemoteViews(context.packageName, R.layout.widget_ipaddress)
            .apply {
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
                        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                            .format(Date())
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