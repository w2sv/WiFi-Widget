package com.w2sv.common.di

import android.content.Context
import android.content.res.Resources
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
internal object CommonModule {

    @Provides
    fun resources(@ApplicationContext context: Context): Resources =
        context.resources
}
