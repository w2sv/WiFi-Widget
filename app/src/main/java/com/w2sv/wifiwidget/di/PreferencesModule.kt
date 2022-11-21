package com.w2sv.wifiwidget.di

import android.content.Context
import com.w2sv.typedpreferences.extensions.appPreferences
import com.w2sv.wifiwidget.preferences.BooleanPreferences
import com.w2sv.wifiwidget.preferences.WidgetPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object PreferencesModule{

    @Provides
    @Singleton
    fun provideBooleanPreferences(@ApplicationContext context: Context): BooleanPreferences =
        BooleanPreferences(context.appPreferences())

    @Provides
    @Singleton
    fun provideWidgetPreferences(@ApplicationContext context: Context): WidgetPreferences =
        WidgetPreferences(context.appPreferences())
}