package com.w2sv.networking.wifistatus

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.wifi.WifiManager
import app.cash.turbine.test
import com.w2sv.domain.networking.WifiStatus
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.util.concurrent.Executor
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WifiStatusSourceImplApi36Test {
    private val context = mockk<Context>(relaxed = true)
    private val wifiManager = mockk<WifiManager>()
    private val connectivity = mockk<ConnectivityManager>(relaxed = true)
    private val executor = Executor { it.run() }
    private val network = mockk<Network>()
    private var enabled = true
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private lateinit var wifiStateListener: WifiManager.WifiStateChangedListener

    init {
        every { wifiManager.isWifiEnabled } answers { enabled }
        every { wifiManager.wifiState } answers {
            if (enabled) WifiManager.WIFI_STATE_ENABLED else WifiManager.WIFI_STATE_DISABLED
        }
        every { wifiManager.addWifiStateChangedListener(any(), any()) } answers {
            wifiStateListener = secondArg()
        }
        every { wifiManager.removeWifiStateChangedListener(any()) } just runs
        every { context.mainExecutor } returns executor
        every { connectivity.activeNetwork } returns null
        @Suppress("DEPRECATION")
        every { connectivity.allNetworks } returns emptyArray()
        every { connectivity.registerNetworkCallback(any(), any<ConnectivityManager.NetworkCallback>()) } answers {
            networkCallback = secondArg()
        }
        every { connectivity.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) } just runs
    }

    @Test fun `listener refreshes disabled status after network loss and is removed after collection`() =
        runTest {
            WifiStatusSourceImpl(wifiManager, connectivity, context).status.test {
                assertEquals(WifiStatus.NotConnected, awaitItem())
                verify(exactly = 1) { wifiManager.addWifiStateChangedListener(executor, wifiStateListener) }
                verify(exactly = 0) { context.registerReceiver(any<BroadcastReceiver>(), any<IntentFilter>(), null, null, any<Int>()) }

                networkCallback.onLost(network)
                assertEquals(WifiStatus.NotConnected, awaitItem())
                enabled = false
                wifiStateListener.onWifiStateChanged()
                assertEquals(WifiStatus.Disabled, awaitItem())
            }
            verify(exactly = 1) { wifiManager.removeWifiStateChangedListener(wifiStateListener) }
            verify(exactly = 1) { connectivity.unregisterNetworkCallback(networkCallback) }
        }

    @Test fun `listener registration failure cleans up network callback and preserves original error`() =
        runTest {
            val registrationFailure = IllegalStateException("listener registration failed")
            val cleanupFailure = IllegalStateException("network callback cleanup failed")
            every { wifiManager.addWifiStateChangedListener(any(), any()) } answers { throw registrationFailure }
            every { connectivity.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) } answers { throw cleanupFailure }

            WifiStatusSourceImpl(wifiManager, connectivity, context).status.test {
                val error = awaitError()
                assertEquals(registrationFailure::class, error::class)
                assertEquals(registrationFailure.message, error.message)
            }

            verify(exactly = 1) { connectivity.registerNetworkCallback(any(), networkCallback) }
            verify(exactly = 1) { connectivity.unregisterNetworkCallback(networkCallback) }
            verify(exactly = 0) { wifiManager.removeWifiStateChangedListener(any()) }
            assertEquals(listOf(cleanupFailure), registrationFailure.suppressed.toList())
        }
}
