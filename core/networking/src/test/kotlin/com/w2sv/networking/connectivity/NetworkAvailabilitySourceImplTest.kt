package com.w2sv.networking.connectivity

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.PowerManager
import com.w2sv.domain.networking.NetworkAvailabilitySnapshot
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

@Suppress("DEPRECATION")
class NetworkAvailabilitySourceImplTest {
    private val powerManager = mockk<PowerManager>()
    private val connectivityManager = mockk<ConnectivityManager>()
    private val wifiManager = mockk<WifiManager>()

    @Test
    fun `missing default network is captured once while Wi-Fi remains connected`() {
        val wifiNetwork = mockk<Network>()
        every { powerManager.isPowerSaveMode } returns true
        every { wifiManager.isWifiEnabled } returns true
        every { connectivityManager.activeNetwork } returns null
        every { connectivityManager.allNetworks } returns arrayOf(wifiNetwork)
        every { connectivityManager.getNetworkCapabilities(wifiNetwork) } returns wifiCapabilities()
        every { connectivityManager.getLinkProperties(wifiNetwork) } returns null

        val actual = source().invoke()

        assertEquals(NetworkAvailabilitySnapshot(true, false, false, true), actual)
        assertEquals(false, actual.canRefreshWidgetRemoteData)
        verify(exactly = 1) { powerManager.isPowerSaveMode }
        verify(exactly = 1) { connectivityManager.activeNetwork }
        verify(exactly = 1) { connectivityManager.allNetworks }
        verify(exactly = 1) { connectivityManager.getNetworkCapabilities(wifiNetwork) }
        verify(exactly = 1) { connectivityManager.getLinkProperties(wifiNetwork) }
        verify(exactly = 1) { wifiManager.isWifiEnabled }
    }

    @Test
    fun `accessible Wi-Fi LinkProperties remain available without a default route`() {
        val wifiNetwork = mockk<Network>()
        every { powerManager.isPowerSaveMode } returns true
        every { wifiManager.isWifiEnabled } returns true
        every { connectivityManager.activeNetwork } returns null
        every { connectivityManager.allNetworks } returns arrayOf(wifiNetwork)
        every { connectivityManager.getNetworkCapabilities(wifiNetwork) } returns wifiCapabilities()
        every { connectivityManager.getLinkProperties(wifiNetwork) } returns mockk<LinkProperties>()

        assertEquals(NetworkAvailabilitySnapshot(true, false, true, true), source().invoke())
        verify(exactly = 1) { connectivityManager.getLinkProperties(wifiNetwork) }
    }

    @Test
    fun `missing default capabilities do not assign another network's LinkProperties`() {
        val network = defaultNetwork()
        val wifiNetwork = mockk<Network>()
        every { powerManager.isPowerSaveMode } returns true
        every { wifiManager.isWifiEnabled } returns true
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns null
        every { connectivityManager.allNetworks } returns arrayOf(network, wifiNetwork)
        every { connectivityManager.getNetworkCapabilities(wifiNetwork) } returns wifiCapabilities()

        val actual = source().invoke()

        assertEquals(NetworkAvailabilitySnapshot(true, false, false, true, 1L), actual)
        assertEquals(false, actual.canRefreshWidgetRemoteData)
        verify(exactly = 1) { connectivityManager.activeNetwork }
        verify(exactly = 1) { connectivityManager.getNetworkCapabilities(network) }
        verify(exactly = 1) { connectivityManager.getNetworkCapabilities(wifiNetwork) }
        verify(exactly = 0) { connectivityManager.getLinkProperties(any()) }
    }

    @Test
    fun `network without internet capability is unusable for remote data`() {
        val network = defaultNetwork()
        every { powerManager.isPowerSaveMode } returns true
        every { wifiManager.isWifiEnabled } returns true
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns mockk<NetworkCapabilities> {
            every { hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns false
            every { hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true
            every { hasTransport(NetworkCapabilities.TRANSPORT_VPN) } returns false
        }
        every { connectivityManager.getLinkProperties(network) } returns mockk<LinkProperties>()

        assertEquals(NetworkAvailabilitySnapshot(true, false, true, true, 1L), source().invoke())
        verify(exactly = 0) { connectivityManager.allNetworks }
    }

    @Test
    fun `available network sources and Battery Saver remain separate observations`() {
        val network = defaultNetwork()
        every { powerManager.isPowerSaveMode } returns false
        every { wifiManager.isWifiEnabled } returns true
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns mockk<NetworkCapabilities> {
            every { hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
            every { hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true
            every { hasTransport(NetworkCapabilities.TRANSPORT_VPN) } returns false
            every { hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns false
        }
        every { connectivityManager.getLinkProperties(network) } returns mockk<LinkProperties>()

        val snapshot = source().invoke()
        assertEquals(NetworkAvailabilitySnapshot(false, true, true, true, 1L, true), snapshot)
        assertEquals(true, snapshot.canRefreshWidgetRemoteData)
        verify(exactly = 0) { connectivityManager.allNetworks }
    }

    @Test
    fun `inactive Wi-Fi is detected without querying default network capabilities again`() {
        val defaultNetwork = defaultNetwork()
        val wifiNetwork = mockk<Network>()
        every { powerManager.isPowerSaveMode } returns true
        every { wifiManager.isWifiEnabled } returns true
        every { connectivityManager.activeNetwork } returns defaultNetwork
        every { connectivityManager.getNetworkCapabilities(defaultNetwork) } returns mockk<NetworkCapabilities> {
            every { hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
            every { hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns false
            every { hasTransport(NetworkCapabilities.TRANSPORT_VPN) } returns false
        }
        every { connectivityManager.getLinkProperties(defaultNetwork) } returns mockk<LinkProperties>()
        every { connectivityManager.allNetworks } returns arrayOf(defaultNetwork, wifiNetwork)
        every { connectivityManager.getNetworkCapabilities(wifiNetwork) } returns wifiCapabilities()
        every { connectivityManager.getLinkProperties(wifiNetwork) } returns mockk<LinkProperties>()

        assertEquals(NetworkAvailabilitySnapshot(true, true, true, true, 1L), source().invoke())
        verify(exactly = 1) { connectivityManager.getNetworkCapabilities(defaultNetwork) }
        verify(exactly = 1) { connectivityManager.getNetworkCapabilities(wifiNetwork) }
    }

    @Test
    fun `VPN default over connected Wi-Fi is eligible without becoming direct Wi-Fi`() {
        val vpn = defaultNetwork()
        val wifiNetwork = mockk<Network>()
        every { powerManager.isPowerSaveMode } returns true
        every { wifiManager.isWifiEnabled } returns true
        every { connectivityManager.activeNetwork } returns vpn
        every { connectivityManager.getNetworkCapabilities(vpn) } returns mockk {
            every { hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
            every { hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns false
            every { hasTransport(NetworkCapabilities.TRANSPORT_VPN) } returns true
        }
        every { connectivityManager.allNetworks } returns arrayOf(vpn, wifiNetwork)
        every { connectivityManager.getNetworkCapabilities(wifiNetwork) } returns wifiCapabilities()
        every { connectivityManager.getLinkProperties(wifiNetwork) } returns mockk<LinkProperties>()

        val snapshot = source().invoke()
        assertEquals(NetworkAvailabilitySnapshot(true, true, true, true, 1L, false, true), snapshot)
        assertEquals(true, snapshot.canRefreshWidgetRemoteData)
        verify(exactly = 1) { connectivityManager.getNetworkCapabilities(vpn) }
    }

    @Test
    fun `VPN default without connected Wi-Fi cannot refresh widget data`() {
        val vpn = defaultNetwork()
        every { powerManager.isPowerSaveMode } returns false
        every { wifiManager.isWifiEnabled } returns false
        every { connectivityManager.activeNetwork } returns vpn
        every { connectivityManager.getNetworkCapabilities(vpn) } returns mockk {
            every { hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
            every { hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns false
            every { hasTransport(NetworkCapabilities.TRANSPORT_VPN) } returns true
        }

        val snapshot = source().invoke()

        assertEquals(false, snapshot.canRefreshWidgetRemoteData)
        assertEquals(true, snapshot.isVpnDefaultNetwork)
    }

    @Test
    fun `mixed Wi-Fi and cellular default is not direct Wi-Fi for recovery`() {
        val network = defaultNetwork()
        every { powerManager.isPowerSaveMode } returns true
        every { wifiManager.isWifiEnabled } returns true
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns mockk {
            every { hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
            every { hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true
            every { hasTransport(NetworkCapabilities.TRANSPORT_VPN) } returns false
            every { hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns true
        }
        every { connectivityManager.getLinkProperties(network) } returns mockk<LinkProperties>()

        val snapshot = source().invoke()
        assertEquals(NetworkAvailabilitySnapshot(true, true, true, true, 1L), snapshot)
        assertEquals(false, snapshot.canRefreshWidgetRemoteData)
        verify(exactly = 1) { connectivityManager.getNetworkCapabilities(network) }
    }

    @Test
    fun `disabled Wi-Fi does not appear connected even when networks remain visible`() {
        every { powerManager.isPowerSaveMode } returns true
        every { wifiManager.isWifiEnabled } returns false
        every { connectivityManager.activeNetwork } returns null

        assertEquals(NetworkAvailabilitySnapshot(true, false, false, false), source().invoke())
        verify(exactly = 0) { connectivityManager.allNetworks }
    }

    @Test
    fun `enabled but disconnected Wi-Fi does not appear connected`() {
        every { powerManager.isPowerSaveMode } returns true
        every { wifiManager.isWifiEnabled } returns true
        every { connectivityManager.activeNetwork } returns null
        every { connectivityManager.allNetworks } returns emptyArray()

        assertEquals(NetworkAvailabilitySnapshot(true, false, false, false), source().invoke())
    }

    private fun wifiCapabilities() =
        mockk<NetworkCapabilities> {
            every { hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true
            every { hasTransport(NetworkCapabilities.TRANSPORT_VPN) } returns false
        }

    private fun source() =
        NetworkAvailabilitySourceImpl(powerManager, connectivityManager, wifiManager)

    private fun defaultNetwork() =
        mockk<Network> {
            every { networkHandle } returns 1L
        }
}
