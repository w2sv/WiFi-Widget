package com.w2sv.common.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PackageName

@InstallIn(SingletonComponent::class)
@Module
object CommonModule {

    @PackageName
    @Provides
    fun packageName(@ApplicationContext context: Context): String =
        context.packageName
}