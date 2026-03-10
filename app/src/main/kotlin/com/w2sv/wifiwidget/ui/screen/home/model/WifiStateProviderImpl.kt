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
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import com.w2sv.networking.wifistatus.WifiStatusMonitor
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

    private val sharedConfigFlow = widgetConfigFlow.shareIn(scope, SharingStarted.WhileSubscribed(), replay = 1)

    private val locationAccessChanged = MutableSharedFlow<Unit>(replay = 1)

    private val propertyRefreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    private val locationAccessChangedWhileDependentPropertiesEnabled: Flow<Unit> =
        combine(locationAccessChanged, sharedConfigFlow) { _, config -> config }
            .filter { it.isAnyLocationAccessRequiringPropertyEnabled }
            .map { }

    private val combinedPropertyRefreshTrigger: Flow<Unit> =
        merge(
            propertyRefreshTrigger,
            locationAccessChangedWhileDependentPropertiesEnabled
        )
            .onStart { emit(Unit) } // Ensures initial computation

    override val wifiState: StateFlow<WifiState> = wifiStatusMonitor.wifiStatus
        .flatMapLatest { wifiStatus ->
            when (wifiStatus) {
                WifiStatus.Disabled -> flowOf(WifiState.Disabled)
                WifiStatus.Disconnected -> flowOf(WifiState.Disconnected)
                WifiStatus.Connected, WifiStatus.ConnectedInactive -> connectedWifiState
            }
                .log { "Set wifiState=$it" }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(), WifiState.Disconnected)

    private val connectedWifiState: Flow<WifiState.Connected> = combine(
        sharedConfigFlow,
        remoteNetworkInfoRepository.data,
        combinedPropertyRefreshTrigger
    ) { config, remoteNetworkInfo, _ ->
        WifiState.Connected(
            propertyViewData = wifiPropertyViewDataProvider(
                enabledProperties = config.orderedEnabledProperties(),
                enabledIpSettings = config::enabledIpSettings,
                remoteNetworkInfo = remoteNetworkInfo
            )
        )
    }
        .shareIn(
            scope,
            SharingStarted.WhileSubscribed(),
            replay = 1
        ) // Avoids recomputation on toggles between WifiStatus.Connected & WifiStatus.ConnectedInactive

    init {
        // Refresh RemoteNetworkInfo on config change or property refresh
        merge(sharedConfigFlow, propertyRefreshTrigger).collectOn(scope) {
            remoteNetworkInfoRepository.refresh()
        }

        // Emit on locationAccessChanged on change of gps enablement
        context
            .isGpsEnabledFlow()
            .distinctUntilChanged()
            .collectOn(scope) { locationAccessChanged.emit(Unit) }
    }

    override fun onLocationAccessChanged() {
        scope.launch { locationAccessChanged.emit(Unit) }
    }

    override fun refreshProperties() {
        scope.launch { propertyRefreshTrigger.emit(Unit) }
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
