package com.w2sv.widget.ui.container

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.w2sv.common.utils.activityPendingIntent
import com.w2sv.common.utils.broadcastPendingIntent
import com.w2sv.common.utils.openWifiSettingsIntent
import com.w2sv.widget.WifiWidgetProvider

internal object WidgetActionPendingIntent {

    fun openWifiSettings(context: Context): PendingIntent =
        activityPendingIntent(context, openWifiSettingsIntent, PendingIntent.FLAG_IMMUTABLE)

    fun refreshWidget(context: Context): PendingIntent =
        broadcastPendingIntent(
            context,
            WifiWidgetProvider.refreshIntent(context),
            PendingIntent.FLAG_IMMUTABLE
        )

    fun openWidgetConfigScreen(context: Context): PendingIntent =
        activityPendingIntent(
            context,
            Intent().setComponent(
                ComponentName(
                    context,
                    "com.w2sv.wifiwidget.ui.WidgetConfigActivity"
                )
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
            PendingIntent.FLAG_IMMUTABLE
        )
}
