package com.w2sv.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.w2sv.domain.repository.WidgetConfigFlow
import com.w2sv.widget.refreshing.WifiWidgetWorkScheduler
import com.w2sv.widget.ui.WifiGlanceWidget
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import slimber.log.i

@AndroidEntryPoint
internal class WifiWidgetProvider : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WifiGlanceWidget

    @Inject
    lateinit var refreshManager: WifiWidgetWorkScheduler

    @Inject
    lateinit var widgetConfigFlow: WidgetConfigFlow

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        i { "WifiWidgetProvider.onEnabled" }

        val refreshing = runBlocking { widgetConfigFlow.first().refreshing }
        refreshManager.applyRefreshingPolicy(refreshing)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        i { "WifiWidgetProvider.onDisabled" }
        refreshManager.cancelPeriodicWork()
    }
}
