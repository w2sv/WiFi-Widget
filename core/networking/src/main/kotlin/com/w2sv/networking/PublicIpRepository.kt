package com.w2sv.networking

import com.w2sv.domain.model.IpAddress
import com.w2sv.domain.model.WifiProperty
import com.w2sv.domain.repository.WidgetConfigRepository
import com.w2sv.networking.extensions.fetchFromUrl
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import okhttp3.OkHttpClient
import slimber.log.e
import slimber.log.i
import java.io.IOException
import java.net.InetAddress
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
internal class PublicIpRepository @Inject constructor(
    private val httpClient: OkHttpClient,
    private val widgetConfigRepository: WidgetConfigRepository
) {

    private val _data = MutableStateFlow<Map<IpAddress.Version, IpAddress?>>(emptyMap())
    val data: StateFlow<Map<IpAddress.Version, IpAddress?>> get() = _data

    suspend fun refresh() = coroutineScope {
        val requiredVersions = widgetConfigRepository.requiredPublicIpVersions.first()

        val results = requiredVersions.associateWith { version ->
            async {
                fetchAndMapOnSuccess(version)
                    .getOrElse { throwable ->
                        if (throwable is CancellationException) throw throwable
                        e { throwable.toString() }
                        null
                    }
            }
        }
            .mapValues { it.value.await() }

        _data.value = results
    }

    /**
     * Fetches the public IP address string from the respective [publicAddressFetchUrl] and parses it via [InetAddress.getByName].
     *
     * @return a [Result] wrapping either
     * - [IpAddress] for valid address strings matching the [version]
     * - [java.net.UnknownHostException] if the retrieved address is invalid
     * - [IOException] if the parsed [IpAddress] type doesn't match [version]
     * - an exception thrown by [fetchFromUrl]
     */
    private suspend fun fetchAndMapOnSuccess(version: IpAddress.Version): Result<IpAddress> =
        httpClient.fetchFromUrl(publicAddressFetchUrl(version)) { address ->
            i { "Got public $version address $address" }
            InetAddress.getByName(address).toDomain(prefixLength = null)
                .also {
                    if (it.version != version) {
                        throw IOException("Obtained $version address $address of incorrect format")
                    }
                }
        }
}

private val WidgetConfigRepository.requiredPublicIpVersions: Flow<List<IpAddress.Version>>
    get() = wifiPropertyEnablementMap
        .getValue(WifiProperty.IP.Public)
        .flatMapLatest { enabled ->
            if (!enabled) return@flatMapLatest flowOf(emptyList())

            combine(
                ipSubPropertyEnablementMap.getValue(WifiProperty.IP.Public.v4EnabledSubProperty),
                ipSubPropertyEnablementMap.getValue(WifiProperty.IP.Public.v6EnabledSubProperty)
            ) { v4, v6 ->
                buildList {
                    if (v4) add(IpAddress.Version.V4)
                    if (v6) add(IpAddress.Version.V6)
                }
            }
        }

private fun publicAddressFetchUrl(version: IpAddress.Version): String =
    when (version) {
        IpAddress.Version.V4 -> "https://api.ipify.org"
        IpAddress.Version.V6 -> "https://api6.ipify.org"
    }
