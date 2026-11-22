package com.w2sv.widget.ui.state

import android.Manifest
import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.w2sv.core.resources.R
import com.w2sv.domain.networking.NetworkAvailabilitySnapshot
import com.w2sv.domain.networking.NetworkAvailabilitySource
import com.w2sv.domain.networking.RemoteNetworkData
import com.w2sv.domain.networking.RemoteNetworkDataRepository
import com.w2sv.domain.networking.WifiStatus
import com.w2sv.domain.networking.WifiStatusSource
import com.w2sv.domain.widget.FontSize
import com.w2sv.domain.widget.WidgetColoring
import com.w2sv.domain.widget.WidgetColoringStrategy
import com.w2sv.domain.widget.WidgetColors
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.domain.widget.WidgetUtility
import com.w2sv.domain.widget.WifiPropertyValueAlignment
import com.w2sv.domain.wifiproperty.ResolvedWifiProperty
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.WifiPropertyResolutionError
import com.w2sv.domain.wifiproperty.WifiPropertyResolver
import com.w2sv.domain.wifiproperty.WifiPropertyValue
import com.w2sv.domain.wifiproperty.presentation.WifiPropertyPresentationProvider
import com.w2sv.domain.wifiproperty.txt
import com.w2sv.widget.preview.widgetPreviewState
import com.w2sv.widget.rendering.WidgetRenderCoordinator
import com.w2sv.widget.ui.actions.WidgetButtonAction
import com.w2sv.widget.ui.theme.toRenderColors
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WidgetPresentationStateProviderTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val configSource = mockk<WidgetConfigSource>()
    private val statusSource = mockk<WifiStatusSource>()
    private val remoteData = mockk<RemoteNetworkDataRepository>()
    private val renderCoordinator = mockk<WidgetRenderCoordinator>()
    private val revisions = MutableStateFlow(0L)
    private val snapshot = RemoteNetworkData.empty
    private val availability = NetworkAvailabilitySource { NetworkAvailabilitySnapshot(false, true, true, true) }
    private val clock = Clock.fixed(Instant.parse("2026-08-16T10:30:00Z"), ZoneOffset.UTC)
    private var resolvedProperties = emptyList<WifiProperty>()
    private val resolver = WifiPropertyResolver { enabled, _, remote ->
        assertEquals(snapshot, remote)
        resolvedProperties = enabled
        enabled.map { ResolvedWifiProperty(it, listOf(WifiPropertyValue("value".txt))) }
    }
    private val presentations = WifiPropertyPresentationProvider(resolver, context.resources)
    private val colors = WidgetColors(0xFF112233.toInt(), 0xFF445566.toInt(), 0xFF778899.toInt())
    private val config = WidgetConfig.default.copy(
        appearance = WidgetConfig.default.appearance.copy(
            coloring = WidgetColoring(custom = WidgetColoringStrategy.Custom(colors), useCustom = true),
            backgroundOpacity = 0.5f,
            fontSize = FontSize.Large,
            propertyValueAlignment = WifiPropertyValueAlignment.Left
        ),
        utilities = WidgetUtility.entries.associateWith { it != WidgetUtility.GoToWifiSettingsButton }
    )

    private fun provider(presentations: WifiPropertyPresentationProvider = this.presentations) =
        WidgetPresentationStateProvider(
            context,
            configSource,
            statusSource,
            remoteData,
            availability,
            presentations,
            renderCoordinator,
            clock
        )

    @Test fun `unresolved identifiers distinguish foreground-only access from background access`() =
        runTest {
            shadowOf(
                context as Application
            ).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            val unresolved =
                WifiPropertyValue(R.string.no_location_access.txt, resolutionError = WifiPropertyResolutionError.NoLocationAccessPermission)

            val rows = locationRows(unresolved)

            assertEquals(List(2) { "No background location access" }, rows.take(2).map { it.presentation.value })
            assertEquals(
                List(2) { WifiPropertyResolutionError.NoBackgroundLocationAccessPermission },
                rows.take(2).map { it.presentation.resolutionError }
            )
            assertEquals("No location access", rows.last().presentation.value)
            assertEquals(WifiPropertyResolutionError.NoLocationAccessPermission, rows.last().presentation.resolutionError)

            shadowOf(context).grantPermissions(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            assertEquals(List(2) { "No location access" }, locationRows(unresolved).take(2).map { it.presentation.value })
        }

    @Test fun `missing precise access and disabled services keep their existing errors`() =
        runTest {
            shadowOf(context as Application).grantPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
            val unresolved =
                WifiPropertyValue(R.string.no_location_access.txt, resolutionError = WifiPropertyResolutionError.NoLocationAccessPermission)
            val withoutPreciseAccess = locationRows(unresolved).take(2)
            assertEquals(List(2) { "No location access" }, withoutPreciseAccess.map { it.presentation.value })
            assertEquals(
                List(2) {
                    WifiPropertyResolutionError.NoLocationAccessPermission
                },
                withoutPreciseAccess.map { it.presentation.resolutionError }
            )

            shadowOf(context).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
            val disabled = locationRows(
                WifiPropertyValue(R.string.gps_disabled.txt, resolutionError = WifiPropertyResolutionError.GpsDisabled)
            ).take(2)
            assertEquals(List(2) { "Location services turned off" }, disabled.map { it.presentation.value })
            assertEquals(List(2) { WifiPropertyResolutionError.GpsDisabled }, disabled.map { it.presentation.resolutionError })
        }

    @Test
    @Config(sdk = [28])
    fun `Android without separate background permission retains the generic unresolved error`() =
        runTest {
            shadowOf(
                context as Application
            ).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

            val rows = locationRows(
                WifiPropertyValue(R.string.no_location_access.txt, resolutionError = WifiPropertyResolutionError.NoLocationAccessPermission)
            ).take(2)

            assertEquals(List(2) { "No location access" }, rows.map { it.presentation.value })
            assertEquals(List(2) { WifiPropertyResolutionError.NoLocationAccessPermission }, rows.map { it.presentation.resolutionError })
        }

    @Test fun `resolved values remain available without background access`() =
        runTest {
            shadowOf(
                context as Application
            ).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

            val rows = locationRows(WifiPropertyValue("resolved value".txt))

            assertEquals(List(3) { "resolved value" }, rows.map { it.presentation.value })
            assertTrue(rows.all { it.presentation.resolutionError == null })
        }

    private suspend fun locationRows(value: WifiPropertyValue): List<WidgetPropertyRow> {
        val locationConfig = config.copy(propertyOrder = listOf(WifiProperty.SSID, WifiProperty.BSSID, WifiProperty.Frequency))
            .withEnabledLocationAccessRequiringProperties()
        coEvery { configSource.snapshot() } returns locationConfig
        every { statusSource.snapshot() } returns WifiStatus.Connected
        every { remoteData.widgetSnapshot(locationConfig.remoteDataConfig) } returns snapshot
        val source = WifiPropertyPresentationProvider(
            WifiPropertyResolver { enabled, _, _ -> enabled.map { ResolvedWifiProperty(it, listOf(value)) } },
            context.resources
        )
        return (provider(source)().wifiState as WidgetWifiState.Connected).rows
    }

    @Test fun `connected state uses configured order appearance utilities and current widget snapshot`() =
        runTest {
            coEvery { configSource.snapshot() } returns config
            every { statusSource.snapshot() } returns WifiStatus.ConnectedInactive
            every { remoteData.widgetSnapshot(config.remoteDataConfig) } returns snapshot

            val state = provider()()
            val rows = (state.wifiState as WidgetWifiState.Connected).rows

            assertEquals(WifiStatus.ConnectedInactive, state.wifiState.status)
            assertEquals(config.enabledProperties, resolvedProperties)
            assertEquals(config.enabledProperties.size, rows.size)
            assertEquals(rows.size, rows.map { it.id }.toSet().size)
            assertEquals(config.enabledProperties.map { it.protoId.toLong() * 1_000 }, rows.map { it.id })
            assertEquals(colors.toRenderColors(config.appearance.backgroundOpacity), state.colors)
            assertEquals(widgetPreviewState(context, config).colors, state.colors)
            assertEquals(FontSize.Large, state.fontSize)
            assertEquals(WifiPropertyValueAlignment.Left, state.valueAlignment)
            assertTrue(state.utilities.showRefreshTime)
            assertEquals(listOf(WidgetButtonAction.Refresh), state.utilities.actions)
            assertTrue(state.renderTime.normal.isNotBlank())
            verify(exactly = 1) { remoteData.widgetSnapshot(config.remoteDataConfig) }
            coVerify(exactly = 0) { remoteData.refresh(any()) }
            coVerify(exactly = 0) { remoteData.refreshForWidget(any()) }
        }

    @Test fun `multiple property rows keep IDs when unrelated properties move`() =
        runTest {
            coEvery { configSource.snapshot() } returns config
            every { statusSource.snapshot() } returns WifiStatus.Connected
            every { remoteData.widgetSnapshot(config.remoteDataConfig) } returns snapshot
            var order = listOf(WifiProperty.Gateway, WifiProperty.SiteLocalIp, WifiProperty.DNS)
            val sourceResolver = WifiPropertyResolver { _, _, _ ->
                order.map { property ->
                    val values = if (property == WifiProperty.SiteLocalIp) {
                        listOf(WifiPropertyValue("first".txt), WifiPropertyValue("second".txt))
                    } else {
                        listOf(WifiPropertyValue("value".txt))
                    }
                    ResolvedWifiProperty(property, values)
                }
            }
            val stateProvider = WidgetPresentationStateProvider(
                context,
                configSource,
                statusSource,
                remoteData,
                availability,
                WifiPropertyPresentationProvider(sourceResolver, context.resources),
                renderCoordinator,
                clock
            )

            val firstIds = (stateProvider().wifiState as WidgetWifiState.Connected).rows.map { it.id }
            order = listOf(WifiProperty.DNS, WifiProperty.SiteLocalIp, WifiProperty.Gateway)
            val reorderedIds = (stateProvider().wifiState as WidgetWifiState.Connected).rows.map { it.id }

            assertEquals(listOf(19_000L, 6_000L, 6_001L, 20_000L), firstIds)
            assertEquals(listOf(20_000L, 6_000L, 6_001L, 19_000L), reorderedIds)
        }

    @Test fun `disconnected and disabled states do not resolve or read remote properties`() =
        runTest {
            coEvery { configSource.snapshot() } returns config
            listOf(WifiStatus.Disabled, WifiStatus.NotConnected).forEach { status ->
                every { statusSource.snapshot() } returns status
                val state = provider()()
                assertEquals(status, state.wifiState.status)
                assertTrue(state.wifiState is WidgetWifiState.Disconnected)
            }
            verify(exactly = 0) { remoteData.widgetSnapshot(any()) }
        }

    @Test fun `active Glance composition receives a fresh complete state after a render request`() =
        runBlocking {
            coEvery { configSource.snapshot() } returns config
            every { renderCoordinator.revisions } returns revisions
            every { statusSource.snapshot() } returns WifiStatus.Connected
            every { remoteData.widgetSnapshot(config.remoteDataConfig) } returns snapshot
            val states = async(start = CoroutineStart.UNDISPATCHED) { provider().updates(0).take(1).toList() }

            revisions.value = 1
            val snapshots = withTimeout(5_000) { states.await() }

            assertEquals(1, snapshots.size)
            verify(exactly = 1) { remoteData.widgetSnapshot(config.remoteDataConfig) }
            coVerify(exactly = 0) { remoteData.refresh(any()) }
        }

    @Test fun `pending rapid render requests produce one current snapshot after subscription`() =
        runBlocking {
            coEvery { configSource.snapshot() } returns config
            every { renderCoordinator.revisions } returns revisions
            every { statusSource.snapshot() } returns WifiStatus.Connected
            every { remoteData.widgetSnapshot(config.remoteDataConfig) } returns snapshot

            revisions.value = 1
            revisions.value = 2
            revisions.value = 3
            val state = withTimeout(5_000) { provider().updates(afterRevision = 0).first() }

            assertEquals(WifiStatus.Connected, state.wifiState.status)
            verify(exactly = 1) { remoteData.widgetSnapshot(config.remoteDataConfig) }
            coVerify(exactly = 0) { remoteData.refreshForWidget(any()) }
        }

    @Test fun `Battery Saver placeholders are prepared without fetching remote data`() =
        runTest {
            coEvery { configSource.snapshot() } returns config
            every { statusSource.snapshot() } returns WifiStatus.Connected
            every { remoteData.widgetSnapshot(config.remoteDataConfig) } returns snapshot
            val unresolved = WifiPropertyPresentationProvider(
                WifiPropertyResolver { enabled, _, _ -> enabled.map { ResolvedWifiProperty(it, emptyList()) } },
                context.resources
            )
            val restricted = NetworkAvailabilitySource {
                NetworkAvailabilitySnapshot(true, false, false, true)
            }
            val state = WidgetPresentationStateProvider(
                context,
                configSource,
                statusSource,
                remoteData,
                restricted,
                unresolved,
                renderCoordinator,
                clock
            )()

            val rows = (state.wifiState as WidgetWifiState.Connected).rows
            assertTrue(rows.any { it.presentation.resolutionError == WifiPropertyResolutionError.BatterySaverRestricted })
            coVerify(exactly = 0) { remoteData.refresh(any()) }
            coVerify(exactly = 0) { remoteData.refreshForWidget(any()) }
        }

    @Test fun `a failed active render does not stop later presentation updates`() =
        runBlocking {
            every { renderCoordinator.revisions } returns revisions
            var snapshotsRead = 0
            coEvery { configSource.snapshot() } coAnswers {
                snapshotsRead++
                if (snapshotsRead == 1) {
                    throw IllegalStateException("temporary config read failure")
                }
                config
            }
            every { statusSource.snapshot() } returns WifiStatus.Connected
            every { remoteData.widgetSnapshot(config.remoteDataConfig) } returns snapshot
            val states = async(start = CoroutineStart.UNDISPATCHED) { provider().updates(0).take(1).toList() }
            revisions.value = 1
            // The collector handles the failure before the next revision.
            withTimeout(5_000) {
                while (snapshotsRead == 0) kotlinx.coroutines.yield()
            }
            revisions.value = 2

            assertEquals(1, withTimeout(5_000) { states.await() }.size)
            assertEquals(2, snapshotsRead)
        }
}
