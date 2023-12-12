package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.w2sv.domain.model.WidgetButton
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.content.PropertyCheckRow
import com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model.PropertyCheckRowData
import kotlinx.collections.immutable.ImmutableList

@Composable
fun ButtonSelection(
    propertyCheckRowData: ImmutableList<PropertyCheckRowData<WidgetButton>>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        propertyCheckRowData
            .forEach {
                PropertyCheckRow(data = it)
            }
    }
}
