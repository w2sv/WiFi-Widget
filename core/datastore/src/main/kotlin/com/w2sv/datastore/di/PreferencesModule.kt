package com.w2sv.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.w2sv.datastore.WidgetColoringProto
import com.w2sv.datastore.WidgetColoringProtoSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object PreferencesModule {

    @Singleton
    @Provides
    fun preferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            migrations = listOf(SharedPreferencesMigration(context, context.sharedPreferencesName)),
            produceFile = { context.preferencesDataStoreFile(context.sharedPreferencesName) },
        )

    @Provides
    @Singleton
    internal fun providesUserProtoDataStore(
        @ApplicationContext context: Context,
    ): DataStore<WidgetColoringProto> =
        DataStoreFactory.create(
            serializer = WidgetColoringProtoSerializer,
            corruptionHandler = ReplaceFileCorruptionHandler { WidgetColoringProto.getDefaultInstance() },
            produceFile = {
                context.dataStoreFile("widget_coloring.pb")
            }
        )
}

private val Context.sharedPreferencesName: String
    get() = packageName
