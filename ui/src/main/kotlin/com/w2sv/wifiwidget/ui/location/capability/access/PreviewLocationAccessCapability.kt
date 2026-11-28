package com.w2sv.wifiwidget.ui.location.capability.access

import com.w2sv.domain.location.LocationPermissionLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

internal class PreviewLocationAccessCapability : LocationAccessCapability {
    override val permissionLevel = LocationPermissionLevel.PreciseForeground
    override val foregroundRequestSuppressed: Boolean = false
    override val foregroundRequestResults: Flow<Boolean> = emptyFlow()
    override val backgroundRequestResults: Flow<Boolean> = emptyFlow()

    override val locationServicesEnabled: Boolean = true
    override val locationServicesEnabledFlow: Flow<Boolean> = flowOf(true)
    override fun openLocationSettings() {}
    override fun openAppSettings() {}

    override fun launchBackgroundPermission() {}

    override fun requestPermission() {}
}
