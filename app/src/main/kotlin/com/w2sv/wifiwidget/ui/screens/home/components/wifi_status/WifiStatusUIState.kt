package com.w2sv.wifiwidget.ui.screens.home.components.wifi_status

import com.w2sv.data.model.WifiProperty
import com.w2sv.data.model.WifiStatus
import com.w2sv.data.networking.WifiStatusMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WifiStatusUIState(
    private val wifiPropertyValueGetterResourcesProvider: WifiProperty.ValueGetterResources.Provider,
    wifiStatusMonitor: WifiStatusMonitor,
    private val scope: CoroutineScope
) {
    val status = wifiStatusMonitor.wifiStatus.stateIn(
        scope,
        SharingStarted.WhileSubscribed(),
        WifiStatus.Disabled
    )
        .apply {
            scope.launch {
                collectLatest { status ->
                    _propertiesViewData.value =
                        if (status == WifiStatus.Connected) {
                            getPropertiesViewData()
                        } else {
                            null
                        }
                }
            }
            scope.launch {
                wifiStatusMonitor.wifiPropertiesHaveChanged.collectLatest {
                    if (value == WifiStatus.Connected) {
                        refreshPropertiesViewData()
                    }
                }
            }
        }

    fun triggerPropertiesViewDataRefresh() {
        scope.launch {
            refreshPropertiesViewData()
        }
    }

    val propertiesViewData get() = _propertiesViewData.asStateFlow()
    private var _propertiesViewData = MutableStateFlow<List<WifiPropertyViewData>?>(null)

    private fun refreshPropertiesViewData() {
        _propertiesViewData.value = getPropertiesViewData()
    }

    private fun getPropertiesViewData(): List<WifiPropertyViewData> {
        val valueGetterResources =
            wifiPropertyValueGetterResourcesProvider.provide()
        return WifiProperty.values().map { property ->
            WifiPropertyViewData(
                property,
                property.getValue(valueGetterResources)
            )
        }
    }
}