package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable

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
