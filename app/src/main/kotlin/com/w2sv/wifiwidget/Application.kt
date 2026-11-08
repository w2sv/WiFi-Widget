package com.w2sv.wifiwidget

import android.app.Application
import android.content.res.Configuration as AndroidConfiguration
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.w2sv.widget.RemoteDataWidgetObserver
import com.w2sv.widget.preview.WidgetPreviewPublisher
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import timber.log.Timber

@HiltAndroidApp
class Application :
    Application(),
    Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var remoteDataWidgetObserver: RemoteDataWidgetObserver

    @Inject
    lateinit var widgetPreviewPublisher: WidgetPreviewPublisher

    override val workManagerConfiguration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        remoteDataWidgetObserver.initialize()
        widgetPreviewPublisher.publishIfOutdated()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun onConfigurationChanged(newConfig: AndroidConfiguration) {
        super.onConfigurationChanged(newConfig)
        widgetPreviewPublisher.publishIfOutdated()
    }
}
