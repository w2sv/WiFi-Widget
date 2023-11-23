package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import com.w2sv.domain.model.WidgetWifiProperty

@Stable
open class PropertyCheckRowData<T>(
    val type: T,
    @StringRes val labelRes: Int,
    val isChecked: () -> Boolean,
    val onCheckedChange: (Boolean) -> Unit,
    val allowCheckChange: (Boolean) -> Boolean = { true },
) {
    constructor(
        type: T,
        @StringRes labelRes: Int,
        isCheckedMap: MutableMap<T, Boolean>,
        allowCheckChange: (Boolean) -> Boolean = { true },
    ) : this(
        type = type,
        labelRes = labelRes,
        isChecked = { isCheckedMap.getValue(type) },
        onCheckedChange = { isCheckedMap[type] = it },
        allowCheckChange = allowCheckChange,
    )
}

@Stable
open class WifiPropertyCheckRowData(
    type: WidgetWifiProperty,
    isCheckedMap: MutableMap<WidgetWifiProperty, Boolean>,
    allowCheckChange: (Boolean) -> Boolean = { true },
) : PropertyCheckRowData<WidgetWifiProperty>(
    type,
    type.viewData.labelRes,
    isCheckedMap,
    allowCheckChange,
)

@Stable
class IPPropertyCheckRowData(
    type: WidgetWifiProperty.IPProperty,
    isCheckedMap: MutableMap<WidgetWifiProperty, Boolean>,
    val subPropertyIsCheckedMap: MutableMap<WidgetWifiProperty.IPProperty.SubProperty, Boolean>,
    allowCheckChange: (Boolean) -> Boolean = { true },
) : WifiPropertyCheckRowData(type, isCheckedMap, allowCheckChange)
