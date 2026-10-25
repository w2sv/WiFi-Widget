package com.w2sv.networking.wifiproperty

import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.w2sv.domain.location.IsLocationEnabled
import com.w2sv.domain.networking.RemoteNetworkData
import com.w2sv.domain.wifiproperty.ResolvedWifiProperty
import com.w2sv.domain.wifiproperty.WifiProperty
import com.w2sv.domain.wifiproperty.WifiPropertyResolver
import com.w2sv.domain.wifiproperty.settings.IpSetting
import com.w2sv.kotlinutils.coroutines.runCatchingCancellable
import com.w2sv.networking.connectivity.systemIpAddresses
import com.w2sv.networking.connectivity.wifiNetworkSnapshot
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import slimber.log.e

internal class WifiPropertyResolverImpl @Inject constructor(
    private val wifiManager: WifiManager,
    private val connectivityManager: ConnectivityManager,
    private val isLocationEnabled: IsLocationEnabled
) : WifiPropertyResolver {

    override suspend fun invoke(
        enabledProperties: List<WifiProperty>,
        enabledIpSettings: (WifiProperty.IpProperty) -> List<IpSetting>,
        remoteNetworkData: RemoteNetworkData
    ): List<ResolvedWifiProperty> =
        withContext(Dispatchers.Default) {
            val wifiSnapshot = captureWifiSnapshot(remoteNetworkData)

            enabledProperties.map { property ->
                ensureActive()

                val values = runCatchingCancellable {
                    property.resolve(
                        wifiSnapshot,
                        enabledIpSettings
                    )
                }
                    .getOrElse { throwable ->
                        e { "Failed resolving property $property; $throwable" }
                        throw CancellationException()
                    }

                ResolvedWifiProperty(property = property, values = values)
            }
        }

    /** Collects the local link once so all IP properties in this pass use the same addresses. */
    @Suppress("DEPRECATION")
    private fun captureWifiSnapshot(remoteNetworkData: RemoteNetworkData): WifiSnapshot {
        val defaultNetwork = connectivityManager.activeNetwork
        val defaultCapabilities = defaultNetwork?.let(connectivityManager::getNetworkCapabilities)
        // NAT64 and classified addresses must come from the same identified Wi-Fi link.
        val linkProperties = connectivityManager.wifiNetworkSnapshot(
            wifiEnabled = wifiManager.isWifiEnabled,
            defaultNetwork = defaultNetwork,
            defaultCapabilities = defaultCapabilities
        ).linkProperties
        return WifiSnapshot(
            connectionInfo = wifiManager.connectionInfo,
            dhcpInfo = wifiManager.dhcpInfo,
            linkProperties = linkProperties,
            publicIps = remoteNetworkData.publicIps,
            systemIps = linkProperties.systemIpAddresses(),
            ipApiData = remoteNetworkData.ipApiData,
            isLocationEnabled = isLocationEnabled()
        )
    }
}
