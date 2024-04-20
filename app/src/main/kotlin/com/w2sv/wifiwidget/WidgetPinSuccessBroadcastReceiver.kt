package com.w2sv.wifiwidget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.w2sv.wifiwidget.di.WidgetPinSuccessFlow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WidgetPinSuccessBroadcastReceiver : BroadcastReceiver() {

    @Inject
    @WidgetPinSuccessFlow
    lateinit var widgetPinSuccessFlow: MutableSharedFlow<Unit>

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onReceive(p0: Context?, p1: Intent?) {
        scope.launch { widgetPinSuccessFlow.emit(Unit) }
    }

    companion object {
        const val REQUEST_CODE = 1447
    }
}