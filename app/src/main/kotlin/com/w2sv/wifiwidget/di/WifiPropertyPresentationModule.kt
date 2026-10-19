package com.w2sv.wifiwidget.di

import android.content.res.Resources
import com.w2sv.domain.wifiproperty.WifiPropertyResolver
import com.w2sv.domain.wifiproperty.presentation.WifiPropertyPresentationProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object WifiPropertyPresentationModule {

    @Provides
    fun wifiPropertyPresentationProvider(resolver: WifiPropertyResolver, resources: Resources): WifiPropertyPresentationProvider =
        WifiPropertyPresentationProvider(
            resolver = resolver,
            resources = resources
        )
}
