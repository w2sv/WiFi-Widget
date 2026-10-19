package com.w2sv.networking.di

import com.w2sv.domain.networking.RemoteNetworkDataRepository
import com.w2sv.domain.networking.WifiStatusSource
import com.w2sv.domain.wifiproperty.WifiPropertyResolver
import com.w2sv.networking.propertyresolution.WifiPropertyResolverImpl
import com.w2sv.networking.remotenetworkdata.RemoteNetworkDataRepositoryImpl
import com.w2sv.networking.wifistatus.WifiStatusSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
internal interface NetworkingBinderModule {

    @Binds
    fun wifiPropertyResolver(impl: WifiPropertyResolverImpl): WifiPropertyResolver

    @Binds
    fun wifiStatusSource(instance: WifiStatusSourceImpl): WifiStatusSource

    @Binds
    fun remoteNetworkDataRepository(instance: RemoteNetworkDataRepositoryImpl): RemoteNetworkDataRepository
}
