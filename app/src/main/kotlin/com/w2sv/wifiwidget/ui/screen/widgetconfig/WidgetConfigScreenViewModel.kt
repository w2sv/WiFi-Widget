package com.w2sv.wifiwidget.ui.screen.widgetconfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.repository.WidgetConfigDataSource
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import com.w2sv.reversiblestate.ReversibleStateFlow
import com.w2sv.widget.actions.WidgetActions
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import slimber.log.i

@HiltViewModel
class WidgetConfigScreenViewModel @Inject constructor(dataSource: WidgetConfigDataSource, private val widgetActions: WidgetActions) :
    ViewModel() {

    init {
        // Apply new refreshing settings
        dataSource.config
            .map { it.refreshing }
            .distinctUntilChanged()
            .drop(1)
            .collectOn(viewModelScope) { widgetActions.applyRefreshing(it) }
    }

    private val _changesHaveBeenCommitted = MutableSharedFlow<Unit>()
    val changesHaveBeenCommitted = _changesHaveBeenCommitted.asSharedFlow()

    val reversibleConfig = ReversibleStateFlow(
        scope = viewModelScope,
        appliedState = dataSource.config.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = WidgetConfig.default
        ),
        commitState = { state ->
            dataSource.update { state }
            _changesHaveBeenCommitted.emit(Unit)
            widgetActions.refresh()
            i { "Triggered widget data refresh on widget config change" }
        }
    )
}
