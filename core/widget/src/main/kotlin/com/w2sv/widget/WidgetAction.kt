package com.w2sv.widget

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.w2sv.common.AppAction
import com.w2sv.common.utils.activityPendingIntent
import com.w2sv.common.utils.openWifiSettingsIntent

internal object WidgetAction {

    fun openWifiSettings(context: Context): PendingIntent =
        activityPendingIntent(context, openWifiSettingsIntent, PendingIntent.FLAG_IMMUTABLE)

    fun refreshWidget(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            1447,
            WifiWidgetProvider.refreshDataIntent(context),
            PendingIntent.FLAG_IMMUTABLE
        )

    fun openWidgetConfigScreen(context: Context): PendingIntent =
        activityPendingIntent(
            context,
            Intent.makeRestartActivityTask(
                ComponentName(
                    context,
                    "com.w2sv.wifiwidget.MainActivity"
                )
            )
                .setAction(AppAction.OPEN_WIDGET_CONFIGURATION_SCREEN),
            PendingIntent.FLAG_IMMUTABLE
        )
}
