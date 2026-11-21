package com.w2sv.wifiwidget.di

import com.w2sv.androidutils.content.intent
import com.w2sv.widget.ui.actions.LocationAccessActivityIntentProvider
import com.w2sv.wifiwidget.ui.LocationAccessActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object LocationAccessActivityModule {
    @Provides
    fun provideLocationAccessActivityIntentProvider(): LocationAccessActivityIntentProvider =
        LocationAccessActivityIntentProvider { context -> intent<LocationAccessActivity>(context) }
}
