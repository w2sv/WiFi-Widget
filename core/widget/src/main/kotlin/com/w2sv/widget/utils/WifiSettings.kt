package com.w2sv.widget.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.w2sv.widget.PendingIntentCode

val goToWifiSettingsIntent = Intent(Settings.ACTION_WIFI_SETTINGS)
    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

internal fun goToWifiSettingsPendingIntent(context: Context) =
    PendingIntent.getActivity(
        context,
        PendingIntentCode.GoToWifiSettings.ordinal,
        goToWifiSettingsIntent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
