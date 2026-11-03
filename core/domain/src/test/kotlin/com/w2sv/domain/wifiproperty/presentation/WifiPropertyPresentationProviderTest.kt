package com.w2sv.domain.wifiproperty.presentation

import android.content.res.Resources
import com.w2sv.core.resources.R
import com.w2sv.domain.networking.RemoteNetworkData
import com.w2sv.domain.wifiproperty.ResolvedWifiProperty
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.WifiPropertyResolutionError
import com.w2sv.domain.wifiproperty.WifiPropertyResolver
import com.w2sv.domain.wifiproperty.WifiPropertyValue
import com.w2sv.domain.wifiproperty.txt
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
                R.string.battery_saver_restricted -> "Restricted by Battery Saver"
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

    @Test
    fun `fallback replaces only empty resolutions without changing order or multiple values`() =
        runTest {
            val requested = listOf(WifiProperty.PublicIp, WifiProperty.SSID, WifiProperty.DNS)
            val resolver = WifiPropertyResolver { _, _, _ ->
                listOf(
                    ResolvedWifiProperty(WifiProperty.PublicIp, emptyList()),
                    ResolvedWifiProperty(
                        WifiProperty.SSID,
                        listOf(WifiPropertyValue("Home".txt), WifiPropertyValue("Guest".txt))
                    ),
                    ResolvedWifiProperty(WifiProperty.DNS, emptyList())
                )
            }
            val unresolved = mutableListOf<WifiProperty>()

            val actual = WifiPropertyPresentationProvider(resolver, resources)(
                enabledProperties = requested,
                enabledIpSettings = { emptyList() },
                remoteNetworkData = RemoteNetworkData.empty,
                onUnresolved = { property ->
                    unresolved += property
                    if (property == WifiProperty.PublicIp) {
                        WifiPropertyValue(
                            R.string.battery_saver_restricted.txt,
                            resolutionError = WifiPropertyResolutionError.BatterySaverRestricted
                        )
                    } else {
                        null
                    }
                }
            )

            assertEquals(listOf(WifiProperty.PublicIp, WifiProperty.DNS), unresolved)
            assertEquals(
                listOf(
                    WifiPropertyPresentation(
                        SubscriptableText("IP", "Public"),
                        "Restricted by Battery Saver",
                        resolutionError = WifiPropertyResolutionError.BatterySaverRestricted
                    ),
                    WifiPropertyPresentation(SubscriptableText("SSID 1"), "Home"),
                    WifiPropertyPresentation(SubscriptableText("SSID 2"), "Guest")
                ),
                actual
            )
        }
}
