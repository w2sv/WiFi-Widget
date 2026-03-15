package com.w2sv.wifiwidget.ui.designsystem.configlist

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import com.w2sv.domain.model.Labelled
import com.w2sv.wifiwidget.ui.util.ShakeController
import kotlinx.collections.immutable.ImmutableList

sealed interface ConfigItem {

    @Immutable
    data class Custom(val content: @Composable () -> Unit) : ConfigItem

    @Immutable
    data class Checkable(
        val property: Labelled,
        val isChecked: () -> Boolean,
        val onCheckedChange: (Boolean) -> Unit,

        val show: () -> Boolean = { true },
        val showInfoDialog: (() -> Unit)? = null,
        val shakeController: ShakeController? = null,

        val contentBeneath: Beneath? = null,

        val modifier: Modifier = Modifier
    ) : ConfigItem

    sealed interface Beneath {

        val asSubSettingsOrNull get() = this as? SubSettings

        @JvmInline
        value class Explanation(@StringRes val stringRes: Int) : Beneath
        data class SubSettings(val elements: ImmutableList<ConfigItem>, val allowCollapsing: Boolean = true) : Beneath
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
