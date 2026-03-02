package com.w2sv.wifiwidget.ui.sharedstate.location.permission_capability

import com.w2sv.wifiwidget.ui.sharedstate.location.OnLocationAccessGrant

interface LocationPermissionCapability {

    // ========= Permission State =========

    val foregroundPermissionsGranted: Boolean
    val isBackgroundPermissionMissing: Boolean

    // ========= Foreground Rational =========

    val showForegroundRational: Boolean
    fun onForegroundRationalProceed()

    // ========= Background Rational =========

    val showBackgroundRational: Boolean
    fun maybeShowBackgroundRational()
    fun launchBackgroundPermission()
    fun dismissBackgroundRational()

    // ========= Grant Actions =========

    fun requestPermission(onGrant: OnLocationAccessGrant?)
    fun consumeOnGrantAction(): OnLocationAccessGrant?
}
