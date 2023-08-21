package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model

import androidx.compose.runtime.Stable
import com.w2sv.data.model.WifiProperty

@Stable
open class WifiPropertyCheckRowData(
    type: WifiProperty,
    isCheckedMap: MutableMap<WifiProperty, Boolean>,
    allowCheckChange: (Boolean) -> Boolean = { true },
) : PropertyCheckRowData<WifiProperty>(
    type,
    type.viewData.labelRes,
    isCheckedMap,
    allowCheckChange,
)
