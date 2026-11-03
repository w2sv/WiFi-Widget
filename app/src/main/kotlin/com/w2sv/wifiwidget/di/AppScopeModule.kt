package com.w2sv.wifiwidget.di

import com.w2sv.common.di.qualifier.AppDefaultScope
import com.w2sv.common.di.qualifier.AppIoScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

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
