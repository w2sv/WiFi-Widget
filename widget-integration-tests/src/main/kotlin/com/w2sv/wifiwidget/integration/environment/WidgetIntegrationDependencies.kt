package com.w2sv.wifiwidget.integration.environment

import com.w2sv.domain.networking.RemoteNetworkDataRepository
import com.w2sv.domain.networking.WifiStatusSource
import com.w2sv.domain.widget.WidgetActions
import com.w2sv.domain.widget.WidgetConfigDataSource
import com.w2sv.domain.widget.WidgetConfigSource
import com.w2sv.widget.RemoteDataWidgetObserver
import com.w2sv.widget.ui.properties.WifiPropertyRemoteViewsFactory
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetIntegrationDependencies {
    fun widgetConfigDataSource(): WidgetConfigDataSource
    fun widgetConfigSource(): WidgetConfigSource
    fun widgetActions(): WidgetActions
    fun remoteNetworkDataRepository(): RemoteNetworkDataRepository
    fun remoteDataWidgetObserver(): RemoteDataWidgetObserver
    fun wifiStatusSource(): WifiStatusSource
    fun wifiPropertyRemoteViewsFactory(): WifiPropertyRemoteViewsFactory
}
