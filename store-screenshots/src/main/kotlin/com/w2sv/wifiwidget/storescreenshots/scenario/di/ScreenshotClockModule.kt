package com.w2sv.wifiwidget.storescreenshots.scenario.di

import com.w2sv.widget.di.WidgetClockModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [WidgetClockModule::class])
internal object ScreenshotClockModule {
    @Provides
    fun clock(): Clock =
        Clock.fixed(Instant.parse("2026-10-20T08:08:00Z"), ZoneId.of("Europe/Berlin"))
}
