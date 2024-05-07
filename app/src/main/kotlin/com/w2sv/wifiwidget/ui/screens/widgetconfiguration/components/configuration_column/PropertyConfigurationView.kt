package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.configuration_column

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import com.w2sv.domain.model.WidgetProperty
import com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.dialog.model.InfoDialogData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

sealed interface PropertyConfigurationView {

    @Immutable
    data class Custom(val content: @Composable () -> Unit) : PropertyConfigurationView

    @Immutable
    data class CheckRow<T : WidgetProperty>(
        val property: T,
        val isChecked: () -> Boolean,
        val onCheckedChange: (Boolean) -> Unit,
        val infoDialogData: InfoDialogData?,
        val subPropertyCheckRowDataList: ImmutableList<PropertyConfigurationView> = persistentListOf(),
        val subPropertyColumnModifier: Modifier = Modifier,
        val modifier: Modifier = Modifier
    ) : PropertyConfigurationView {

        val hasSubProperties: Boolean
            get() = subPropertyCheckRowDataList.isNotEmpty()

        companion object {
            fun <T : WidgetProperty> fromIsCheckedMap(
                property: T,
                isCheckedMap: MutableMap<T, Boolean>,
                subPropertyCheckRowDataList: ImmutableList<PropertyConfigurationView> = persistentListOf(),
                subPropertyCheckRowColumnModifier: Modifier = Modifier,
                allowCheckChange: (Boolean) -> Boolean = { true },
                onCheckedChangedDisallowed: () -> Unit = {},
                infoDialogData: InfoDialogData? = null,
                modifier: Modifier = Modifier
            ): CheckRow<T> {
                return CheckRow(
                    property = property,
                    isChecked = { isCheckedMap.getValue(property) },
                    onCheckedChange = {
                        if (allowCheckChange(it)) {
                            isCheckedMap[property] = it
                        } else {
                            onCheckedChangedDisallowed()
                        }
                    },
                    subPropertyCheckRowDataList = subPropertyCheckRowDataList,
                    subPropertyColumnModifier = subPropertyCheckRowColumnModifier,
                    infoDialogData = infoDialogData,
                    modifier = modifier,
                )
            }
        }
    }
}
