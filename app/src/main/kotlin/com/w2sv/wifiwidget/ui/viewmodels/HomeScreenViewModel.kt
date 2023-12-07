package com.w2sv.wifiwidget.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.coroutines.collectFromFlow
import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.domain.model.WifiStatus
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.networking.WifiStatusMonitor
import com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.LocationAccessPermissionState
import com.w2sv.wifiwidget.ui.screens.home.components.wifistatus.model.WifiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import slimber.log.i
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    preferencesRepository: PreferencesRepository,
    @ApplicationContext context: Context,
    private val widgetWifiPropertyValueViewDataFactory: WidgetWifiProperty.ValueViewData.Factory,
    wifiStatusMonitor: WifiStatusMonitor,
) : ViewModel() {

    fun onStart() {
        triggerWifiPropertiesViewDataRefresh()
        lapState.updateBackgroundAccessGranted()
    }

    val showWidgetConfigurationDialog get() = _showWidgetConfigurationDialog.asStateFlow()
    private val _showWidgetConfigurationDialog = MutableStateFlow(false)

    fun setShowWidgetConfigurationDialog(value: Boolean) {
        _showWidgetConfigurationDialog.value = value
    }

    val lapState = LocationAccessPermissionState(
        preferencesRepository = preferencesRepository,
        scope = viewModelScope,
        context = context,
    )
        .apply {
            viewModelScope.collectFromFlow(newlyGranted) {
                triggerWifiPropertiesViewDataRefresh()
            }
        }

    private val wifiStatus = wifiStatusMonitor.wifiStatus.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        WifiStatus.Disconnected
    )

    val wifiState get() = _wifiState.asStateFlow()
    private val _wifiState = MutableStateFlow(WifiState(WifiStatus.Disconnected, null))
        .apply {
            viewModelScope.launch {
                wifiStatus.collect {
                    i { "Collected WifiStatus=$it" }
                    value = WifiState(
                        status = it,
                        propertyViewData = if (it == WifiStatus.Connected)
                            getWifiPropertyViewData()
                        else
                            null
                    ).also { i { "Emitted $it" } }
                }
            }
        }

    private fun triggerWifiPropertiesViewDataRefresh() {
        if (wifiStatus.value == WifiStatus.Connected) {
            _wifiState.value = WifiState(
                status = WifiStatus.Connected,
                propertyViewData = getWifiPropertyViewData()
            )
        }
    }

    private fun getWifiPropertyViewData(): Flow<WidgetWifiProperty.ValueViewData> =
        widgetWifiPropertyValueViewDataFactory(WidgetWifiProperty.entries)
}
