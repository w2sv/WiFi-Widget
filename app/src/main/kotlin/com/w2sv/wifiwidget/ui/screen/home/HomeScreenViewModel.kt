package com.w2sv.wifiwidget.ui.screen.home

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.common.utils.log
import com.w2sv.core.common.R
import com.w2sv.domain.model.networking.WifiStatus
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewDataProvider
import com.w2sv.domain.repository.RemoteNetworkInfoRepository
import com.w2sv.domain.repository.WidgetConfigDataSource
import com.w2sv.networking.wifistatus.WifiStatusMonitor
import com.w2sv.widget.di.WidgetPinSuccessFlow
import com.w2sv.widget.utils.attemptWifiWidgetPin
import com.w2sv.wifiwidget.ui.designsystem.AppSnackbarVisuals
import com.w2sv.wifiwidget.ui.designsystem.SnackbarKind
import com.w2sv.wifiwidget.ui.screen.home.wifistatus.model.WifiState
import com.w2sv.wifiwidget.ui.util.snackbar.EmitSnackbarBuilder
import com.w2sv.wifiwidget.ui.util.snackbar.SnackbarBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    wifiStatusMonitor: WifiStatusMonitor,
    wifiPropertyViewDataProvider: WifiPropertyViewDataProvider,
    remoteNetworkInfoRepository: RemoteNetworkInfoRepository,
    private val widgetConfigDataSource: WidgetConfigDataSource,
    private val appWidgetManager: AppWidgetManager,
    private val emitSnackbarBuilder: EmitSnackbarBuilder,
    @WidgetPinSuccessFlow val widgetPinSuccessFlow: SharedFlow<Unit>,
    @ApplicationContext val context: Context,
    val snackbarBuilderFlow: SharedFlow<@JvmSuppressWildcards SnackbarBuilder>
) : ViewModel() {

    /**
     * For computation of a new wifiState upon location access change.
     */
    private val locationAccessChanged = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    val isAnyLocationAccessRequiringPropertyEnabled: Boolean get() = _isAnyLocationAccessRequiringPropertyEnabled.value
    private val _isAnyLocationAccessRequiringPropertyEnabled = widgetConfigDataSource.config
        .map { it.isAnyLocationAccessRequiringPropertyEnabled }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            false
        )

    val wifiState: StateFlow<WifiState> = wifiStatusMonitor.wifiStatus
        .flatMapLatest { wifiStatus ->
            when (wifiStatus) {
                WifiStatus.Disabled -> flowOf(WifiState.Disabled)
                WifiStatus.Disconnected -> flowOf(WifiState.Disconnected)
                WifiStatus.Connected, WifiStatus.ConnectedInactive -> connectedWifiState
            }
                .log { "Set wifiState=$it" }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), WifiState.Disconnected)

    // TODO trigger on locationAccessChange
    private val connectedWifiState: Flow<WifiState.Connected> = widgetConfigDataSource.config.flatMapLatest { config ->
        flow {
            // Refresh remote data
            remoteNetworkInfoRepository.refresh()

            // Emit the connected state with freshly fetched data
            val remoteNetworkInfo = remoteNetworkInfoRepository.data.first()
            emit(
                WifiState.Connected(
                    propertyViewData = wifiPropertyViewDataProvider(
                        enabledProperties = config.orderedEnabledProperties(),
                        enabledIpSettings = config::enabledIpSettings,
                        remoteNetworkInfo = remoteNetworkInfo
                    )
                )
            )
        }
    }

    fun pinWidget() {
        appWidgetManager.attemptWifiWidgetPin(
            context = context,
            onFailure = {
                viewModelScope.launch {
                    emitSnackbarBuilder {
                        AppSnackbarVisuals(
                            msg = getString(R.string.widget_pinning_failed),
                            kind = SnackbarKind.Warning
                        )
                    }
                }
            }
        )
    }

    fun onLocationAccessChanged() {
        viewModelScope.launch { locationAccessChanged.emit(Unit) }
    }

    fun enableLocationAccessRequiringProperties() {
        viewModelScope.launch { widgetConfigDataSource.update { it.withEnabledLocationAccessRequiringProperties() } }
    }
}
