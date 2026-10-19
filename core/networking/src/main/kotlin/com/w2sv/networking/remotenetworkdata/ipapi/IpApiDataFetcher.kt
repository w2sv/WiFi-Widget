package com.w2sv.networking.remotenetworkdata.ipapi

import com.w2sv.common.utils.log
import com.w2sv.domain.networking.IpApiData
import com.w2sv.domain.wifiproperty.settings.LocationParameter
import com.w2sv.networking.remotenetworkdata.fetchFromUrl
import java.io.IOException
import javax.inject.Inject
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

private const val IP_API_URL = "http://ip-api.com/json?fields=5828601"

internal class IpApiDataFetcher @Inject constructor(private val client: OkHttpClient, private val json: Json) {

    suspend fun fetch(locationParameters: Set<LocationParameter>): Result<IpApiData> =
        client.fetchFromUrl(IP_API_URL) { jsonString ->
            json
                .decodeFromString<IpApiResponse>(jsonString)
                .log { "Fetched $it" }
                .also {
                    if (it.status == "fail") {
                        throw IOException("IpApi failed: ${it.message}")
                    }
                }
                .toDomain(locationParameters)
        }
}
