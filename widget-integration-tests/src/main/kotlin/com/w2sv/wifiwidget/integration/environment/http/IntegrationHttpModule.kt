package com.w2sv.wifiwidget.integration.environment.http

import com.w2sv.networking.di.HttpClientModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [HttpClientModule::class])
object IntegrationHttpModule {
    @Provides
    @Singleton
    fun httpClient(responses: TestRemoteHttpResponses): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(responses)
            .callTimeout(35, TimeUnit.SECONDS)
            .build()
}
