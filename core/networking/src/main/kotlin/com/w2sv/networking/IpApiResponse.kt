package com.w2sv.networking

import com.w2sv.domain.model.IpApiData
import com.w2sv.domain.model.LocationParameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data fetched from json retrieved from [ip-api](http://ip-api.com/json?fields=5828601).
 * [Field documentation](https://ip-api.com/docs/api:json)
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
    val lat: Double = -1.0,
    val lon: Double = -1.0,
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

    private fun location(parameters: Collection<LocationParameter>): String =
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

    private fun gpsCoordinates(): String? =
        "$lat, $lon".takeIf { lat == -1.0 || lon == -1.0 }

    private fun asn(): String? =
        asNumberAndOrganization.run { substringBefore(" ").takeIf { isNotBlank() } }
}
