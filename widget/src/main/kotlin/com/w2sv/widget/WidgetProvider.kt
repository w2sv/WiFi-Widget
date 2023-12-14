package com.w2sv.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.w2sv.androidutils.generic.getIntExtraOrNull
import com.w2sv.widget.ui.WidgetLayoutPopulator
import com.w2sv.widget.utils.getWifiWidgetIds
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import slimber.log.i
import javax.inject.Inject
import javax.inject.Singleton

@AndroidEntryPoint
class WidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var optionsChanged: OptionsChanged

    @Inject
    lateinit var widgetDataRefreshWorkerManager: WidgetDataRefreshWorker.Manager

    @Inject
    lateinit var widgetLayoutPopulator: WidgetLayoutPopulator

    @Inject
    lateinit var appWidgetManager: AppWidgetManager

    /**
     * Called upon the first AppWidget instance being created.
     *
     * Enqueues [WidgetDataRefreshWorker] as UniquePeriodicWork if not already enqueued.
     */
    override fun onEnabled(context: Context?) {
        super.onEnabled(context)

        CoroutineScope(Dispatchers.Default).launch {
            widgetDataRefreshWorkerManager.applyChangedParameters()
        }
    }

    /**
     * Called upon last AppWidget instance of provider being deleted.
     *
     * Cancels [WidgetDataRefreshWorker].
     */
    override fun onDisabled(context: Context?) {
        super.onDisabled(context)

        widgetDataRefreshWorkerManager.cancelWorker()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent) // Required for DI

        i {
            "${this::class.java.simpleName}.onReceive | ${intent?.action} | ${
                intent?.extras?.keySet()?.toList()
            }"
        }

        when (intent?.action) {
            ACTION_REFRESH_DATA -> {
                context?.let {
                    // Refresh data
                    onUpdate(
                        it,
                        appWidgetManager,
                        appWidgetManager.getWifiWidgetIds(it),
                    )
                }
            }

            AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED -> intent.getIntExtraOrNull(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                -1,
            )?.let { widgetId ->
                i { "AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED | id = $widgetId" }
                optionsChanged.onOptionsChanged(widgetId)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        i { "${this::class.java.simpleName}.onUpdate | appWidgetIds=${appWidgetIds.toList()}" }

        val widgetView = RemoteViews(
            context.packageName,
            R.layout.widget,
        )

        appWidgetIds.forEach { id ->
            i { "updateWidget | appWidgetId=$id" }

            appWidgetManager.updateAppWidget(
                id,
                widgetLayoutPopulator
                    .populate(
                        widget = widgetView,
                        appWidgetId = id,
                    ),
            )
        }
    }

    @Singleton
    class OptionsChanged @Inject constructor() {
        private val scope = CoroutineScope(Dispatchers.Default)

        val widgetId get() = _widgetId.asSharedFlow()
        private val _widgetId = MutableSharedFlow<Int>()

        fun onOptionsChanged(widgetId: Int) {
            scope.launch {
                _widgetId.emit(widgetId)
            }
        }
    }

    companion object {

        // ===============
        // Refreshing
        // ===============

        fun triggerDataRefresh(context: Context) {
            context.sendBroadcast(getRefreshDataIntent(context))
        }

        fun getRefreshDataIntent(context: Context): Intent =
            Intent(context, WidgetProvider::class.java)
                .setAction(ACTION_REFRESH_DATA)

        private const val ACTION_REFRESH_DATA = "com.w2sv.wifiwidget.action.REFRESH_DATA"
    }
}
