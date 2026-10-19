package com.w2sv.networking.remotenetworkdata.ip_api

import com.w2sv.domain.networking.IpApiData
import com.w2sv.domain.wifiproperty.settings.LocationParameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data fetched from json retrieved from [ip-api](http://ip-api.com/json?fields=5828601).
 * [Field documentation](https://ip-api.com/docs/api:json)
 * Missing latitude or longitude means the API did not provide a usable coordinate pair.
 *
 * @property status `success` or `fail`
 * @property message fail reason, if any
 */
@Serializable
internal data class IpApiResponse(
    val status: String = "",
    val message: String = "",
    val continent: String = "",
    val country: String = "",
    val regionName: String = "",
    val city: String = "",
    val district: String = "",
    val zip: String = "",
    val lat: Double? = null,
    val lon: Double? = null,
    val timezone: String = "",
    val isp: String = "",
    @SerialName("as") val asNumberAndOrganization: String = ""
) {
    fun toDomain(parameters: Collection<LocationParameter>): IpApiData =
        IpApiData(
            location = location(parameters),
            gpsCoordinates = gpsCoordinates(),
            timezone = timezone,
            isp = isp,
            asn = asn()
        )

    private fun location(parameters: Collection<LocationParameter>): String? =
        mapOf(
            LocationParameter.ZipCode to zip,
            LocationParameter.District to district,
            LocationParameter.City to city,
            LocationParameter.Region to regionName,
            LocationParameter.Country to country,
            LocationParameter.Continent to continent
        )
            .map { (parameter, field) -> field.takeIf { parameters.contains(parameter) } }
            .filter { !it.isNullOrBlank() }
            .joinToString(", ")
            .ifBlank { null }

    private fun gpsCoordinates(): String? =
        if (lat != null && lon != null) "$lat, $lon" else null

    private fun asn(): String? =
        asNumberAndOrganization.run { substringBefore(" ").takeIf { isNotBlank() } }
}
