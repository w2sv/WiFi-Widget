package com.w2sv.datastore.preferences.di

import com.w2sv.datastore.preferences.LocationAccessRepositoryImpl
import com.w2sv.datastore.preferences.PreferencesRepositoryImpl
import com.w2sv.domain.location.LocationAccessRepository
import com.w2sv.domain.preferences.PreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
internal interface PreferencesBinderModule {

    @Binds
    fun preferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository

    @Binds
    fun permissionRepository(impl: LocationAccessRepositoryImpl): LocationAccessRepository
}
