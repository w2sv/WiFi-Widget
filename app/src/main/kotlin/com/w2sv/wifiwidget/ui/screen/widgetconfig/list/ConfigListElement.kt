package com.w2sv.wifiwidget.ui.screen.widgetconfig.list

import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.w2sv.domain.model.Labelled
import com.w2sv.wifiwidget.ui.util.ShakeController
import com.w2sv.wifiwidget.ui.util.orAlphaDecreasedIf
import kotlinx.collections.immutable.ImmutableList

sealed interface ConfigListElement {

    @Immutable
    data class Custom(val content: @Composable () -> Unit) : ConfigListElement

    @Immutable
    data class CheckRow<T : Labelled>(
        val property: T,
        @StringRes val explanation: Int? = null,
        val isChecked: () -> Boolean,
        val onCheckedChange: (Boolean) -> Unit,
        val show: () -> Boolean = { true },
        val showInfoDialog: (() -> Unit)? = null,
        val shakeController: ShakeController? = null,
        val subPropertyColumnElements: ImmutableList<ConfigListElement>? = null,
        val modifier: Modifier = Modifier
    ) : ConfigListElement {

        val leadingIconAndLabelColor: Color
            @Composable
            @ReadOnlyComposable
            get() = MaterialTheme.colorScheme.onBackground.orAlphaDecreasedIf(!isChecked())

        val hasSubProperties: Boolean
            get() = subPropertyColumnElements != null
    }
}

fun makeOnCheckedChange(
    allowCheckChange: (Boolean) -> Boolean = { true },
    onCheckedChangedDisallowed: () -> Unit = {},
    update: (Boolean) -> Unit
): (Boolean) -> Unit = {
    if (allowCheckChange(it)) {
        update(it)
    } else {
        onCheckedChangedDisallowed()
    }
}
