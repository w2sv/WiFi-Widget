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
import com.w2sv.domain.repository.WidgetConfigSource
import com.w2sv.networking.remotenetworkinfo.ip_api.IpApiDataFetcher
import com.w2sv.networking.remotenetworkinfo.public_ip.PublicIpFetcher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
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
    private val failed = Result.failure<Nothing>(IOException("unavailable"))
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
        coEvery { api.fetch(any()) } returns Result.success(apiData)
        coEvery { ips.fetch(any()) } returns mapOf(
            IpAddress.Version.V4 to Result.success(ipv4),
            IpAddress.Version.V6 to Result.success(ipv6)
        )
        val configSource = mockk<WidgetConfigSource> {
            coEvery { snapshot() } coAnswers { configProvider() }
        }
        return RemoteWifiDataRepositoryImpl(api, ips, configSource, connectivity)
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
                WifiProperty.IpGpsLocation,
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
            val onlyV6 = listOf(WifiProperty.Location, WifiProperty.IpGpsLocation, WifiProperty.ISP, WifiProperty.ASN)
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
                WifiProperty.IpGpsLocation,
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
            assertEquals(RemoteWifiData.empty, repository.reconciledSnapshot(config.remoteDataConfig))
            assertEquals(RemoteWifiData.empty, repository.data.value)
            every { connectivity.activeNetwork } returns networkA
            assertFalse(repository.needsRecovery(config.remoteDataConfig))
            assertEquals(2, repository.data.value.publicIps.size)
        }

    @Test fun `temporarily unavailable capabilities withhold and restore same-network data`() =
        runTest {
            val repository = repository()
            repository.refresh()
            every { connectivity.getNetworkCapabilities(networkA) } returns null
            assertEquals(RemoteWifiData.empty, repository.reconciledSnapshot(config.remoteDataConfig))
            assertEquals(RemoteWifiData.empty, repository.data.value)
            every { connectivity.getNetworkCapabilities(networkA) } returns capabilities
            assertEquals(2, repository.reconciledSnapshot(config.remoteDataConfig).publicIps.size)
            assertFalse(repository.needsRecovery(config.remoteDataConfig))
        }

    @Test fun `known new identity invalidates cache even before its capabilities are known`() =
        runTest {
            val repository = repository()
            repository.refresh()
            every { connectivity.activeNetwork } returns networkB
            every { connectivity.getNetworkCapabilities(networkB) } returns null
            assertEquals(RemoteWifiData.empty, repository.reconciledSnapshot(config.remoteDataConfig))
            every { connectivity.activeNetwork } returns networkA
            assertEquals(RemoteWifiData.empty, repository.reconciledSnapshot(config.remoteDataConfig))
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
            assertEquals(null, repository.reconciledSnapshot(config.remoteDataConfig).ipApiData)
            assertTrue(repository.reconciledSnapshot(config.remoteDataConfig).publicIps.isEmpty())
            assertTrue(repository.needsRecovery(config.remoteDataConfig))
        }

    @Test fun `route change during request discards its result`() =
        runTest {
            val repository = repository()
            val release = CompletableDeferred<Unit>()
            coEvery { api.fetch(any()) } coAnswers {
                release.await()
                Result.success(apiData)
            }
            val refresh = async { repository.refresh() }
            testScheduler.runCurrent()
            every { connectivity.activeNetwork } returns networkB
            release.complete(Unit)
            assertEquals(RemoteRefreshResult.NetworkUnavailable, refresh.await())
            assertTrue(repository.data.value.publicIps.isEmpty())
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

    @Test fun `concurrent refreshes cannot publish older results`() =
        runTest {
            val repository = repository()
            val release = CompletableDeferred<Unit>()
            var calls = 0
            coEvery { api.fetch(any()) } coAnswers {
                calls++
                if (calls == 1) release.await()
                Result.success(apiData.copy(isp = "ISP $calls"))
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
            coEvery { api.fetch(any()) } throws CancellationException("cancelled")
            val result = runCatching { repository.refresh() }
            assertTrue(result.exceptionOrNull() is CancellationException)
        }
}
