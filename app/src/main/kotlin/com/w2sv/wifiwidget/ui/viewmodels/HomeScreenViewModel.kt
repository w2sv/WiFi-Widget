package com.w2sv.wifiwidget.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.common.constants.Extra
import com.w2sv.common.utils.collectLatestFromFlow
import com.w2sv.common.utils.enabledKeys
import com.w2sv.common.utils.stateIn
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.domain.model.WifiStatus
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.networking.WifiStatusMonitor
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.LocationAccessPermissionState
import com.w2sv.wifiwidget.ui.screens.home.components.wifistatus.model.WifiState
import com.w2sv.wifiwidget.ui.utils.SHARING_STARTED_WHILE_SUBSCRIBED_TIMEOUT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.shareIn
import slimber.log.i
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    preferencesRepository: PreferencesRepository,
    widgetRepository: WidgetRepository,
    wifiStatusMonitor: WifiStatusMonitor,
    widgetWifiPropertyViewDataFactory: WidgetWifiProperty.ViewData.Factory,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    fun onStart() {
        wifiStateEmitter.refreshPropertyViewDataIfConnected()
    }

    val lapState = LocationAccessPermissionState(
        preferencesRepository = preferencesRepository,
        scope = viewModelScope,
    )

    private val wifiStateEmitter = WifiStateEmitter(
        wifiPropertyEnablementMap = widgetRepository.getWifiPropertyEnablementMap().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SHARING_STARTED_WHILE_SUBSCRIBED_TIMEOUT),
            true
        ),
        ipSubPropertyEnablementMap = widgetRepository.getIPSubPropertyEnablementMap().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(SHARING_STARTED_WHILE_SUBSCRIBED_TIMEOUT),
            true
        ),
        wifiStatusFlow = wifiStatusMonitor.wifiStatus.shareIn(
            viewModelScope,
            SharingStarted.Eagerly
        ),
        widgetWifiPropertyViewDataFactory = widgetWifiPropertyViewDataFactory,
        scope = viewModelScope
    )

    val wifiState by wifiStateEmitter::state

    val showWidgetConfigurationDialog get() = _showWidgetConfigurationDialog.asStateFlow()
    private val _showWidgetConfigurationDialog =
        MutableStateFlow(savedStateHandle.get<Boolean>(Extra.OPEN_WIDGET_CONFIGURATION_DIALOG) == true)

    fun setShowWidgetConfigurationDialog(value: Boolean) {
        _showWidgetConfigurationDialog.value = value
    }
}

class WifiStateEmitter(
    private val wifiPropertyEnablementMap: Map<WidgetWifiProperty, StateFlow<Boolean>>,
    private val ipSubPropertyEnablementMap: Map<WidgetWifiProperty.IP.SubProperty, StateFlow<Boolean>>,
    private val wifiStatusFlow: SharedFlow<WifiStatus>,
    private val widgetWifiPropertyViewDataFactory: WidgetWifiProperty.ViewData.Factory,
    scope: CoroutineScope
) {
    val state: StateFlow<WifiState> get() = _state.asStateFlow()
    private val _state = MutableStateFlow<WifiState>(WifiState.Disconnected)

    private fun getPropertyViewData(): Flow<WidgetWifiProperty.ViewData> =
        widgetWifiPropertyViewDataFactory(
            properties = wifiPropertyEnablementMap.enabledKeys,
            ipSubProperties = ipSubPropertyEnablementMap.enabledKeys.toSet(),
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