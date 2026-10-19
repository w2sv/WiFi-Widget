package com.w2sv.domain.wifiproperty.presentation

import com.w2sv.domain.networking.RemoteWifiData
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.settings.IpSetting

fun interface WifiPropertyPresentationProvider {
    /**
     * @return List of [WifiPropertyPresentation], the element-order of which corresponds to the one of the [enabledProperties].
     * One [WifiProperty] may result in the creation of multiple [WifiPropertyPresentation] elements.
     */
    suspend operator fun invoke(
        enabledProperties: List<WifiProperty>,
        enabledIpSettings: (WifiProperty.IpProperty) -> List<IpSetting>,
        remoteWifiData: RemoteWifiData
    ): List<WifiPropertyPresentation>
}
