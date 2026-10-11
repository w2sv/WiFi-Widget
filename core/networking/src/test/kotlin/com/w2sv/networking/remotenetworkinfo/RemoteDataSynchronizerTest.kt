package com.w2sv.networking.remotenetworkinfo

import app.cash.turbine.test
import com.w2sv.domain.model.networking.IpApiData
import com.w2sv.domain.model.networking.RemoteRefreshResult.Complete
import com.w2sv.domain.model.networking.RemoteWifiData
import com.w2sv.domain.model.networking.WifiStatus
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.model.wifiproperty.WifiProperty
import com.w2sv.domain.repository.RemoteWifiDataRepository
import com.w2sv.domain.repository.WidgetConfigSource
import com.w2sv.networking.wifistatus.WifiStatusSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RemoteDataSynchronizerTest {
    private val status = MutableSharedFlow<WifiStatus>()
    private val egress = MutableSharedFlow<Unit>()
    private val config = MutableStateFlow(WidgetConfig.default)
    private val data = MutableStateFlow(RemoteWifiData.empty)
    private var snapshotStatus: WifiStatus = WifiStatus.Connected
    private val wifiStatusSource = mockk<WifiStatusSource> {
        every { this@mockk.status } returns this@RemoteDataSynchronizerTest.status
        every { defaultEgressChanges } returns egress
        every { snapshot() } answers { snapshotStatus }
    }
    private val source = mockk<WidgetConfigSource> {
        every { flow } returns config
        coEvery { snapshot() } answers { config.value }
    }
    private val repository = mockk<RemoteWifiDataRepository>(relaxed = true) {
        every { this@mockk.data } returns this@RemoteDataSynchronizerTest.data
        every { reconciledSnapshot(any()) } answers { data.value }
    }
    private fun TestScope.synchronizer() =
        RemoteDataSynchronizer(
            wifiStatusSource,
            source,
            repository,
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
                coVerify(exactly = 2) { repository.refresh() }
                val firstEnabled = config.value.enabledProperties.first()
                val lastEnabled = config.value.enabledProperties.last()
                val snapshotsBeforeReorder = snapshots
                config.value = config.value.withUpdatedPropertyPosition(
                    config.value.propertyOrder.indexOf(firstEnabled),
                    config.value.propertyOrder.indexOf(lastEnabled)
                )
                expectNoEvents()
                assertEquals(snapshotsBeforeReorder, snapshots)
                coVerify(exactly = 2) { repository.refresh() }
                config.value = config.value.copy(refreshing = config.value.refreshing.copy(refreshPeriodically = false))
                expectNoEvents()
                coVerify(exactly = 2) { repository.refresh() }
            }
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
                coVerify(exactly = 4) { repository.refresh() }
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

    @Test fun `egress replaces active HTTP without blocking event publication`() =
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
                egress.emit(Unit)
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
            every { repository.needsRecovery(any()) } returns false
            synchronizer.recoverOnForeground()
            coVerify(exactly = 0) { repository.refresh(onlyIfMissing = true) }
            every { repository.needsRecovery(any()) } returns true
            synchronizer.recoverOnForeground()
            coVerify(exactly = 1) { repository.refresh(onlyIfMissing = true) }
            snapshotStatus = WifiStatus.NotConnected
            synchronizer.recoverOnForeground()
            coVerify(exactly = 1) { repository.refresh(onlyIfMissing = true) }
        }

    @Test fun `repository reconciliation precedes each egress publication`() =
        runTest {
            var snapshots = 0
            every { repository.reconciledSnapshot(any()) } answers {
                snapshots++
                if (snapshots == 2) data.value = RemoteWifiData.empty
                data.value
            }
            val synchronizer = synchronizer()
            synchronizer.inputs.test {
                this@RemoteDataSynchronizerTest.status.emit(WifiStatus.Connected)
                awaitItem()
                assertEquals(1, snapshots)
                data.value = RemoteWifiData(IpApiData(null, null, "UTC", "old ISP", null), emptyList())
                egress.emit(Unit)
                awaitItem()
                assertEquals(2, snapshots)
                assertEquals(RemoteWifiData.empty, data.value)
            }
        }
}
