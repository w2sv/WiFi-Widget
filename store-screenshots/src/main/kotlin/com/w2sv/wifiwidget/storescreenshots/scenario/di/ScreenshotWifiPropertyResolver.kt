package com.w2sv.wifiwidget.storescreenshots.scenario.di

import com.w2sv.domain.networking.RemoteNetworkData
import com.w2sv.domain.wifiproperty.ResolvedWifiProperty
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.WifiPropertyResolver
import com.w2sv.domain.wifiproperty.settings.IpSetting
import com.w2sv.wifiwidget.storescreenshots.scenario.ActiveScreenshotScenario
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenshotWifiPropertyResolver @Inject constructor(private val activeScenario: ActiveScreenshotScenario) : WifiPropertyResolver {

    override suspend fun invoke(
        enabledProperties: List<WifiProperty>,
        enabledIpSettings: (WifiProperty.IpProperty) -> List<IpSetting>,
        remoteNetworkData: RemoteNetworkData
    ): List<ResolvedWifiProperty> {
        val resolutionsByProperty = activeScenario.current.properties.associateBy { it.property }
        return enabledProperties.map { property ->
            checkNotNull(resolutionsByProperty[property]) {
                "No screenshot value for requested Wi-Fi property $property"
            }
        }
    }
}
