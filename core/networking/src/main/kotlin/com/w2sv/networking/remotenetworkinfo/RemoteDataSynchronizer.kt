package com.w2sv.networking.remotenetworkinfo

import com.w2sv.domain.model.networking.WifiStatus
import com.w2sv.domain.repository.RemoteWifiDataRepository
import com.w2sv.domain.repository.WidgetConfigSource
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import com.w2sv.networking.wifistatus.WifiStatusSource
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import slimber.log.d

/**
 * Orchestrates remote-data synchronization for one home ViewModel lifetime. It observes Wi-Fi
 * status, remote-relevant configuration changes, and default-egress changes on the ViewModel scope.
 * Each event reconciles the repository cache before publishing [inputs], so presentation never
 * receives an input for a route whose old remote data is still displayable. HTTP runs in a
 * separate replaceable job so slow requests cannot hold up later events or their reconciliation.
 */
@ViewModelScoped
class RemoteDataSynchronizer @Inject constructor(
    private val wifiStatusSource: WifiStatusSource,
    private val widgetConfigSource: WidgetConfigSource,
    private val remoteWifiDataRepository: RemoteWifiDataRepository,
    private val scope: CoroutineScope
) {

    /** Replays the latest reconciled inputs; identical status callbacks still emit for local values. */
    val inputs: SharedFlow<ReconciledWifiInputs>
        field = MutableSharedFlow<ReconciledWifiInputs>(replay = 1)

    private var refreshJob: Job? = null
    private var lastWifiStatus: WifiStatus? = null
    private val configEvents = widgetConfigSource.flow
        .distinctUntilChangedBy { it.remoteDataConfig }
        .drop(1)
        .map { SynchronizationEvent.Configuration }

    init {
        observeEvents()
    }

    private fun observeEvents() {
        merge(
            wifiStatusSource.status.map(SynchronizationEvent::Status),
            configEvents,
            wifiStatusSource.defaultEgressChanges.map { SynchronizationEvent.DefaultEgress }
        )
            .collectOn(scope) { event -> synchronize(event) }
    }

    /** Reconciles the current config before publishing status, then updates the HTTP refresh job. */
    private suspend fun synchronize(event: SynchronizationEvent) {
        val remoteDataConfig = widgetConfigSource.snapshot().remoteDataConfig
        remoteWifiDataRepository.reconciledSnapshot(remoteDataConfig)
        val status = when (event) {
            is SynchronizationEvent.Status -> event.status
            SynchronizationEvent.DefaultEgress -> wifiStatusSource.snapshot()
            SynchronizationEvent.Configuration -> lastWifiStatus
        }
        if (status != null) {
            inputs.emit(ReconciledWifiInputs(status, remoteDataConfig))
        }

        val refresh = when (event) {
            is SynchronizationEvent.Status -> event.status.isConnected && event.status != lastWifiStatus
            else -> status?.isConnected == true
        }

        if (event is SynchronizationEvent.Status || event == SynchronizationEvent.DefaultEgress) {
            lastWifiStatus = status
            if (status?.isConnected == false) {
                refreshJob?.cancel()
                refreshJob = null
            }
        }
        if (refresh) {
            // Cancellation makes an obsolete request discard its result; repository locking
            // still orders its cancellation/commit boundary with the replacement request.
            refreshJob?.cancel()
            refreshJob = scope.launch {
                d { "Refreshing remote data after $event" }
                remoteWifiDataRepository.refresh()
            }
        }
    }

    /** Bypasses worker backoff and fetches only requirements still missing after validation. */
    suspend fun recoverOnForeground() {
        val remoteDataConfig = widgetConfigSource.snapshot().remoteDataConfig
        val needsRecovery = remoteWifiDataRepository.needsRecovery(remoteDataConfig)
        if (needsRecovery && wifiStatusSource.snapshot().isConnected) {
            // The repository rechecks under its mutex after any concurrent worker or home refresh.
            remoteWifiDataRepository.refresh(onlyIfMissing = true)
        }
    }
}

/**
 * An observation that requires remote-data cache reconciliation before inputs are
 * published. The event also informs refresh and cancellation decisions, but does not
 * itself imply an HTTP request.
 */
private sealed interface SynchronizationEvent {
    /** A Wi-Fi observation; identical statuses still publish updated local inputs. */
    @JvmInline
    value class Status(val status: WifiStatus) : SynchronizationEvent

    /** A change to remote-data requirements. */
    data object Configuration : SynchronizationEvent

    /** A default-route change that can affect remote data without changing Wi-Fi status. */
    data object DefaultEgress : SynchronizationEvent
}
