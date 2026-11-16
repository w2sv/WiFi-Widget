package com.w2sv.wifiwidget.ui.screen.widgetconfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.domain.widget.WidgetActions
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetConfigDataSource
import com.w2sv.kotlinutils.coroutines.runOrHandle
import com.w2sv.reversiblestate.ReversibleStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import slimber.log.e

/** Manages reversible widget configuration editing and persistence. */
@HiltViewModel
class WidgetConfigScreenViewModel @Inject constructor(dataSource: WidgetConfigDataSource, private val widgetActions: WidgetActions) :
    ViewModel() {

    val reversibleConfig = ReversibleStateFlow(
        scope = viewModelScope,
        appliedState = dataSource.config.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = WidgetConfig.default
        ),
        commitState = { commit ->
            dataSource.update { commit.current }

            // Persistence has succeeded. Widget actions are best-effort
            // and must not cause a confirmed write to be retried.
            runOrHandle(onFailure = { failure -> e { "Failed applying widget configuration side effects: error=$failure" } }) {
                widgetActions.applyConfigurationChange(
                    previous = commit.previous,
                    current = commit.current
                )
            }
        }
    )

    val changesHaveBeenCommitted =
        reversibleConfig.commits.map { it.current }

    fun applyChanges() {
        viewModelScope.launch {
            runOrHandle(onFailure = { failure -> e { "Failed committing widget configuration: error=$failure" } }) {
                reversibleConfig.commit()
            }
        }
    }
}
