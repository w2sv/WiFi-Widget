package com.w2sv.wifiwidget.ui.di

import android.content.Context
import androidx.compose.material3.SnackbarVisuals
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Singleton

typealias MutableSharedSnackbarVisualsFlow = MutableSharedFlow<(Context) -> SnackbarVisuals>

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Provides
    @Singleton
    fun mutableSharedSnackbarVisuals(): MutableSharedSnackbarVisualsFlow =
        MutableSharedFlow()
}