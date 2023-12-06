package com.w2sv.wifiwidget.ui.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LaunchBackgroundLocationAccessPermissionRequest

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Provides
    @Singleton
    @LaunchBackgroundLocationAccessPermissionRequest
    fun launchBackgroundLocationAccessPermissionRequest(): MutableSharedFlow<Unit> =
        MutableSharedFlow()
}