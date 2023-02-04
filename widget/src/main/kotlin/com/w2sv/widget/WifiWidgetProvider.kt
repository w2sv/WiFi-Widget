@file:Suppress("DEPRECATION")

package com.w2sv.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.RemoteViews
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.w2sv.androidutils.extensions.getAppWidgetIds
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.widget.utils.setMakeUniqueActivityFlags
import slimber.log.i
import java.text.DateFormat
import java.time.Duration
import java.util.*

class WifiWidgetProvider : AppWidgetProvider() {

    companion object {
        const val EXTRA_OPEN_PROPERTIES_CONFIGURATION_DIALOG_ON_START =
            "com.w2sv.wifiwidget.extra.OPEN_PROPERTIES_CONFIGURATION_DIALOG_ON_START"

        fun getWidgetIds(context: Context): IntArray =
            AppWidgetManager.getInstance(context)
                .getAppWidgetIds(context, WifiWidgetProvider::class.java)

        fun pinWidget(context: Context) {
            with(context){
                getSystemService(AppWidgetManager::class.java).let {
                    if (it.isRequestPinAppWidgetSupported) {
                        it.requestPinAppWidget(
                            ComponentName(
                                this,
                                WifiWidgetProvider::class.java
                            ),
                            null,
                            null
                        )
                    } else
                        showToast("Widget pinning not supported by your device launcher")
                }
            }
        }

        /**
         * Data Refreshing
         */

        fun triggerDataRefresh(context: Context) {
            context.sendBroadcast(getRefreshDataIntent(context))
        }

        fun getRefreshDataPendingIntent(context: Context): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                PendingIntentCode.RefreshWidgetData.ordinal,
                getRefreshDataIntent(context),
                PendingIntent.FLAG_IMMUTABLE
            )

        private fun getRefreshDataIntent(context: Context): Intent =
            Intent(context, WifiWidgetProvider::class.java)
                .setAction(ACTION_REFRESH_DATA)

        private const val ACTION_REFRESH_DATA = "com.w2sv.wifiwidget.action.REFRESH_DATA"
    }

    /**
     * Called upon the first AppWidget instance being created.
     *
     * Enqueues [WidgetDataRefreshWorker] as UniquePeriodicWork if not already enqueued.
     */
    override fun onEnabled(context: Context?) {
        super.onEnabled(context)

        context?.run {
            val refreshPeriod = Duration.ofMinutes(15L)

            WorkManager.getInstance(applicationContext)
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
    }

    /**
     * Called upon last AppWidget instance of provider being deleted.
     *
     * Cancels [WidgetDataRefreshWorker].
     */
    override fun onDisabled(context: Context?) {
        super.onDisabled(context)

        context?.run {
            WorkManager.getInstance(applicationContext)
                .cancelUniqueWork(WidgetDataRefreshWorker.UNIQUE_WORK_NAME)

            i { "Cancelled unique ${WidgetDataRefreshWorker.UNIQUE_WORK_NAME}" }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        i {
            "${this::class.java.name}.onReceive | ${intent?.action} | ${
                intent?.extras?.keySet()?.toList()
            }"
        }

        when (intent?.action) {
            ACTION_REFRESH_DATA -> {
                context?.let {
                    onUpdate(
                        it,
                        AppWidgetManager.getInstance(it),
                        getWidgetIds(it)
                    )
                }
                return
            }
            AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED -> context?.let {
                LocalBroadcastManager.getInstance(it).sendBroadcast(intent)
                i{"Forwarded AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED intent as local broadcast"}
            }
        }

        super.onReceive(context, intent)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        i { "${this::class.java.name}.onUpdate | appWidgetIds=${appWidgetIds.toList()}" }

        appWidgetIds.forEach {
            appWidgetManager.updateWidget(it, context)
        }
    }
}

private fun AppWidgetManager.updateWidget(
    appWidgetId: Int,
    context: Context
) {
    i { "updateWidget | appWidgetId=$appWidgetId" }

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

                /**
                 * OnClickPendingIntents
                 */

                // refresh_button
                setOnClickPendingIntent(
                    R.id.refresh_button,
                    WifiWidgetProvider.getRefreshDataPendingIntent(context)
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