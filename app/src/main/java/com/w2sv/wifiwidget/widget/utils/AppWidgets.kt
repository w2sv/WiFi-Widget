package com.w2sv.wifiwidget.widget.utils

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.w2sv.wifiwidget.widget.WifiWidgetProvider

fun Context.anyAppWidgetInUse(): Boolean =
    AppWidgetManager.getInstance(this).getWifiWidgetIds(this)
        .isNotEmpty()

fun AppWidgetManager.getWifiWidgetIds(context: Context): IntArray =
    getAppWidgetIds(
        ComponentName(
            context.packageName,
            WifiWidgetProvider::class.java.name
        )
    )