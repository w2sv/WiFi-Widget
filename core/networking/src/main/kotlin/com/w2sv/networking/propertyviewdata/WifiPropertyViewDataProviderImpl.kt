package com.w2sv.networking.propertyviewdata

import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.w2sv.common.utils.IsGpsEnabled
import com.w2sv.domain.model.networking.RemoteNetworkInfo
import com.w2sv.domain.model.wifiproperty.WifiProperty
import com.w2sv.domain.model.wifiproperty.settings.IpSetting
import com.w2sv.domain.model.wifiproperty.viewdata.SubscriptableText
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewData
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewDataProvider
import com.w2sv.networking.linkProperties
import com.w2sv.networking.propertyviewdata.values.WifiPropertyValue
import com.w2sv.networking.propertyviewdata.values.resolve
import com.w2sv.networking.systemIpAddresses
import javax.inject.Inject

internal class WifiPropertyViewDataProviderImpl @Inject constructor(
    private val wifiManager: WifiManager,
    private val connectivityManager: ConnectivityManager,
    private val resources: Resources,
    private val isGpsEnabled: IsGpsEnabled
) : WifiPropertyViewDataProvider {

    @Suppress("DEPRECATION")
    override fun invoke(
        enabledProperties: List<WifiProperty>,
        enabledIpSettings: (WifiProperty.IpProperty) -> List<IpSetting>,
        remoteNetworkInfo: RemoteNetworkInfo
    ): List<WifiPropertyViewData> {
        val wifiSnapshot = WifiSnapshot(
            connectionInfo = wifiManager.connectionInfo,
            dhcpInfo = wifiManager.dhcpInfo,
            linkProperties = connectivityManager.linkProperties,
            publicIps = remoteNetworkInfo.publicIps,
            systemIps = connectivityManager.systemIpAddresses(),
            ipApiData = remoteNetworkInfo.ipApiData,
            isGpsEnabled = isGpsEnabled()
        )

        return enabledProperties.flatMap { property ->
            property.viewData(
                values = property.resolve(wifiSnapshot, enabledIpSettings),
                resources = resources
            )
        }
    }
}

private fun WifiProperty.viewData(values: List<WifiPropertyValue>, resources: Resources): List<WifiPropertyViewData> =
    if (values.size == 1) {
        listOf(viewData(values.first(), resources, null))
    } else {
        values.mapIndexed { index, value ->
            viewData(value, resources, index + 1)
        }
    }

private fun WifiProperty.viewData(
    value: WifiPropertyValue,
    resources: Resources,
    enumeration: Int?
): WifiPropertyViewData =
    WifiPropertyViewData(
        label = resolvedLabel(resources::getString, enumeration),
        value = value.value.resolve(resources),
        subValues = value.subValues,
        isError = value.isError
    )

private fun WifiProperty.resolvedLabel(resolveRes: (Int) -> String, enumeration: Int?): SubscriptableText =
    when (this) {
        is WifiProperty.IpProperty -> SubscriptableText(
            text = "IP",
            subscript = enumeratedText(
                text = resolveRes(subscriptResId),
                enumeration = enumeration
            )
        )

        else -> SubscriptableText(enumeratedText(text = resolveRes(labelRes), enumeration = enumeration))
    }

private fun enumeratedText(text: String, enumeration: Int?): String =
    buildString {
        append(text)
        enumeration?.let { append(" $it") }
    }
