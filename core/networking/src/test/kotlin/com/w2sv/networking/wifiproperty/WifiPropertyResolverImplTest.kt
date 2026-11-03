package com.w2sv.networking.wifiproperty

import android.net.ConnectivityManager
import android.net.DhcpInfo
import android.net.IpPrefix
import android.net.LinkAddress
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import com.w2sv.domain.networking.RemoteNetworkData
import com.w2sv.domain.wifiproperty.ResolvedWifiProperty
import com.w2sv.domain.wifiproperty.Text
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.WifiPropertyValue
import com.w2sv.domain.wifiproperty.settings.IpSetting
import com.w2sv.domain.wifiproperty.txt
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.net.InetAddress
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Suppress("DEPRECATION")
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
        every { wifiManager.isWifiEnabled } returns false
        every { connectivityManager.activeNetwork } returns null
    }

    private fun resolver() =
        WifiPropertyResolverImpl(
            wifiManager = wifiManager,
            connectivityManager = connectivityManager,
            isLocationEnabled = { locationEnabled }
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
    fun `Wi-Fi default supplies classified addresses prefixes and NAT64 from one LinkProperties instance`() =
        runTest {
            val network = mockk<Network>()
            val nat64 = mockk<IpPrefix> {
                every { address } returns InetAddress.getByName("64:ff9b::")
            }
            val properties = localProperties(
                "192.168.1.7" to 24,
                "fd00::7" to 64,
                "2001:db8::7" to 64,
                nat64 = nat64
            )
            every { wifiManager.isWifiEnabled } returns true
            every { connectivityManager.activeNetwork } returns network
            every { connectivityManager.getNetworkCapabilities(network) } returns capabilities(wifi = true)
            every { connectivityManager.getLinkProperties(network) } returns properties

            val resolved = resolver()(
                enabledProperties = listOf(WifiProperty.SiteLocalIp, WifiProperty.ULA, WifiProperty.GUA, WifiProperty.NAT64Prefix),
                enabledIpSettings = {
                    listOf(IpSetting.V4Enabled, IpSetting.V6Enabled, IpSetting.ShowPrefixLength, IpSetting.ShowSubnetMask)
                },
                remoteNetworkData = RemoteNetworkData.empty
            )

            assertEquals(
                listOf(
                    WifiPropertyValue("192.168.1.7".txt, listOf("/24", "255.255.255.0")),
                    WifiPropertyValue("fd00:0:0:0:0:0:0:7".txt, listOf("/64")),
                    WifiPropertyValue("2001:db8:0:0:0:0:0:7".txt, listOf("/64")),
                    WifiPropertyValue(checkNotNull(nat64.address.hostAddress).txt)
                ),
                resolved.flatMap { it.values }
            )
            verify(exactly = 1) { connectivityManager.activeNetwork }
            verify(exactly = 1) { connectivityManager.getNetworkCapabilities(network) }
            verify(exactly = 1) { connectivityManager.getLinkProperties(network) }
            verify(exactly = 0) { connectivityManager.allNetworks }
        }

    @Test
    fun `visible Wi-Fi resolves local IP values when default network is unavailable`() =
        runTest {
            val network = mockk<Network>()
            every { wifiManager.isWifiEnabled } returns true
            every { connectivityManager.allNetworks } returns arrayOf(network)
            every { connectivityManager.getNetworkCapabilities(network) } returns capabilities(wifi = true)
            every { connectivityManager.getLinkProperties(network) } returns localProperties("10.0.0.8" to 24)

            val resolved = resolver()(
                listOf(WifiProperty.SiteLocalIp),
                { listOf(IpSetting.V4Enabled) },
                RemoteNetworkData.empty
            )

            assertEquals(listOf(WifiPropertyValue("10.0.0.8".txt)), resolved.single().values)
            verify(exactly = 1) { connectivityManager.activeNetwork }
            verify(exactly = 1) { connectivityManager.allNetworks }
            verify(exactly = 1) { connectivityManager.getLinkProperties(network) }
        }

    @Test
    fun `non-Wi-Fi default does not supply addresses to Wi-Fi properties`() =
        runTest {
            val cellular = mockk<Network>()
            val wifi = mockk<Network>()
            val nat64 = mockk<IpPrefix> {
                every { address } returns InetAddress.getByName("64:ff9b::")
            }
            every { wifiManager.isWifiEnabled } returns true
            every { connectivityManager.activeNetwork } returns cellular
            every { connectivityManager.getNetworkCapabilities(cellular) } returns capabilities(wifi = false)
            every { connectivityManager.allNetworks } returns arrayOf(cellular, wifi)
            every { connectivityManager.getNetworkCapabilities(wifi) } returns capabilities(wifi = true)
            every { connectivityManager.getLinkProperties(wifi) } returns localProperties("192.168.5.9" to 24, nat64 = nat64)

            val resolved = resolver()(
                listOf(WifiProperty.SiteLocalIp, WifiProperty.NAT64Prefix),
                { listOf(IpSetting.V4Enabled) },
                RemoteNetworkData.empty
            )

            assertEquals(
                listOf(WifiPropertyValue("192.168.5.9".txt), WifiPropertyValue(checkNotNull(nat64.address.hostAddress).txt)),
                resolved.flatMap { it.values }
            )
            verify(exactly = 0) { connectivityManager.getLinkProperties(cellular) }
        }

    @Test
    fun `visible Wi-Fi with inaccessible LinkProperties leaves local values unresolved`() =
        runTest {
            val wifi = mockk<Network>()
            every { wifiManager.isWifiEnabled } returns true
            every { connectivityManager.allNetworks } returns arrayOf(wifi)
            every { connectivityManager.getNetworkCapabilities(wifi) } returns capabilities(wifi = true)
            every { connectivityManager.getLinkProperties(wifi) } returns null

            val resolved = resolver()(
                listOf(WifiProperty.SiteLocalIp, WifiProperty.NAT64Prefix),
                { listOf(IpSetting.V4Enabled) },
                RemoteNetworkData.empty
            )

            assertEquals(listOf(emptyList<WifiPropertyValue>(), emptyList()), resolved.map { it.values })
            verify(exactly = 1) { connectivityManager.getLinkProperties(wifi) }
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

    private fun localProperties(vararg addresses: Pair<String, Int>, nat64: IpPrefix? = null) =
        mockk<LinkProperties> {
            every { linkAddresses } returns addresses.map { (ipAddress, prefix) ->
                mockk<LinkAddress> {
                    every { address } returns InetAddress.getByName(ipAddress)
                    every { prefixLength } returns prefix
                }
            }
            every { nat64Prefix } returns nat64
        }

    private fun capabilities(wifi: Boolean) =
        mockk<NetworkCapabilities> {
            every { hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns wifi
            if (wifi) every { hasTransport(NetworkCapabilities.TRANSPORT_VPN) } returns false
        }
}
