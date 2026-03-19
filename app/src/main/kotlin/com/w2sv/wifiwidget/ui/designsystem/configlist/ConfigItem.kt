package com.w2sv.wifiwidget.ui.designsystem.configlist

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import com.w2sv.domain.model.Labelled
import com.w2sv.wifiwidget.ui.util.ShakeController
import kotlinx.collections.immutable.ImmutableList

sealed interface ConfigItem {

    @Immutable
    data class Custom(val content: @Composable () -> Unit) : ConfigItem

    sealed interface Actionable : ConfigItem {
        val property: Labelled
        val show: () -> Boolean
        val shakeController: ShakeController?
        val contentBeneath: ContentBeneath?
        val modifier: Modifier

        fun isEnabled(): Boolean =
            when (this) {
                is WithCustomTrailing -> true
                is Checkable -> isChecked()
            }
    }

    data class WithCustomTrailing(
        override val property: Labelled,
        override val show: () -> Boolean = { true },
        override val shakeController: ShakeController? = null,
        override val contentBeneath: ContentBeneath? = null,
        override val modifier: Modifier = Modifier,
        val trailing: @Composable RowScope.() -> Unit
    ) : Actionable

    @Immutable
    data class Checkable(
        override val property: Labelled,
        override val show: () -> Boolean = { true },
        override val shakeController: ShakeController? = null,
        override val contentBeneath: ContentBeneath? = null,
        override val modifier: Modifier = Modifier,
        val isChecked: () -> Boolean,
        val onCheckedChange: (Boolean) -> Unit,
        val showInfoDialog: (() -> Unit)? = null
    ) : Actionable

    sealed interface ContentBeneath {
        val asSubSettingsOrNull get() = this as? SubSettings
    }

    @JvmInline
    value class Explanation(@StringRes val stringRes: Int) : ContentBeneath
    data class SubSettings(val elements: ImmutableList<ConfigItem>, val allowCollapsing: Boolean = true) : ContentBeneath
}

fun <T> makeOnCheckedChange(
    updateVetoReason: (Boolean) -> T? = { null },
    onVeto: (T) -> Unit = {},
    update: (Boolean) -> Unit
): (Boolean) -> Unit =
    { isCheckedNew ->
        updateVetoReason(isCheckedNew)?.let(onVeto) ?: update(isCheckedNew)
    }

fun makeOnCheckedChange(
    allowUpdate: (Boolean) -> Boolean,
    onUpdateDisallowed: () -> Unit = {},
    update: (Boolean) -> Unit
): (Boolean) -> Unit =
    makeOnCheckedChange(
        updateVetoReason = { isCheckedNew -> if (allowUpdate(isCheckedNew)) null else Unit },
        onVeto = { onUpdateDisallowed() },
        update = update
    )
