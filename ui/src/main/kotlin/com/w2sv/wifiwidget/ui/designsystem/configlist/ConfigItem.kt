package com.w2sv.wifiwidget.ui.designsystem.configlist

import androidx.annotation.StringRes
import com.w2sv.domain.WithLabel

data class ConfigItem(
    val property: WithLabel,
    val isChecked: () -> Boolean,
    /** Returns false when the change is rejected and the row should show shake feedback. */
    val trySetChecked: (Boolean) -> Boolean,
    @StringRes val supportingTextRes: Int? = null,
    val showInfoDialog: (() -> Unit)? = null,
    val show: () -> Boolean = { true }
)

fun makeTrySetChecked(
    allowUpdate: (Boolean) -> Boolean,
    onUpdateDisallowed: () -> Unit = {},
    update: (Boolean) -> Unit
): (Boolean) -> Boolean =
    { checked ->
        if (allowUpdate(checked)) {
            update(checked)
            true
        } else {
            onUpdateDisallowed()
            false
        }
    }
