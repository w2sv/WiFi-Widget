package com.w2sv.wifiwidget.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.common.utils.log
import com.w2sv.domain.model.WifiStatus
import com.w2sv.domain.model.WifiViewData
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.networking.WifiStatusMonitor
import com.w2sv.wifiwidget.ui.screens.home.components.wifistatus.model.WifiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    widgetRepository: WidgetRepository,
    wifiStatusMonitor: WifiStatusMonitor,
    wifiViewDataFactory: WifiViewData.Factory
) : ViewModel() {

    /**
     * For computation of a new wifiState upon location access change.
     */
    private val locationAccessChanged = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    val wifiState = combine(
        wifiStatusMonitor.wifiStatus.distinctUntilChanged(),
        widgetRepository.sortedEnabledWifiProperties.distinctUntilChanged(),
        widgetRepository.enabledIpSubProperties.distinctUntilChanged(),
        widgetRepository.enabledLocationParameters.distinctUntilChanged(),
        locationAccessChanged
    ) { wifiStatus, enabledWifiProperties, enabledIpSubProperties, enabledLocationParameters, _ ->
        when (wifiStatus) {
            WifiStatus.Disabled -> WifiState.Disabled
            WifiStatus.Disconnected -> WifiState.Disconnected
            WifiStatus.Connected, WifiStatus.ConnectedInactive -> WifiState.Connected(
                wifiViewDataFlow = wifiViewDataFactory(
                    properties = enabledWifiProperties,
                    ipSubProperties = enabledIpSubProperties,
                    locationParameters = enabledLocationParameters
                )
            )
        }
            .log { "Set wifiState=$it" }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), WifiState.Disconnected)

    fun onLocationAccessChanged() {
        viewModelScope.launch { locationAccessChanged.emit(Unit) }
    }
}
