package com.w2sv.wifiwidget.ui.states

import android.Manifest
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.w2sv.common.utils.log
import com.w2sv.composed.OnChange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun rememberBackgroundLocationAccessState(): BackgroundLocationAccessState? {
    val permissionState = if (backgroundLocationAccessGrantRequired) {
        rememberPermissionState(permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else {
        null
    }

    return remember {
        permissionState?.let {
            BackgroundLocationAccessState(it)
        }
    }
        .also {
            OnChange(it?.status?.isGranted) {
                it.log { "Background location access granted=$it" }
            }
        }
}

@Stable
class BackgroundLocationAccessState(private val permissionState: PermissionState) : PermissionState by permissionState {

    val showRational get() = _showRational.asStateFlow()
    private val _showRational = MutableStateFlow(false)

    fun showRationalIfPermissionNotGranted() {
        if (!status.isGranted) {
            _showRational.value = true
        }
    }

    fun hideRational() {
        _showRational.value = false
    }
}

@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
private val backgroundLocationAccessGrantRequired: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
