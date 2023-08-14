package com.w2sv.wifiwidget.ui.screens.home.components.widget_configuration_dialog.model

import androidx.compose.runtime.Stable
import com.w2sv.data.model.WifiProperty

@Stable
class IPPropertyCheckRowData(
    type: WifiProperty.IPProperty,
    isCheckedMap: MutableMap<WifiProperty, Boolean>,
    val subPropertyIsCheckedMap: MutableMap<WifiProperty.IPProperty.SubProperty, Boolean>,
    allowCheckChange: (Boolean) -> Boolean = { true }
) : WifiPropertyCheckRowData(type, isCheckedMap, allowCheckChange)