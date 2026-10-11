package com.w2sv.networking.di

import com.w2sv.domain.model.wifiproperty.viewdata.WifiPropertyViewDataProvider
import com.w2sv.domain.repository.RemoteWifiDataRepository
import com.w2sv.networking.propertyviewdata.WifiPropertyViewDataProviderImpl
import com.w2sv.networking.remotenetworkinfo.RemoteWifiDataRepositoryImpl
import com.w2sv.networking.wifistatus.WifiStatusSource
import com.w2sv.networking.wifistatus.WifiStatusSourceImpl
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
    fun wifiStatusSource(instance: WifiStatusSourceImpl): WifiStatusSource

    @Binds
    fun remoteWifiDataRepository(instance: RemoteWifiDataRepositoryImpl): RemoteWifiDataRepository
}
