package com.w2sv.widget.actions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.w2sv.common.AppAction
import com.w2sv.common.utils.openWifiSettingsIntent

internal fun wifiSettingsIntent(): Intent =
    openWifiSettingsIntent

internal fun widgetSettingsIntent(context: Context): Intent =
    Intent.makeRestartActivityTask(
        ComponentName(
            context,
            "com.w2sv.wifiwidget.MainActivity"
        )
    ).setAction(AppAction.OPEN_WIDGET_CONFIGURATION_SCREEN)
