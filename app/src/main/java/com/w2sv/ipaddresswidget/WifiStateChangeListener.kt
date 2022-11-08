package com.w2sv.ipaddresswidget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import slimber.log.i

class WifiStateChangeListener : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        i{"onReceive"}

        context.sendBroadcast(
            Intent(context, IPAddressWidget::class.java)
                .apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(
                        AppWidgetManager.EXTRA_APPWIDGET_IDS,
                        AppWidgetManager.getInstance(context.applicationContext)
                            .getAppWidgetIds(ComponentName(context.applicationContext, IPAddressWidget::class.java))
                    )
                }
        )
    }
}