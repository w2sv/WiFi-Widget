package com.w2sv.wifiwidget.di

import android.content.Context
import android.location.LocationManager
import android.os.PowerManager
import com.w2sv.androidutils.location.isLocationEnabledCompat
import com.w2sv.androidutils.service.systemService
import com.w2sv.domain.location.IsLocationEnabled
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object SharedSystemServiceModule {

    @Provides
    @Singleton
    fun powerManager(@ApplicationContext context: Context): PowerManager =
        context.systemService()

    @Provides
    fun isLocationEnabled(@ApplicationContext context: Context): IsLocationEnabled {
        val locationManager = context.systemService<LocationManager>()
        return IsLocationEnabled { locationManager.isLocationEnabledCompat() }
    }
}
