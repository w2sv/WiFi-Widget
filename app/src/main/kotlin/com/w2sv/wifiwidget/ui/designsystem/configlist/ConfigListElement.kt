package com.w2sv.wifiwidget.ui.designsystem.configlist

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
    data class CheckRow(
        val property: Labelled,
        @StringRes val explanation: Int? = null,
        val isChecked: () -> Boolean,
        val onCheckedChange: (Boolean) -> Unit,
        val show: () -> Boolean = { true },
        val showInfoDialog: (() -> Unit)? = null,
        val shakeController: ShakeController? = null,
        val subSettings: ImmutableList<ConfigListElement>? = null,
        val allowSubSettingCollapsing: Boolean = true,
        val modifier: Modifier = Modifier
    ) : ConfigListElement {

        val leadingIconAndLabelColor: Color
            @Composable
            @ReadOnlyComposable
            get() = MaterialTheme.colorScheme.onBackground.orAlphaDecreasedIf(!isChecked())

        val hasSubSettings: Boolean
            get() = subSettings != null
    }
}

fun makeOnCheckedChange(
    allow: (Boolean) -> Boolean = { true },
    onDisallowed: () -> Unit = {},
    update: (Boolean) -> Unit
): (Boolean) -> Unit =
    {
        if (allow(it)) {
            update(it)
        } else {
            onDisallowed()
        }
    }
