package com.w2sv.wifiwidget.ui.di

import android.content.Context
import android.location.LocationManager
import androidx.compose.material3.SnackbarVisuals
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Provides
    @Singleton
    fun locationManager(@ApplicationContext context: Context): LocationManager =
        context.getSystemService(LocationManager::class.java)
}