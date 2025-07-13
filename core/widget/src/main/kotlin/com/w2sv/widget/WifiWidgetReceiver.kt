package com.w2sv.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.w2sv.widget.data.WidgetModuleWidgetRepository
import com.w2sv.widget.ui.WifiWidget
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WifiWidgetReceiver : GlanceAppWidgetReceiver() {

    @Inject
    internal lateinit var widgetRepository: WidgetModuleWidgetRepository

    override val glanceAppWidget: GlanceAppWidget = WifiWidget(widgetRepository)
}
