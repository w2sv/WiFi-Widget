package com.w2sv.networking.remotenetworkdata

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import com.w2sv.domain.networking.IpAddress
import com.w2sv.domain.networking.IpApiData
import com.w2sv.domain.networking.RemoteNetworkData
import com.w2sv.domain.networking.RemoteRefreshResult
import com.w2sv.domain.widget.WidgetConfig
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.settings.IpSetting
import com.w2sv.domain.wifiproperty.settings.LocationParameter
import com.w2sv.networking.remotenetworkdata.ipapi.IpApiDataFetcher
import com.w2sv.networking.remotenetworkdata.publicip.PublicIpFetcher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.IOException
import java.util.concurrent.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@Suppress("DEPRECATION")
class RemoteNetworkDataRepositoryImplTest {
    private val failed = Result.failure<Nothing>(IOException("unavailable"))
    private val api = mockk<IpApiDataFetcher>()
    private val ips = mockk<PublicIpFetcher>()
    private val connectivity = mockk<ConnectivityManager>()
    private val wifiManager = mockk<WifiManager> {
        every { isWifiEnabled } returns true
    }
    private val networkA = mockk<Network>()
    private val networkB = mockk<Network>()
    private val capabilities = mockk<NetworkCapabilities> {
        every { hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true
        every { hasTransport(NetworkCapabilities.TRANSPORT_VPN) } returns false
        every { hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns false
        every { hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
    }
    private val config = WidgetConfig.default
        .withUpdatedPropertyEnablement(WifiProperty.PublicIp, true)
        .withUpdatedPropertyEnablement(WifiProperty.ISP, true)
    private val apiData = IpApiData(null, null, "UTC", "Example ISP", "AS123")
    private val ipv4 = mockk<IpAddress.V4> { every { version } returns IpAddress.Version.V4 }
    private val ipv6 = mockk<IpAddress.V6> { every { version } returns IpAddress.Version.V6 }

    private fun repository(configProvider: suspend () -> WidgetConfig = { config }): RemoteNetworkDataRepositoryImpl {
        every { connectivity.activeNetwork } returns networkA
        every { connectivity.allNetworks } returns arrayOf(networkA)
        every { connectivity.getNetworkCapabilities(any()) } returns capabilities
        coEvery { api.fetch(any()) } returns Result.success(apiData)
        coEvery { ips.fetch(any()) } returns mapOf(
            IpAddress.Version.V4 to Result.success(ipv4),
            IpAddress.Version.V6 to Result.success(ipv6)
        )
        val configSource = mockk<WidgetConfigSource> {
            coEvery { snapshot() } coAnswers { configProvider() }
        }
        return RemoteNetworkDataRepositoryImpl(api, ips, configSource, connectivity, wifiManager)
    }

    @Test fun `transient failures preserve each successful field`() =
        runTest {
            val repository = repository()
            repository.refresh()
            coEvery { api.fetch(any()) } returns failed
            coEvery { ips.fetch(any()) } returns mapOf(
                IpAddress.Version.V4 to failed,
                IpAddress.Version.V6 to Result.success(ipv6)
            )
            assertEquals(RemoteRefreshResult.Complete, repository.refresh())
            assertEquals(apiData.isp, repository.data.value.ipApiData?.isp)
            assertEquals(setOf(ipv4, ipv6), repository.data.value.publicIps.toSet())
            assertFalse(repository.needsRecovery(config.remoteDataConfig))
        }

    @Test fun `recovery waiting behind a completed refresh skips duplicate HTTP`() =
        runTest {
            val repository = repository()
            assertEquals(RemoteRefreshResult.Complete, repository.refresh())
            assertEquals(RemoteRefreshResult.Complete, repository.refresh(onlyIfMissing = true))
            coVerify(exactly = 1) { api.fetch(any()) }
            coVerify(exactly = 1) { ips.fetch(any()) }
        }

    @Test fun `recovery fetches only missing IP-API after public IP succeeds`() =
        runTest {
            val repository = repository()
            coEvery { api.fetch(any()) } returnsMany listOf(failed, Result.success(apiData))
            assertEquals(RemoteRefreshResult.Incomplete, repository.refresh())
            assertEquals(RemoteRefreshResult.Complete, repository.refresh(onlyIfMissing = true))
            coVerify(exactly = 2) { api.fetch(any()) }
            coVerify(exactly = 1) { ips.fetch(any()) }
        }

    @Test fun `recovery fetches only missing public IP after IP-API succeeds`() =
        runTest {
            val repository = repository()
            coEvery { ips.fetch(any()) } returnsMany listOf(
                mapOf(IpAddress.Version.V4 to failed, IpAddress.Version.V6 to failed),
                mapOf(IpAddress.Version.V4 to Result.success(ipv4), IpAddress.Version.V6 to failed)
            )
            assertEquals(RemoteRefreshResult.Incomplete, repository.refresh())
            assertEquals(RemoteRefreshResult.Complete, repository.refresh(onlyIfMissing = true))
            coVerify(exactly = 1) { api.fetch(any()) }
            coVerify(exactly = 2) { ips.fetch(any()) }
        }

    @Test fun `unavailable IPv6 is attempted without repeated recovery`() =
        runTest {
            val repository = repository()
            coEvery { ips.fetch(any()) } returns mapOf(
                IpAddress.Version.V4 to Result.success(ipv4),
                IpAddress.Version.V6 to failed
            )
            assertEquals(RemoteRefreshResult.Complete, repository.refresh())
            assertEquals(listOf(ipv4), repository.data.value.publicIps)
            assertFalse(repository.needsRecovery(config.remoteDataConfig))
        }

    @Test fun `enabling IPv6 after IPv4 success attempts only the new version during recovery`() =
        runTest {
            val initial = config.withUpdatedPropertyConfig(WifiProperty.PublicIp) {
                it.copy(settings = it.settings + (IpSetting.V6Enabled to false))
            }
            val configs = MutableStateFlow(initial)
            val requested = mutableListOf<Set<IpAddress.Version>>()
            val repository = repository { configs.value }
            coEvery { ips.fetch(any()) } coAnswers {
                val versions = firstArg<Set<IpAddress.Version>>()
                requested += versions
                versions.associateWith { version ->
                    if (version == IpAddress.Version.V4) Result.success(ipv4) else failed
                }
            }

            assertEquals(RemoteRefreshResult.Complete, repository.refresh())
            configs.value = config
            assertTrue(repository.needsRecovery(config.remoteDataConfig))
            assertEquals(RemoteRefreshResult.Complete, repository.refresh(onlyIfMissing = true))
            assertEquals(listOf(setOf(IpAddress.Version.V4), setOf(IpAddress.Version.V6)), requested)
            assertEquals(listOf(ipv4), repository.data.value.publicIps)
            assertFalse(repository.needsRecovery(config.remoteDataConfig))
        }

    @Test fun `re-enabling a previously attempted IP version requires a new attempt`() =
        runTest {
            val configs = MutableStateFlow(config)
            val requested = mutableListOf<Set<IpAddress.Version>>()
            val repository = repository { configs.value }
            coEvery { ips.fetch(any()) } coAnswers {
                val versions = firstArg<Set<IpAddress.Version>>()
                requested += versions
                versions.associateWith { if (it == IpAddress.Version.V4) Result.success(ipv4) else failed }
            }

            assertEquals(RemoteRefreshResult.Complete, repository.refresh())
            configs.value = config.withUpdatedPropertyConfig(WifiProperty.PublicIp) {
                it.copy(settings = it.settings + (IpSetting.V6Enabled to false))
            }
            assertFalse(repository.needsRecovery(configs.value.remoteDataConfig))
            configs.value = config
            assertTrue(repository.needsRecovery(config.remoteDataConfig))
            assertEquals(RemoteRefreshResult.Complete, repository.refresh(onlyIfMissing = true))
            assertEquals(
                listOf(setOf(IpAddress.Version.V4, IpAddress.Version.V6), setOf(IpAddress.Version.V6)),
                requested
            )
        }

    @Test fun `disabled remote groups are excluded from HTTP execution`() =
        runTest {
            val localOnly = listOf(
                WifiProperty.Location,
                WifiProperty.IpCoordinates,
                WifiProperty.ISP,
                WifiProperty.ASN,
                WifiProperty.PublicIp
            ).fold(config) { current, property -> current.withUpdatedPropertyEnablement(property, false) }
            val repository = repository { localOnly }
            assertEquals(RemoteRefreshResult.Complete, repository.refresh())
            coVerify(exactly = 0) { api.fetch(any()) }
            coVerify(exactly = 0) { ips.fetch(any()) }
        }

    @Test fun `all initial failures leave both groups uninitialized`() =
        runTest {
            val repository = repository()
            coEvery { api.fetch(any()) } returns failed
            coEvery { ips.fetch(any()) } returns mapOf(
                IpAddress.Version.V4 to failed,
                IpAddress.Version.V6 to failed
            )
            assertEquals(RemoteRefreshResult.Incomplete, repository.refresh())
            assertTrue(repository.needsRecovery(config.remoteDataConfig))
            assertEquals(null, repository.reconciledSnapshot(config.remoteDataConfig).ipApiData)
            assertTrue(repository.data.value.publicIps.isEmpty())
        }

    @Test fun `initial failures recover after later success`() =
        runTest {
            val repository = repository()
            coEvery { api.fetch(any()) } returns failed
            coEvery { ips.fetch(any()) } returns
                mapOf(IpAddress.Version.V4 to failed, IpAddress.Version.V6 to failed)
            assertEquals(RemoteRefreshResult.Incomplete, repository.refresh())
            coEvery { api.fetch(any()) } returns Result.success(apiData)
            coEvery { ips.fetch(any()) } returns mapOf(
                IpAddress.Version.V4 to Result.success(ipv4),
                IpAddress.Version.V6 to failed
            )
            assertEquals(RemoteRefreshResult.Complete, repository.refresh())
            assertFalse(repository.needsRecovery(config.remoteDataConfig))
        }

    @Test fun `public IPv4 success does not hide missing IP-API data`() =
        runTest {
            val repository = repository()
            coEvery { api.fetch(any()) } returns failed
            coEvery { ips.fetch(any()) } returns mapOf(
                IpAddress.Version.V4 to Result.success(ipv4),
                IpAddress.Version.V6 to failed
            )
            assertEquals(RemoteRefreshResult.Incomplete, repository.refresh())
            assertEquals(listOf(ipv4), repository.data.value.publicIps)
            assertTrue(repository.needsRecovery(config.remoteDataConfig))
        }

    @Test fun `only IPv6 failure remains recoverable`() =
        runTest {
            val onlyV6 = listOf(WifiProperty.Location, WifiProperty.IpCoordinates, WifiProperty.ISP, WifiProperty.ASN)
                .fold(config) { current, property -> current.withUpdatedPropertyEnablement(property, false) }
                .withUpdatedPropertyConfig(WifiProperty.PublicIp) { propertyConfig ->
                    propertyConfig.copy(settings = propertyConfig.settings + (IpSetting.V4Enabled to false))
                }
            val repository = repository { onlyV6 }
            coEvery { ips.fetch(any()) } returns mapOf(IpAddress.Version.V6 to failed)
            assertEquals(RemoteRefreshResult.Incomplete, repository.refresh())
            assertTrue(repository.needsRecovery(onlyV6.remoteDataConfig))
        }

    @Test fun `disabled properties clear their cached values`() =
        runTest {
            val repository = repository()
            repository.refresh()
            val disabled = config.withUpdatedPropertyEnablement(WifiProperty.PublicIp, false)
                .withUpdatedPropertyEnablement(WifiProperty.ISP, false)
            repository.needsRecovery(disabled.remoteDataConfig)
            assertTrue(repository.data.value.publicIps.isEmpty())
            assertEquals("", repository.data.value.ipApiData?.isp)
        }

    @Test fun `no enabled remote properties do not need recovery`() =
        runTest {
            val repository = repository()
            val disabled = listOf(
                WifiProperty.PublicIp,
                WifiProperty.Location,
                WifiProperty.IpCoordinates,
                WifiProperty.ISP,
                WifiProperty.ASN
            ).fold(config) { current, property -> current.withUpdatedPropertyEnablement(property, false) }
            assertFalse(repository.needsRecovery(disabled.remoteDataConfig))
        }

    @Test fun `new network invalidates and requires recovery`() =
        runTest {
            val repository = repository()
            repository.refresh()
            every { connectivity.activeNetwork } returns networkB
            assertTrue(repository.needsRecovery(config.remoteDataConfig))
            assertEquals(emptyList<IpAddress>(), repository.data.value.publicIps)
        }

    @Test fun `unavailable identity withholds without discarding same network cache`() =
        runTest {
            val repository = repository()
            repository.refresh()
            every { connectivity.activeNetwork } returns null
            assertEquals(RemoteNetworkData.empty, repository.reconciledSnapshot(config.remoteDataConfig))
            assertEquals(RemoteNetworkData.empty, repository.data.value)
            repeat(2) {
                assertTrue(repository.needsRecovery(config.remoteDataConfig))
                assertEquals(2, repository.widgetSnapshot(config.remoteDataConfig).publicIps.size)
            }
            every { connectivity.activeNetwork } returns networkA
            assertFalse(repository.needsRecovery(config.remoteDataConfig))
            assertEquals(2, repository.data.value.publicIps.size)
            assertEquals(RemoteRefreshResult.Complete, repository.refresh(onlyIfMissing = true))
            coVerify(exactly = 1) { api.fetch(any()) }
            coVerify(exactly = 1) { ips.fetch(any()) }
        }

    @Test fun `visible cached network retains widget data without verifying the default egress`() =
        runTest {
            val repository = repository()
            repository.refresh()
            every { connectivity.activeNetwork } returns null
            every { connectivity.allNetworks } returns arrayOf(networkA)

            assertEquals(RemoteNetworkData.empty, repository.reconciledSnapshot(config.remoteDataConfig))
            assertTrue(repository.needsRecovery(config.remoteDataConfig))
            assertEquals(RemoteNetworkData.empty, repository.data.value)
            verify(exactly = 0) { connectivity.allNetworks }
            assertEquals(2, repository.widgetSnapshot(config.remoteDataConfig).publicIps.size)
            assertEquals(RemoteNetworkData.empty, repository.data.value)

            every { connectivity.activeNetwork } returns networkB
            assertEquals(RemoteNetworkData.empty, repository.reconciledSnapshot(config.remoteDataConfig))
            every { connectivity.activeNetwork } returns networkA
            assertEquals(2, repository.reconciledSnapshot(config.remoteDataConfig).publicIps.size)
            assertTrue(repository.needsRecovery(config.remoteDataConfig))
        }

    @Test fun `requirements changed while route is hidden are applied before cache reappears`() =
        runTest {
            val repository = repository()
            repository.refresh()
            val withoutPublicIp = config.withUpdatedPropertyEnablement(WifiProperty.PublicIp, false)
            every { connectivity.activeNetwork } returns null
            assertEquals(RemoteNetworkData.empty, repository.reconciledSnapshot(withoutPublicIp.remoteDataConfig))

            every { connectivity.activeNetwork } returns networkA
            val restored = repository.reconciledSnapshot(withoutPublicIp.remoteDataConfig)
            assertTrue(restored.publicIps.isEmpty())
            assertEquals(apiData.isp, restored.ipApiData?.isp)
        }

    @Test fun `temporarily unavailable capabilities withhold and restore same-network data`() =
        runTest {
            val repository = repository()
            repository.refresh()
            every { connectivity.getNetworkCapabilities(networkA) } returns null
            assertEquals(RemoteNetworkData.empty, repository.reconciledSnapshot(config.remoteDataConfig))
            assertEquals(RemoteNetworkData.empty, repository.data.value)
            every { connectivity.getNetworkCapabilities(networkA) } returns capabilities
            assertEquals(2, repository.reconciledSnapshot(config.remoteDataConfig).publicIps.size)
            assertFalse(repository.needsRecovery(config.remoteDataConfig))
        }

    @Test fun `known new default hides egress data while the old Wi-Fi cache remains identifiable`() =
        runTest {
            val repository = repository()
            repository.refresh()
            every { connectivity.activeNetwork } returns networkB
            every { connectivity.getNetworkCapabilities(networkB) } returns null
            assertEquals(RemoteNetworkData.empty, repository.reconciledSnapshot(config.remoteDataConfig))
            every { connectivity.activeNetwork } returns networkA
            assertEquals(2, repository.reconciledSnapshot(config.remoteDataConfig).publicIps.size)
            assertTrue(repository.needsRecovery(config.remoteDataConfig))
        }

    @Test fun `confirmed route change restores Wi-Fi values but requires one new recovery refresh`() =
        runTest {
            val repository = repository()
            assertEquals(RemoteRefreshResult.Complete, repository.refresh())
            every { connectivity.activeNetwork } returns networkB
            every { connectivity.allNetworks } returns arrayOf(networkA, networkB)
            every { connectivity.getNetworkCapabilities(networkB) } returns otherCapabilities(wifi = false)
            assertEquals(RemoteNetworkData.empty, repository.reconciledSnapshot(config.remoteDataConfig))
            assertEquals(apiData.isp, repository.widgetSnapshot(config.remoteDataConfig).ipApiData?.isp)

            every { connectivity.activeNetwork } returns networkA
            assertEquals(apiData.isp, repository.reconciledSnapshot(config.remoteDataConfig).ipApiData?.isp)
            assertTrue(repository.needsRecovery(config.remoteDataConfig))
            assertEquals(2, repository.widgetSnapshot(config.remoteDataConfig).publicIps.size)
            coEvery { api.fetch(any()) } returns Result.success(apiData.copy(isp = "Updated ISP"))

            assertEquals(RemoteRefreshResult.Complete, repository.refresh(onlyIfMissing = true))
            assertEquals("Updated ISP", repository.data.value.ipApiData?.isp)
            assertEquals("Updated ISP", repository.widgetSnapshot(config.remoteDataConfig).ipApiData?.isp)
            assertFalse(repository.needsRecovery(config.remoteDataConfig))
            assertEquals(RemoteRefreshResult.Complete, repository.refresh(onlyIfMissing = true))
            coVerify(exactly = 2) { api.fetch(any()) }
            coVerify(exactly = 2) { ips.fetch(any()) }
        }

    @Test fun `delayed Wi-Fi default capabilities restore cached values exactly once`() =
        runTest {
            val repository = repository()
            repository.refresh()
            every { connectivity.activeNetwork } returns networkB
            every { connectivity.getNetworkCapabilities(networkB) } returns otherCapabilities(wifi = false)
            assertEquals(RemoteNetworkData.empty, repository.reconciledSnapshot(config.remoteDataConfig))

            every { connectivity.activeNetwork } returns networkA
            every { connectivity.getNetworkCapabilities(networkA) } returns null
            assertEquals(RemoteNetworkData.empty, repository.reconciledSnapshot(config.remoteDataConfig))
            assertEquals(RemoteNetworkData.empty, repository.data.value)

            every { connectivity.getNetworkCapabilities(networkA) } returns capabilities
            assertEquals(apiData.isp, repository.reconciledSnapshot(config.remoteDataConfig).ipApiData?.isp)
            assertTrue(repository.needsRecovery(config.remoteDataConfig))
            assertEquals(2, repository.widgetSnapshot(config.remoteDataConfig).publicIps.size)
            coEvery { api.fetch(any()) } returns Result.success(apiData.copy(isp = "Refreshed ISP"))

            assertEquals(RemoteRefreshResult.Complete, repository.refresh(onlyIfMissing = true))
            repeat(2) {
                assertEquals("Refreshed ISP", repository.reconciledSnapshot(config.remoteDataConfig).ipApiData?.isp)
                assertFalse(repository.needsRecovery(config.remoteDataConfig))
            }
            assertEquals(RemoteRefreshResult.Complete, repository.refresh(onlyIfMissing = true))
            coVerify(exactly = 2) { api.fetch(any()) }
            coVerify(exactly = 2) { ips.fetch(any()) }
        }

    @Test fun `missing INTERNET capability keeps Wi-Fi restoration pending`() =
        runTest {
            val repository = repository()
            repository.refresh()
            every { connectivity.activeNetwork } returns networkB
            every { connectivity.getNetworkCapabilities(networkB) } returns otherCapabilities(wifi = false)
            repository.reconciledSnapshot(config.remoteDataConfig)

            every { connectivity.activeNetwork } returns networkA
            every { connectivity.getNetworkCapabilities(networkA) } returns mockk {
                every { hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns false
            }
            assertEquals(RemoteNetworkData.empty, repository.reconciledSnapshot(config.remoteDataConfig))

            every { connectivity.getNetworkCapabilities(networkA) } returns capabilities
            assertEquals(apiData.isp, repository.reconciledSnapshot(config.remoteDataConfig).ipApiData?.isp)
            assertTrue(repository.needsRecovery(config.remoteDataConfig))
            assertEquals(apiData.isp, repository.widgetSnapshot(config.remoteDataConfig).ipApiData?.isp)
        }

    @Test fun `failed recovery retains restored Wi-Fi values and remains eligible`() =
        runTest {
            val repository = repository()
            repository.refresh()
            every { connectivity.activeNetwork } returns networkB
            every { connectivity.getNetworkCapabilities(networkB) } returns otherCapabilities(wifi = false)
            repository.reconciledSnapshot(config.remoteDataConfig)
            every { connectivity.activeNetwork } returns networkA
            coEvery { api.fetch(any()) } returns failed
            coEvery { ips.fetch(any()) } returns mapOf(
                IpAddress.Version.V4 to failed,
                IpAddress.Version.V6 to failed
            )

            assertTrue(repository.needsRecovery(config.remoteDataConfig))
            assertEquals(RemoteRefreshResult.Incomplete, repository.refresh(onlyIfMissing = true))
            assertEquals(apiData.isp, repository.widgetSnapshot(config.remoteDataConfig).ipApiData?.isp)
            assertEquals(2, repository.data.value.publicIps.size)
            assertTrue(repository.needsRecovery(config.remoteDataConfig))
        }

    @Test fun `default route changing to VPN invalidates and refetches egress data`() =
        runTest {
            val repository = repository()
            repository.refresh()
            val vpn = mockk<NetworkCapabilities> {
                every { hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns false
                every { hasTransport(NetworkCapabilities.TRANSPORT_VPN) } returns true
                every { hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
            }
            every { connectivity.activeNetwork } returns networkB
            every { connectivity.getNetworkCapabilities(networkB) } returns vpn
            assertTrue(repository.reconciledSnapshot(config.remoteDataConfig).publicIps.isEmpty())
            assertTrue(repository.needsRecovery(config.remoteDataConfig))
            coEvery { api.fetch(any()) } returns Result.success(apiData.copy(isp = "VPN ISP"))
            assertEquals(RemoteRefreshResult.Complete, repository.refresh())
            assertEquals("VPN ISP", repository.reconciledSnapshot(config.remoteDataConfig).ipApiData?.isp)
        }

    @Test fun `widget HTTP fetches through direct Wi-Fi or VPN but not cellular`() =
        runTest {
            val repository = repository()
            assertEquals(RemoteRefreshResult.Complete, repository.refreshForWidget())

            every { connectivity.activeNetwork } returns networkB
            every { connectivity.allNetworks } returns arrayOf(networkA, networkB)
            every { connectivity.getNetworkCapabilities(networkB) } returns otherCapabilities(wifi = false, vpn = true)
            coEvery { api.fetch(any()) } returns Result.success(apiData.copy(isp = "VPN ISP"))
            assertEquals(RemoteRefreshResult.Complete, repository.refreshForWidget())
            assertEquals("VPN ISP", repository.widgetSnapshot(config.remoteDataConfig).ipApiData?.isp)

            every { connectivity.getNetworkCapabilities(networkB) } returns otherCapabilities(wifi = false)
            assertEquals(RemoteRefreshResult.NetworkUnavailable, repository.refreshForWidget())
            assertEquals(apiData.isp, repository.widgetSnapshot(config.remoteDataConfig).ipApiData?.isp)
            coVerify(exactly = 2) { api.fetch(any()) }
            coVerify(exactly = 2) { ips.fetch(any()) }
        }

    @Test fun `widget HTTP rejects missing route details and disconnected Wi-Fi`() =
        runTest {
            val repository = repository()
            every { connectivity.activeNetwork } returns null
            assertEquals(RemoteRefreshResult.NetworkUnavailable, repository.refreshForWidget())

            every { connectivity.activeNetwork } returns networkA
            every { connectivity.getNetworkCapabilities(networkA) } returns null
            assertEquals(RemoteRefreshResult.NetworkUnavailable, repository.refreshForWidget())

            every { connectivity.getNetworkCapabilities(networkA) } returns mockk {
                every { hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns false
            }
            assertEquals(RemoteRefreshResult.NetworkUnavailable, repository.refreshForWidget())

            every { connectivity.getNetworkCapabilities(networkA) } returns capabilities
            every { wifiManager.isWifiEnabled } returns false
            assertEquals(RemoteRefreshResult.NetworkUnavailable, repository.refreshForWidget())
            coVerify(exactly = 0) { api.fetch(any()) }
            coVerify(exactly = 0) { ips.fetch(any()) }
        }

    @Test fun `default route changing to cellular cannot reuse Wi-Fi egress data`() =
        runTest {
            val repository = repository()
            repository.refresh()
            val cellular = mockk<NetworkCapabilities> {
                every { hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns true
                every { hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns false
                every { hasTransport(NetworkCapabilities.TRANSPORT_VPN) } returns false
                every { hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
            }
            every { connectivity.activeNetwork } returns networkB
            every { connectivity.getNetworkCapabilities(networkB) } returns cellular
            assertEquals(null, repository.reconciledSnapshot(config.remoteDataConfig).ipApiData)
            assertTrue(repository.reconciledSnapshot(config.remoteDataConfig).publicIps.isEmpty())
            assertTrue(repository.needsRecovery(config.remoteDataConfig))
        }

    @Test fun `connected Wi-Fi keeps its widget values when cellular becomes the default`() =
        runTest {
            val repository = repository()
            repository.refresh()
            every { connectivity.activeNetwork } returns networkB
            every { connectivity.allNetworks } returns arrayOf(networkA, networkB)
            every { connectivity.getNetworkCapabilities(networkB) } returns otherCapabilities(wifi = false)

            assertEquals(RemoteNetworkData.empty, repository.reconciledSnapshot(config.remoteDataConfig))
            assertEquals(apiData.isp, repository.widgetSnapshot(config.remoteDataConfig).ipApiData?.isp)
            assertEquals(2, repository.widgetSnapshot(config.remoteDataConfig).publicIps.size)
            assertEquals(RemoteNetworkData.empty, repository.data.value)
            assertTrue(repository.needsRecovery(config.remoteDataConfig))

            coEvery { api.fetch(any()) } returns Result.success(apiData.copy(isp = "Cellular ISP"))
            repository.refresh()
            assertEquals("Cellular ISP", repository.data.value.ipApiData?.isp)
            assertEquals(apiData.isp, repository.widgetSnapshot(config.remoteDataConfig).ipApiData?.isp)
        }

    @Test fun `VPN values take precedence while its default route is verified`() =
        runTest {
            val repository = repository()
            repository.refresh()
            every { connectivity.activeNetwork } returns networkB
            every { connectivity.allNetworks } returns arrayOf(networkA, networkB)
            every { connectivity.getNetworkCapabilities(networkB) } returns otherCapabilities(wifi = true, vpn = true)

            // Before a VPN fetch, the widget can still use values from connected Wi-Fi A.
            assertEquals(apiData.isp, repository.widgetSnapshot(config.remoteDataConfig).ipApiData?.isp)
            coEvery { api.fetch(any()) } returns Result.success(apiData.copy(isp = "VPN ISP"))
            repository.refresh()
            assertEquals("VPN ISP", repository.data.value.ipApiData?.isp)
            assertEquals("VPN ISP", repository.widgetSnapshot(config.remoteDataConfig).ipApiData?.isp)

            every { wifiManager.isWifiEnabled } returns false
            assertEquals(RemoteNetworkData.empty, repository.widgetSnapshot(config.remoteDataConfig))
            every { wifiManager.isWifiEnabled } returns true

            // The VPN fetch must not replace A's Wi-Fi association.
            every { connectivity.activeNetwork } returns null
            assertEquals(apiData.isp, repository.widgetSnapshot(config.remoteDataConfig).ipApiData?.isp)

            every { connectivity.activeNetwork } returns networkA
            assertEquals(apiData.isp, repository.widgetSnapshot(config.remoteDataConfig).ipApiData?.isp)
        }

    @Test fun `disconnected or replaced Wi-Fi never displays another network's cached data`() =
        runTest {
            val repository = repository()
            repository.refresh()
            every { connectivity.activeNetwork } returns null
            every { connectivity.allNetworks } returns emptyArray()
            assertEquals(RemoteNetworkData.empty, repository.widgetSnapshot(config.remoteDataConfig))

            every { connectivity.allNetworks } returns arrayOf(networkB)
            assertEquals(RemoteNetworkData.empty, repository.widgetSnapshot(config.remoteDataConfig))

            every { connectivity.activeNetwork } returns networkB
            assertEquals(RemoteNetworkData.empty, repository.widgetSnapshot(config.remoteDataConfig))
            repository.refresh()
            assertEquals(2, repository.widgetSnapshot(config.remoteDataConfig).publicIps.size)

            every { wifiManager.isWifiEnabled } returns false
            assertEquals(RemoteNetworkData.empty, repository.widgetSnapshot(config.remoteDataConfig))
        }

    @Test fun `ambiguous or unidentifiable Wi-Fi does not expose retained data`() =
        runTest {
            val repository = repository()
            repository.refresh()
            every { connectivity.activeNetwork } returns null
            every { connectivity.allNetworks } returns arrayOf(networkA, networkB)
            assertEquals(RemoteNetworkData.empty, repository.widgetSnapshot(config.remoteDataConfig))

            every { connectivity.allNetworks } returns arrayOf(networkA)
            every { connectivity.getNetworkCapabilities(networkA) } returns null
            assertEquals(RemoteNetworkData.empty, repository.widgetSnapshot(config.remoteDataConfig))
            assertTrue(repository.needsRecovery(config.remoteDataConfig))
        }

    @Test fun `cellular data is hidden and VPN data is visible only while its default remains verified`() =
        runTest {
            val repository = repository()
            every { connectivity.activeNetwork } returns networkB
            every { connectivity.allNetworks } returns arrayOf(networkA, networkB)
            every { connectivity.getNetworkCapabilities(networkB) } returns otherCapabilities(wifi = false)
            repository.refresh()
            assertEquals(RemoteNetworkData.empty, repository.widgetSnapshot(config.remoteDataConfig))

            every { connectivity.getNetworkCapabilities(networkB) } returns otherCapabilities(wifi = true, vpn = true)
            // The same Network handle changing transport cannot make cellular values VPN data.
            assertEquals(RemoteNetworkData.empty, repository.widgetSnapshot(config.remoteDataConfig))
            coEvery { api.fetch(any()) } returns failed
            coEvery { ips.fetch(any()) } returns mapOf(
                IpAddress.Version.V4 to failed,
                IpAddress.Version.V6 to failed
            )
            repository.refresh()
            assertEquals(RemoteNetworkData.empty, repository.widgetSnapshot(config.remoteDataConfig))

            coEvery { api.fetch(any()) } returns Result.success(apiData.copy(isp = "VPN ISP"))
            coEvery { ips.fetch(any()) } returns mapOf(
                IpAddress.Version.V4 to Result.success(ipv4),
                IpAddress.Version.V6 to Result.success(ipv6)
            )
            repository.refresh()
            assertEquals("VPN ISP", repository.widgetSnapshot(config.remoteDataConfig).ipApiData?.isp)

            every { connectivity.activeNetwork } returns null
            assertEquals(RemoteNetworkData.empty, repository.widgetSnapshot(config.remoteDataConfig))
        }

    @Test fun `mixed Wi-Fi and cellular transport cannot establish a Wi-Fi HTTP origin`() =
        runTest {
            val repository = repository()
            every { connectivity.activeNetwork } returns networkB
            every { connectivity.allNetworks } returns arrayOf(networkA, networkB)
            every { connectivity.getNetworkCapabilities(networkB) } returns mockk {
                every { hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true
                every { hasTransport(NetworkCapabilities.TRANSPORT_VPN) } returns false
                every { hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns true
                every { hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
            }

            repository.refresh()

            assertEquals(RemoteNetworkData.empty, repository.widgetSnapshot(config.remoteDataConfig))
            assertEquals(apiData.isp, repository.data.value.ipApiData?.isp)
        }

    @Test fun `retained Wi-Fi values obey changed remote requirements`() =
        runTest {
            val repository = repository()
            repository.refresh()
            every { connectivity.activeNetwork } returns null
            every { connectivity.allNetworks } returns arrayOf(networkA)
            val withoutPublicIp = config.withUpdatedPropertyEnablement(WifiProperty.PublicIp, false)
                .withUpdatedPropertyEnablement(WifiProperty.ISP, false)

            val retained = repository.widgetSnapshot(withoutPublicIp.remoteDataConfig)
            assertTrue(retained.publicIps.isEmpty())
            assertEquals("", retained.ipApiData?.isp)
            assertTrue(repository.needsRecovery(config.remoteDataConfig))
            assertTrue(repository.widgetSnapshot(config.remoteDataConfig).publicIps.isEmpty())
        }

    @Test fun `returning to the same Wi-Fi refreshes retained values when HTTP becomes available`() =
        runTest {
            val repository = repository()
            repository.refresh()
            every { connectivity.activeNetwork } returns null
            assertEquals(apiData.isp, repository.widgetSnapshot(config.remoteDataConfig).ipApiData?.isp)
            assertTrue(repository.needsRecovery(config.remoteDataConfig))

            every { connectivity.activeNetwork } returns networkA
            coEvery { api.fetch(any()) } returns Result.success(apiData.copy(isp = "Updated ISP"))
            assertEquals(RemoteRefreshResult.Complete, repository.refresh())
            assertEquals("Updated ISP", repository.widgetSnapshot(config.remoteDataConfig).ipApiData?.isp)
            assertEquals("Updated ISP", repository.data.value.ipApiData?.isp)
            assertFalse(repository.needsRecovery(config.remoteDataConfig))
        }

    @Test fun `retained Wi-Fi public IP versions follow configuration changes`() =
        runTest {
            val repository = repository()
            repository.refresh()
            every { connectivity.activeNetwork } returns null
            val onlyV4 = config.withUpdatedPropertyConfig(WifiProperty.PublicIp) {
                it.copy(settings = it.settings + (IpSetting.V6Enabled to false))
            }

            assertEquals(listOf(ipv4), repository.widgetSnapshot(onlyV4.remoteDataConfig).publicIps)
            assertEquals(listOf(ipv4), repository.widgetSnapshot(config.remoteDataConfig).publicIps)
            assertTrue(repository.needsRecovery(config.remoteDataConfig))
        }

    private fun otherCapabilities(wifi: Boolean, vpn: Boolean = false) =
        mockk<NetworkCapabilities> {
            every { hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns wifi
            every { hasTransport(NetworkCapabilities.TRANSPORT_VPN) } returns vpn
            every { hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns !wifi
            every { hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        }

    @Test fun `widget route change during request discards its result`() =
        runTest {
            val repository = repository()
            val release = CompletableDeferred<Unit>()
            coEvery { api.fetch(any()) } coAnswers {
                release.await()
                Result.success(apiData)
            }
            val refresh = async { repository.refreshForWidget() }
            testScheduler.runCurrent()
            every { connectivity.activeNetwork } returns networkB
            every { connectivity.allNetworks } returns arrayOf(networkB)
            release.complete(Unit)
            assertEquals(RemoteRefreshResult.NetworkUnavailable, refresh.await())
            assertTrue(repository.data.value.publicIps.isEmpty())
            assertEquals(RemoteNetworkData.empty, repository.widgetSnapshot(config.remoteDataConfig))
            assertTrue(repository.needsRecovery(config.remoteDataConfig))
        }

    @Test fun `configuration change during request discards old selection`() =
        runTest {
            val configs = MutableStateFlow(config)
            val repository = repository { configs.value }
            val release = CompletableDeferred<Unit>()
            coEvery { api.fetch(any()) } coAnswers {
                release.await()
                Result.success(apiData)
            }
            val refresh = async { repository.refresh() }
            testScheduler.runCurrent()
            configs.value = config.withUpdatedPropertyEnablement(WifiProperty.PublicIp, false)
            release.complete(Unit)
            assertEquals(RemoteRefreshResult.Incomplete, refresh.await())
            assertTrue(repository.data.value.publicIps.isEmpty())
            assertTrue(repository.needsRecovery(configs.value.remoteDataConfig))
        }

    @Test fun `unrelated configuration change during request retains results`() =
        runTest {
            val configs = MutableStateFlow(config)
            val repository = repository { configs.value }
            val release = CompletableDeferred<Unit>()
            coEvery { api.fetch(any()) } coAnswers {
                release.await()
                Result.success(apiData)
            }
            val refresh = async { repository.refresh() }
            testScheduler.runCurrent()
            configs.value = config.copy(appearance = config.appearance.copy(backgroundOpacity = 0.5f))
                .withUpdatedPropertyEnablement(WifiProperty.SSID, true)
                .withUpdatedPropertyPosition(0, 1)
            release.complete(Unit)
            assertEquals(RemoteRefreshResult.Complete, refresh.await())
            assertEquals(apiData.isp, repository.data.value.ipApiData?.isp)
            assertFalse(repository.needsRecovery(configs.value.remoteDataConfig))
        }

    @Test fun `discarded configuration result does not record public IP attempts`() =
        runTest {
            val onlyV4 = config.withUpdatedPropertyConfig(WifiProperty.PublicIp) {
                it.copy(settings = it.settings + (IpSetting.V6Enabled to false))
            }
            val configs = MutableStateFlow(onlyV4)
            val release = CompletableDeferred<Unit>()
            val requested = mutableListOf<Set<IpAddress.Version>>()
            val repository = repository { configs.value }
            coEvery { ips.fetch(any()) } coAnswers {
                val versions = firstArg<Set<IpAddress.Version>>()
                requested += versions
                if (requested.size == 2) release.await()
                versions.associateWith { if (it == IpAddress.Version.V4) Result.success(ipv4) else failed }
            }

            assertEquals(RemoteRefreshResult.Complete, repository.refresh())
            configs.value = config
            assertTrue(repository.needsRecovery(config.remoteDataConfig))
            val inFlight = async { repository.refresh() }
            testScheduler.runCurrent()
            configs.value = onlyV4
            assertFalse(repository.needsRecovery(onlyV4.remoteDataConfig))
            release.complete(Unit)
            assertEquals(RemoteRefreshResult.Complete, inFlight.await())

            configs.value = config
            assertTrue(repository.needsRecovery(config.remoteDataConfig))
            assertEquals(RemoteRefreshResult.Complete, repository.refresh(onlyIfMissing = true))
            assertEquals(
                listOf(
                    setOf(IpAddress.Version.V4),
                    setOf(IpAddress.Version.V4, IpAddress.Version.V6),
                    setOf(IpAddress.Version.V6)
                ),
                requested
            )
        }

    @Test fun `foreground full refresh satisfies multiple waiting widget callers`() =
        runTest { assertOverlappingFullRefreshes(firstForWidget = false) }

    @Test fun `widget full refresh satisfies multiple waiting foreground callers`() =
        runTest { assertOverlappingFullRefreshes(firstForWidget = true) }

    @Test fun `overlapping widget full refreshes share one fetch plan`() =
        runTest { assertOverlappingFullRefreshes(firstForWidget = true, waitingForWidget = true) }

    private suspend fun TestScope.assertOverlappingFullRefreshes(firstForWidget: Boolean, waitingForWidget: Boolean = !firstForWidget) {
        val repository = repository()
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        coEvery { api.fetch(any()) } coAnswers {
            entered.complete(Unit)
            release.await()
            Result.success(apiData)
        }
        // An unavailable family is compatible with successful completion of the full plan.
        coEvery { ips.fetch(any()) } returns mapOf(IpAddress.Version.V4 to Result.success(ipv4), IpAddress.Version.V6 to failed)
        val first = async { if (firstForWidget) repository.refreshForWidget() else repository.refresh() }
        entered.await()
        val waiting = List(3) {
            async(start = CoroutineStart.UNDISPATCHED) {
                if (waitingForWidget) repository.refreshForWidget() else repository.refresh()
            }
        }
        release.complete(Unit)
        (listOf(first) + waiting).forEach { assertEquals(RemoteRefreshResult.Complete, it.await()) }
        coVerify(exactly = 1) { api.fetch(any()) }
        coVerify(exactly = 1) { ips.fetch(any()) }
        assertEquals(apiData.isp, repository.data.value.ipApiData?.isp)
        assertEquals(listOf(ipv4), repository.data.value.publicIps)
    }

    @Test fun `non-overlapping full refreshes each execute HTTP`() =
        runTest {
            val repository = repository()
            repeat(3) { assertEquals(RemoteRefreshResult.Complete, repository.refresh()) }
            coVerify(exactly = 3) { api.fetch(any()) }
            coVerify(exactly = 3) { ips.fetch(any()) }
        }

    @Test fun `failed full retrieval does not suppress a waiting caller`() =
        runTest { assertFullRefreshRetried(apiResult = failed, publicIpResult = failed) }

    @Test fun `incomplete full retrieval does not suppress a waiting caller`() =
        runTest { assertFullRefreshRetried(apiResult = Result.success(apiData), publicIpResult = failed) }

    @Test fun `cached API values do not qualify a failed API retrieval`() =
        runTest { assertFullRefreshRetried(apiResult = failed, publicIpResult = Result.success(ipv4), primeCache = true) }

    @Test fun `cached IP values do not qualify an all-family failure`() =
        runTest { assertFullRefreshRetried(apiResult = Result.success(apiData), publicIpResult = failed, primeCache = true) }

    private suspend fun TestScope.assertFullRefreshRetried(
        apiResult: Result<IpApiData>,
        publicIpResult: Result<IpAddress>,
        primeCache: Boolean = false
    ) {
        val repository = repository()
        if (primeCache) repository.refresh()
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        var calls = 0
        coEvery { api.fetch(any()) } coAnswers {
            if (++calls == 1) {
                entered.complete(Unit)
                release.await()
                apiResult
            } else {
                Result.success(apiData)
            }
        }
        coEvery { ips.fetch(any()) } returnsMany listOf(
            mapOf(IpAddress.Version.V4 to publicIpResult, IpAddress.Version.V6 to failed),
            mapOf(IpAddress.Version.V4 to Result.success(ipv4), IpAddress.Version.V6 to failed)
        )
        val first = async { repository.refresh() }
        entered.await()
        val waiting = async(start = CoroutineStart.UNDISPATCHED) { repository.refreshForWidget() }
        release.complete(Unit)
        assertEquals(if (primeCache) RemoteRefreshResult.Complete else RemoteRefreshResult.Incomplete, first.await())
        assertEquals(RemoteRefreshResult.Complete, waiting.await())
        val expected = if (primeCache) 3 else 2
        coVerify(exactly = expected) { api.fetch(any()) }
        coVerify(exactly = expected) { ips.fetch(any()) }
    }

    @Test fun `cancelled retrieval leaves a waiting caller free to retrieve`() =
        runTest {
            val repository = repository()
            val entered = CompletableDeferred<Unit>()
            var calls = 0
            coEvery { api.fetch(any()) } coAnswers {
                if (++calls == 1) {
                    entered.complete(Unit)
                    awaitCancellation()
                }
                Result.success(apiData)
            }
            val first = async { repository.refresh() }
            entered.await()
            val waiting = async(start = CoroutineStart.UNDISPATCHED) { repository.refreshForWidget() }
            first.cancel()
            first.join()
            assertEquals(RemoteRefreshResult.Complete, waiting.await())
            coVerify(exactly = 2) { api.fetch(any()) }
        }

    @Test fun `cancelling a waiter does not cancel the active retrieval or other waiters`() =
        runTest {
            val repository = repository()
            val entered = CompletableDeferred<Unit>()
            val release = CompletableDeferred<Unit>()
            coEvery { api.fetch(any()) } coAnswers {
                entered.complete(Unit)
                release.await()
                Result.success(apiData)
            }
            val first = async { repository.refresh() }
            entered.await()
            val cancelled = async(start = CoroutineStart.UNDISPATCHED) { repository.refreshForWidget() }
            val waiting = async(start = CoroutineStart.UNDISPATCHED) { repository.refreshForWidget() }
            cancelled.cancel()
            release.complete(Unit)
            assertEquals(RemoteRefreshResult.Complete, first.await())
            assertEquals(RemoteRefreshResult.Complete, waiting.await())
            coVerify(exactly = 1) { api.fetch(any()) }
        }

    @Test fun `waiting widget caller checks Wi-Fi eligibility before reusing foreground completion`() =
        runTest {
            val repository = repository()
            val entered = CompletableDeferred<Unit>()
            val release = CompletableDeferred<Unit>()
            coEvery { api.fetch(any()) } coAnswers {
                entered.complete(Unit)
                release.await()
                Result.success(apiData)
            }
            val first = async { repository.refresh() }
            entered.await()
            val waiting = async(start = CoroutineStart.UNDISPATCHED) { repository.refreshForWidget() }
            every { wifiManager.isWifiEnabled } returns false
            release.complete(Unit)
            assertEquals(RemoteRefreshResult.Complete, first.await())
            assertEquals(RemoteRefreshResult.NetworkUnavailable, waiting.await())
            coVerify(exactly = 1) { api.fetch(any()) }
        }

    @Test fun `concurrent missing-data retrievals still deduplicate by completeness`() =
        runTest { assertOverlappingRecovery(firstOnlyIfMissing = true, waitingOnlyIfMissing = true, expectedFetches = 1) }

    @Test fun `missing-data completion does not suppress a waiting full retrieval`() =
        runTest { assertOverlappingRecovery(firstOnlyIfMissing = true, waitingOnlyIfMissing = false, expectedFetches = 2) }

    @Test fun `full completion satisfies waiting missing-data recovery`() =
        runTest { assertOverlappingRecovery(firstOnlyIfMissing = false, waitingOnlyIfMissing = true, expectedFetches = 1) }

    private suspend fun TestScope.assertOverlappingRecovery(
        firstOnlyIfMissing: Boolean,
        waitingOnlyIfMissing: Boolean,
        expectedFetches: Int
    ) {
        val repository = repository()
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        coEvery { api.fetch(any()) } coAnswers {
            entered.complete(Unit)
            release.await()
            Result.success(apiData)
        }
        val first = async { repository.refresh(firstOnlyIfMissing) }
        entered.await()
        val waiting = async(start = CoroutineStart.UNDISPATCHED) { repository.refreshForWidget(waitingOnlyIfMissing) }
        release.complete(Unit)
        assertEquals(RemoteRefreshResult.Complete, first.await())
        assertEquals(RemoteRefreshResult.Complete, waiting.await())
        coVerify(exactly = expectedFetches) { api.fetch(any()) }
        coVerify(exactly = expectedFetches) { ips.fetch(any()) }
    }

    @Test fun `cancellation propagates`() =
        runTest {
            val repository = repository()
            coEvery { api.fetch(any()) } throws CancellationException("cancelled")
            val result = runCatching { repository.refresh() }
            assertTrue(result.exceptionOrNull() is CancellationException)
        }

    @Test fun `observed network A to B to A during fetching prevents completion reuse`() =
        runTest {
            assertReconciliationDuringFetch(expectedFetches = 2) { repository, configs ->
                every { connectivity.activeNetwork } returns networkB
                repository.reconciledSnapshot(configs.value.remoteDataConfig)
                every { connectivity.activeNetwork } returns networkA
                repository.reconciledSnapshot(configs.value.remoteDataConfig)
            }
        }

    @Test fun `observed new network without capabilities during fetching prevents completion reuse`() =
        runTest {
            assertReconciliationDuringFetch(expectedFetches = 2) { repository, configs ->
                every { connectivity.activeNetwork } returns networkB
                every { connectivity.getNetworkCapabilities(networkB) } returns null
                repository.reconciledSnapshot(configs.value.remoteDataConfig)
                every { connectivity.activeNetwork } returns networkA
                repository.reconciledSnapshot(configs.value.remoteDataConfig)
            }
        }

    @Test fun `observed config A to B to A during fetching prevents completion reuse`() =
        runTest {
            assertReconciliationDuringFetch(expectedFetches = 2) { repository, configs ->
                configs.value = config.withUpdatedPropertyEnablement(WifiProperty.PublicIp, false)
                repository.reconciledSnapshot(configs.value.remoteDataConfig)
                configs.value = config
                repository.reconciledSnapshot(configs.value.remoteDataConfig)
            }
        }

    @Test fun `observed transport classification change and restoration during fetching prevents reuse`() =
        runTest {
            assertReconciliationDuringFetch(expectedFetches = 2) { repository, configs ->
                every { connectivity.getNetworkCapabilities(networkA) } returns otherCapabilities(wifi = true, vpn = true)
                repository.reconciledSnapshot(configs.value.remoteDataConfig)
                every { connectivity.getNetworkCapabilities(networkA) } returns capabilities
                repository.reconciledSnapshot(configs.value.remoteDataConfig)
            }
        }

    @Test fun `temporary route unavailability during fetching preserves completion reuse`() =
        runTest {
            assertReconciliationDuringFetch(expectedFetches = 1) { repository, configs ->
                every { connectivity.activeNetwork } returns null
                repository.reconciledSnapshot(configs.value.remoteDataConfig)
                every { connectivity.activeNetwork } returns networkA
                every { connectivity.getNetworkCapabilities(networkA) } returns null
                repository.reconciledSnapshot(configs.value.remoteDataConfig)
                every { connectivity.getNetworkCapabilities(networkA) } returns capabilities
                repository.reconciledSnapshot(configs.value.remoteDataConfig)
            }
        }

    @Test fun `unchanged reconciliation during fetching preserves completion reuse`() =
        runTest {
            assertReconciliationDuringFetch(expectedFetches = 1) { repository, configs ->
                repeat(3) {
                    repository.reconciledSnapshot(configs.value.remoteDataConfig)
                    repository.needsRecovery(configs.value.remoteDataConfig)
                    repository.widgetSnapshot(configs.value.remoteDataConfig)
                }
            }
        }

    /** Observations run before releasing the first HTTP plan, including before any completion exists. */
    private suspend fun TestScope.assertReconciliationDuringFetch(
        expectedFetches: Int,
        observe: (RemoteNetworkDataRepositoryImpl, MutableStateFlow<WidgetConfig>) -> Unit
    ) {
        val configs = MutableStateFlow(config)
        val repository = repository { configs.value }
        val entered = CompletableDeferred<Unit>()
        val releaseFirst = CompletableDeferred<Unit>()
        val releaseSecond = CompletableDeferred<Unit>()
        var calls = 0
        coEvery { api.fetch(any()) } coAnswers {
            val call = ++calls
            if (call == 1) {
                entered.complete(Unit)
                releaseFirst.await()
            } else {
                releaseSecond.await()
            }
            Result.success(apiData.copy(isp = "ISP $call"))
        }
        val first = async { repository.refresh() }
        entered.await()
        val waiting = async(start = CoroutineStart.UNDISPATCHED) { repository.refreshForWidget() }
        assertFalse(first.isCompleted)
        observe(repository, configs)
        assertFalse(first.isCompleted)
        releaseFirst.complete(Unit)
        assertEquals(RemoteRefreshResult.Complete, first.await())
        testScheduler.runCurrent()
        // A revision mismatch only prevents deduplication; the accepted first result still publishes.
        assertEquals("ISP 1", repository.data.value.ipApiData?.isp)
        coVerify(exactly = expectedFetches) { api.fetch(any()) }
        coVerify(exactly = expectedFetches) { ips.fetch(any()) }
        releaseSecond.complete(Unit)
        assertEquals(RemoteRefreshResult.Complete, waiting.await())
        assertEquals("ISP $expectedFetches", repository.data.value.ipApiData?.isp)
    }

    @Test fun `network change after completion prevents waiting caller reuse`() =
        runTest { assertNetworkCompletionInvalidated(returnToOriginal = false) }

    @Test fun `observed network A to B to A cannot resurrect a completion`() =
        runTest { assertNetworkCompletionInvalidated(returnToOriginal = true) }

    @Test fun `temporarily hidden same-network values remain eligible for full completion reuse`() =
        runTest {
            assertCompletionReconciledBeforeWaiter(expectedFetches = 1) { repository ->
                every { connectivity.activeNetwork } returns null
                assertEquals(RemoteNetworkData.empty, repository.reconciledSnapshot(config.remoteDataConfig))
                every { connectivity.activeNetwork } returns networkA
                assertEquals(apiData.isp, repository.reconciledSnapshot(config.remoteDataConfig).ipApiData?.isp)
                config
            }
        }

    private suspend fun TestScope.assertNetworkCompletionInvalidated(returnToOriginal: Boolean) {
        assertCompletionReconciledBeforeWaiter { repository ->
            every { connectivity.activeNetwork } returns networkB
            every { connectivity.allNetworks } returns arrayOf(networkB)
            repository.reconciledSnapshot(config.remoteDataConfig)
            if (returnToOriginal) {
                every { connectivity.activeNetwork } returns networkA
                every { connectivity.allNetworks } returns arrayOf(networkA)
                // This restores Wi-Fi values; restoration must not restore completion metadata.
                assertEquals(apiData.isp, repository.reconciledSnapshot(config.remoteDataConfig).ipApiData?.isp)
            }
            config
        }
    }

    @Test fun `transport classification change on the same network prevents reuse`() =
        runTest {
            assertCompletionReconciledBeforeWaiter { repository ->
                every { connectivity.getNetworkCapabilities(networkA) } returns otherCapabilities(wifi = true, vpn = true)
                every { connectivity.getNetworkCapabilities(networkB) } returns capabilities
                every { connectivity.allNetworks } returns arrayOf(networkA, networkB)
                repository.reconciledSnapshot(config.remoteDataConfig)
                config
            }
        }

    @Test fun `configuration change after completion prevents waiting caller reuse`() =
        runTest { assertConfigurationCompletionInvalidated(returnToOriginal = false) }

    @Test fun `observed config A to B to A cannot resurrect a completion`() =
        runTest { assertConfigurationCompletionInvalidated(returnToOriginal = true) }

    private suspend fun TestScope.assertConfigurationCompletionInvalidated(returnToOriginal: Boolean) {
        assertCompletionReconciledBeforeWaiter { repository ->
            // Change only public-IP requirements so the retained API group stays complete.
            val changed = config.withUpdatedPropertyEnablement(WifiProperty.PublicIp, false)
            repository.reconciledSnapshot(changed.remoteDataConfig)
            if (returnToOriginal) {
                repository.reconciledSnapshot(config.remoteDataConfig)
                config
            } else {
                changed
            }
        }
    }

    /** The waiting caller's first snapshot runs after publication and before its reuse check. */
    private suspend fun TestScope.assertCompletionReconciledBeforeWaiter(
        expectedFetches: Int = 2,
        transition: (RemoteNetworkDataRepositoryImpl) -> WidgetConfig
    ) {
        var snapshots = 0
        var currentConfig = config
        val initialized = CompletableDeferred<RemoteNetworkDataRepositoryImpl>()
        val repository = repository {
            if (++snapshots == 3) currentConfig = transition(initialized.await())
            currentConfig
        }
        initialized.complete(repository)
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        coEvery { api.fetch(any()) } coAnswers {
            entered.complete(Unit)
            release.await()
            Result.success(apiData)
        }
        val first = async { repository.refresh() }
        entered.await()
        val waiting = async(start = CoroutineStart.UNDISPATCHED) { repository.refreshForWidget() }
        release.complete(Unit)
        assertEquals(RemoteRefreshResult.Complete, first.await())
        assertEquals(RemoteRefreshResult.Complete, waiting.await())
        coVerify(exactly = expectedFetches) { api.fetch(any()) }
    }

    @Test fun `adding API requirements recovers API without retrieving complete public IPs again`() =
        runTest {
            var current = listOf(WifiProperty.Location, WifiProperty.IpCoordinates, WifiProperty.ISP, WifiProperty.ASN)
                .fold(config) { config, property -> config.withUpdatedPropertyEnablement(property, false) }
            val repository = repository { current }
            repository.refresh()
            current = current.withUpdatedPropertyEnablement(WifiProperty.ISP, true)
            assertEquals(RemoteRefreshResult.Complete, repository.refresh(onlyIfMissing = true))
            coVerify(exactly = 1) { api.fetch(any()) }
            coVerify(exactly = 1) { ips.fetch(any()) }
            assertEquals(setOf(ipv4, ipv6), repository.data.value.publicIps.toSet())
        }

    @Test fun `removing complete remote requirements performs no HTTP recovery`() =
        runTest {
            var current = config
            val repository = repository { current }
            repository.refresh()
            current = current.withUpdatedPropertyEnablement(WifiProperty.PublicIp, false)
            assertEquals(RemoteRefreshResult.Complete, repository.refresh(onlyIfMissing = true))
            current = listOf(WifiProperty.Location, WifiProperty.IpCoordinates, WifiProperty.ISP, WifiProperty.ASN)
                .fold(current) { config, property -> config.withUpdatedPropertyEnablement(property, false) }
            assertEquals(RemoteRefreshResult.Complete, repository.refresh(onlyIfMissing = true))
            coVerify(exactly = 1) { api.fetch(any()) }
            coVerify(exactly = 1) { ips.fetch(any()) }
            assertEquals(RemoteNetworkData.empty, repository.data.value)
        }

    @Test fun `removing one group still recovers another unsatisfied group`() =
        runTest {
            var current = config
            val repository = repository { current }
            coEvery { api.fetch(any()) } returnsMany listOf(failed, Result.success(apiData))
            assertEquals(RemoteRefreshResult.Incomplete, repository.refresh())
            current = current.withUpdatedPropertyEnablement(WifiProperty.PublicIp, false)
            assertEquals(RemoteRefreshResult.Complete, repository.refresh(onlyIfMissing = true))
            coVerify(exactly = 2) { api.fetch(any()) }
            coVerify(exactly = 1) { ips.fetch(any()) }
        }

    @Test fun `removing ASN retains ISP initialization and discards ASN without HTTP recovery`() =
        runTest {
            var current = ipApiConfig(WifiProperty.ISP, WifiProperty.ASN)
            val repository = repository { current }
            assertEquals(RemoteRefreshResult.Complete, repository.refresh())
            current = current.withUpdatedPropertyEnablement(WifiProperty.ASN, false)
            assertFalse(repository.needsRecovery(current.remoteDataConfig))
            assertEquals(RemoteRefreshResult.Complete, repository.refresh(onlyIfMissing = true))
            assertEquals(apiData.isp, repository.data.value.ipApiData?.isp)
            assertEquals(null, repository.data.value.ipApiData?.asn)
            coVerify(exactly = 1) { api.fetch(any()) }
            coVerify(exactly = 0) { ips.fetch(any()) }
        }

    @Test fun `removing Location retains ISP initialization despite changed location parameters`() =
        runTest {
            var current = ipApiConfig(WifiProperty.ISP, WifiProperty.Location)
            val repository = repository { current }
            coEvery { api.fetch(any()) } returns Result.success(apiData.copy(location = "Old location"))
            repository.refresh()
            assertEquals("Old location", repository.data.value.ipApiData?.location)
            current = current.withUpdatedPropertyEnablement(WifiProperty.Location, false)
                .withUpdatedPropertyConfig(WifiProperty.Location) { it.withUpdatedSetting(LocationParameter.Country, false) }
            assertFalse(repository.needsRecovery(current.remoteDataConfig))
            assertEquals(RemoteRefreshResult.Complete, repository.refresh(onlyIfMissing = true))
            assertEquals(apiData.isp, repository.data.value.ipApiData?.isp)
            assertEquals(null, repository.data.value.ipApiData?.location)
            coVerify(exactly = 1) { api.fetch(any()) }
        }

    @Test fun `changing parameters while Location remains enabled requires IP-API recovery`() =
        runTest {
            var current = ipApiConfig(WifiProperty.ISP, WifiProperty.Location)
            val repository = repository { current }
            coEvery { api.fetch(any()) } returns Result.success(apiData.copy(location = "Old location"))
            repository.refresh()
            current = current.withUpdatedPropertyConfig(WifiProperty.Location) { it.withUpdatedSetting(LocationParameter.Country, false) }
            assertTrue(repository.needsRecovery(current.remoteDataConfig))
            assertEquals(apiData.isp, repository.data.value.ipApiData?.isp)
            assertEquals(null, repository.data.value.ipApiData?.location)
            assertEquals(RemoteRefreshResult.Complete, repository.refresh(onlyIfMissing = true))
            coVerify(exactly = 2) { api.fetch(any()) }
            coVerify(exactly = 1) { api.fetch(current.remoteDataConfig.locationParameters) }
        }

    @Test fun `adding ASN to initialized ISP requires IP-API recovery`() =
        runTest {
            var current = ipApiConfig(WifiProperty.ISP)
            val repository = repository { current }
            repository.refresh()
            current = current.withUpdatedPropertyEnablement(WifiProperty.ASN, true)
            assertTrue(repository.needsRecovery(current.remoteDataConfig))
            assertEquals(RemoteRefreshResult.Complete, repository.refresh(onlyIfMissing = true))
            coVerify(exactly = 2) { api.fetch(any()) }
            assertEquals(apiData.asn, repository.data.value.ipApiData?.asn)
        }

    @Test fun `narrowing uninitialized IP-API requirements still requires recovery`() =
        runTest {
            var current = ipApiConfig(WifiProperty.ISP, WifiProperty.ASN)
            val repository = repository { current }
            coEvery { api.fetch(any()) } returnsMany listOf(failed, Result.success(apiData))
            assertEquals(RemoteRefreshResult.Incomplete, repository.refresh())
            current = current.withUpdatedPropertyEnablement(WifiProperty.ASN, false)
            assertTrue(repository.needsRecovery(current.remoteDataConfig))
            assertEquals(RemoteRefreshResult.Complete, repository.refresh(onlyIfMissing = true))
            coVerify(exactly = 2) { api.fetch(any()) }
            assertEquals(apiData.isp, repository.data.value.ipApiData?.isp)
            assertEquals(null, repository.data.value.ipApiData?.asn)
        }

    @Test fun `removing all IP-API requirements clears initialization before re-enabling ISP`() =
        runTest {
            var current = ipApiConfig(WifiProperty.ISP, WifiProperty.ASN)
            val repository = repository { current }
            repository.refresh()
            current = ipApiConfig()
            assertFalse(repository.needsRecovery(current.remoteDataConfig))
            assertEquals(RemoteRefreshResult.Complete, repository.refresh(onlyIfMissing = true))
            assertEquals(null, repository.data.value.ipApiData)
            coVerify(exactly = 1) { api.fetch(any()) }
            current = ipApiConfig(WifiProperty.ISP)
            assertTrue(repository.needsRecovery(current.remoteDataConfig))
            assertEquals(RemoteRefreshResult.Complete, repository.refresh(onlyIfMissing = true))
            coVerify(exactly = 2) { api.fetch(any()) }
        }

    @Test fun `explicit full refresh retrieves every current group after IP-API requirements narrow`() =
        runTest {
            var current = ipApiConfig(WifiProperty.ISP, WifiProperty.ASN)
                .withUpdatedPropertyEnablement(WifiProperty.PublicIp, true)
            val repository = repository { current }
            repository.refresh()
            current = current.withUpdatedPropertyEnablement(WifiProperty.ASN, false)
            assertFalse(repository.needsRecovery(current.remoteDataConfig))
            assertEquals(RemoteRefreshResult.Complete, repository.refresh())
            coVerify(exactly = 2) { api.fetch(any()) }
            coVerify(exactly = 2) { ips.fetch(current.remoteDataConfig.publicIpVersions) }
            assertEquals(null, repository.data.value.ipApiData?.asn)
        }

    @Test fun `narrowed initialized IP-API requirements leave failed public IPs independently recoverable`() =
        runTest {
            var current = ipApiConfig(WifiProperty.ISP, WifiProperty.ASN)
                .withUpdatedPropertyEnablement(WifiProperty.PublicIp, true)
            val repository = repository { current }
            coEvery { ips.fetch(any()) } returns current.remoteDataConfig.publicIpVersions.associateWith { failed }
            assertEquals(RemoteRefreshResult.Incomplete, repository.refresh())
            current = current.withUpdatedPropertyEnablement(WifiProperty.ASN, false)
            assertTrue(repository.needsRecovery(current.remoteDataConfig))
            coEvery { ips.fetch(any()) } returns
                mapOf(IpAddress.Version.V4 to Result.success(ipv4), IpAddress.Version.V6 to Result.success(ipv6))
            assertEquals(RemoteRefreshResult.Complete, repository.refresh(onlyIfMissing = true))
            coVerify(exactly = 1) { api.fetch(any()) }
            coVerify(exactly = 2) { ips.fetch(any()) }
        }

    private fun ipApiConfig(vararg properties: WifiProperty): WidgetConfig =
        listOf(WifiProperty.ISP, WifiProperty.ASN, WifiProperty.Location, WifiProperty.IpCoordinates, WifiProperty.PublicIp)
            .fold(config) { current, property -> current.withUpdatedPropertyEnablement(property, property in properties) }

    @Test fun `waiting recovery initializes requirements enabled after its invocation`() =
        runTest {
            val configs = MutableStateFlow(config.withUpdatedPropertyEnablement(WifiProperty.PublicIp, false))
            val repository = repository { configs.value }
            val entered = CompletableDeferred<Unit>()
            val release = CompletableDeferred<Unit>()
            coEvery { api.fetch(any()) } coAnswers {
                entered.complete(Unit)
                release.await()
                Result.success(apiData)
            }
            val first = async { repository.refresh() }
            entered.await()
            val recovery = async(start = CoroutineStart.UNDISPATCHED) { repository.refreshForWidget(onlyIfMissing = true) }
            configs.value = config
            release.complete(Unit)
            assertEquals(RemoteRefreshResult.Incomplete, first.await())
            assertEquals(RemoteRefreshResult.Complete, recovery.await())
            coVerify(exactly = 2) { api.fetch(any()) }
            coVerify(exactly = 1) { ips.fetch(any()) }
            assertFalse(repository.needsRecovery(config.remoteDataConfig))
        }

    @Test fun `waiting recovery does not retrieve requirements removed after its invocation`() =
        runTest {
            val configs = MutableStateFlow(config)
            val repository = repository { configs.value }
            val entered = CompletableDeferred<Unit>()
            val release = CompletableDeferred<Unit>()
            coEvery { api.fetch(any()) } coAnswers {
                entered.complete(Unit)
                release.await()
                Result.success(apiData)
            }
            val first = async { repository.refresh() }
            entered.await()
            val recovery = async(start = CoroutineStart.UNDISPATCHED) { repository.refreshForWidget(onlyIfMissing = true) }
            configs.value =
                listOf(WifiProperty.PublicIp, WifiProperty.ISP, WifiProperty.ASN, WifiProperty.Location, WifiProperty.IpCoordinates)
                    .fold(config) { current, property -> current.withUpdatedPropertyEnablement(property, false) }
            release.complete(Unit)
            assertEquals(RemoteRefreshResult.Complete, first.await())
            assertEquals(RemoteRefreshResult.Complete, recovery.await())
            coVerify(exactly = 1) { api.fetch(any()) }
            coVerify(exactly = 1) { ips.fetch(any()) }
            assertEquals(RemoteNetworkData.empty, repository.data.value)
        }
}
