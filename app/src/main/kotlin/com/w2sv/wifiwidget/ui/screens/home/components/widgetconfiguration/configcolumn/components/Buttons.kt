package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.w2sv.data.model.WidgetButton
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.ParameterCheckRow
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.ParameterCheckRowData

@Composable
internal fun ButtonSelection(
    buttonMap: MutableMap<WidgetButton, Boolean>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        remember {
            listOf(
                ParameterCheckRowData(
                    WidgetButton.Refresh,
                    R.string.refresh
                ),
                ParameterCheckRowData(
                    WidgetButton.GoToWifiSettings,
                    R.string.go_to_wifi_settings
                ),
                ParameterCheckRowData(
                    WidgetButton.GoToWidgetSettings,
                    R.string.go_to_widget_settings
                )
            )
        }
            .forEach {
                ParameterCheckRow(data = it, typeToIsChecked = buttonMap)
            }
    }
}