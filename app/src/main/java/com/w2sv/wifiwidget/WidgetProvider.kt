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
import androidx.annotation.StringRes
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
                    populatePropertiesLayout(this@updateWidget, wifiManager)

                    setViewVisibility(R.id.property_layout, View.VISIBLE)
                    setViewVisibility(R.id.no_wifi_connection_tv, View.GONE)
                } else {
                    setViewVisibility(R.id.property_layout, View.GONE)
                    setViewVisibility(R.id.no_wifi_connection_tv, View.VISIBLE)
                }

                // set last_updated_tv
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

/**
 * dhcpInfo: 'ipaddr 192.168.1.233 gateway 192.168.1.1 netmask 0.0.0.0 dns1 192.168.1.1 dns2 0.0.0.0 DHCP server 192.168.1.1 lease 28800 seconds'
 * connectionInfo: 'connection info: SSID: , BSSID: 02:00:00:00:00:00, MAC: 02:00:00:00:00:00, Supplicant state: COMPLETED, Wi-Fi standard: 5, RSSI: -47, Link speed: 433Mbps, Tx Link speed: 433Mbps, Max Supported Tx Link speed: 433Mbps, Rx Link speed: -1Mbps, Max Supported Rx Link speed: 433Mbps, Frequency: 5180MHz, Net ID: -1, Metered hint: false, score: 60'
 */
@Suppress("DEPRECATION")
private fun RemoteViews.populatePropertiesLayout(context: Context, wifiManager: WifiManager){
    arrayOf(
        Triple(R.id.ssid_tv, R.string.ssid) { wifiManager.connectionInfo.ssid.replace("\"", "") },
        Triple(R.id.logged_in_tv, R.string.logged_in) {""},
        Triple(R.id.ip_tv, R.string.ipv4) {wifiManager.connectionInfo.ipAddress.asFormattedIpAddress()},
        Triple(R.id.frequency_tv, R.string.frequency) {"${wifiManager.connectionInfo.frequency}Hz"},
        Triple(R.id.gateway_tv, R.string.gateway) {wifiManager.dhcpInfo.gateway.asFormattedIpAddress()},
        Triple(R.id.subnet_mask_tv, R.string.subnet_mask) {wifiManager.dhcpInfo.netmask.asFormattedIpAddress()},
        Triple(R.id.dns_tv, R.string.dns) {wifiManager.dhcpInfo.dns1.asFormattedIpAddress()},
        Triple(R.id.dhcp_tv, R.string.dhcp) {wifiManager.dhcpInfo.serverAddress.asFormattedIpAddress()},
    )
        .forEach {
            setTextViewText(it.first, context.propertyRow(it.second, it.third()))
        }
}

private fun Context.propertyRow(@StringRes propertyStringId: Int, value: String): SpannableStringBuilder =
    SpannableStringBuilder()
        .italic {
            color(getColor(R.color.mischka)) {
                append((getString(propertyStringId)))
            }
        }
        .append(value)

@Suppress("DEPRECATION")
private fun Int.asFormattedIpAddress(): String =
    Formatter.formatIpAddress(this)
