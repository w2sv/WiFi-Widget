package com.w2sv.wifiwidget.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.coroutines.collectFromFlow
import com.w2sv.common.constants.Extra
import com.w2sv.common.utils.collectLatestFromFlow
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.domain.model.WifiStatus
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.networking.WifiStatusMonitor
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states.LocationAccessPermissionState
import com.w2sv.wifiwidget.ui.screens.home.components.wifistatus.model.WifiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.shareIn
import slimber.log.i
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    preferencesRepository: PreferencesRepository,
    wifiStatusMonitor: WifiStatusMonitor,
    private val widgetWifiPropertyValueViewDataFactory: WidgetWifiProperty.ValueViewData.Factory,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    fun onStart() {
        refreshWifiPropertyViewData()
    }

    val lapState = LocationAccessPermissionState(
        preferencesRepository = preferencesRepository,
        scope = viewModelScope,
    )

    private fun setWifiState(wifiStatus: WifiStatus) {
        _wifiState.value = WifiState(
            status = wifiStatus,
            propertyViewData = if (wifiStatus == WifiStatus.Connected)
                widgetWifiPropertyValueViewDataFactory(
                    properties = WidgetWifiProperty.entries,
                    ipSubPropertyEnablementMap = allIpSubPropertiesEnabled
                )
            else
                null
        )
            .also {
                i { "Set wifiState=$it" }
            }
    }

    private val wifiStatus = wifiStatusMonitor.wifiStatus.shareIn(
        viewModelScope,
        SharingStarted.Eagerly,
        1
    )

    val wifiState get() = _wifiState.asStateFlow()
    private val _wifiState = MutableStateFlow(WifiState(WifiStatus.Disconnected, null))

    private fun refreshWifiPropertyViewData() {
        if (wifiStatus.replayCache.lastOrNull() == WifiStatus.Connected) {
            setWifiState(WifiStatus.Connected)
        }
    }

    init {
        with(viewModelScope) {
            collectFromFlow(lapState.isGranted) {
                refreshWifiPropertyViewData()
            }
            collectLatestFromFlow(wifiStatus) { status ->
                i { "Collected WifiStatus=$status" }
                setWifiState(status)
            }
        }
    }

    val showWidgetConfigurationDialog get() = _showWidgetConfigurationDialog.asStateFlow()
    private val _showWidgetConfigurationDialog =
        MutableStateFlow(savedStateHandle.get<Boolean>(Extra.OPEN_WIDGET_CONFIGURATION_DIALOG) == true)

    fun setShowWidgetConfigurationDialog(value: Boolean) {
        _showWidgetConfigurationDialog.value = value
    }
}

private val allIpSubPropertiesEnabled = WidgetWifiProperty.IP.entries
    .flatMap { it.subProperties }
    .associateWith { true }