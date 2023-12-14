package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model

import androidx.compose.runtime.Immutable
import com.w2sv.domain.model.WidgetProperty
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class PropertyCheckRowData<T : WidgetProperty>(
    val property: T,
    val isChecked: () -> Boolean,
    val onCheckedChange: (Boolean) -> Unit,
    val allowCheckChange: (Boolean) -> Boolean = { true },
    val subPropertyCheckRowData: ImmutableList<PropertyCheckRowData<*>> = persistentListOf(),
    val infoDialogData: PropertyInfoDialogData? = null
) {
    companion object {
        fun <T : WidgetProperty> fromMutableMap(
            property: T,
            isCheckedMap: MutableMap<T, Boolean>,
            allowCheckChange: (Boolean) -> Boolean = { true },
            subPropertyCheckRowData: ImmutableList<PropertyCheckRowData<*>> = persistentListOf(),
            infoDialogData: PropertyInfoDialogData? = null
        ): PropertyCheckRowData<T> =
            PropertyCheckRowData(
                property = property,
                isChecked = { isCheckedMap.getValue(property) },
                onCheckedChange = { isCheckedMap[property] = it },
                allowCheckChange = allowCheckChange,
                subPropertyCheckRowData = subPropertyCheckRowData,
                infoDialogData = infoDialogData
            )
    }
}
