package com.w2sv.widget.di

import com.w2sv.widget.refreshing.WifiWidgetWorkScheduler
import com.w2sv.widget.ui.WifiWidgetStateProvider
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface GlanceWidgetEntryPoint {
    fun contentProvider(): WifiWidgetStateProvider

    fun workScheduler(): WifiWidgetWorkScheduler
}
