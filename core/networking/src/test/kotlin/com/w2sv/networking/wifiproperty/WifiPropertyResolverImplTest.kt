package com.w2sv.networking.wifiproperty

import android.net.ConnectivityManager
import android.net.DhcpInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import com.w2sv.domain.location.IsLocationEnabled
import com.w2sv.domain.networking.RemoteNetworkData
import com.w2sv.domain.wifiproperty.ResolvedWifiProperty
import com.w2sv.domain.wifiproperty.Text
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.WifiPropertyValue
import com.w2sv.domain.wifiproperty.txt
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WifiPropertyResolverImplTest {
    private val wifiManager = mockk<WifiManager>()
    private val connectionInfo = mockk<WifiInfo>()
    private val dhcpInfo = DhcpInfo()
    private val connectivityManager = mockk<ConnectivityManager>()
    private var locationEnabled = true

    init {
        every { wifiManager.connectionInfo } returns connectionInfo
        every { wifiManager.dhcpInfo } returns dhcpInfo
        every { connectivityManager.activeNetwork } returns null
        every { connectivityManager.getLinkProperties(null) } returns null
    }

    private fun resolver() =
        WifiPropertyResolverImpl(
            wifiManager = wifiManager,
            connectivityManager = connectivityManager,
            isLocationEnabled = IsLocationEnabled { locationEnabled }
        )

    @Test
    fun `resolves requested properties in order and retains multiple values`() =
        runTest {
            dhcpInfo.dns1 = 0x01010101
            dhcpInfo.dns2 = 0x08080808
            every { connectionInfo.frequency } returns 2412

            assertEquals(
                listOf(
                    ResolvedWifiProperty(
                        WifiProperty.DNS,
                        listOf(WifiPropertyValue(Text.Raw("1.1.1.1")), WifiPropertyValue(Text.Raw("8.8.8.8")))
                    ),
                    ResolvedWifiProperty(WifiProperty.Frequency, listOf(WifiPropertyValue(Text.Raw("2412 MHz"))))
                ),
                resolver()(
                    enabledProperties = listOf(WifiProperty.DNS, WifiProperty.Frequency),
                    enabledIpSettings = { emptyList() },
                    remoteNetworkData = RemoteNetworkData.empty
                )
            )
        }

    @Test
    fun `retains empty values and location resolution errors`() =
        runTest {
            locationEnabled = false
            every { connectionInfo.ssid } returns "<unknown ssid>"

            assertEquals(
                listOf(
                    ResolvedWifiProperty(WifiProperty.PublicIp, emptyList()),
                    ResolvedWifiProperty(
                        WifiProperty.SSID,
                        listOf(
                            WifiPropertyValue(
                                com.w2sv.core.resources.R.string.gps_disabled.txt,
                                resolutionError = GpsDisabled
                            )
                        )
                    )
                ),
                resolver()(
                    enabledProperties = listOf(WifiProperty.PublicIp, WifiProperty.SSID),
                    enabledIpSettings = { emptyList() },
                    remoteNetworkData = RemoteNetworkData.empty
                )
            )
        }

    @Test
    fun `resolution failure becomes cancellation`() {
        every { connectionInfo.ssid } throws IllegalStateException("failed")

        assertThrows(CancellationException::class.java) {
            kotlinx.coroutines.runBlocking {
                resolver()(listOf(WifiProperty.SSID), { emptyList() }, RemoteNetworkData.empty)
            }
        }
    }

    @Test
    fun `cancellation propagates`() {
        every { connectionInfo.ssid } throws CancellationException("cancelled")

        assertThrows(CancellationException::class.java) {
            kotlinx.coroutines.runBlocking {
                resolver()(listOf(WifiProperty.SSID), { emptyList() }, RemoteNetworkData.empty)
            }
        }
    }

    @Test
    fun `textual IPv4 representation retains byte order`() {
        assertEquals("127.0.0.1", textualIPv4Representation(0x0100007F))
        assertEquals("255.255.255.255", textualIPv4Representation(-1))
        assertEquals("192.168.1.1", textualIPv4Representation(0x0101A8C0))
    }
}
