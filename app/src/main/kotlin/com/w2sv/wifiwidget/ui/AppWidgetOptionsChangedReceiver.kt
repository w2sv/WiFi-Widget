package com.w2sv.wifiwidget.ui

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.w2sv.androidutils.lifecycle.SelfManagingLocalBroadcastReceiver

class AppWidgetOptionsChangedReceiver(
    context: Context,
    callback: (Context?, Intent?) -> Unit,
) : SelfManagingLocalBroadcastReceiver.Impl(
    context,
    IntentFilter(AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED),
    callback,
)
