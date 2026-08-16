package com.w2sv.wifiwidget

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.w2sv.common.di.AppDefaultScope
import com.w2sv.widget.WidgetPreviewPublisher
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltAndroidApp
class Application :
    Application(),
    Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var widgetPreviewPublisher: WidgetPreviewPublisher

    @Inject
    @AppDefaultScope
    lateinit var appScope: CoroutineScope

    override val workManagerConfiguration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        appScope.launch {
            widgetPreviewPublisher.publishIfOutdated()
        }
    }
}
