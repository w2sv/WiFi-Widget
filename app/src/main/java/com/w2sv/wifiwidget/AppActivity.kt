package com.w2sv.wifiwidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleObserver

abstract class AppActivity : ComponentActivity() {

    abstract val lifecycleObservers: List<LifecycleObserver>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleObservers.forEach(lifecycle::addObserver)
    }
}