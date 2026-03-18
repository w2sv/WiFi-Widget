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
import slimber.log.d

@ViewModelScoped
class LocationEnabledProviderImpl @Inject constructor(@ApplicationContext context: Context) : LocationEnabledProvider {
    override val isEnabled: Flow<Boolean> = context.isLocationEnabledFlow()
}

/**
 * Callback flow registering a [LocationManager.MODE_CHANGED_ACTION] broadcast receiver.
 * [LocationManager.MODE_CHANGED_ACTION] only contains the boolean extra [LocationManager.EXTRA_LOCATION_ENABLED] starting from Android R,
 * so we use broadcasts only as trigger to compute the value via [LocationManager.isLocationEnabledCompat].
 */
private fun Context.isLocationEnabledFlow(): Flow<Boolean> =
    callbackFlow {
        val locationManager = systemService<LocationManager>()
        val receiver = broadcastReceiver { _, _ ->
            val isLocationEnabled = locationManager.isLocationEnabledCompat()
            d { "Received LocationManager.MODE_CHANGED_ACTION broadcast; Emitting isLocationEnabled=$isLocationEnabled" }
            trySend(isLocationEnabled)
        }

        registerReceiver(receiver, IntentFilter(LocationManager.MODE_CHANGED_ACTION))
        awaitClose { unregisterReceiver(receiver) }
    }
