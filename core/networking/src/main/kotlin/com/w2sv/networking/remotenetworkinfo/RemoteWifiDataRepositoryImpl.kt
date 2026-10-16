package com.w2sv.networking.remotenetworkinfo

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.w2sv.domain.model.networking.IpAddress
import com.w2sv.domain.model.networking.IpApiData
import com.w2sv.domain.model.networking.RemoteRefreshResult
import com.w2sv.domain.model.networking.RemoteWifiData
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.repository.RemoteWifiDataRepository
import com.w2sv.domain.repository.WidgetConfigReader
import com.w2sv.networking.remotenetworkinfo.ip_api.IpApiDataFetcher
import com.w2sv.networking.remotenetworkinfo.public_ip.PublicIpFetcher
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import slimber.log.d
import slimber.log.i

@Singleton
internal class RemoteWifiDataRepositoryImpl @Inject constructor(
    private val ipApiDataFetcher: IpApiDataFetcher,
    private val publicIpFetcher: PublicIpFetcher,
    private val widgetConfigReader: WidgetConfigReader,
    private val connectivityManager: ConnectivityManager
) : RemoteWifiDataRepository {
    private val _data = MutableStateFlow(RemoteWifiData.empty)
    override val data = _data.asStateFlow()

    // Refreshes are ordered, while synchronous widget snapshots share only the short state
    // sections. No state lock is held during HTTP or suspendable configuration reads.
    private val refreshMutex = Mutex()
    private val stateLock = Any()
    private var cache = RemoteDataCache()

    /**
     * Values and initialization belong to one confirmed default-egress network and selection.
     * Reconciliation can retain applicable values while resetting initialization for a changed
     * IP-API selection or removing attempts for disabled public-IP versions.
     */
    private data class RemoteDataCache(
        val network: Network? = null,
        val selection: RemoteDataSelection? = null,
        val data: RemoteWifiData = RemoteWifiData.empty,
        val ipApiInitializedForSelection: Boolean = false,
        val attemptedIpVersions: Set<IpAddress.Version> = emptySet()
    ) {
        // A public-IP value from any enabled family is useful once every enabled family has
        // received an attempt. If every family fails, recovery remains possible.
        private fun isPublicIpRecoverySatisfied(selection: RemoteDataSelection): Boolean =
            !selection.requiresPublicIp ||
                (
                    data.publicIps.any { it.version in selection.publicIpVersions } &&
                        attemptedIpVersions.containsAll(selection.publicIpVersions)
                    )

        fun isRecoverySatisfied(selection: RemoteDataSelection): Boolean =
            (!selection.requiresIpApi || ipApiInitializedForSelection) &&
                isPublicIpRecoverySatisfied(selection)

        fun recoveryResult(selection: RemoteDataSelection): RemoteRefreshResult =
            if (isRecoverySatisfied(selection)) RemoteRefreshResult.Complete else RemoteRefreshResult.Incomplete

        private fun ipVersionsNeedingRecovery(selection: RemoteDataSelection): Set<IpAddress.Version> =
            when {
                isPublicIpRecoverySatisfied(selection) -> emptySet()
                data.publicIps.none { it.version in selection.publicIpVersions } -> selection.publicIpVersions
                else -> selection.publicIpVersions - attemptedIpVersions
            }

        fun fetchPlan(selection: RemoteDataSelection, onlyIfMissing: Boolean): RemoteFetchPlan =
            if (!onlyIfMissing) {
                RemoteFetchPlan(selection.requiresIpApi, selection.publicIpVersions)
            } else {
                RemoteFetchPlan(
                    fetchIpApi = selection.requiresIpApi && !ipApiInitializedForSelection,
                    publicIpVersions = ipVersionsNeedingRecovery(selection)
                )
            }
    }

    private data class RemoteFetchPlan(val fetchIpApi: Boolean, val publicIpVersions: Set<IpAddress.Version>) {
        val isEmpty get() = !fetchIpApi && publicIpVersions.isEmpty()
    }

    private data class FetchResults(
        val ipApi: Result<IpApiData>?,
        val publicIps: Map<IpAddress.Version, Result<IpAddress>>,
        val attemptedIpVersions: Set<IpAddress.Version>
    )

    /** A confirmed identity change clears cache; unavailable identity or capability only hides it. */
    private fun reconcileNetwork(): Network? {
        val observed = connectivityManager.activeNetwork
        if (observed == null) d { "Remote data validation: activeNetwork is missing; retaining cache for network=${cache.network}" }
        if (observed != null && observed != cache.network) {
            i { "Remote data validation: network changed from ${cache.network} to $observed; invalidating cache" }
            cache = RemoteDataCache(network = observed)
        }
        val capabilities = observed?.let(connectivityManager::getNetworkCapabilities)
        return when {
            observed == null -> null
            capabilities == null -> {
                d { "Remote data validation: capabilities missing for network=$observed" }
                null
            }
            !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> {
                d { "Remote data validation: network=$observed lacks INTERNET capability" }
                null
            }
            else -> observed
        }
    }

    private fun reconcileSelection(selection: RemoteDataSelection) {
        val previous = cache.selection
        cache = cache.copy(
            selection = selection,
            data = selection.retainApplicable(cache.data, previous),
            ipApiInitializedForSelection = cache.ipApiInitializedForSelection &&
                previous?.ipApiProperties == selection.ipApiProperties &&
                previous.locationParameters == selection.locationParameters,
            attemptedIpVersions = cache.attemptedIpVersions.intersect(selection.publicIpVersions)
        )
    }

    private fun publishDisplayableData(network: Network?) {
        _data.value = if (network == null) RemoteWifiData.empty else cache.data
    }

    /** Called under [stateLock]; the published flow never contains data for an unconfirmed route. */
    private fun reconcile(selection: RemoteDataSelection): Network? {
        val network = reconcileNetwork()
        if (network != null) reconcileSelection(selection)
        publishDisplayableData(network)
        return network
    }

    override fun snapshot(config: WidgetConfig): RemoteWifiData =
        synchronized(stateLock) {
            reconcile(RemoteDataSelection.from(config))
            _data.value
        }

    override fun needsRecovery(config: WidgetConfig): Boolean =
        synchronized(stateLock) {
            val selection = RemoteDataSelection.from(config)
            val network = reconcile(selection)
            (selection.requiresIpApi || selection.requiresPublicIp) &&
                (network == null || !cache.isRecoverySatisfied(selection))
        }

    private suspend fun executeFetchPlan(plan: RemoteFetchPlan, selection: RemoteDataSelection): FetchResults =
        coroutineScope {
            val api = if (plan.fetchIpApi) async { ipApiDataFetcher.fetch(selection.locationParameters) } else null
            val ips = if (plan.publicIpVersions.isNotEmpty()) async { publicIpFetcher.fetch(plan.publicIpVersions) } else null
            FetchResults(api?.await(), ips?.await().orEmpty(), plan.publicIpVersions)
        }

    private fun applyFetchResults(
        network: Network,
        selection: RemoteDataSelection,
        results: FetchResults,
        latestSelection: RemoteDataSelection
    ): RemoteRefreshResult =
        synchronized(stateLock) {
            // The system routes HTTP on its default egress. Rechecking here rejects observed
            // changes, though it cannot identify an A -> B -> A transition between checks.
            val current = reconcile(latestSelection)
            if (current == null || current != network) {
                d { "Remote data refresh discarded: network changed or became unavailable; start=$network, end=$current" }
                return@synchronized RemoteRefreshResult.NetworkUnavailable
            }
            if (latestSelection != selection) {
                d { "Remote data refresh discarded: enabled properties changed during fetch" }
                return@synchronized cache.recoveryResult(latestSelection)
            }

            val updatedIps = cache.data.publicIps.associateBy { it.version }.toMutableMap()
            results.publicIps.forEach { (version, result) ->
                result.getOrNull()?.let { updatedIps[version] = it }
            }
            // Failed requests retain successful data from the same egress network. A completed
            // attempt counts even when an IP family is unsupported, but success is tracked separately.
            cache = cache.copy(
                data = cache.data.copy(
                    ipApiData = results.ipApi?.getOrNull()?.let(selection::retainApplicable) ?: cache.data.ipApiData,
                    publicIps = selection.publicIpVersions.mapNotNull(updatedIps::get)
                ),
                ipApiInitializedForSelection = cache.ipApiInitializedForSelection || results.ipApi?.isSuccess == true,
                attemptedIpVersions = cache.attemptedIpVersions + results.attemptedIpVersions
            )
            publishDisplayableData(current)
            cache.recoveryResult(selection)
        }

    override suspend fun refresh(onlyIfMissing: Boolean): RemoteRefreshResult {
        d { "Remote data refresh started: onlyIfMissing=$onlyIfMissing" }
        val result = refreshMutex.withLock {
            val selection = RemoteDataSelection.from(widgetConfigReader.read())
            val (network, plan) = synchronized(stateLock) {
                val current = reconcile(selection)
                current to current?.let { cache.fetchPlan(selection, onlyIfMissing) }
            }
            if (network == null) {
                d { "Remote data refresh aborted: no usable default network" }
                return@withLock RemoteRefreshResult.NetworkUnavailable
            }
            checkNotNull(plan)
            if (plan.isEmpty) {
                d { "Remote data refresh skipped: enabled data already complete for network=$network" }
                return@withLock RemoteRefreshResult.Complete
            }
            val results = executeFetchPlan(plan, selection)
            applyFetchResults(network, selection, results, RemoteDataSelection.from(widgetConfigReader.read()))
        }
        i { "Remote data refresh finished: onlyIfMissing=$onlyIfMissing, result=$result" }
        return result
    }
}
