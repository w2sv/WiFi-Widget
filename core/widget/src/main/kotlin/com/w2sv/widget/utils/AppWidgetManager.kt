package com.w2sv.widget.utils

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import com.w2sv.androidutils.appwidget.getAppWidgetIds
import com.w2sv.androidutils.content.componentName
import com.w2sv.androidutils.content.intent
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
            componentName<WifiWidgetProvider>(context),
            null,
            PendingIntent.getBroadcast(
                context,
                WidgetPinSuccessBroadcastReceiver.REQUEST_CODE,
                intent<WidgetPinSuccessBroadcastReceiver>(context),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
    } else {
        onFailure()
    }
}
