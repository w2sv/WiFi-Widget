package com.w2sv.widget.ui.content

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import com.w2sv.widget.ui.model.WifiWidgetState

@Composable
internal fun WifiWidgetContent(state: WifiWidgetState) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(state.backgroundColor)
            .cornerRadius(16.dp)
            .padding(horizontal = 10.dp)
    ) {
        if (state.status.isConnected) {
            WifiPropertyList(
                modifier = GlanceModifier.defaultWeight(),
                properties = state.properties,
                alignment = state.propertyValueAlignment,
                colors = state.colors,
                fontSize = state.fontSize
            )
        } else {
            DisconnectedContent(
                modifier = GlanceModifier.defaultWeight(),
                status = state.status,
                colors = state.colors,
                fontSize = state.fontSize
            )
        }

        if (state.enabledUtilities.isNotEmpty()) {
            WidgetUtilityRow(state)
        }
    }
}
