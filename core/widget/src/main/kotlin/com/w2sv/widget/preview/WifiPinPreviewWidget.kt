package com.w2sv.widget.preview

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.w2sv.widget.ui.content.WifiWidgetContent
import com.w2sv.widget.ui.state.WidgetPresentationState

internal class WifiPinPreviewWidget(private val state: WidgetPresentationState) : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { WifiWidgetContent(state) }
    }
}
