package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.w2sv.domain.model.WidgetWifiProperty

@Immutable
open class PropertyCheckRowData<T>(
    val property: T,
    @StringRes val labelRes: Int,
    val isChecked: () -> Boolean,
    val onCheckedChange: (Boolean) -> Unit,
    val allowCheckChange: (Boolean) -> Boolean = { true },
) {
    constructor(
        property: T,
        @StringRes labelRes: Int,
        isCheckedMap: MutableMap<T, Boolean>,
        allowCheckChange: (Boolean) -> Boolean = { true },
    ) : this(
        property = property,
        labelRes = labelRes,
        isChecked = { isCheckedMap.getValue(property) },
        onCheckedChange = { isCheckedMap[property] = it },
        allowCheckChange = allowCheckChange,
    )
}

@Immutable
open class WifiPropertyCheckRowData(
    property: WidgetWifiProperty,
    isCheckedMap: MutableMap<WidgetWifiProperty, Boolean>,
    allowCheckChange: (Boolean) -> Boolean = { true },
) : PropertyCheckRowData<WidgetWifiProperty>(
    property,
    property.labelRes,
    isCheckedMap,
    allowCheckChange,
)

@Immutable
class IPPropertyCheckRowData(
    property: WidgetWifiProperty.IP,
    isCheckedMap: MutableMap<WidgetWifiProperty, Boolean>,
    val subPropertyIsCheckedMap: MutableMap<WidgetWifiProperty.IP.SubProperty, Boolean>,
    allowCheckChange: (Boolean) -> Boolean = { true },
) : WifiPropertyCheckRowData(property, isCheckedMap, allowCheckChange)
