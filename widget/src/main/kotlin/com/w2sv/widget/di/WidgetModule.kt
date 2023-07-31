package com.w2sv.widget.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object WidgetModule {

    @Provides
    fun workManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}