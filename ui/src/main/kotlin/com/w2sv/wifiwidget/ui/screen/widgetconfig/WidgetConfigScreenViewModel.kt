package com.w2sv.wifiwidget.ui.screen.widgetconfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.domain.widget.WidgetActions
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetConfigDataSource
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.reversiblestate.ReversibleStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import slimber.log.e

/** Manages reversible configuration editing and persistence; widget side effects belong to [WidgetActions]. */
@HiltViewModel
class WidgetConfigScreenViewModel @Inject constructor(
    dataSource: WidgetConfigDataSource,
    private val widgetConfigSource: WidgetConfigSource,
    private val widgetActions: WidgetActions
) : ViewModel() {
    private val commitMutex = Mutex()

    val changesHaveBeenCommitted: SharedFlow<WidgetConfig>
        field = MutableSharedFlow<WidgetConfig>()

    val reversibleConfig = ReversibleStateFlow(
        scope = viewModelScope,
        appliedState = dataSource.config.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = WidgetConfig.default
        ),
        commitState = { state ->
            val previous = widgetConfigSource.snapshot()
            dataSource.update { state }
            widgetActions.applyConfigurationChange(previous, state)
            changesHaveBeenCommitted.emit(state)
        },
        autoSyncWithAppliedState = false
    )

    init {
        viewModelScope.launch {
            var previousApplied = reversibleConfig.appliedState.value
            reversibleConfig.appliedState.collect { currentApplied ->
                // A DataStore emission from an earlier Apply must not replace edits made while it was pending.
                if (reversibleConfig.value == previousApplied) reversibleConfig.value = currentApplied
                previousApplied = currentApplied
            }
        }
    }

    /** Applies pending changes and their widget side effects. */
    fun applyChanges() {
        viewModelScope.launch {
            try {
                commitMutex.withLock { commitChangesIfNeeded() }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                e { "Failed committing widget configuration: error=$failure" }
            }
        }
    }

    /** Commits only when the editable configuration differs from the persisted configuration. */
    private suspend fun commitChangesIfNeeded() {
        if (reversibleConfig.isDirty.value) {
            reversibleConfig.commit()
        }
    }
}
