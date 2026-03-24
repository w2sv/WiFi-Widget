package com.w2sv.networking.di

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.w2sv.androidutils.service.systemService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

@InstallIn(SingletonComponent::class)
@Module
internal object NetworkingModule {

    @Provides
    @Singleton
    fun httpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .callTimeout(5, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun wifiManager(@ApplicationContext context: Context): WifiManager =
        context.systemService()

    @Provides
    @Singleton
    fun connectivityManager(@ApplicationContext context: Context): ConnectivityManager =
        context.systemService()

    @Provides
    @Singleton
    fun json(): Json =
        Json { ignoreUnknownKeys = true }
}
