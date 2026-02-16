package com.w2sv.networking.di

import com.w2sv.domain.model.WifiViewData
import com.w2sv.networking.WidgetWifiPropertyViewDataFactoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
internal interface NetworkingBinderModule {

    @Binds
    fun wifiViewDataFactory(instance: WidgetWifiPropertyViewDataFactoryImpl): WifiViewData.Factory
}
