package com.w2sv.widget.ui.properties.renderdata

import com.w2sv.core.resources.R
import com.w2sv.domain.networking.NetworkAvailabilitySnapshot
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.WifiPropertyResolutionError.BatterySaverRestricted
import com.w2sv.domain.wifiproperty.WifiPropertyValue
import com.w2sv.domain.wifiproperty.txt
import com.w2sv.kotlinutils.makeIf

/**
 * Returns a widget-only placeholder when this property's missing value may reflect restricted
 * network access. Battery Saver and connected Wi-Fi are both required; the snapshot alone cannot
 * prove why access is missing. Foreground presentation does not use this extension.
 */
internal fun NetworkAvailabilitySnapshot.batterySaverPlaceholderFor(property: WifiProperty): WifiPropertyValue? {
    if (!isBatterySaverEnabled || !isWifiConnected) return null

    val isRestricted = when (property) {
        WifiProperty.PublicIp,
        WifiProperty.Location,
        WifiProperty.IpCoordinates,
        WifiProperty.ISP,
        WifiProperty.ASN -> !hasUsableDefaultNetwork

        WifiProperty.ULA,
        WifiProperty.GUA,
        WifiProperty.LoopbackIp,
        WifiProperty.SiteLocalIp,
        WifiProperty.LinkLocalIp,
        WifiProperty.MulticastIp,
        WifiProperty.NAT64Prefix -> !hasLinkProperties

        else -> false
    }

    // Accessible LinkProperties can simply lack a particular address type (for example ULA).
    return makeIf(isRestricted) {
        WifiPropertyValue(
            value = R.string.battery_saver_restricted.txt,
            resolutionError = BatterySaverRestricted
        )
    }
}
