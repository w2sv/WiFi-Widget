package com.w2sv.widget.ui

import android.content.Context
import androidx.compose.runtime.LaunchedEffect
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
import kotlin.time.TimeSource
import slimber.log.d

internal class WifiGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact
    override val previewSizeMode = SizeMode.Responsive(launcherPreviewSizes)

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val started = TimeSource.Monotonic.markNow()
        d { "Widget provideGlance invoked: glanceId=$id" }
        val entryPoint = glanceWidgetEntryPoint(context)
        val stateProvider = entryPoint.stateProvider()
        val initial = entryPoint.renderCoordinator().withPresentationInitialization { stateProvider() }
        d { "Widget initial presentation acquired: glanceId=$id, revision=${initial.revision}, duration=${started.elapsedNow()}" }
        val updates = stateProvider.updates(initial.revision)

        provideContent {
            val snapshot by updates.collectAsState(initial)
            LaunchedEffect(snapshot.revision) {
                d { "Widget presentation consumed: glanceId=$id, revision=${snapshot.revision}" }
            }
            WifiWidgetContent(snapshot.state)
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent { WifiWidgetContent(widgetPreviewState(context)) }
    }
}
