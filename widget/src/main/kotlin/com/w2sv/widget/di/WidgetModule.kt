package com.w2sv.widget.di

import android.content.Context
import androidx.work.WorkManager
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.common.data.model.WidgetAppearance
import com.w2sv.common.data.model.WidgetRefreshing
import com.w2sv.common.data.model.WifiProperty
import com.w2sv.common.data.storage.WidgetConfigurationRepository
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

    @Provides
    fun widgetRefreshing(widgetConfigurationRepository: WidgetConfigurationRepository): WidgetRefreshing =
        widgetConfigurationRepository.refreshing.getValueSynchronously()

    @Provides
    fun widgetAppearance(widgetConfigurationRepository: WidgetConfigurationRepository): WidgetAppearance =
        widgetConfigurationRepository.appearance.getValueSynchronously()

    @Provides
    fun setWifiProperties(widgetConfigurationRepository: WidgetConfigurationRepository): Set<WifiProperty> =
        widgetConfigurationRepository.getSetWifiProperties()
}