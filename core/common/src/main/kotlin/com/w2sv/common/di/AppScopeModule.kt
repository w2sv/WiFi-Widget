package com.w2sv.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppDefaultScope

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppIoScope

@InstallIn(SingletonComponent::class)
@Module
internal object AppScopeModule {

    @AppDefaultScope
    @Singleton
    @Provides
    fun appDefaultScope(): CoroutineScope =
        CoroutineScope(Dispatchers.Default + SupervisorJob())

    @AppIoScope
    @Singleton
    @Provides
    fun appIoScope(): CoroutineScope =
        CoroutineScope(Dispatchers.IO + SupervisorJob())
}
