package com.w2sv.widget.di

import android.appwidget.AppWidgetManager
import android.content.ClipboardManager
import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class MutableWidgetPinSuccessFlow

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WidgetPinSuccessFlow

@InstallIn(SingletonComponent::class)
@Module
internal object WidgetModule {

    @Provides
    @Singleton
    fun workManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun appWidgetManager(@ApplicationContext context: Context): AppWidgetManager =
        AppWidgetManager.getInstance(context)

    @Provides
    @Singleton
    fun clipboardManager(@ApplicationContext context: Context): ClipboardManager =
        context.getSystemService(ClipboardManager::class.java)

    @MutableWidgetPinSuccessFlow
    @Provides
    @Singleton
    fun mutableWidgetPinSuccessFlow(): MutableSharedFlow<Unit> =
        MutableSharedFlow()

    @WidgetPinSuccessFlow
    @Provides
    fun widgetPinSuccessFlow(@MutableWidgetPinSuccessFlow mutableWidgetPinSuccessFlow: MutableSharedFlow<Unit>): SharedFlow<Unit> =
        mutableWidgetPinSuccessFlow.asSharedFlow()
}
