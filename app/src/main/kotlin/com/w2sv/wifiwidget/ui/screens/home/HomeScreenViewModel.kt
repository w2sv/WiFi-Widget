package com.w2sv.wifiwidget.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.common.utils.log
import com.w2sv.domain.model.WifiStatus
import com.w2sv.domain.model.WifiViewData
import com.w2sv.domain.repository.RemoteNetworkInfoRepository
import com.w2sv.domain.repository.WidgetConfigRepository
import com.w2sv.kotlinutils.coroutines.flow.combineToTriple
import com.w2sv.networking.wifistatus.WifiStatusMonitor
import com.w2sv.wifiwidget.ui.screens.home.components.wifistatus.model.WifiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    widgetConfigRepository: WidgetConfigRepository,
    wifiStatusMonitor: WifiStatusMonitor,
    wifiViewDataProvider: WifiViewData.Provider,
    remoteNetworkInfoRepository: RemoteNetworkInfoRepository
) : ViewModel() {

    /**
     * For computation of a new wifiState upon location access change.
     */
    private val locationAccessChanged = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

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

    // TODO trigger on locationAccessChanged
    private val connectedWifiState: Flow<WifiState.Connected> = combineToTriple(
        widgetConfigRepository.sortedEnabledWifiProperties.distinctUntilChanged(),
        widgetConfigRepository.enabledIpSubProperties.distinctUntilChanged(),
        widgetConfigRepository.enabledLocationParameters.distinctUntilChanged()
    )
        .flatMapLatest { (enabledWifiProperties, enabledIpSubProperties, enabledLocationParameters) ->
            flow {
                // Refresh remote data
                remoteNetworkInfoRepository.refresh()

                // Emit the connected state with freshly fetched data
                val remoteNetworkInfo = remoteNetworkInfoRepository.data.first()
                emit(
                    WifiState.Connected(
                        wifiViewData = wifiViewDataProvider(
                            properties = enabledWifiProperties,
                            ipSubProperties = enabledIpSubProperties,
                            remoteNetworkInfo = remoteNetworkInfo
                        )
                    )
                )
            }
        }

    fun onLocationAccessChanged() {
        viewModelScope.launch { locationAccessChanged.emit(Unit) }
    }
}
