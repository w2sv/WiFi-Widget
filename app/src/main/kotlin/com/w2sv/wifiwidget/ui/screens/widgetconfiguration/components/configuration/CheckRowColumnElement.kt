package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.configuration

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.w2sv.domain.model.WidgetProperty
import com.w2sv.wifiwidget.ui.utils.ShakeController
import com.w2sv.wifiwidget.ui.utils.orAlphaDecreasedIf
import kotlinx.collections.immutable.ImmutableList

sealed interface CheckRowColumnElement {

    @Immutable
    @JvmInline
    value class Custom(val content: @Composable () -> Unit) : CheckRowColumnElement

    @Immutable
    data class CheckRow<T : WidgetProperty>(
        val property: T,
        val isChecked: () -> Boolean,
        val onCheckedChange: (Boolean) -> Unit,
        val showInfoDialog: (() -> Unit)? = null,
        val shakeController: ShakeController? = null,
        val subPropertyColumnElements: ImmutableList<CheckRowColumnElement>? = null,
        val modifier: Modifier = Modifier
    ) : CheckRowColumnElement {

        val leadingIconAndLabelColor: Color
            @Composable
            @ReadOnlyComposable
            get() = MaterialTheme.colorScheme.onBackground.orAlphaDecreasedIf(!isChecked())

        val hasSubProperties: Boolean
            get() = subPropertyColumnElements != null

        companion object {
            fun <T : WidgetProperty> fromIsCheckedMap(
                property: T,
                isCheckedMap: MutableMap<T, Boolean>,
                allowCheckChange: (Boolean) -> Boolean = { true },
                onCheckedChangedDisallowed: () -> Unit = {},
                showInfoDialog: (() -> Unit)? = null,
                shakeController: ShakeController? = null,
                subPropertyColumnElements: ImmutableList<CheckRowColumnElement>? = null,
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
                    subPropertyColumnElements = subPropertyColumnElements,
                    modifier = modifier
                )
            }
        }
    }
}
