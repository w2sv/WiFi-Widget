package com.w2sv.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import androidx.glance.ExperimentalGlanceApi
import androidx.test.core.app.ApplicationProvider
import com.w2sv.domain.networking.NetworkAvailabilitySnapshot
import com.w2sv.domain.networking.NetworkAvailabilitySource
import com.w2sv.domain.networking.RemoteNetworkDataRepository
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.widget.refreshing.WifiWidgetWorkScheduler
import com.w2sv.widget.rendering.WidgetRenderCoordinator
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WifiWidgetProviderTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val scheduler = mockk<WifiWidgetWorkScheduler>(relaxed = true)
    private val coordinator = mockk<WidgetRenderCoordinator>(relaxed = true)
    private val repository = mockk<RemoteNetworkDataRepository>()
    private val observer = mockk<RemoteDataWidgetObserver>(relaxed = true)
    private var directWifiDefault = true
    private var vpnDefault = false
    private val availabilitySource = NetworkAvailabilitySource {
        NetworkAvailabilitySnapshot(false, true, true, true, 1L, directWifiDefault, vpnDefault)
    }
    private val config = WidgetConfig.default

    private fun provider(): WifiWidgetProvider =
        WifiWidgetProvider().apply {
            renderCoordinator = coordinator
            refreshManager = scheduler
            remoteNetworkDataRepository = repository
            networkAvailabilitySource = availabilitySource
            remoteDataWidgetObserver = observer
            widgetConfigSource = mockk<WidgetConfigSource> { every { blockingSnapshot() } returns config }
        }

    @Test fun `first widget applies persisted periodic policy and starts observation`() {
        provider().onEnabled(context)
        verify(exactly = 1) { scheduler.applyRefreshingPolicy(config.refreshing) }
        verify(exactly = 1) { observer.onWidgetsAvailable() }
    }

    @OptIn(ExperimentalGlanceApi::class)
    @Test
    fun `system update invalidates before Glance and prepares recovery afterward`() =
        runTest {
            val events = mutableListOf<String>()
            val receiver = spyk(provider())
            val dispatchJob = Job()
            every { receiver.coroutineContext } returns StandardTestDispatcher(testScheduler) + dispatchJob
            // Observe the public broadcast dispatch boundary; cancel queued library work before it runs.
            every { receiver.goAsync() } answers {
                events += "Glance"
                mockk<BroadcastReceiver.PendingResult>(relaxed = true)
            }
            every { coordinator.invalidatePresentation() } answers {
                events += "invalidate"
                1L
            }
            every { observer.onWidgetsAvailable() } answers { events += "observe" }
            every { receiver.widgetConfigSource.blockingSnapshot() } answers {
                events += "configuration"
                config
            }
            every { repository.needsRecovery(any()) } answers {
                events += "reconcile"
                false
            }
            try {
                receiver.onUpdate(context, mockk<AppWidgetManager>(), intArrayOf(1))
                assertEquals(listOf("invalidate", "Glance", "observe", "configuration", "reconcile"), events)
                events.clear()
                receiver.onUpdate(context, mockk<AppWidgetManager>(), intArrayOf())
                assertEquals(listOf("Glance"), events)
            } finally {
                dispatchJob.cancel()
            }
        }

    @Test fun `full update schedules recovery for missing remote data`() {
        every { repository.needsRecovery(config.remoteDataConfig) } returns true
        provider().reconcileUpdate(intArrayOf(1))
        verify(exactly = 1) { scheduler.enqueueRecovery() }
        verify(exactly = 1) { observer.onWidgetsAvailable() }
        verify(exactly = 0) { coordinator.invalidatePresentation() }
    }

    @Test fun `full update schedules recovery through VPN while Wi-Fi is connected`() {
        every { repository.needsRecovery(config.remoteDataConfig) } returns true
        directWifiDefault = false
        vpnDefault = true
        provider().reconcileUpdate(intArrayOf(1))
        verify(exactly = 1) { scheduler.enqueueRecovery() }
    }

    @Test fun `initialized data does not schedule recovery`() {
        every { repository.needsRecovery(config.remoteDataConfig) } returns false
        repeat(3) { provider().reconcileUpdate(intArrayOf(1)) }
        verify(exactly = 0) { scheduler.enqueueRecovery() }
    }

    @Test fun `cellular route does not schedule recovery`() {
        every { repository.needsRecovery(config.remoteDataConfig) } returns true
        directWifiDefault = false
        provider().reconcileUpdate(intArrayOf(1))
        verify(exactly = 0) { scheduler.enqueueRecovery() }
    }

    @Test fun `empty update does not observe or recover`() {
        provider().reconcileUpdate(intArrayOf())
        verify(exactly = 0) { observer.onWidgetsAvailable() }
        verify(exactly = 0) { repository.needsRecovery(any()) }
        verify(exactly = 0) { coordinator.invalidatePresentation() }
    }

    @Test fun `last widget cancels periodic and recovery work`() {
        provider().onDisabled(context)
        verify(exactly = 1) { scheduler.cancelPeriodicWork() }
        verify(exactly = 1) { scheduler.cancelRecoveryWork() }
        verify(exactly = 1) { observer.onLastWidgetRemoved() }
    }
}
