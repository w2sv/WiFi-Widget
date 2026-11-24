package com.w2sv.widget.refreshing

import android.os.PowerManager
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.w2sv.domain.networking.IpApiData
import com.w2sv.domain.networking.NetworkAvailabilitySnapshot
import com.w2sv.domain.networking.NetworkAvailabilitySource
import com.w2sv.domain.networking.RemoteNetworkData
import com.w2sv.domain.networking.RemoteNetworkDataRepository
import com.w2sv.domain.networking.RemoteRefreshResult.Complete
import com.w2sv.domain.networking.RemoteRefreshResult.Incomplete
import com.w2sv.domain.networking.RemoteRefreshResult.NetworkUnavailable
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.domain.widget.WidgetManager
import com.w2sv.widget.rendering.WidgetRenderCoordinator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.assertFailsWith
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WifiWidgetRefreshWorkerTest {
    private val power = mockk<PowerManager>()
    private val repository = mockk<RemoteNetworkDataRepository>(relaxed = true)
    private var availability = NetworkAvailabilitySnapshot(false, true, true, true, 1L, true)
    private val availabilitySource = NetworkAvailabilitySource { availability }
    private val remoteData = MutableStateFlow(RemoteNetworkData.empty)
    private val actions = mockk<WidgetManager>(relaxed = true)
    private val coordinator = mockk<WidgetRenderCoordinator>(relaxed = true)
    private val configSource = mockk<WidgetConfigSource> {
        coEvery { snapshot() } returns WidgetConfig.default
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
            availabilitySource,
            actions,
            configSource,
            coordinator
        )
    }

    @Test fun `periodic screen off skips and succeeds`() =
        runTest {
            every { power.isInteractive } returns false
            assertEquals(ListenableWorker.Result.success(), worker(Periodic).doWork())
            coVerify(exactly = 0) { repository.refreshForWidget(onlyIfMissing = true) }
        }

    @Test fun `periodic render precedes a suspended remote fetch`() =
        runTest {
            every { power.isInteractive } returns true
            val fetching = CompletableDeferred<Unit>()
            val release = CompletableDeferred<Unit>()
            coEvery { repository.refreshForWidget() } coAnswers {
                verify(exactly = 1) { actions.render() }
                fetching.complete(Unit)
                release.await()
                Complete
            }
            val work = async { worker(Periodic).doWork() }
            fetching.await()
            assertFalse(work.isCompleted)
            verify(exactly = 1) { actions.render() }
            release.complete(Unit)
            work.await()
            verify(exactly = 1) { actions.render() }
            verify(exactly = 0) { coordinator.requestRender() }
        }

    @Test fun `persisted immediate work renders without a preceding in-memory render request`() =
        runTest {
            every { power.isInteractive } returns true
            coEvery { repository.refreshForWidget() } returns Complete
            worker(Immediate).doWork()
            verify(exactly = 1) { actions.render() }
            verify(exactly = 0) { coordinator.requestRender() }
            coVerify(exactly = 1) { coordinator.dispatchPending() }
        }

    @Test fun `immediate work requests rendering before a suspended remote fetch`() =
        runTest {
            every { power.isInteractive } returns true
            val entered = CompletableDeferred<Unit>()
            val release = CompletableDeferred<Unit>()
            coEvery { repository.refreshForWidget() } coAnswers {
                verify(exactly = 1) { actions.render() }
                entered.complete(Unit)
                release.await()
                Complete
            }
            val work = async { worker(Immediate).doWork() }
            entered.await()
            assertFalse(work.isCompleted)
            release.complete(Unit)
            assertEquals(ListenableWorker.Result.success(), work.await())
        }

    @Test fun `normal pending dispatch remains cancellable`() =
        runTest {
            every { power.isInteractive } returns true
            coEvery { repository.refreshForWidget() } returns Complete
            val dispatching = CompletableDeferred<Unit>()
            coEvery { coordinator.dispatchPending() } coAnswers {
                dispatching.complete(Unit)
                awaitCancellation()
            }
            val work = async { worker(Immediate).doWork() }
            dispatching.await()
            work.cancel()
            work.join()
            verify(exactly = 0) { coordinator.requestRender() }
        }

    @Test fun `recovery HTTP retry semantics survive dispatch failure`() =
        runTest {
            every { power.isInteractive } returns true
            every { repository.needsRecovery(any()) } returns true
            coEvery { repository.refreshForWidget(true) } returns Incomplete
            coEvery { coordinator.dispatchPending() } throws IllegalStateException("dispatch failed")
            assertEquals(ListenableWorker.Result.retry(), worker(Recovery).doWork())
            coEvery { repository.refreshForWidget(true) } returns Complete
            assertEquals(ListenableWorker.Result.success(), worker(Recovery).doWork())
        }

    @Test fun `changed remote data is dispatched before work can finish`() =
        runTest {
            every { power.isInteractive } returns true
            every { repository.needsRecovery(any()) } returns true
            coEvery { repository.refreshForWidget(true) } coAnswers {
                remoteData.value = RemoteNetworkData(IpApiData(null, null, "UTC", "ISP", null), emptyList())
                Complete
            }
            val dispatching = CompletableDeferred<Unit>()
            val release = CompletableDeferred<Unit>()
            coEvery { coordinator.dispatchPending() } coAnswers {
                dispatching.complete(Unit)
                release.await()
            }
            val work = async { worker(Recovery).doWork() }
            dispatching.await()
            assertFalse(work.isCompleted)
            verify(exactly = 1) { coordinator.requestRender() }
            release.complete(Unit)
            assertEquals(ListenableWorker.Result.success(), work.await())
        }

    @Test fun `successful HTTP is not retried because dispatch failed`() =
        runTest {
            every { power.isInteractive } returns true
            coEvery { repository.refreshForWidget() } returns Complete
            coEvery { coordinator.dispatchPending() } throws IllegalStateException("dispatch failed")
            assertEquals(ListenableWorker.Result.success(), worker(Immediate).doWork())
            coVerify(exactly = 1) { repository.refreshForWidget() }
            // A later render can retry dispatch independently of HTTP work.
            coEvery { coordinator.dispatchPending() } returns Unit
            coordinator.dispatchPending()
            coVerify(exactly = 1) { repository.refreshForWidget() }
        }

    @Test fun `recovery delivers another callers publication while waiting for the repository`() =
        runTest {
            every { power.isInteractive } returns true
            every { repository.needsRecovery(any()) } returns true
            val waiting = CompletableDeferred<Unit>()
            val release = CompletableDeferred<Unit>()
            coEvery { repository.refreshForWidget(true) } coAnswers {
                waiting.complete(Unit)
                release.await()
                // Another refresh already completed the cache, so recovery performs no HTTP.
                Complete
            }
            val work = async { worker(Recovery).doWork() }
            waiting.await()
            remoteData.value = RemoteNetworkData(IpApiData(null, null, "UTC", "other refresh", null), emptyList())
            release.complete(Unit)
            work.await()
            verify(exactly = 1) { coordinator.requestRender() }
            coVerify(exactly = 1) { coordinator.dispatchPending() }
        }

    @Test fun `cancellation after publication signals rendering without waiting on pending dispatch`() =
        runTest {
            every { power.isInteractive } returns true
            every { repository.needsRecovery(any()) } returns true
            val published = CompletableDeferred<Unit>()
            coEvery { repository.refreshForWidget(true) } coAnswers {
                remoteData.value = RemoteNetworkData(IpApiData(null, null, "UTC", "ISP", null), emptyList())
                published.complete(Unit)
                awaitCancellation()
            }
            coEvery { coordinator.dispatchPending() } coAnswers { awaitCancellation() }
            val work = async { worker(Recovery).doWork() }
            published.await()
            work.cancel()
            work.join()
            verify(exactly = 1) { coordinator.requestRender() }
            coVerify(exactly = 0) { coordinator.dispatchPending() }
        }

    @Test fun `manual and periodic refresh fetch through direct Wi-Fi and VPN`() =
        runTest {
            every { power.isInteractive } returns true
            coEvery { repository.refreshForWidget() } returns Complete

            listOf(RefreshWork.Immediate, RefreshWork.Periodic).forEach { reason ->
                assertEquals(ListenableWorker.Result.success(), worker(reason).doWork())
                availability = availability.copy(isDirectWifiDefaultNetwork = false, isVpnDefaultNetwork = true)
                assertEquals(ListenableWorker.Result.success(), worker(reason).doWork())
                availability = availability.copy(isDirectWifiDefaultNetwork = true, isVpnDefaultNetwork = false)
            }

            coVerify(exactly = 4) { repository.refreshForWidget() }
            verify(exactly = 4) { actions.render() }
        }

    @Test fun `manual and periodic refresh skip cellular and unavailable routes but still render`() =
        runTest {
            every { power.isInteractive } returns true
            availability = availability.copy(isDirectWifiDefaultNetwork = false)

            listOf(RefreshWork.Immediate, RefreshWork.Periodic).forEach { reason ->
                assertEquals(ListenableWorker.Result.success(), worker(reason).doWork())
                availability = availability.copy(hasUsableDefaultNetwork = false, defaultNetworkIdentity = null)
                assertEquals(ListenableWorker.Result.success(), worker(reason).doWork())
                availability = availability.copy(hasUsableDefaultNetwork = true, defaultNetworkIdentity = 2L)
            }

            coVerify(exactly = 0) { repository.refreshForWidget() }
            verify(exactly = 4) { actions.render() }
        }

    @Test fun `recovery remains retryable after prolonged non-interactive execution`() =
        runTest {
            every { power.isInteractive } returns false
            assertEquals(ListenableWorker.Result.retry(), worker(Recovery).doWork())
            assertEquals(ListenableWorker.Result.retry(), worker(Recovery, 100).doWork())
            coVerify(exactly = 0) { repository.refreshForWidget(onlyIfMissing = true) }
        }

    @Test fun `initialized recovery does not fetch or render`() =
        runTest {
            every { power.isInteractive } returns true
            every { repository.needsRecovery(any()) } returns false
            assertEquals(ListenableWorker.Result.success(), worker(Recovery).doWork())
            coVerify(exactly = 0) { repository.refreshForWidget(onlyIfMissing = true) }
            verify(exactly = 0) { actions.render() }
        }

    @Test fun `validation restores cached data and renders without HTTP`() =
        runTest {
            every { power.isInteractive } returns true
            val restored = RemoteNetworkData(IpApiData(null, null, "UTC", "restored ISP", null), emptyList())
            every { repository.needsRecovery(any()) } answers {
                remoteData.value = restored
                false
            }
            assertEquals(ListenableWorker.Result.success(), worker(Recovery).doWork())
            coVerify(exactly = 0) { repository.refreshForWidget(onlyIfMissing = true) }
            verify(exactly = 1) { coordinator.requestRender() }
        }

    @Test fun `recovery completes and renders newly published data`() =
        runTest {
            every { power.isInteractive } returns true
            every { repository.needsRecovery(any()) } returns true
            coEvery { repository.refreshForWidget(onlyIfMissing = true) } answers {
                remoteData.value = RemoteNetworkData(IpApiData(null, null, "UTC", "ISP", null), emptyList())
                Complete
            }
            assertEquals(ListenableWorker.Result.success(), worker(Recovery).doWork())
            coVerify(exactly = 1) { repository.refreshForWidget(onlyIfMissing = true) }
            verify(exactly = 1) { coordinator.requestRender() }
        }

    @Test fun `recovery retries when initial HTTP requests fail`() =
        runTest {
            every { power.isInteractive } returns true
            every { repository.needsRecovery(any()) } returns true
            coEvery { repository.refreshForWidget(onlyIfMissing = true) } returns Incomplete
            assertEquals(ListenableWorker.Result.retry(), worker(Recovery).doWork())
            verify(exactly = 0) { actions.render() }
        }

    @Test fun `queued recovery does not fetch through cellular or unavailable default`() =
        runTest {
            every { power.isInteractive } returns true
            every { repository.needsRecovery(any()) } returns true
            availability = availability.copy(isDirectWifiDefaultNetwork = false)

            assertEquals(ListenableWorker.Result.retry(), worker(Recovery).doWork())
            availability = availability.copy(hasUsableDefaultNetwork = false, defaultNetworkIdentity = null)
            assertEquals(ListenableWorker.Result.retry(), worker(Recovery).doWork())

            coVerify(exactly = 0) { repository.refreshForWidget(onlyIfMissing = true) }
            verify(exactly = 2) { repository.needsRecovery(any()) }
        }

    @Test fun `queued recovery resumes when direct Wi-Fi or VPN becomes the default`() =
        runTest {
            every { power.isInteractive } returns true
            every { repository.needsRecovery(any()) } returns true
            coEvery { repository.refreshForWidget(onlyIfMissing = true) } returns Complete
            availability = availability.copy(isDirectWifiDefaultNetwork = false)
            assertEquals(ListenableWorker.Result.retry(), worker(Recovery).doWork())

            availability = availability.copy(isVpnDefaultNetwork = true)
            assertEquals(ListenableWorker.Result.success(), worker(Recovery).doWork())

            availability = availability.copy(isVpnDefaultNetwork = false, isDirectWifiDefaultNetwork = true)
            assertEquals(ListenableWorker.Result.success(), worker(Recovery).doWork())
            coVerify(exactly = 2) { repository.refreshForWidget(onlyIfMissing = true) }
        }

    @Test fun `recovery renders partial data before retrying`() =
        runTest {
            every { power.isInteractive } returns true
            every { repository.needsRecovery(any()) } returns true
            coEvery { repository.refreshForWidget(onlyIfMissing = true) } answers {
                remoteData.value = RemoteNetworkData(IpApiData(null, null, "UTC", "ISP", null), emptyList())
                Incomplete
            }
            assertEquals(ListenableWorker.Result.retry(), worker(Recovery, 20).doWork())
            coVerify(exactly = 1) { repository.refreshForWidget(onlyIfMissing = true) }
            verify(exactly = 1) { coordinator.requestRender() }
        }

    @Test fun `recovery with unchanged published data does not render`() =
        runTest {
            every { power.isInteractive } returns true
            every { repository.needsRecovery(any()) } returns true
            coEvery { repository.refreshForWidget(onlyIfMissing = true) } returns Complete

            assertEquals(ListenableWorker.Result.success(), worker(Recovery).doWork())
            coVerify(exactly = 1) { repository.refreshForWidget(onlyIfMissing = true) }
            verify(exactly = 0) { actions.render() }
        }

    @Test fun `temporary connectivity loss retries without rendering`() =
        runTest {
            every { power.isInteractive } returns true
            every { repository.needsRecovery(any()) } returns true
            coEvery { repository.refreshForWidget(onlyIfMissing = true) } returns NetworkUnavailable
            assertEquals(ListenableWorker.Result.retry(), worker(Recovery).doWork())
            verify(exactly = 0) { actions.render() }
        }

    @Test fun `known route invalidation renders even when recovery cannot fetch`() =
        runTest {
            every { power.isInteractive } returns true
            remoteData.value = RemoteNetworkData(IpApiData(null, null, "UTC", "old ISP", null), emptyList())
            every { repository.needsRecovery(any()) } answers {
                remoteData.value = RemoteNetworkData.empty
                true
            }
            coEvery { repository.refreshForWidget(onlyIfMissing = true) } returns NetworkUnavailable
            assertEquals(ListenableWorker.Result.retry(), worker(Recovery).doWork())
            coVerify(exactly = 1) { repository.refreshForWidget(onlyIfMissing = true) }
            verify(exactly = 1) { coordinator.requestRender() }
        }

    @Test fun `cache loss and restoration each render on the same route`() =
        runTest {
            every { power.isInteractive } returns true
            val saved = RemoteNetworkData(IpApiData(null, null, "UTC", "ISP", null), emptyList())
            remoteData.value = saved
            var attempt = 0
            every { repository.needsRecovery(any()) } answers {
                if (attempt++ == 0) remoteData.value = RemoteNetworkData.empty
                true
            }
            coEvery { repository.refreshForWidget(onlyIfMissing = true) } answers {
                if (attempt == 1) {
                    NetworkUnavailable
                } else {
                    remoteData.value = saved
                    Complete
                }
            }
            assertEquals(ListenableWorker.Result.retry(), worker(Recovery).doWork())
            assertEquals(ListenableWorker.Result.success(), worker(Recovery).doWork())
            verify(exactly = 2) { coordinator.requestRender() }
        }

    @Test fun `recovery remains possible after Wi-Fi connectivity returns`() =
        runTest {
            every { power.isInteractive } returns true
            every { repository.needsRecovery(any()) } returns true
            coEvery { repository.refreshForWidget(onlyIfMissing = true) } returns Complete
            availability = availability.copy(isWifiConnected = false)
            assertEquals(ListenableWorker.Result.retry(), worker(Recovery).doWork())
            coVerify(exactly = 0) { repository.refreshForWidget(onlyIfMissing = true) }
            availability = availability.copy(isWifiConnected = true)
            assertEquals(ListenableWorker.Result.success(), worker(Recovery).doWork())
            coVerify(exactly = 1) { repository.refreshForWidget(onlyIfMissing = true) }
        }

    @Test fun `cancelling recovery propagates without rendering`() =
        runTest {
            every { power.isInteractive } returns true
            every { repository.needsRecovery(any()) } returns true
            coEvery { repository.refreshForWidget(onlyIfMissing = true) } throws CancellationException("cancelled")

            assertFailsWith<CancellationException> { worker(Recovery).doWork() }
            verify(exactly = 0) { actions.render() }
        }

    @Test fun `cancellation after publication still renders changed data`() =
        runTest {
            every { power.isInteractive } returns true
            every { repository.needsRecovery(any()) } returns true
            coEvery { repository.refreshForWidget(onlyIfMissing = true) } coAnswers {
                remoteData.value = RemoteNetworkData(IpApiData(null, null, "UTC", "ISP", null), emptyList())
                throw CancellationException("cancelled after publication")
            }

            assertFailsWith<CancellationException> { worker(Recovery).doWork() }
            coVerify(exactly = 1) { repository.refreshForWidget(onlyIfMissing = true) }
            verify(exactly = 1) { coordinator.requestRender() }
        }
}
