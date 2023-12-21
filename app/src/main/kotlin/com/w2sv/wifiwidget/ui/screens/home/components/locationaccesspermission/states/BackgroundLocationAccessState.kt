package com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.runtime.Stable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Stable
class BackgroundLocationAccessState(
    private val permissionState: PermissionState,
    private val scope: CoroutineScope
) {
    val isGranted get() = permissionState.status.isGranted

    val showRational get() = _showRational.asSharedFlow()
    private val _showRational = MutableSharedFlow<Unit>()

    fun showRationalIfPermissionNotGranted() {
        if (!isGranted) {
            scope.launch {
                _showRational.emit(Unit)
            }
        }
    }

    // ===================
    // Requesting
    // ===================

    fun launchRequest() {
        permissionState.launchPermissionRequest()
    }
}

@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
val backgroundLocationAccessGrantRequired: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q