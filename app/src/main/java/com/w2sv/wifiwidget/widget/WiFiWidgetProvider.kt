package com.w2sv.wifiwidget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.w2sv.wifiwidget.R
import slimber.log.i
import java.text.DateFormat
import java.util.Date

class WiFiWidgetProvider : AppWidgetProvider() {

    companion object {
        fun restartPendingIntent(context: Context): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                69,
                Intent(context, WiFiWidgetProvider::class.java)
                    .setAction(ACTION_REFRESH_DATA),
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )

        private const val ACTION_REFRESH_DATA = "com.w2sv.wifiwidget.ACTION_REFRESH_DATA"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        i { "onReceive" }

        if (intent?.action == ACTION_REFRESH_DATA) {
            i { "onReceive.ACTION_REFRESH_DATA" }

            val appWidgetManager = AppWidgetManager.getInstance(context!!)

            appWidgetManager.updateWidgets(
                appWidgetManager.getAppWidgetIds(
                    ComponentName(
                        context.packageName,
                        this::class.java.name
                    )
                ),
                context
            )
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetManager.updateWidgets(appWidgetIds, context)
    }
}

private fun AppWidgetManager.updateWidgets(
    appWidgetIds: IntArray,
    context: Context
) {
    for (appWidgetId in appWidgetIds) {
        updateWidget(appWidgetId, context)
    }
}

private fun AppWidgetManager.updateWidget(
    appWidgetId: Int,
    context: Context
) {
    i { "updateWidget" }

    updateAppWidget(
        appWidgetId,
        RemoteViews(context.packageName, R.layout.widget)
            .apply {
                setWifiDependentContent(context)

                // set last_updated_tv text
                setTextViewText(
                    R.id.last_updated_tv,
                    DateFormat.getTimeInstance(DateFormat.SHORT).format(Date())
                )

                // set refresh_button onClickListener
                setOnClickPendingIntent(
                    R.id.refresh_button,
                    WiFiWidgetProvider.restartPendingIntent(context)
                )
            }
    )
}