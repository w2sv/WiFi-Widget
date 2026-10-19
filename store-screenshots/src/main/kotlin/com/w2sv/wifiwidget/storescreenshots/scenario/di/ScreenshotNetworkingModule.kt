package com.w2sv.wifiwidget.storescreenshots.scenario.di

import com.w2sv.domain.networking.RemoteNetworkDataRepository
import com.w2sv.domain.networking.WifiStatusSource
import com.w2sv.domain.wifiproperty.WifiPropertyResolver
import com.w2sv.networking.di.NetworkingBinderModule
import dagger.Binds
import dagger.Module
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
}
