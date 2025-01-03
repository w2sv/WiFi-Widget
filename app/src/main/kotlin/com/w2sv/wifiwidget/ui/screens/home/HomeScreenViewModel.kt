package com.w2sv.wifiwidget.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.common.utils.log
import com.w2sv.domain.model.WifiProperty
import com.w2sv.domain.model.WifiStatus
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
    wifiPropertyViewDataFactory: WifiProperty.ViewData.Factory
) : ViewModel() {

    fun onLocationAccessChanged() {
        viewModelScope.launch { locationAccessChanged.emit(Unit) }
    }

    /**
     * For computation of a new wifiState upon locationAccessChange triggered through a snackbar action.
     */
    private val locationAccessChanged = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    val wifiState = combine(
        wifiStatusMonitor.wifiStatus.distinctUntilChanged(),
        widgetRepository.sortedEnabledWifiProperties,
        widgetRepository.enabledIpSubProperties,
        widgetRepository.enabledLocationParameters,
        locationAccessChanged
    ) { wifiStatus, enabledWifiProperties, enabledIpSubProperties, enabledLocationParameters, _ ->
        when (wifiStatus) {
            WifiStatus.Disabled -> WifiState.Disabled
            WifiStatus.Disconnected -> WifiState.Disconnected
            WifiStatus.Connected -> WifiState.Connected(
                viewDataFlow = wifiPropertyViewDataFactory(
                    properties = enabledWifiProperties,
                    getIpSubProperties = { enabledIpSubProperties },
                    getLocationParameters = { enabledLocationParameters }
                )
            )
        }
            .log { "Set wifiState=$it" }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), WifiState.Disconnected)
}
