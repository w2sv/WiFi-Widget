package com.w2sv.widget.di

import com.w2sv.domain.widget.WidgetActions
import com.w2sv.domain.widget.WidgetStatus
import com.w2sv.widget.actions.WidgetActionsImpl
import com.w2sv.widget.status.WidgetStatusImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
internal interface WidgetBinderModule {

    @Binds
    fun widgetActions(instance: WidgetActionsImpl): WidgetActions

    @Binds
    fun widgetStatus(instance: WidgetStatusImpl): WidgetStatus
}
