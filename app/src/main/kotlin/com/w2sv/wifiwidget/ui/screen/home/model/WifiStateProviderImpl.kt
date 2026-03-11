package com.w2sv.wifiwidget.ui.screen.home.model

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import com.w2sv.androidutils.location.isLocationEnabledCompat
import com.w2sv.androidutils.service.systemService
import com.w2sv.common.utils.log
import com.w2sv.domain.model.networking.WifiStatus
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewDataProvider
import com.w2sv.domain.repository.RemoteNetworkInfoRepository
import com.w2sv.domain.repository.WidgetConfigFlow
import com.w2sv.kotlinutils.coroutines.flow.collectLatestOn
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import com.w2sv.networking.wifistatus.monitor.WifiStatusMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
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
    @ApplicationContext private val context: Context,
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
        sharedConfig,
        remoteNetworkInfoRepository.data,
        locationAccessChangedWhileDependentPropertiesEnabled,
        sharedWifiStatus.filter { it.isConnected }
    ) { config, remoteNetworkInfo, _, _ ->
        WifiState.Connected(
            propertyViewData = wifiPropertyViewDataProvider(
                enabledProperties = config.orderedEnabledProperties(),
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
        merge(sharedWifiStatus, sharedConfig).collectLatestOn(scope) {
            remoteNetworkInfoRepository.refresh()
        }

        // React to GPS enablement changes
        context
            .isGpsEnabledFlow()
            .distinctUntilChanged()
            .collectOn(scope) { locationAccessChanged.emit(Unit) }
    }

    override fun onLocationAccessChanged() {
        scope.launch { locationAccessChanged.emit(Unit) }
    }
}

private fun Context.isGpsEnabledFlow(): Flow<Boolean> =
    callbackFlow {
        val locationManager = systemService<LocationManager>().also { trySend(it.isLocationEnabledCompat()) }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(locationManager.isLocationEnabledCompat())
            }
        }

        registerReceiver(receiver, IntentFilter(LocationManager.MODE_CHANGED_ACTION))
        awaitClose { unregisterReceiver(receiver) }
    }
