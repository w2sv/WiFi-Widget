package com.w2sv.wifiwidget.ui.sharedstate.location.permission_capability

import com.w2sv.wifiwidget.ui.sharedstate.location.OnLocationAccessGranted
import kotlinx.coroutines.flow.Flow

interface LocationPermissionCapability {

    // ========= Permission State =========

    val foregroundPermissionsGranted: Boolean
    val isBackgroundPermissionMissing: Boolean

    // ========= Foreground Rational =========

    val showForegroundRational: Boolean
    fun onForegroundRationalProceed()

    // ========= Background Rational =========

    val showBackgroundRational: Boolean
    fun launchBackgroundPermission()
    fun dismissBackgroundRational()

    // ========= Grant Actions =========
    val grantEvents: Flow<OnLocationAccessGranted>
    fun requestPermission(onGrant: OnLocationAccessGranted? = null)
    suspend fun onPermissionGranted()
}
