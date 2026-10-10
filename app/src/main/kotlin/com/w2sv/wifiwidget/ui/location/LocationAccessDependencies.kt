package com.w2sv.wifiwidget.ui.location

import com.w2sv.augmentedpermissions.PermissionRequestHistory
import com.w2sv.common.di.AppIoScope
import com.w2sv.common.utils.IsLocationEnabled
import com.w2sv.domain.repository.PermissionRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Provides location status and persisted permission histories to the UI. */
class LocationAccessDependencies @Inject constructor(
    val isLocationEnabled: IsLocationEnabled,
    permissionRepository: PermissionRepository,
    @AppIoScope scope: CoroutineScope
) {
    val requestHistory = PermissionRequestHistory(
        wasRequestLaunchedBefore = permissionRepository.locationAccessPermissionRequested,
        recordRequestLaunched = { scope.launch { permissionRepository.locationAccessPermissionRequested.save(true) } }
    )

    val rationalHistory = LocationAccessRationalHistory(
        wasShownBefore = permissionRepository.locationAccessPermissionRationalShown,
        recordWasShown = { scope.launch { permissionRepository.locationAccessPermissionRationalShown.save(true) } }
    )
}
