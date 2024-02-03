package com.w2sv.widget.utils

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.w2sv.androidutils.appwidgets.getAppWidgetIds
import com.w2sv.widget.WidgetProvider

fun AppWidgetManager.getWifiWidgetIds(context: Context): IntArray =
    getWifiWidgetIds(context.packageName)

fun AppWidgetManager.getWifiWidgetIds(packageName: String): IntArray =
    getAppWidgetIds(packageName, WidgetProvider::class.java)

fun AppWidgetManager.attemptWifiWidgetPin(packageName: String, onFailure: () -> Unit) {
    if (isRequestPinAppWidgetSupported) {
        requestPinAppWidget(
            ComponentName(packageName, WidgetProvider::class.java.name),
            null,
            null
        )
    } else {
        onFailure()
    }
}
