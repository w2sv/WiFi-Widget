package com.w2sv.wifiwidget.ui.location

import com.w2sv.augmentedpermissions.PermissionRequestHistory
import com.w2sv.common.di.qualifier.AppIoScope
import com.w2sv.domain.location.IsLocationEnabled
import com.w2sv.domain.location.LocationAccessRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Supplies shared location services, widget recovery, and persisted permission-request history to the UI. */
class LocationAccessDependencies @Inject constructor(
    val isLocationEnabled: IsLocationEnabled,
    private val locationAccessRepository: LocationAccessRepository,
    @AppIoScope private val scope: CoroutineScope,
    val widgetLocationRecoveryObserver: WidgetLocationRecoveryObserver
) {
    val requestHistory = PermissionRequestHistory(
        wasRequestLaunchedBefore = locationAccessRepository.permissionRequested,
        recordRequestLaunched = { scope.launch { locationAccessRepository.recordPermissionRequested() } }
    )
}
