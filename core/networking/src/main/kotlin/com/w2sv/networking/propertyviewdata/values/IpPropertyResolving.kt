package com.w2sv.networking.propertyviewdata.values

import com.w2sv.common.txt
import com.w2sv.domain.model.networking.IpAddress
import com.w2sv.domain.model.wifiproperty.WifiProperty
import com.w2sv.domain.model.wifiproperty.settings.IpSetting
import com.w2sv.domain.model.wifiproperty.settings.enabledVersions

internal fun WifiProperty.IpProperty.resolve(
    publicIps: Map<IpAddress.Version, IpAddress?>,
    systemIps: List<IpAddress>,
    enabledIpSettings: (WifiProperty.IpProperty) -> List<IpSetting>
): List<WifiPropertyValue> {
    val enabledSettings = enabledIpSettings(this)

    val showPrefix = IpSetting.ShowPrefixLength in enabledSettings
    val showSubnet = IpSetting.ShowSubnetMask in enabledSettings

    val addresses = getAddresses(
        systemIps = systemIps,
        publicIps = publicIps,
        enabledVersions = enabledSettings.enabledVersions()
    )

    return addresses.map { address ->
        WifiPropertyValue(
            value = address.hostAddressRepresentation.txt,
            subValues = buildList {
                if (showPrefix && address.prefixLength != null) add("/${address.prefixLength}")
                if (showSubnet) address.asV4OrNull?.subnetMask?.let(::add)
            }
        )
    }
}

private fun WifiProperty.IpProperty.getAddresses(
    systemIps: List<IpAddress>,
    publicIps: Map<IpAddress.Version, IpAddress?>,
    enabledVersions: Collection<IpAddress.Version>
): List<IpAddress> = when (this) {
    WifiProperty.ULA -> systemIps.filter { it.asV6OrNull?.isUniqueLocal == true }
    WifiProperty.GUA -> systemIps.filter { it.asV6OrNull?.isGlobalUnicast == true }
    WifiProperty.LoopbackIp -> systemIps.filterByVersionAndPredicate(enabledVersions) { it.isLoopback }
    WifiProperty.SiteLocalIp -> systemIps.filterByVersionAndPredicate(enabledVersions) { it.isSiteLocal }
    WifiProperty.LinkLocalIp -> systemIps.filterByVersionAndPredicate(enabledVersions) { it.isLinkLocal }
    WifiProperty.MulticastIp -> systemIps.filterByVersionAndPredicate(enabledVersions) { it.isMulticast }
    WifiProperty.PublicIp -> enabledVersions.mapNotNull { version -> publicIps.getValue(version) }
}

private inline fun List<IpAddress>.filterByVersionAndPredicate(
    versionsToBeIncluded: Collection<IpAddress.Version>,
    predicate: (IpAddress) -> Boolean
): List<IpAddress> =
    filter { predicate(it) && versionsToBeIncluded.contains(it.version) }
