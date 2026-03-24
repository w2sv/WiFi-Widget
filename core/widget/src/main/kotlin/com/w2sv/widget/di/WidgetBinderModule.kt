package com.w2sv.widget.di

import com.w2sv.widget.actions.WidgetActions
import com.w2sv.widget.actions.WidgetActionsImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
internal interface WidgetBinderModule {

    @Binds
    fun widgetActions(instance: WidgetActionsImpl): WidgetActions
}
