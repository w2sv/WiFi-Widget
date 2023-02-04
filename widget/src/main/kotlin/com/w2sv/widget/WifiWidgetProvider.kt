@file:Suppress("DEPRECATION")

package com.w2sv.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.RemoteViews
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.w2sv.androidutils.extensions.getAppWidgetIds
import com.w2sv.widget.utils.setMakeUniqueActivityFlags
import slimber.log.i
import java.text.DateFormat
import java.time.Duration
import java.util.*

class WifiWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_WIDGET_OPTIONS_CHANGED =
            "com.w2sv.wifiwidget.action.WIDGET_OPTIONS_CHANGED"
        const val EXTRA_WIDGET_ID = "com.w2sv.wifiwidget.EXTRA_WIDGET_ID"
        const val EXTRA_OPEN_PROPERTIES_CONFIGURATION_DIALOG_ON_START =
            "com.w2sv.wifiwidget.extra.OPEN_PROPERTIES_CONFIGURATION_DIALOG_ON_START"

        fun getWidgetIds(context: Context): IntArray =
            AppWidgetManager.getInstance(context)
                .getAppWidgetIds(context, WifiWidgetProvider::class.java)

        private fun getRefreshDataPendingIntent(context: Context): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                PendingIntentCode.RefreshWidgetData.ordinal,
                getRefreshDataIntent(context),
                PendingIntent.FLAG_IMMUTABLE
            )

        fun refreshData(context: Context) {
            context.sendBroadcast(getRefreshDataIntent(context))
        }

        private fun getRefreshDataIntent(context: Context): Intent =
            Intent(context, WifiWidgetProvider::class.java)
                .setAction(ACTION_REFRESH_DATA)

        private const val ACTION_REFRESH_DATA = "com.w2sv.wifiwidget.action.REFRESH_DATA"
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)

        val refreshPeriod = Duration.ofMinutes(15L)

        getWorkManager(context)
            .enqueueUniquePeriodicWork(
                WidgetDataRefreshWorker.UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<WidgetDataRefreshWorker>(refreshPeriod)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiresBatteryNotLow(true)
                            .build()
                    )
                    .setInitialDelay(refreshPeriod)
                    .build()
            )

        i { "Enqueued ${WidgetDataRefreshWorker.UNIQUE_WORK_NAME}" }
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)

        getWorkManager(context)
            .cancelUniqueWork(WidgetDataRefreshWorker.UNIQUE_WORK_NAME)

        i { "Cancelled unique ${WidgetDataRefreshWorker.UNIQUE_WORK_NAME}" }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        context?.let {
            LocalBroadcastManager.getInstance(it).sendBroadcast(
                Intent(ACTION_WIDGET_OPTIONS_CHANGED)
                    .putExtra(EXTRA_WIDGET_ID, appWidgetId)
            )
            i { "Sent ACTION_WIDGET_OPTIONS_CHANGED Intent with EXTRA_WIDGET_ID=$appWidgetId" }
        }

        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        i { "onReceive | ${intent?.action} | ${intent?.extras?.keySet()?.toList()}" }

        when (intent?.action) {
            ACTION_REFRESH_DATA -> context?.let {
                onUpdate(
                    it,
                    AppWidgetManager.getInstance(it),
                    getWidgetIds(it)
                )
            }

            else -> super.onReceive(context, intent)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        i { "onUpdate ${appWidgetIds.toList()}" }

        appWidgetIds.forEach {
            appWidgetManager.updateWidget(it, context)
        }
    }

    private fun AppWidgetManager.updateWidget(
        appWidgetId: Int,
        context: Context
    ) {
        i { "update widget $appWidgetId" }

        updateAppWidget(
            appWidgetId,
            RemoteViews(context.packageName, R.layout.widget)
                .apply {
                    ConnectionDependentWidgetLayoutSetter.getInstance(context).populate(this)

                    // set last_updated_tv text
                    setTextViewText(
                        R.id.last_updated_tv,
                        DateFormat.getTimeInstance(DateFormat.SHORT).format(Date())
                    )

                    // refresh_button
                    setOnClickPendingIntent(
                        R.id.refresh_button,
                        getRefreshDataPendingIntent(context)
                    )

                    // connection_dependent_layout
                    setOnClickPendingIntent(
                        R.id.widget_layout,
                        PendingIntent.getActivity(
                            context,
                            PendingIntentCode.LaunchHomeActivity.ordinal,
                            Intent(Settings.ACTION_WIFI_SETTINGS)
                                .setMakeUniqueActivityFlags(),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                }
        )
    }
}

private fun getWorkManager(context: Context?): WorkManager =
    WorkManager
        .getInstance(context!!.applicationContext)