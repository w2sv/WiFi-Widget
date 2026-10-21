package com.w2sv.domain.networking

import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.settings.LocationParameter
import com.w2sv.domain.wifiproperty.settings.enabledVersions
import com.w2sv.kotlinutils.filterToSet

/**
 * Remote-data requirements derived from a widget configuration, independent of cache state.
 * The repository decides which required values are already available or need fetching.
 */
@ConsistentCopyVisibility
data class RemoteDataConfig private constructor(
    val ipApiProperties: Set<WifiProperty>,
    val publicIpVersions: Set<IpAddress.Version>,
    val locationParameters: Set<LocationParameter>
) {
    val requiresIpApi get() = ipApiProperties.isNotEmpty()
    val requiresPublicIp get() = publicIpVersions.isNotEmpty()

    companion object {
        fun from(config: WidgetConfig): RemoteDataConfig =
            RemoteDataConfig(
                ipApiProperties = ipApiRequiringProperties.filterToSet(config::isEnabled),
                publicIpVersions = buildSet {
                    if (config.isEnabled(WifiProperty.PublicIp)) {
                        addAll(config.enabledIpSettings(WifiProperty.PublicIp).enabledVersions())
                    }
                },
                locationParameters = buildSet {
                    if (config.isEnabled(WifiProperty.Location)) {
                        addAll(config.enabledLocationParameters())
                    }
                }
            )
    }
}

private val ipApiRequiringProperties =
    setOf(WifiProperty.Location, WifiProperty.IpCoordinates, WifiProperty.ISP, WifiProperty.ASN)
