package com.w2sv.widget.ui.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.unit.ColorProvider
import com.w2sv.widget.ui.model.WifiWidgetState

@Composable
internal fun WifiWidgetContent(state: WifiWidgetState) {
    val fontSize = state.config.appearance.fontSize

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(state.backgroundColor))
            .cornerRadius(16.dp)
            .padding(horizontal = 10.dp)
    ) {
        if (state.status.isConnected) {
            WifiPropertyList(
                modifier = GlanceModifier.defaultWeight(),
                properties = state.properties,
                alignment = state.config.appearance.propertyValueAlignment,
                colors = state.colors,
                fontSize = fontSize
            )
        } else {
            DisconnectedContent(
                modifier = GlanceModifier.defaultWeight(),
                status = state.status,
                colors = state.colors,
                fontSize = fontSize
            )
        }

        WidgetUtilityRow(state)
    }
}
