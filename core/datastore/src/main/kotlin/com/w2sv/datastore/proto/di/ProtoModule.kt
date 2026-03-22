package com.w2sv.datastore.proto.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import com.w2sv.datastore.WidgetColoringProto
import com.w2sv.datastore.WidgetConfigProto
import com.w2sv.datastore.proto.WidgetConfigProtoSerializer
import com.w2sv.datastore.proto.migration.WidgetColoringProtoSerializer
import com.w2sv.datastore.proto.migration.WidgetConfigMigration
import com.w2sv.datastore.proto.migration.defaultWidgetColoringProto
import com.w2sv.domain.repository.WidgetConfigDataSource
import com.w2sv.domain.repository.WidgetConfigFlow
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
internal object ProtoModule {

    @Provides
    @Singleton
    fun widgetConfigDataStore(@ApplicationContext context: Context, migration: WidgetConfigMigration): DataStore<WidgetConfigProto> =
        DataStoreFactory.create(
            serializer = WidgetConfigProtoSerializer,
            corruptionHandler = ReplaceFileCorruptionHandler { WidgetConfigProtoSerializer.defaultValue },
            produceFile = { context.dataStoreFile("widget_config.pb") },
            migrations = listOf(migration)
        )

    @Provides
    fun widgetConfigFlow(widgetConfigDataSource: WidgetConfigDataSource): WidgetConfigFlow =
        widgetConfigDataSource.config

    @Provides
    internal fun widgetColoringDataStore(@ApplicationContext context: Context): DataStore<WidgetColoringProto> =
        DataStoreFactory.create(
            serializer = WidgetColoringProtoSerializer(),
            corruptionHandler = ReplaceFileCorruptionHandler { defaultWidgetColoringProto() },
            produceFile = { context.widgetColoringProtoFile() }
        )
}
