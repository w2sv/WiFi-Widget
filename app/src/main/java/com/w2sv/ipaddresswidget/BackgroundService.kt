package com.w2sv.ipaddresswidget

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.IBinder

class BackgroundService: Service(){
    override fun onBind(intent: Intent?): IBinder? = null

    private val receiver by lazy {
        WifiStateChangeListener()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        registerReceiver(
            receiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(receiver)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        sendBroadcast(Intent(this, RestartBroadcastReceiver::class.java))
    }

    class RestartBroadcastReceiver: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            with(context!!){
                startService(Intent(this, BackgroundService::class.java))
            }
        }
    }
}