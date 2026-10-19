package com.w2sv.widget.actions

import android.appwidget.AppWidgetManager
import android.content.Context
import com.w2sv.androidutils.appwidget.appWidgetIds
import com.w2sv.androidutils.content.componentName
import com.w2sv.domain.widget.WidgetActions
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.widget.WifiWidgetProvider
import com.w2sv.widget.di.WidgetPinSuccessFlow
import com.w2sv.widget.refreshing.WifiWidgetWorkScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.SharedFlow

internal class WidgetActionsImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appWidgetManager: AppWidgetManager,
    private val refreshManager: WifiWidgetWorkScheduler,
    @WidgetPinSuccessFlow override val pinSuccesses: SharedFlow<Unit>
) : WidgetActions {

    override fun pin(onFailure: () -> Unit) {
        appWidgetManager.attemptWifiWidgetPin(
            context = context,
            onFailure = onFailure
        )
    }

    override fun refresh() {
        context.sendBroadcast(WifiWidgetProvider.refreshIntent(context))
    }

    override fun render() {
        context.sendBroadcast(WifiWidgetProvider.renderIntent(context))
    }

    override fun applyConfigurationChange(previous: WidgetConfig, current: WidgetConfig) {
        if (previous.refreshing != current.refreshing && appWidgetManager.appWidgetIds<WifiWidgetProvider>(context).isNotEmpty()) {
            refreshManager.applyRefreshingPolicy(current.refreshing)
        }

        // Trigger an immediate render unblocked by remote data refresh
        render()

        if (previous.remoteDataConfig != current.remoteDataConfig) {
            refresh()
        }
    }
}

/**
 * Calls [AppWidgetManager.requestPinAppWidget] if [AppWidgetManager.isRequestPinAppWidgetSupported] returns true.
 * Requests widget pinning if supported, invoking [onFailure] if the request
 * cannot be submitted.
 */
private fun AppWidgetManager.attemptWifiWidgetPin(context: Context, onFailure: () -> Unit) {
    if (!isRequestPinAppWidgetSupported ||
        !requestPinAppWidget(
            componentName<WifiWidgetProvider>(context),
            null,
            WidgetPinSuccessBroadcastReceiver.pendingIntent(context)
        )
    ) {
        onFailure()
    }
}
