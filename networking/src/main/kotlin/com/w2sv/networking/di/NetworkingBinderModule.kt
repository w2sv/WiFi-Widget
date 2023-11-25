package com.w2sv.networking.di

import com.w2sv.domain.model.WidgetWifiProperty
import com.w2sv.networking.WidgetWifiPropertyValueGetterImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
interface NetworkingBinderModule {

    @Binds
    fun widgetWifiPropertyValueGetter(instance: WidgetWifiPropertyValueGetterImpl): WidgetWifiProperty.ValueGetter
}