package com.w2sv.widget

import com.w2sv.domain.networking.IpApiData
import com.w2sv.domain.networking.NetworkAvailabilitySnapshot
import com.w2sv.domain.networking.NetworkAvailabilitySource
import com.w2sv.domain.networking.RemoteNetworkData
import com.w2sv.domain.networking.RemoteNetworkDataRepository
import com.w2sv.domain.networking.WifiStatus
import com.w2sv.domain.networking.WifiStatusSource
import com.w2sv.domain.widget.WidgetActions
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.domain.widget.WidgetStatus
import com.w2sv.widget.refreshing.WifiWidgetWorkScheduler
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RemoteDataWidgetObserverTest {
    private val data = MutableStateFlow(RemoteNetworkData.empty)
    private var recoveryNeeded = false
    private val repository = mockk<RemoteNetworkDataRepository> {
        every { data } returns this@RemoteDataWidgetObserverTest.data
        every { needsRecovery(any()) } answers { recoveryNeeded }
    }
    private val wifiEvents = MutableSharedFlow<WifiStatus>(extraBufferCapacity = 1)
    private val defaultEgressEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private var wifiIdentity: Long? = 1L
    private var availability = NetworkAvailabilitySnapshot(false, true, true, true, 1L, true)
    private val availabilitySource = NetworkAvailabilitySource { availability }
    private val wifiStatusSource = mockk<WifiStatusSource> {
        every { this@mockk.status } returns wifiEvents
        every { defaultEgressChanges } returns defaultEgressEvents
        every { connectedWifiIdentity() } answers { wifiIdentity }
    }
    private val configSource = mockk<WidgetConfigSource> {
        coEvery { snapshot() } returns WidgetConfig.default
    }
    private val scheduler = mockk<WifiWidgetWorkScheduler>(relaxed = true)
    private val actions = mockk<WidgetActions>(relaxed = true)
    private var widgetsInstalled = true
    private val widgetStatus = mockk<WidgetStatus> {
        every { hasInstalledWidgets } answers { widgetsInstalled }
    }

    private fun TestScope.observer() =
        RemoteDataWidgetObserver(
            repository,
            wifiStatusSource,
            availabilitySource,
            configSource,
            scheduler,
            actions,
            widgetStatus,
            CoroutineScope(backgroundScope.coroutineContext + StandardTestDispatcher(testScheduler))
        )

    private fun published(isp: String) =
        RemoteNetworkData(IpApiData(null, null, "UTC", isp, null), emptyList())

    @Test fun `initial subscription skips current data`() =
        runTest {
            data.value = published("initial")
            observer().initialize()
            runCurrent()
            verify(exactly = 0) { actions.render() }
        }

    @Test fun `changed data renders while equal data does not`() =
        runTest {
            observer().initialize()
            data.value = published("one")
            runCurrent()
            verify(exactly = 1) { actions.render() }
            data.value = published("one")
            runCurrent()
            verify(exactly = 1) { actions.render() }
            data.value = published("two")
            runCurrent()
            verify(exactly = 2) { actions.render() }
        }

    @Test fun `no installed widgets suppresses rendering`() =
        runTest {
            widgetsInstalled = false
            observer().initialize()
            data.value = published("one")
            runCurrent()
            verify(exactly = 0) { actions.render() }
        }

    @Test fun `repeated initialization keeps one subscription`() =
        runTest {
            val observer = observer()
            observer.initialize()
            observer.initialize()
            data.value = published("one")
            runCurrent()
            verify(exactly = 1) { actions.render() }
            assertEquals(1, wifiEvents.subscriptionCount.value)
            assertEquals(1, defaultEgressEvents.subscriptionCount.value)
        }

    @Test fun `Wi-Fi disconnect renders even when cellular default data stays unchanged`() =
        runTest {
            observer().initialize()
            wifiEvents.emit(WifiStatus.ConnectedInactive)
            runCurrent()
            wifiIdentity = null
            wifiEvents.emit(WifiStatus.NotConnected)
            runCurrent()

            verify(exactly = 1) { actions.render() }
            assertEquals(RemoteNetworkData.empty, data.value)
        }

    @Test fun `two inactive Wi-Fi connections with different identities trigger a render`() =
        runTest {
            observer().initialize()
            wifiEvents.emit(WifiStatus.ConnectedInactive)
            runCurrent()
            wifiIdentity = 2L
            wifiEvents.emit(WifiStatus.ConnectedInactive)
            runCurrent()

            verify(exactly = 1) { actions.render() }
            wifiEvents.emit(WifiStatus.ConnectedInactive)
            runCurrent()
            verify(exactly = 1) { actions.render() }
        }

    @Test fun `unchanged Wi-Fi identity does not duplicate a default route data render`() =
        runTest {
            observer().initialize()
            wifiEvents.emit(WifiStatus.ConnectedInactive)
            runCurrent()
            data.value = published("cellular route")
            runCurrent()
            wifiEvents.emit(WifiStatus.ConnectedInactive)
            runCurrent()

            verify(exactly = 1) { actions.render() }
        }

    @Test fun `returning to Wi-Fi default schedules recovery without a Wi-Fi status change`() =
        runTest {
            val observer = observer()
            observer.initialize()
            recoveryNeeded = false
            availability = availability.copy(defaultNetworkIdentity = 2L, isDirectWifiDefaultNetwork = false)
            defaultEgressEvents.emit(Unit)
            runCurrent()
            verify(exactly = 0) { scheduler.enqueueRecovery() }

            recoveryNeeded = true
            availability = availability.copy(defaultNetworkIdentity = 1L, isDirectWifiDefaultNetwork = true)
            defaultEgressEvents.emit(Unit)
            runCurrent()

            verify(exactly = 1) { scheduler.enqueueRecovery() }
            verify(exactly = 2) { actions.render() }
            verify(exactly = 0) { wifiStatusSource.connectedWifiIdentity() }
            assertEquals(RemoteNetworkData.empty, data.value)
        }

    @Test fun `VPN default over connected Wi-Fi reconciles and schedules recovery`() =
        runTest {
            observer().initialize()
            recoveryNeeded = true
            availability = availability.copy(
                defaultNetworkIdentity = 2L,
                isDirectWifiDefaultNetwork = false,
                isVpnDefaultNetwork = true
            )
            defaultEgressEvents.emit(Unit)
            runCurrent()

            verify(exactly = 1) { repository.needsRecovery(any()) }
            verify(exactly = 1) { scheduler.enqueueRecovery() }
            verify(exactly = 1) { actions.render() }

            availability = availability.copy(
                defaultNetworkIdentity = 1L,
                isDirectWifiDefaultNetwork = true,
                isVpnDefaultNetwork = false
            )
            defaultEgressEvents.emit(Unit)
            runCurrent()
            verify(exactly = 2) { scheduler.enqueueRecovery() }
        }

    @Test fun `Wi-Fi connection enables VPN recovery without a default route change`() =
        runTest {
            wifiIdentity = null
            availability = availability.copy(isWifiConnected = false, isDirectWifiDefaultNetwork = false, isVpnDefaultNetwork = true)
            recoveryNeeded = true
            observer().initialize()
            wifiEvents.emit(WifiStatus.NotConnected)
            runCurrent()

            wifiIdentity = 1L
            availability = availability.copy(isWifiConnected = true)
            wifiEvents.emit(WifiStatus.ConnectedInactive)
            runCurrent()

            verify(exactly = 1) { scheduler.enqueueRecovery() }
            verify(exactly = 1) { actions.render() }
            wifiEvents.emit(WifiStatus.ConnectedInactive)
            runCurrent()
            verify(exactly = 1) { scheduler.enqueueRecovery() }
        }

    @Test fun `direct Wi-Fi default without INTERNET does not schedule recovery`() =
        runTest {
            observer().initialize()
            recoveryNeeded = true
            availability = availability.copy(hasUsableDefaultNetwork = false, isDirectWifiDefaultNetwork = false)
            defaultEgressEvents.emit(Unit)
            runCurrent()

            verify(exactly = 1) { repository.needsRecovery(any()) }
            verify(exactly = 0) { scheduler.enqueueRecovery() }
            verify(exactly = 1) { actions.render() }
        }

    @Test fun `unavailable default does not enqueue recovery and restoration does`() =
        runTest {
            observer().initialize()
            recoveryNeeded = true
            availability = availability.copy(
                hasUsableDefaultNetwork = false,
                defaultNetworkIdentity = null,
                isDirectWifiDefaultNetwork = false
            )
            defaultEgressEvents.emit(Unit)
            runCurrent()
            verify(exactly = 0) { scheduler.enqueueRecovery() }

            availability = availability.copy(
                hasUsableDefaultNetwork = true,
                defaultNetworkIdentity = 1L,
                isDirectWifiDefaultNetwork = true
            )
            defaultEgressEvents.emit(Unit)
            runCurrent()
            verify(exactly = 1) { scheduler.enqueueRecovery() }
        }

    @Test fun `repeated default observations reconcile and schedule only once`() =
        runTest {
            observer().initialize()
            recoveryNeeded = true
            defaultEgressEvents.emit(Unit)
            runCurrent()
            defaultEgressEvents.emit(Unit)
            runCurrent()

            verify(exactly = 1) { repository.needsRecovery(any()) }
            verify(exactly = 1) { scheduler.enqueueRecovery() }
            verify(exactly = 1) { actions.render() }
        }

    @Test fun `repository publication supplies the render after default reconciliation`() =
        runTest {
            every { repository.needsRecovery(any()) } answers {
                data.value = published("restored Wi-Fi")
                true
            }
            observer().initialize()
            defaultEgressEvents.emit(Unit)
            runCurrent()

            verify(exactly = 1) { scheduler.enqueueRecovery() }
            verify(exactly = 1) { actions.render() }
            assertEquals("restored Wi-Fi", data.value.ipApiData?.isp)
        }

    @Test fun `default route changes do not schedule recovery without Wi-Fi`() =
        runTest {
            observer().initialize()
            recoveryNeeded = true
            availability = availability.copy(isWifiConnected = false, isDirectWifiDefaultNetwork = false)
            defaultEgressEvents.emit(Unit)
            runCurrent()

            verify(exactly = 0) { scheduler.enqueueRecovery() }
            verify(exactly = 1) { actions.render() }
        }

    @Test fun `Wi-Fi observation starts and stops with installed widgets`() =
        runTest {
            widgetsInstalled = false
            val observer = observer()
            observer.initialize()
            runCurrent()
            assertEquals(0, wifiEvents.subscriptionCount.value)
            assertEquals(0, defaultEgressEvents.subscriptionCount.value)

            widgetsInstalled = true
            observer.onWidgetsAvailable()
            observer.onWidgetsAvailable()
            runCurrent()
            assertEquals(1, wifiEvents.subscriptionCount.value)
            assertEquals(1, defaultEgressEvents.subscriptionCount.value)
            wifiEvents.emit(WifiStatus.ConnectedInactive)
            runCurrent()
            wifiIdentity = 2L
            wifiEvents.emit(WifiStatus.ConnectedInactive)
            runCurrent()
            verify(exactly = 1) { actions.render() }

            widgetsInstalled = false
            observer.onLastWidgetRemoved()
            runCurrent()
            assertEquals(0, wifiEvents.subscriptionCount.value)
            assertEquals(0, defaultEgressEvents.subscriptionCount.value)
            wifiIdentity = 3L
            wifiEvents.emit(WifiStatus.ConnectedInactive)
            runCurrent()
            verify(exactly = 1) { actions.render() }
        }

    @Test fun `update immediately after startup is observed`() =
        runTest {
            val observer = observer()
            observer.initialize()
            data.value = published("immediate")
            runCurrent()
            verify(exactly = 1) { actions.render() }
        }

    @Test fun `render failure does not stop observing later data changes`() =
        runTest {
            var attempts = 0
            var successfulRenders = 0
            every { actions.render() } answers {
                attempts++
                if (attempts == 1) throw IllegalStateException("launcher unavailable")
                successfulRenders++
            }
            observer().initialize()

            data.value = published("first")
            runCurrent()
            assertEquals(1, attempts)
            assertEquals(0, successfulRenders)

            data.value = published("second")
            runCurrent()
            assertEquals(2, attempts)
            assertEquals(1, successfulRenders)
        }
}
