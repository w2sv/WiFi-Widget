package com.w2sv.wifiwidget.ui.sharedstate.location.access_capability

import com.w2sv.wifiwidget.ui.sharedstate.location.OnLocationAccessGrant

class PreviewLocationAccessCapability : LocationAccessCapability {
    override val foregroundPermissionsGranted: Boolean = true
    override val isBackgroundPermissionMissing: Boolean = true
    override val showForegroundRational: Boolean = false
    override val showBackgroundRational: Boolean = false

    override fun onForegroundRationalProceed() {}
    override fun maybeShowBackgroundRational() {}
    override fun launchBackgroundPermission() {}
    override fun dismissBackgroundRational() {}
    override fun requestPermission(onGrant: OnLocationAccessGrant?) {}
    override fun consumeOnGrantAction(): OnLocationAccessGrant? = null

    override val isGpsEnabled: Boolean = true
}
