package com.w2sv.domain.wifiproperty.presentation

import android.content.res.Resources
import com.w2sv.common.txt
import com.w2sv.core.common.R
import com.w2sv.domain.networking.RemoteNetworkData
import com.w2sv.domain.wifiproperty.ResolvedWifiProperty
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.WifiPropertyResolutionError
import com.w2sv.domain.wifiproperty.WifiPropertyResolver
import com.w2sv.domain.wifiproperty.WifiPropertyValue
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class WifiPropertyPresentationProviderTest {
    private val resources = mockk<Resources> {
        every { getString(any()) } answers {
            when (firstArg<Int>()) {
                R.string.ip -> "IP"
                R.string.public_ -> "Public"
                R.string.ssid -> "SSID"
                R.string.dns -> "DNS"
                R.string.gps_disabled -> "GPS disabled"
                else -> error("Unexpected string resource")
            }
        }
    }

    @Test
    fun `converts labels values subvalues and errors in resolver order`() =
        runTest {
            val requested = listOf(WifiProperty.DNS, WifiProperty.PublicIp, WifiProperty.SSID)
            val resolutions = listOf(
                ResolvedWifiProperty(WifiProperty.DNS, emptyList()),
                ResolvedWifiProperty(
                    WifiProperty.PublicIp,
                    listOf(
                        WifiPropertyValue("192.0.2.1".txt, listOf("/24")),
                        WifiPropertyValue("2001:db8::1".txt)
                    )
                ),
                ResolvedWifiProperty(
                    WifiProperty.SSID,
                    listOf(WifiPropertyValue(R.string.gps_disabled.txt, resolutionError = WifiPropertyResolutionError.GpsDisabled))
                )
            )
            var receivedProperties: List<WifiProperty>? = null
            val resolver = WifiPropertyResolver { enabledProperties, enabledIpSettings, remoteNetworkData ->
                receivedProperties = enabledProperties
                assertEquals(emptyList<com.w2sv.domain.wifiproperty.settings.IpSetting>(), enabledIpSettings(WifiProperty.PublicIp))
                assertEquals(RemoteNetworkData.empty, remoteNetworkData)
                resolutions
            }

            val actual = WifiPropertyPresentationProvider(resolver, resources)(
                enabledProperties = requested,
                enabledIpSettings = { emptyList() },
                remoteNetworkData = RemoteNetworkData.empty
            )

            assertEquals(requested, receivedProperties)
            assertEquals(
                listOf(
                    WifiPropertyPresentation(SubscriptableText("IP", "Public 1"), "192.0.2.1", listOf("/24")),
                    WifiPropertyPresentation(SubscriptableText("IP", "Public 2"), "2001:db8::1"),
                    WifiPropertyPresentation(
                        SubscriptableText("SSID"),
                        "GPS disabled",
                        resolutionError = WifiPropertyResolutionError.GpsDisabled
                    )
                ),
                actual
            )
        }

    @Test
    fun `single non IP value has an unnumbered localized label`() =
        runTest {
            val resolver = WifiPropertyResolver { _, _, _ ->
                listOf(ResolvedWifiProperty(WifiProperty.DNS, listOf(WifiPropertyValue("1.1.1.1".txt))))
            }

            assertEquals(
                listOf(WifiPropertyPresentation(SubscriptableText("DNS"), "1.1.1.1")),
                WifiPropertyPresentationProvider(resolver, resources)(
                    enabledProperties = listOf(WifiProperty.DNS),
                    enabledIpSettings = { emptyList() },
                    remoteNetworkData = RemoteNetworkData.empty
                )
            )
        }
}
