package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model

import androidx.compose.runtime.Stable
import com.w2sv.data.model.WifiProperty

@Stable
class IPPropertyCheckRowData(
    type: WidgetWifiProperty.IPProperty,
    isCheckedMap: MutableMap<WifiProperty, Boolean>,
    val subPropertyIsCheckedMap: MutableMap<WidgetWifiProperty.IPProperty.SubProperty, Boolean>,
    allowCheckChange: (Boolean) -> Boolean = { true },
) : WifiPropertyCheckRowData(type, isCheckedMap, allowCheckChange)
