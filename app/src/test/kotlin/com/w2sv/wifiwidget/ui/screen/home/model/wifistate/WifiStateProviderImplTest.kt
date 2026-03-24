package com.w2sv.wifiwidget.ui.screen.home.model.wifistate

import app.cash.turbine.test
import com.w2sv.domain.model.networking.RemoteNetworkInfo
import com.w2sv.domain.model.networking.WifiStatus
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.model.wifiproperty.WifiProperty
import com.w2sv.domain.model.wifiproperty.settings.IpSetting
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewDataProvider
import com.w2sv.domain.repository.RemoteNetworkInfoRepository
import com.w2sv.kotlinutils.copy
import com.w2sv.kotlinutils.update
import com.w2sv.networking.wifistatus.monitor.WifiStatusMonitor
import com.w2sv.wifiwidget.ui.screen.home.model.gpsstatus.LocationEnabledProvider
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
    private val widgetConfigFlow = MutableStateFlow(WidgetConfig.default)
    private val remoteNetworkFlow = MutableStateFlow(RemoteNetworkInfo.empty)
    private val gpsIsEnabledFlow = MutableStateFlow(true)
    private val wifiStatusMonitor = mockk<WifiStatusMonitor> {
        every { wifiStatus } returns wifiStatusFlow
    }
    private val wifiPropertyViewDataProvider = mockk<WifiPropertyViewDataProvider>(relaxed = true)
    private val remoteNetworkInfoRepository = mockk<RemoteNetworkInfoRepository>(relaxed = true) {
        every { data } returns remoteNetworkFlow
    }
    private val gpsStateProvider = mockk<LocationEnabledProvider> {
        every { isEnabled } returns gpsIsEnabledFlow
    }

    private val classUnderTest = WifiStateProviderImpl(
        wifiStatusMonitor = wifiStatusMonitor,
        widgetConfigFlow = widgetConfigFlow,
        wifiPropertyViewDataProvider = wifiPropertyViewDataProvider,
        remoteNetworkInfoRepository = remoteNetworkInfoRepository,
        locationEnabledProvider = gpsStateProvider,
        scope = CoroutineScope(UnconfinedTestDispatcher())
    )

    private suspend fun emitWifiStatus(status: WifiStatus) {
        wifiStatusFlow.emit(status)
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
    fun `connectedWifiState recomputes on locationAccessChanged`() =
        runTest {
            classUnderTest.wifiState.test {
                // Recomputes on Connected
                emitWifiStatus(WifiStatus.Connected)
                coVerify(exactly = 1) { wifiPropertyViewDataProvider(any(), any(), any()) }

                // Recomputes on re-emission of Connected
                emitWifiStatus(WifiStatus.Connected)
                coVerify(exactly = 3) { wifiPropertyViewDataProvider(any(), any(), any()) }

                // Does not recompute on location access changed while no location dependent property enabled
                gpsIsEnabledFlow.value = false
                coVerify(exactly = 3) { wifiPropertyViewDataProvider(any(), any(), any()) }

                // Recomputes on property enablement change
                widgetConfigFlow.update { it.withUpdatedPropertyEnablement(WifiProperty.SSID, true) }
                coVerify(exactly = 4) { wifiPropertyViewDataProvider(any(), any(), any()) }

                // Does recompute on location access changed while location dependent property enabled
                gpsIsEnabledFlow.value = true
                coVerify(exactly = 5) { wifiPropertyViewDataProvider(any(), any(), any()) }

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `RemoteNetworkInfoRepository refreshes on relevant changes`() =
        runTest {
            // No refreshing on WifiStatus.Disconnected
            coVerify(exactly = 0) { remoteNetworkInfoRepository.refresh() }

            // No refreshing on property change during WifiStatus.Disconnected
            widgetConfigFlow.update { it.withUpdatedPropertyEnablement(WifiProperty.SSID, true) }
            coVerify(exactly = 0) { remoteNetworkInfoRepository.refresh() }

            // Refreshing on WifiStatus.Connected
            emitWifiStatus(WifiStatus.Connected)
            coVerify(exactly = 1) { remoteNetworkInfoRepository.refresh() }

            // Refreshing on property change during WifiStatus.Connected
            widgetConfigFlow.update { it.withUpdatedPropertyEnablement(WifiProperty.SSID, false) }
            coVerify(exactly = 2) { remoteNetworkInfoRepository.refresh() }

            // No refreshing on irrelevant config change
            widgetConfigFlow.update { it.copy(refreshing = it.refreshing.copy(refreshPeriodically = false)) }
            coVerify(exactly = 2) { remoteNetworkInfoRepository.refresh() }

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
            coVerify(exactly = 3) { remoteNetworkInfoRepository.refresh() }
        }
}
