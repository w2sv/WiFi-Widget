package com.w2sv.networking.di

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.w2sv.androidutils.services.getConnectivityManager
import com.w2sv.androidutils.services.getWifiManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkingModule {

    @Provides
    @Singleton
    fun httpClient(): OkHttpClient =
        OkHttpClient()

    @Provides
    @Singleton
    fun wifiManager(@ApplicationContext context: Context): WifiManager =
        context.getWifiManager()

    @Provides
    @Singleton
    fun connectivityManager(@ApplicationContext context: Context): ConnectivityManager =
        context.getConnectivityManager()
}