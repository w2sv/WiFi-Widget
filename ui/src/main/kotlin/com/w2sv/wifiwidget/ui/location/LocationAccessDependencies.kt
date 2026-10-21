package com.w2sv.wifiwidget.ui.location

import com.w2sv.augmentedpermissions.PermissionRequestHistory
import com.w2sv.common.di.AppIoScope
import com.w2sv.common.utils.IsLocationEnabled
import com.w2sv.domain.permission.LocationAccessRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Provides location status and persisted permission histories to the UI. */
class LocationAccessDependencies @Inject constructor(
    val isLocationEnabled: IsLocationEnabled,
    locationAccessRepository: LocationAccessRepository,
    @AppIoScope scope: CoroutineScope
) {
    val requestHistory = PermissionRequestHistory(
        wasRequestLaunchedBefore = locationAccessRepository.permissionRequested,
        recordRequestLaunched = { scope.launch { locationAccessRepository.recordPermissionRequested() } }
    )

    val rationalHistory = LocationAccessRationalHistory(
        wasShownBefore = locationAccessRepository.rationalShown,
        recordWasShown = { scope.launch { locationAccessRepository.recordRationalShown() } }
    )
}
