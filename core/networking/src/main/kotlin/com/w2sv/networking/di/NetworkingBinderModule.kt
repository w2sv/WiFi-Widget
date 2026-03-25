package com.w2sv.networking.di

import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewDataProvider
import com.w2sv.domain.repository.RemoteWifiDataRepository
import com.w2sv.networking.propertyviewdata.WifiPropertyViewDataProviderImpl
import com.w2sv.networking.remotenetworkinfo.RemoteWifiDataRepositoryImpl
import com.w2sv.networking.wifistatus.monitor.WifiStatusMonitor
import com.w2sv.networking.wifistatus.monitor.WifiStatusMonitorImpl
import com.w2sv.networking.wifistatus.provider.WifiStatusProvider
import com.w2sv.networking.wifistatus.provider.WifiStatusProviderImpl
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
    fun wifiStatusProvider(instance: WifiStatusProviderImpl): WifiStatusProvider

    @Binds
    fun remoteWifiDataRepository(instance: RemoteWifiDataRepositoryImpl): RemoteWifiDataRepository
}
