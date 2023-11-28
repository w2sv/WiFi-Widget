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
        type: T,
        @StringRes labelRes: Int,
        isCheckedMap: MutableMap<T, Boolean>,
        allowCheckChange: (Boolean) -> Boolean = { true },
    ) : this(
        property = type,
        labelRes = labelRes,
        isChecked = { isCheckedMap.getValue(type) },
        onCheckedChange = { isCheckedMap[type] = it },
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
    property.viewData.labelRes,
    isCheckedMap,
    allowCheckChange,
)

@Immutable
class IPPropertyCheckRowData(
    property: WidgetWifiProperty.IPProperty,
    isCheckedMap: MutableMap<WidgetWifiProperty, Boolean>,
    val subPropertyIsCheckedMap: MutableMap<WidgetWifiProperty.IPProperty.SubProperty, Boolean>,
    allowCheckChange: (Boolean) -> Boolean = { true },
) : WifiPropertyCheckRowData(property, isCheckedMap, allowCheckChange)
