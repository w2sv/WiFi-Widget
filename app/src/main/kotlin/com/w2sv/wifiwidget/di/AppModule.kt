package com.w2sv.wifiwidget.di

import android.content.Context
import android.location.LocationManager
import com.w2sv.androidutils.service.systemService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Provides
    @Singleton
    fun locationManager(@ApplicationContext context: Context): LocationManager =
        context.systemService()
}
