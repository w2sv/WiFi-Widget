package com.w2sv.wifiwidget

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.w2sv.widget.di.MutableWallpaperChangedFlow
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import slimber.log.i
import timber.log.Timber

@HiltAndroidApp
class Application : Application() {

    @MutableWallpaperChangedFlow
    @Inject
    lateinit var mutableWallpaperChangedFlow: MutableSharedFlow<Unit>

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        registerReceiver(wallpaperChangeReceiver, IntentFilter(Intent.ACTION_WALLPAPER_CHANGED))
        i { "Registered wallpaperChangeReceiver" }
    }

    private val wallpaperChangeReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                i { "wallpaperChangeReceiver.onReceive | Action=${intent.action}" }
                if (intent.action == Intent.ACTION_WALLPAPER_CHANGED) {
                    i { "Received Intent.ACTION_WALLPAPER_CHANGED" }

                    CoroutineScope(Dispatchers.Default).launch {
                        i { "Emitting on mutableWallpaperChangedFlow" }
                        mutableWallpaperChangedFlow.emit(Unit)
                    }
                }
            }
        }
    }
}
