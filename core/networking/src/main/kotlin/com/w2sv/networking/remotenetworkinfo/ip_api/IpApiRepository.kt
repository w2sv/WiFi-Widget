package com.w2sv.networking.remotenetworkinfo.ip_api

import com.w2sv.common.utils.log
import com.w2sv.domain.model.networking.IpApiData
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.model.wifiproperty.WifiProperty
import com.w2sv.domain.model.wifiproperty.settings.LocationParameter
import com.w2sv.domain.repository.WidgetConfigFlow
import com.w2sv.networking.remotenetworkinfo.fetchFromUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class IpApiRepository @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json,
    private val widgetConfigFlow: WidgetConfigFlow
) {
    private val _data = MutableStateFlow<IpApiData?>(null)
    val data: StateFlow<IpApiData?> = _data

    suspend fun refresh() {
        val widgetConfig = widgetConfigFlow.first()
        if (!widgetConfig.isIpApiDataRequired) {
            _data.value = null
            return
        }

        val enabledLocationParameters = widgetConfig.enabledLocationParameters()
        _data.value = fetchIpApiData(enabledLocationParameters)
    }

    private suspend fun fetchIpApiData(enabledLocationParameters: Collection<LocationParameter>): IpApiData? {
        val response = client.fetchFromUrl("http://ip-api.com/json?fields=5828601") { jsonString ->
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

private val WidgetConfig.isIpApiDataRequired: Boolean
    get() = ipApiRequiringProperties.any { isEnabled(it) }

private val ipApiRequiringProperties =
    setOf(
        WifiProperty.Location,
        WifiProperty.IpGpsLocation,
        WifiProperty.ISP,
        WifiProperty.ASN
    )
