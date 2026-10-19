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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

/** Manages reversible configuration editing and persistence; widget side effects belong to [WidgetActions]. */
@HiltViewModel
class WidgetConfigScreenViewModel @Inject constructor(
    dataSource: WidgetConfigDataSource,
    widgetConfigSource: WidgetConfigSource,
    private val widgetActions: WidgetActions
) : ViewModel() {

    val changesHaveBeenCommitted: SharedFlow<Unit>
        field = MutableSharedFlow<Unit>()

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
            changesHaveBeenCommitted.emit(Unit)
        }
    )
}
