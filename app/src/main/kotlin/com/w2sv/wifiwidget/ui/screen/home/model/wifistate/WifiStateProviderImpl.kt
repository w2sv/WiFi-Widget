package com.w2sv.wifiwidget.ui.screen.home.model.wifistate

import com.hoc081098.flowext.withLatestFrom
import com.w2sv.common.utils.logOnCancellation
import com.w2sv.common.utils.logOnEach
import com.w2sv.common.utils.refreshOn
import com.w2sv.domain.model.networking.WifiStatus
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewDataProvider
import com.w2sv.domain.repository.RemoteWifiDataRepository
import com.w2sv.domain.repository.WidgetConfigFlow
import com.w2sv.kotlinutils.coroutines.flow.collectLatestOn
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import com.w2sv.networking.wifistatus.monitor.WifiStatusMonitor
import com.w2sv.networking.wifistatus.provider.WifiStatusProvider
import com.w2sv.widget.actions.WidgetActions
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import slimber.log.d

@ViewModelScoped
class WifiStateProviderImpl @Inject constructor(
    wifiStatusMonitor: WifiStatusMonitor,
    private val widgetConfigFlow: WidgetConfigFlow,
    wifiPropertyViewDataProvider: WifiPropertyViewDataProvider,
    private val remoteWifiDataRepository: RemoteWifiDataRepository,
    private val widgetActions: WidgetActions,
    private val wifiStatusProvider: WifiStatusProvider,
    locationEnabledProvider: LocationEnabledProvider,
    private val scope: CoroutineScope
) : WifiStateProvider {

    // The home state and remote refreshes must observe one callback registration. Keep raw
    // statuses available: a disconnect matters even though it cannot render connected data.
    private val wifiStatus = wifiStatusMonitor.wifiStatus
        .shareIn(scope, SharingStarted.WhileSubscribed(), replay = 1)

    private val connectedWifiStatus = wifiStatus
        .filter { it.isConnected }
        .shareIn(scope, SharingStarted.WhileSubscribed(), replay = 1)
        .logOnEach("connectedWifiStatus")

    // An egress switch can leave Wi-Fi status equal, so it needs its own recomputation signal.
    private val defaultEgressChanges = wifiStatusMonitor.defaultEgressChanges
        .shareIn(scope, SharingStarted.WhileSubscribed())

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
        widgetConfigFlow.distinctUntilChangedBy { it.propertyConfigMap to it.enabledProperties }.logOnEach("sharedConfig"),
        remoteWifiDataRepository.data.logOnEach("remoteWifiData"),
        connectedWifiStatus,
        locationAccessChangedWhileDependentPropertiesEnabled,
        defaultEgressChanges.onStart { emit(Unit) }
    ) { config, _, connectedStatus, _, _ ->
        // A default-route callback may arrive before a refresh. Validate the snapshot here so
        // the home screen never renders remote data from the previous VPN/cellular/Wi-Fi route.
        val previous = remoteWifiDataRepository.data.value
        val remoteWifiData = remoteWifiDataRepository.snapshot(config)
        // Snapshot can confirm a new route and invalidate the cache before the async refresh
        // succeeds. Push that invalidation to launcher widgets so they cannot retain old IP data.
        if (remoteWifiDataRepository.data.value != previous) widgetActions.render()
        d { "Computing connectedWifiState for $connectedStatus $remoteWifiData $config" }
        WifiState.Connected(
            status = connectedStatus,
            propertyViewData = wifiPropertyViewDataProvider(
                enabledProperties = config.enabledProperties,
                enabledIpSettings = config::enabledIpSettings,
                remoteWifiData = remoteWifiData
            )
        )
    }
        .logOnCancellation("connectedWifiState")

    override val wifiState: StateFlow<WifiState> = wifiStatus
        .flatMapLatest { wifiStatus ->
            d { "Wi-Fi status changed: status=$wifiStatus" }
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
        merge(
            // Initial callbacks often repeat a connected status. Deduplicate *before* filtering:
            // ConnectedInactive -> NotConnected -> ConnectedInactive must refresh even when
            // cellular/VPN stays the default route. Local rendering still sees every callback.
            wifiStatus.distinctUntilChanged().filter { it.isConnected }
                .refreshOn(widgetConfigFlow.distinctUntilChangedBy { it.propertyConfigMap }),
            // connectedWifiStatus replays its last Connected value across a disconnection.
            // The raw shared stream carries NotConnected, so it can gate egress refreshes.
            defaultEgressChanges.withLatestFrom(wifiStatus) { _, status -> status }.filter { it.isConnected }
        )
            .collectLatestOn(scope) {
                d { "Refreshing remote data after Wi-Fi or default network change" }
                val previous = remoteWifiDataRepository.data.value
                remoteWifiDataRepository.refresh()
                // The home screen may be the first observer after standby or an egress switch.
                // Publish its successful/invalidating refresh to existing launcher widgets too.
                if (remoteWifiDataRepository.data.value != previous) widgetActions.render()
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

    override fun onForeground() {
        scope.launch {
            val config = widgetConfigFlow.first()
            val before = remoteWifiDataRepository.data.value
            val needsRecovery = remoteWifiDataRepository.needsRecovery(config)
            // A pending WorkManager retry may have hours of backoff after standby. Foreground
            // resume can try promptly; onlyIfMissing rechecks under the repository mutex.
            if (needsRecovery && wifiStatusProvider().isConnected) {
                remoteWifiDataRepository.refresh(onlyIfMissing = true)
            }
            // Validation can change published data without HTTP, including restoring retained
            // values for the same network after a temporary loss of route information.
            if (remoteWifiDataRepository.data.value != before) widgetActions.render()
        }
    }
}
