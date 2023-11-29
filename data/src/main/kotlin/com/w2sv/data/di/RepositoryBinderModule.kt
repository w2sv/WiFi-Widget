package com.w2sv.data.di

import com.w2sv.data.repository.PreferencesRepositoryImpl
import com.w2sv.data.repository.WidgetRepositoryImpl
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
}