package com.w2sv.domain.wifiproperty

import com.w2sv.domain.networking.RemoteWifiData
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.settings.IpSetting
import com.w2sv.domain.wifiproperty.viewdata.WifiPropertyViewData

fun interface WifiPropertyViewDataProvider {
    /**
     * @return List of [WifiPropertyViewData], the element-order of which corresponds to the one of the [enabledProperties].
     * One [WifiProperty] may result in the creation of multiple [WifiPropertyViewData] elements.
     */
    suspend operator fun invoke(
        enabledProperties: List<WifiProperty>,
        enabledIpSettings: (WifiProperty.IpProperty) -> List<IpSetting>,
        remoteWifiData: RemoteWifiData
    ): List<WifiPropertyViewData>
}
