package com.w2sv.networking.remotenetworkdata

import android.net.ConnectivityManager
import android.net.Network
import com.w2sv.common.utils.log
import com.w2sv.domain.networking.IpAddress
import com.w2sv.domain.networking.IpApiData
import com.w2sv.domain.networking.RemoteDataConfig
import com.w2sv.domain.networking.RemoteNetworkData
import com.w2sv.domain.networking.RemoteNetworkDataRepository
import com.w2sv.domain.networking.RemoteRefreshResult
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.kotlinutils.makeIf
import com.w2sv.networking.connectivity.hasInternetCapability
import com.w2sv.networking.remotenetworkdata.ipapi.IpApiDataFetcher
import com.w2sv.networking.remotenetworkdata.publicip.PublicIpFetcher
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import slimber.log.d
import slimber.log.i

@Singleton
internal class RemoteNetworkDataRepositoryImpl @Inject constructor(
    private val ipApiDataFetcher: IpApiDataFetcher,
    private val publicIpFetcher: PublicIpFetcher,
    private val widgetConfigSource: WidgetConfigSource,
    private val connectivityManager: ConnectivityManager
) : RemoteNetworkDataRepository {

    override val data: StateFlow<RemoteNetworkData>
        field = MutableStateFlow(RemoteNetworkData.empty)

    /** Serializes asynchronous [refresh] operations. */
    private val refreshMutex = Mutex()

    /** Protects synchronous access to shared cache state, including calls from widget rendering. */
    private val stateLock = Any()
    private var cache = RemoteDataCache()

    override fun reconciledSnapshot(config: RemoteDataConfig): RemoteNetworkData =
        synchronized(stateLock) {
            reconcile(config)
            data.value
        }

    /** Called under [stateLock]; the published flow never contains data for an unconfirmed route. */
    private fun reconcile(config: RemoteDataConfig): Network? {
        val network = reconcileNetwork()
        if (network != null) reconcileConfig(config)
        publishData(network)
        return network
    }

    /** A confirmed identity change clears cache; unavailable identity or capability only hides it. */
    private fun reconcileNetwork(): Network? {
        val network = connectivityManager.activeNetwork

        if (network == null) {
            d { "Default network unavailable; retaining cache for ${cache.network}" }
            return null
        }

        if (network != cache.network) {
            i { "Default network changed: ${cache.network} -> $network; invalidating cache" }
            cache = RemoteDataCache(network = network)
        }

        val capabilities = connectivityManager.getNetworkCapabilities(network)

        if (capabilities == null) {
            d { "Capabilities unavailable for network=$network" }
            return null
        }

        if (!capabilities.hasInternetCapability) {
            d { "Network=$network lacks INTERNET capability" }
            return null
        }

        return network
    }

    private fun reconcileConfig(config: RemoteDataConfig) {
        val previous = cache.config
        if (previous == config) return

        val sameIpApiRequirements = previous != null &&
            previous.ipApiProperties == config.ipApiProperties &&
            previous.locationParameters == config.locationParameters

        cache = cache.copy(
            config = config,
            data = config.retainApplicable(cache.data, previous),
            ipApiInitializedForConfig = cache.ipApiInitializedForConfig && sameIpApiRequirements,
            attemptedIpVersions = cache.attemptedIpVersions.intersect(config.publicIpVersions)
        )
    }

    override fun needsRecovery(config: RemoteDataConfig): Boolean =
        synchronized(stateLock) {
            val network = reconcile(config)
            (config.requiresIpApi || config.requiresPublicIp) &&
                (network == null || !cache.isRecoverySatisfied(config))
        }

    override suspend fun refresh(onlyIfMissing: Boolean): RemoteRefreshResult {
        d { "Remote data refresh started: onlyIfMissing=$onlyIfMissing" }
        return refreshMutex.withLock {
            val config = widgetConfigSource.snapshot().remoteDataConfig
            val (network, fetchPlan) = synchronized(stateLock) {
                val current = reconcile(config)
                current to current?.let { cache.fetchPlan(config, onlyIfMissing) }
            }
            if (network == null) {
                d { "Remote data refresh aborted: no usable default network" }
                return@withLock RemoteRefreshResult.NetworkUnavailable
            }
            checkNotNull(fetchPlan)
            if (fetchPlan.isEmpty) {
                d { "Remote data refresh skipped: enabled data already complete for network=$network" }
                return@withLock RemoteRefreshResult.Complete
            }
            val results = fetchPlan.execute(config)
            val latestConfig = widgetConfigSource.snapshot().remoteDataConfig
            applyFetchResults(network, config, results, latestConfig)
        }
            .log { result -> "Remote data refresh finished: onlyIfMissing=$onlyIfMissing, result=$result" }
    }

    private suspend fun RemoteFetchPlan.execute(config: RemoteDataConfig): FetchResults =
        coroutineScope {
            val api = makeIf(fetchIpApi) {
                async { ipApiDataFetcher.fetch(config.locationParameters) }
            }
            val ips = makeIf(publicIpVersions.isNotEmpty()) {
                async { publicIpFetcher.fetch(publicIpVersions) }
            }

            FetchResults(api?.await(), ips?.await().orEmpty(), publicIpVersions)
        }

    private fun applyFetchResults(
        network: Network,
        config: RemoteDataConfig,
        results: FetchResults,
        latestConfig: RemoteDataConfig
    ): RemoteRefreshResult =
        synchronized(stateLock) {
            // The system routes HTTP on its default egress. Rechecking here rejects observed
            // changes, though it cannot identify an A -> B -> A transition between checks.
            val current = reconcile(latestConfig)
            if (current == null || current != network) {
                d { "Remote data refresh discarded: network changed or became unavailable; start=$network, end=$current" }
                return@synchronized RemoteRefreshResult.NetworkUnavailable
            }
            if (latestConfig != config) {
                d { "Remote data refresh discarded: remote-data requirements changed during fetch" }
                return@synchronized cache.recoveryResult(latestConfig)
            }

            cache = cache.withFetchResults(config, results)

            publishData(current)
            cache.recoveryResult(config)
        }

    private fun publishData(network: Network?) {
        data.value = if (network == null) RemoteNetworkData.empty else cache.data
    }
}

/**
 * Values and initialization belong to one confirmed default-egress network and config.
 * Reconciliation can retain applicable values while resetting initialization for a changed
 * IP-API config or removing attempts for disabled public-IP versions.
 */
private data class RemoteDataCache(
    val network: Network? = null,
    val config: RemoteDataConfig? = null,
    val data: RemoteNetworkData = RemoteNetworkData.empty,
    val ipApiInitializedForConfig: Boolean = false,
    val attemptedIpVersions: Set<IpAddress.Version> = emptySet()
) {
    // A public-IP value from any enabled family is useful once every enabled family has
    // received an attempt. If every family fails, recovery remains possible.
    private fun isPublicIpRecoverySatisfied(config: RemoteDataConfig): Boolean =
        !config.requiresPublicIp ||
            (
                data.publicIps.any { it.version in config.publicIpVersions } &&
                    attemptedIpVersions.containsAll(config.publicIpVersions)
                )

    fun isRecoverySatisfied(config: RemoteDataConfig): Boolean =
        (!config.requiresIpApi || ipApiInitializedForConfig) &&
            isPublicIpRecoverySatisfied(config)

    fun recoveryResult(config: RemoteDataConfig): RemoteRefreshResult =
        if (isRecoverySatisfied(config)) RemoteRefreshResult.Complete else RemoteRefreshResult.Incomplete

    private fun ipVersionsNeedingRecovery(config: RemoteDataConfig): Set<IpAddress.Version> =
        when {
            isPublicIpRecoverySatisfied(config) -> emptySet()
            data.publicIps.none { it.version in config.publicIpVersions } -> config.publicIpVersions
            else -> config.publicIpVersions - attemptedIpVersions
        }

    fun fetchPlan(config: RemoteDataConfig, onlyIfMissing: Boolean): RemoteFetchPlan =
        if (!onlyIfMissing) {
            RemoteFetchPlan(config.requiresIpApi, config.publicIpVersions)
        } else {
            RemoteFetchPlan(
                fetchIpApi = config.requiresIpApi && !ipApiInitializedForConfig,
                publicIpVersions = ipVersionsNeedingRecovery(config)
            )
        }

    fun withFetchResults(config: RemoteDataConfig, results: FetchResults): RemoteDataCache {
        val publicIps = data.publicIps.associateBy { it.version }.toMutableMap()

        results.publicIps.forEach { (version, result) ->
            result.getOrNull()?.let { publicIps[version] = it }
        }

        return copy(
            data = data.copy(
                ipApiData = results.ipApi?.getOrNull()
                    ?.let(config::retainApplicable)
                    ?: data.ipApiData,
                publicIps = config.publicIpVersions.mapNotNull(publicIps::get)
            ),
            ipApiInitializedForConfig =
            ipApiInitializedForConfig || results.ipApi?.isSuccess == true,
            attemptedIpVersions = attemptedIpVersions + results.attemptedIpVersions
        )
    }
}

/**
 * The requests to execute for one refresh of a [RemoteDataConfig]. A full refresh
 * includes every required data group. Recovery narrows the plan using successful cached
 * values and attempted public-IP families, so satisfied requirements are skipped.
 * The plan controls fetching; the config governs which results may be retained.
 */
private data class RemoteFetchPlan(val fetchIpApi: Boolean, val publicIpVersions: Set<IpAddress.Version>) {
    val isEmpty get() = !fetchIpApi && publicIpVersions.isEmpty()
}

private data class FetchResults(
    val ipApi: Result<IpApiData>?,
    val publicIps: Map<IpAddress.Version, Result<IpAddress>>,
    val attemptedIpVersions: Set<IpAddress.Version>
)
