package com.w2sv.datastore.di

import com.w2sv.datastore.repository.PermissionRepositoryImpl
import com.w2sv.datastore.repository.PreferencesRepositoryImpl
import com.w2sv.datastore.repository.WidgetRepositoryImpl
import com.w2sv.domain.repository.PermissionRepository
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.domain.repository.WidgetRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
interface RepositoryBinderModule {

    @Binds
    fun widgetRepository(impl: WidgetRepositoryImpl): WidgetRepository

    @Binds
    fun preferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository

    @Binds
    fun permissionRepository(impl: PermissionRepositoryImpl): PermissionRepository
}