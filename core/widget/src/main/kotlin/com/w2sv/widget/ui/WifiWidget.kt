package com.w2sv.widget.ui

import WifiPropertyList
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.Text
import com.w2sv.domain.model.FontSize
import com.w2sv.domain.model.PropertyValueAlignment
import com.w2sv.domain.model.WidgetBottomBarElement
import com.w2sv.domain.model.WidgetColoring
import com.w2sv.domain.model.WidgetWifiState
import com.w2sv.domain.model.WifiProperty
import com.w2sv.widget.data.WidgetModuleWidgetRepository
import com.w2sv.widget.model.TopBar
import com.w2sv.widget.model.WidgetAppearance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last

internal class WifiWidget(private val widgetRepository: WidgetModuleWidgetRepository) : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appearance = widgetRepository.widgetAppearance.first()
        val state = widgetRepository.widgetState.last()

        provideContent {
            Content(state, appearance)
        }
    }
}

@Composable
private fun Content(state: WidgetWifiState, appearance: WidgetAppearance) {
    Scaffold(titleBar = { TitleBar(appearance.topBar.elements) }) {
        Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (state) {
                is WidgetWifiState.Disconnected -> Text("Disconnected")
                is WidgetWifiState.Disabled -> Text("Disabled")
                is WidgetWifiState.Connected.PropertiesLoading -> CircularProgressIndicator()
                is WidgetWifiState.Connected.PropertiesAvailable -> WifiPropertyList(state.properties)
            }
        }
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 200, heightDp = 200)
@Preview(widthDp = 300, heightDp = 200)
@Composable
private fun Prev() {
    Content(
        WidgetWifiState.Disconnected,
        WidgetAppearance(
            WidgetColoring.Config(),
            1f,
            FontSize.Medium,
            PropertyValueAlignment.Left,
            TopBar(
                listOf(
                    WidgetBottomBarElement.RefreshButton,
                    WidgetBottomBarElement.GoToWidgetSettingsButton
                )
            ),
            true
        )
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 300, heightDp = 200)
@Composable
private fun LoadingPrev() {
    Content(
        WidgetWifiState.Connected.PropertiesLoading,
        WidgetAppearance(
            WidgetColoring.Config(),
            1f,
            FontSize.Medium,
            PropertyValueAlignment.Left,
            TopBar(
                listOf(
                    WidgetBottomBarElement.RefreshButton,
                    WidgetBottomBarElement.GoToWidgetSettingsButton
                )
            ),
            true
        )
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 300, heightDp = 200)
@Composable
private fun PropertiesPrev() {
    Content(
        WidgetWifiState.Connected.PropertiesAvailable(
            listOf(
                WifiProperty.ViewData.NonIP("SSID", "YourSSID"),
                WifiProperty.ViewData.NonIP("Link Speed", "390 Mbps"),
            )
        ),
        WidgetAppearance(
            WidgetColoring.Config(),
            1f,
            FontSize.Medium,
            PropertyValueAlignment.Left,
            TopBar(
                listOf(
                    WidgetBottomBarElement.RefreshButton,
                    WidgetBottomBarElement.GoToWidgetSettingsButton
                )
            ),
            true
        )
    )
}
