package com.w2sv.networking.fetching.public_ip

import com.w2sv.domain.model.networking.IpAddress
import com.w2sv.domain.model.wifiproperty.WifiProperty
import com.w2sv.domain.model.wifiproperty.settings.enabledVersions
import com.w2sv.domain.repository.WidgetConfigFlow
import com.w2sv.networking.fetching.fetchFromUrl
import com.w2sv.networking.toDomain
import java.io.IOException
import java.net.InetAddress
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import slimber.log.e
import slimber.log.i

@Singleton
internal class PublicIpRepository @Inject constructor(
    private val httpClient: OkHttpClient,
    private val widgetConfigFlow: WidgetConfigFlow
) {
    private val _data = MutableStateFlow<Map<IpAddress.Version, IpAddress?>>(emptyMap())
    val data: StateFlow<Map<IpAddress.Version, IpAddress?>> get() = _data

    suspend fun refresh() =
        coroutineScope {
            val config = widgetConfigFlow.first()
            if (!config.isEnabled(WifiProperty.PublicIp)) {
                _data.value = emptyMap()
                return@coroutineScope
            }

            val enabledVersions = config.enabledIpSettings(WifiProperty.PublicIp).enabledVersions()
            val results = enabledVersions.associateWith { version ->
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

private fun publicAddressFetchUrl(version: IpAddress.Version): String =
    when (version) {
        IpAddress.Version.V4 -> "https://api.ipify.org"
        IpAddress.Version.V6 -> "https://api6.ipify.org"
    }
