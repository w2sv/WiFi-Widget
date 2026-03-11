package com.w2sv.wifiwidget.ui.screen.home.model.wifistate

import com.w2sv.common.utils.log
import com.w2sv.domain.model.networking.WifiStatus
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewDataProvider
import com.w2sv.domain.repository.RemoteNetworkInfoRepository
import com.w2sv.domain.repository.WidgetConfigFlow
import com.w2sv.kotlinutils.coroutines.flow.collectLatestOn
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import com.w2sv.networking.wifistatus.monitor.WifiStatusMonitor
import com.w2sv.wifiwidget.ui.screen.home.model.gpsstatus.GpsStatusProvider
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@ViewModelScoped
class WifiStateProviderImpl @Inject constructor(
    wifiStatusMonitor: WifiStatusMonitor,
    widgetConfigFlow: WidgetConfigFlow,
    wifiPropertyViewDataProvider: WifiPropertyViewDataProvider,
    remoteNetworkInfoRepository: RemoteNetworkInfoRepository,
    gpsStatusProvider: GpsStatusProvider,
    private val scope: CoroutineScope
) : WifiStateProvider {

    // Shared flows to avoid multiple subscriptions
    private val sharedWifiStatus = wifiStatusMonitor.wifiStatus.shareIn(scope, SharingStarted.WhileSubscribed(), replay = 1)
    private val sharedConfig = widgetConfigFlow.shareIn(scope, SharingStarted.WhileSubscribed(), replay = 1)

    private val locationAccessChanged = MutableSharedFlow<Unit>(replay = 1)

    // Emits whenever location-dependent properties might need recomputation
    private val locationAccessChangedWhileDependentPropertiesEnabled: Flow<Unit> =
        combine(locationAccessChanged, sharedConfig) { _, config -> config }
            .filter { it.isAnyLocationAccessRequiringPropertyEnabled }
            .map { }
            .onStart { emit(Unit) }

    // Connected Wi-Fi state recomputation triggers:
    // - config changes
    // - remote network info changes
    // - location access changes while location dependent properties are enabled
    // - wifiStatus emits Connected
    private val connectedWifiState: Flow<WifiState.Connected> = combine(
        sharedConfig.distinctUntilChangedBy { it.properties to it.orderedEnabledProperties },
        remoteNetworkInfoRepository.data,
        sharedWifiStatus.filter { it.isConnected },
        locationAccessChangedWhileDependentPropertiesEnabled
    ) { config, remoteNetworkInfo, connectedStatus, _ ->
        WifiState.Connected(
            status = connectedStatus,
            propertyViewData = wifiPropertyViewDataProvider(
                enabledProperties = config.orderedEnabledProperties,
                enabledIpSettings = config::enabledIpSettings,
                remoteNetworkInfo = remoteNetworkInfo
            )
        )
    }

    override val wifiState: StateFlow<WifiState> = sharedWifiStatus
        .flatMapLatest { wifiStatus ->
            when (wifiStatus) {
                WifiStatus.Disabled -> flowOf(WifiState.Disabled)
                WifiStatus.Disconnected -> flowOf(WifiState.Disconnected)
                else -> connectedWifiState
            }
                .log { "Set wifiState=$it" }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(), WifiState.Disconnected)

    init {
        // Refresh RemoteNetworkInfo on config change or property refresh
        sharedWifiStatus
            .filter { it.isConnected }
            .combine(sharedConfig.distinctUntilChangedBy { it.properties }) { _, config -> config }
            .collectLatestOn(scope) {
                remoteNetworkInfoRepository.refresh()
            }

        // React to GPS enablement changes
        gpsStatusProvider
            .isEnabled
            .distinctUntilChanged()
            .collectOn(scope) { locationAccessChanged.emit(Unit) }
    }

    override fun onLocationAccessChanged() {
        scope.launch { locationAccessChanged.emit(Unit) }
    }
}
