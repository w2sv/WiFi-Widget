package com.w2sv.common.di

import android.content.Context
import android.content.res.Resources
import android.location.LocationManager
import com.w2sv.androidutils.location.isLocationEnabledCompat
import com.w2sv.androidutils.service.systemService
import com.w2sv.common.utils.IsGpsEnabled
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
internal object CommonModule {

    @Provides
    fun resources(@ApplicationContext context: Context): Resources =
        context.resources

    @Provides
    fun isGpsEnabled(@ApplicationContext context: Context): IsGpsEnabled {
        val locationManager = context.systemService<LocationManager>()
        return IsGpsEnabled { locationManager.isLocationEnabledCompat() }
    }
}
