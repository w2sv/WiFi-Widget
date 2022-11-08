package com.w2sv.ipaddresswidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startService(Intent(applicationContext, BackgroundService::class.java))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()

        with(getSystemService(AppWidgetManager::class.java)){
            if (isRequestPinAppWidgetSupported){
                requestPinAppWidget(
                    ComponentName(this@MainActivity, IPAddressWidget::class.java),
                    null,
                    null
                )
            }
        }
    }
}