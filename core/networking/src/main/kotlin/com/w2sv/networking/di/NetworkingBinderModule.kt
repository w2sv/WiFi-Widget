package com.w2sv.networking.di

import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewDataProvider
import com.w2sv.domain.repository.RemoteNetworkInfoRepository
import com.w2sv.networking.fetching.remote_network_info.RemoteNetworkInfoRepositoryImpl
import com.w2sv.networking.propertyviewdata.WifiPropertyViewDataProviderImpl
import com.w2sv.networking.wifistatus.WifiStatusMonitor
import com.w2sv.networking.wifistatus.WifiStatusMonitorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
internal interface NetworkingBinderModule {

    @Binds
    fun wifiViewDataProvider(instance: WifiPropertyViewDataProviderImpl): WifiPropertyViewDataProvider

    @Binds
    fun wifiStatusMonitor(instance: WifiStatusMonitorImpl): WifiStatusMonitor

    @Binds
    fun remoteNetworkInfoRepository(instance: RemoteNetworkInfoRepositoryImpl): RemoteNetworkInfoRepository
}
