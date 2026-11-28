package com.w2sv.wifiwidget.ui.screen.home.synchronization

import app.cash.turbine.test
import com.w2sv.domain.networking.IpApiData
import com.w2sv.domain.networking.NetworkAvailabilitySnapshot
import com.w2sv.domain.networking.NetworkAvailabilitySource
import com.w2sv.domain.networking.RemoteNetworkData
import com.w2sv.domain.networking.RemoteNetworkDataRepository
import com.w2sv.domain.networking.RemoteRefreshResult.Complete
import com.w2sv.domain.networking.WifiStatus
import com.w2sv.domain.networking.WifiStatusSource
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.domain.wifiproperty.WifiProperty
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@OptIn(ExperimentalCoroutinesApi::class)
class RemoteDataSynchronizerTest {
    private val status = MutableSharedFlow<WifiStatus>()
    private val egress = MutableSharedFlow<Unit>()
    private val config = MutableStateFlow(WidgetConfig.default)
    private val data = MutableStateFlow(RemoteNetworkData.empty)
    private var snapshotStatus: WifiStatus = WifiStatus.Connected
    private var defaultIdentity = 1L
    private var wifiIdentity = 1L
    private var hasUsableDefaultNetwork = true
    private var isDirectWifiDefaultNetwork = true
    private var isVpnDefaultNetwork = false
    private val wifiStatusSource = mockk<WifiStatusSource> {
        every { this@mockk.status } returns this@RemoteDataSynchronizerTest.status
        every { defaultEgressChanges } returns egress
        every { snapshot() } answers { snapshotStatus }
        every { connectedWifiIdentity() } answers { wifiIdentity }
    }
    private val source = mockk<WidgetConfigSource> {
        every { flow } returns config
        coEvery { snapshot() } answers { config.value }
    }
    private val repository = mockk<RemoteNetworkDataRepository>(relaxed = true) {
        every { this@mockk.data } returns this@RemoteDataSynchronizerTest.data
        every { reconciledSnapshot(any()) } answers { data.value }
    }
    private fun TestScope.synchronizer() =
        RemoteDataSynchronizer(
            wifiStatusSource,
            source,
            repository,
            NetworkAvailabilitySource {
                NetworkAvailabilitySnapshot(
                    false,
                    hasUsableDefaultNetwork,
                    true,
                    true,
                    defaultIdentity,
                    isDirectWifiDefaultNetwork,
                    isVpnDefaultNetwork
                )
            },
            CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler))
        )

    @Test fun `initial status reconciles before publication and refreshes once`() =
        runTest {
            var reconciled = false
            every { repository.reconciledSnapshot(any()) } answers {
                reconciled = true
                data.value
            }
            val synchronizer = synchronizer()
            synchronizer.inputs.test {
                this@RemoteDataSynchronizerTest.status.emit(WifiStatus.Connected)
                assertEquals(WifiStatus.Connected, awaitItem().status)
                assertTrue(reconciled)
                coVerify(exactly = 1) { repository.refresh() }
            }
        }

    @Test fun `resume before initial status defers to the full startup refresh with a partial cache`() =
        runTest {
            val entered = CompletableDeferred<Unit>()
            val release = CompletableDeferred<Unit>()
            var missing = true
            every { repository.needsRecovery(any()) } answers { missing }
            data.value = RemoteNetworkData(IpApiData(null, null, "UTC", "cached ISP", null), emptyList())
            coEvery { repository.refresh(false) } coAnswers {
                entered.complete(Unit)
                release.await()
                missing = false
                Complete
            }
            val synchronizer = synchronizer()
            repeat(3) { synchronizer.recoverOnResume() }
            coVerify(exactly = 0) { repository.refresh(any()) }
            status.emit(WifiStatus.Connected)
            entered.await()
            val resume = async { synchronizer.recoverOnResume() }
            runCurrent()
            coVerify(exactly = 1) { repository.refresh(false) }
            release.complete(Unit)
            resume.await()
            synchronizer.recoverOnResume()
            coVerify(exactly = 0) { repository.refresh(true) }
        }

    @Test fun `disconnected startup clears ownership before subsequent full synchronization`() =
        runTest {
            val synchronizer = synchronizer()
            synchronizer.recoverOnResume()
            status.emit(WifiStatus.NotConnected)
            snapshotStatus = WifiStatus.NotConnected
            synchronizer.recoverOnResume()
            snapshotStatus = WifiStatus.Connected
            status.emit(WifiStatus.Connected)
            runCurrent()
            coVerify(exactly = 0) { repository.refresh(true) }
            coVerify(exactly = 1) { repository.refresh(false) }
        }

    @Test fun `initial status fetch satisfies overlapping resume recovery`() =
        runTest {
            val entered = CompletableDeferred<Unit>()
            val release = CompletableDeferred<Unit>()
            var missing = true
            data.value = RemoteNetworkData(IpApiData(null, null, "UTC", "cached ISP", null), emptyList())
            every { repository.needsRecovery(any()) } answers { missing }
            coEvery { repository.refresh(false) } coAnswers {
                entered.complete(Unit)
                release.await()
                missing = false
                Complete
            }
            val synchronizer = synchronizer()
            status.emit(WifiStatus.Connected)
            entered.await()
            val resumes = List(3) { async { synchronizer.recoverOnResume() } }
            runCurrent()
            coVerify(exactly = 0) { repository.refresh(true) }
            release.complete(Unit)
            resumes.forEach { it.await() }
            synchronizer.recoverOnResume()
            coVerify(exactly = 1) { repository.refresh(false) }
            coVerify(exactly = 0) { repository.refresh(true) }
        }

    @Test fun `changed route before initial status still receives one full startup synchronization`() =
        runTest {
            every { repository.needsRecovery(any()) } returns true
            coEvery { repository.refresh(true) } returns Complete
            val synchronizer = synchronizer()
            synchronizer.recoverOnResume()
            defaultIdentity = 2L
            status.emit(WifiStatus.Connected)
            runCurrent()
            coVerify(exactly = 0) { repository.refresh(true) }
            coVerify(exactly = 1) { repository.refresh(false) }
        }

    @Test fun `configuration change replaces recovery without waiting for its HTTP`() =
        runTest {
            every { repository.needsRecovery(any()) } returns true
            val entered = CompletableDeferred<Unit>()
            val cancelled = CompletableDeferred<Unit>()
            coEvery { repository.refresh(true) } coAnswers {
                entered.complete(Unit)
                try {
                    awaitCancellation()
                } finally {
                    cancelled.complete(Unit)
                }
            }
            val synchronizer = synchronizer()
            status.emit(WifiStatus.Connected)
            runCurrent()
            val recovery = async { synchronizer.recoverOnResume() }
            entered.await()
            coEvery { repository.refresh(true) } returns Complete
            config.value = config.value.withUpdatedPropertyEnablement(WifiProperty.PublicIp, false)
            cancelled.await()
            recovery.await()
            coVerify(exactly = 1) { repository.refresh(false) }
            coVerify(exactly = 2) { repository.refresh(true) }
            assertEquals(config.value.remoteDataConfig, synchronizer.inputs.replayCache.last().remoteDataConfig)
        }

    @Test fun `failed or cancelled requests leave missing data recoverable on resume`() =
        runTest {
            every { repository.needsRecovery(any()) } returns true
            val synchronizer = synchronizer()
            coEvery { repository.refresh(false) } throws IllegalStateException("startup failure")
            status.emit(WifiStatus.Connected)
            runCurrent()
            coEvery { repository.refresh(true) } throws IllegalStateException("endpoint failure")
            synchronizer.recoverOnResume()
            coEvery { repository.refresh(true) } throws CancellationException("obsolete request")
            synchronizer.recoverOnResume()
            coEvery { repository.refresh(true) } returns Complete
            synchronizer.recoverOnResume()
            coVerify(exactly = 3) { repository.refresh(true) }
        }

    @Test fun `default egress before status cannot take ownership of startup fetching`() =
        runTest {
            val synchronizer = synchronizer()
            egress.emit(Unit)
            synchronizer.recoverOnResume()
            coVerify(exactly = 0) { repository.refresh(any()) }
            status.emit(WifiStatus.Connected)
            runCurrent()
            coVerify(exactly = 1) { repository.refresh(false) }
        }

    @Test fun `changed Wi-Fi identity refreshes even when connected status stays equal`() =
        runTest {
            synchronizer()
            status.emit(WifiStatus.ConnectedInactive)
            runCurrent()
            wifiIdentity = 2L
            status.emit(WifiStatus.ConnectedInactive)
            runCurrent()
            coVerify(exactly = 2) { repository.refresh(false) }
        }

    @Test fun `a complete prior cache skips resume HTTP but preserves initial full synchronization`() =
        runTest {
            every { repository.needsRecovery(any()) } returns false
            val synchronizer = synchronizer()
            synchronizer.recoverOnResume()
            status.emit(WifiStatus.Connected)
            runCurrent()
            coVerify(exactly = 0) { repository.refresh(true) }
            coVerify(exactly = 1) { repository.refresh(false) }
        }

    @Test fun `identical callbacks republish inputs without redundant HTTP`() =
        runTest {
            val synchronizer = synchronizer()
            synchronizer.inputs.test {
                this@RemoteDataSynchronizerTest.status.emit(WifiStatus.Connected)
                awaitItem()
                repeat(3) {
                    this@RemoteDataSynchronizerTest.status.emit(WifiStatus.Connected)
                    assertEquals(WifiStatus.Connected, awaitItem().status)
                }
                coVerify(exactly = 1) { repository.refresh() }
            }
        }

    @Test fun `remote requirements refresh but local settings do not reconcile`() =
        runTest {
            var snapshots = 0
            every { repository.reconciledSnapshot(any()) } answers {
                snapshots++
                data.value
            }
            val synchronizer = synchronizer()
            synchronizer.inputs.test {
                this@RemoteDataSynchronizerTest.status.emit(WifiStatus.NotConnected)
                awaitItem()
                config.value = config.value.withUpdatedPropertyEnablement(WifiProperty.SSID, true)
                expectNoEvents()
                coVerify(exactly = 0) { repository.refresh() }
                this@RemoteDataSynchronizerTest.status.emit(WifiStatus.Connected)
                awaitItem()
                coVerify(exactly = 1) { repository.refresh() }
                config.value = config.value.withUpdatedPropertyEnablement(WifiProperty.PublicIp, false)
                awaitItem()
                coVerify(exactly = 1) { repository.refresh(false) }
                coVerify(exactly = 1) { repository.refresh(true) }
                val firstEnabled = config.value.enabledProperties.first()
                val lastEnabled = config.value.enabledProperties.last()
                val snapshotsBeforeReorder = snapshots
                config.value = config.value.withUpdatedPropertyPosition(
                    config.value.propertyOrder.indexOf(firstEnabled),
                    config.value.propertyOrder.indexOf(lastEnabled)
                )
                expectNoEvents()
                assertEquals(snapshotsBeforeReorder, snapshots)
                coVerify(exactly = 1) { repository.refresh(false) }
                coVerify(exactly = 1) { repository.refresh(true) }
                config.value = config.value.copy(refreshing = config.value.refreshing.copy(refreshPeriodically = false))
                expectNoEvents()
                coVerify(exactly = 1) { repository.refresh(false) }
                coVerify(exactly = 1) { repository.refresh(true) }
            }
        }

    @Test fun `enabling remote requirements reconciles before publishing and requests missing data`() =
        runTest {
            config.value = config.value.withUpdatedPropertyEnablement(WifiProperty.PublicIp, false)
            val synchronizer = synchronizer()
            status.emit(WifiStatus.Connected)
            runCurrent()
            var reconciled = config.value.remoteDataConfig
            every { repository.reconciledSnapshot(any()) } answers {
                reconciled = firstArg()
                data.value
            }
            synchronizer.inputs.test {
                awaitItem()
                config.value = config.value.withUpdatedPropertyEnablement(WifiProperty.PublicIp, true)
                val input = awaitItem()
                assertEquals(config.value.remoteDataConfig, input.remoteDataConfig)
                assertEquals(input.remoteDataConfig, reconciled)
            }
            coVerify(exactly = 1) { repository.refresh(false) }
            coVerify(exactly = 1) { repository.refresh(true) }
        }

    @Test fun `default egress refreshes independently of status only while connected`() =
        runTest {
            val synchronizer = synchronizer()
            synchronizer.inputs.test {
                this@RemoteDataSynchronizerTest.status.emit(WifiStatus.ConnectedInactive)
                awaitItem()
                snapshotStatus = WifiStatus.ConnectedInactive
                egress.emit(Unit)
                assertEquals(WifiStatus.ConnectedInactive, awaitItem().status)
                coVerify(exactly = 2) { repository.refresh() }
                this@RemoteDataSynchronizerTest.status.emit(WifiStatus.NotConnected)
                awaitItem()
                snapshotStatus = WifiStatus.NotConnected
                egress.emit(Unit)
                assertEquals(WifiStatus.NotConnected, awaitItem().status)
                coVerify(exactly = 2) { repository.refresh() }
            }
            verify(exactly = 2) { wifiStatusSource.snapshot() }
        }

    @Test fun `default egress snapshots VPN and Wi-Fi transitions and updates configuration status`() =
        runTest {
            val synchronizer = synchronizer()
            synchronizer.inputs.test {
                this@RemoteDataSynchronizerTest.status.emit(WifiStatus.Connected)
                assertEquals(WifiStatus.Connected, awaitItem().status)

                snapshotStatus = WifiStatus.ConnectedInactive
                egress.emit(Unit)
                assertEquals(WifiStatus.ConnectedInactive, awaitItem().status)

                config.value = config.value.withUpdatedPropertyEnablement(WifiProperty.PublicIp, false)
                assertEquals(WifiStatus.ConnectedInactive, awaitItem().status)

                snapshotStatus = WifiStatus.Connected
                egress.emit(Unit)
                assertEquals(WifiStatus.Connected, awaitItem().status)
                coVerify(exactly = 3) { repository.refresh(false) }
                coVerify(exactly = 1) { repository.refresh(true) }
            }
            verify(exactly = 2) { wifiStatusSource.snapshot() }
        }

    @Test fun `connected status transitions refresh while identical callbacks do not`() =
        runTest {
            val synchronizer = synchronizer()
            synchronizer.inputs.test {
                this@RemoteDataSynchronizerTest.status.emit(WifiStatus.Connected)
                awaitItem()
                this@RemoteDataSynchronizerTest.status.emit(WifiStatus.Connected)
                awaitItem()
                coVerify(exactly = 1) { repository.refresh() }
                this@RemoteDataSynchronizerTest.status.emit(WifiStatus.ConnectedInactive)
                assertEquals(WifiStatus.ConnectedInactive, awaitItem().status)
                coVerify(exactly = 2) { repository.refresh() }
            }
        }

    @Test fun `equivalent status and egress events share active full HTTP and still reconcile and publish`() =
        runTest {
            val started = CompletableDeferred<Unit>()
            val release = CompletableDeferred<Unit>()
            val cancelled = CompletableDeferred<Unit>()
            coEvery { repository.refresh(false) } coAnswers {
                started.complete(Unit)
                try {
                    release.await()
                } catch (failure: CancellationException) {
                    cancelled.complete(Unit)
                    throw failure
                }
                Complete
            }
            val synchronizer = synchronizer()
            synchronizer.inputs.test {
                this@RemoteDataSynchronizerTest.status.emit(WifiStatus.Connected)
                awaitItem()
                started.await()
                var reconciled = false
                every { repository.reconciledSnapshot(any()) } answers {
                    reconciled = true
                    data.value
                }
                egress.emit(Unit)
                assertEquals(WifiStatus.Connected, awaitItem().status)
                assertTrue(reconciled)
                runCurrent()
                assertFalse(cancelled.isCompleted)
                coVerify(exactly = 1) { repository.refresh(false) }
                release.complete(Unit)
                runCurrent()
                egress.emit(Unit)
                awaitItem()
                runCurrent()
                coVerify(exactly = 2) { repository.refresh(false) }
            }
        }

    @Test fun `active full retrieval satisfies resume missing-data request even with a complete cache`() =
        runTest {
            val started = CompletableDeferred<Unit>()
            val release = CompletableDeferred<Unit>()
            coEvery { repository.refresh(false) } coAnswers {
                started.complete(Unit)
                release.await()
                Complete
            }
            every { repository.needsRecovery(any()) } returns false
            val synchronizer = synchronizer()
            status.emit(WifiStatus.Connected)
            started.await()
            val resume = async { synchronizer.recoverOnResume() }
            runCurrent()
            assertFalse(resume.isCompleted)
            coVerify(exactly = 1) { repository.refresh(false) }
            coVerify(exactly = 0) { repository.refresh(true) }
            release.complete(Unit)
            resume.await()
        }

    @Test fun `overlapping resume recoveries share an active missing-data retrieval`() =
        runTest {
            val synchronizer = synchronizer()
            status.emit(WifiStatus.Connected)
            runCurrent()
            every { repository.needsRecovery(any()) } returns true
            val started = CompletableDeferred<Unit>()
            val release = CompletableDeferred<Unit>()
            coEvery { repository.refresh(true) } coAnswers {
                started.complete(Unit)
                release.await()
                Complete
            }
            val first = async { synchronizer.recoverOnResume() }
            started.await()
            val second = async { synchronizer.recoverOnResume() }
            runCurrent()
            coVerify(exactly = 1) { repository.refresh(true) }
            assertFalse(first.isCompleted)
            assertFalse(second.isCompleted)
            release.complete(Unit)
            first.await()
            second.await()
        }

    @Test fun `active missing-data retrieval is replaced by an equivalent full egress request`() =
        runTest {
            val synchronizer = synchronizer()
            status.emit(WifiStatus.Connected)
            runCurrent()
            every { repository.needsRecovery(any()) } returns true
            val started = CompletableDeferred<Unit>()
            val cancelled = CompletableDeferred<Unit>()
            coEvery { repository.refresh(true) } coAnswers {
                started.complete(Unit)
                try {
                    awaitCancellation()
                } finally {
                    cancelled.complete(Unit)
                }
            }
            val recovery = async { synchronizer.recoverOnResume() }
            started.await()
            egress.emit(Unit)
            cancelled.await()
            recovery.await()
            coVerify(exactly = 2) { repository.refresh(false) }
            coVerify(exactly = 1) { repository.refresh(true) }
        }

    @Test fun `changed default identity replaces active HTTP without blocking event publication`() =
        runTest {
            assertActiveFullRefreshReplaced {
                defaultIdentity = 2L
                egress.emit(Unit)
            }
        }

    @Test fun `changed Wi-Fi identity replaces active HTTP`() =
        runTest {
            assertActiveFullRefreshReplaced {
                wifiIdentity = 2L
                status.emit(WifiStatus.Connected)
            }
        }

    @Test fun `changed direct Wi-Fi classification replaces active HTTP on the same network`() =
        runTest {
            assertActiveFullRefreshReplaced {
                isDirectWifiDefaultNetwork = false
                egress.emit(Unit)
            }
        }

    @Test fun `changed VPN classification replaces active HTTP on the same network`() =
        runTest {
            isDirectWifiDefaultNetwork = false
            assertActiveFullRefreshReplaced {
                isVpnDefaultNetwork = true
                egress.emit(Unit)
            }
        }

    @Test fun `changed default route usability replaces active HTTP on the same network`() =
        runTest {
            assertActiveFullRefreshReplaced {
                hasUsableDefaultNetwork = false
                egress.emit(Unit)
            }
        }

    @Test fun `changed requirements replace active full HTTP with missing-data recovery`() =
        runTest {
            val started = CompletableDeferred<Unit>()
            val cancelled = CompletableDeferred<Unit>()
            coEvery { repository.refresh(false) } coAnswers {
                started.complete(Unit)
                try {
                    awaitCancellation()
                } finally {
                    cancelled.complete(Unit)
                }
            }
            val synchronizer = synchronizer()
            status.emit(WifiStatus.Connected)
            started.await()
            config.value = config.value.withUpdatedPropertyEnablement(WifiProperty.PublicIp, false)
            cancelled.await()
            runCurrent()
            coVerify(exactly = 1) { repository.refresh(false) }
            coVerify(exactly = 1) { repository.refresh(true) }
            assertEquals(config.value.remoteDataConfig, synchronizer.inputs.replayCache.last().remoteDataConfig)
        }

    private suspend fun TestScope.assertActiveFullRefreshReplaced(changeContext: suspend () -> Unit) {
        val started = CompletableDeferred<Unit>()
        val cancelled = CompletableDeferred<Unit>()
        var calls = 0
        coEvery { repository.refresh() } coAnswers {
            if (++calls == 1) {
                started.complete(Unit)
                try {
                    awaitCancellation()
                } finally {
                    cancelled.complete(Unit)
                }
            }
            Complete
        }
        val synchronizer = synchronizer()
        synchronizer.inputs.test {
            this@RemoteDataSynchronizerTest.status.emit(WifiStatus.Connected)
            awaitItem()
            started.await()
            changeContext()
            assertEquals(WifiStatus.Connected, awaitItem().status)
            cancelled.await()
            coVerify(exactly = 2) { repository.refresh() }
        }
    }

    @Test fun `disconnection cancels HTTP and reconnection fetches again`() =
        runTest {
            val started = CompletableDeferred<Unit>()
            val cancelled = CompletableDeferred<Unit>()
            var calls = 0
            coEvery { repository.refresh() } coAnswers {
                if (++calls == 1) {
                    started.complete(Unit)
                    try {
                        awaitCancellation()
                    } finally {
                        cancelled.complete(Unit)
                    }
                }
                Complete
            }
            val synchronizer = synchronizer()
            synchronizer.inputs.test {
                this@RemoteDataSynchronizerTest.status.emit(WifiStatus.Connected)
                awaitItem()
                started.await()
                this@RemoteDataSynchronizerTest.status.emit(WifiStatus.NotConnected)
                assertEquals(WifiStatus.NotConnected, awaitItem().status)
                cancelled.await()
                this@RemoteDataSynchronizerTest.status.emit(WifiStatus.Connected)
                awaitItem()
                coVerify(exactly = 2) { repository.refresh() }
            }
        }

    @Test fun `default egress disconnection cancels pending HTTP`() =
        runTest {
            val started = CompletableDeferred<Unit>()
            val cancelled = CompletableDeferred<Unit>()
            coEvery { repository.refresh() } coAnswers {
                started.complete(Unit)
                try {
                    awaitCancellation()
                } finally {
                    cancelled.complete(Unit)
                }
            }
            val synchronizer = synchronizer()
            synchronizer.inputs.test {
                this@RemoteDataSynchronizerTest.status.emit(WifiStatus.Connected)
                awaitItem()
                started.await()
                snapshotStatus = WifiStatus.NotConnected
                egress.emit(Unit)
                assertEquals(WifiStatus.NotConnected, awaitItem().status)
                cancelled.await()
                coVerify(exactly = 1) { repository.refresh() }
            }
        }

    @Test fun `foreground validates cache and fetches only missing requirements`() =
        runTest {
            val synchronizer = synchronizer()
            status.emit(WifiStatus.Connected)
            runCurrent()
            every { repository.needsRecovery(any()) } returns false
            synchronizer.recoverOnResume()
            coVerify(exactly = 0) { repository.refresh(onlyIfMissing = true) }
            every { repository.needsRecovery(any()) } returns true
            synchronizer.recoverOnResume()
            coVerify(exactly = 1) { repository.refresh(onlyIfMissing = true) }
            snapshotStatus = WifiStatus.NotConnected
            synchronizer.recoverOnResume()
            coVerify(exactly = 1) { repository.refresh(onlyIfMissing = true) }
        }

    @Test fun `resume recovery remains available after disconnecting an owned recovery`() =
        runTest {
            every { repository.needsRecovery(any()) } returns true
            val entered = CompletableDeferred<Unit>()
            var recoveryCalls = 0
            coEvery { repository.refresh(true) } coAnswers {
                if (++recoveryCalls == 1) {
                    entered.complete(Unit)
                    awaitCancellation()
                }
                Complete
            }
            val synchronizer = synchronizer()
            status.emit(WifiStatus.Connected)
            runCurrent()
            val recovery = async { synchronizer.recoverOnResume() }
            entered.await()
            snapshotStatus = WifiStatus.NotConnected
            status.emit(WifiStatus.NotConnected)
            recovery.await()
            synchronizer.recoverOnResume()
            snapshotStatus = WifiStatus.Connected
            status.emit(WifiStatus.Connected)
            runCurrent()
            synchronizer.recoverOnResume()
            defaultIdentity = 2L
            egress.emit(Unit)
            runCurrent()
            config.value = config.value.withUpdatedPropertyEnablement(WifiProperty.PublicIp, false)
            runCurrent()
            coVerify(exactly = 3) { repository.refresh(true) }
            coVerify(exactly = 3) { repository.refresh(false) }
        }

    @Test fun `resume observed disconnection cannot suppress the next connected refresh`() =
        runTest {
            val synchronizer = synchronizer()
            status.emit(WifiStatus.Connected)
            runCurrent()
            snapshotStatus = WifiStatus.NotConnected
            synchronizer.recoverOnResume()
            snapshotStatus = WifiStatus.Connected
            status.emit(WifiStatus.Connected)
            runCurrent()
            coVerify(exactly = 2) { repository.refresh(false) }
            coVerify(exactly = 0) { repository.refresh(true) }
        }

    @Test fun `repository reconciliation precedes each egress publication`() =
        runTest {
            var snapshots = 0
            every { repository.reconciledSnapshot(any()) } answers {
                snapshots++
                if (snapshots == 2) data.value = RemoteNetworkData.empty
                data.value
            }
            val synchronizer = synchronizer()
            synchronizer.inputs.test {
                this@RemoteDataSynchronizerTest.status.emit(WifiStatus.Connected)
                awaitItem()
                assertEquals(1, snapshots)
                data.value = RemoteNetworkData(IpApiData(null, null, "UTC", "old ISP", null), emptyList())
                egress.emit(Unit)
                awaitItem()
                assertEquals(2, snapshots)
                assertEquals(RemoteNetworkData.empty, data.value)
            }
        }
}
