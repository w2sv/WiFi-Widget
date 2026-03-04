package com.w2sv.wifiwidget.ui.screen.widgetconfig

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow

@Stable
data class ConfigEditState(
    val isDirty: () -> Boolean,
    val revertChanges: () -> Unit,
    val commitChanges: () -> Unit,
    val changesHaveBeenCommitted: Flow<Unit>
)

@Composable
fun rememberConfigEditState(viewModel: WidgetConfigScreenViewModel): ConfigEditState {
    val isDirty by viewModel.reversibleConfig.isDirty.collectAsStateWithLifecycle()
    return remember(viewModel) {
        ConfigEditState(
            isDirty = { isDirty },
            revertChanges = viewModel.reversibleConfig::revert,
            commitChanges = viewModel.reversibleConfig::launchCommit,
            changesHaveBeenCommitted = viewModel.changesHaveBeenCommitted
        )
    }
}
