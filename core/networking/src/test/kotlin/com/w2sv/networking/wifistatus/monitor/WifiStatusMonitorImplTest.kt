package com.w2sv.networking.wifistatus.monitor

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.w2sv.domain.model.networking.WifiStatus
import com.w2sv.networking.wifistatus.provider.WifiStatusProvider
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WifiStatusMonitorImplTest {
    @Test fun `loss of current default route emits transition to no network`() =
        runTest {
            val connectivity = mockk<ConnectivityManager>()
            val network = mockk<Network>()
            val capabilities = mockk<NetworkCapabilities> {
                every { hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
            }
            every { connectivity.activeNetwork } returns network
            every { connectivity.getNetworkCapabilities(network) } returns capabilities
            lateinit var callback: ConnectivityManager.NetworkCallback
            every { connectivity.registerDefaultNetworkCallback(any()) } answers {
                callback = firstArg<ConnectivityManager.NetworkCallback>()
            }
            every { connectivity.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) } just runs
            val monitor = WifiStatusMonitorImpl(mockk(), connectivity, ApplicationProvider.getApplicationContext<Context>())

            monitor.defaultEgressChanges.test {
                callback.onAvailable(network)
                callback.onCapabilitiesChanged(network, capabilities)
                expectNoEvents()
                callback.onLost(network)
                awaitItem()
                callback.onLost(network)
                expectNoEvents()
            }
        }

    @Test fun `restoring the same default network after loss emits again`() =
        runTest {
            val connectivity = mockk<ConnectivityManager>()
            val network = mockk<Network>()
            val capabilities = mockk<NetworkCapabilities> {
                every { hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
            }
            every { connectivity.activeNetwork } returns network
            every { connectivity.getNetworkCapabilities(network) } returns capabilities
            lateinit var callback: ConnectivityManager.NetworkCallback
            every { connectivity.registerDefaultNetworkCallback(any()) } answers {
                callback = firstArg<ConnectivityManager.NetworkCallback>()
            }
            every { connectivity.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) } just runs
            val monitor = WifiStatusMonitorImpl(mockk(), connectivity, ApplicationProvider.getApplicationContext<Context>())

            monitor.defaultEgressChanges.test {
                callback.onLost(network)
                awaitItem()
                callback.onAvailable(network)
                expectNoEvents()
                callback.onCapabilitiesChanged(network, capabilities)
                awaitItem()
            }
        }

    @Test fun `initial callbacks and old network loss do not duplicate a route change`() =
        runTest {
            val connectivity = mockk<ConnectivityManager>()
            val status = mockk<WifiStatusProvider>()
            val first = mockk<Network>()
            val second = mockk<Network>()
            val capabilities = mockk<NetworkCapabilities> {
                every { hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
            }
            every { connectivity.activeNetwork } returns first
            every { connectivity.getNetworkCapabilities(first) } returns capabilities
            lateinit var callback: ConnectivityManager.NetworkCallback
            every { connectivity.registerDefaultNetworkCallback(any()) } answers {
                callback = firstArg<ConnectivityManager.NetworkCallback>()
            }
            every { connectivity.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) } just runs
            val monitor = WifiStatusMonitorImpl(status, connectivity, ApplicationProvider.getApplicationContext<Context>())

            monitor.defaultEgressChanges.test {
                callback.onAvailable(first)
                callback.onCapabilitiesChanged(first, capabilities)
                expectNoEvents()
                callback.onAvailable(second)
                expectNoEvents() // onAvailable precedes usable capabilities.
                callback.onCapabilitiesChanged(second, capabilities)
                awaitItem()
                callback.onLost(first)
                callback.onCapabilitiesChanged(second, capabilities)
                expectNoEvents()
            }
            verify(exactly = 0) { status.invoke() }
        }

    @Test fun `Wi-Fi callback does not query status synchronously`() =
        runTest {
            val connectivity = mockk<ConnectivityManager>()
            val status = mockk<WifiStatusProvider>()
            every { status.invoke() } returns WifiStatus.Connected
            lateinit var callback: ConnectivityManager.NetworkCallback
            every { connectivity.registerNetworkCallback(any(), any<ConnectivityManager.NetworkCallback>()) } answers {
                callback = secondArg<ConnectivityManager.NetworkCallback>()
            }
            every { connectivity.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) } just runs
            val monitor = WifiStatusMonitorImpl(status, connectivity, ApplicationProvider.getApplicationContext<Context>())
            monitor.wifiStatus.test {
                awaitItem()
                val network = mockk<Network>()
                callback.onAvailable(network)
                awaitItem()
                verify(exactly = 2) { status.invoke() }
            }
        }
}
