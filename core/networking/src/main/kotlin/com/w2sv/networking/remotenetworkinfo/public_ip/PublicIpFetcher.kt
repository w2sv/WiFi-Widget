package com.w2sv.networking.remotenetworkinfo.public_ip

import com.w2sv.domain.model.networking.IpAddress
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.model.wifiproperty.WifiProperty
import com.w2sv.domain.model.wifiproperty.settings.enabledVersions
import com.w2sv.networking.remotenetworkinfo.ConditionalFetcher
import com.w2sv.networking.remotenetworkinfo.fetchFromUrl
import com.w2sv.networking.toDomain
import java.io.IOException
import java.net.InetAddress
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import slimber.log.i

internal class PublicIpFetcher @Inject constructor(private val httpClient: OkHttpClient) : ConditionalFetcher<List<IpAddress>>() {

    override fun shouldFetch(config: WidgetConfig): Boolean =
        config.isEnabled(WifiProperty.PublicIp)

    override suspend fun performFetch(config: WidgetConfig) =
        coroutineScope {
            val enabledVersions = config.enabledIpSettings(WifiProperty.PublicIp).enabledVersions()
            enabledVersions
                .map { version -> async { fetchAndMapOnSuccess(version).getOrNull() } }
                .awaitAll()
                .filterNotNull()
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
            InetAddress
                .getByName(address)
                .toDomain(prefixLength = null)
                .also {
                    if (it.version != version) {
                        error("Obtained $version address $address of incorrect format")
                    }
                }
        }
}

private fun publicAddressFetchUrl(version: IpAddress.Version): String =
    when (version) {
        IpAddress.Version.V4 -> "https://api.ipify.org"
        IpAddress.Version.V6 -> "https://api6.ipify.org"
    }
