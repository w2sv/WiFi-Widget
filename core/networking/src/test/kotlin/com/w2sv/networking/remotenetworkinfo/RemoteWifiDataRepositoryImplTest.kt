package com.w2sv.networking.remotenetworkinfo

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.w2sv.domain.model.networking.IpAddress
import com.w2sv.domain.model.networking.IpApiData
import com.w2sv.domain.model.networking.RemoteRefreshResult
import com.w2sv.domain.model.networking.RemoteWifiData
import com.w2sv.domain.model.widget.WidgetConfig
import com.w2sv.domain.model.wifiproperty.WifiProperty
import com.w2sv.domain.model.wifiproperty.settings.IpSetting
import com.w2sv.domain.repository.WidgetConfigReader
import com.w2sv.networking.remotenetworkinfo.ip_api.IpApiDataFetcher
import com.w2sv.networking.remotenetworkinfo.public_ip.PublicIpFetcher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.util.concurrent.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteWifiDataRepositoryImplTest {
    private val api = mockk<IpApiDataFetcher>()
    private val ips = mockk<PublicIpFetcher>()
    private val connectivity = mockk<ConnectivityManager>()
    private val networkA = mockk<Network>()
    private val networkB = mockk<Network>()
    private val capabilities = mockk<NetworkCapabilities> {
        every { hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true
        every { hasTransport(NetworkCapabilities.TRANSPORT_VPN) } returns false
        every { hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
    }
    private val config = WidgetConfig.default
        .withUpdatedPropertyEnablement(WifiProperty.PublicIp, true)
        .withUpdatedPropertyEnablement(WifiProperty.ISP, true)
    private val apiData = IpApiData(null, null, "UTC", "Example ISP", "AS123")
    private val ipv4 = mockk<IpAddress.V4> { every { version } returns IpAddress.Version.V4 }
    private val ipv6 = mockk<IpAddress.V6> { every { version } returns IpAddress.Version.V6 }

    private fun repository(configProvider: () -> WidgetConfig = { config }): RemoteWifiDataRepositoryImpl {
        every { connectivity.activeNetwork } returns networkA
        every { connectivity.getNetworkCapabilities(any()) } returns capabilities
        coEvery { api.fetchIfNecessary(any()) } returns FetchResult.Success(apiData)
        coEvery { ips.fetchIfNecessary(any()) } returns FetchResult.Success(
            mapOf(IpAddress.Version.V4 to FetchResult.Success(ipv4), IpAddress.Version.V6 to FetchResult.Success(ipv6))
        )
        val configReader = mockk<WidgetConfigReader> {
            coEvery { read() } coAnswers { configProvider() }
        }
        return RemoteWifiDataRepositoryImpl(api, ips, configReader, connectivity)
    }

    @Test fun `transient failures preserve each successful field`() =
        runTest {
            val repository = repository()
            repository.refresh()
            coEvery { api.fetchIfNecessary(any()) } returns FetchResult.Failed
            coEvery { ips.fetchIfNecessary(any()) } returns FetchResult.Success(
                mapOf(IpAddress.Version.V4 to FetchResult.Failed, IpAddress.Version.V6 to FetchResult.Success(ipv6))
            )
            assertEquals(RemoteRefreshResult.Complete, repository.refresh())
            assertEquals(apiData.isp, repository.data.value.ipApiData?.isp)
            assertEquals(setOf(ipv4, ipv6), repository.data.value.publicIps.toSet())
            assertFalse(repository.needsRecovery(config))
        }

    @Test fun `recovery waiting behind a completed refresh skips duplicate HTTP`() =
        runTest {
            val repository = repository()
            assertEquals(RemoteRefreshResult.Complete, repository.refresh())
            assertEquals(RemoteRefreshResult.Complete, repository.refresh(onlyIfMissing = true))
            coVerify(exactly = 1) { api.fetchIfNecessary(any()) }
            coVerify(exactly = 1) { ips.fetchIfNecessary(any()) }
        }

    @Test fun `recovery fetches only missing IP-API after public IP succeeds`() =
        runTest {
            val repository = repository()
            coEvery { api.fetchIfNecessary(any()) } returnsMany listOf(FetchResult.Failed, FetchResult.Success(apiData))
            assertEquals(RemoteRefreshResult.Incomplete, repository.refresh())
            assertEquals(RemoteRefreshResult.Complete, repository.refresh(onlyIfMissing = true))
            coVerify(exactly = 2) { api.fetchIfNecessary(any()) }
            coVerify(exactly = 1) { ips.fetchIfNecessary(any()) }
        }

    @Test fun `recovery fetches only missing public IP after IP-API succeeds`() =
        runTest {
            val repository = repository()
            coEvery { ips.fetchIfNecessary(any()) } returnsMany listOf(
                FetchResult.Success(mapOf(IpAddress.Version.V4 to FetchResult.Failed, IpAddress.Version.V6 to FetchResult.Failed)),
                FetchResult.Success(mapOf(IpAddress.Version.V4 to FetchResult.Success(ipv4)))
            )
            assertEquals(RemoteRefreshResult.Incomplete, repository.refresh())
            assertEquals(RemoteRefreshResult.Complete, repository.refresh(onlyIfMissing = true))
            coVerify(exactly = 1) { api.fetchIfNecessary(any()) }
            coVerify(exactly = 2) { ips.fetchIfNecessary(any()) }
        }

    @Test fun `unavailable IPv6 is attempted without repeated recovery`() =
        runTest {
            val repository = repository()
            coEvery { ips.fetchIfNecessary(any()) } returns FetchResult.Success(
                mapOf(
                    IpAddress.Version.V4 to FetchResult.Success(ipv4),
                    IpAddress.Version.V6 to FetchResult.Failed
                )
            )
            assertEquals(RemoteRefreshResult.Complete, repository.refresh())
            assertEquals(listOf(ipv4), repository.data.value.publicIps)
            assertFalse(repository.needsRecovery(config))
        }

    @Test fun `all initial failures leave both groups uninitialized`() =
        runTest {
            val repository = repository()
            coEvery { api.fetchIfNecessary(any()) } returns FetchResult.Failed
            coEvery { ips.fetchIfNecessary(any()) } returns FetchResult.Success(
                mapOf(IpAddress.Version.V4 to FetchResult.Failed, IpAddress.Version.V6 to FetchResult.Failed)
            )
            assertEquals(RemoteRefreshResult.Incomplete, repository.refresh())
            assertTrue(repository.needsRecovery(config))
            assertEquals(null, repository.snapshot(config).ipApiData)
            assertTrue(repository.data.value.publicIps.isEmpty())
        }

    @Test fun `initial failures recover after later success`() =
        runTest {
            val repository = repository()
            coEvery { api.fetchIfNecessary(any()) } returns FetchResult.Failed
            coEvery { ips.fetchIfNecessary(any()) } returns FetchResult.Failed
            assertEquals(RemoteRefreshResult.Incomplete, repository.refresh())
            coEvery { api.fetchIfNecessary(any()) } returns FetchResult.Success(apiData)
            coEvery { ips.fetchIfNecessary(any()) } returns FetchResult.Success(
                mapOf(IpAddress.Version.V4 to FetchResult.Success(ipv4))
            )
            assertEquals(RemoteRefreshResult.Complete, repository.refresh())
            assertFalse(repository.needsRecovery(config))
        }

    @Test fun `public IPv4 success does not hide missing IP-API data`() =
        runTest {
            val repository = repository()
            coEvery { api.fetchIfNecessary(any()) } returns FetchResult.Failed
            coEvery { ips.fetchIfNecessary(any()) } returns FetchResult.Success(
                mapOf(IpAddress.Version.V4 to FetchResult.Success(ipv4), IpAddress.Version.V6 to FetchResult.Failed)
            )
            assertEquals(RemoteRefreshResult.Incomplete, repository.refresh())
            assertEquals(listOf(ipv4), repository.data.value.publicIps)
            assertTrue(repository.needsRecovery(config))
        }

    @Test fun `only IPv6 failure remains recoverable`() =
        runTest {
            val onlyV6 = listOf(WifiProperty.Location, WifiProperty.IpGpsLocation, WifiProperty.ISP, WifiProperty.ASN)
                .fold(config) { current, property -> current.withUpdatedPropertyEnablement(property, false) }
                .withUpdatedPropertyConfig(WifiProperty.PublicIp) { propertyConfig ->
                    propertyConfig.copy(settings = propertyConfig.settings + (IpSetting.V4Enabled to false))
                }
            val repository = repository { onlyV6 }
            coEvery { ips.fetchIfNecessary(any()) } returns FetchResult.Success(
                mapOf(IpAddress.Version.V6 to FetchResult.Failed)
            )
            assertEquals(RemoteRefreshResult.Incomplete, repository.refresh())
            assertTrue(repository.needsRecovery(onlyV6))
        }

    @Test fun `disabled properties clear their cached values`() =
        runTest {
            val repository = repository()
            repository.refresh()
            val disabled = config.withUpdatedPropertyEnablement(WifiProperty.PublicIp, false)
                .withUpdatedPropertyEnablement(WifiProperty.ISP, false)
            repository.needsRecovery(disabled)
            assertTrue(repository.data.value.publicIps.isEmpty())
            assertEquals("", repository.data.value.ipApiData?.isp)
        }

    @Test fun `no enabled remote properties do not need recovery`() =
        runTest {
            val repository = repository()
            val disabled = listOf(
                WifiProperty.PublicIp,
                WifiProperty.Location,
                WifiProperty.IpGpsLocation,
                WifiProperty.ISP,
                WifiProperty.ASN
            ).fold(config) { current, property -> current.withUpdatedPropertyEnablement(property, false) }
            assertFalse(repository.needsRecovery(disabled))
        }

    @Test fun `new network invalidates and requires recovery`() =
        runTest {
            val repository = repository()
            repository.refresh()
            every { connectivity.activeNetwork } returns networkB
            assertTrue(repository.needsRecovery(config))
            assertEquals(emptyList<IpAddress>(), repository.data.value.publicIps)
        }

    @Test fun `unavailable identity withholds without discarding same network cache`() =
        runTest {
            val repository = repository()
            repository.refresh()
            every { connectivity.activeNetwork } returns null
            assertEquals(RemoteWifiData.empty, repository.snapshot(config))
            assertEquals(RemoteWifiData.empty, repository.data.value)
            every { connectivity.activeNetwork } returns networkA
            assertFalse(repository.needsRecovery(config))
            assertEquals(2, repository.data.value.publicIps.size)
        }

    @Test fun `temporarily unavailable capabilities withhold and restore same-network data`() =
        runTest {
            val repository = repository()
            repository.refresh()
            every { connectivity.getNetworkCapabilities(networkA) } returns null
            assertEquals(RemoteWifiData.empty, repository.snapshot(config))
            assertEquals(RemoteWifiData.empty, repository.data.value)
            every { connectivity.getNetworkCapabilities(networkA) } returns capabilities
            assertEquals(2, repository.snapshot(config).publicIps.size)
            assertFalse(repository.needsRecovery(config))
        }

    @Test fun `known new identity invalidates cache even before its capabilities are known`() =
        runTest {
            val repository = repository()
            repository.refresh()
            every { connectivity.activeNetwork } returns networkB
            every { connectivity.getNetworkCapabilities(networkB) } returns null
            assertEquals(RemoteWifiData.empty, repository.snapshot(config))
            every { connectivity.activeNetwork } returns networkA
            assertEquals(RemoteWifiData.empty, repository.snapshot(config))
            assertTrue(repository.needsRecovery(config))
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
            assertTrue(repository.snapshot(config).publicIps.isEmpty())
            assertTrue(repository.needsRecovery(config))
            coEvery { api.fetchIfNecessary(any()) } returns FetchResult.Success(apiData.copy(isp = "VPN ISP"))
            assertEquals(RemoteRefreshResult.Complete, repository.refresh())
            assertEquals("VPN ISP", repository.snapshot(config).ipApiData?.isp)
        }

    @Test fun `default route changing to cellular cannot reuse Wi-Fi egress data`() =
        runTest {
            val repository = repository()
            repository.refresh()
            val cellular = mockk<NetworkCapabilities> {
                every { hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns true
                every { hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
            }
            every { connectivity.activeNetwork } returns networkB
            every { connectivity.getNetworkCapabilities(networkB) } returns cellular
            assertEquals(null, repository.snapshot(config).ipApiData)
            assertTrue(repository.snapshot(config).publicIps.isEmpty())
            assertTrue(repository.needsRecovery(config))
        }

    @Test fun `route change during request discards its result`() =
        runTest {
            val repository = repository()
            val release = CompletableDeferred<Unit>()
            coEvery { api.fetchIfNecessary(any()) } coAnswers {
                release.await()
                FetchResult.Success(apiData)
            }
            val refresh = async { repository.refresh() }
            testScheduler.runCurrent()
            every { connectivity.activeNetwork } returns networkB
            release.complete(Unit)
            assertEquals(RemoteRefreshResult.NetworkUnavailable, refresh.await())
            assertTrue(repository.data.value.publicIps.isEmpty())
            assertTrue(repository.needsRecovery(config))
        }

    @Test fun `configuration change during request discards old selection`() =
        runTest {
            val configs = MutableStateFlow(config)
            val repository = repository { configs.value }
            val release = CompletableDeferred<Unit>()
            coEvery { api.fetchIfNecessary(any()) } coAnswers {
                release.await()
                FetchResult.Success(apiData)
            }
            val refresh = async { repository.refresh() }
            testScheduler.runCurrent()
            configs.value = config.withUpdatedPropertyEnablement(WifiProperty.PublicIp, false)
            release.complete(Unit)
            assertEquals(RemoteRefreshResult.Incomplete, refresh.await())
            assertTrue(repository.data.value.publicIps.isEmpty())
            assertTrue(repository.needsRecovery(configs.value))
        }

    @Test fun `concurrent refreshes cannot publish older results`() =
        runTest {
            val repository = repository()
            val release = CompletableDeferred<Unit>()
            var calls = 0
            coEvery { api.fetchIfNecessary(any()) } coAnswers {
                calls++
                if (calls == 1) release.await()
                FetchResult.Success(apiData.copy(isp = "ISP $calls"))
            }
            val first = async { repository.refresh() }
            testScheduler.runCurrent()
            val second = async { repository.refresh() }
            release.complete(Unit)
            first.await()
            second.await()
            assertEquals("ISP 2", repository.data.value.ipApiData?.isp)
        }

    @Test fun `cancellation propagates`() =
        runTest {
            val repository = repository()
            coEvery { api.fetchIfNecessary(any()) } throws CancellationException("cancelled")
            val result = runCatching { repository.refresh() }
            assertTrue(result.exceptionOrNull() is CancellationException)
        }
}
