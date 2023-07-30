package com.w2sv.widget.properties

import android.content.Context
import com.w2sv.common.data.storage.WidgetConfigurationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object WifiPropertiesModule {

    @Singleton
    @Provides
    fun provideWifiPropertyViewsFactory(
        @ApplicationContext context: Context,
        widgetConfigurationRepository: WidgetConfigurationRepository
    ): WifiPropertyViewsFactory =
        WifiPropertyViewsFactory(context, widgetConfigurationRepository)
}