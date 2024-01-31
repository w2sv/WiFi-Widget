package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import com.w2sv.domain.model.WidgetProperty
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class PropertyCheckRowData<T : WidgetProperty>(
    val property: T,
    val isChecked: () -> Boolean,
    val onCheckedChange: (Boolean) -> Unit,
    val allowCheckChange: (Boolean) -> Boolean = { true },
    val subPropertyCheckRowDataList: ImmutableList<PropertyCheckRowData<*>>? = null,
    val infoDialogData: InfoDialogData? = null,
    val modifier: Modifier = Modifier
) {
    companion object {
        fun <T : WidgetProperty> fromMutableMap(
            property: T,
            isCheckedMap: MutableMap<T, Boolean>,
            allowCheckChange: (Boolean) -> Boolean = { true },
            subPropertyCheckRowData: ImmutableList<PropertyCheckRowData<*>>? = null,
            infoDialogData: InfoDialogData? = null,
            modifier: Modifier = Modifier
        ): PropertyCheckRowData<T> =
            PropertyCheckRowData(
                property = property,
                isChecked = { isCheckedMap.getValue(property) },
                onCheckedChange = { isCheckedMap[property] = it },
                allowCheckChange = allowCheckChange,
                subPropertyCheckRowDataList = subPropertyCheckRowData,
                infoDialogData = infoDialogData,
                modifier = modifier
            )
    }
}
