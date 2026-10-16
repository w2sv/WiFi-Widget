package com.w2sv.networking.remotenetworkinfo

import com.w2sv.domain.model.networking.IpAddress
import com.w2sv.domain.model.networking.IpApiData
import com.w2sv.domain.model.networking.RemoteWifiData
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.model.wifiproperty.WifiProperty
import com.w2sv.domain.model.wifiproperty.settings.LocationParameter
import com.w2sv.domain.model.wifiproperty.settings.enabledVersions

/** Maps enabled widget properties to remote requirements and filters cached values after changes. */
internal data class RemoteDataSelection(
    val ipApiProperties: Set<WifiProperty>,
    val publicIpVersions: Set<IpAddress.Version>,
    val locationParameters: Set<LocationParameter>
) {
    val requiresIpApi get() = ipApiProperties.isNotEmpty()
    val requiresPublicIp get() = publicIpVersions.isNotEmpty()

    fun retainApplicable(data: RemoteWifiData, previous: RemoteDataSelection?): RemoteWifiData =
        data.copy(
            ipApiData = data.ipApiData?.takeIf { requiresIpApi }?.keepEnabled()
                ?.let { if (previous?.locationParameters != locationParameters) it.copy(location = null) else it },
            publicIps = data.publicIps.filter { it.version in publicIpVersions }
        )

    fun retainApplicable(data: IpApiData): IpApiData =
        data.keepEnabled()

    private fun IpApiData.keepEnabled() =
        copy(
            location = location.takeIf { WifiProperty.Location in ipApiProperties },
            gpsCoordinates = gpsCoordinates.takeIf { WifiProperty.IpGpsLocation in ipApiProperties },
            isp = isp.takeIf { WifiProperty.ISP in ipApiProperties }.orEmpty(),
            asn = asn.takeIf { WifiProperty.ASN in ipApiProperties }
        )

    companion object {
        fun from(config: WidgetConfig): RemoteDataSelection =
            RemoteDataSelection(
                ipApiProperties = ipApiRequiringProperties.filterTo(mutableSetOf(), config::isEnabled),
                publicIpVersions = buildSet {
                    if (config.isEnabled(WifiProperty.PublicIp)) {
                        addAll(config.enabledIpSettings(WifiProperty.PublicIp).enabledVersions())
                    }
                },
                locationParameters = buildSet {
                    if (config.isEnabled(WifiProperty.Location)) addAll(config.enabledLocationParameters())
                }
            )

        private val ipApiRequiringProperties =
            setOf(WifiProperty.Location, WifiProperty.IpGpsLocation, WifiProperty.ISP, WifiProperty.ASN)
    }
}
