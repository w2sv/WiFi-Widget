package com.w2sv.wifiwidget.ui.screen.home.model.wifistate

import com.hoc081098.flowext.withLatestFrom
import com.w2sv.common.utils.logOnCancellation
import com.w2sv.domain.model.networking.WifiStatus
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewDataProvider
import com.w2sv.domain.repository.RemoteWifiDataRepository
import com.w2sv.domain.repository.WidgetConfigFlow
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import com.w2sv.networking.wifistatus.monitor.WifiStatusMonitor
import com.w2sv.networking.wifistatus.provider.WifiStatusProvider
import com.w2sv.widget.actions.WidgetActions
import com.w2sv.wifiwidget.ui.screen.home.model.gpsstatus.LocationEnabledProvider
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
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

    private val wifiStatus = wifiStatusMonitor.wifiStatus
    private val defaultEgressChanges = wifiStatusMonitor.defaultEgressChanges

    // UI inputs are published only after synchronization validates the remote-data snapshot.
    // SharedFlow preserves repeated connected callbacks for local-property recomputation.
    private val validatedInputs = MutableSharedFlow<DisplayInputs>(replay = 1)
    private var refreshJob: Job? = null
    private var lastWifiStatus: WifiStatus? = null

    private val locationAccessChanged = MutableSharedFlow<Unit>()

    // Emits whenever location-dependent properties might need recomputation
    private val locationAccessChangedWhileDependentPropertiesEnabled: Flow<Unit> = locationAccessChanged
        .withLatestFrom(widgetConfigFlow) { _, config -> config }
        .filter { it.isAnyLocationAccessRequiringPropertyEnabled }
        .map { }
        .onStart { emit(Unit) }

    override val wifiState: StateFlow<WifiState> = combine(
        validatedInputs,
        remoteWifiDataRepository.data,
        locationAccessChangedWhileDependentPropertiesEnabled
    ) { inputs, remoteData, _ ->
        when (inputs.status) {
            WifiStatus.Disabled -> WifiState.Disabled
            WifiStatus.NotConnected -> WifiState.Disconnected
            else -> {
                d { "Computing connected Wi-Fi state for ${inputs.status} $remoteData ${inputs.config}" }
                WifiState.Connected(
                    status = inputs.status,
                    propertyViewData = wifiPropertyViewDataProvider(
                        enabledProperties = inputs.config.enabledProperties,
                        enabledIpSettings = inputs.config::enabledIpSettings,
                        remoteWifiData = remoteData
                    )
                )
            }
        }
    }
        .logOnCancellation("wifiState")
        .stateIn(scope, SharingStarted.WhileSubscribed(), WifiState.Disconnected)

    init {
        observeSynchronizationEvents()

        locationEnabledProvider.isEnabled
            .distinctUntilChanged()
            .collectOn(scope) { locationAccessChanged.emit(Unit) }
    }

    private fun observeSynchronizationEvents() {
        var previousConfig: WidgetConfig? = null

        val configEvents = widgetConfigFlow
            .distinctUntilChangedBy { it.propertyConfigMap to it.enabledProperties }
            .map { config ->
                val requiresRefresh = previousConfig?.propertyConfigMap != config.propertyConfigMap
                previousConfig = config
                SynchronizationEvent.Configuration(requiresRefresh)
            }
            .drop(1)

        // Reconcile and publish display inputs independently of HTTP refresh execution.
        // Refresh jobs can be cancelled and replaced without delaying subsequent events.
        merge(
            wifiStatus.map(SynchronizationEvent::Status),
            configEvents,
            defaultEgressChanges.map { SynchronizationEvent.DefaultEgress }
        ).collectOn(scope) { event -> synchronize(event) }
    }

    override fun onLocationAccessChanged() {
        scope.launch { locationAccessChanged.emit(Unit) }
    }

    override fun onForeground() {
        scope.launch { recoverRemoteDataOnForeground() }
    }

    private suspend fun synchronize(event: SynchronizationEvent) {
        val config = widgetConfigFlow.first()
        renderIfRemoteDataChanged { remoteWifiDataRepository.snapshot(config) }
        val status = when (event) {
            is SynchronizationEvent.Status -> event.status
            else -> lastWifiStatus
        }
        if (status != null) validatedInputs.emit(DisplayInputs(status, config))

        val refresh = when (event) {
            is SynchronizationEvent.Status -> event.status.isConnected && event.status != lastWifiStatus
            is SynchronizationEvent.Configuration -> event.requiresRefresh && status?.isConnected == true
            SynchronizationEvent.DefaultEgress -> status?.isConnected == true
        }
        if (event is SynchronizationEvent.Status) {
            lastWifiStatus = event.status
            if (!event.status.isConnected) {
                refreshJob?.cancel()
                refreshJob = null
            }
        }
        if (refresh) {
            // Replacement cancels obsolete HTTP work; the repository mutex orders any
            // cancellation/commit boundary with a newer worker or foreground refresh.
            refreshJob?.cancel()
            refreshJob = scope.launch {
                d { "Refreshing remote data after $event" }
                renderIfRemoteDataChanged { remoteWifiDataRepository.refresh() }
            }
        }
    }

    private suspend fun recoverRemoteDataOnForeground() {
        val config = widgetConfigFlow.first()
        val needsRecovery = renderIfRemoteDataChanged { remoteWifiDataRepository.needsRecovery(config) }
        // Foreground resume bypasses delayed WorkManager backoff. The repository rechecks under
        // its refresh mutex after any concurrent worker or home refresh completes.
        if (needsRecovery && wifiStatusProvider().isConnected) {
            renderIfRemoteDataChanged { remoteWifiDataRepository.refresh(onlyIfMissing = true) }
        }
    }

    private suspend fun <T> renderIfRemoteDataChanged(action: suspend () -> T): T {
        val before = remoteWifiDataRepository.data.value
        try {
            return action()
        } finally {
            if (remoteWifiDataRepository.data.value != before) widgetActions.render()
        }
    }
}

private data class DisplayInputs(val status: WifiStatus, val config: WidgetConfig)

private sealed interface SynchronizationEvent {
    data class Status(val status: WifiStatus) : SynchronizationEvent
    data class Configuration(val requiresRefresh: Boolean) : SynchronizationEvent
    data object DefaultEgress : SynchronizationEvent
}
