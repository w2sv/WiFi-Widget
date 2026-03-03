package com.w2sv.wifiwidget.ui.sharedstate.location.access_capability

import com.w2sv.wifiwidget.ui.sharedstate.location.OnLocationAccessGranted
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class PreviewLocationAccessCapability : LocationAccessCapability {
    override val foregroundPermissionsGranted: Boolean = true
    override val isBackgroundPermissionMissing: Boolean = true
    override val showForegroundRational: Boolean = false
    override val showBackgroundRational: Boolean = false
    override val grantEvents: Flow<OnLocationAccessGranted> = emptyFlow()

    override val isGpsEnabled: Boolean = true

    override fun onForegroundRationalProceed() {}
    override fun launchBackgroundPermission() {}

    override fun dismissBackgroundRational() {}
    override fun requestPermission(onGrant: OnLocationAccessGranted?) {}

    override suspend fun onPermissionGranted() {}
}
