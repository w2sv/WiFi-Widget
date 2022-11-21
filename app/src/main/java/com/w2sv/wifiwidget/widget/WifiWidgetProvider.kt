package com.w2sv.wifiwidget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.preferences.WidgetPreferences
import com.w2sv.wifiwidget.widget.utils.getWifiWidgetIds
import dagger.hilt.android.AndroidEntryPoint
import slimber.log.i
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class WifiWidgetProvider : AppWidgetProvider() {

    companion object {
        fun refreshDataPendingIntent(context: Context): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                69,
                refreshDataIntent(context),
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )

        fun refreshData(context: Context) {
            with(context) {
                sendBroadcast(refreshDataIntent(this))
            }
        }

        private fun refreshDataIntent(context: Context): Intent =
            Intent(context, WifiWidgetProvider::class.java)
                .setAction(ACTION_REFRESH_DATA)

        private const val ACTION_REFRESH_DATA = "com.w2sv.wifiwidget.ACTION_REFRESH_DATA"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        i { "onReceive" }

        if (intent?.action == ACTION_REFRESH_DATA) {
            i { "onReceive.ACTION_REFRESH_DATA" }

            val appWidgetManager = AppWidgetManager.getInstance(context!!)

            appWidgetManager.updateWidgets(
                appWidgetManager.getWifiWidgetIds(context),
                context
            )
        }
    }

    @Inject
    lateinit var widgetPreferences: WidgetPreferences

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetManager.updateWidgets(appWidgetIds, context)
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
                    setWifiDependentContent(context, widgetPreferences)

                    // set last_updated_tv text
                    setTextViewText(
                        R.id.last_updated_tv,
                        DateFormat.getTimeInstance(DateFormat.SHORT).format(Date())
                    )

                    // set refresh_button onClickListener
                    setOnClickPendingIntent(
                        R.id.refresh_button,
                        refreshDataPendingIntent(context)
                    )
                }
        )
    }
}