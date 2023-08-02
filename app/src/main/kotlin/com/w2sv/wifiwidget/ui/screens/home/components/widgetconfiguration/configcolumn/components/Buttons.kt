package com.w2sv.wifiwidget.ui.screens.home.components.widgetconfiguration.configcolumn.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.w2sv.data.model.WidgetButton
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.components.JostText
import com.w2sv.wifiwidget.ui.components.bulletPointText

@Stable
private data class ButtonSelectionViewData(val button: WidgetButton, val label: String)

@Composable
internal fun ButtonSelection(
    buttonMap: MutableMap<WidgetButton, Boolean>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        remember {
            listOf(
                ButtonSelectionViewData(WidgetButton.Refresh, "Refresh"),
                ButtonSelectionViewData(WidgetButton.GoToWifiSettings, "Go to WiFi Settings"),
                ButtonSelectionViewData(WidgetButton.GoToWidgetSettings, "Go to Widget Settings")
            )
        }
            .forEach {
                ButtonSelectionRow(data = it, buttonMap = buttonMap)
            }
    }
}

@Composable
private fun ButtonSelectionRow(
    data: ButtonSelectionViewData,
    buttonMap: MutableMap<WidgetButton, Boolean>,
    modifier: Modifier = Modifier
) {
    val checkBoxCD = stringResource(id = R.string.set_unset).format(data.label)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        JostText(text = bulletPointText(data.label))
        Checkbox(
            checked = buttonMap.getValue(data.button),
            onCheckedChange = {
                buttonMap[data.button] = it
            },
            modifier = Modifier.semantics {
                contentDescription = checkBoxCD
            }
        )
    }
}