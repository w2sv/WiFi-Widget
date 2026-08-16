package com.w2sv.widget.ui

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import com.w2sv.widget.di.GlanceWidgetEntryPoint
import com.w2sv.widget.ui.content.WifiWidgetContent
import com.w2sv.widget.ui.preview.connectedWifiWidgetPreviewState
import com.w2sv.widget.ui.preview.wifiWidgetPreviewSizes
import dagger.hilt.android.EntryPointAccessors

internal class WifiGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override val previewSizeMode = SizeMode.Responsive(wifiWidgetPreviewSizes)

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val stateProvider = EntryPointAccessors.fromApplication(
            context,
            GlanceWidgetEntryPoint::class.java
        ).stateProvider()
        val state = stateProvider(context)

        provideContent {
            WifiWidgetContent(state)
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent {
            WifiWidgetContent(connectedWifiWidgetPreviewState())
        }
    }
}
