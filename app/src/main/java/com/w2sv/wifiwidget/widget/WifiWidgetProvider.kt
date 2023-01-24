package com.w2sv.wifiwidget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.preferences.WidgetPreferences
import dagger.hilt.android.AndroidEntryPoint
import slimber.log.i
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject

fun anyWifiWidgetInUse(context: Context): Boolean =
    AppWidgetManager
        .getInstance(context)
        .getWifiWidgetIds(context)
        .isNotEmpty()

private fun AppWidgetManager.getWifiWidgetIds(context: Context): IntArray =
    getAppWidgetIds(
        ComponentName(
            context.packageName,
            WifiWidgetProvider::class.java.name
        )
    )

@AndroidEntryPoint
class WifiWidgetProvider : AppWidgetProvider() {

    companion object {
        fun getRefreshDataPendingIntent(context: Context): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                69,
                getRefreshDataIntent(context),
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )

        fun refreshData(context: Context) {
            context.sendBroadcast(getRefreshDataIntent(context))
        }

        private fun getRefreshDataIntent(context: Context): Intent =
            Intent(context, WifiWidgetProvider::class.java)
                .setAction(ACTION_REFRESH_DATA)

        private const val ACTION_REFRESH_DATA = "com.w2sv.wifiwidget.action.REFRESH_DATA"
    }

    @Inject
    lateinit var widgetPreferences: WidgetPreferences

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        i { "onReceive" }

        if (intent?.action == ACTION_REFRESH_DATA) {
            i { "onReceive.ACTION_REFRESH_DATA" }

            with(AppWidgetManager.getInstance(context!!)) {
                onUpdate(context, this, getWifiWidgetIds(context))
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach {
            appWidgetManager.updateWidget(it, context)
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
                        getRefreshDataPendingIntent(context)
                    )
                }
        )
    }
}