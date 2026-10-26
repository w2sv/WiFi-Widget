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
import com.w2sv.networking.connectivity.linkProperties
import com.w2sv.networking.connectivity.systemIpAddresses
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

    @Suppress("DEPRECATION")
    override suspend fun invoke(
        enabledProperties: List<WifiProperty>,
        enabledIpSettings: (WifiProperty.IpProperty) -> List<IpSetting>,
        remoteNetworkData: RemoteNetworkData
    ): List<ResolvedWifiProperty> =
        withContext(Dispatchers.Default) {
            val wifiSnapshot = WifiSnapshot(
                connectionInfo = wifiManager.connectionInfo,
                dhcpInfo = wifiManager.dhcpInfo,
                linkProperties = connectivityManager.linkProperties,
                publicIps = remoteNetworkData.publicIps,
                systemIps = connectivityManager.systemIpAddresses(),
                ipApiData = remoteNetworkData.ipApiData,
                isLocationEnabled = isLocationEnabled()
            )

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
}
