package com.w2sv.wifiwidget.widget.utils

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context

fun AppWidgetManager.getAppWidgetIds(
    context: Context,
    appWidgetProviderClass: Class<out AppWidgetProvider>
): IntArray =
    getAppWidgetIds(
        ComponentName(
            context.packageName,
            appWidgetProviderClass.name
        )
    )