package com.w2sv.widget.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock

// For manipulatable clock injection from screenshot generation tasks
@Module
@InstallIn(SingletonComponent::class)
object WidgetClockModule {
    @Provides
    fun clock(): Clock =
        Clock.systemDefaultZone()
}
