package com.w2sv.wifiwidget.ui.screen.home.di

import com.w2sv.wifiwidget.ui.screen.home.model.WifiStateProvider
import com.w2sv.wifiwidget.ui.screen.home.model.WifiStateProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@InstallIn(ViewModelComponent::class)
@Module
interface HomeScreenBinderModule {

    @Binds
    fun wifiStateProvider(instance: WifiStateProviderImpl): WifiStateProvider
}
