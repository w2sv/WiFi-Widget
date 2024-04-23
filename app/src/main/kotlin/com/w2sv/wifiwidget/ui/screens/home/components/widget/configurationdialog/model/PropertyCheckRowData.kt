package com.w2sv.wifiwidget.ui.screens.home.components.widget.configurationdialog.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import com.w2sv.domain.model.WidgetProperty
import kotlinx.collections.immutable.ImmutableList

sealed interface PropertyConfigurationElement {

    @Immutable
    data class Custom(val content: @Composable () -> Unit) : PropertyConfigurationElement

    @Immutable
    sealed interface CheckRow<T : WidgetProperty> : PropertyConfigurationElement {
        val property: T
        val isChecked: () -> Boolean
        val onCheckedChange: (Boolean) -> Unit
        val infoDialogData: InfoDialogData?
        val modifier: Modifier

        @Immutable
        data class WithoutSubProperties<T : WidgetProperty>(
            override val property: T,
            override val isChecked: () -> Boolean,
            override val onCheckedChange: (Boolean) -> Unit,
            override val infoDialogData: InfoDialogData? = null,
            override val modifier: Modifier = Modifier,
        ) : CheckRow<T> {

            companion object {
                fun <T : WidgetProperty> fromIsCheckedMap(
                    property: T,
                    isCheckedMap: MutableMap<T, Boolean>,
                    allowCheckChange: (Boolean) -> Boolean = { true },
                    onCheckedChangedDisallowed: () -> Unit = {},
                    infoDialogData: InfoDialogData? = null,
                    modifier: Modifier = Modifier
                ): CheckRow<T> {
                    return WithoutSubProperties(
                        property = property,
                        isChecked = { isCheckedMap.getValue(property) },
                        onCheckedChange = {
                            if (allowCheckChange(it)) {
                                isCheckedMap[property] = it
                            } else {
                                onCheckedChangedDisallowed()
                            }
                        },
                        infoDialogData = infoDialogData,
                        modifier = modifier
                    )
                }
            }
        }

        @Immutable
        data class WithSubProperties<T : WidgetProperty>(
            override val property: T,
            override val isChecked: () -> Boolean,
            override val onCheckedChange: (Boolean) -> Unit,
            val subPropertyCheckRowDataList: ImmutableList<PropertyConfigurationElement>,
            val subPropertyColumnModifier: Modifier = Modifier,
            override val infoDialogData: InfoDialogData? = null,
            override val modifier: Modifier = Modifier
        ) : CheckRow<T> {

            companion object {
                fun <T : WidgetProperty> fromIsCheckedMap(
                    property: T,
                    isCheckedMap: MutableMap<T, Boolean>,
                    subPropertyCheckRowDataList: ImmutableList<PropertyConfigurationElement>,
                    subPropertyCheckRowColumnModifier: Modifier = Modifier,
                    allowCheckChange: (Boolean) -> Boolean = { true },
                    onCheckedChangedDisallowed: () -> Unit = {},
                    infoDialogData: InfoDialogData? = null,
                    modifier: Modifier = Modifier
                ): CheckRow<T> {
                    return WithSubProperties(
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
}
