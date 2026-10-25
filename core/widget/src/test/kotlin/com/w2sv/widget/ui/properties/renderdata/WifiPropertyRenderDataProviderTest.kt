package com.w2sv.widget.ui.properties.renderdata

import android.content.Context
import android.content.res.Resources
import com.w2sv.core.resources.R
import com.w2sv.domain.networking.IpApiData
import com.w2sv.domain.networking.NetworkAvailabilitySnapshot
import com.w2sv.domain.networking.NetworkAvailabilitySource
import com.w2sv.domain.networking.RemoteNetworkData
import com.w2sv.domain.networking.RemoteNetworkDataRepository
import com.w2sv.domain.widget.WidgetColoring
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.domain.wifiproperty.ResolvedWifiProperty
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.WifiPropertyResolutionError
import com.w2sv.domain.wifiproperty.WifiPropertyResolver
import com.w2sv.domain.wifiproperty.WifiPropertyValue
import com.w2sv.domain.wifiproperty.presentation.SubscriptableText
import com.w2sv.domain.wifiproperty.presentation.WifiPropertyPresentation
import com.w2sv.domain.wifiproperty.presentation.WifiPropertyPresentationProvider
import com.w2sv.domain.wifiproperty.txt
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WifiPropertyRenderDataProviderTest {
    private val context = mockk<Context>()
    private val availabilitySource = mockk<NetworkAvailabilitySource>()
    private val remoteDataRepository = mockk<RemoteNetworkDataRepository> {
        every { widgetSnapshot(any()) } returns RemoteNetworkData.empty
    }
    private val resources = mockk<Resources> {
        every { getString(any()) } answers {
            when (firstArg<Int>()) {
                R.string.ip -> "IP"
                R.string.site_local_short -> "SiteLocal"
                R.string.ula -> "ULA"
                R.string.public_ -> "Public"
                R.string.ssid -> "SSID"
                R.string.isp -> "ISP"
                R.string.dns -> "DNS"
                R.string.battery_saver_restricted -> "Unavailable while Battery Saver is on"
                else -> error("Unexpected string resource")
            }
        }
    }

    @Before
    fun initializePropertyCatalog() {
        // Initialize the sealed catalog before nested property objects to avoid a JVM class-init cycle.
        WifiProperty.entries
    }

    @Test
    fun `one connectivity snapshot supplies individual widget placeholders while preserving resolved rows`() {
        val properties = listOf(
            WifiProperty.SSID,
            WifiProperty.SiteLocalIp,
            WifiProperty.ULA,
            WifiProperty.PublicIp,
            WifiProperty.ISP,
            WifiProperty.DNS
        )
        val config = config(properties)
        val resolutions = listOf(
            ResolvedWifiProperty(WifiProperty.SSID, listOf(WifiPropertyValue("Home".txt))),
            ResolvedWifiProperty(
                WifiProperty.SiteLocalIp,
                listOf(WifiPropertyValue("192.168.1.2".txt), WifiPropertyValue("fe80::2".txt))
            ),
            ResolvedWifiProperty(WifiProperty.ULA, emptyList()),
            ResolvedWifiProperty(WifiProperty.PublicIp, emptyList()),
            ResolvedWifiProperty(WifiProperty.ISP, emptyList()),
            ResolvedWifiProperty(WifiProperty.DNS, emptyList())
        )
        every { availabilitySource() } returns NetworkAvailabilitySnapshot(true, false, false, true)

        val actual = provider(config, resolutions)(context).presentations

        assertEquals(
            listOf(
                WifiPropertyPresentation(SubscriptableText("SSID"), "Home"),
                WifiPropertyPresentation(SubscriptableText("IP", "SiteLocal 1"), "192.168.1.2"),
                WifiPropertyPresentation(SubscriptableText("IP", "SiteLocal 2"), "fe80::2"),
                placeholder(SubscriptableText("IP", "ULA")),
                placeholder(SubscriptableText("IP", "Public")),
                placeholder(SubscriptableText("ISP"))
            ),
            actual
        )
        verify(exactly = 1) { availabilitySource() }
        verify(exactly = 1) { remoteDataRepository.widgetSnapshot(config.remoteDataConfig) }
    }

    @Test
    fun `accessible LinkProperties leave absent IP types omitted`() {
        val properties = listOf(WifiProperty.ULA, WifiProperty.PublicIp)
        every { availabilitySource() } returns NetworkAvailabilitySnapshot(true, true, true, true)

        val actual = provider(
            config(properties),
            properties.map { ResolvedWifiProperty(it, emptyList()) }
        )(context).presentations

        assertEquals(emptyList<WifiPropertyPresentation>(), actual)
    }

    @Test
    fun `accessible Wi-Fi addresses stay visible while unavailable remote data uses a placeholder`() {
        val properties = listOf(WifiProperty.SiteLocalIp, WifiProperty.PublicIp, WifiProperty.ULA)
        every { availabilitySource() } returns NetworkAvailabilitySnapshot(true, false, true, true)

        val actual = provider(
            config(properties),
            listOf(
                ResolvedWifiProperty(WifiProperty.SiteLocalIp, listOf(WifiPropertyValue("192.168.1.7".txt))),
                ResolvedWifiProperty(WifiProperty.PublicIp, emptyList()),
                ResolvedWifiProperty(WifiProperty.ULA, emptyList())
            )
        )(context).presentations

        assertEquals(
            listOf(
                WifiPropertyPresentation(SubscriptableText("IP", "SiteLocal"), "192.168.1.7"),
                placeholder(SubscriptableText("IP", "Public"))
            ),
            actual
        )
    }

    @Test
    fun `unusable default network explains missing remote data while accessible IP addresses stay omitted`() {
        every { availabilitySource() } returns NetworkAvailabilitySnapshot(true, false, true, true)

        val actual = provider(
            config(listOf(WifiProperty.PublicIp, WifiProperty.ULA)),
            listOf(
                ResolvedWifiProperty(WifiProperty.PublicIp, emptyList()),
                ResolvedWifiProperty(WifiProperty.ULA, emptyList())
            )
        )(context).presentations

        assertEquals(listOf(placeholder(SubscriptableText("IP", "Public"))), actual)
    }

    @Test
    fun `retained Wi-Fi data resolves a normal copyable row despite an unavailable default route`() {
        val retained = RemoteNetworkData(
            IpApiData(null, null, "UTC", "Last-known ISP", null),
            emptyList()
        )
        every { remoteDataRepository.widgetSnapshot(any()) } returns retained
        every { availabilitySource() } returns NetworkAvailabilitySnapshot(true, false, true, true)
        val config = config(listOf(WifiProperty.ISP))

        val actual = provider(
            config,
            listOf(ResolvedWifiProperty(WifiProperty.ISP, listOf(WifiPropertyValue("Last-known ISP".txt)))),
            expectedRemoteData = retained
        )(context).presentations

        assertEquals(listOf(WifiPropertyPresentation(SubscriptableText("ISP"), "Last-known ISP")), actual)
        assertEquals(null, actual.single().resolutionError)
    }

    @Test
    fun `verified VPN data resolves a normal property row while Wi-Fi is connected`() {
        val vpnData = RemoteNetworkData(IpApiData(null, null, "UTC", "VPN ISP", null), emptyList())
        every { remoteDataRepository.widgetSnapshot(any()) } returns vpnData
        every { availabilitySource() } returns NetworkAvailabilitySnapshot(
            isBatterySaverEnabled = true,
            hasUsableDefaultNetwork = true,
            hasLinkProperties = true,
            isWifiConnected = true,
            isVpnDefaultNetwork = true
        )

        val actual = provider(
            config(listOf(WifiProperty.ISP)),
            listOf(ResolvedWifiProperty(WifiProperty.ISP, listOf(WifiPropertyValue("VPN ISP".txt)))),
            expectedRemoteData = vpnData
        )(context).presentations

        assertEquals(listOf(WifiPropertyPresentation(SubscriptableText("ISP"), "VPN ISP")), actual)
        assertEquals(null, actual.single().resolutionError)
    }

    @Test
    fun `default route becoming unavailable does not alternate retained values with placeholders`() {
        val retained = RemoteNetworkData(IpApiData(null, null, "UTC", "Wi-Fi ISP", null), emptyList())
        every { remoteDataRepository.widgetSnapshot(any()) } returns retained
        every { availabilitySource() } returnsMany listOf(
            NetworkAvailabilitySnapshot(true, true, true, true),
            NetworkAvailabilitySnapshot(true, false, true, true)
        )
        val config = config(listOf(WifiProperty.ISP))
        val provider = provider(
            config,
            listOf(ResolvedWifiProperty(WifiProperty.ISP, listOf(WifiPropertyValue("Wi-Fi ISP".txt)))),
            expectedRemoteData = retained
        )

        val foreground = provider(context).presentations
        val background = provider(context).presentations

        assertEquals(foreground, background)
        assertEquals(listOf(WifiPropertyPresentation(SubscriptableText("ISP"), "Wi-Fi ISP")), background)
        verify(exactly = 0) { remoteDataRepository.reconciledSnapshot(any()) }
    }

    private fun config(properties: List<WifiProperty>): WidgetConfig =
        WidgetConfig.default
            .copy(
                propertyOrder = properties,
                appearance = WidgetConfig.default.appearance.copy(coloring = WidgetColoring(useCustom = true))
            )
            .withUpdatedPropertyEnablement(WifiProperty.SSID, true)

    private fun provider(
        config: WidgetConfig,
        resolutions: List<ResolvedWifiProperty>,
        expectedRemoteData: RemoteNetworkData = RemoteNetworkData.empty
    ): WifiPropertyRenderDataProvider {
        val configSource = mockk<WidgetConfigSource> {
            every { blockingSnapshot() } returns config
        }
        val resolver = WifiPropertyResolver { properties, _, remoteData ->
            assertEquals(config.enabledProperties, properties)
            assertEquals(expectedRemoteData, remoteData)
            resolutions
        }

        return WifiPropertyRenderDataProvider(
            configSource,
            remoteDataRepository,
            WifiPropertyPresentationProvider(resolver, resources),
            availabilitySource
        )
    }

    private fun placeholder(label: SubscriptableText) =
        WifiPropertyPresentation(
            label,
            "Unavailable while Battery Saver is on",
            resolutionError = WifiPropertyResolutionError.BatterySaverRestricted
        )
}
