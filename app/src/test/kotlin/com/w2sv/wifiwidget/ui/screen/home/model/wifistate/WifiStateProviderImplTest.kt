package com.w2sv.wifiwidget.ui.screen.home.model.wifistate

import app.cash.turbine.test
import com.w2sv.domain.model.networking.IpApiData
import com.w2sv.domain.model.networking.RemoteWifiData
import com.w2sv.domain.model.networking.WifiStatus
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.model.wifiproperty.WifiProperty
import com.w2sv.domain.model.wifiproperty.settings.IpSetting
import com.w2sv.domain.model.wifiproperty.viewdata.SubscriptableText
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewData
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewDataProvider
import com.w2sv.domain.repository.RemoteWifiDataRepository
import com.w2sv.kotlinutils.copy
import com.w2sv.kotlinutils.update
import com.w2sv.networking.wifistatus.monitor.WifiStatusMonitor
import com.w2sv.networking.wifistatus.provider.WifiStatusProvider
import com.w2sv.widget.actions.WidgetActions
import com.w2sv.wifiwidget.ui.screen.home.model.gpsstatus.LocationEnabledProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals

@ExperimentalCoroutinesApi
class WifiStateProviderImplTest {

    private val wifiStatusFlow = MutableSharedFlow<WifiStatus>().apply { tryEmit(WifiStatus.NotConnected) }
    private val egressChanges = MutableSharedFlow<Unit>()
    private val widgetConfigFlow = MutableStateFlow(WidgetConfig.default)
    private val remoteNetworkFlow = MutableStateFlow(RemoteWifiData.empty)
    private val gpsIsEnabledFlow = MutableStateFlow(true)
    private val wifiStatusMonitor = mockk<WifiStatusMonitor> {
        every { wifiStatus } returns wifiStatusFlow
        every { defaultEgressChanges } returns egressChanges
    }
    private val wifiPropertyViewDataProvider = mockk<WifiPropertyViewDataProvider>(relaxed = true)
    private val remoteWifiDataRepository = mockk<RemoteWifiDataRepository>(relaxed = true) {
        every { data } returns remoteNetworkFlow
        every { snapshot(any()) } answers { remoteNetworkFlow.value }
    }
    private val gpsStateProvider = mockk<LocationEnabledProvider> {
        every { isEnabled } returns gpsIsEnabledFlow
    }
    private val widgetActions = mockk<WidgetActions>(relaxed = true)
    private val wifiStatusProvider = mockk<WifiStatusProvider>().also {
        every { it.invoke() } returns WifiStatus.Connected
    }

    private val classUnderTest = WifiStateProviderImpl(
        wifiStatusMonitor = wifiStatusMonitor,
        widgetConfigFlow = widgetConfigFlow,
        wifiPropertyViewDataProvider = wifiPropertyViewDataProvider,
        remoteWifiDataRepository = remoteWifiDataRepository,
        widgetActions = widgetActions,
        wifiStatusProvider = wifiStatusProvider,
        locationEnabledProvider = gpsStateProvider,
        scope = CoroutineScope(UnconfinedTestDispatcher())
    )

    private suspend fun emitWifiStatus(status: WifiStatus) {
        wifiStatusFlow.emit(status)
    }

    @Test
    fun `foreground validation renders restored remote data without fetching`() =
        runTest {
            val restored = RemoteWifiData(IpApiData(null, null, "UTC", "ISP", null), emptyList())
            every { remoteWifiDataRepository.needsRecovery(any()) } answers {
                remoteNetworkFlow.value = restored
                false
            }

            classUnderTest.onForeground()

            coVerify(exactly = 0) { remoteWifiDataRepository.refresh(onlyIfMissing = true) }
            verify(exactly = 1) { widgetActions.render() }
        }

    @Test
    fun `emitted wifiState corresponds to received wifiStatus types`() =
        runTest {
            classUnderTest.wifiState.test {
                assertEquals(WifiState.Disconnected, awaitItem())

                emitWifiStatus(WifiStatus.Disabled)
                assertEquals(WifiState.Disabled, awaitItem())

                emitWifiStatus(WifiStatus.Connected)
                assertEquals(WifiState.Connected(WifiStatus.Connected, emptyList()), awaitItem())

                emitWifiStatus(WifiStatus.ConnectedInactive)
                assertEquals(WifiState.Connected(WifiStatus.ConnectedInactive, emptyList()), awaitItem())
            }
        }

    @Test
    fun `location access changes update local properties only when needed`() =
        runTest {
            coEvery { wifiPropertyViewDataProvider(any(), any(), any()) } coAnswers {
                listOf(WifiPropertyViewData(SubscriptableText("location"), if (gpsIsEnabledFlow.value) "available" else "unavailable"))
            }
            classUnderTest.wifiState.test {
                assertEquals(WifiState.Disconnected, awaitItem())
                emitWifiStatus(WifiStatus.Connected)
                assertEquals("available", awaitItem().asConnectedOrNull?.propertyViewData?.single()?.value)
                gpsIsEnabledFlow.value = false
                assertEquals("available", classUnderTest.wifiState.value.asConnectedOrNull?.propertyViewData?.single()?.value)
                widgetConfigFlow.update { it.withUpdatedPropertyEnablement(WifiProperty.SSID, true) }
                assertEquals("unavailable", awaitItem().asConnectedOrNull?.propertyViewData?.single()?.value)
                gpsIsEnabledFlow.value = true
                assertEquals("available", awaitItem().asConnectedOrNull?.propertyViewData?.single()?.value)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `RemoteNetworkInfoRepository refreshes on relevant changes`() =
        runTest {
            // No refreshing on WifiStatus.Disconnected
            coVerify(exactly = 0) { remoteWifiDataRepository.refresh() }

            // No refreshing on property change during WifiStatus.Disconnected
            widgetConfigFlow.update { it.withUpdatedPropertyEnablement(WifiProperty.SSID, true) }
            coVerify(exactly = 0) { remoteWifiDataRepository.refresh() }

            // Refreshing on WifiStatus.Connected
            emitWifiStatus(WifiStatus.Connected)
            coVerify(exactly = 1) { remoteWifiDataRepository.refresh() }

            // Refreshing on property change during WifiStatus.Connected
            widgetConfigFlow.update { it.withUpdatedPropertyEnablement(WifiProperty.SSID, false) }
            coVerify(exactly = 2) { remoteWifiDataRepository.refresh() }

            // No refreshing on irrelevant config change
            widgetConfigFlow.update { it.copy(refreshing = it.refreshing.copy(refreshPeriodically = false)) }
            coVerify(exactly = 2) { remoteWifiDataRepository.refresh() }

            // Refreshing on IP setting change
            widgetConfigFlow.update { config ->
                config.copy(
                    propertyConfigMap = config.propertyConfigMap.copy {
                        update(WifiProperty.PublicIp) {
                            it.copy(settings = it.settings.copy { put(IpSetting.V4Enabled, false) })
                        }
                    }
                )
            }
            coVerify(exactly = 3) { remoteWifiDataRepository.refresh() }
        }

    @Test
    fun `same Wi-Fi status with a new default route refreshes and recomputes home state`() =
        runTest {
            classUnderTest.wifiState.test {
                emitWifiStatus(WifiStatus.Connected)
                awaitItem()
                coVerify(exactly = 1) { remoteWifiDataRepository.refresh() }
                egressChanges.emit(Unit)
                coVerify(exactly = 2) { remoteWifiDataRepository.refresh() }
                coVerify(atLeast = 2) { wifiPropertyViewDataProvider(any(), any(), any()) }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `default egress change while Wi-Fi is disconnected does not fetch remote data`() =
        runTest {
            classUnderTest.wifiState.test {
                assertEquals(WifiState.Disconnected, awaitItem())
                emitWifiStatus(WifiStatus.ConnectedInactive)
                assertEquals(WifiStatus.ConnectedInactive, awaitItem().status)
                coVerify(exactly = 1) { remoteWifiDataRepository.refresh() }
                emitWifiStatus(WifiStatus.NotConnected)
                assertEquals(WifiState.Disconnected, awaitItem())
                egressChanges.emit(Unit)
                coVerify(exactly = 1) { remoteWifiDataRepository.refresh() }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `initial Wi-Fi callback sequence refreshes remote data once`() =
        runTest {
            classUnderTest.wifiState.test {
                assertEquals(1, wifiStatusFlow.subscriptionCount.value)
                emitWifiStatus(WifiStatus.Connected) // initial evaluation
                awaitItem()
                emitWifiStatus(WifiStatus.Connected) // onAvailable
                emitWifiStatus(WifiStatus.Connected) // onCapabilitiesChanged
                emitWifiStatus(WifiStatus.Connected) // onLinkPropertiesChanged
                coVerify(exactly = 1) { remoteWifiDataRepository.refresh() }
                egressChanges.emit(Unit)
                coVerify(exactly = 2) { remoteWifiDataRepository.refresh() }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `reconnection with unchanged cellular egress refreshes remote data again`() =
        runTest {
            classUnderTest.wifiState.test {
                assertEquals(WifiState.Disconnected, awaitItem())
                emitWifiStatus(WifiStatus.ConnectedInactive)
                assertEquals(WifiStatus.ConnectedInactive, awaitItem().status)
                coVerify(exactly = 1) { remoteWifiDataRepository.refresh() }
                emitWifiStatus(WifiStatus.NotConnected)
                assertEquals(WifiState.Disconnected, awaitItem())
                emitWifiStatus(WifiStatus.ConnectedInactive)
                coVerify(exactly = 2) { remoteWifiDataRepository.refresh() }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `local property value updates on repeated connected callbacks`() =
        runTest {
            var ssid = "first"
            coEvery { wifiPropertyViewDataProvider(any(), any(), any()) } coAnswers {
                listOf(WifiPropertyViewData(SubscriptableText("SSID"), ssid))
            }
            classUnderTest.wifiState.test {
                assertEquals(WifiState.Disconnected, awaitItem())
                emitWifiStatus(WifiStatus.Connected)
                assertEquals("first", awaitItem().asConnectedOrNull?.propertyViewData?.single()?.value)
                coVerify(exactly = 1) { remoteWifiDataRepository.refresh() }
                ssid = "second"
                emitWifiStatus(WifiStatus.Connected)
                assertEquals("second", awaitItem().asConnectedOrNull?.propertyViewData?.single()?.value)
                coVerify(exactly = 1) { remoteWifiDataRepository.refresh() }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `successful home refresh updates the launcher widget`() =
        runTest {
            coEvery { remoteWifiDataRepository.refresh() } answers {
                remoteNetworkFlow.value = RemoteWifiData(IpApiData(null, null, "UTC", "new ISP", null), emptyList())
                Complete
            }
            emitWifiStatus(WifiStatus.Connected)
            verify(exactly = 1) { widgetActions.render() }
        }

    @Test
    fun `route invalidation updates the launcher before a successful fetch`() =
        runTest {
            remoteNetworkFlow.value = RemoteWifiData(IpApiData(null, null, "UTC", "old ISP", null), emptyList())
            every { remoteWifiDataRepository.snapshot(any()) } answers {
                remoteNetworkFlow.value = RemoteWifiData.empty
                RemoteWifiData.empty
            }
            classUnderTest.wifiState.test {
                emitWifiStatus(WifiStatus.Connected)
                awaitItem()
                verify(exactly = 1) { widgetActions.render() }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `foreground resume attempts missing recovery despite pending worker backoff`() =
        runTest {
            every { remoteWifiDataRepository.needsRecovery(any()) } returns true
            coEvery { remoteWifiDataRepository.refresh(onlyIfMissing = true) } returns
                Incomplete
            classUnderTest.onForeground()
            coVerify(exactly = 1) { remoteWifiDataRepository.refresh(onlyIfMissing = true) }
        }
}
