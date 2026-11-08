package com.w2sv.widget.ui.content

import android.R as AndroidR
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import com.w2sv.widget.ui.layout.calculateWidgetLayout
import com.w2sv.widget.ui.state.WidgetPresentationState
import com.w2sv.widget.ui.state.WidgetWifiState
import com.w2sv.widget.ui.util.thenIfApiAtLeast

@Composable
internal fun WifiWidgetContent(state: WidgetPresentationState) {
    val size = LocalSize.current
    val layout = calculateWidgetLayout(size, state.utilities, state.renderTime, state.valueAlignment)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .thenIfApiAtLeast(SYSTEM_WIDGET_CORNER_RADIUS_SUPPORTING_API) {
                this
                    .background(Color(state.backgroundColor))
                    .cornerRadius(AndroidR.dimen.system_app_widget_background_radius)
            }
    ) {
        if (!supportsSystemWidgetCornerRadius) {
            LegacyWidgetBackground(size, state.backgroundColor)
        }
        when (val connection = state.wifiState) {
            is WidgetWifiState.Connected -> {
                WidgetPropertyList(
                    rows = connection.rows,
                    state = state,
                    layout = layout.properties,
                    bottomClearance = layout.bottomClearance
                )
            }

            is WidgetWifiState.Disconnected -> {
                DisconnectedContent(state, layout.status)
            }
        }
        FloatingUtilities(state, layout.actions, layout.timestamp)
    }
}

private const val SYSTEM_WIDGET_CORNER_RADIUS_SUPPORTING_API = Build.VERSION_CODES.S

@get:ChecksSdkIntAtLeast(api = SYSTEM_WIDGET_CORNER_RADIUS_SUPPORTING_API)
private val supportsSystemWidgetCornerRadius: Boolean
    get() = Build.VERSION.SDK_INT >= SYSTEM_WIDGET_CORNER_RADIUS_SUPPORTING_API
