package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.w2sv.data.model.WidgetButton
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.PropertyCheckRow
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.PropertyCheckRowData

@Composable
internal fun ButtonSelection(
    buttonMap: MutableMap<WidgetButton, Boolean>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        remember {
            listOf(
                PropertyCheckRowData(
                    WidgetButton.Refresh,
                    R.string.refresh,
                    buttonMap
                ),
                PropertyCheckRowData(
                    WidgetButton.GoToWifiSettings,
                    R.string.go_to_wifi_settings,
                    buttonMap
                ),
                PropertyCheckRowData(
                    WidgetButton.GoToWidgetSettings,
                    R.string.go_to_widget_settings,
                    buttonMap
                )
            )
        }
            .forEach {
                PropertyCheckRow(data = it)
            }
    }
}