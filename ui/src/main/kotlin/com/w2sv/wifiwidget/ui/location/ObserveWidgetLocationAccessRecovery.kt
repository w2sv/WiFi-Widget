package com.w2sv.wifiwidget.ui.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.w2sv.domain.location.IsLocationEnabled
import com.w2sv.wifiwidget.ui.location.capability.access.LocationAccessCapability
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

/** Permission and services emissions are triggers only; every observation rereads Android state. */
@Composable
internal fun ObserveWidgetLocationAccessRecovery(
    capability: LocationAccessCapability,
    isLocationEnabled: IsLocationEnabled,
    observer: WidgetLocationRecoveryObserver
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(context, lifecycleOwner, capability, isLocationEnabled, observer) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            fun observeCurrentAvailability() {
                observer.observe(LocationAccessAvailability(context.locationPermissionLevel(), isLocationEnabled()))
            }

            // Also recheck on every resume, before either reactive source needs to emit.
            observeCurrentAvailability()
            merge(
                snapshotFlow { capability.permissionLevel }.map {},
                capability.locationServicesEnabledFlow.map {}
            )
                .collect { observeCurrentAvailability() }
        }
    }
}
