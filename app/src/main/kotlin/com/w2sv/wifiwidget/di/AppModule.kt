package com.w2sv.wifiwidget.di

import android.content.Context
import android.location.LocationManager
import androidx.compose.material3.SnackbarVisuals
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MakeSnackbarVisualsFlow

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @MakeSnackbarVisualsFlow
    @Provides
    @Singleton
    fun mutableSnackbarVisualsFlow(): MutableSharedFlow<(Context) -> SnackbarVisuals> =
        MutableSharedFlow()

    @Provides
    @Singleton
    fun locationManager(@ApplicationContext context: Context): LocationManager =
        context.getSystemService(LocationManager::class.java)
}
