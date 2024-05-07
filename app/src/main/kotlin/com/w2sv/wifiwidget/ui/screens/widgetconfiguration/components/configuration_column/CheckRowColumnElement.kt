package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.configuration_column

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import com.w2sv.domain.model.WidgetProperty
import com.w2sv.wifiwidget.ui.utils.ShakeController

sealed interface CheckRowColumnElement {

    @Immutable
    data class Custom(val content: @Composable () -> Unit) : CheckRowColumnElement

    @Immutable
    data class CheckRow<T : WidgetProperty>(
        val property: T,
        val isChecked: () -> Boolean,
        val onCheckedChange: (Boolean) -> Unit,
        val showInfoDialog: (() -> Unit)? = null,
        val shakeController: ShakeController? = null,
        val subPropertyContent: (@Composable () -> Unit)? = null,
        val modifier: Modifier = Modifier
    ) : CheckRowColumnElement {

        val hasSubProperties: Boolean
            get() = subPropertyContent != null

        companion object {
            fun <T : WidgetProperty> fromIsCheckedMap(
                property: T,
                isCheckedMap: MutableMap<T, Boolean>,
                allowCheckChange: (Boolean) -> Boolean = { true },
                onCheckedChangedDisallowed: () -> Unit = {},
                showInfoDialog: (() -> Unit)? = null,
                shakeController: ShakeController? = null,
                subPropertyContent: (@Composable () -> Unit)? = null,
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
                    showInfoDialog = showInfoDialog,
                    shakeController = shakeController,
                    subPropertyContent = subPropertyContent,
                    modifier = modifier,
                )
            }
        }
    }
}