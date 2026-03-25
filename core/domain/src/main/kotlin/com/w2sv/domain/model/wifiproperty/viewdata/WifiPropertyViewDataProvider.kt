package com.w2sv.domain.model.wifiproperty.viewdata

import com.w2sv.domain.model.networking.RemoteWifiData
import com.w2sv.domain.model.wifiproperty.WifiProperty
import com.w2sv.domain.model.wifiproperty.settings.IpSetting

fun interface WifiPropertyViewDataProvider {
    /**
     * @return List of [WifiPropertyViewData], the element-order of which corresponds to the one of the [enabledProperties].
     * One [WifiProperty] may result in the the creation of multiple [WifiPropertyViewData] elements.
     */
    suspend operator fun invoke(
        enabledProperties: List<WifiProperty>,
        enabledIpSettings: (WifiProperty.IpProperty) -> List<IpSetting>,
        remoteWifiData: RemoteWifiData
    ): List<WifiPropertyViewData>
}
