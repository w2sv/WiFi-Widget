package com.w2sv.wifiwidget.ui.screen.home.synchronization

import com.w2sv.domain.networking.NetworkAvailabilitySource
import com.w2sv.domain.networking.RemoteDataConfig
import com.w2sv.domain.networking.RemoteNetworkDataRepository
import com.w2sv.domain.networking.WifiStatus
import com.w2sv.domain.networking.WifiStatusSource
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import com.w2sv.kotlinutils.coroutines.runOrHandle
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import slimber.log.d
import slimber.log.e

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
    private val remoteNetworkDataRepository: RemoteNetworkDataRepository,
    private val networkAvailabilitySource: NetworkAvailabilitySource,
    private val scope: CoroutineScope
) {

    /** Replays the latest reconciled inputs; identical status callbacks still emit for local values. */
    val inputs: SharedFlow<ReconciledWifiInputs>
        field = MutableSharedFlow<ReconciledWifiInputs>(replay = 1)

    // Only ownership and reconciliation use this mutex; HTTP always runs outside it.
    private val ownershipMutex = Mutex()
    private var activeRefresh: ActiveRefresh? = null
    private var lastWifiStatus: WifiStatus? = null
    private var lastWifiIdentity: Long? = null
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

    /** Reconciliation and publication never wait for HTTP completion. */
    private suspend fun synchronize(event: SynchronizationEvent) =
        ownershipMutex.withLock {
            val remoteDataConfig = widgetConfigSource.snapshot().remoteDataConfig
            remoteNetworkDataRepository.reconciledSnapshot(remoteDataConfig)
            val status = when (event) {
                is SynchronizationEvent.Status -> event.status
                SynchronizationEvent.DefaultEgress -> wifiStatusSource.snapshot()
                SynchronizationEvent.Configuration -> lastWifiStatus
            }
            if (status != null) inputs.emit(ReconciledWifiInputs(status, remoteDataConfig))

            val initialized = lastWifiStatus != null
            val wifiIdentity = wifiStatusSource.connectedWifiIdentity()
            val refresh = when (event) {
                is SynchronizationEvent.Status ->
                    event.status.isConnected &&
                        (event.status != lastWifiStatus || wifiIdentity != lastWifiIdentity)
                else -> initialized && status?.isConnected == true
            }
            if (event is SynchronizationEvent.Status || (initialized && event == SynchronizationEvent.DefaultEgress)) {
                lastWifiStatus = status
                lastWifiIdentity = wifiIdentity
            }
            if (status?.isConnected == false) {
                cancelRefresh()
                return@withLock
            }
            if (!refresh || status == null) return@withLock

            val key = refreshKey(status, remoteDataConfig, wifiIdentity)
            ensureRefresh(key, onlyIfMissing = event == SynchronizationEvent.Configuration)
        }

    /**
     * Initial status synchronization owns the full startup refresh, regardless of resume ordering.
     * After status establishment, resume validates the current route and requirements, shares an
     * applicable active request, or fetches only missing data. A caller's cancellation stops waiting;
     * the ViewModel owns the shared job and obsolete events cancel it.
     */
    suspend fun recoverOnResume() {
        val job = ownershipMutex.withLock {
            if (lastWifiStatus == null) return@withLock null
            val config = widgetConfigSource.snapshot().remoteDataConfig
            val status = wifiStatusSource.snapshot()
            val key = refreshKey(status, config)
            // Validate even when another caller is fetching: an obsolete route must be hidden now.
            val missing = remoteNetworkDataRepository.needsRecovery(config)
            if (!status.isConnected) {
                cancelRefresh()
                // A resume can observe a disconnection before its callback arrives. Remember it
                // so a subsequent equal-looking connected callback still performs a full refresh.
                lastWifiStatus = status
                lastWifiIdentity = key.wifiIdentity
                return@withLock null
            }
            ensureRefresh(key, onlyIfMissing = true, needed = missing)
        }
        job?.join()
    }

    private fun cancelRefresh() {
        activeRefresh?.job?.cancel()
        activeRefresh = null
    }

    /**
     * Called under ownershipMutex; LAZY assigns ownership before the coroutine can finish.
     * Resume may join an applicable request even if cached data already satisfies recovery.
     */
    private fun ensureRefresh(
        key: RefreshKey,
        onlyIfMissing: Boolean,
        needed: Boolean = true
    ): Job? {
        activeRefresh?.takeIf { it.satisfies(key, onlyIfMissing) }?.let { return it.job }
        cancelRefresh()
        if (!needed) return null
        val job = scope.launch(start = CoroutineStart.LAZY) {
            runOrHandle(onFailure = { exception -> e { "Foreground remote refresh failed: error=$exception" } }) {
                d { "Foreground remote refresh started: onlyIfMissing=$onlyIfMissing" }
                remoteNetworkDataRepository.refresh(onlyIfMissing)
            }
        }
        activeRefresh = ActiveRefresh(key, onlyIfMissing, job)
        job.start()
        return job
    }

    private fun refreshKey(
        status: WifiStatus,
        config: RemoteDataConfig,
        wifiIdentity: Long? = wifiStatusSource.connectedWifiIdentity()
    ): RefreshKey {
        val availability = networkAvailabilitySource()
        return RefreshKey(
            config,
            status,
            wifiIdentity,
            availability.defaultNetworkIdentity,
            availability.hasUsableDefaultNetwork,
            availability.isDirectWifiDefaultNetwork,
            availability.isVpnDefaultNetwork
        )
    }

    private data class RefreshKey(
        val config: RemoteDataConfig,
        val status: WifiStatus,
        val wifiIdentity: Long?,
        val defaultIdentity: Long?,
        val hasUsableDefaultNetwork: Boolean,
        val isDirectWifiDefaultNetwork: Boolean,
        val isVpnDefaultNetwork: Boolean
    )

    private data class ActiveRefresh(val key: RefreshKey, val onlyIfMissing: Boolean, val job: Job) {
        fun satisfies(key: RefreshKey, onlyIfMissing: Boolean): Boolean =
            job.isActive && this.key == key && (onlyIfMissing || !this.onlyIfMissing)
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
