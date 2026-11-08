package com.w2sv.widget.di

import android.content.Context
import com.w2sv.widget.refreshing.WifiWidgetWorkScheduler
import com.w2sv.widget.ui.state.WidgetPresentationStateProvider
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Exposes Hilt-managed dependencies to Glance components that cannot use direct injection.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface GlanceWidgetEntryPoint {
    fun stateProvider(): WidgetPresentationStateProvider
    fun workScheduler(): WifiWidgetWorkScheduler
}

internal fun glanceWidgetEntryPoint(context: Context): GlanceWidgetEntryPoint =
    EntryPointAccessors.fromApplication(context, GlanceWidgetEntryPoint::class.java)
