package com.w2sv.wifiwidget.ui.screen.widgetconfig

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.common.utils.log
import com.w2sv.domain.model.widget.WifiWidgetConfig
import com.w2sv.domain.model.wifiproperty.WifiProperty
import com.w2sv.domain.repository.WidgetConfigDataSource
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class WidgetConfigScreenViewModel @Inject constructor(
    dataSource: WidgetConfigDataSource,
    private val widgetRefreshManager: WifiWidgetRefreshManager,
    private val emitSnackbarBuilder: EmitSnackbarBuilder,
    @ApplicationContext context: Context,
    val snackbarBuilderFlow: SharedFlow<@JvmSuppressWildcards SnackbarBuilder>
) : ViewModel() {

    val reversibleConfig = ReversibleStateFlow(
        scope = viewModelScope,
        appliedStateFlow = dataSource.config.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = WifiWidgetConfig.default
        ),
        syncState = {
            withContext(Dispatchers.IO) {
                dataSource.update { it }
                WifiWidgetProvider.triggerDataRefresh(context).log { "Triggered widget data refresh on configuration state sync" }
            }
            emitSnackbarBuilder {
                AppSnackbarVisuals(
                    msg = getString(R.string.updated_widget_configuration),
                    kind = SnackbarKind.Success
                )
            }
        }
    )

    fun saveChanges() {
        viewModelScope.launch { reversibleConfig.sync() }
    }
}
