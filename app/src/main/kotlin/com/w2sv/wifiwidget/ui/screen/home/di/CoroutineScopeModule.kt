package com.w2sv.wifiwidget.ui.screen.home.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(ViewModelComponent::class)
object CoroutineScopeModule {

    @Provides
    @ViewModelScoped
    fun provideViewModelCoroutineScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
}
