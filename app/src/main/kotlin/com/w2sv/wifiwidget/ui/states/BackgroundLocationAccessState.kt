package com.w2sv.wifiwidget.ui.states

import android.Manifest
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.w2sv.common.utils.log
import com.w2sv.composed.OnChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@Composable
fun rememberBackgroundLocationAccessState(scope: CoroutineScope = rememberCoroutineScope()): BackgroundLocationAccessState? {
    val permissionState = if (backgroundLocationAccessGrantRequired) {
        rememberPermissionState(permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else {
        null
    }

    return remember(scope) {
        permissionState?.let {
            BackgroundLocationAccessState(it, scope)
        }
    }
        .also {
            OnChange(it?.status?.isGranted) {
                it.log { "background access granted=$it" }
            }
        }
}

@Stable
class BackgroundLocationAccessState(
    private val permissionState: PermissionState,
    private val scope: CoroutineScope
) : PermissionState by permissionState {

    val isGranted by status::isGranted

    val showRational get() = _showRational.asSharedFlow()
    private val _showRational = MutableSharedFlow<Unit>()

    fun showRationalIfPermissionNotGranted() {
        if (!isGranted) {
            scope.launch {
                _showRational.emit(Unit)
            }
        }
    }
}

@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
private val backgroundLocationAccessGrantRequired: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
