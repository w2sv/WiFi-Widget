package com.w2sv.networking.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
}