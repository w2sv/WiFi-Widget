package com.w2sv.networking.remotenetworkinfo

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.w2sv.domain.model.networking.IpAddress
import com.w2sv.domain.model.networking.IpApiData
import com.w2sv.domain.model.networking.RemoteRefreshResult
import com.w2sv.domain.model.networking.RemoteWifiData
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.model.wifiproperty.WifiProperty
import com.w2sv.domain.model.wifiproperty.settings.LocationParameter
import com.w2sv.domain.model.wifiproperty.settings.enabledVersions
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

    // The mutex orders worker and home-screen refreshes. Synchronous snapshots use the same
    // stateLock as refresh commits, but never hold it across suspended HTTP calls.
    private val refreshMutex = Mutex()
    private val stateLock = Any()
    private var network: Network? = null
    private var cached = RemoteWifiData.empty
    private var selection: Selection? = null
    private var ipApiInitialized = false

    private data class Selection(
        val apiProperties: Set<WifiProperty>,
        val versions: Set<IpAddress.Version>,
        val locationParameters: Set<LocationParameter>
    ) {
        val requiresIpApi get() = apiProperties.isNotEmpty()
        val requiresPublicIp get() = versions.isNotEmpty()
    }

    private fun configuration(config: WidgetConfig): Selection =
        Selection(
            apiProperties = setOf(WifiProperty.Location, WifiProperty.IpGpsLocation, WifiProperty.ISP, WifiProperty.ASN)
                .filterTo(mutableSetOf(), config::isEnabled),
            versions = buildSet {
                if (config.isEnabled(WifiProperty.PublicIp)) {
                    addAll(config.enabledIpSettings(WifiProperty.PublicIp).enabledVersions())
                }
            },
            locationParameters = buildSet {
                if (config.isEnabled(WifiProperty.Location)) {
                    addAll(config.enabledLocationParameters())
                }
            }
        )

    private fun IpApiData.keepEnabled(selection: Selection) =
        copy(
            location = location.takeIf { WifiProperty.Location in selection.apiProperties },
            gpsCoordinates = gpsCoordinates.takeIf { WifiProperty.IpGpsLocation in selection.apiProperties },
            isp = isp.takeIf { WifiProperty.ISP in selection.apiProperties }.orEmpty(),
            asn = asn.takeIf { WifiProperty.ASN in selection.apiProperties }
        )

    /**
     * Ordinary HTTP follows the application's default egress network. SSID/BSSID identifies the
     * Wi-Fi association, not that route; a VPN or cellular default may have a different public IP.
     * VPN is therefore an eligible egress context, scoped by its own Network identity. A
     * temporarily missing identity/capability withholds data without proving the route changed.
     * Called only under [stateLock]. Missing identity hides but retains the last known cache.
     */
    private fun validate(config: WidgetConfig): Network? {
        val observed = connectivityManager.activeNetwork
        if (observed == null) {
            d { "Remote data validation: activeNetwork is missing; retaining cache for network=$network" }
        }
        if (observed != null && observed != network) {
            // The identity alone confirms a route change even if capabilities are temporarily
            // unavailable. Only a wholly missing identity leaves the old cache intact.
            i { "Remote data validation: network changed from $network to $observed; invalidating cache" }
            network = observed
            cached = RemoteWifiData.empty
            selection = null
            ipApiInitialized = false
            _data.value = RemoteWifiData.empty
        }
        val capabilities = observed?.let { connectivityManager.getNetworkCapabilities(it) }
        val current = when {
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
        if (current == null) {
            // Unknown identity is not proof that the old cache is invalid. Retain it privately,
            // but publish only displayable data so data.value and snapshot agree. A route-loss
            // render may briefly show no remote fields; restoring the same identity restores them.
            _data.value = RemoteWifiData.empty
            return null
        }
        val enabled = configuration(config)
        val previous = selection
        if (previous?.apiProperties != enabled.apiProperties ||
            previous.locationParameters != enabled.locationParameters
        ) {
            // Keep still-enabled values visible, but a new IP-API selection needs its own success.
            ipApiInitialized = false
        }
        cached = cached.copy(
            ipApiData = cached.ipApiData
                ?.takeIf { enabled.requiresIpApi }
                ?.keepEnabled(enabled)
                ?.let { data ->
                    if (previous?.locationParameters != enabled.locationParameters) data.copy(location = null) else data
                },
            publicIps = cached.publicIps.filter { it.version in enabled.versions }
        )
        selection = enabled
        _data.value = cached
        return current
    }

    override fun snapshot(config: WidgetConfig): RemoteWifiData =
        synchronized(stateLock) {
            validate(config)
            _data.value
        }

    private fun isComplete(enabled: Selection): Boolean =
        (!enabled.requiresIpApi || ipApiInitialized) &&
            // One successful family initializes public IP; unsupported IPv6 must not cause
            // endless recovery when IPv4 is already available. IP-API is independent.
            (!enabled.requiresPublicIp || publicIpInitialized(enabled))

    private fun publicIpInitialized(enabled: Selection): Boolean =
        cached.publicIps.any { it.version in enabled.versions }

    override fun needsRecovery(config: WidgetConfig): Boolean =
        synchronized(stateLock) {
            val current = validate(config)
            val enabled = configuration(config)
            (enabled.requiresIpApi || enabled.requiresPublicIp) &&
                (current == null || !isComplete(enabled))
        }

    override suspend fun refresh(onlyIfMissing: Boolean): RemoteRefreshResult {
        d { "Remote data refresh started: onlyIfMissing=$onlyIfMissing" }
        val result = refreshMutex.withLock {
            val config = widgetConfigReader.read()
            val start = synchronized(stateLock) { validate(config) } ?: run {
                d { "Remote data refresh aborted: no usable default network" }
                return@withLock RemoteRefreshResult.NetworkUnavailable
            }
            // Recheck *inside* the mutex: a home/manual refresh may have completed while this
            // recovery worker waited, so it must not make the same HTTP requests again.
            if (onlyIfMissing && synchronized(stateLock) { isComplete(configuration(config)) }) {
                d { "Remote data refresh skipped: enabled data already complete for network=$start" }
                return@withLock RemoteRefreshResult.Complete
            }

            val (fetchApi, fetchPublicIp) = synchronized(stateLock) {
                val enabled = configuration(config)
                // Recovery fills missing groups independently. A successful public IP must not
                // be fetched again while only IP-API is missing, and vice versa. Ordinary
                // refreshes still update every enabled group.
                (!onlyIfMissing || (enabled.requiresIpApi && !ipApiInitialized)) to
                    (!onlyIfMissing || (enabled.requiresPublicIp && !publicIpInitialized(enabled)))
            }

            val (ipApi, publicIps) = coroutineScope {
                val api = if (fetchApi) async { ipApiDataFetcher.fetchIfNecessary(config) } else null
                val ips = if (fetchPublicIp) async { publicIpFetcher.fetchIfNecessary(config) } else null
                api?.await() to ips?.await()
            }
            val latestConfig = widgetConfigReader.read()
            synchronized(stateLock) {
                val end = validate(latestConfig)
                // Comparing before/after prevents known stale commits, although it cannot prove the
                // route of every in-flight HTTP packet. Binding OkHttp to a Network would also alter
                // VPN behavior and is intentionally outside this focused change.
                if (end == null || end != start) {
                    d { "Remote data refresh discarded: network changed or became unavailable; start=$start, end=$end" }
                    return@synchronized RemoteRefreshResult.NetworkUnavailable
                }
                // Configuration can change during HTTP too; never publish an old selection.
                if (configuration(latestConfig) != configuration(config)) {
                    d { "Remote data refresh discarded: enabled properties changed during fetch" }
                    return@synchronized if (isComplete(configuration(latestConfig))) {
                        RemoteRefreshResult.Complete
                    } else {
                        RemoteRefreshResult.Incomplete
                    }
                }
                val enabled = configuration(config)
                // A failed request preserves successful values from this same egress context.
                cached = cached.copy(
                    ipApiData = when (ipApi) {
                        is FetchResult.Success -> ipApi.value.keepEnabled(enabled)
                        FetchResult.Failed -> cached.ipApiData
                        FetchResult.Skipped -> null
                        null -> cached.ipApiData
                    },
                    publicIps = when (publicIps) {
                        is FetchResult.Success -> enabled.versions.mapNotNull { version ->
                            when (val result = publicIps.value[version]) {
                                is FetchResult.Success -> result.value
                                else -> cached.publicIps.firstOrNull { it.version == version }
                            }
                        }

                        FetchResult.Failed -> cached.publicIps
                        FetchResult.Skipped -> emptyList()
                        null -> cached.publicIps
                    }
                )
                if (ipApi is FetchResult.Success) ipApiInitialized = true
                _data.value = cached
                // Completing HTTP calls is not initialization: failures can leave a required group
                // missing even while the other group has useful data to render.
                if (isComplete(enabled)) RemoteRefreshResult.Complete else RemoteRefreshResult.Incomplete
            }
        }
        i { "Remote data refresh finished: onlyIfMissing=$onlyIfMissing, result=$result" }
        return result
    }
}
