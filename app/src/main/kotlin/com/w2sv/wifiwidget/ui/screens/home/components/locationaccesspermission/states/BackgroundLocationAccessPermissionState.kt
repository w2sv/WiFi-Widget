package com.w2sv.wifiwidget.ui.screens.home.components.locationaccesspermission.states

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.runtime.Stable
import com.w2sv.androidutils.permissions.hasPermission
import com.w2sv.common.utils.trigger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Stable
class BackgroundLocationAccessPermissionState(private val scope: CoroutineScope) {

    // ===================
    // Rational
    // ===================

    val showRational get() = _showRational.asStateFlow()
    private val _showRational = MutableStateFlow(false)

    fun showRational(value: Boolean) {
        _showRational.value = value
    }

    // ===================
    // Requesting
    // ===================

    val launchRequest
        get() = _launchRequest.asSharedFlow()
    private val _launchRequest = MutableSharedFlow<Unit>()

    fun launchRequest() {
        scope.launch {
            _launchRequest.trigger()
        }
    }
}

@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
val backgroundLocationAccessGrantRequired: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

fun hasBackgroundLocationAccess(context: Context): Boolean =
    !backgroundLocationAccessGrantRequired || context.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)