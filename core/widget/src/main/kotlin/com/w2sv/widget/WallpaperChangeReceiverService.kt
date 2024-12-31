package com.w2sv.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.w2sv.androidutils.UnboundService
import slimber.log.i

@Suppress("DEPRECATION")
internal class WallpaperChangeReceiverService : UnboundService() {

    private val wallpaperChangeReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                i { "wallpaperChangeReceiver.onReceive | Action=${intent.action}" }
                if (intent.action == Intent.ACTION_WALLPAPER_CHANGED) {
                    i { "Received Intent.ACTION_WALLPAPER_CHANGED" }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        i { "onCreate" }

        registerReceiver(wallpaperChangeReceiver, IntentFilter(Intent.ACTION_WALLPAPER_CHANGED))
        i { "Registered wallpaperChangeReceiver" }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        i { "onStartCommand" }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        i { "onDestroy" }

        unregisterReceiver(wallpaperChangeReceiver)
        i { "Unregistered wallpaperChangeReceiver" }
    }

    companion object {
        fun start(context: Context) {
            context.startService(intent(context))
        }

        fun stop(context: Context) {
            context.stopService(intent(context))
        }

        private fun intent(context: Context): Intent =
            Intent(context, WallpaperChangeReceiverService::class.java)
    }
}
