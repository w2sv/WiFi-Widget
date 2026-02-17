package com.w2sv.networking

import com.w2sv.common.utils.log
import com.w2sv.domain.model.IpApiData
import com.w2sv.domain.model.LocationParameter
import com.w2sv.domain.model.WifiProperty
import com.w2sv.domain.repository.WidgetConfigRepository
import com.w2sv.networking.extensions.fetchFromUrl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class IpApiRepository @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json,
    private val widgetConfigRepository: WidgetConfigRepository
) {

    private val _data = MutableStateFlow<IpApiData?>(null)
    val data: StateFlow<IpApiData?> = _data

    suspend fun refresh() {
        val required = widgetConfigRepository.isIpApiDataRequired.first()
        if (!required) {
            _data.value = null
            return
        }

        val enabledLocationParameters = widgetConfigRepository.enabledLocationParameters.first()
        _data.value = fetchIpApiData(enabledLocationParameters)
    }

    private suspend fun fetchIpApiData(enabledLocationParameters: Set<LocationParameter>): IpApiData? {
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

private val ipApiRequiringProperties =
    setOf(
        WifiProperty.NonIP.Location,
        WifiProperty.NonIP.IpGpsLocation,
        WifiProperty.NonIP.ISP,
        WifiProperty.NonIP.ASN
    )

// TODO use map instead of list
private val WidgetConfigRepository.isIpApiDataRequired: Flow<Boolean>
    get() = sortedEnabledWifiProperties
        .map { enabledProperties -> enabledProperties.any { it in ipApiRequiringProperties } }
