package com.w2sv.wifiwidget.location

import android.content.Context
import android.content.IntentFilter
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.w2sv.androidutils.location.isLocationEnabledCompat
import com.w2sv.common.android.broadcastReceiver
import com.w2sv.domain.location.IsLocationEnabled
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/** Observes system location mode only while [flow] is collected. */
class AndroidLocationEnabled(private val context: Context, private val locationManager: LocationManager) : IsLocationEnabled {
    override fun invoke(): Boolean =
        locationManager.isLocationEnabledCompat()

    override val flow: Flow<Boolean> = callbackFlow {
        // The broadcast is only a trigger; the actual mode comes from LocationManager on every API level.
        val receiver = broadcastReceiver { _, _ -> trySend(invoke()) }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(LocationManager.MODE_CHANGED_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        trySend(invoke())
        awaitClose { context.unregisterReceiver(receiver) }
    }.distinctUntilChanged()
}
