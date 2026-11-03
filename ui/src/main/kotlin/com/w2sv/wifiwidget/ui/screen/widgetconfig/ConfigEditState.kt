package com.w2sv.wifiwidget.ui.screen.widgetconfig

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.domain.widget.WidgetConfig
import kotlinx.coroutines.flow.Flow

@Stable
data class ConfigEditState(
    val isDirty: () -> Boolean,
    val editableConfig: () -> WidgetConfig,
    val revertChanges: () -> Unit,
    val commitChanges: () -> Unit,
    val changesHaveBeenCommitted: Flow<WidgetConfig>
)

@Composable
fun rememberConfigEditState(viewModel: WidgetConfigScreenViewModel): ConfigEditState {
    val isDirty by viewModel.reversibleConfig.isDirty.collectAsStateWithLifecycle()
    val editableConfig by viewModel.reversibleConfig.collectAsStateWithLifecycle()
    return remember(viewModel) {
        ConfigEditState(
            isDirty = { isDirty },
            editableConfig = { editableConfig },
            revertChanges = viewModel.reversibleConfig::revert,
            commitChanges = viewModel::applyChanges,
            changesHaveBeenCommitted = viewModel.changesHaveBeenCommitted
        )
    }
}
