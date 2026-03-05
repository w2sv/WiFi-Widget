package com.w2sv.common.utils

import android.content.Intent
import android.provider.Settings

val openWifiSettingsIntent = Intent(Settings.ACTION_WIFI_SETTINGS)
    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

