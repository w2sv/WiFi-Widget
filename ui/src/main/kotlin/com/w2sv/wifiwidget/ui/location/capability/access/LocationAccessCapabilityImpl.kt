package com.w2sv.wifiwidget.ui.location.capability.access

import android.Manifest
import androidx.compose.runtime.Stable
import com.w2sv.augmentedpermissions.PermissionState
import com.w2sv.domain.location.IsLocationEnabled
import com.w2sv.wifiwidget.ui.location.LocationPermissionLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Stable
internal class LocationAccessCapabilityImpl(
    private val foregroundPermissionsState: PermissionState,
    private val backgroundPermissionState: PermissionState?,
    private val launchAppSettings: () -> Unit,
    private val locationEnabledProvider: IsLocationEnabled,
    private val openLocationServiceSettings: () -> Unit
) : LocationAccessCapability {
    override val permissionLevel: LocationPermissionLevel
        get() = LocationPermissionLevel.fromGrants(
            preciseGranted = Manifest.permission.ACCESS_FINE_LOCATION !in foregroundPermissionsState.revokedPermissions,
            approximateGranted = Manifest.permission.ACCESS_COARSE_LOCATION !in foregroundPermissionsState.revokedPermissions,
            backgroundAccessAvailable = backgroundPermissionState?.isGranted != false
        )
    override val foregroundRequestSuppressed: Boolean get() = foregroundPermissionsState.isLaunchingSuppressed
    override val locationServicesEnabled: Boolean get() = locationEnabledProvider()
    override val locationServicesEnabledFlow: Flow<Boolean> get() = locationEnabledProvider.flow

    override fun openLocationSettings() =
        openLocationServiceSettings()

    override val foregroundRequestResults: Flow<Boolean> get() = foregroundPermissionsState.grantedFromRequest
    override val backgroundRequestResults: Flow<Boolean> get() = backgroundPermissionState?.grantedFromRequest ?: emptyFlow()

    override fun launchBackgroundPermission() {
        backgroundPermissionState?.launchRequest()
    }

    override fun openAppSettings() =
        launchAppSettings()

    override fun requestPermission() {
        foregroundPermissionsState.launchRequest { launchAppSettings() }
    }
}
