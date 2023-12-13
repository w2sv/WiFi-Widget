package com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LocationAccessPermissionRationals(
    lapState: LocationAccessPermissionState,
) {
    if (lapState.showRational.collectAsStateWithLifecycle().value) {
        LocationAccessPermissionRational(
            onProceed = {
                lapState.onRationalShown()
            },
        )
    }
    if (lapState.showBackgroundAccessRational.collectAsStateWithLifecycle().value) {
        BackgroundLocationAccessRational(
            launchPermissionRequest = {
                lapState.launchBackgroundAccessPermissionRequest()
            },
            onDismissRequest = {
                lapState.dismissBackgroundAccessRational()
            },
        )
    }
}