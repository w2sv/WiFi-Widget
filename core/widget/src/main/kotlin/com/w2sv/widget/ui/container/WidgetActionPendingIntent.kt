package com.w2sv.widget.ui.container

import android.app.PendingIntent
import android.content.Context
import com.w2sv.common.android.SettingsIntents
import com.w2sv.common.android.activityPendingIntent
import com.w2sv.common.android.broadcastPendingIntent
import com.w2sv.widget.WifiWidgetProvider

internal object WidgetActionPendingIntent {

    fun openWifiSettings(context: Context): PendingIntent =
        activityPendingIntent(context, SettingsIntents.wifi, PendingIntent.FLAG_IMMUTABLE)

    fun refreshWidget(context: Context): PendingIntent =
        broadcastPendingIntent(
            context,
            WifiWidgetProvider.refreshIntent(context),
            PendingIntent.FLAG_IMMUTABLE
        )
}
