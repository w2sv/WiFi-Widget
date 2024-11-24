package com.w2sv.widget.utils

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.w2sv.androidutils.appwidget.getAppWidgetIds
import com.w2sv.widget.WidgetPinSuccessBroadcastReceiver
import com.w2sv.widget.WifiWidgetProvider

fun AppWidgetManager.getWifiWidgetIds(context: Context): IntArray =
    getWifiWidgetIds(context.packageName)

fun AppWidgetManager.getWifiWidgetIds(packageName: String): IntArray =
    getAppWidgetIds(packageName, WifiWidgetProvider::class.java)

/**
 * Calls [AppWidgetManager.requestPinAppWidget] if [android.appwidget.AppWidgetManager.isRequestPinAppWidgetSupported] returns true.
 * Otherwise invokes [onFailure].
 */
fun AppWidgetManager.attemptWifiWidgetPin(context: Context, onFailure: () -> Unit) {
    if (isRequestPinAppWidgetSupported) {
        requestPinAppWidget(
            ComponentName(context.packageName, WifiWidgetProvider::class.java.name),
            null,
            PendingIntent.getBroadcast(
                context,
                WidgetPinSuccessBroadcastReceiver.REQUEST_CODE,
                Intent(context, WidgetPinSuccessBroadcastReceiver::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
    } else {
        onFailure()
    }
}
