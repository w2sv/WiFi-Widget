package com.w2sv.wifiwidget.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.common.utils.collectLatestFromFlow
import com.w2sv.common.utils.valueEnabledKeys
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.domain.model.WifiStatus
import com.w2sv.domain.repository.PermissionRepository
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.networking.WifiStatusMonitor
import com.w2sv.wifiwidget.ui.screens.home.components.wifistatus.model.WifiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import slimber.log.i
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    val permissionRepository: PermissionRepository,
    widgetRepository: WidgetRepository,
    wifiStatusMonitor: WifiStatusMonitor,
    widgetWifiPropertyViewDataFactory: WidgetWifiProperty.ViewData.Factory,
) : ViewModel() {

    fun onStart() {
        wifiStateEmitter.refreshPropertyViewDataIfConnected()
    }

    fun saveLocationAccessPermissionRequestLaunched() {
        viewModelScope.launch { permissionRepository.locationAccessPermissionRequested.save(true) }
    }

    fun saveLocationAccessRationalShown() {
        viewModelScope.launch { permissionRepository.locationAccessPermissionRationalShown.save(true) }
    }

    private val wifiStateEmitter = WifiStateEmitter(
        wifiPropertyEnablementMap = widgetRepository.wifiPropertyEnablementMap,
        ipSubPropertyEnablementMap = widgetRepository.ipSubPropertyEnablementMap,
        wifiStatusFlow = wifiStatusMonitor.wifiStatus,
        widgetWifiPropertyViewDataFactory = widgetWifiPropertyViewDataFactory,
        scope = viewModelScope
    )

    val wifiState by wifiStateEmitter::state
}

private class WifiStateEmitter(
    private val wifiPropertyEnablementMap: Map<WidgetWifiProperty, StateFlow<Boolean>>,
    private val ipSubPropertyEnablementMap: Map<WidgetWifiProperty.IP.SubProperty, StateFlow<Boolean>>,
    private val wifiStatusFlow: Flow<WifiStatus>,
    private val widgetWifiPropertyViewDataFactory: WidgetWifiProperty.ViewData.Factory,
    scope: CoroutineScope
) {
    val state: StateFlow<WifiState> get() = _state.asStateFlow()
    private val _state = MutableStateFlow<WifiState>(WifiState.Disconnected)

    private fun getPropertyViewData(): Flow<WidgetWifiProperty.ViewData> =
        widgetWifiPropertyViewDataFactory(
            properties = wifiPropertyEnablementMap.valueEnabledKeys,
            ipSubProperties = ipSubPropertyEnablementMap.valueEnabledKeys.toSet(),
        )

    private fun setState(wifiStatus: WifiStatus) {
        _state.value = when (wifiStatus) {
            WifiStatus.Disabled -> WifiState.Disabled
            WifiStatus.Disconnected -> WifiState.Disconnected
            WifiStatus.Connected -> WifiState.Connected(
                propertyViewData = getPropertyViewData()
            )
        }
            .also {
                i { "Set wifiState=$it" }
            }
    }

    fun refreshPropertyViewDataIfConnected() {
        if (state.value is WifiState.Connected) {
            _state.value = WifiState.Connected(getPropertyViewData())
        }
    }

    init {
        with(scope) {
            collectLatestFromFlow(wifiStatusFlow) { status ->
                i { "Collected WifiStatus=$status" }
                setState(status)
            }
            collectLatestFromFlow(
                (wifiPropertyEnablementMap.values + ipSubPropertyEnablementMap.values)
                    .merge()
            ) {
                i { "Refreshing on property enablement change" }
                refreshPropertyViewDataIfConnected()
            }
        }
    }
}