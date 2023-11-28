package com.w2sv.wifiwidget.ui.screens.home.components.wifistatus

import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.domain.model.WifiStatus
import com.w2sv.networking.WifiStatusMonitor
import com.w2sv.wifiwidget.ui.utils.SHARING_STARTED_WHILE_SUBSCRIBED_TIMEOUT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

class WifiStatusUIState(
    private val widgetWifiPropertyValueViewDataFactory: WidgetWifiProperty.ValueViewData.Factory,
    wifiStatusMonitor: WifiStatusMonitor,
    private val scope: CoroutineScope,
) {
    val propertiesViewData get() = _propertiesViewData.asStateFlow()
    private val _propertiesViewData =
        MutableStateFlow<List<WidgetWifiProperty.ValueViewData>?>(null)

    fun triggerPropertiesViewDataRefresh() {
        scope.launch {
            _propertiesViewData.value =
                widgetWifiPropertyValueViewDataFactory(WidgetWifiProperty.entries).toList()
        }
    }

    val status = wifiStatusMonitor.wifiStatus.stateIn(
        scope,
        SharingStarted.WhileSubscribed(SHARING_STARTED_WHILE_SUBSCRIBED_TIMEOUT),
        WifiStatus.Disabled,
    )
        .apply {
            scope.launch {
                collectLatest { status ->
                    if (status == WifiStatus.Connected) {
                        triggerPropertiesViewDataRefresh()
                    } else {
                        _propertiesViewData.value = null
                    }
                }
            }
            scope.launch {
                wifiStatusMonitor.wifiPropertiesHaveChanged.collectLatest {
                    if (value == WifiStatus.Connected) {
                        triggerPropertiesViewDataRefresh()
                    }
                }
            }
        }
}
