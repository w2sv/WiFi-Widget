package com.w2sv.widget.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WidgetPinSuccessFlow

@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class MutableWidgetPinSuccessFlow

@Module
@InstallIn(SingletonComponent::class)
internal object WidgetPinSuccessModule {

    @MutableWidgetPinSuccessFlow
    @Provides
    @Singleton
    fun provideMutableWidgetPinSuccessFlow(): MutableSharedFlow<Unit> =
        MutableSharedFlow()

    @WidgetPinSuccessFlow
    @Provides
    fun provideWidgetPinSuccessFlow(
        @MutableWidgetPinSuccessFlow flow: MutableSharedFlow<Unit>
    ): SharedFlow<Unit> =
        flow.asSharedFlow()

    @Provides
    fun provideEmitWidgetPinSuccess(
        @MutableWidgetPinSuccessFlow flow: MutableSharedFlow<Unit>
    ): EmitWidgetPinSuccess =
        EmitWidgetPinSuccess { flow.emit(Unit) }
}
