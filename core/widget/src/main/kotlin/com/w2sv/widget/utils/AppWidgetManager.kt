package com.w2sv.widget.utils

import android.appwidget.AppWidgetManager
import android.content.Context
import com.w2sv.androidutils.content.componentName
import com.w2sv.widget.WidgetPinSuccessBroadcastReceiver
import com.w2sv.widget.WifiWidgetProvider

/**
 * Calls [AppWidgetManager.requestPinAppWidget] if [AppWidgetManager.isRequestPinAppWidgetSupported] returns true.
 * Otherwise invokes [onFailure].
 */
fun AppWidgetManager.attemptWifiWidgetPin(context: Context, onFailure: () -> Unit) {
    if (isRequestPinAppWidgetSupported) {
        requestPinAppWidget(
            componentName<WifiWidgetProvider>(context),
            null,
            WidgetPinSuccessBroadcastReceiver.pendingIntent(context)
        )
    } else {
        onFailure()
    }
}
