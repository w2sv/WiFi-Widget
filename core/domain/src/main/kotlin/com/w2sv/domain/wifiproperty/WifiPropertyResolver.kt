package com.w2sv.domain.wifiproperty

import com.w2sv.domain.networking.RemoteNetworkData
import com.w2sv.domain.wifiproperty.settings.IpSetting

fun interface WifiPropertyResolver {
    /** Resolves each requested property in order, including properties with no values. */
    suspend operator fun invoke(
        enabledProperties: List<WifiProperty>,
        enabledIpSettings: (WifiProperty.IpProperty) -> List<IpSetting>,
        remoteNetworkData: RemoteNetworkData
    ): List<ResolvedWifiProperty>
}
