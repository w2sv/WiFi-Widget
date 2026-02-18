package com.w2sv.datastore.proto.di

import com.w2sv.datastore.proto.WidgetConfigDataSourceImpl
import com.w2sv.domain.repository.WidgetConfigDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
internal interface ProtoBinderModule {

    @Binds
    fun widgetConfigDataSource(impl: WidgetConfigDataSourceImpl): WidgetConfigDataSource
}
