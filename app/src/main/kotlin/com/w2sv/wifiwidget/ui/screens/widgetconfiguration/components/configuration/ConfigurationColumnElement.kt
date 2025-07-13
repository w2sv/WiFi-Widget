package com.w2sv.wifiwidget.ui.screens.widgetconfiguration.components.configuration

import androidx.annotation.StringRes
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

sealed interface ConfigurationColumnElement {

    @Immutable
    data class Custom(val content: @Composable () -> Unit) : ConfigurationColumnElement

    @Immutable
    data class CheckRow<T : WidgetProperty>(
        val property: T,
        @param:StringRes val explanation: Int? = null,
        val isChecked: () -> Boolean,
        val onCheckedChange: (Boolean) -> Unit,
        val show: () -> Boolean = { true },
        val showInfoDialog: (() -> Unit)? = null,
        val shakeController: ShakeController? = null,
        val subPropertyColumnElements: ImmutableList<ConfigurationColumnElement>? = null,
        val modifier: Modifier = Modifier
    ) : ConfigurationColumnElement {

        val leadingIconAndLabelColor: Color
            @Composable
            @ReadOnlyComposable
            get() = MaterialTheme.colorScheme.onBackground.orAlphaDecreasedIf(!isChecked())

        val hasSubProperties: Boolean
            get() = subPropertyColumnElements != null

        companion object {
            fun <T : WidgetProperty> fromIsCheckedMap(
                property: T,
                @StringRes explanation: Int? = null,
                isCheckedMap: MutableMap<T, Boolean>,
                allowCheckChange: (Boolean) -> Boolean = { true },
                onCheckedChangedDisallowed: () -> Unit = {},
                show: () -> Boolean = { true },
                showInfoDialog: (() -> Unit)? = null,
                shakeController: ShakeController? = null,
                subPropertyColumnElements: ImmutableList<ConfigurationColumnElement>? = null,
                modifier: Modifier = Modifier
            ): CheckRow<T> =
                CheckRow(
                    property = property,
                    explanation = explanation,
                    isChecked = { isCheckedMap.getValue(property) },
                    onCheckedChange = {
                        if (allowCheckChange(it)) {
                            isCheckedMap[property] = it
                        } else {
                            onCheckedChangedDisallowed()
                        }
                    },
                    show = show,
                    showInfoDialog = showInfoDialog,
                    shakeController = shakeController,
                    subPropertyColumnElements = subPropertyColumnElements,
                    modifier = modifier
                )
        }
    }
}
