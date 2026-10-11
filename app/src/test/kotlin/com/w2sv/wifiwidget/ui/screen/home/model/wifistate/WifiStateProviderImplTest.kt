package com.w2sv.wifiwidget.ui.screen.home.model.wifistate

import app.cash.turbine.test
import com.w2sv.domain.model.networking.IpApiData
import com.w2sv.domain.model.networking.RemoteWifiData
import com.w2sv.domain.model.networking.WifiStatus
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.model.wifiproperty.WifiProperty
import com.w2sv.domain.model.wifiproperty.viewdata.SubscriptableText
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewData
import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewDataProvider
import com.w2sv.domain.repository.RemoteWifiDataRepository
import com.w2sv.domain.repository.WidgetConfigSource
import com.w2sv.networking.remotenetworkinfo.ReconciledWifiInputs
import com.w2sv.networking.remotenetworkinfo.RemoteDataSynchronizer
import com.w2sv.wifiwidget.ui.screen.home.model.gpsstatus.LocationEnabledProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
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
import org.junit.Assert.assertNotEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WifiStateProviderImplTest {
    private val inputs = MutableSharedFlow<ReconciledWifiInputs>(replay = 1)
    private val remoteData = MutableStateFlow(RemoteWifiData.empty)
    private val config = MutableStateFlow(WidgetConfig.default)
    private val locationEnabled = MutableStateFlow(true)
    private val synchronizer = mockk<RemoteDataSynchronizer> { every { inputs } returns this@WifiStateProviderImplTest.inputs }
    private val repository = mockk<RemoteWifiDataRepository> { every { data } returns remoteData }
    private val configSource = mockk<WidgetConfigSource> { every { flow } returns config }
    private val propertyProvider = mockk<WifiPropertyViewDataProvider>(relaxed = true)
    private val locationProvider = mockk<LocationEnabledProvider> { every { isEnabled } returns locationEnabled }

    private fun TestScope.provider() =
        WifiStateProviderImpl(
            synchronizer,
            configSource,
            propertyProvider,
            repository,
            locationProvider,
            CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler))
        )

    @Test fun `property reorder updates presentation without synchronized inputs`() =
        runTest {
            coEvery { propertyProvider(any(), any(), any()) } coAnswers {
                firstArg<List<WifiProperty>>().map { property ->
                    WifiPropertyViewData(SubscriptableText(property.toString()), property.toString())
                }
            }
            val provider = provider()
            provider.wifiState.test {
                awaitItem()
                inputs.emit(ReconciledWifiInputs(Connected, config.value.remoteDataConfig))
                assertEquals(
                    config.value.enabledProperties.map(WifiProperty::toString),
                    awaitItem().asConnectedOrNull!!.propertyViewData.map {
                        it.value
                    }
                )
                val enabled = config.value.enabledProperties
                config.value = config.value.withUpdatedPropertyPosition(
                    config.value.propertyOrder.indexOf(enabled.first()),
                    config.value.propertyOrder.indexOf(enabled.last())
                )
                assertEquals(
                    config.value.enabledProperties.map(WifiProperty::toString),
                    awaitItem().asConnectedOrNull!!.propertyViewData.map {
                        it.value
                    }
                )
            }
        }

    @Test fun `local property change updates presentation without synchronized inputs`() =
        runTest {
            coEvery { propertyProvider(any(), any(), any()) } coAnswers {
                listOf(WifiPropertyViewData(SubscriptableText("count"), firstArg<List<WifiProperty>>().size.toString()))
            }
            val provider = provider()
            provider.wifiState.test {
                awaitItem()
                inputs.emit(ReconciledWifiInputs(Connected, config.value.remoteDataConfig))
                awaitItem()
                config.value = config.value.withUpdatedPropertyEnablement(WifiProperty.SSID, true)
                assertEquals(
                    config.value.enabledProperties.size.toString(),
                    awaitItem().asConnectedOrNull!!.propertyViewData.single().value
                )
            }
        }

    @Test fun `remote requirements wait for reconciliation even when data changes`() =
        runTest {
            coEvery { propertyProvider(any(), any(), any()) } coAnswers {
                listOf(WifiPropertyViewData(SubscriptableText("order"), firstArg<List<WifiProperty>>().joinToString()))
            }
            val provider = provider()
            provider.wifiState.test {
                awaitItem()
                inputs.emit(ReconciledWifiInputs(Connected, config.value.remoteDataConfig))
                awaitItem()
                val changed = config.value.withUpdatedPropertyEnablement(WifiProperty.PublicIp, false)
                config.value = changed
                remoteData.value = RemoteWifiData(IpApiData(null, null, "UTC", "ISP", null), emptyList())
                expectNoEvents()
                val enabled = changed.enabledProperties
                config.value = changed.withUpdatedPropertyPosition(
                    changed.propertyOrder.indexOf(enabled.first()),
                    changed.propertyOrder.indexOf(enabled.last())
                )
                assertNotEquals(enabled, config.value.enabledProperties)
                expectNoEvents()
                inputs.emit(ReconciledWifiInputs(Connected, changed.remoteDataConfig))
                assertEquals(config.value.enabledProperties.joinToString(), awaitItem().asConnectedOrNull!!.propertyViewData.single().value)
            }
        }

    @Test fun `connected disconnected and disabled inputs produce home states`() =
        runTest {
            val provider = provider()
            provider.wifiState.test {
                assertEquals(WifiState.Disconnected, awaitItem())
                inputs.emit(ReconciledWifiInputs(Disabled, WidgetConfig.default.remoteDataConfig))
                assertEquals(WifiState.Disabled, awaitItem())
                inputs.emit(ReconciledWifiInputs(Connected, WidgetConfig.default.remoteDataConfig))
                assertEquals(WifiStatus.Connected, awaitItem().status)
                inputs.emit(ReconciledWifiInputs(ConnectedInactive, WidgetConfig.default.remoteDataConfig))
                assertEquals(WifiStatus.ConnectedInactive, awaitItem().status)
                inputs.emit(ReconciledWifiInputs(NotConnected, WidgetConfig.default.remoteDataConfig))
                assertEquals(WifiState.Disconnected, awaitItem())
            }
        }

    @Test fun `remote data and configuration recompute presentation`() =
        runTest {
            coEvery { propertyProvider(any(), any(), any()) } coAnswers {
                listOf(
                    WifiPropertyViewData(
                        SubscriptableText("value"),
                        thirdArg<RemoteWifiData>().ipApiData?.isp.orEmpty() + firstArg<List<WifiProperty>>().size
                    )
                )
            }
            val provider = provider()
            provider.wifiState.test {
                awaitItem()
                inputs.emit(ReconciledWifiInputs(Connected, WidgetConfig.default.remoteDataConfig))
                val initial = awaitItem().asConnectedOrNull!!.propertyViewData.single().value
                remoteData.value = RemoteWifiData(IpApiData(null, null, "UTC", "ISP", null), emptyList())
                assertEquals(
                    "ISP${WidgetConfig.default.enabledProperties.size}",
                    awaitItem().asConnectedOrNull!!.propertyViewData.single().value
                )
                val changed = WidgetConfig.default.withUpdatedPropertyEnablement(WifiProperty.SSID, true)
                config.value = changed
                inputs.emit(ReconciledWifiInputs(Connected, changed.remoteDataConfig))
                assertEquals("ISP${changed.enabledProperties.size}", awaitItem().asConnectedOrNull!!.propertyViewData.single().value)
                assertEquals(true, initial.isNotEmpty())
            }
        }

    @Test fun `location access recomputes only when dependent properties are enabled`() =
        runTest {
            coEvery { propertyProvider(any(), any(), any()) } coAnswers {
                listOf(WifiPropertyViewData(SubscriptableText("location"), if (locationEnabled.value) "available" else "unavailable"))
            }
            val provider = provider()
            provider.wifiState.test {
                awaitItem()
                inputs.emit(ReconciledWifiInputs(Connected, WidgetConfig.default.remoteDataConfig))
                assertEquals("available", awaitItem().asConnectedOrNull!!.propertyViewData.single().value)
                locationEnabled.value = false
                val locationConfig = WidgetConfig.default.withUpdatedPropertyEnablement(WifiProperty.SSID, true)
                config.value = locationConfig
                inputs.emit(ReconciledWifiInputs(Connected, locationConfig.remoteDataConfig))
                assertEquals("unavailable", awaitItem().asConnectedOrNull!!.propertyViewData.single().value)
                locationEnabled.value = true
                assertEquals("available", awaitItem().asConnectedOrNull!!.propertyViewData.single().value)
            }
        }

    @Test fun `repeated connected inputs recompute local values`() =
        runTest {
            var ssid = "first"
            coEvery { propertyProvider(any(), any(), any()) } coAnswers {
                listOf(WifiPropertyViewData(SubscriptableText("SSID"), ssid))
            }
            val provider = provider()
            provider.wifiState.test {
                awaitItem()
                inputs.emit(ReconciledWifiInputs(Connected, WidgetConfig.default.remoteDataConfig))
                assertEquals("first", awaitItem().asConnectedOrNull!!.propertyViewData.single().value)
                ssid = "second"
                inputs.emit(ReconciledWifiInputs(Connected, WidgetConfig.default.remoteDataConfig))
                assertEquals("second", awaitItem().asConnectedOrNull!!.propertyViewData.single().value)
            }
        }

    @Test fun `obsolete suspending computation cannot publish stale state`() =
        runTest {
            val started = CompletableDeferred<Unit>()
            val cancelled = CompletableDeferred<Unit>()
            var calls = 0
            coEvery { propertyProvider(any(), any(), any()) } coAnswers {
                if (++calls == 1) {
                    started.complete(Unit)
                    try {
                        awaitCancellation()
                    } finally {
                        cancelled.complete(Unit)
                    }
                }
                listOf(WifiPropertyViewData(SubscriptableText("SSID"), "latest"))
            }
            val provider = provider()
            provider.wifiState.test {
                awaitItem()
                inputs.emit(ReconciledWifiInputs(Connected, WidgetConfig.default.remoteDataConfig))
                started.await()
                inputs.emit(ReconciledWifiInputs(ConnectedInactive, WidgetConfig.default.remoteDataConfig))
                cancelled.await()
                assertEquals("latest", awaitItem().asConnectedOrNull!!.propertyViewData.single().value)
                assertEquals(WifiStatus.ConnectedInactive, provider.wifiState.value.status)
            }
        }
}
