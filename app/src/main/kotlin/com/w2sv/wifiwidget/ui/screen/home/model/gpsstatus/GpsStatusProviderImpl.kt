package com.w2sv.wifiwidget.ui.screen.home.model.gpsstatus

import android.content.Context
import android.content.IntentFilter
import android.location.LocationManager
import com.w2sv.androidutils.location.isLocationEnabledCompat
import com.w2sv.androidutils.service.systemService
import com.w2sv.common.utils.broadcastReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@ViewModelScoped
class GpsStatusProviderImpl @Inject constructor(@ApplicationContext context: Context) : GpsStatusProvider {
    override val isEnabled: Flow<Boolean> = context.isGpsEnabledFlow()
}

private fun Context.isGpsEnabledFlow(): Flow<Boolean> =
    callbackFlow {
        val locationManager = systemService<LocationManager>()
        val receiver = broadcastReceiver { _, _ -> trySend(locationManager.isLocationEnabledCompat()) }

        registerReceiver(receiver, IntentFilter(LocationManager.MODE_CHANGED_ACTION))
        awaitClose { unregisterReceiver(receiver) }
    }
