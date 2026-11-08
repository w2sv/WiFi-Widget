package com.w2sv.widget.ui

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import com.w2sv.widget.di.glanceWidgetEntryPoint
import com.w2sv.widget.preview.launcherPreviewSizes
import com.w2sv.widget.preview.widgetPreviewState
import com.w2sv.widget.ui.content.WifiWidgetContent

internal class WifiGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact
    override val previewSizeMode = SizeMode.Responsive(launcherPreviewSizes)

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val stateProvider = glanceWidgetEntryPoint(context).stateProvider()
        val revision = stateProvider.currentRevision
        val initialState = stateProvider()
        val updates = stateProvider.updates(revision)

        provideContent {
            val state by updates.collectAsState(initialState)
            WifiWidgetContent(state)
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent { WifiWidgetContent(widgetPreviewState(context)) }
    }
}
