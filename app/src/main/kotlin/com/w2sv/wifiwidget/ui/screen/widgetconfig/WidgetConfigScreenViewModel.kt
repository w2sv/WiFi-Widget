package com.w2sv.wifiwidget.ui.screen.widgetconfig

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.common.utils.log
import com.w2sv.domain.model.widget.WifiWidgetConfig
import com.w2sv.domain.repository.WidgetConfigDataSource
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import com.w2sv.reversiblestate.ReversibleStateFlow
import com.w2sv.widget.WifiWidgetProvider
import com.w2sv.widget.WifiWidgetRefreshManager
import com.w2sv.wifiwidget.R
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.util.EmitSnackbarBuilder
import com.w2sv.wifiwidget.ui.util.SnackbarBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class WidgetConfigScreenViewModel @Inject constructor(
    dataSource: WidgetConfigDataSource,
    private val widgetRefreshManager: WifiWidgetRefreshManager,
    private val emitSnackbarBuilder: EmitSnackbarBuilder,
    @ApplicationContext context: Context,
    val snackbarBuilderFlow: SharedFlow<@JvmSuppressWildcards SnackbarBuilder>
) : ViewModel() {

    init {
        // Apply new refreshing settings
        dataSource.config.map { it.refreshing }.distinctUntilChanged().drop(1).collectOn(viewModelScope) { refreshing ->
            widgetRefreshManager.applyRefreshingSettings(refreshing)
        }
    }

    val reversibleConfig = ReversibleStateFlow(
        scope = viewModelScope,
        appliedState = dataSource.config.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = WifiWidgetConfig.default
        ),
        commitState = { state ->
            dataSource.update { state }
            WifiWidgetProvider.triggerDataRefresh(context).log { "Triggered widget data refresh on configuration state sync" }
            emitSnackbarBuilder {
                AppSnackbarVisuals(
                    msg = getString(R.string.updated_widget_configuration),
                    kind = SnackbarKind.Success
                )
            }
        }
    )
}
