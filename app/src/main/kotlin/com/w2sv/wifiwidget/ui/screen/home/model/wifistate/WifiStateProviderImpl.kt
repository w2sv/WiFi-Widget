package com.w2sv.wifiwidget.ui.screen.home.model.wifistate

import com.hoc081098.flowext.withLatestFrom
import com.w2sv.common.utils.logOnCancellation
import com.w2sv.common.utils.logOnEach
import com.w2sv.common.utils.refreshOn
import com.w2sv.domain.model.networking.WifiStatus
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewDataProvider
import com.w2sv.domain.repository.RemoteNetworkInfoRepository
import com.w2sv.domain.repository.WidgetConfigFlow
import com.w2sv.kotlinutils.coroutines.flow.collectLatestOn
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import com.w2sv.networking.wifistatus.monitor.WifiStatusMonitor
import com.w2sv.wifiwidget.ui.screen.home.model.gpsstatus.LocationEnabledProvider
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
import slimber.log.i

@ViewModelScoped
class WifiStateProviderImpl @Inject constructor(
    wifiStatusMonitor: WifiStatusMonitor,
    widgetConfigFlow: WidgetConfigFlow,
    wifiPropertyViewDataProvider: WifiPropertyViewDataProvider,
    remoteNetworkInfoRepository: RemoteNetworkInfoRepository,
    locationEnabledProvider: LocationEnabledProvider,
    private val scope: CoroutineScope
) : WifiStateProvider {

    // Shared flows to avoid multiple subscriptions
    private val connectedWifiStatus = wifiStatusMonitor.wifiStatus
        .filter { it.isConnected }
        .shareIn(scope, SharingStarted.WhileSubscribed(), replay = 1)
        .logOnEach("connectedWifiStatus")

    private val locationAccessChanged = MutableSharedFlow<Unit>()

    // Emits whenever location-dependent properties might need recomputation
    private val locationAccessChangedWhileDependentPropertiesEnabled: Flow<Unit> = locationAccessChanged
        .withLatestFrom(widgetConfigFlow) { _, config -> config }
        .filter { it.isAnyLocationAccessRequiringPropertyEnabled }
        .map { }
        .onStart { emit(Unit) }
        .logOnEach("locationAccessChangedWhileDependentPropertiesEnabled")

    // Connected Wi-Fi state recomputation triggers:
    // - config changes
    // - remote network info changes
    // - location access changes while location dependent properties are enabled
    // - wifiStatus emits Connected
    private val connectedWifiState: Flow<WifiState.Connected> = combine(
        widgetConfigFlow.distinctUntilChangedBy { it.properties to it.orderedEnabledProperties }.logOnEach("sharedConfig"),
        remoteNetworkInfoRepository.data.logOnEach("remoteNetworkInfoData"),
        connectedWifiStatus,
        locationAccessChangedWhileDependentPropertiesEnabled
    ) { config, remoteNetworkInfo, connectedStatus, _ ->
        i { "Computing connectedWifiState for $connectedStatus $remoteNetworkInfo $config" }
        WifiState.Connected(
            status = connectedStatus,
            propertyViewData = wifiPropertyViewDataProvider(
                enabledProperties = config.orderedEnabledProperties,
                enabledIpSettings = config::enabledIpSettings,
                remoteNetworkInfo = remoteNetworkInfo
            )
        )
    }
        .logOnCancellation("connectedWifiState")

    override val wifiState: StateFlow<WifiState> = wifiStatusMonitor.wifiStatus
        .flatMapLatest { wifiStatus ->
            i { "Received wifiStatus=$wifiStatus" }
            when (wifiStatus) {
                WifiStatus.Disabled -> flowOf(WifiState.Disabled)
                WifiStatus.NotConnected -> flowOf(WifiState.Disconnected)
                else -> connectedWifiState
            }
        }
        .stateIn(
            scope,
            SharingStarted.WhileSubscribed(),
            WifiState.Disconnected
        )

    init {
        // Refresh RemoteNetworkInfo on config change or property refresh
        connectedWifiStatus
            .refreshOn(widgetConfigFlow.distinctUntilChangedBy { it.properties })
            .collectLatestOn(scope) {
                i { "Refreshing RemoteNetworkInfo" }
                remoteNetworkInfoRepository.refresh()
            }

        // React to GPS enablement changes
        locationEnabledProvider
            .isEnabled
            .distinctUntilChanged()
            .collectOn(scope) { locationAccessChanged.emit(Unit) }
    }

    override fun onLocationAccessChanged() {
        scope.launch { locationAccessChanged.emit(Unit) }
    }
}
