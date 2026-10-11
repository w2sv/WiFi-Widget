package com.w2sv.widget.refreshing

import android.os.PowerManager
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.w2sv.domain.model.networking.IpApiData
import com.w2sv.domain.model.networking.RemoteWifiData
import com.w2sv.domain.model.networking.WifiStatus
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.repository.RemoteWifiDataRepository
import com.w2sv.domain.repository.WidgetConfigReader
import com.w2sv.networking.wifistatus.provider.WifiStatusProvider
import com.w2sv.widget.actions.WidgetActions
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.assertFailsWith
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WifiWidgetRefreshWorkerTest {
    private val power = mockk<PowerManager>()
    private val repository = mockk<RemoteWifiDataRepository>(relaxed = true)
    private val remoteData = MutableStateFlow(RemoteWifiData.empty)
    private val actions = mockk<WidgetActions>(relaxed = true)
    private val wifiStatus = mockk<WifiStatusProvider>()
    private val configReader = mockk<WidgetConfigReader> {
        coEvery { read() } returns WidgetConfig.default
    }

    private fun worker(reason: RefreshWork, attempts: Int = 0): WifiWidgetRefreshWorker {
        every { repository.data } returns remoteData
        val params = mockk<WorkerParameters>(relaxed = true) {
            every { inputData } returns reason.toWorkData()
            every { runAttemptCount } returns attempts
        }
        return WifiWidgetRefreshWorker(
            ApplicationProvider.getApplicationContext(),
            params,
            power,
            repository,
            actions,
            wifiStatus,
            configReader
        )
    }

    @Test fun `periodic screen off skips and succeeds`() =
        runTest {
            every { power.isInteractive } returns false
            assertEquals(ListenableWorker.Result.success(), worker(Periodic).doWork())
            coVerify(exactly = 0) { repository.refresh(onlyIfMissing = true) }
        }

    @Test fun `recovery remains retryable after prolonged non-interactive execution`() =
        runTest {
            every { power.isInteractive } returns false
            assertEquals(ListenableWorker.Result.retry(), worker(Recovery).doWork())
            assertEquals(ListenableWorker.Result.retry(), worker(Recovery, 100).doWork())
            coVerify(exactly = 0) { repository.refresh(onlyIfMissing = true) }
        }

    @Test fun `initialized recovery does not fetch or render`() =
        runTest {
            every { power.isInteractive } returns true
            every { repository.needsRecovery(any()) } returns false
            assertEquals(ListenableWorker.Result.success(), worker(Recovery).doWork())
            coVerify(exactly = 0) { repository.refresh(onlyIfMissing = true) }
            verify(exactly = 0) { actions.render() }
        }

    @Test fun `validation restores cached data and renders without HTTP`() =
        runTest {
            every { power.isInteractive } returns true
            val restored = RemoteWifiData(IpApiData(null, null, "UTC", "restored ISP", null), emptyList())
            every { repository.needsRecovery(any()) } answers {
                remoteData.value = restored
                false
            }
            assertEquals(ListenableWorker.Result.success(), worker(Recovery).doWork())
            coVerify(exactly = 0) { repository.refresh(onlyIfMissing = true) }
            verify(exactly = 1) { actions.render() }
        }

    @Test fun `recovery completes and renders when all groups are initialized`() =
        runTest {
            every { power.isInteractive } returns true
            every { repository.needsRecovery(any()) } returns true
            every { wifiStatus() } returns WifiStatus.Connected
            coEvery { repository.refresh(onlyIfMissing = true) } answers {
                remoteData.value = RemoteWifiData(IpApiData(null, null, "UTC", "ISP", null), emptyList())
                Complete
            }
            assertEquals(ListenableWorker.Result.success(), worker(Recovery).doWork())
            verify(exactly = 1) { actions.render() }
        }

    @Test fun `recovery retries when initial HTTP requests fail`() =
        runTest {
            every { power.isInteractive } returns true
            every { repository.needsRecovery(any()) } returns true
            every { wifiStatus() } returns WifiStatus.Connected
            coEvery { repository.refresh(onlyIfMissing = true) } returns Incomplete
            assertEquals(ListenableWorker.Result.retry(), worker(Recovery).doWork())
            verify(exactly = 0) { actions.render() }
        }

    @Test fun `recovery renders partial data before retrying`() =
        runTest {
            every { power.isInteractive } returns true
            every { repository.needsRecovery(any()) } returns true
            every { wifiStatus() } returns WifiStatus.Connected
            coEvery { repository.refresh(onlyIfMissing = true) } answers {
                remoteData.value = RemoteWifiData(IpApiData(null, null, "UTC", "ISP", null), emptyList())
                Incomplete
            }
            assertEquals(ListenableWorker.Result.retry(), worker(Recovery, 20).doWork())
            verify(exactly = 1) { actions.render() }
        }

    @Test fun `temporary connectivity loss retries without rendering`() =
        runTest {
            every { power.isInteractive } returns true
            every { repository.needsRecovery(any()) } returns true
            every { wifiStatus() } returns WifiStatus.ConnectedInactive
            coEvery { repository.refresh(onlyIfMissing = true) } returns NetworkUnavailable
            assertEquals(ListenableWorker.Result.retry(), worker(Recovery).doWork())
            verify(exactly = 0) { actions.render() }
        }

    @Test fun `known route invalidation renders even when recovery cannot fetch`() =
        runTest {
            every { power.isInteractive } returns true
            remoteData.value = RemoteWifiData(IpApiData(null, null, "UTC", "old ISP", null), emptyList())
            every { repository.needsRecovery(any()) } answers {
                remoteData.value = RemoteWifiData.empty
                true
            }
            every { wifiStatus() } returns WifiStatus.Connected
            coEvery { repository.refresh(onlyIfMissing = true) } returns NetworkUnavailable
            assertEquals(ListenableWorker.Result.retry(), worker(Recovery).doWork())
            verify(exactly = 1) { actions.render() }
        }

    @Test fun `widget renders cache loss and restoration on the same route`() =
        runTest {
            every { power.isInteractive } returns true
            val saved = RemoteWifiData(IpApiData(null, null, "UTC", "ISP", null), emptyList())
            remoteData.value = saved
            var attempt = 0
            every { repository.needsRecovery(any()) } answers {
                if (attempt++ == 0) remoteData.value = RemoteWifiData.empty
                true
            }
            every { wifiStatus() } returns WifiStatus.Connected
            coEvery { repository.refresh(onlyIfMissing = true) } answers {
                if (attempt == 1) {
                    NetworkUnavailable
                } else {
                    remoteData.value = saved
                    Complete
                }
            }
            assertEquals(ListenableWorker.Result.retry(), worker(Recovery).doWork())
            assertEquals(ListenableWorker.Result.success(), worker(Recovery).doWork())
            verify(exactly = 2) { actions.render() }
        }

    @Test fun `recovery remains possible after Wi-Fi connectivity returns`() =
        runTest {
            every { power.isInteractive } returns true
            every { repository.needsRecovery(any()) } returns true
            every { wifiStatus() } returnsMany listOf(WifiStatus.Disabled, WifiStatus.Connected)
            coEvery { repository.refresh(onlyIfMissing = true) } returns Complete
            assertEquals(ListenableWorker.Result.retry(), worker(Recovery).doWork())
            coVerify(exactly = 0) { repository.refresh(onlyIfMissing = true) }
            assertEquals(ListenableWorker.Result.success(), worker(Recovery).doWork())
            coVerify(exactly = 1) { repository.refresh(onlyIfMissing = true) }
        }

    @Test fun `cancelling recovery propagates without rendering`() =
        runTest {
            every { power.isInteractive } returns true
            every { repository.needsRecovery(any()) } returns true
            every { wifiStatus() } returns WifiStatus.Connected
            coEvery { repository.refresh(onlyIfMissing = true) } throws CancellationException("cancelled")

            assertFailsWith<CancellationException> { worker(Recovery).doWork() }
            verify(exactly = 0) { actions.render() }
        }
}
