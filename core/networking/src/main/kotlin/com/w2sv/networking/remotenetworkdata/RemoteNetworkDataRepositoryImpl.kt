package com.w2sv.networking.remotenetworkdata

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import com.w2sv.common.logging.log
import com.w2sv.domain.networking.IpAddress
import com.w2sv.domain.networking.IpApiData
import com.w2sv.domain.networking.RemoteDataConfig
import com.w2sv.domain.networking.RemoteNetworkData
import com.w2sv.domain.networking.RemoteNetworkDataRepository
import com.w2sv.domain.networking.RemoteRefreshResult
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.kotlinutils.makeIf
import com.w2sv.networking.connectivity.hasInternetCapability
import com.w2sv.networking.connectivity.isDirectWifiEgress
import com.w2sv.networking.connectivity.selectWifiNetwork
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
    private val connectivityManager: ConnectivityManager,
    private val wifiManager: WifiManager
) : RemoteNetworkDataRepository {

    override val data: StateFlow<RemoteNetworkData>
        field = MutableStateFlow(RemoteNetworkData.empty)

    /** Serializes asynchronous [refresh] operations. */
    private val refreshMutex = Mutex()

    /** Protects synchronous access to shared cache state, including calls from widget rendering. */
    private val stateLock = Any()
    private var cache = RemoteDataCache()

    // This copy survives default-egress changes; it is visible only while its exact Network is
    // still the uniquely identified direct Wi-Fi connection.
    private var wifiCache: RemoteDataCache? = null

    // A new default can appear before Android exposes its capabilities. Keep its identity until
    // one usable observation decides whether the previous Wi-Fi values may be restored.
    private var pendingRestorationNetwork: Network? = null

    override fun reconciledSnapshot(config: RemoteDataConfig): RemoteNetworkData =
        synchronized(stateLock) {
            reconcile(config)
            data.value
        }

    override fun widgetSnapshot(config: RemoteDataConfig): RemoteNetworkData =
        synchronized(stateLock) {
            val defaultNetwork = connectivityManager.activeNetwork
            val capabilities = defaultNetwork?.let(connectivityManager::getNetworkCapabilities)
            val verified = reconcile(config, defaultNetwork, capabilities)
            val wifiConnection = connectivityManager.selectWifiNetwork(
                wifiManager.isWifiEnabled,
                defaultNetwork,
                capabilities
            )
            if (!wifiConnection.isConnected) {
                RemoteNetworkData.empty
            } else {
                // VPN values belong to the verified VPN egress, never to the underlying Wi-Fi.
                cache.data.takeIf {
                    verified != null &&
                        (verified.isDirectWifi || verified.isVpn) &&
                        cache.fetchOrigin == verified &&
                        it != RemoteNetworkData.empty
                } ?: wifiDataForIdentifiableConnection(wifiConnection.network)
            }
        }

    /** Exposes cached values only when the identified Wi-Fi Network matches their fetch origin. */
    private fun wifiDataForIdentifiableConnection(selectedNetwork: Network?): RemoteNetworkData =
        wifiCache?.takeIf { selectedNetwork != null && it.network == selectedNetwork }?.data
            ?: RemoteNetworkData.empty

    /** Called under [stateLock]; the published flow never contains data for an unconfirmed route. */
    private fun reconcile(
        config: RemoteDataConfig,
        defaultNetwork: Network? = connectivityManager.activeNetwork,
        capabilities: NetworkCapabilities? = defaultNetwork?.let(connectivityManager::getNetworkCapabilities)
    ): VerifiedDefaultNetwork? {
        val verified = reconcileNetwork(defaultNetwork, capabilities)
        reconcileConfig(config)
        publishData(verified?.network)
        return verified
    }

    /** A confirmed identity change clears cache; unavailable identity or capability only hides it. */
    private fun reconcileNetwork(network: Network?, capabilities: NetworkCapabilities?): VerifiedDefaultNetwork? {
        if (network == null) {
            // Wi-Fi visibility cannot validate the current HTTP route for the published flow.
            d { "Default network unavailable; retaining cache for ${cache.network}" }
            return null
        }

        observeDefaultNetwork(network)

        if (capabilities == null) {
            d { "Capabilities unavailable for network=$network" }
            return null
        }

        if (!capabilities.hasInternetCapability) {
            d { "Network=$network lacks INTERNET capability" }
            return null
        }

        val isDirectWifi = capabilities.isDirectWifiEgress
        val verified = VerifiedDefaultNetwork(
            network = network,
            isDirectWifi = isDirectWifi,
            isVpn = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        )
        if (cache.fetchOrigin != null && cache.fetchOrigin != verified) {
            // A transport change on the same Network handle must not relabel old HTTP values
            // as VPN data, even when the new request fails.
            cache = RemoteDataCache(network = network)
            pendingRestorationNetwork = network
        }
        restoreWifiCacheIfPending(network, isDirectWifi)
        return verified
    }

    /** Invalidates default-route state while retaining Wi-Fi data until the new route is verified. */
    private fun observeDefaultNetwork(network: Network) {
        if (network == cache.network) return

        i { "Default network changed: ${cache.network} -> $network; invalidating cache" }
        cache = RemoteDataCache(network = network)
        pendingRestorationNetwork = network
    }

    /** Restores Wi-Fi values once after a confirmed route change, leaving fresh HTTP recovery due. */
    private fun restoreWifiCacheIfPending(network: Network, isDirectWifi: Boolean) {
        if (pendingRestorationNetwork != network) return

        if (isDirectWifi && wifiCache?.network == network) {
            // A -> B -> A restores values once but requires fresh HTTP attempts. Temporary
            // A -> unavailable -> A leaves cache.network unchanged and never enters here.
            cache = checkNotNull(wifiCache).requiringRecoveryAfterRouteChange()
        }
        pendingRestorationNetwork = null
    }

    private fun reconcileConfig(config: RemoteDataConfig) {
        cache = cache.reconciledFor(config)
        wifiCache = wifiCache?.reconciledFor(config)
    }

    override fun needsRecovery(config: RemoteDataConfig): Boolean =
        synchronized(stateLock) {
            val network = reconcile(config)
            (config.requiresIpApi || config.requiresPublicIp) &&
                (network == null || !cache.isRecoverySatisfied(config))
        }

    override suspend fun refresh(onlyIfMissing: Boolean): RemoteRefreshResult =
        refresh(onlyIfMissing, forWidget = false)

    override suspend fun refreshForWidget(onlyIfMissing: Boolean): RemoteRefreshResult =
        refresh(onlyIfMissing, forWidget = true)

    /** Applies widget route eligibility under the same lock that creates the HTTP fetch plan. */
    private suspend fun refresh(onlyIfMissing: Boolean, forWidget: Boolean): RemoteRefreshResult {
        d { "Remote data refresh started: onlyIfMissing=$onlyIfMissing, forWidget=$forWidget" }
        return refreshMutex.withLock {
            val config = widgetConfigSource.snapshot().remoteDataConfig
            val (network, fetchPlan) = synchronized(stateLock) {
                val defaultNetwork = connectivityManager.activeNetwork
                val capabilities = defaultNetwork?.let(connectivityManager::getNetworkCapabilities)
                val current = reconcile(config, defaultNetwork, capabilities)
                val eligible = !forWidget ||
                    (
                        current != null &&
                            (current.isDirectWifi || current.isVpn) &&
                            connectivityManager.selectWifiNetwork(
                                wifiManager.isWifiEnabled,
                                defaultNetwork,
                                capabilities
                            ).isConnected
                        )
                val target = current?.takeIf { eligible }
                target to target?.let { cache.fetchPlan(config, onlyIfMissing) }
            }
            if (network == null) {
                d { "Remote data refresh aborted: no eligible default network" }
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
            .log { result -> "Remote data refresh finished: onlyIfMissing=$onlyIfMissing, forWidget=$forWidget, result=$result" }
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
        network: VerifiedDefaultNetwork,
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

            cache = cache.withFetchResults(config, results).copy(fetchOrigin = current)
            if (current.isDirectWifi) {
                // Both fetch-boundary checks saw this same direct Wi-Fi default. Never assign
                // cellular or VPN responses to a visible underlying Wi-Fi connection.
                wifiCache = cache
            }

            publishData(current.network)
            cache.recoveryResult(config)
        }

    private fun publishData(network: Network?) {
        data.value = if (network == null) RemoteNetworkData.empty else cache.data
    }
}

/** A default HTTP route whose identity and INTERNET capability were checked for this read. */
private data class VerifiedDefaultNetwork(val network: Network, val isDirectWifi: Boolean, val isVpn: Boolean)

/**
 * Values and initialization originate from one confirmed default-egress network and config.
 * Reconciliation can retain applicable values while resetting initialization for a changed
 * IP-API config or removing attempts for disabled public-IP versions.
 */
private data class RemoteDataCache(
    val network: Network? = null,
    val config: RemoteDataConfig? = null,
    val data: RemoteNetworkData = RemoteNetworkData.empty,
    /** Egress of the last fetch attempt; retained values cannot be reinterpreted after a transport change. */
    val fetchOrigin: VerifiedDefaultNetwork? = null,
    val ipApiInitializedForConfig: Boolean = false,
    val attemptedIpVersions: Set<IpAddress.Version> = emptySet()
) {
    /** Keeps retrieved values while requiring new HTTP attempts on a restored default route. */
    fun requiringRecoveryAfterRouteChange(): RemoteDataCache =
        copy(ipApiInitializedForConfig = false, attemptedIpVersions = emptySet())

    /** Removes values invalidated by changed remote requirements without marking a fetch complete. */
    fun reconciledFor(config: RemoteDataConfig): RemoteDataCache {
        val previous = this.config
        if (previous == config) return this
        val sameIpApiRequirements = previous != null &&
            previous.ipApiProperties == config.ipApiProperties &&
            previous.locationParameters == config.locationParameters
        return copy(
            config = config,
            data = config.retainApplicable(data, previous),
            ipApiInitializedForConfig = ipApiInitializedForConfig && sameIpApiRequirements,
            attemptedIpVersions = attemptedIpVersions.intersect(config.publicIpVersions)
        )
    }

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
