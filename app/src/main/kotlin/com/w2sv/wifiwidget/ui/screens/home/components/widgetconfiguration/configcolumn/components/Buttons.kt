package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.w2sv.data.model.WidgetButton
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.ParameterCheckRow
import com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.ParameterSelection

@Composable
internal fun ButtonSelection(
    buttonMap: MutableMap<WidgetButton, Boolean>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        remember {
            listOf(
                ParameterSelection("Refresh", WidgetButton.Refresh),
                ParameterSelection("Go to WiFi Settings", WidgetButton.GoToWifiSettings),
                ParameterSelection("Go to Widget Settings", WidgetButton.GoToWidgetSettings)
            )
        }
            .forEach {
                ParameterCheckRow(data = it, typeToIsChecked = buttonMap)
            }
    }
}