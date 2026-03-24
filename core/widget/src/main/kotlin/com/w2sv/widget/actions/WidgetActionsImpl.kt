package com.w2sv.widget.actions

import android.appwidget.AppWidgetManager
import android.content.Context
import com.w2sv.androidutils.content.componentName
import com.w2sv.domain.model.widget.WidgetRefreshing
import com.w2sv.widget.WifiWidgetProvider
import com.w2sv.widget.refreshing.WifiWidgetWorkScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class WidgetActionsImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appWidgetManager: AppWidgetManager,
    private val refreshManager: WifiWidgetWorkScheduler
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

    override fun applyRefreshingPolicy(refreshing: WidgetRefreshing) {
        refreshManager.applyRefreshingPolicy(refreshing)
    }
}

/**
 * Calls [AppWidgetManager.requestPinAppWidget] if [AppWidgetManager.isRequestPinAppWidgetSupported] returns true.
 * Otherwise invokes [onFailure].
 */
private fun AppWidgetManager.attemptWifiWidgetPin(context: Context, onFailure: () -> Unit) {
    if (isRequestPinAppWidgetSupported) {
        requestPinAppWidget(
            componentName<WifiWidgetProvider>(context),
            null,
            WidgetPinSuccessBroadcastReceiver.pendingIntent(context)
        )
    } else {
        onFailure()
    }
}
