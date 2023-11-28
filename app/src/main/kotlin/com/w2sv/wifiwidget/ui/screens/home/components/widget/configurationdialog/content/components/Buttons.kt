package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.w2sv.domain.model.WidgetButton
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.PropertyCheckRow
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.PropertyCheckRowData

@Composable
internal fun ButtonSelection(
    buttonMap: MutableMap<WidgetButton, Boolean>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        remember {
            WidgetButton.entries.map {
                PropertyCheckRowData(
                    type = it,
                    labelRes = it.labelRes,
                    isCheckedMap = buttonMap
                )
            }
        }
            .forEach {
                PropertyCheckRow(data = it)
            }
    }
}
