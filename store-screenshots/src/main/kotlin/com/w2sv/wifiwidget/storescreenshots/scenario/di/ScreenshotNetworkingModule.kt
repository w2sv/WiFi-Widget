package com.w2sv.wifiwidget.storescreenshots.scenario.di

import com.w2sv.domain.networking.NetworkAvailabilitySnapshot
import com.w2sv.domain.networking.NetworkAvailabilitySource
import com.w2sv.domain.networking.RemoteNetworkDataRepository
import com.w2sv.domain.networking.WifiStatusSource
import com.w2sv.domain.wifiproperty.WifiPropertyResolver
import com.w2sv.networking.di.NetworkingBinderModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [NetworkingBinderModule::class])
abstract class ScreenshotNetworkingModule {
    @Binds
    abstract fun wifiStatusSource(impl: ScreenshotWifiStatusSource): WifiStatusSource

    @Binds
    abstract fun wifiPropertyResolver(impl: ScreenshotWifiPropertyResolver): WifiPropertyResolver

    @Binds
    abstract fun remoteNetworkDataRepository(impl: ScreenshotRemoteNetworkDataRepository): RemoteNetworkDataRepository

    companion object {
        /** Screenshot fixtures represent accessible Wi-Fi without Battery Saver restrictions. */
        @Provides
        fun networkAvailabilitySource(): NetworkAvailabilitySource =
            NetworkAvailabilitySource {
                NetworkAvailabilitySnapshot(
                    isBatterySaverEnabled = false,
                    hasUsableDefaultNetwork = true,
                    hasLinkProperties = true,
                    isWifiConnected = true,
                    defaultNetworkIdentity = 1L,
                    isDirectWifiDefaultNetwork = true
                )
            }
    }
}
