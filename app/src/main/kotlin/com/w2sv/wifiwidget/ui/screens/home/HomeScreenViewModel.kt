package com.w2sv.wifiwidget.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.w2sv.common.utils.log
import com.w2sv.datastoreutils.preferences.map.DataStoreFlowMap
import com.w2sv.domain.model.WifiProperty
import com.w2sv.domain.model.WifiStatus
import com.w2sv.domain.repository.WidgetRepository
import com.w2sv.networking.WifiStatusMonitor
import com.w2sv.wifiwidget.ui.screens.home.components.wifistatus.model.WifiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.collections.component1
import kotlin.collections.component2
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    widgetRepository: WidgetRepository,
    wifiStatusMonitor: WifiStatusMonitor,
    wifiPropertyViewDataFactory: WifiProperty.ViewData.Factory
) : ViewModel() {

    val wifiState = combine(
        wifiStatusMonitor.wifiStatus.distinctUntilChanged(),
        widgetRepository.wifiPropertyEnablementMap.enabledKeysFlow().distinctUntilChanged(),
        widgetRepository.ipSubPropertyEnablementMap.enabledKeysFlow().distinctUntilChanged()
    ) { wifiStatus, enabledWifiProperties, enabledIpSubProperties ->
        when (wifiStatus) {
            WifiStatus.Disabled -> WifiState.Disabled
            WifiStatus.Disconnected -> WifiState.Disconnected
            WifiStatus.Connected -> WifiState.Connected(
                viewDataFlow = wifiPropertyViewDataFactory(
                    properties = enabledWifiProperties,
                    ipSubProperties = enabledIpSubProperties.toSet()
                )
            )
        }
            .log { "Set wifiState=$it" }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), WifiState.Disconnected)
}

private fun <K> DataStoreFlowMap<K, Boolean>.enabledKeysFlow(): Flow<List<K>> =
    combine(
        entries.map { (k, v) ->
            v.map { k to it }
        }
    ) {
        buildList {
            it.forEach { (k, isEnabled) ->
                if (isEnabled) {
                    add(k)
                }
            }
        }
    }
