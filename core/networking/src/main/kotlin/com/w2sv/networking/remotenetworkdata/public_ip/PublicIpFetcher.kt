package com.w2sv.networking.remotenetworkdata.public_ip

import com.w2sv.domain.networking.IpAddress
import com.w2sv.networking.remotenetworkdata.fetchFromUrl
import com.w2sv.networking.toDomain
import java.net.InetAddress
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import slimber.log.i

internal class PublicIpFetcher @Inject constructor(private val httpClient: OkHttpClient) {

    suspend fun fetch(versions: Set<IpAddress.Version>): Map<IpAddress.Version, Result<IpAddress>> =
        coroutineScope {
            versions
                .map { version ->
                    async {
                        version to fetchAndMapOnSuccess(version)
                    }
                }
                .awaitAll()
                .toMap()
        }

    /** Invalid responses and addresses of the wrong version become failed results. */
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
