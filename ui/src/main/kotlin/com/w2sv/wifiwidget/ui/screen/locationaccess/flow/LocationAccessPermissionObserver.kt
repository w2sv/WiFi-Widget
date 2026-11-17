package com.w2sv.wifiwidget.ui.screen.locationaccess.flow

import android.Manifest
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.androidutils.content.hasPermission
import com.w2sv.composed.runtime.CollectFromFlow
import com.w2sv.wifiwidget.ui.location.LocationPermissionLevel
import com.w2sv.wifiwidget.ui.location.capability.access.LocationAccessCapability
import com.w2sv.wifiwidget.ui.screen.locationaccess.state.LocationAccessAvailability
import com.w2sv.wifiwidget.ui.util.requiresBackgroundLocationPermission
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun rememberLocationAccessAvailability(
    context: Context,
    capability: LocationAccessCapability,
    flowState: StateFlow<LocationAccessFlowState?>,
    onEvent: (LocationAccessEvent) -> LocationAccessEffect?
): LocationAccessAvailability {
    val locationServicesEnabled by capability.locationServicesEnabledFlow.collectAsStateWithLifecycle(
        initialValue = capability.locationServicesEnabled
    )
    val currentOnEvent by rememberUpdatedState(onEvent)
    val currentContext by rememberUpdatedState(context)
    // Reconcile the saved Settings stage when the entry resumes, including after process recreation.
    LifecycleResumeEffect(flowState) {
        reconcileSettingsReturn(currentContext, flowState.value, currentOnEvent)
        onPauseOrDispose { currentOnEvent(LeavingForSettings) }
    }

    return LocationAccessAvailability(
        permissionLevel = capability.permissionLevel,
        locationServicesEnabled = locationServicesEnabled
    )
}

private fun reconcileSettingsReturn(
    context: Context,
    flowState: LocationAccessFlowState?,
    onEvent: (LocationAccessEvent) -> LocationAccessEffect?
) {
    when (flowState) {
        is LocationAccessFlowState.ForegroundSettings -> if (flowState.hasLeftApp) {
            when (val permissionLevel = context.locationPermissionLevel()) {
                PreciseForeground, PreciseBackground ->
                    onEvent(LocationAccessEvent.ForegroundAccessAvailable(permissionLevel))
                else -> onEvent(ForegroundDenied)
            }
        }
        is LocationAccessFlowState.BackgroundSettings -> if (flowState.hasLeftApp) {
            if (context.locationPermissionLevel() == PreciseBackground) {
                onEvent(BackgroundAccessAvailable)
            } else {
                onEvent(BackgroundDenied)
            }
        }
        else -> Unit
    }
}

private fun Context.locationPermissionLevel(): LocationPermissionLevel {
    // Lifecycle reconciliation can precede Compose permission-state refresh after Settings.
    return LocationPermissionLevel.fromGrants(
        preciseGranted = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION),
        approximateGranted = hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION),
        backgroundAccessAvailable = !requiresBackgroundLocationPermission || hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    )
}

@Composable
internal fun ObserveLocationPermissionRequests(
    capability: LocationAccessCapability,
    availability: LocationAccessAvailability,
    flowState: LocationAccessFlowState?,
    onEvent: (LocationAccessEvent) -> LocationAccessEffect?
) {
    val currentOnEvent by rememberUpdatedState(onEvent)

    CollectFromFlow(capability.foregroundRequestResults) { granted ->
        if (!granted) currentOnEvent(ForegroundDenied)
    }
    CollectFromFlow(capability.backgroundRequestResults) { granted ->
        if (!granted) currentOnEvent(BackgroundDenied)
    }
    LaunchedEffect(flowState, availability.permissionLevel) {
        when (flowState) {
            is LocationAccessFlowState.RequestingForeground -> when (availability.permissionLevel) {
                PreciseForeground, PreciseBackground ->
                    currentOnEvent(LocationAccessEvent.ForegroundAccessAvailable(availability.permissionLevel))
                else -> Unit
            }
            is LocationAccessFlowState.RequestingBackground ->
                if (availability.permissionLevel == PreciseBackground) {
                    currentOnEvent(BackgroundAccessAvailable)
                }
            else -> Unit
        }
    }
}
