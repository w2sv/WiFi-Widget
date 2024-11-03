package com.w2sv.networking.di

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.w2sv.androidutils.getConnectivityManager
import com.w2sv.androidutils.getWifiManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient

@InstallIn(SingletonComponent::class)
@Module
internal object NetworkingModule {

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
