package com.w2sv.wifiwidget.ui.screen.home.di

import com.w2sv.wifiwidget.ui.screen.home.model.gpsstatus.GpsStatusProvider
import com.w2sv.wifiwidget.ui.screen.home.model.gpsstatus.GpsStatusProviderImpl
import com.w2sv.wifiwidget.ui.screen.home.model.wifistate.WifiStateProvider
import com.w2sv.wifiwidget.ui.screen.home.model.wifistate.WifiStateProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@InstallIn(ViewModelComponent::class)
@Module
interface HomeScreenBinderModule {

    @Binds
    fun wifiStateProvider(instance: WifiStateProviderImpl): WifiStateProvider

    @Binds
    fun gpsStatusProvider(instance: GpsStatusProviderImpl): GpsStatusProvider
}
