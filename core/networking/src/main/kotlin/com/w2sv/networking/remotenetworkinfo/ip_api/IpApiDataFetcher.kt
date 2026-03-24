package com.w2sv.networking.remotenetworkinfo.ip_api

import com.w2sv.common.utils.log
import com.w2sv.domain.model.networking.IpApiData
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.model.wifiproperty.WifiProperty
import com.w2sv.networking.remotenetworkinfo.ConditionalFetcher
import com.w2sv.networking.remotenetworkinfo.fetchFromUrl
import java.io.IOException
import javax.inject.Inject
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

private const val IP_API_URL = "http://ip-api.com/json?fields=5828601"

internal class IpApiDataFetcher @Inject constructor(private val client: OkHttpClient, private val json: Json) :
    ConditionalFetcher<IpApiData?>() {

    override fun shouldFetch(config: WidgetConfig): Boolean =
        ipApiRequiringProperties.any { config.isEnabled(it) }

    override suspend fun performFetch(config: WidgetConfig): IpApiData? {
        val enabledLocationParameters = config.enabledLocationParameters()
        val response = client.fetchFromUrl(IP_API_URL) { jsonString ->
            json
                .decodeFromString<IpApiResponse>(jsonString)
                .log { "Fetched $it" }
                .also {
                    if (it.status == "fail") {
                        throw IOException("IpApi failed: ${it.message}")
                    }
                }
        }

        return response.getOrNull()?.toDomain(enabledLocationParameters)
    }
}

private val ipApiRequiringProperties =
    setOf(
        WifiProperty.Location,
        WifiProperty.IpGpsLocation,
        WifiProperty.ISP,
        WifiProperty.ASN
    )
