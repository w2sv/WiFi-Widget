package com.w2sv.networking.remotenetworkinfo

import com.w2sv.domain.model.networking.IpApiData
import com.w2sv.domain.model.networking.RemoteDataConfig
import com.w2sv.domain.model.networking.RemoteWifiData
import com.w2sv.domain.model.wifiproperty.WifiProperty

/** Retains only values applicable to the current requirements on the same network. */
internal fun RemoteDataConfig.retainApplicable(data: RemoteWifiData, previous: RemoteDataConfig?): RemoteWifiData =
    data.copy(
        ipApiData = data.ipApiData
            ?.takeIf { requiresIpApi }
            ?.let(::retainApplicable)
            ?.let {
                if (previous?.locationParameters != locationParameters) it.copy(location = null) else it
            },
        publicIps = data.publicIps.filter { it.version in publicIpVersions }
    )

internal fun RemoteDataConfig.retainApplicable(data: IpApiData): IpApiData =
    data.copy(
        location = data.location.takeIf { WifiProperty.Location in ipApiProperties },
        gpsCoordinates = data.gpsCoordinates.takeIf { WifiProperty.IpGpsLocation in ipApiProperties },
        isp = data.isp.takeIf { WifiProperty.ISP in ipApiProperties }.orEmpty(),
        asn = data.asn.takeIf { WifiProperty.ASN in ipApiProperties }
    )
