@file:Suppress("DEPRECATION")

package com.w2sv.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.w2sv.androidutils.extensions.getAppWidgetIds
import com.w2sv.androidutils.extensions.showToast
import slimber.log.i
import java.util.*

class WifiWidgetProvider : AppWidgetProvider() {

    companion object {
        const val EXTRA_OPEN_CONFIGURATION_DIALOG_ON_START =
            "com.w2sv.wifiwidget.extra.OPEN_CONFIGURATION_DIALOG_ON_START"

        fun getWidgetIds(context: Context): IntArray =
            AppWidgetManager.getInstance(context)
                .getAppWidgetIds(context, WifiWidgetProvider::class.java)

        fun pinWidget(context: Context) {
            with(context) {
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

        context?.let {
            WidgetDataRefreshWorker.Administrator.getInstance(it).enableWorkerIfApplicable()
        }
    }

    /**
     * Called upon last AppWidget instance of provider being deleted.
     *
     * Cancels [WidgetDataRefreshWorker].
     */
    override fun onDisabled(context: Context?) {
        super.onDisabled(context)

        context?.let {
            WidgetDataRefreshWorker.Administrator.getInstance(it).cancelWorker()
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        i {
            "${this::class.java.simpleName}.onReceive | ${intent?.action} | ${
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
                i { "Forwarded AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED intent as local broadcast" }
            }
        }

        super.onReceive(context, intent)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        i { "${this::class.java.simpleName}.onUpdate | appWidgetIds=${appWidgetIds.toList()}" }

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
        WidgetPopulator
            .getInstance(context)
            .populate(
                RemoteViews(
                    context.packageName,
                    R.layout.widget
                )
            )
    )
}