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
                if (context.wifiEnabled()){
                    setTextViewText(
                        R.id.ip_tv,
                        SpannableStringBuilder()
                            .italic {
                                color(context.resources.getColor(R.color.purple_500, context.theme)) {
                                    append(("IP Address: "))
                                }
                            }
                            .append(context.ipAddress())
                    )
                    setViewVisibility(R.id.ip_tv, View.VISIBLE)
                    setViewVisibility(R.id.no_wifi_connection_tv, View.INVISIBLE)
                }
                else{
                    setViewVisibility(R.id.ip_tv, View.INVISIBLE)
                    setViewVisibility(R.id.no_wifi_connection_tv, View.VISIBLE)
                }
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