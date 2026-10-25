package com.w2sv.networking.connectivity

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiNetworkSnapshotTest {
    private val connectivity = mockk<ConnectivityManager>()

    @Test
    fun `known Wi-Fi default supplies local properties without enumerating networks`() {
        val network = mockk<Network>()
        val properties = mockk<LinkProperties>()
        every { connectivity.getLinkProperties(network) } returns properties

        val snapshot = connectivity.wifiNetworkSnapshot(true, network, capabilities(wifi = true))

        assertEquals(WifiNetworkSnapshot(true, properties), snapshot)
        verify(exactly = 0) { connectivity.allNetworks }
        verify(exactly = 1) { connectivity.getLinkProperties(network) }
    }

    @Test
    fun `single visible Wi-Fi network supplies local properties without a default route`() {
        val wifi = mockk<Network>()
        val properties = mockk<LinkProperties>()
        every { connectivity.allNetworks } returns arrayOf(wifi)
        every { connectivity.getNetworkCapabilities(wifi) } returns capabilities(wifi = true)
        every { connectivity.getLinkProperties(wifi) } returns properties

        assertEquals(WifiNetworkSnapshot(true, properties), connectivity.wifiNetworkSnapshot(true, null, null))
        verify(exactly = 1) { connectivity.allNetworks }
        verify(exactly = 1) { connectivity.getNetworkCapabilities(wifi) }
        verify(exactly = 1) { connectivity.getLinkProperties(wifi) }
    }

    @Test
    fun `cellular default does not supply Wi-Fi local properties`() {
        val cellular = mockk<Network>()
        val wifi = mockk<Network>()
        val properties = mockk<LinkProperties>()
        every { connectivity.allNetworks } returns arrayOf(cellular, wifi)
        every { connectivity.getNetworkCapabilities(wifi) } returns capabilities(wifi = true)
        every { connectivity.getLinkProperties(wifi) } returns properties

        assertEquals(
            WifiNetworkSnapshot(true, properties),
            connectivity.wifiNetworkSnapshot(true, cellular, capabilities(wifi = false))
        )
        verify(exactly = 0) { connectivity.getNetworkCapabilities(cellular) }
        verify(exactly = 0) { connectivity.getLinkProperties(cellular) }
    }

    @Test
    fun `VPN default with Wi-Fi transport is not used for local IP information`() {
        val vpn = mockk<Network>()
        val wifi = mockk<Network>()
        val properties = mockk<LinkProperties>()
        every { connectivity.allNetworks } returns arrayOf(vpn, wifi)
        every { connectivity.getNetworkCapabilities(wifi) } returns capabilities(wifi = true)
        every { connectivity.getLinkProperties(wifi) } returns properties

        assertEquals(
            WifiNetworkSnapshot(true, properties),
            connectivity.wifiNetworkSnapshot(true, vpn, capabilities(wifi = true, vpn = true))
        )
        verify(exactly = 0) { connectivity.getLinkProperties(vpn) }
    }

    @Test
    fun `VPN transport alone does not establish an accessible Wi-Fi connection`() {
        val vpn = mockk<Network>()
        every { connectivity.allNetworks } returns arrayOf(vpn)

        val snapshot = connectivity.wifiNetworkSnapshot(true, vpn, capabilities(wifi = true, vpn = true))

        assertFalse(snapshot.isConnected)
        assertNull(snapshot.linkProperties)
        verify(exactly = 0) { connectivity.getLinkProperties(vpn) }
    }

    @Test
    fun `visible Wi-Fi without LinkProperties stays connected but has no local values`() {
        val wifi = mockk<Network>()
        every { connectivity.allNetworks } returns arrayOf(wifi)
        every { connectivity.getNetworkCapabilities(wifi) } returns capabilities(wifi = true)
        every { connectivity.getLinkProperties(wifi) } returns null

        val snapshot = connectivity.wifiNetworkSnapshot(true, null, null)

        assertTrue(snapshot.isConnected)
        assertNull(snapshot.linkProperties)
    }

    @Test
    fun `multiple visible Wi-Fi networks do not select local addresses arbitrarily`() {
        val first = mockk<Network>()
        val second = mockk<Network>()
        every { connectivity.allNetworks } returns arrayOf(first, second)
        every { connectivity.getNetworkCapabilities(first) } returns capabilities(wifi = true)
        every { connectivity.getNetworkCapabilities(second) } returns capabilities(wifi = true)

        val snapshot = connectivity.wifiNetworkSnapshot(true, null, null)

        assertTrue(snapshot.isConnected)
        assertNull(snapshot.linkProperties)
        verify(exactly = 0) { connectivity.getLinkProperties(any()) }
    }

    @Test
    fun `unknown default transport prevents assigning another network's addresses`() {
        val unknownDefault = mockk<Network>()
        val wifi = mockk<Network>()
        every { connectivity.allNetworks } returns arrayOf(unknownDefault, wifi)
        every { connectivity.getNetworkCapabilities(wifi) } returns capabilities(wifi = true)

        val snapshot = connectivity.wifiNetworkSnapshot(true, unknownDefault, null)

        assertTrue(snapshot.isConnected)
        assertNull(snapshot.linkProperties)
        verify(exactly = 0) { connectivity.getLinkProperties(any()) }
    }

    @Test
    fun `disabled Wi-Fi does not expose stale visible network information`() {
        val snapshot = connectivity.wifiNetworkSnapshot(false, null, null)

        assertFalse(snapshot.isConnected)
        assertNull(snapshot.linkProperties)
        verify(exactly = 0) { connectivity.allNetworks }
    }

    private fun capabilities(wifi: Boolean, vpn: Boolean = false) =
        mockk<NetworkCapabilities> {
            every { hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns wifi
            if (wifi) every { hasTransport(NetworkCapabilities.TRANSPORT_VPN) } returns vpn
        }
}
