package com.w2sv.networking.wifistatus

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import app.cash.turbine.test
import com.w2sv.domain.model.networking.WifiStatus
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WifiStatusSourceImplTest {
    private val context = mockk<Context>(relaxed = true)
    private val wifiManager = mockk<WifiManager>()
    private val connectivity = mockk<ConnectivityManager>(relaxed = true)
    private val first = mockk<Network>()
    private val second = mockk<Network>()
    private val wifiWithInternet = capabilities(wifi = true, internet = true)
    private val wifiWithoutInternet = capabilities(wifi = true, internet = false)
    private val mobileWithInternet = capabilities(wifi = false, internet = true)
    private var enabled = true
    private var active: Network? = null
    private var networks = emptyArray<Network>()
    private var firstCapabilities: NetworkCapabilities? = null
    private var secondCapabilities: NetworkCapabilities? = null
    private lateinit var wifiCallback: ConnectivityManager.NetworkCallback
    private lateinit var defaultCallback: ConnectivityManager.NetworkCallback
    private lateinit var receiver: BroadcastReceiver

    init {
        every { wifiManager.isWifiEnabled } answers { enabled }
        every { connectivity.activeNetwork } answers { active }
        every { connectivity.allNetworks } answers { networks }
        every { connectivity.getNetworkCapabilities(any()) } answers {
            when (firstArg<Network?>()) {
                first -> firstCapabilities
                second -> secondCapabilities
                else -> null
            }
        }
        every { connectivity.registerNetworkCallback(any(), any<ConnectivityManager.NetworkCallback>()) } answers {
            wifiCallback = secondArg()
        }
        every { connectivity.registerDefaultNetworkCallback(any()) } answers {
            defaultCallback = firstArg()
        }
        every { connectivity.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) } just runs
        every { context.registerReceiver(any(), any<IntentFilter>()) } answers {
            receiver = firstArg()
            null
        }
        every { context.unregisterReceiver(any<BroadcastReceiver>()) } just runs
    }

    private fun source() =
        WifiStatusSourceImpl(wifiManager, connectivity, context)

    @Test fun `snapshot classifies Wi-Fi and default route without subscribing`() {
        val source = source()
        enabled = false
        assertEquals(WifiStatus.Disabled, source.snapshot())
        enabled = true
        assertEquals(WifiStatus.NotConnected, source.snapshot())
        active = first
        networks = arrayOf(first)
        firstCapabilities = wifiWithInternet
        assertEquals(WifiStatus.Connected, source.snapshot())
        firstCapabilities = wifiWithoutInternet
        assertEquals(WifiStatus.ConnectedNoInternet, source.snapshot())
        active = second
        secondCapabilities = mobileWithInternet
        assertEquals(WifiStatus.ConnectedInactive, source.snapshot())
        networks = emptyArray()
        assertEquals(WifiStatus.NotConnected, source.snapshot())
        verify(exactly = 0) { connectivity.registerNetworkCallback(any(), any<ConnectivityManager.NetworkCallback>()) }
        verify(exactly = 0) { connectivity.registerDefaultNetworkCallback(any()) }
        verify(exactly = 0) { context.registerReceiver(any(), any<IntentFilter>()) }
    }

    @Test fun `Wi-Fi collection emits initial and repeated identical statuses and cleans up`() =
        runTest {
            val source = source()
            source.status.test {
                assertEquals(WifiStatus.NotConnected, awaitItem())
                wifiCallback.onAvailable(first)
                assertEquals(WifiStatus.NotConnected, awaitItem())
                wifiCallback.onLost(first)
                assertEquals(WifiStatus.NotConnected, awaitItem())
            }
            verify(exactly = 1) { connectivity.unregisterNetworkCallback(wifiCallback) }
            verify(exactly = 1) { context.unregisterReceiver(receiver) }
        }

    @Test fun `Wi-Fi broadcasts emit disabled and not connected states`() =
        runTest {
            source().status.test {
                awaitItem()
                receiver.onReceive(context, wifiStateIntent(WifiManager.WIFI_STATE_DISABLED))
                assertEquals(WifiStatus.Disabled, awaitItem())
                receiver.onReceive(context, wifiStateIntent(WifiManager.WIFI_STATE_ENABLED))
                assertEquals(WifiStatus.NotConnected, awaitItem())
            }
        }

    @Test fun `non-default Wi-Fi link and capability changes emit connected inactive status`() =
        runTest {
            active = second
            networks = arrayOf(first, second)
            firstCapabilities = wifiWithInternet
            secondCapabilities = mobileWithInternet
            source().status.test {
                assertEquals(WifiStatus.ConnectedInactive, awaitItem())
                wifiCallback.onLinkPropertiesChanged(first, mockk<LinkProperties>())
                assertEquals(WifiStatus.ConnectedInactive, awaitItem())
                wifiCallback.onCapabilitiesChanged(first, wifiWithInternet)
                assertEquals(WifiStatus.ConnectedInactive, awaitItem())
            }
        }

    @Test fun `loss and late callbacks recalculate status after network replacement`() =
        runTest {
            active = first
            networks = arrayOf(first)
            firstCapabilities = wifiWithInternet
            source().status.test {
                assertEquals(WifiStatus.Connected, awaitItem())
                active = second
                networks = arrayOf(second)
                secondCapabilities = wifiWithoutInternet
                wifiCallback.onLost(first)
                assertEquals(WifiStatus.ConnectedNoInternet, awaitItem())
                wifiCallback.onCapabilitiesChanged(first, wifiWithInternet)
                assertEquals(WifiStatus.ConnectedNoInternet, awaitItem())
                wifiCallback.onLinkPropertiesChanged(first, mockk<LinkProperties>())
                assertEquals(WifiStatus.ConnectedNoInternet, awaitItem())
                wifiCallback.onCapabilitiesChanged(second, wifiWithoutInternet)
                assertEquals(WifiStatus.ConnectedNoInternet, awaitItem())
                networks = emptyArray()
                active = null
                wifiCallback.onLinkPropertiesChanged(first, mockk<LinkProperties>())
                assertEquals(WifiStatus.NotConnected, awaitItem())
            }
        }

    @Test fun `network callbacks defer connectivity queries`() =
        runTest {
            val inCallback = ThreadLocal.withInitial { false }
            every { connectivity.activeNetwork } answers {
                assertFalse(inCallback.get())
                active
            }
            source().status.test {
                awaitItem()
                inCallback.set(true)
                wifiCallback.onAvailable(first)
                inCallback.set(false)
                awaitItem()
                inCallback.set(true)
                wifiCallback.onCapabilitiesChanged(first, wifiWithInternet)
                inCallback.set(false)
                awaitItem()
                inCallback.set(true)
                wifiCallback.onLinkPropertiesChanged(first, mockk<LinkProperties>())
                inCallback.set(false)
                awaitItem()
                inCallback.set(true)
                wifiCallback.onLost(first)
                inCallback.set(false)
                awaitItem()
            }
        }

    @Test fun `default route tracks identity capability loss and restoration without initial duplicates`() =
        runTest {
            active = first
            firstCapabilities = wifiWithInternet
            source().defaultEgressChanges.test {
                defaultCallback.onAvailable(first)
                defaultCallback.onCapabilitiesChanged(first, wifiWithInternet)
                expectNoEvents()
                defaultCallback.onCapabilitiesChanged(first, wifiWithoutInternet)
                awaitItem()
                defaultCallback.onCapabilitiesChanged(first, wifiWithoutInternet)
                expectNoEvents()
                defaultCallback.onAvailable(second)
                expectNoEvents()
                defaultCallback.onCapabilitiesChanged(second, mobileWithInternet)
                awaitItem()
                defaultCallback.onLost(first)
                expectNoEvents()
                defaultCallback.onLost(second)
                awaitItem()
                defaultCallback.onAvailable(first)
                defaultCallback.onCapabilitiesChanged(first, wifiWithInternet)
                awaitItem()
            }
            verify(exactly = 1) { connectivity.unregisterNetworkCallback(defaultCallback) }
            verify(exactly = 0) { context.registerReceiver(any(), any<IntentFilter>()) }
        }

    private fun wifiStateIntent(state: Int) =
        Intent(WifiManager.WIFI_STATE_CHANGED_ACTION).putExtra(WifiManager.EXTRA_WIFI_STATE, state)

    private fun capabilities(wifi: Boolean, internet: Boolean) =
        mockk<NetworkCapabilities> {
            every { hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns wifi
            every { hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns internet
            every { hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns internet
        }
}
