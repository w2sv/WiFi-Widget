package com.w2sv.widget.utils

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import com.w2sv.androidutils.appwidgets.getAppWidgetIds
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.widget.WidgetProvider

val Context.appWidgetManager: AppWidgetManager
    get() =
        AppWidgetManager
            .getInstance(this)

fun getWifiWidgetIds(context: Context): IntArray =
    context.appWidgetManager.getWifiWidgetIds(context)

fun AppWidgetManager.getWifiWidgetIds(context: Context): IntArray =
    getAppWidgetIds(context, WidgetProvider::class.java)

fun AppWidgetManager.getWifiWidgetIds(packageName: String): IntArray =
    getAppWidgetIds(packageName, WidgetProvider::class.java)

fun attemptWifiWidgetPin(context: Context) {
    context.appWidgetManager.attemptWifiWidgetPin(context)
}

fun AppWidgetManager.attemptWifiWidgetPin(context: Context) {
    if (isRequestPinAppWidgetSupported) {
        requestPinAppWidget(
            ComponentName(
                context,
                WidgetProvider::class.java
            ),
            null,
            null
        )
    } else {
        context.showToast(com.w2sv.common.R.string.widget_pinning_not_supported_by_your_device_launcher)
    }
}